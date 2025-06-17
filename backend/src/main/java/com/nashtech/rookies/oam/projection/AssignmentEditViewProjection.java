package com.nashtech.rookies.oam.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface AssignmentEditViewProjection {
    UUID getId();

    UserAssignmentEditViewProjection getUser();

    AssetAssignmentEditViewProjection getAsset();

    LocalDate getAssignedDate();

    String getNote();

    Long getVersion();
}
