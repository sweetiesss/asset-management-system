package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.validation.CaseInsensitiveEnumMatch;
import com.nashtech.rookies.oam.model.enums.AssetState;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateAssetRequest {
    @NotBlank(message = "This is required field")
    @Size(min = 1, max = 255, message = "Asset code must be between 1 and 255 characters long")
    @Pattern(regexp = "^[a-zA-Z\\s\\d]*$", message = "Asset name must not contain special characters")
    private String name;

    @NotBlank(message = "This is required field")
    @Size(max = 2000, message = "Specification must be at most 2000 characters long")
    private String specification;

    @NotNull(message = "This is required field")
    @PastOrPresent(message = "Installed date must be in the past or present")
    private LocalDate installedDate;

    @NotNull(message = "This is required field")
    @CaseInsensitiveEnumMatch(enumClass = AssetState.class, message = "State must be either AVAILABLE, NOT_AVAILABLE, WAITING_FOR_RECYCLING or RECYCLED")
    private String state;

    private Long version;
}
