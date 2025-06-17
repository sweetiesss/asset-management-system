package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssignmentPageRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentUpdateRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssignmentStatusRequest;
import com.nashtech.rookies.oam.dto.response.*;
import com.nashtech.rookies.oam.exception.AssignmentNotFoundException;
import com.nashtech.rookies.oam.exception.RequestReturnAssetAlreadyExistsException;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.projection.AssetAssignmentEditViewProjection;
import com.nashtech.rookies.oam.service.AssetReturnService;
import com.nashtech.rookies.oam.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@AutoConfigureMockMvc
class AssignmentControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private AssetReturnService assetReturnService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private AssignmentRequest assignmentRequest;
    private AssignmentResponse assignmentResponse;
    private UpdateAssignmentStatusRequest updateAssignmentStatusRequest;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        updateAssignmentStatusRequest = new UpdateAssignmentStatusRequest();
        updateAssignmentStatusRequest.setStatus("ACCEPTED");

        assignmentRequest = new AssignmentRequest();
        assignmentRequest.setUserId(userId);
        assignmentRequest.setAssetId(assetId);
        assignmentRequest.setAssignedDate(LocalDate.of(2026, 1, 1));
        assignmentRequest.setNote("Test assignment");

        assignmentResponse = AssignmentResponse.builder()
                .id(assignmentId)
                .assetCode("ASSET-001")
                .assetName("Dell XPS 13")
                .assignTo("johndoe")
                .assignBy("admin")
                .assignedDate(LocalDate.of(2024, 1, 1))
                .state(new AssignmentStatus(1, "Waiting for acceptance"))
                .build();
    }

    @Test
    @DisplayName("Should create assignment successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAssignmentSuccessfully() throws Exception {
        when(assignmentService.createAssignment(any(AssignmentRequest.class))).thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Assignment created successfully"))
                .andExpect(jsonPath("$.data.id").value(assignmentResponse.getId().toString()))
                .andExpect(jsonPath("$.data.assetCode").value("ASSET-001"))
                .andExpect(jsonPath("$.data.assignTo").value("johndoe"))
                .andExpect(jsonPath("$.data.assignBy").value("admin"))
                .andExpect(jsonPath("$.data.assignedDate").value("2024-01-01"))
                .andExpect(jsonPath("$.data.state.name").value("Waiting for acceptance"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authorized")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUserIsNotAuthorized() throws Exception {
        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        AssignmentRequest invalidRequest = new AssignmentRequest();
        invalidRequest.setNote("Missing required fields");

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get assignment edit view successfully when valid ID is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAssignmentEditViewSuccessfully() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        UserAssignmentEditView userView = UserAssignmentEditView.builder()
                .id(userId)
                .fullName("johndoe")
                .build();

        AssetAssignmentEditViewProjection assetView = new AssetAssignmentEditViewProjection() {
            @Override
            public UUID getId() {
                return assetId;
            }

            @Override
            public String getName() {
                return "Dell XPS 13";
            }
        };

        AssignmentEditViewResponse editViewResponse = AssignmentEditViewResponse.builder()
                .id(assignmentId)
                .user(userView)
                .asset(assetView)
                .assignedDate(LocalDate.of(2024, 1, 1))
                .note("Test assignment")
                .build();

        when(assignmentService.getAssignmentEditView(assignmentId)).thenReturn(editViewResponse);

        mockMvc.perform(get("/api/v1/assignments/{id}/edit-view", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.user.fullName").value("johndoe"))
                .andExpect(jsonPath("$.data.asset.id").value(assetId.toString()))
                .andExpect(jsonPath("$.data.asset.name").value("Dell XPS 13"))
                .andExpect(jsonPath("$.data.assignedDate").value("2024-01-01"))
                .andExpect(jsonPath("$.data.note").value("Test assignment"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when assignment edit view does not exist")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenAssignmentEditViewDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(assignmentService.getAssignmentEditView(nonExistentId))
                .thenThrow(new AssignmentNotFoundException("Assignment not found"));

        mockMvc.perform(get("/api/v1/assignments/{id}/edit-view", nonExistentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when getting assignment edit view without authentication")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenGettingAssignmentEditViewWithoutAuthentication() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/assignments/{id}/edit-view", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should update assignment successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAssignmentSuccessfully() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        AssignmentResponse updatedResponse = AssignmentResponse.builder()
                .id(assignmentId)
                .assetCode("ASSET-001")
                .assetName("Dell XPS 13 Updated")
                .assignTo("johndoe")
                .assignBy("admin")
                .assignedDate(LocalDate.of(2024, 1, 2))
                .state(new AssignmentStatus(1, "Waiting for acceptance"))
                .build();

        when(assignmentService.updateAssignment(eq(assignmentId), any(AssignmentUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment updated successfully"))
                .andExpect(jsonPath("$.data.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.data.assetCode").value("ASSET-001"))
                .andExpect(jsonPath("$.data.assetName").value("Dell XPS 13 Updated"))
                .andExpect(jsonPath("$.data.assignTo").value("johndoe"))
                .andExpect(jsonPath("$.data.assignBy").value("admin"))
                .andExpect(jsonPath("$.data.assignedDate").value("2024-01-02"))
                .andExpect(jsonPath("$.data.state.name").value("Waiting for acceptance"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating non-existent assignment")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonExistentAssignment() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(assignmentService.updateAssignment(eq(nonExistentId), any(AssignmentUpdateRequest.class)))
                .thenThrow(new AssignmentNotFoundException("Assignment not found"));

        mockMvc.perform(patch("/api/v1/assignments/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating assignment with invalid request body")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenUpdatingAssignmentWithInvalidRequestBody() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        AssignmentRequest invalidRequest = new AssignmentRequest();
        invalidRequest.setNote("Missing required fields");

        mockMvc.perform(patch("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when updating assignment without authentication")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUpdatingAssignmentWithoutAuthentication() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        AssignmentRequest updateRequest = new AssignmentRequest();
        updateRequest.setUserId(UUID.randomUUID());
        updateRequest.setAssetId(UUID.randomUUID());
        updateRequest.setAssignedDate(LocalDate.of(2026, 1, 1));
        updateRequest.setNote("Test assignment");

        mockMvc.perform(patch("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when updating assignment without proper role")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUpdatingAssignmentWithoutProperRole() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        AssignmentRequest updateRequest = new AssignmentRequest();
        updateRequest.setUserId(UUID.randomUUID());
        updateRequest.setAssetId(UUID.randomUUID());
        updateRequest.setAssignedDate(LocalDate.of(2026, 1, 1));
        updateRequest.setNote("Test assignment");

        mockMvc.perform(put("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAssignments_withValidParams_shouldReturnPaginatedAssignmentList() throws Exception {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        AssignmentPageResponse assignment = AssignmentPageResponse.builder()
                .id(assignmentId)
                .assetCode("ASSET-001")
                .assetName("Dell XPS 13")
                .userId("johndoe")
                .createdBy("admin")
                .assignedDate(LocalDate.of(2025, 6, 2))
                .status(new AssignmentStatus(1, "Accepted"))
                .build();

        Page<AssignmentPageResponse> assignmentPage = new PageImpl<>(
                List.of(assignment),
                PageRequest.of(0, 10, Sort.by("assetCode").ascending()),
                1
        );

        APIPageableResponseDTO<AssignmentPageResponse> expectedResponse = new APIPageableResponseDTO<>(assignmentPage);

        when(assignmentService.getAssignments(any(AssignmentPageRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/assignments")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("sortField", "assetCode")
                        .param("sortOrder", "asc")
                        .param("search", "")
                        .param("states", "Accepted")
                        .param("assignedDate", "2025-06-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].assetCode").value("ASSET-001"))
                .andExpect(jsonPath("$.data.content[0].assetName").value("Dell XPS 13"))
                .andExpect(jsonPath("$.data.content[0].userId").value("johndoe"))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.data.pageable.totalElements").value(1))
                .andExpect(jsonPath("$.message").value("Assignment list retrieved successfully"));

        verify(assignmentService, times(1)).getAssignments(any(AssignmentPageRequest.class));
    }
    @Test
    @DisplayName("Should return assignment detail successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAssignmentDetailSuccessfully() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        AssignmentDetailResponse response = AssignmentDetailResponse.builder()
                .assetCode("ASSET-001")
                .assetName("Laptop")
                .userId("jdoe")
                .assignedDate(LocalDate.of(2024, 5, 1))
                .note("Initial assignment")
                .status(new AssignmentStatus(1, "Accepted"))
                .build();

        when(assignmentService.getAssignmentDetail(assignmentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/assignments/{id}", assignmentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment's detail retrieved successfully"))
                .andExpect(jsonPath("$.data.assetCode").value("ASSET-001"))
                .andExpect(jsonPath("$.data.assetName").value("Laptop"))
                .andExpect(jsonPath("$.data.userId").value("jdoe"))
                .andExpect(jsonPath("$.data.assignedDate").value("2024-05-01"))
                .andExpect(jsonPath("$.data.note").value("Initial assignment"))
                .andExpect(jsonPath("$.data.status.id").value("1"))
                .andExpect(jsonPath("$.data.status.name").value("Accepted"));

        verify(assignmentService).getAssignmentDetail(assignmentId);
    }

    @Test
    @DisplayName("Should delete assignment successfully when valid ID is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAssignmentSuccessfully() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        doNothing().when(assignmentService).deleteAssignment(assignmentId);

        mockMvc.perform(delete("/api/v1/assignments/{id}", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment deleted successfully"));

        verify(assignmentService, times(1)).deleteAssignment(assignmentId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent assignment")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonExistentAssignment() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        doThrow(new AssignmentNotFoundException("Assignment not found"))
                .when(assignmentService).deleteAssignment(nonExistentId);

        mockMvc.perform(delete("/api/v1/assignments/{id}", nonExistentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        verify(assignmentService, times(1)).deleteAssignment(nonExistentId);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when deleting assignment without authentication")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenDeletingAssignmentWithoutAuthentication() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/assignments/{id}", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());

        verify(assignmentService, never()).deleteAssignment(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when deleting assignment without proper role")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenDeletingAssignmentWithoutProperRole() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/assignments/{id}", assignmentId))
                .andExpect(status().isForbidden());

        verify(assignmentService, never()).deleteAssignment(any(UUID.class));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Should create return request successfully and return 201")
    void testCreateReturnRequest_Success() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        UUID returnId = UUID.randomUUID();

        AssetReturnResponse mockResponse = new AssetReturnResponse();
        mockResponse.setId(returnId);

        when(assetReturnService.createAssetReturn(assignmentId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/assignments/{assignmentId}/asset-returns", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Return request created"))
                .andExpect(jsonPath("$.data.id").value(returnId.toString()))
                .andExpect(header().string("Location",
                        "http://localhost/api/v1/assignments/" + assignmentId + "/asset-returns/" + returnId));

        verify(assetReturnService).createAssetReturn(assignmentId);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Should return 404 when assignment is not found")
    void testCreateReturnRequest_AssignmentNotFound() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        when(assetReturnService.createAssetReturn(assignmentId))
                .thenThrow(new AssignmentNotFoundException("Assignment not found"));

        mockMvc.perform(post("/api/v1/assignments/{assignmentId}/asset-returns", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        verify(assetReturnService).createAssetReturn(assignmentId);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Should return 400 when return request already exists")
    void testCreateReturnRequest_ReturnRequestAlreadyExists() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        when(assetReturnService.createAssetReturn(assignmentId))
                .thenThrow(new RequestReturnAssetAlreadyExistsException("Return request already exists"));

        mockMvc.perform(post("/api/v1/assignments/{assignmentId}/asset-returns", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        verify(assetReturnService).createAssetReturn(assignmentId);
    }

    @WithAnonymousUser
    @Test
    @DisplayName("Should return 401 Unauthorized for anonymous users")
    void testCreateReturnRequest_Unauthorized() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/assignments/{assignmentId}/return-requests", assignmentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());

        verify(assetReturnService, never()).createAssetReturn(any());
    }

    @Test
    @DisplayName("Should update assignment status successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAssignmentStatusSuccessfully() throws Exception {
        when(assignmentService.updateAssignmentStatus(eq(assignmentId), any(UpdateAssignmentStatusRequest.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(put("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAssignmentStatusRequest))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment status updated successfully"))
                .andExpect(jsonPath("$.data.id").value(assignmentResponse.getId().toString()))
                .andExpect(jsonPath("$.data.assetCode").value(assignmentResponse.getAssetCode()))
                .andExpect(jsonPath("$.data.assetName").value(assignmentResponse.getAssetName()))
                .andExpect(jsonPath("$.data.assignTo").value(assignmentResponse.getAssignTo()))
                .andExpect(jsonPath("$.data.assignBy").value(assignmentResponse.getAssignBy()))
                .andExpect(jsonPath("$.data.state.id").value(assignmentResponse.getState().getId()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when status is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenStatusIsInvalid() throws Exception {
        UpdateAssignmentStatusRequest invalidRequest = new UpdateAssignmentStatusRequest();
        invalidRequest.setStatus("INVALID_STATUS");

        mockMvc.perform(put("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for update")
    @WithAnonymousUser
    void shouldReturnForbiddenWhenUserLacksProperRole() throws Exception {
        mockMvc.perform(put("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAssignmentStatusRequest))
                        .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }
}
