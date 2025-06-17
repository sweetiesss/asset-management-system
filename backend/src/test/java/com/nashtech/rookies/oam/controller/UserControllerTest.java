package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.*;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.LocationCode;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRequestDto = new UserRequestDto();
        userRequestDto.setFirstName("John");
        userRequestDto.setLastName("Doe");
        userRequestDto.setGender(Gender.MALE.name());
        userRequestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userRequestDto.setJoinedOn(LocalDate.of(2023, 5, 15)); // A Monday
        userRequestDto.setType(RoleName.STAFF.name());
        userRequestDto.setLocationCode(LocationCode.HN.name());


        userResponseDto = new UserResponseDto();
        userResponseDto.setId("user123");
        userResponseDto.setUsername("johndoe");
        userResponseDto.setStaffCode("SD0001");
        userResponseDto.setFirstName("John");
        userResponseDto.setLastName("Doe");
        userResponseDto.setStatus(UserStatus.ACTIVE);

        Set<RoleResponse> roles = new HashSet<>();
        RoleResponse roleResponse = new RoleResponse("role123", RoleName.STAFF.name());
        roles.add(roleResponse);
        userResponseDto.setRoles(roles);

        userResponseDto.setJoinedOn(LocalDate.of(2023, 5, 15));

        Location location = new Location();
        location.setCode(LocationCode.HN.name());
        location.setName("Hanoi");
        userResponseDto.setLocation(location);
    }

    @Test
    @DisplayName("Should create user successfully when valid request is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(userResponseDto.getId()))
                .andExpect(jsonPath("$.data.firstName").value(userResponseDto.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(userResponseDto.getLastName()))
                .andExpect(jsonPath("$.data.staffCode").value(userResponseDto.getStaffCode()))
                .andExpect(jsonPath("$.data.location.code").value(userResponseDto.getLocation().getCode()))
                .andExpect(jsonPath("$.data.location.name").value(userResponseDto.getLocation().getName()))
                .andExpect(jsonPath("$.data.roles[0].name").value(userResponseDto.getRoles().iterator().next().getName()))
                .andExpect(jsonPath("$.data.joinedOn").value(userResponseDto.getJoinedOn().toString()))
                .andExpect(jsonPath("$.data.status").value(userResponseDto.getStatus().name()));

    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        // Given
        UserRequestDto invalidRequestDto = new UserRequestDto();
        invalidRequestDto.setLastName("");

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return user detail when valid ID is provided")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUserDetailWhenValidId() throws Exception {
        String userId = "user123";

        when(userService.getUserById(userId)).thenReturn(
                new UserDetailResponseDto(
                        "SD0001",
                        "John Doe",
                        "johndoe",
                        LocalDate.of(1990, 1, 1),
                        Gender.MALE,
                        LocalDate.of(2023, 5, 15),
                        userResponseDto.getRoles(),
                        userResponseDto.getLocation(),
                        userResponseDto.getFirstName(),
                        userResponseDto.getLastName(),
                        1L
                )
        );

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.username").value("johndoe"))
                .andExpect(jsonPath("$.data.location.code").value("HN"));
    }

    @Test
    @DisplayName("Should return paginated users when valid params are provided")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPaginatedUsersSuccessfully() throws Exception {
        // Given
        UserPageResponseDto userDto = new UserPageResponseDto(
                UUID.randomUUID(),
                "SD0001",
                "John Doe",
                "johndoe",
                LocalDate.of(2023, 5, 15),
                Set.of(new RoleResponse("role123", RoleName.STAFF.name()))
        );

        List<UserPageResponseDto> userList = List.of(userDto);
        Page<UserPageResponseDto> page = new PageImpl<>(userList, PageRequest.of(0, 1), 1);

        APIPageableResponseDTO<UserPageResponseDto> responseDTO = new APIPageableResponseDTO<>(page);

        when(userService.getUsers(0, 1, "", "firstName", "ASC", null,null))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "firstName")
                        .param("sortOrder", "ASC")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User list retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].staffCode").value("SD0001"))
                .andExpect(jsonPath("$.data.content[0].username").value("johndoe"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCurrentUser_success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        CurrentUserResponseDto mockUser = CurrentUserResponseDto.builder()
                .id(userId)
                .username("johndoe")
                .roles(List.of("USER", "ADMIN"))
                .changePasswordRequired(false)
                .build();

        Mockito.when(userService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Optional if needed
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Current user retrieved successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.username").value("johndoe"))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"))
                .andExpect(jsonPath("$.data.roles[1]").value("ADMIN"))
                .andExpect(jsonPath("$.data.changePasswordRequired").value(false));
    }
}