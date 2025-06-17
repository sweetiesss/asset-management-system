package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssignmentPageRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentUpdateRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssignmentStatusRequest;
import com.nashtech.rookies.oam.dto.response.*;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.service.AssetReturnService;
import com.nashtech.rookies.oam.service.AssignmentService;
import com.nashtech.rookies.oam.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.nashtech.rookies.oam.constant.SortConstants.DEFAULT_ASSIGNMENT_LIST_SORT_FIELD;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Assignments", description = "Endpoints for retrieving and managing assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final AssetReturnService assetReturnService;

    @PostMapping
    @Operation(summary = "Create a new assignment", responses = {
            @ApiResponse(responseCode = "201", description = "Asset created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<AssignmentResponse>> createAssignment(
            @RequestBody @Valid AssignmentRequest assignmentRequest) {
        AssignmentResponse assignmentResponse = assignmentService.createAssignment(assignmentRequest);

        ApiGenericResponse<AssignmentResponse> body = ApiResult.success(
                "Assignment created successfully",
                assignmentResponse);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(assignmentResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}/edit-view")
    @Operation(summary = "Get assignment edit view", responses = {
            @ApiResponse(responseCode = "200", description = "Assignment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<AssignmentEditViewResponse>> getAssignmentEditView(
            @PathVariable UUID id) {
        AssignmentEditViewResponse assignmentResponse = assignmentService.getAssignmentEditView(id);

        ApiGenericResponse<AssignmentEditViewResponse> body = ApiResult.success(
                "Assignment retrieved successfully",
                assignmentResponse);

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing assignment", responses = {
            @ApiResponse(responseCode = "200", description = "Assignment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<AssignmentResponse>> updateAssignment(
            @PathVariable UUID id,
            @RequestBody @Valid AssignmentUpdateRequest assignmentRequest) throws BadRequestException {
        AssignmentResponse assignmentResponse = assignmentService.updateAssignment(id, assignmentRequest);

        ApiGenericResponse<AssignmentResponse> body = ApiResult.success(
                "Assignment updated successfully",
                assignmentResponse);

        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiGenericResponse<AssignmentResponse>> updateAssignmentStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateAssignmentStatusRequest request) {
        AssignmentResponse assignmentResponse = assignmentService.updateAssignmentStatus(id, request);

        ApiGenericResponse<AssignmentResponse> body = ApiResult.success(
                "Assignment status updated successfully",
                assignmentResponse);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<AssignmentPageResponse>>> getAssignments(
            @Valid @ModelAttribute AssignmentPageRequest request) {
        if (StringUtils.isBlank(request.getSort())) {
            request.setSort(DEFAULT_ASSIGNMENT_LIST_SORT_FIELD);
        }

        return ResponseUtil.success(
                "Assignment list retrieved successfully",
                assignmentService.getAssignments(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiGenericResponse<AssignmentDetailResponse>> getAssignmentDetail(@PathVariable UUID id) {
        ApiGenericResponse<AssignmentDetailResponse> body = ApiResult.success(
                "Assignment's detail retrieved successfully",
                assignmentService.getAssignmentDetail(id));
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an assignment", responses = {
            @ApiResponse(responseCode = "200", description = "Assignment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<Void>> deleteAssignment(@PathVariable UUID id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResult.success("Assignment deleted successfully", null));
    }

    @PostMapping("/{assignmentId}/asset-returns")
    @Operation(summary = "Create a return request for an assignment")
    public ResponseEntity<ApiGenericResponse<AssetReturnResponse>> createReturnRequest(
            @PathVariable UUID assignmentId) {

        AssetReturnResponse response = assetReturnService.createAssetReturn(assignmentId);

        ApiGenericResponse<AssetReturnResponse> body = ApiResult.success("Return request created", response);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/assignment-statuses")
    @Operation(summary = "Get all assignment statuses", responses = {
            @ApiResponse(responseCode = "200", description = "Assignment statuses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<List<AssignmentStatus>>> getAllAssignmentStatuses() {
        List<AssignmentStatus> assignmentStatuses = assignmentService.getAllAssignmentStatus();
        ApiGenericResponse<List<AssignmentStatus>> body = ApiResult.success(
                "Assignment statuses retrieved successfully",
                assignmentStatuses);
        return ResponseEntity.ok(body);
    }

}
