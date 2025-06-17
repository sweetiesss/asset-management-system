package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.validation.CaseInsensitiveEnumMatch;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAssignmentStatusRequest {
    @NotBlank(message = "This is required field")
    @CaseInsensitiveEnumMatch(enumClass = AssignmentStatusType.class, message = "Status must be either ACCEPTED, DECLINED, WAITING_FOR_ACCEPTANCE")
    private String status;
}
