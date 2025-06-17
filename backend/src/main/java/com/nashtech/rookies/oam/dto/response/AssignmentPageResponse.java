package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignmentPageResponse {
    private UUID id;
    private String assetCode;
    private String assetName;
    private String userId;
    private String createdBy;
    private LocalDate assignedDate;
    private CategoryResponse category;
    private AssignmentStatus status;
    private ReturnState returnState;
}
