import { AssignmentService } from '@/services/AssignmentService';
import { Response } from '@/types/dto';
import { AssignmentListParams, AssignmentResponse } from '@/types/assignment';
import { useCallback, useState } from 'react';
import { useApiError } from '../useApiError';
import useSWR from 'swr';
import { isUUID } from 'validator';
import { AssignmentFormRequest } from '@/utils/form/schemas/create-assignment-schema';
import { mutateKeyContain } from '@/utils/revalidateUtils';

export const useCreateAssignment = () => {
  const [isCreating, setIsCreating] = useState(false);
  const { setApiError, clearErrors } = useApiError();
  const createAssignment = useCallback(
    async (assignment: AssignmentFormRequest): Promise<Response<AssignmentResponse>> => {
      clearErrors();
      setIsCreating(true);
      try {
        const response = await AssignmentService.createAssignment(assignment);

        if (!response.success) {
          setApiError(response);
        }

        return response;
      } catch (error) {
        console.error('Error creating assignment:', error);
        throw error;
      } finally {
        setIsCreating(false);
      }
    },
    [clearErrors, setApiError]
  );
  return {
    createAssignment,
    isCreating,
  };
};

export const useAssignmentList = (params: AssignmentListParams) => {
  const queryKey = ['personalAssignments', params];

  const { data, error, isLoading, mutate } = useSWR(queryKey, () =>
    AssignmentService.getAssignments(params)
  );

  return {
    assignments: data?.data?.content ?? [],
    pagination: data?.data?.pageable,
    isLoading,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};

export const useUpdateAssignmentStatus = () => {
  const [isUpdating, setIsUpdating] = useState(false);
  const { setApiError, clearErrors } = useApiError();
  const updateAssignmenStatus = useCallback(
    async ({
      id,
      status,
    }: {
      id: string;
      status: string;
    }): Promise<Response<AssignmentResponse>> => {
      clearErrors();
      setIsUpdating(true);
      try {
        const response = await AssignmentService.updateAssignmentStatus(id, status);

        if (!response.success) {
          setApiError(response);
        }

        mutateKeyContain('personalAssignments');
        return response;
      } catch (error) {
        console.error('Error updating assignment:', error);
        throw error;
      } finally {
        setIsUpdating(false);
      }
    },
    [clearErrors, setApiError]
  );
  return {
    updateAssignmenStatus,
    isUpdating,

  };
};

export const useAssignment = (id?: string) => {
  const isValidId = typeof id === 'string' && isUUID(id);
  const key = isValidId ? (['assignments', id] as const) : null;

  const {
    data: response,
    error,
    isLoading,
    isValidating,
    mutate,
  } = useSWR(key, ([, uuid]) => AssignmentService.getAssignmentEditView(uuid), {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
  });

  return {
    assignment: response?.data ?? null,
    isLoading,
    isValidating,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};

export const useAssignmentDetail = (id?: string) => {
  const isValidId = typeof id === 'string' && isUUID(id);
  const key = isValidId ? (['assignments', id] as const) : null;

  const {
    data: response,
    error,
    isLoading,
    isValidating,
    mutate,
  } = useSWR(
    key,
    // The fetcher receives the key tuple; we know uuid is defined when key !== null
    ([, uuid]) => AssignmentService.getAssignment(uuid),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    assignment: response?.data ?? null,
    isLoading,
    isValidating,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};
