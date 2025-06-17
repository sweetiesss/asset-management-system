import { passwordService } from '@/services/PasswordService';
import { ChangePasswordRequest } from '@/utils/form/schemas/change-password-schema';
import { useState } from 'react';

export function usePassword() {
  const [error, setError] = useState<string | null>(null);
  const [errorCode, setErrorCode] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState<boolean | null>(null);
  const [isFetching, setIsFetching] = useState<boolean>(false);

  const updatePassword = async (request: ChangePasswordRequest) => {
    setIsFetching(true);
    const res = await passwordService.changePassword(request);
    if (res.success) {
      setIsSuccess(true);
      setError(null);
      setErrorCode(null);
    } else {
      setIsSuccess(false);
      setError(res.error?.message || 'An unknown error occurred');
      setErrorCode(res.error?.code || 'UNKNOWN_ERROR');
    }
    setIsFetching(false);
  };

  const resetErrors = () => {
    setError(null);
    setErrorCode(null);
  };

  return { updatePassword, errorCode, error, resetErrors, isSuccess, isFetching };
}
