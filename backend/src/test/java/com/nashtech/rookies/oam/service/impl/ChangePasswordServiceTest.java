package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.dto.request.ChangePasswordRequest;
import com.nashtech.rookies.oam.exception.OldPasswordNotMatchException;
import com.nashtech.rookies.oam.exception.OldPasswordNullException;
import com.nashtech.rookies.oam.exception.PasswordUnchangedException;
import com.nashtech.rookies.oam.exception.UserNotFoundException;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String ENCODED_OLD_PASSWORD = "encodedOldPassword";
    private static final String ENCODED_NEW_PASSWORD = "encodedNewPassword";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";

    @InjectMocks
    private ChangePasswordServiceImpl changePasswordService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User user;
    private ChangePasswordRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setHashedPassword(ENCODED_OLD_PASSWORD);
        user.setStatus(UserStatus.ACTIVE);
        Role role = new Role();
        role.setName(RoleName.STAFF.getName());
        user.setRoles(new HashSet<>(Set.of(role)));

        request = new ChangePasswordRequest();
        request.setUserId(USER_ID);
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        // Setup default security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void whenUserNotFound_thenThrowsUserNotFoundException() {
        setupSecurityContext(USER_ID, RoleName.ADMIN);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> changePasswordService.changePassword(request));

        assertTrue(exception.getMessage().contains("User not found with id:"));
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void whenNonAdminChangesOtherUserPassword_thenThrowsAccessDeniedException() {
        request.setUserId(OTHER_USER_ID);
        setupSecurityContext(USER_ID, RoleName.STAFF);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> changePasswordService.changePassword(request));

        assertEquals("You are not allowed to change password for this user", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void whenAdminChangesOtherUserPassword_thenProceeds() {
        request.setUserId(OTHER_USER_ID);
        setupSecurityContext(USER_ID, RoleName.ADMIN);

        when(userRepository.findById(OTHER_USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);

        changePasswordService.changePassword(request);

        verify(userRepository).findById(OTHER_USER_ID);
        verify(userRepository).save(user);
    }

    @Test
    void whenActiveUserHasNullOldPassword_thenThrowsOldPasswordNullException() {
        request.setOldPassword(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        setupSecurityContext(USER_ID, RoleName.STAFF);

        OldPasswordNullException exception = assertThrows(OldPasswordNullException.class,
                () -> changePasswordService.changePassword(request));

        assertEquals("Old password must be provided if not first login", exception.getMessage());
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void whenActiveUserHasBlankOldPassword_thenThrowsOldPasswordNullException() {
        request.setOldPassword("  ");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        setupSecurityContext(USER_ID, RoleName.STAFF);

        OldPasswordNullException exception = assertThrows(OldPasswordNullException.class,
                () -> changePasswordService.changePassword(request));

        assertEquals("Old password must be provided if not first login", exception.getMessage());
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void whenOldPasswordDoesNotMatch_thenThrowsOldPasswordNotMatchException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);
        setupSecurityContext(USER_ID, RoleName.STAFF);

        OldPasswordNotMatchException exception = assertThrows(OldPasswordNotMatchException.class,
                () -> changePasswordService.changePassword(request));

        assertEquals("Password is incorrect", exception.getMessage());
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
    }

    @Test
    void whenNewPasswordSameAsOld_thenThrowsPasswordUnchangedException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);
        setupSecurityContext(USER_ID, RoleName.STAFF);

        PasswordUnchangedException exception = assertThrows(PasswordUnchangedException.class,
                () -> changePasswordService.changePassword(request));

        assertEquals("New password must be different from old password", exception.getMessage());
        verify(passwordEncoder, times(2)).matches(anyString(), eq(ENCODED_OLD_PASSWORD));
    }

    @Test
    void whenFirstLoginUserChangesPassword_thenSucceedsAndSetsActiveStatus() {
        user.setStatus(UserStatus.FIRST_LOGIN);
        request.setOldPassword(null); // First login doesn't require old password
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        setupSecurityContext(USER_ID, RoleName.STAFF);

        changePasswordService.changePassword(request);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(ENCODED_NEW_PASSWORD, user.getHashedPassword());
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    @Test
    void whenActiveUserChangesPassword_thenSucceeds() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        setupSecurityContext(USER_ID, RoleName.STAFF);

        changePasswordService.changePassword(request);

        assertEquals(ENCODED_NEW_PASSWORD, user.getHashedPassword());
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    private void setupSecurityContext(UUID userId, RoleName roleName) {
        User authUser = new User();
        authUser.setId(userId);
        Role role = new Role();
        role.setName(roleName.getName());
        authUser.setRoles(new HashSet<>(Set.of(role)));


        CustomUserDetails userDetails = new CustomUserDetails(authUser);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> ("ROLE_"+roleName.getName()));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
    }
}
