import { useCallback, useState } from 'react';
import { AssetReturnService } from '@/services/AssetReturnService';
import { useApiError } from '../useApiError';
import { AssetReturn } from '@/types/asset';
import { Response } from '@/types/dto';

export function useCreateReturnRequest() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const createReturnRequest = async (assignmentId: string) => {
    setIsLoading(true);
    setError(null);

    try {
      await AssetReturnService.createReturnRequest(assignmentId);
    } catch (err) {
      setError(err as Error);
      throw err; 
    } finally {
      setIsLoading(false);
    }
  };

  return {
    createReturnRequest,
    isLoading,
    error,
  };
}

export const useUpdateAssetReturnState = () => {
  const [isUpdating, setIsUpdating] = useState(false);
  const { setApiError, clearErrors } = useApiError();
  const updateAssetReturnState = useCallback(
    async ({
      id,
      state,
    }: {
      id: string;
      state: string;
    }): Promise<Response<AssetReturn>> => {
      clearErrors();
      setIsUpdating(true);
      try {
        const response = await AssetReturnService.updateAssetReturnState(id, state);

        if (!response.success) {
          setApiError(response);
        }

        // mutateKeyContain('personalAssignments');
        return response;
      } catch (error) {
        console.error('Error updating asset returns:', error);
        throw error;
      } finally {
        setIsUpdating(false);
      }
    },
    [clearErrors, setApiError]
  );
  return {
    updateAssetReturnState,
    isUpdating,
  };
};
