package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.projection.AssetAssignmentEditViewProjection;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentEditViewResponse {
    UUID id;
    UserAssignmentEditView user;
    AssetAssignmentEditViewProjection asset;
    String note;
    LocalDate assignedDate;
    Long version;
}
