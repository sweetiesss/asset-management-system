package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.enums.ReturnState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AssetReturnPageResponse {
    private UUID id;
    private String assetCode;
    private String assetName;
    private String createdBy;
    private LocalDate assignedDate;
    private String updatedBy;
    private LocalDate returnedDate;
    private ReturnState state;
}
