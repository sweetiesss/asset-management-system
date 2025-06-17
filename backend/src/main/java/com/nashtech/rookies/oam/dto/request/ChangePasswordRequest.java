package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
    private String oldPassword;

    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character from @$!%*?&#"
    )
    private String newPassword;

}
