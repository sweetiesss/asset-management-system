package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Pattern(regexp = "^[a-zA-Z\\s\\d]*$", message = "Category name must not contain special characters")
    private String name;

    @NotBlank(message = "Category prefix is required")
    @Pattern(regexp = "^[A-Z]{2}", message = "Category prefix must be exactly 2 uppercase letters")
    private String prefix;
}
