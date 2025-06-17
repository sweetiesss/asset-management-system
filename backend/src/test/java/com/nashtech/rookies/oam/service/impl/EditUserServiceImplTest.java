package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.exception.RoleNotFoundException;
import com.nashtech.rookies.oam.exception.UserNotFoundException;
import com.nashtech.rookies.oam.mapper.UserMapper;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.LocationRepository;
import com.nashtech.rookies.oam.repository.RoleRepository;
import com.nashtech.rookies.oam.repository.UserRepository;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.StaffCodeGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EditUserServiceImplTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userServiceImpl;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StaffCodeGeneratorService staffCodeGeneratorService;
    @Mock
    private AssignmentRepository assignmentRepository;
    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        userRepository = mock(UserRepository.class);
        authService = mock(AuthService.class);
        userMapper = mock(UserMapper.class);
        userServiceImpl = new UserServiceImpl(userRepository, roleRepository, locationRepository, authService, passwordEncoder,staffCodeGeneratorService, userMapper, assignmentRepository);
    }

    @Test
    @DisplayName("Should edit user successfully when all inputs are valid")
    void shouldEditUserSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        EditUserRequest request = new EditUserRequest();
        request.setGender("MALE");
        request.setType("STAFF");
        request.setJoinedOn(LocalDate.of(2023, 5, 15));
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setStatus("INACTIVE");

        Role staffRole = new Role();
        staffRole.setName(RoleName.STAFF.getName());

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("john.doe");
        existingUser.setRoles(new HashSet<>(Set.of(staffRole)));
        existingUser.setStatus(UserStatus.ACTIVE);


        User authenticatedUser = new User();
        authenticatedUser.setUsername("admin");

        User updatedUserEntity = new User(); // entity passed to save()
        User savedUser = new User(); // entity returned by save()

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId.toString());
        responseDto.setStatus(UserStatus.INACTIVE);

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staffRole));
        when(userRepository.save(updatedUserEntity)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userServiceImpl.updateUser(request, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId.toString());
        assertThat(existingUser.getRoles()).contains(staffRole);
        assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);

        verify(authService).getAuthenticatedUser();
        verify(userRepository).findById(userId);
        verify(roleRepository).findByName("STAFF");
        verify(userRepository).save(updatedUserEntity);
        verify(userMapper).toResponseDto(savedUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found")
    void shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        EditUserRequest request = new EditUserRequest();
        request.setGender("MALE");
        request.setType("STAFF");

        when(authService.getAuthenticatedUser()).thenReturn(new User());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.updateUser(request, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when role is not found")
    void shouldThrowWhenRoleNotFound() {
        UUID userId = UUID.randomUUID();
        EditUserRequest request = new EditUserRequest();
        request.setGender("MALE");
        request.setType("ADMIN");

        User existingUser = new User();
        existingUser.setId(userId);

        when(authService.getAuthenticatedUser()).thenReturn(new User());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.updateUser(request, userId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("Role not found");
    }
}
