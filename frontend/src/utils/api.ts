import type { Response } from '@/types/dto';
import { ErrorCode, getErrorMessage } from '@/configs/error-codes';
import { toast } from 'react-toastify';
import { AxiosError } from 'axios';

/**
 * Handles API errors and returns a standardized response object
 * Extracts error codes and messages from the backend response
 */
export const handleApiError = <T>(error: unknown, showToast = false): Response<T> => {
  let errorResponse: Response<T>;

  if (error instanceof AxiosError && error.response?.data) {
    // Extract the error from the Axios response
    errorResponse = error.response.data as Response<T>;

    // If the error code exists but no message is provided, use our predefined messages
    if (errorResponse.error?.code && !errorResponse.error.message) {
      errorResponse.error.message = getErrorMessage(errorResponse.error.code);
    }

    // Handle specific error codes
    handleSpecificErrors(errorResponse.error?.code);
  } else if (error instanceof Error) {
    // Handle regular JavaScript errors
    errorResponse = {
      success: false,
      error: {
        message: error.message,
        code: ErrorCode.UNKNOWN_ERROR,
      },
    } as Response<T>;
  } else {
    // Handle unknown errors
    errorResponse = {
      success: false,
      error: {
        message: 'An unknown error occurred',
        code: ErrorCode.UNKNOWN_ERROR,
      },
    } as Response<T>;
  }

  // Optionally show a toast notification
  if (showToast && errorResponse.error?.message) {
    toast.error(errorResponse.error.message);
  }

  return errorResponse;
};

/**
 * Handle specific error codes that require special treatment
 * For example, redirect to login page on authentication errors
 */
function handleSpecificErrors(errorCode?: string): void {
  if (!errorCode) return;

  switch (errorCode) {
    case ErrorCode.TOKEN_EXPIRED:
    case ErrorCode.UNAUTHORIZED:
      // Redirect to login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      break;

    // Add other special cases as needed
  }
}
