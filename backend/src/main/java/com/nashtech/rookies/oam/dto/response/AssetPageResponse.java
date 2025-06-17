package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.enums.AssetState;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AssetPageResponse {
    private UUID id;
    private String code;
    private String name;
    private String categoryName;
    private AssetState state;
}
