package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.CurrentUserResponseDto;
import com.nashtech.rookies.oam.dto.response.UserDetailResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);
    APIPageableResponseDTO<UserPageResponseDto> getUsers(
            int pageNo,
            int pageSize,
            String search,
            String sortField,
            String sortOrder,
            List<String> roles,
            List<String> status
    );
    UserDetailResponseDto getUserById(String id);
    CurrentUserResponseDto getCurrentUser();
    UserResponseDto updateUser(EditUserRequest editUserRequest, UUID userId);

    User getUserById(UUID id);

}
