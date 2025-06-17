package com.nashtech.rookies.oam.exception.handler;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.api.ApiErrorResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.api.FieldErrorDetail;
import com.nashtech.rookies.oam.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.AUTH_CREDENTIALS_NOT_FOUND, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientAuth(InsufficientAuthenticationException ex) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.AUTHENTICATION_NOT_FOUND, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiErrorResponse response = ApiResult.validationError(errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> resourceNotFound(ResourceNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.RESOURCE_NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.BAD_CREDENTIALS, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }


    @ExceptionHandler(RefreshTokenMissingException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenMissing(RefreshTokenMissingException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledException(DisabledException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ACCOUNT_DISABLED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenExpiredException(TokenExpiredException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.TOKEN_EXPIRED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(CategoryNameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNameAlreadyExistsException(CategoryNameAlreadyExistsException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(CategoryPrefixAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryPrefixAlreadyExistsException(CategoryPrefixAlreadyExistsException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.CATEGORY_PREFIX_ALREADY_EXISTS, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AssetNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetNotFoundException(AssetNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(AssetNotEditableException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetNotEditableException(AssetNotEditableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_NOT_EDITABLE, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(AssetNotDeletableException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetNotDeletableException(AssetNotDeletableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_NOT_DELETABLE, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    @ExceptionHandler(AssignmentNotUpdatableException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentNotUpdatableException(AssignmentNotUpdatableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_NOT_UPDATABLE, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentNotFoundException(AssignmentNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AssignmentAlreadyDeletedException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentAlreadyDeletedException(AssignmentAlreadyDeletedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.SYSTEM_INTERNAL_ERROR, e.getMessage());
        log.error("An error occurred: " + e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.USER_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.CATEGORY_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CategoryEmptyException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryEmptyException(CategoryEmptyException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.CATEGORY_EMPTY, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFoundException(RoleNotFoundException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ROLE_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @ExceptionHandler(AssignmentStatusNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentStatusNotFoundException(AssignmentStatusNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AssetNotAvailableException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetNotAvailableException(AssetNotAvailableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_NOT_AVAILABLE, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException e, HttpServletRequest request) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.OPTIMISTIC_LOCKING_FAILURE, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ACCESS_DENIED, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException ex) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiErrorResponse response = ApiResult.validationError(errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(RequestReturnAssetAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> requestReturnAssetAlreadyExistsException(RequestReturnAssetAlreadyExistsException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.REQUEST_RETURN_ASSET_ALREADY_EXISTS, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AssignmentNotAcceptedException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentNotAcceptedException(AssignmentNotAcceptedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_NOT_ACCEPTED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AssignmentBeingModifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentBeingModifiedException(AssignmentBeingModifiedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSIGNMENT_BEING_MODIFIED, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AssetAlreadyReturnedException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetAlreadyReturnedException(AssetAlreadyReturnedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_ALREADY_RETURNED, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AssetReturnStateInvalidException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetReturnStateInvalid(AssetReturnStateInvalidException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_RETURN_STATE_INVALID, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RequestReturnNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReturnRequestNotFound(RequestReturnNotFoundException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.REQUEST_RETURN_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AssetReturnRequestNotUpdatableException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetReturnRequestNotUpdatable(AssetReturnRequestNotUpdatableException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_RETURN_NOT_UPDATABLE, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidRequestReturnStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestReturnState(InvalidRequestReturnStateException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.INVALID_ASSET_RETURN_STATE, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ReturnAssetBeingModifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleReturnAssetBeingModified(ReturnAssetBeingModifiedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.RETURN_ASSET_BEING_MODIFIED, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handle AccessDeniedException in service, because CustomAccessDeniedHandler won't catch there
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ACCESS_DENIED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(DisableUserException.class)
    public ResponseEntity<ApiErrorResponse> handleDisableUserException(DisableUserException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.USER_CAN_NOT_BE_DISABLED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AssetBeingModifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleAssetBeingModifiedException(AssetBeingModifiedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.ASSET_BEING_MODIFIED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(UserBeingModifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserBeingModifiedException(UserBeingModifiedException e) {
        ApiErrorResponse response = ApiResult.error(ErrorCode.USER_BEING_MODIFIED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
