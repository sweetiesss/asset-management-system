package com.nashtech.rookies.oam.util;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.api.ApiErrorResponse;
import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ResponseEntity<ApiGenericResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResult.success(message, data));
    }

    public static ResponseEntity<ApiGenericResponse<Void>> success(String message) {
        return ResponseEntity.ok(ApiResult.success(message));
    }

    public static <T> ResponseEntity<ApiGenericResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(message, data));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static ResponseEntity<ApiErrorResponse> badRequest(ErrorCode code, String message, Object details) {
        return ResponseEntity.badRequest().body(ApiResult.error(code, message, details));
    }

    public static ResponseEntity<ApiErrorResponse> badRequest(ErrorCode code, String message) {
        return ResponseEntity.badRequest().body(ApiResult.error(code, message));
    }

    public static ResponseEntity<ApiErrorResponse> notFound(ErrorCode code, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.error(code, message));
    }

    public static ResponseEntity<ApiErrorResponse> internalError(ErrorCode code, String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.error(code, message));
    }
}
