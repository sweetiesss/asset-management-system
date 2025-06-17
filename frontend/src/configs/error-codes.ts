/**
 * Error codes that match the backend ErrorCode enum
 * These should be kept in sync with the backend
 */
export enum ErrorCode {
  // Authentication errors
  INVALID_CREDENTIALS = 'INVALID_CREDENTIALS',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  TOKEN_EXPIRED = 'TOKEN_EXPIRED',
  BAD_CREDENTIALS = 'BAD_CREDENTIALS',

  // User errors
  USER_NOT_FOUND = 'USER_NOT_FOUND',
  USERNAME_ALREADY_EXISTS = 'USERNAME_ALREADY_EXISTS',
  EMAIL_ALREADY_EXISTS = 'EMAIL_ALREADY_EXISTS',
  INVALID_USER_TYPE = 'INVALID_USER_TYPE',

  // Password errors
  OLD_PASSWORD_NOT_MATCH = 'OLD_PASSWORD_NOT_MATCH',
  NEW_PASSWORD_MUST_BE_DIFFERENT = 'NEW_PASSWORD_MUST_BE_DIFFERENT',
  PASSWORD_DOES_NOT_MATCH = 'PASSWORD_DOES_NOT_MATCH',

  // Category errors
  CATEGORY_NOT_FOUND = 'CATEGORY_NOT_FOUND',
  CATEGORY_NAME_ALREADY_EXISTS = 'CATEGORY_NAME_ALREADY_EXISTS',
  CATEGORY_PREFIX_ALREADY_EXISTS = 'CATEGORY_PREFIX_ALREADY_EXISTS',

  // Asset errors
  ASSET_NOT_FOUND = 'ASSET_NOT_FOUND',
  ASSET_CODE_ALREADY_EXISTS = 'ASSET_CODE_ALREADY_EXISTS',

  // Validation errors
  VALIDATION_ERROR = 'VALIDATION_ERROR',

  // General errors
  INTERNAL_SERVER_ERROR = 'INTERNAL_SERVER_ERROR',
  BAD_REQUEST = 'BAD_REQUEST',
  NOT_FOUND = 'NOT_FOUND',

  // Default
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

/**
 * User-friendly error messages mapped to error codes
 * This centralizes all error messages for consistency
 */
export const ERROR_MESSAGES: Record<ErrorCode | string, string> = {
  // Authentication errors
  [ErrorCode.INVALID_CREDENTIALS]: 'Invalid username or password',
  [ErrorCode.UNAUTHORIZED]: 'You are not authorized to access this resource',
  [ErrorCode.FORBIDDEN]: "You don't have permission to perform this action",
  [ErrorCode.TOKEN_EXPIRED]: 'Your session has expired. Please log in again',
  [ErrorCode.BAD_CREDENTIALS]: 'Invalid username or password',

  // User errors
  [ErrorCode.USER_NOT_FOUND]: 'User not found',
  [ErrorCode.USERNAME_ALREADY_EXISTS]: 'Username is already taken',
  [ErrorCode.EMAIL_ALREADY_EXISTS]: 'Email is already registered',
  [ErrorCode.INVALID_USER_TYPE]: 'Invalid user type',

  // Password errors
  [ErrorCode.OLD_PASSWORD_NOT_MATCH]: 'Current password is incorrect',
  [ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT]:
    'New password must be different from the current password',
  [ErrorCode.PASSWORD_DOES_NOT_MATCH]: 'Passwords do not match',

  // Category errors
  [ErrorCode.CATEGORY_NOT_FOUND]: 'Category not found',
  [ErrorCode.CATEGORY_NAME_ALREADY_EXISTS]: 'Category name already exists',
  [ErrorCode.CATEGORY_PREFIX_ALREADY_EXISTS]: 'Category prefix already exists',

  // Asset errors
  [ErrorCode.ASSET_NOT_FOUND]: 'Asset not found',
  [ErrorCode.ASSET_CODE_ALREADY_EXISTS]: 'Asset code already exists',

  // Validation errors
  [ErrorCode.VALIDATION_ERROR]: 'Validation error',

  // General errors
  [ErrorCode.INTERNAL_SERVER_ERROR]: 'An internal server error occurred',
  [ErrorCode.BAD_REQUEST]: 'Invalid request',
  [ErrorCode.NOT_FOUND]: 'Resource not found',

  // Default
  [ErrorCode.UNKNOWN_ERROR]: 'An unknown error occurred',
};

/**
 * Get a user-friendly error message for an error code
 * Falls back to the provided message or a default message if the code is not found
 */
export function getErrorMessage(code: string, fallbackMessage?: string): string {
  return ERROR_MESSAGES[code] || fallbackMessage || ERROR_MESSAGES[ErrorCode.UNKNOWN_ERROR];
}
