package com.nashtech.rookies.oam.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nashtech.rookies.oam.constant.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        ErrorCode code,
        String message,
        Object details
) {
}