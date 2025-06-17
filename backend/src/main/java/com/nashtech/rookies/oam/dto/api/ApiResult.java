package com.nashtech.rookies.oam.dto.api;

import com.nashtech.rookies.oam.constant.ErrorCode;

import java.util.List;

public final class ApiResult {
    private ApiResult() {
        // prevent instantiation
    }

    public static <T> ApiGenericResponse<T> success(String message, T data) {
        return new ApiGenericResponse<>(true, message, data);
    }

    public static ApiGenericResponse<Void> success(String message) {
        return new ApiGenericResponse<>(true, message, null);
    }

    public static ApiErrorResponse error(ErrorCode code, String message, Object details) {
        return new ApiErrorResponse(false, new ErrorDetail(code, message, details));
    }

    public static ApiErrorResponse error(ErrorCode code, String message) {
        return new ApiErrorResponse(false, new ErrorDetail(code, message, null));
    }

    public static ApiErrorResponse validationError(List<FieldErrorDetail> fieldErrors) {
        return error(ErrorCode.VALIDATION_FAILED, "Input validation failed", fieldErrors);
    }
}