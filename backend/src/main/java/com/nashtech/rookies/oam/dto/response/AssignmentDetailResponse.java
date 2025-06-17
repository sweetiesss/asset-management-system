package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AssignmentDetailResponse {
    String assetCode;
    String assetName;
    String specification;
    String userId;
    String createdBy;
    LocalDate assignedDate;
    AssignmentStatus status;
    String note;
}
