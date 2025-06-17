package com.nashtech.rookies.oam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for {@link com.nashtech.rookies.oam.model.Location}
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LocationResponse {
    UUID id;
    String code;
    String name;
}