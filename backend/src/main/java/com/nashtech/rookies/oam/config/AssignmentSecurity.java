package com.nashtech.rookies.oam.config;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.AssignmentNotFoundException;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentSecurity {
    private final AssignmentRepository assignmentRepository;
    private final AuthService authService;

    public boolean isCurrentUserAssigned(UUID id) {
        User user = authService.getAuthenticatedUser();

        try {
            Assignment assignment = assignmentRepository.findById(id)
                    .orElseThrow(() -> new AssignmentNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));
            return assignment.getUser().equals(user);
        } catch (AssignmentNotFoundException e) {
            return false;
        }
    }
}
