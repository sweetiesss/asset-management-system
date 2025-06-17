package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setHashedPassword("password123");

        Role role = new Role();
        role.setName("ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        testUser.setRoles(roles);
    }

    @Test
    void testLoadUserByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        CustomUserDetails result = (CustomUserDetails) userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN")));

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("missinguser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("missinguser");
        });

        verify(userRepository, times(1)).findByUsername("missinguser");
    }
}
