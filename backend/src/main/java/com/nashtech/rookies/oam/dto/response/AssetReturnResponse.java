package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.enums.ReturnState;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetReturnResponse {
    private UUID id;
    private UUID assignmentId;
    private ReturnState state;
    private LocalDate returnedDate;
}
