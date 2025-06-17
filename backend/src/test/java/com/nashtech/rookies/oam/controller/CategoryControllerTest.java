package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.service.CategoryService;
import com.nashtech.rookies.oam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private List<CategoryResponse> categoryResponses;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Electronics");
        categoryRequest.setPrefix("EL");

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1);
        categoryResponse.setName("Electronics");
        categoryResponse.setPrefix("EL");

        categoryResponses = Arrays.asList(categoryResponse);
    }

    @Test
    @DisplayName("Should retrieve all categories successfully when authenticated")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllCategoriesSuccessfully() throws Exception {
        List<CategoryResponse> categories = List.of(categoryResponse);
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Retrieved category list successfully"))
                .andExpect(jsonPath("$.data[0].id").value(categoryResponse.getId()))
                .andExpect(jsonPath("$.data[0].name").value(categoryResponse.getName()))
                .andExpect(jsonPath("$.data[0].prefix").value(categoryResponse.getPrefix()));
    }

    @Test
    @DisplayName("Should create category successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategorySuccessfully() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Category created successfully"))
                .andExpect(jsonPath("$.data.id").value(categoryResponse.getId()))
                .andExpect(jsonPath("$.data.name").value(categoryResponse.getName()))
                .andExpect(jsonPath("$.data.prefix").value(categoryResponse.getPrefix()));
    }

    @Test
    @DisplayName("Should return 401 unauthorized when user is not authenticated for get categories")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForGet() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated for create category")
    @WithMockUser(roles = "STAFF")
    void shouldReturnForbidenWhenUserIsNotAuthenticatedForPost() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        CategoryRequest invalidRequest = new CategoryRequest();
        invalidRequest.setName("");
        invalidRequest.setPrefix("");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

}
