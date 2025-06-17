package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;
import com.nashtech.rookies.oam.model.AssetReturn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetReturnMapper {
    @Mapping(source = "assignment.id", target = "assignmentId")
    AssetReturnResponse toDTO(AssetReturn assetReturn);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "assignment.asset.code", target = "assetCode")
    @Mapping(source = "assignment.asset.name", target = "assetName")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "assignment.assignedDate", target = "assignedDate")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(source = "returnedDate", target = "returnedDate")
    @Mapping(source = "state", target = "state")
    AssetReturnPageResponse toAssetReturnPageResponse(AssetReturn assetReturn);
}
