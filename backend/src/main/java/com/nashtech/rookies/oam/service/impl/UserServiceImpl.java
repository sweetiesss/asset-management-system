package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.CurrentUserResponseDto;
import com.nashtech.rookies.oam.dto.response.UserDetailResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.UserMapper;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.LocationRepository;
import com.nashtech.rookies.oam.repository.RoleRepository;
import com.nashtech.rookies.oam.repository.UserRepository;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.StaffCodeGeneratorService;
import com.nashtech.rookies.oam.service.UserService;
import com.nashtech.rookies.oam.specification.UserSpecification;
import com.nashtech.rookies.oam.util.EnumValidationUtils;
import com.nashtech.rookies.oam.util.SortUtil;
import com.nashtech.rookies.oam.util.StringUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_DATE_FORMAT;
import static com.nashtech.rookies.oam.constant.UserConstants.DEFAULT_PASSWORD_FORMAT;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    LocationRepository locationRepository;
    AuthService authService;
    PasswordEncoder passwordEncoder;
    StaffCodeGeneratorService staffCodeGeneratorService;
    UserMapper userMapper;
    AssignmentRepository assignmentRepository;

    static final String DISABLE_USER_ERROR_MESSAGE = "There are valid assignments belonging to this user. " +
            "Please close all assignments before disabling user.";

    static final String INVALID_USER_STATUS_ERROR = "Allowed values are: ACTIVE, INACTIVE, FIRST_LOGIN";

    static final List<UserStatus> DEFAULT_GET_USERS_STATUS = List.of(
            UserStatus.ACTIVE,
            UserStatus.FIRST_LOGIN);

    static final List<String> ASSIGNMENT_STATUSES_PREVENTING_USER_DISABLE = List.of(
            AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName(),
            AssignmentStatusType.ACCEPTED.getDbName());

    @Transactional
    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User userAction = authService.getAuthenticatedUser();
        log.info("User {} is creating a new user", userAction.getUsername());

        Role role = findRoleByName(userRequestDto.getType());
        Location location = resolveLocation(role, userRequestDto.getLocationCode(), userAction);

        String firstName = compactName(userRequestDto.getFirstName());
        String lastName = normalizedName(userRequestDto.getLastName());

        String username = generateUsername(firstName, lastName);
        String staffCode = staffCodeGeneratorService.generateStaffCode();
        String password = generateDefaultPassword(username, userRequestDto.getDateOfBirth());
        Gender gender = Gender.valueOf(userRequestDto.getGender().toUpperCase());

        User user = userMapper.toEntity(userRequestDto).toBuilder()
                .firstName(firstName)
                .lastName(lastName)
                .gender(gender)
                .staffCode(staffCode)
                .username(username)
                .hashedPassword(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .status(UserStatus.FIRST_LOGIN)
                .location(location)
                .build();

        userRepository.save(user);
        log.info("User created with username: {}", username);

        return userMapper.toResponseDto(user);
    }

    @Override
    public UserDetailResponseDto getUserById(String id){
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
        return userMapper.toUserDetailResponseDto(user);
    }

    @Override
    public CurrentUserResponseDto getCurrentUser(){
        User currentUser = authService.getAuthenticatedUser();
        return CurrentUserResponseDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .roles(currentUser.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .changePasswordRequired(currentUser.getStatus().isFirstLogin())
                .build();
    }

    @Override
    public APIPageableResponseDTO<UserPageResponseDto> getUsers(
            int pageNo,
            int pageSize,
            String search,
            String sortField,
            String sortOrder,
            List<String> roles,
            List<String> status
    ) {
        User currentUser = authService.getAuthenticatedUser();
        UUID locationId = getCurrentUserLocationId(currentUser);
        Sort sort = SortUtil.buildUserSort(sortField, sortOrder);
        List<String> resolvedRoles = validateAndResolveRoles(roles);

        List<UserStatus> resolvedStatus = CollectionUtils.isEmpty(status)
                ? DEFAULT_GET_USERS_STATUS
                : resolveUserStatuses(status);

        Pageable pageable = PageRequest.of(Math.max(pageNo, 0), Math.max(pageSize, 1), sort);

        Specification<User> spec = UserSpecification.build(
                search,
                resolvedRoles,
                currentUser.getId(),
                locationId,
                sortField,
                resolvedStatus
        );

        Page<User> userPage = userRepository.findAll(spec, pageable);
        Page<UserPageResponseDto> dtoPage = userPage.map(userMapper::toUserPageResponseDto);

        return new APIPageableResponseDTO<>(dtoPage);
    }

    private List<String> validateAndResolveRoles(List<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return roleRepository.findAll().stream()
                    .map(Role::getName)
                    .toList();
        }

        return roles.stream()
                .map(role -> findRoleByName(role).getName())
                .toList();
    }

    private UUID getCurrentUserLocationId(User currentUser) {
        return Optional.ofNullable(currentUser.getLocation())
                .map(Location::getId)
                .orElseThrow(() -> new InternalErrorException(ErrorCode.LOCATION_NOT_FOUND.getMessage(), null));
    }
    @Override
    @Transactional
    public UserResponseDto updateUser(EditUserRequest editUserRequest, UUID userId) {
        User userAction = authService.getAuthenticatedUser();
        log.info("User {} is editing a user", userAction.getUsername());

        User user = findUserById(userId);

        if (StringUtils.hasText(editUserRequest.getType()))
        {
            Role role = findRoleByName(editUserRequest.getType());
            updateUserRole(user, role);
        }

        if(StringUtils.hasText(editUserRequest.getLocationCode())){
            Location location = findLocationByCode(editUserRequest.getLocationCode());
            user.setLocation(location);
        }

        if (StringUtils.hasText(editUserRequest.getStatus())){

            UserStatus editStatus=UserStatus.valueOf(editUserRequest.getStatus().toUpperCase());

            if (editStatus.isInActive())
                checkUserCanBeDisable(userId);
        }

        if (StringUtils.hasText(editUserRequest.getGender())) {
            user.setGender(Gender.valueOf(editUserRequest.getGender().toUpperCase()));
        }

        try {
            User updatedUser = user.toBuilder()
                    .version(editUserRequest.getVersion())
                    .build();

            userRepository.save(updatedUser);
            return userMapper.toResponseDto(updatedUser);
        } catch (OptimisticLockingFailureException e) {
            log.error("User with id {} is being modified by another transaction", userId);
            throw new UserBeingModifiedException(ErrorCode.USER_BEING_MODIFIED.getMessage());
        }

    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    private List<UserStatus> resolveUserStatuses(List<String> status) {

        List<String> invalidStatuses = status.stream()
                .filter(s -> !EnumValidationUtils.isValidEnum(s, UserStatus.class))
                .toList();

        if (!invalidStatuses.isEmpty())
            throw new IllegalArgumentException(INVALID_USER_STATUS_ERROR);

        return status.stream()
                .map(s -> UserStatus.valueOf(s.toUpperCase().trim()))
                .toList();
    }

    private void checkUserCanBeDisable(UUID userId) {
        if (assignmentRepository.hasAssignmentsWithStatuses(userId, ASSIGNMENT_STATUSES_PREVENTING_USER_DISABLE))
            throw new DisableUserException(DISABLE_USER_ERROR_MESSAGE);
    }

    private Location findLocationByCode(String locationCode) {
        return locationRepository.findByCode(locationCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.LOCATION_NOT_FOUND.getMessage()));
    }
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
    }
    private void updateUserRole(User user, Role role) {
        user.getRoles().clear();
        user.getRoles().add(role);
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new RoleNotFoundException(ErrorCode.ROLE_NOT_FOUND.getMessage()));
    }

    private Location resolveLocation(Role role, String locationCode, User creator) {
        if (RoleName.ADMIN.getName().equals(role.getName())) {
            return locationRepository.findByCode(locationCode.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.LOCATION_NOT_FOUND.getMessage()));
        }
        return creator.getLocation();
    }

    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + StringUtil.getInitialsFromWords(lastName).toLowerCase();
        long count = userRepository.countByUsernameStartingWith(baseUsername);
        return (count == 0) ? baseUsername : baseUsername + count;
    }

    private String generateDefaultPassword(String username, LocalDate dateOfBirth) {
        String formattedDate = dateOfBirth.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
        return String.format(DEFAULT_PASSWORD_FORMAT, username, formattedDate);
    }

    private String normalizedName(String name) {
        return StringUtil.capitalizeWords(StringUtil.normalizeWhitespace(name));
    }

    private String compactName(String firstName) {
        String normalizedFirstName = normalizedName(firstName);
        return StringUtil.removeAllWhitespace(normalizedFirstName);
    }

}
