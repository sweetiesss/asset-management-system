package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.response.UserAssignmentEditView;
import com.nashtech.rookies.oam.projection.UserAssignmentEditViewProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserAssignmentMapper {
    @Named("combineFullName")
    static String toFullName(UserAssignmentEditViewProjection projection) {
        return projection.getFirstName() + " " + projection.getLastName();
    }

    @Mapping(target = "fullName", source = ".", qualifiedByName = "combineFullName")
    UserAssignmentEditView toView(UserAssignmentEditViewProjection projection);

}