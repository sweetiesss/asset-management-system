package com.nashtech.rookies.oam.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.nashtech.rookies.oam.model.Asset}
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetResponse {
    private UUID id;
    private String code;
    private String name;
    private String specification;
    private LocalDate installedDate;
    private String state;
    private CategoryResponse category;
    private LocationResponse location;
    private Long version;
}


