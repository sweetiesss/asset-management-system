package com.nashtech.rookies.oam.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface EditAssetProjection {
    UUID getId();
    
    String getName();

    CategoryProjection getCategory();

    String getSpecification();

    LocalDate getInstalledDate();

    String getState();

    Long getVersion();
}
