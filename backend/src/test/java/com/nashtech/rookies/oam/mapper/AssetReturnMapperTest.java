package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.AssetReturn;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AssetReturnMapperTest {

    private AssetReturnMapper mapper;

    @BeforeEach
    void setUp() {
        // If you use MapStruct generated implementation
        mapper = new AssetReturnMapperImpl();
    }

    @Test
    void testToAssetReturnPageResponse() {
        // Setup nested entities
        Asset asset = Asset.builder()
                .id(UUID.randomUUID())
                .code("ASSET001")
                .name("Laptop Dell XPS")
                .specification("16GB RAM, 512GB SSD")
                .installedDate(LocalDate.of(2022, 1, 1))
                .version(1L)
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .assignedDate(LocalDate.of(2023, 3, 15))
                .build();

        // Audit fields come from AuditableEntity; for simplicity, set manually or mock
        AssetReturn assetReturn = AssetReturn.builder()
                .id(UUID.randomUUID())
                .assignment(assignment)
                .returnedDate(LocalDate.of(2023, 5, 10))
                .state(ReturnState.COMPLETED)
                .build();

        // Map entity to DTO
        AssetReturnPageResponse dto = mapper.toAssetReturnPageResponse(assetReturn);

        // Assertions
        assertNotNull(dto);
        assertEquals(assetReturn.getId(), dto.getId());
        assertEquals(asset.getCode(), dto.getAssetCode());
        assertEquals(asset.getName(), dto.getAssetName());
        assertEquals(assetReturn.getCreatedBy(), dto.getCreatedBy());
        assertEquals(assignment.getAssignedDate(), dto.getAssignedDate());
        assertEquals(assetReturn.getUpdatedBy(), dto.getUpdatedBy());
        assertEquals(assetReturn.getReturnedDate(), dto.getReturnedDate());
        assertEquals(assetReturn.getState(), dto.getState());
    }
}
