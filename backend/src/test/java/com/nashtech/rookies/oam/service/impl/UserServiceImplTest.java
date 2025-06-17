package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.CurrentUserResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.exception.InternalErrorException;
import com.nashtech.rookies.oam.exception.ResourceNotFoundException;
import com.nashtech.rookies.oam.exception.RoleNotFoundException;
import com.nashtech.rookies.oam.mapper.UserMapper;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.LocationRepository;
import com.nashtech.rookies.oam.repository.RoleRepository;
import com.nashtech.rookies.oam.repository.UserRepository;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.StaffCodeGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StaffCodeGeneratorService staffCodeGeneratorService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserRequestDto userRequestDto;
    private User authenticatedUser;
    private Role role;
    private Location location;
    private User mappedUser;
    private UserResponseDto expectedResponse;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setFirstName("John");
        userRequestDto.setLastName("Doe");
        userRequestDto.setType("ADMIN");
        userRequestDto.setGender("MALE");
        userRequestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userRequestDto.setLocationCode("HCM");

        authenticatedUser = User.builder()
                .username("admin")
                .location(Location.builder().code("HCM").build())
                .build();

        role = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();

        location = Location.builder()
                .id(UUID.randomUUID())
                .code("HCM")
                .build();

        mappedUser = User.builder().build();

        expectedResponse = new UserResponseDto();
        expectedResponse.setUsername("johnd");
    }

    @Test
    void createUser_WithValidData_ShouldCreateUserSuccessfully() {
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(locationRepository.findByCode("HCM")).thenReturn(Optional.of(location));
        when(staffCodeGeneratorService.generateStaffCode()).thenReturn("SD0001");
        when(userRepository.countByUsernameStartingWith("johnd")).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User userBeforeSave = User.builder().build();
        when(userMapper.toEntity(userRequestDto)).thenReturn(userBeforeSave);

        User savedUser = User.builder()
                .username("johnd")
                .staffCode("SD0001")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.createUser(userRequestDto);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("johnd", capturedUser.getUsername());
        assertEquals("SD0001", capturedUser.getStaffCode());
        assertEquals(Gender.MALE, capturedUser.getGender());
        assertEquals(UserStatus.FIRST_LOGIN, capturedUser.getStatus());
        assertEquals(location, capturedUser.getLocation());
        assertEquals(Set.of(role), capturedUser.getRoles());
        verify(passwordEncoder).encode("johnd@01011990");
        assertEquals(expectedResponse, result);
    }

    @Test
    void createUser_WithNonAdminRole_ShouldUseCreatorLocation() {
        userRequestDto.setType("STAFF");

        Role staffRole = Role.builder()
                .id(UUID.randomUUID())
                .name("STAFF")
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staffRole));
        when(staffCodeGeneratorService.generateStaffCode()).thenReturn("SD0001");
        when(userRepository.countByUsernameStartingWith("johnd")).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User userBeforeSave = User.builder().build();
        when(userMapper.toEntity(userRequestDto)).thenReturn(userBeforeSave);

        User savedUser = User.builder()
                .username("johnd")
                .staffCode("SD0001")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        userService.createUser(userRequestDto);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(authenticatedUser.getLocation(), capturedUser.getLocation());
        verify(locationRepository, never()).findByCode(anyString());
    }

    @Test
    void createUser_WithExistingUsername_ShouldAppendCounter() {
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(locationRepository.findByCode("HCM")).thenReturn(Optional.of(location));
        when(staffCodeGeneratorService.generateStaffCode()).thenReturn("SD0001");
        when(userRepository.countByUsernameStartingWith("johnd")).thenReturn(1L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User userBeforeSave = User.builder().build();
        when(userMapper.toEntity(userRequestDto)).thenReturn(userBeforeSave);

        User savedUser = User.builder()
                .username("johnd1")
                .staffCode("SD0001")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        userService.createUser(userRequestDto);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("johnd1", capturedUser.getUsername());
        verify(passwordEncoder).encode("johnd1@01011990");
    }

    @Test
    void createUser_WithInvalidRole_ShouldThrowResourceNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
                () -> userService.createUser(userRequestDto));

        assertEquals(ErrorCode.ROLE_NOT_FOUND.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidLocation_ShouldThrowResourceNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(locationRepository.findByCode("HCM")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.createUser(userRequestDto));

        assertEquals(ErrorCode.LOCATION_NOT_FOUND.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void generateDefaultPassword_ShouldGenerateCorrectFormat() {

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(locationRepository.findByCode("HCM")).thenReturn(Optional.of(location));
        when(staffCodeGeneratorService.generateStaffCode()).thenReturn("SD0001");
        when(userRepository.countByUsernameStartingWith("johnd")).thenReturn(0L);

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        when(passwordEncoder.encode(passwordCaptor.capture())).thenReturn("encodedPassword");

        User userBeforeSave = User.builder().build();
        when(userMapper.toEntity(userRequestDto)).thenReturn(userBeforeSave);

        when(userRepository.save(any(User.class))).thenReturn(mappedUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        userService.createUser(userRequestDto);

        String capturedPassword = passwordCaptor.getValue();
        String expectedDateFormat = userRequestDto.getDateOfBirth()
                .format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String expectedPassword = "johnd@" + expectedDateFormat;

        assertEquals(expectedPassword, capturedPassword);
    }

    @Test
    void getUsers_ShouldReturnPagedUsersSuccessfully() {
        int pageNo = 0;
        int pageSize = 20;
        String search = "john";
        String sortField = "firstName";
        String sortOrder = "ASC";
        List<String> roles = List.of("ADMIN");

        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .location(Location.builder().id(UUID.randomUUID()).build())
                .build();

        // Mock authenticated user from AuthService
        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        // Mock RoleRepository to resolve roles (simulate validateAndResolveRoles)
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));


        // Build Specification and Pageable is created internally, we mock the userRepository call
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "firstName"));

        // Create dummy User and mapped DTO
        User user = User.builder().id(UUID.randomUUID()).build();
        UserPageResponseDto dto = new UserPageResponseDto();
        dto.setStaffCode("SC001");
        dto.setFullName("John Doe");

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserPageResponseDto(user)).thenReturn(dto);

        APIPageableResponseDTO<UserPageResponseDto> response = userService.getUsers(
                pageNo, pageSize, search, sortField, sortOrder, roles, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("SC001", response.getContent().get(0).getStaffCode());

        // Verify the repository was called with specification and pageable
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(authService).getAuthenticatedUser();
    }

    @SuppressWarnings("unchecked")
    private static <T> Specification<T> anySpecification() {
        return (Specification<T>) any(Specification.class);
    }

    @Test
    void getAllUser_WithNullOrEmptyRoles_ShouldResolveAllRoles() {
        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .location(Location.builder().id(UUID.randomUUID()).build())
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        List<Role> allRoles = List.of(
                Role.builder().name("ADMIN").build(),
                Role.builder().name("STAFF").build()
        );
        when(roleRepository.findAll()).thenReturn(allRoles);

        Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName").descending());
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(anySpecification(), any(Pageable.class))).thenReturn(emptyPage);

        APIPageableResponseDTO<UserPageResponseDto> response = userService.getUsers(
                0, 20, "", "lastName", "DESC", null, null);

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(roleRepository).findAll();
    }

    @Test
    void getUsersHasNoLocation_ShouldThrowException() {
        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .location(null)  // No location assigned
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        InternalErrorException ex = assertThrows(InternalErrorException.class,
                () -> userService.getUsers(0, 20, null, null, null, List.of("ADMIN"),null));

        assertEquals(ErrorCode.LOCATION_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getUsers_ShouldBuildSortForFullNameField() {
        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .location(Location.builder().id(UUID.randomUUID()).build())
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "firstName"));
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        APIPageableResponseDTO<UserPageResponseDto> response = userService.getUsers(
                0, 20, null, "fullName", "DESC", List.of("ADMIN"),null);

        assertNotNull(response);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUserResponseDtoSuccessfully() {
        UUID userId = UUID.randomUUID();
        String username = "johndoe";
        List<Role> roles = List.of(
                Role.builder().name("STAFF").build(),
                Role.builder().name("ADMIN").build()
        );

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .roles(Set.copyOf(roles))
                .status(UserStatus.ACTIVE)
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);

        CurrentUserResponseDto result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(username, result.getUsername());
        assertEquals(Set.of("STAFF", "ADMIN"), new HashSet<>(result.getRoles()));

    }
}