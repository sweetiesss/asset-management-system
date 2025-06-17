package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.projection.EditAssetProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    Asset toEntity(AssetRequest assetRequest);
    AssetResponse toResponse(Asset asset);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "version", source = "version")
    AssetResponse toResponseEdit(EditAssetProjection editAssetProjection);


    default AssetPageResponse toAssetPageResponseDto(Asset asset) {
        return AssetPageResponse.builder()
                .id(asset.getId())
                .code(asset.getCode())
                .name(asset.getName())
                .categoryName(asset.getCategory().getName())
                .state(asset.getState())
                .build();
    }
}
