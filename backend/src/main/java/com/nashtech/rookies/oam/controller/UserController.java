package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.CurrentUserResponseDto;
import com.nashtech.rookies.oam.dto.response.UserDetailResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE;
import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE_SIZE;
import static com.nashtech.rookies.oam.constant.SortConstants.ASC;
import static com.nashtech.rookies.oam.constant.SortConstants.DEFAULT_USER_LIST_SORT_FIELD;
import static com.nashtech.rookies.oam.util.ResponseUtil.success;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Users", description = "Endpoints for retrieving and managing users")
public class UserController {

    UserService userService;

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user in the system",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<UserResponseDto>> createUser(
            @Valid @RequestBody UserRequestDto userRequestDto) {

        UserResponseDto userResponseDto = userService.createUser(userRequestDto);
        ApiGenericResponse<UserResponseDto> body = ApiResult.success(
                "User created successfully",
                userResponseDto
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponseDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }
    @GetMapping("")
    @Operation(
            summary = "Get list of users with pagination, sorting and filtering",
            description = "Retrieves a paginated list of users. Supports search by name/username, sorting by any field, and optional filtering by roles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User list retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<UserPageResponseDto>>> getUsers(
            @RequestParam(defaultValue = DEFAULT_PAGE, name = "page") Integer pageNo,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, name = "size") Integer pageSize,
            @RequestParam(defaultValue = "", name = "search", required = false) String search,
            @RequestParam(defaultValue = DEFAULT_USER_LIST_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder,
            @RequestParam(name = "roles", required = false) List<String> roles,
            @RequestParam(name = "status", required = false) List<String> status) {

        return success(
                "User list retrieved successfully",
                userService.getUsers(pageNo, pageSize, search, sortField, sortOrder, roles, status)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed information of a user by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<UserDetailResponseDto>> getUserById(@PathVariable String id) {
        return success("User retrieved successfully", userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiGenericResponse<CurrentUserResponseDto>> getCurrentUser(){
        return success("Current user retrieved successfully", userService.getCurrentUser());
    }
    @Operation(summary = "Edit user", description = "Edit a user's information")
    @PatchMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User edited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<UserResponseDto>> editUser(@RequestBody @Valid EditUserRequest request,
                                                                        @PathVariable("id") UUID id) {
        UserResponseDto updatedUser = userService.updateUser(request, id);
        ApiGenericResponse<UserResponseDto> body = ApiResult.success(
                "User edited successfully",
                updatedUser
        );
        return ResponseEntity.ok(body);
    }
}
