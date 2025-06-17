package com.nashtech.rookies.oam.dto.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE;
import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE_SIZE;
import static com.nashtech.rookies.oam.constant.SortConstants.ASC;

@Data
public class PageableRequest {
    @Schema(description = "Page number (0-based)", example = DEFAULT_PAGE, defaultValue = DEFAULT_PAGE)
    @Min(value = 0, message = "Page number must be 0 or greater")
    private Integer page = Integer.parseInt(DEFAULT_PAGE);

    @Schema(description = "Page size", example = DEFAULT_PAGE_SIZE, defaultValue = DEFAULT_PAGE_SIZE)
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private String sort;

    @Schema(description = "Sort order", example = "ASC", defaultValue = ASC)
    private String sortOrder = ASC;

    @Schema(description = "Search keyword", example = "", defaultValue = "")
    private String search = "";
}
