package com.nashtech.rookies.oam.config;

import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentSecurity Unit Tests")
class AssignmentSecurityTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AssignmentSecurity assignmentSecurity;

    private User mockUser;
    private Assignment mockAssignment;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setStaffCode("SD0001");
        mockUser.setUsername("testuser");

        assignmentId = UUID.randomUUID();
        mockAssignment = new Assignment();
        mockAssignment.setId(assignmentId);
        mockAssignment.setUser(mockUser);
    }

    @Test
    @DisplayName("Should return true when current user is assigned to the assignment")
    void isCurrentUserAssigned_ShouldReturnTrue_WhenUserIsAssigned() {
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(mockAssignment));

        boolean result = assignmentSecurity.isCurrentUserAssigned(assignmentId);

        assertTrue(result);
        verify(authService).getAuthenticatedUser();
        verify(assignmentRepository).findById(assignmentId);
    }

    @Test
    @DisplayName("Should return false when current user is not assigned to the assignment")
    void isCurrentUserAssigned_ShouldReturnFalse_WhenUserIsNotAssigned() {
        User differentUser = new User();
        differentUser.setId(UUID.randomUUID());
        differentUser.setStaffCode("SD0002");
        differentUser.setUsername("differentuser");

        when(authService.getAuthenticatedUser()).thenReturn(differentUser);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(mockAssignment));

        boolean result = assignmentSecurity.isCurrentUserAssigned(assignmentId);

        assertFalse(result);
        verify(authService).getAuthenticatedUser();
        verify(assignmentRepository).findById(assignmentId);
    }

    @Test
    @DisplayName("Should return false when assignment is not found")
    void isCurrentUserAssigned_ShouldReturnFalse_WhenAssignmentNotFound() {
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        boolean result = assignmentSecurity.isCurrentUserAssigned(assignmentId);

        assertFalse(result);
        verify(authService).getAuthenticatedUser();
        verify(assignmentRepository).findById(assignmentId);
    }
}