package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Category;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.enums.AssetState;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssetMapperTest {
    private final AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {
        AssetRequest request = new AssetRequest();
        request.setName("Laptop");
        request.setSpecification("Dell XPS 13");
        request.setInstalledDate(LocalDate.of(2023, 10, 15));
        request.setState("AVAILABLE");
        request.setCategoryId(1);

        Asset asset = assetMapper.toEntity(request);

        assertThat(asset).isNotNull();
        assertThat(asset.getName()).isEqualTo(request.getName());
        assertThat(asset.getSpecification()).isEqualTo(request.getSpecification());
        assertThat(asset.getInstalledDate()).isEqualTo(request.getInstalledDate());
        assertThat(asset.getState()).isEqualTo(AssetState.AVAILABLE);
        assertThat(asset.getCategory()).isNull();
        assertThat(asset.getLocation()).isNull();
        assertThat(asset.getId()).isNull();
    }

    @Test
    void toEntity_ShouldHandleNullValues() {
        AssetRequest request = new AssetRequest();
        request.setName("Monitor");
        request.setInstalledDate(LocalDate.of(2023, 5, 20));

        Asset asset = assetMapper.toEntity(request);

        assertThat(asset).isNotNull();
        assertThat(asset.getName()).isEqualTo(request.getName());
        assertThat(asset.getSpecification()).isNull();
        assertThat(asset.getInstalledDate()).isEqualTo(request.getInstalledDate());
        assertThat(asset.getState()).isEqualTo(AssetState.AVAILABLE); // Default value
        assertThat(asset.getCategory()).isNull();
        assertThat(asset.getLocation()).isNull();
        assertThat(asset.getId()).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFieldsCorrectly() {
        UUID locationId = UUID.randomUUID();

        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setCode("AST12345");
        asset.setName("Projector");
        asset.setSpecification("4K UHD");
        asset.setInstalledDate(LocalDate.of(2022, 12, 1));
        asset.setState(AssetState.AVAILABLE);

        Category category = new Category();
        category.setId(1);
        category.setName("Electronics");
        category.setPrefix("EL");
        asset.setCategory(category);

        Location location = new Location();
        location.setId(locationId);
        location.setName("Office A");
        asset.setLocation(location);

        AssetResponse response = assetMapper.toResponse(asset);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(asset.getId());
        assertThat(response.getCode()).isEqualTo(asset.getCode());
        assertThat(response.getName()).isEqualTo(asset.getName());
        assertThat(response.getSpecification()).isEqualTo(asset.getSpecification());
        assertThat(response.getInstalledDate()).isEqualTo(asset.getInstalledDate());
        assertThat(response.getState()).isEqualTo(asset.getState().name());
        assertThat(response.getCategory()).isNotNull();
        assertThat(response.getCategory().getId()).isEqualTo(category.getId());
        assertThat(response.getCategory().getName()).isEqualTo(category.getName());
        assertThat(response.getCategory().getPrefix()).isEqualTo(category.getPrefix());
        assertThat(response.getLocation()).isNotNull();
        assertThat(response.getLocation().getId()).isEqualTo(location.getId());
        assertThat(response.getLocation().getName()).isEqualTo(location.getName());
    }

    @Test
    void toResponse_ShouldHandleNullValues() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setCode("AST54321");
        asset.setName("Desk");

        AssetResponse response = assetMapper.toResponse(asset);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(asset.getId());
        assertThat(response.getCode()).isEqualTo(asset.getCode());
        assertThat(response.getName()).isEqualTo(asset.getName());
        assertThat(response.getSpecification()).isNull();
        assertThat(response.getInstalledDate()).isNull();
        assertThat(response.getState()).isEqualTo(AssetState.AVAILABLE.name()); // Default value
        assertThat(response.getCategory()).isNull();
        assertThat(response.getLocation()).isNull();
    }
    @Test
    void toAssetPageResponseDto_ShouldMapFieldsCorrectly() {
        // Arrange
        Asset asset = new Asset();
        asset.setCode("AST99999");
        asset.setName("Keyboard");
        asset.setState(AssetState.NOT_AVAILABLE);

        Category category = new Category();
        category.setId(2);
        category.setName("Peripheral");
        category.setPrefix("PR");
        asset.setCategory(category);

        // Act
        AssetPageResponse response = assetMapper.toAssetPageResponseDto(asset);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("AST99999");
        assertThat(response.getName()).isEqualTo("Keyboard");
        assertThat(response.getCategoryName()).isEqualTo("Peripheral");
        assertThat(response.getState()).isEqualTo(AssetState.NOT_AVAILABLE);
    }

}