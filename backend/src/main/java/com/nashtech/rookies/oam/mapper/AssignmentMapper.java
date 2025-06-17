package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.response.*;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import com.nashtech.rookies.oam.projection.AssignmentEditViewProjection;
import com.nashtech.rookies.oam.projection.AssignmentWithReturnDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserAssignmentMapper.class, CategoryMapper.class })
public interface AssignmentMapper {
    default AssignmentHistory toAssignmentHistory(AssignmentWithReturnDate projection) {
        AssignmentHistory history = new AssignmentHistory();
        history.setId(projection.getAssignment().getId());
        history.setAssignedDate(projection.getAssignment().getAssignedDate());
        history.setAssignedTo(projection.getAssignment().getUser().getFirstName() + " " + projection.getAssignment().getUser().getLastName());
        history.setAssignedBy(projection.getAssignment().getCreatedBy());
        history.setReturnedDate(projection.getReturnedDate());
        return history;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "note")
    @Mapping(target = "user")
    @Mapping(target = "asset")
    @Mapping(target = "status")
    @Mapping(target = "assignedDate")
    @Mapping(target = "version", ignore = true)
    Assignment toEntity(
            AssignmentRequest request,
            User user,
            Asset asset,
            AssignmentStatus status
    );

    @Mapping(target = "id", source = "id")
    @Mapping(target = "assetCode", source = "asset.code")
    @Mapping(target = "assetName", source = "asset.name")
    @Mapping(target = "assignTo", source = "user.username")
    @Mapping(target = "assignBy", source = "createdBy")
    @Mapping(target = "assignedDate", source = "assignedDate")
    @Mapping(target = "state", source = "status")
    AssignmentResponse toResponse(Assignment assignment);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "assetCode", source = "asset.code")
    @Mapping(target = "assetName", source = "asset.name")
    @Mapping(target = "assignTo", source = "user.username")
    @Mapping(target = "assignBy", source = "updatedBy")
    @Mapping(target = "assignedDate", source = "assignedDate")
    @Mapping(target = "state", source = "status")
    AssignmentResponse toUpdatedResponse(Assignment assignment);

    @Mapping(target = "user", source = "user")
    AssignmentEditViewResponse toEditViewResponse(AssignmentEditViewProjection projection);

    default AssignmentPageResponse toAssignmentPageResponse(Assignment assignment) {
        return AssignmentPageResponse.builder()
                .id(assignment.getId())
                .assetCode(assignment.getAsset() != null ? assignment.getAsset().getCode() : null)
                .assetName(assignment.getAsset() != null ? assignment.getAsset().getName() : null)
                .userId(assignment.getUser() != null ? assignment.getUser().getUsername() : null)
                .createdBy(assignment.getCreatedBy())
                .assignedDate(assignment.getAssignedDate())
                .status(assignment.getStatus())
                .build();
    }

    @Mapping(target = "assetCode", source = "assignment.asset.code")
    @Mapping(target = "assetName", source = "assignment.asset.name")
    @Mapping(target = "userId", source = "assignment.user.username")
    @Mapping(target = "returnState", source = "latestReturnState")
    @Mapping(target = "category", source = "assignment.asset.category")
    AssignmentPageResponse toAssignmentPageResponse(Assignment assignment, ReturnState latestReturnState);

    @Mapping(target = "assetCode", source = "asset.code")
    @Mapping(target = "assetName", source = "asset.name")
    @Mapping(target = "specification", source = "asset.specification")
    @Mapping(target = "userId", source = "user.username")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "assignedDate", source = "assignedDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "note", source = "note")
    AssignmentDetailResponse toDetailResponse(Assignment assignment);
}
