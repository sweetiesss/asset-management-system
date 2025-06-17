import { useState, useCallback } from 'react';
import { ErrorCode, getErrorMessage } from '@/configs/error-codes';
import type { Response } from '@/types/dto';

/**
 * Custom hook for handling API errors in components
 * Provides methods for setting, clearing, and checking errors
 */
export function useApiError<T = unknown>() {
  const [error, setError] = useState<string | null>(null);
  const [errorCode, setErrorCode] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  /**
   * Set an error from an API response
   */
  const setApiError = useCallback((response: Response<T>) => {
    if (response.error) {
      setErrorCode(response.error.code || ErrorCode.UNKNOWN_ERROR);
      setError(response.error.message || getErrorMessage(response.error.code || ''));

      if (response.error.details && Array.isArray(response.error.details)) {
        const newFieldErrors: Record<string, string> = {};
        response.error.details.forEach((error) => {
          if (error.field && error.message) {
            newFieldErrors[error.field] = error.message;
          }
        });
        setFieldErrors(newFieldErrors);
      }
    }
  }, []);

  /**
   * Clear all errors
   */
  const clearErrors = useCallback(() => {
    setError(null);
    setErrorCode(null);
    setFieldErrors({});
  }, []);

  /**
   * Check if a specific error code is present
   */
  const hasErrorCode = useCallback(
    (code: string) => {
      return errorCode === code;
    },
    [errorCode]
  );

  /**
   * Get a field error message if it exists
   */
  const getFieldError = useCallback(
    (fieldName: string): string | undefined => {
      return fieldErrors[fieldName];
    },
    [fieldErrors]
  );

  return {
    error,
    errorCode,
    fieldErrors,
    setApiError,
    clearErrors,
    hasErrorCode,
    getFieldError,
  };
}
