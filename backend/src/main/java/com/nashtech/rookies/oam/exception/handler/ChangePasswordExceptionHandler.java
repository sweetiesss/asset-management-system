package com.nashtech.rookies.oam.exception.handler;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.api.ApiErrorResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.exception.OldPasswordNotMatchException;
import com.nashtech.rookies.oam.exception.OldPasswordNullException;
import com.nashtech.rookies.oam.exception.PasswordUnchangedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class ChangePasswordExceptionHandler {
    private final String USER_NOT_FOUND_ERROR_CODE = "USER_NOT_FOUND";
    private final String OLD_PASSWORD_NOT_MATCH_ERROR_CODE = "PASSWORD_DOES_NOT_MATCH";
    private final String NEW_PASSWORD_MUST_BE_DIFFERENT_ERROR_CODE ="NEW_PASSWORD_MUST_BE_DIFFERENT";
    private final String OLD_PASSWORD_NULL_ERROR_CODE = "OLD_PASSWORD_NULL";

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, HttpServletRequest request,
                                              String errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", errorCode);
        return problemDetail;
    }


    @ExceptionHandler(OldPasswordNotMatchException.class)
    public ResponseEntity<ApiErrorResponse> handleOldPasswordNotMatchException(OldPasswordNotMatchException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.OLD_PASSWORD_NOT_MATCH, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OldPasswordNullException.class)
    public ResponseEntity<ApiErrorResponse> handleOldPasswordNullException(OldPasswordNullException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.OLD_PASSWORD_NULL, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PasswordUnchangedException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordUnchangedException(PasswordUnchangedException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}