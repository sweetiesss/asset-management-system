package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.response.RoleResponse;
import com.nashtech.rookies.oam.dto.response.UserDetailResponseDto;
import com.nashtech.rookies.oam.dto.response.UserPageResponseDto;
import com.nashtech.rookies.oam.dto.response.UserResponseDto;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponseDto_ShouldMapAllFieldsCorrectly() {
        Role role = new Role();
        role.setId(UUID.fromString("04e5257f-39e6-488f-acf7-84dcab71b42a"));
        role.setName("ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Location location = new Location();
        location.setCode("HCM");
        location.setName("Ho Chi Minh");

        User user = new User();
        user.setId(UUID.fromString("5e7e99e0-271e-4d1e-9ab3-c43440c30d01"));
        user.setUsername("johndoe");
        user.setStaffCode("SD0001");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);
        user.setJoinedOn(LocalDate.of(2023, 5, 15));
        user.setLocation(location);

        UserResponseDto responseDto = userMapper.toResponseDto(user);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(user.getId().toString());
        assertThat(responseDto.getUsername()).isEqualTo(user.getUsername());
        assertThat(responseDto.getStaffCode()).isEqualTo(user.getStaffCode());
        assertThat(responseDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(responseDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(responseDto.getStatus()).isEqualTo(user.getStatus());
        assertThat(responseDto.getJoinedOn()).isEqualTo(user.getJoinedOn());
        assertThat(responseDto.getLocation()).isEqualTo(user.getLocation());

        assertThat(responseDto.getRoles()).hasSize(1);
        RoleResponse roleResponse = responseDto.getRoles().iterator().next();
        assertThat(roleResponse.getId()).isEqualTo(role.getId().toString());
        assertThat(roleResponse.getName()).isEqualTo(role.getName());
    }

    @Test
    void toResponseDto_ShouldHandleNullValues() {
        User user = new User();
        user.setId(UUID.fromString("5e7e99e0-271e-4d1e-9ab3-c43440c30d01"));
        user.setUsername("johndoe");

        UserResponseDto responseDto = userMapper.toResponseDto(user);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(user.getId().toString());
        assertThat(responseDto.getUsername()).isEqualTo(user.getUsername());
        assertThat(responseDto.getStaffCode()).isNull();
        assertThat(responseDto.getFirstName()).isNull();
        assertThat(responseDto.getLastName()).isNull();
        assertThat(responseDto.getStatus()).isNull();
        assertThat(responseDto.getRoles()).isEqualTo(new HashSet<>());
        assertThat(responseDto.getJoinedOn()).isNull();
        assertThat(responseDto.getLocation()).isNull();
    }

    @Test
    void toEntity_ShouldMapAllFieldsExceptGender() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        LocalDate joinDate = LocalDate.of(2023, 5, 15);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setGender("MALE");
        requestDto.setDateOfBirth(dob);
        requestDto.setJoinedOn(joinDate);
        requestDto.setType("ADMIN");
        requestDto.setLocationCode("HCM");

        User user = userMapper.toEntity(requestDto);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(user.getLastName()).isEqualTo(requestDto.getLastName());
        assertThat(user.getDateOfBirth()).isEqualTo(requestDto.getDateOfBirth());
        assertThat(user.getJoinedOn()).isEqualTo(requestDto.getJoinedOn());
    }

    @Test
    void toEntity_ShouldHandleNullValues() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setFirstName("John");

        User user = userMapper.toEntity(requestDto);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(user.getLastName()).isNull();
        assertThat(user.getGender()).isNull();
        assertThat(user.getDateOfBirth()).isNull();
        assertThat(user.getJoinedOn()).isNull();
        assertThat(user.getRoles()).isEqualTo(Set.of());
        assertThat(user.getLocation()).isNull();
    }

    @Test
    void toUserPageResponseDto_ShouldMapFieldsCorrectly() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setStaffCode("SC123");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername("janedoe");
        user.setJoinedOn(LocalDate.of(2023, 5, 15));
        user.setRoles(roles);

        UserPageResponseDto dto = userMapper.toUserPageResponseDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getStaffCode()).isEqualTo(user.getStaffCode());
        assertThat(dto.getFullName()).isEqualTo("Jane Doe");
        assertThat(dto.getUsername()).isEqualTo(user.getUsername());
        assertThat(dto.getJoinedDate()).isEqualTo(user.getJoinedOn());

        assertThat(dto.getType()).hasSize(1);
        var roleResponse = dto.getType().iterator().next();
        assertThat(roleResponse.getId()).isEqualTo(role.getId().toString());
        assertThat(roleResponse.getName()).isEqualTo(role.getName());
    }

    @Test
    void toUserDetailResponseDto_ShouldMapFieldsCorrectly() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("USER");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Location location = new Location();
        location.setCode("HCM");
        location.setName("Ho Chi Minh");

        User user = new User();
        user.setStaffCode("SC456");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setUsername("johnsmith");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setGender(Gender.MALE);
        user.setJoinedOn(LocalDate.of(2020, 10, 10));
        user.setRoles(roles);
        user.setLocation(location);

        UserDetailResponseDto dto = userMapper.toUserDetailResponseDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getStaffCode()).isEqualTo(user.getStaffCode());
        assertThat(dto.getFullName()).isEqualTo("John Smith");
        assertThat(dto.getUsername()).isEqualTo(user.getUsername());
        assertThat(dto.getDateOfBirth()).isEqualTo(user.getDateOfBirth());
        assertThat(dto.getGender()).isEqualTo(user.getGender());
        assertThat(dto.getJoinedOn()).isEqualTo(user.getJoinedOn());

        assertThat(dto.getTypes()).hasSize(1);
        var roleResponse = dto.getTypes().iterator().next();
        assertThat(roleResponse.getId()).isEqualTo(role.getId().toString());
        assertThat(roleResponse.getName()).isEqualTo(role.getName());

        assertThat(dto.getLocation()).isEqualTo(location);
    }
}