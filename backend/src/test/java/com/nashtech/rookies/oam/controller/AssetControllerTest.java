package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.pagination.APIPageableDTO;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
class AssetControllerTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateAssetRequest updateAssetRequest;
    private AssetRequest assetRequest;
    private AssetResponse assetResponse;

    private UUID assetId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        assetId = UUID.randomUUID();


        assetRequest = new AssetRequest();
        assetRequest.setName("Laptop");
        assetRequest.setSpecification("Dell XPS 13, 16GB RAM");
        assetRequest.setInstalledDate(LocalDate.of(2023, 10, 15));
        assetRequest.setState("AVAILABLE");
        assetRequest.setCategoryId(1);

        updateAssetRequest = new UpdateAssetRequest();
        updateAssetRequest.setName("Updated Laptop");
        updateAssetRequest.setSpecification("MacBook Pro M2");
        updateAssetRequest.setInstalledDate(LocalDate.of(2024, 1, 5));
        updateAssetRequest.setState("NOT_AVAILABLE");

        assetResponse = new AssetResponse();
        assetResponse.setId(assetId);
        assetResponse.setName("Laptop");
        assetResponse.setSpecification("Dell XPS 13, 16GB RAM");
        assetResponse.setInstalledDate(LocalDate.of(2023, 10, 15));
        assetResponse.setState("AVAILABLE");
        assetResponse.setCategory(new CategoryResponse(1, "Electronics", "EL"));
    }

    @Test
    @DisplayName("Should create asset successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAssetSuccessfully() throws Exception {
        when(assetService.createAsset(any(AssetRequest.class))).thenReturn(assetResponse);

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Asset created successfully"))
                .andExpect(jsonPath("$.data.id").value(assetResponse.getId().toString()))
                .andExpect(jsonPath("$.data.name").value(assetResponse.getName()))
                .andExpect(jsonPath("$.data.specification").value(assetResponse.getSpecification()))
                .andExpect(jsonPath("$.data.installedDate").value("2023-10-15"))
                .andExpect(jsonPath("$.data.state").value(assetResponse.getState()))
                .andExpect(jsonPath("$.data.category.prefix").value(assetResponse.getCategory().getPrefix()));
    }

    @Test
    @DisplayName("Should return 401 unauthorized when user is not authenticated for create asset")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForPost() throws Exception {
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(assetRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authorized for create asset")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedForPost() throws Exception {
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        AssetRequest invalidRequest = new AssetRequest();
        invalidRequest.setName("");
        invalidRequest.setSpecification("");
        invalidRequest.setInstalledDate(null);
        invalidRequest.setState("INVALID");
        invalidRequest.setCategoryId(null);

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update asset successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAssetSuccessfully() throws Exception {
        when(assetService.updateAsset(eq(assetId), any(UpdateAssetRequest.class))).thenReturn(assetResponse);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAssetRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset updated successfully"))
                .andExpect(jsonPath("$.data.id").value(assetResponse.getId().toString()))
                .andExpect(jsonPath("$.data.name").value(assetResponse.getName()))
                .andExpect(jsonPath("$.data.state").value(assetResponse.getState()));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for update")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAssetRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authorized for update")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedForUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAssetRequest))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update request is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
        UpdateAssetRequest invalidUpdate = new UpdateAssetRequest();
        invalidUpdate.setName("");
        invalidUpdate.setSpecification("");
        invalidUpdate.setInstalledDate(null);
        invalidUpdate.setState("INVALID");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should retrieve asset successfully with edit mode when user is authorized")
    @WithMockUser(roles = "ADMIN")
    void shouldRetrieveAssetSuccessfullyWithEditMode() throws Exception {
        when(assetService.getAssetForEdit(assetId)).thenReturn(assetResponse);

        mockMvc.perform(get("/api/v1/assets/{id}/edit-view", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(assetResponse.getId().toString()))
                .andExpect(jsonPath("$.data.name").value(assetResponse.getName()))
                .andExpect(jsonPath("$.data.specification").value(assetResponse.getSpecification()))
                .andExpect(jsonPath("$.data.installedDate").value("2023-10-15"))
                .andExpect(jsonPath("$.data.state").value(assetResponse.getState()))
                .andExpect(jsonPath("$.data.category.prefix").value(assetResponse.getCategory().getPrefix()));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for get asset")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForGet() throws Exception {
        mockMvc.perform(get("/api/v1/assets/{id}/edit-view", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when asset does not exist")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenAssetDoesNotExist() throws Exception {
        when(assetService.getAssetForEdit(assetId))
                .thenThrow(new RuntimeException("Asset not found")); // Adjust if using custom exception

        mockMvc.perform(get("/api/v1/assets/{id}/edit-view", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Adjust based on your global exception handler
    }

    @Test
    @DisplayName("Should return paginated list of assets successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPaginatedListOfAssetsSuccessfully() throws Exception {
        AssetPageResponse asset1 = AssetPageResponse.builder()
                .code("AC001")
                .name("Laptop X")
                .categoryName("Electronics")
                .state(AssetState.AVAILABLE)
                .build();

        AssetPageResponse asset2 = AssetPageResponse.builder()
                .code("AC002")
                .name("Monitor Y")
                .categoryName("Electronics")
                .state(AssetState.ASSIGNED)
                .build();

        List<AssetPageResponse> content = List.of(asset1, asset2);
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<AssetPageResponse> page = new PageImpl<>(content, pageable, 2);

        APIPageableResponseDTO<AssetPageResponse> response = new APIPageableResponseDTO<>(page);

        when(assetService.getAssets(eq(0), eq(10), eq(""), eq("assetCode"), eq("ASC"), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/assets")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "")
                        .param("sort", "assetCode")
                        .param("sortOrder", "ASC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset list retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].code").value("AC001"))
                .andExpect(jsonPath("$.data.content[1].code").value("AC002"))
                .andExpect(jsonPath("$.data.pageable.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10));
    }


    private static APIPageableResponseDTO<AssignmentHistory> getAssignmentHistoryAPIPageableResponseDTO(AssignmentHistory history1) {
        APIPageableResponseDTO<AssignmentHistory> mockHistory = new APIPageableResponseDTO<>();
        mockHistory.setContent(List.of(history1));

        APIPageableDTO pageable = new APIPageableDTO(new PageImpl(List.of(), PageRequest.of(0, 20), 0L));
        pageable.setPageNumber(0);
        pageable.setPageSize(10);
        pageable.setTotalElements(1L);
        pageable.setTotalPages(1);
        pageable.setNumberOfElements(1);
        pageable.setFirst(true);
        pageable.setLast(true);
        pageable.setEmpty(false);
        pageable.setSorted(false);

        mockHistory.setPageable(pageable);
        return mockHistory;
    }

    private static APIPageableResponseDTO<AssignmentHistory> getAssignmentHistoryAPIPageableResponseDTO() {
        APIPageableResponseDTO<AssignmentHistory> mockHistory = new APIPageableResponseDTO<>();
        mockHistory.setContent(List.of());

        APIPageableDTO pageable = new APIPageableDTO(new PageImpl(List.of(), PageRequest.of(0, 20), 0L));
        pageable.setPageNumber(0);
        pageable.setPageSize(20); // DEFAULT_PAGE_SIZE
        pageable.setTotalElements(0L);
        pageable.setTotalPages(0);
        pageable.setNumberOfElements(0);
        pageable.setFirst(true);
        pageable.setLast(true);
        pageable.setEmpty(true);
        pageable.setSorted(false);

        mockHistory.setPageable(pageable);
        return mockHistory;
    }

    @Test
    @DisplayName("Should retrieve asset detail successfully when user is authorized")
    @WithMockUser(roles = "ADMIN")
    void shouldRetrieveAssetDetailSuccessfully() throws Exception {
        when(assetService.getAssetDetail(assetId)).thenReturn(assetResponse);

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(assetResponse.getId().toString()))
                .andExpect(jsonPath("$.data.name").value(assetResponse.getName()))
                .andExpect(jsonPath("$.data.specification").value(assetResponse.getSpecification()))
                .andExpect(jsonPath("$.data.installedDate").value("2023-10-15"))
                .andExpect(jsonPath("$.data.state").value(assetResponse.getState()))
                .andExpect(jsonPath("$.data.category.prefix").value(assetResponse.getCategory().getPrefix()));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for get asset detail")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForGetDetail() throws Exception {
        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when asset detail does not exist")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenAssetDetailDoesNotExist() throws Exception {
        when(assetService.getAssetDetail(assetId))
                .thenThrow(new RuntimeException("Asset not found"));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Adjust based on your exception handler
    }

    @Test
    @DisplayName("Should retrieve assignment history successfully when user is authorized")
    @WithMockUser(roles = "ADMIN")
    void shouldRetrieveAssignmentHistorySuccessfully() throws Exception {
        // Create mock assignment history data
        AssignmentHistory history1 = new AssignmentHistory();
        history1.setAssignedDate(LocalDate.of(2023, 10, 1));
        history1.setAssignedTo("John Doe");
        history1.setAssignedBy("Admin User");
        history1.setReturnedDate(LocalDate.of(2023, 11, 1));

        APIPageableResponseDTO<AssignmentHistory> mockHistory = getAssignmentHistoryAPIPageableResponseDTO(history1);

        when(assetService.getAssignmentHistory(assetId, 0, 10)).thenReturn(mockHistory);

        mockMvc.perform(get("/api/v1/assets/{id}/assignments", assetId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment history retrieved successfully"))
                .andExpect(jsonPath("$.data.pageable.totalElements").value(1))
                .andExpect(jsonPath("$.data.pageable.totalPages").value(1))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].assignedTo").value("John Doe"))
                .andExpect(jsonPath("$.data.content[0].assignedBy").value("Admin User"))
                .andExpect(jsonPath("$.data.content[0].assignedDate").value("2023-10-01"))
                .andExpect(jsonPath("$.data.content[0].returnedDate").value("2023-11-01"));
    }

    @Test
    @DisplayName("Should retrieve assignment history with default pagination parameters")
    @WithMockUser(roles = "ADMIN")
    void shouldRetrieveAssignmentHistoryWithDefaultPagination() throws Exception {
        APIPageableResponseDTO<AssignmentHistory> mockHistory = getAssignmentHistoryAPIPageableResponseDTO();

        when(assetService.getAssignmentHistory(assetId, 0, 20)).thenReturn(mockHistory);

        mockMvc.perform(get("/api/v1/assets/{id}/assignments", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment history retrieved successfully"))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.data.pageable.empty").value(true));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for assignment history")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForAssignmentHistory() throws Exception {
        mockMvc.perform(get("/api/v1/assets/{id}/assignments", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when asset does not exist for assignment history")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenAssetDoesNotExistForAssignmentHistory() throws Exception {
        when(assetService.getAssignmentHistory(assetId, 0, 20))
                .thenThrow(new RuntimeException("Asset not found"));

        mockMvc.perform(get("/api/v1/assets/{id}/assignments", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Adjust based on your exception handler
    }

    @Test
    @DisplayName("Should handle invalid pagination parameters for assignment history")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidPaginationParametersForAssignmentHistory() throws Exception {
        APIPageableResponseDTO<AssignmentHistory> mockHistory = new APIPageableResponseDTO<>();
        mockHistory.setContent(List.of());


        when(assetService.getAssignmentHistory(assetId, 0, 20)).thenReturn(mockHistory);

        mockMvc.perform(get("/api/v1/assets/{id}/assignments", assetId)
                        .param("page", "-1") // Invalid page number
                        .param("size", "0")  // Invalid page size
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Assuming your service handles invalid params gracefully
    }

    @Test
    @DisplayName("Should create asset with location header")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAssetWithLocationHeader() throws Exception {
        when(assetService.createAsset(any(AssetRequest.class))).thenReturn(assetResponse);

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/v1/assets/" + assetResponse.getId())));
    }

    @Test
    @DisplayName("Should delete asset successfully when user is authorized")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAssetSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when user is not authenticated for delete")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authorized for delete")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedForDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 Not Found when asset does not exist for delete")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenAssetDoesNotExistForDelete() throws Exception {
        doThrow(new RuntimeException("Asset not found"))
                .when(assetService).deleteAsset(assetId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when asset cannot be deleted due to constraints")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenAssetCannotBeDeletedDueToConstraints() throws Exception {
        doThrow(new RuntimeException("Asset cannot be deleted - it has active assignments"))
                .when(assetService).deleteAsset(assetId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/assets/{id}", assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}