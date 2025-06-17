package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.exception.UserNotFoundException;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
class EditUserApiTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private EditUserRequest editUserRequest;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        UUID userId = UUID.randomUUID();
        editUserRequest = new EditUserRequest();
        editUserRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        editUserRequest.setGender(Gender.FEMALE.name());
        LocalDate date = LocalDate.now().minusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        editUserRequest.setJoinedOn(date);
        editUserRequest.setType(RoleName.STAFF.name());

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(userId.toString());
        userResponseDto.setUsername("updatedUser");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldEditUserSuccessfully() throws Exception {
        Mockito.when(userService.updateUser(any(EditUserRequest.class), any(UUID.class)))
                .thenReturn(userResponseDto);

        mockMvc.perform(patch("/api/v1/users/{id}", userResponseDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editUserRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User edited successfully"))
                .andExpect(jsonPath("$.data.id").value(userResponseDto.getId()))
                .andExpect(jsonPath("$.data.username").value(userResponseDto.getUsername()));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        Mockito.when(userService.updateUser(any(EditUserRequest.class), any(UUID.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editUserRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("User not found"));
    }
    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editUserRequest)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        // Given
        EditUserRequest invalidRequest = new EditUserRequest();
        invalidRequest.setGender("");

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }
}
