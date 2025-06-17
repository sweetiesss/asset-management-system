package com.nashtech.rookies.oam.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ACCESS_DENIED("Access denied"),
    ROLE_NOT_FOUND("Role not found"),
    STAFF_CODE_COUNT_NOT_FOUND("Staff code count not found"),
    USER_NOT_FOUND("User not found"),
    AUTHENTICATION_NOT_FOUND("Authentication not found"),
    LOCATION_NOT_FOUND("Location not found"),
    VALIDATION_FAILED("Validation failed"),
    BAD_REQUEST("Bad request"),
    RESOURCE_NOT_FOUND("Resource not found"),
    BAD_CREDENTIALS("Bad credentials"),
    SYSTEM_INTERNAL_ERROR("System internal error"),
    AUTH_CREDENTIALS_NOT_FOUND("Authentication credentials not found"),
    AUTH_PRINCIPAL_TYPE_MISMATCH("Authenticated principal type mismatch"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    ACCOUNT_DISABLED("Account disabled"),
    TOKEN_EXPIRED("Token expired"),
    OLD_PASSWORD_NOT_MATCH ("PASSWORD_DOES_NOT_MATCH"),
    NEW_PASSWORD_MUST_BE_DIFFERENT("NEW_PASSWORD_MUST_BE_DIFFERENT"),
    OLD_PASSWORD_NULL("OLD_PASSWORD_NULL"),
    CATEGORY_ALREADY_EXISTS("Category already exists"),
    CATEGORY_NAME_ALREADY_EXISTS("Category name already exists"),
    CATEGORY_PREFIX_ALREADY_EXISTS("Category prefix already exists"),
    OPTIMISTIC_LOCKING_FAILURE("Optimistic locking failure"),
    ASSET_CODE_COUNT_NOT_FOUND("Asset code count not found"),
    CATEGORY_NOT_FOUND("Category not found"),
    CATEGORY_EMPTY("Category empty"),
    ASSET_NOT_FOUND("Asset does not exist or has already been deleted"),
    ASSET_NOT_EDITABLE("The asset cannot be modified due to its current state or the user is not allowed to edit it"),
    ASSET_BEING_MODIFIED("Asset is currently being modified by another user"),
    ASSIGNMENT_NOT_FOUND("Assignment does not exist or has already been deleted"),
    ASSIGNMENT_STATUS_NOT_FOUND("Assignment status not found"),
    ASSIGNMENT_DATE_UPDATE_INVALID("The updated Assigned Date must be later than the original Assigned Date"),
    ASSET_NOT_AVAILABLE("Asset not available for assignment"),
    ASSET_NOT_DELETABLE("Asset cannot be deleted because it is currently assigned or has been assigned in the past"),
    ASSIGNMENT_NOT_UPDATABLE("The assignment cannot be updated due to its current status or the user is not allowed to update it"),
    USER_AND_ASSET_LOCATION_MISMATCH("User and asset location mismatch"),
    ASSIGNMENT_NOT_DELETABLE("The assignment cannot be deleted due to its current status"),
    ASSIGNMENT_BEING_MODIFIED("Assignment is currently being modified by another user"),
    REQUEST_RETURN_ASSET_ALREADY_EXISTS("Asset return already exists for this assignment"),
    ASSIGNMENT_NOT_ACCEPTED("Assignment is not accepted and cannot be returned"),
    ASSET_ALREADY_RETURNED("Asset has already been returned"),
    USER_ASSIGNMENT_ACCESS_DENIED("You are not authorized to access assignments of this user"),
    ASSET_RETURN_STATE_INVALID("Return state invalid"),
    REQUEST_RETURN_NOT_FOUND("Asset return request not found"),
    ASSET_RETURN_NOT_UPDATABLE("Asset return is not updatable"),
    INVALID_ASSET_RETURN_STATE("Invalid asset return state"),
    RETURN_ASSET_BEING_MODIFIED("Return asset is currently being modified by another user"),
    USER_CAN_NOT_BE_DISABLED("User cannot be disabled because they have active assignments"),
    USER_BEING_MODIFIED("User is currently being modified by another user"),
    ;

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
