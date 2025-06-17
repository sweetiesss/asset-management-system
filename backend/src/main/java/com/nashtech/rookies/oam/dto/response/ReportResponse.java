package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

public class ReportResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class AssetReport {
        String code;
        String name;
        String category;
        LocalDate installedDate;
        String state;
        String location;
        String specification;

        public static AssetReport fromAsset(Asset asset){
            return AssetReport.builder()
                    .code(asset.getCode())
                    .name(asset.getName())
                    .category(asset.getCategory().getName())
                    .installedDate(asset.getInstalledDate())
                    .state(asset.getState().toString())
                    .location(asset.getLocation().getName())
                    .specification(asset.getSpecification())
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class UserReport {
        String staffCode;
        String fullName;
        String username;
        LocalDate joinedDate;
        LocalDate dateOfBirth;
        String gender;
        String location;

        public static UserReport fromUser(User user) {
            return UserReport.builder()
                    .staffCode(user.getStaffCode())
                    .fullName(String.format("%s %s", user.getFirstName(), user.getLastName()))
                    .username(user.getUsername())
                    .joinedDate(user.getJoinedOn())
                    .dateOfBirth(user.getDateOfBirth())
                    .gender(user.getGender().toString())
                    .location(user.getLocation().getName() )
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class AssignmentReport {
        String assetCode;
        String assetName;
        String specification;
        String assignedTo;
        String assignedBy;
        LocalDate assignedDate;
        String state;
        String note;
        public static AssignmentReport fromAssignment(Assignment assignment) {
            return AssignmentReport.builder()
                    .assetCode(assignment.getAsset().getCode())
                    .assetName(assignment.getAsset().getName())
                    .specification(assignment.getAsset().getSpecification())
                    .assignedTo(assignment.getUser().getUsername())
                    .assignedBy(assignment.getUser().getUsername())
                    .assignedDate(assignment.getAssignedDate())
                    .state(assignment.getStatus().getName())
                    .note(assignment.getNote())
                    .build();
        }
    }

    

}
