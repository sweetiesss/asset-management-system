package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.EditUserRequest;
import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.RoleResponse;
import com.nashtech.rookies.oam.dto.response.UserDetailResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.Gender;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toResponseDto(User user);

    @Mapping(target = "gender", ignore = true)
    User toEntity(UserRequestDto userRequestDto);

    default UserPageResponseDto toUserPageResponseDto(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();

        Set<RoleResponse> roleResponses = user.getRoles().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toSet());

        return new UserPageResponseDto(
                user.getId(),
                user.getStaffCode(),
                fullName,
                user.getUsername(),
                user.getJoinedOn(),
                roleResponses
        );
    }

    default UserDetailResponseDto toUserDetailResponseDto(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();

        Set<RoleResponse> roleResponses = user.getRoles().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toSet());

        return new UserDetailResponseDto(
                user.getStaffCode(),
                fullName,
                user.getUsername(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getJoinedOn(),
                roleResponses,
                user.getLocation(),
                user.getLastName(),
                user.getFirstName()
                , user.getVersion()
        );
    }

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "gender", expression = "java(mapGender(editUserRequest.getGender(), user.getGender()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User toEntity(EditUserRequest editUserRequest, @MappingTarget User user);

    default Gender mapGender(String gender, Gender existingGender){
        if (!StringUtils.hasText(gender)) {
            return existingGender;
        }
        return Gender.valueOf(gender.toUpperCase());
    }

}
