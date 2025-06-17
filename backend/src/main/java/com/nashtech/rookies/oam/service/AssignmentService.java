package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssignmentPageRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentUpdateRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssignmentStatusRequest;
import com.nashtech.rookies.oam.dto.response.AssignmentDetailResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentEditViewResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentPageResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentResponse;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {
    AssignmentResponse createAssignment(AssignmentRequest request);
    APIPageableResponseDTO<AssignmentPageResponse> getAssignments(AssignmentPageRequest request);

    AssignmentEditViewResponse getAssignmentEditView(UUID id);

    AssignmentResponse updateAssignment(UUID id, AssignmentUpdateRequest assignmentRequest) throws BadRequestException;
    AssignmentResponse updateAssignmentStatus(UUID id, UpdateAssignmentStatusRequest request);

    AssignmentDetailResponse getAssignmentDetail(UUID id);

    Assignment getAssignmentById(UUID id);

    void deleteAssignment(UUID id);

    List<AssignmentStatus> getAllAssignmentStatus();
}
