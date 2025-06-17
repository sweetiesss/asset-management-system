package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AssignmentUpdateRequest {
    @NotNull(message = "assignTo (user ID) must not be null")
    private UUID userId;

    @NotNull(message = "assetId must not be null")
    private UUID assetId;

    @NotNull(message = "assignedDate must not be null")
    private LocalDate assignedDate;

    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;

    private Long version;
}
