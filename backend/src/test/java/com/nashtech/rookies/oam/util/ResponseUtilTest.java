package com.nashtech.rookies.oam.util;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.api.ApiErrorResponse;
import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseUtilTest {

    @Test
    void testSuccessWithData() {
        String message = "Success";
        String data = "Test Data";

        ResponseEntity<ApiGenericResponse<String>> response = ResponseUtil.success(message, data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(message);
        assertThat(response.getBody().data()).isEqualTo(data);
    }

    @Test
    void testSuccessWithoutData() {
        String message = "Operation done";

        ResponseEntity<ApiGenericResponse<Void>> response = ResponseUtil.success(message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(message);
    }

    @Test
    void testCreated() {
        String message = "Resource created";
        String data = "New Object";

        ResponseEntity<ApiGenericResponse<String>> response = ResponseUtil.created(message, data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(message);
        assertThat(response.getBody().data()).isEqualTo(data);
    }

    @Test
    void testNoContent() {
        ResponseEntity<Void> response = ResponseUtil.noContent();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testBadRequestWithDetails() {
        String message = "Bad request occurred";
        Object details = "Invalid data";
        ErrorCode code = ErrorCode.BAD_REQUEST;

        ResponseEntity<ApiErrorResponse> response = ResponseUtil.badRequest(code, message, details);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).isEqualTo(message);
        assertThat(response.getBody().error().code()).isEqualTo(code);
        assertThat(response.getBody().error().details()).isEqualTo(details);
    }

    @Test
    void testBadRequestWithoutDetails() {
        String message = "Missing required field";
        ErrorCode code = ErrorCode.BAD_REQUEST;

        ResponseEntity<ApiErrorResponse> response = ResponseUtil.badRequest(code, message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).isEqualTo(message);
        assertThat(response.getBody().error().code()).isEqualTo(code);
    }

    @Test
    void testNotFound() {
        String message = "User not found";
        ErrorCode code = ErrorCode.USER_NOT_FOUND;

        ResponseEntity<ApiErrorResponse> response = ResponseUtil.notFound(code, message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).isEqualTo(message);
        assertThat(response.getBody().error().code()).isEqualTo(code);
    }

    @Test
    void testInternalError() {
        String message = "Unexpected error";
        ErrorCode code = ErrorCode.SYSTEM_INTERNAL_ERROR;

        ResponseEntity<ApiErrorResponse> response = ResponseUtil.internalError(code, message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).isEqualTo(message);
        assertThat(response.getBody().error().code()).isEqualTo(code);
    }
}
