package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetReturnRequest;
import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import com.nashtech.rookies.oam.service.AssetReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetReturnController.class)
class AssetReturnControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AssetReturnService assetReturnService;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AssetReturnRequest assetReturnRequest;
    private AssetReturnResponse assetReturnResponse;
    private UUID returnId;
    private AssetReturnPageResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        returnId = UUID.randomUUID();

        assetReturnRequest = new AssetReturnRequest();
        assetReturnRequest.setState("COMPLETED");

        assetReturnResponse = new AssetReturnResponse();
        assetReturnResponse.setId(returnId);
        assetReturnResponse.setState(ReturnState.COMPLETED);
        response = AssetReturnPageResponse.builder()
                .id(UUID.randomUUID())
                .assetCode("LA000001")
                .assetName("Laptop")
                .createdBy("admin")
                .assignedDate(LocalDate.of(2024, 6, 1))
                .updatedBy("admin")
                .returnedDate(LocalDate.of(2024, 6, 15))
                .state(ReturnState.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("Should update return request successfully when authenticated as admin")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateReturnRequestSuccessfully() throws Exception {
        when(assetReturnService.updateAssetReturn(eq(returnId), any(AssetReturnRequest.class)))
                .thenReturn(assetReturnResponse);
    }
    void getReturns_ShouldReturnWrappedPaginatedResponse() throws Exception {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(response), pageable, 1);
        var pageableResponse = new APIPageableResponseDTO<>(page);

        mockMvc.perform(patch("/api/v1/asset-returns/{returnId}", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetReturnRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));
        when(assetReturnService.getAssetReturns(any())).thenReturn(pageableResponse);

        mockMvc.perform(get("/api/v1/asset-returns")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "assetName")
                        .param("sortOrder", "ASC")
                        .param("search", "Laptop")
                        .param("states", "COMPLETED")  // for List<String>
                        .param("returnedDateFrom", "2024-06-01")
                        .param("returnedDateTo", "2024-06-30")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Return request updated"))
                .andExpect(jsonPath("$.data.id").value(returnId.toString()))
                .andExpect(jsonPath("$.data.state").value(assetReturnResponse.getState().toString()));
    }

    @Test
    @DisplayName("Should return 401 unauthorized when user is not authenticated")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/asset-returns/{returnId}", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetReturnRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))

                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 forbidden when user lacks admin role")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbiddenWhenUserLacksAdminRole() throws Exception {
        mockMvc.perform(patch("/api/v1/asset-returns/{returnId}", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetReturnRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 bad request when state is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenStateIsInvalid() throws Exception {
        AssetReturnRequest invalidRequest = new AssetReturnRequest();
        invalidRequest.setState("INVALID_STATE");

        mockMvc.perform(patch("/api/v1/asset-returns/{returnId}", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 bad request when state is blank")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenStateIsBlank() throws Exception {
        AssetReturnRequest invalidRequest = new AssetReturnRequest();
        invalidRequest.setState("");

        mockMvc.perform(patch("/api/v1/asset-returns/{returnId}", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }
}