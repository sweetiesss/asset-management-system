package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.validation.CaseInsensitiveEnumMatch;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetReturnRequest {
    @NotBlank(message = "This is required field")
    @CaseInsensitiveEnumMatch(enumClass = ReturnState.class, message = "State must be either COMPLETED, WAITING_FOR_RETURNING, CANCELED")
    private String state;
}
