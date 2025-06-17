package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.AssignmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignmentResponse {
    private UUID id;
    private String assetCode;
    private String assetName;
    private String assignTo;
    private String assignBy;
    private LocalDate assignedDate;
    private AssignmentStatus state;
}
