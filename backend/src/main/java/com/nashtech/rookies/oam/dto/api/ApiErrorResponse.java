package com.nashtech.rookies.oam.dto.api;


public record ApiErrorResponse(
        boolean success,
        ErrorDetail error
) {
}