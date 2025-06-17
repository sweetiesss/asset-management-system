import { ApiResponse, ErrResponse, ExpectedResponse } from '@/types/dto';
import { BlobData, Page } from '@/types/type';
import { AxiosError } from 'axios';
import { useEffect, useState } from 'react';

import useSWR, { Key, useSWRConfig } from 'swr';
import useSWRMutation, { MutationFetcher } from 'swr/mutation';

/**
 * React hook for performing PATCH (update) operations on entities using SWR mutation.
 * Provides mutation state management, error handling, and cache invalidation for update operations.
 *
 * @template TRequest - The type of the request data for the update
 * @template TResponse - The type of the response data from the update
 * @param {(id: string, data: TRequest) => Promise<TResponse>} patchEntity - Function to perform the patch operation
 * @param {string} id - The ID of the entity to update
 * @param {Key[]} [cacheKeys] - Optional array of cache keys to invalidate after a successful update
 * @returns {Object} An object containing update mutation state and functions
 */
export function usePatch<TRequest, TResponse, CustomError = never>(
  patchEntity: (id: string, data: TRequest) => Promise<ExpectedResponse<TResponse>>,
  id: string,
  cacheKeys: Key[] = []
) {
  const { mutate } = useSWRConfig();

  const mutator: MutationFetcher<TResponse, string, TRequest> = (
    _,
    { arg }: { arg: TRequest }
  ): Promise<TResponse> =>
    patchEntity(id, arg).then((res) => {
      cacheKeys.forEach((key) => mutate(key));
      return res.data;
    });

  const { trigger, isMutating, error, data, reset } = useSWRMutation<
    TResponse,
    AxiosError<ApiResponse<false, never, CustomError>>,
    string,
    TRequest
  >(id, mutator);

  useEffect(() => {
    if (error) {
      console.error('Error updating entity:', error);
    }
  }, [error]);

  return {
    triggerUpdate: trigger,
    isUpdating: isMutating,
    updateError: error ? error.response?.data.error : undefined,
    updateData: data,
    resetUpdateState: reset,
    
  };
}

/**
 * React hook for performing DELETE operations on entities using SWR mutation.
 * Provides mutation state management and error handling for delete operations.
 *
 * @param {(id: string) => Promise<void>} deleteEntity - Function to perform the delete operation
 * @param {string} id - The ID of the entity to delete
 * @returns {Object} An object containing delete mutation state and functions
 * @returns {Function} returns.triggerDelete - Function to trigger the delete mutation
 * @returns {boolean} returns.isDeleting - Whether the deletion is currently in progress
 * @returns {string | undefined} returns.deleteError - Error message if the deletion failed
 * @returns {void | undefined} returns.deleteData - The response data from the deletion
 * @returns {Function} returns.resetDeleteState - Function to reset the mutation state
 */
export function useDelete(
  deleteEntity: (id: string) => Promise<ExpectedResponse<void>>,
  id: string
) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const mutator: MutationFetcher<void, string, void> = (_: string): Promise<void> =>
    deleteEntity(id).then((res) => res.data);

  const { trigger, isMutating, error, data, reset } = useSWRMutation<
    void,
    AxiosError<ApiResponse<false, never, never>>,
    string
  >(id, mutator);

  useEffect(() => {
    if (error) {
      console.error('Error deleting entity:', error);
    }
  }, [error]);

  return {
    triggerDelete: trigger,
    isDeleting: isMutating,
    deleteError: error ? error.response?.data.error : undefined,
    deleteData: data,
    resetDeleteState: reset,
  };
}

/**
 * React hook for performing POST (create) operations on entities using SWR mutation.
 * Provides mutation state management and error handling for create operations.
 *
 * @template CacheKey - The type of the cache key extending SWR Key
 * @template TRequest - The type of the request data for the creation
 * @template TResponse - The type of the response data from the creation
 * @param {CacheKey} key - The cache key for SWR mutation
 * @param {(data: TRequest) => Promise<TResponse>} postEntity - Function to perform the post operation
 * @returns {Object} An object containing post mutation state and functions
 * @returns {Function} returns.triggerPost - Function to trigger the post mutation
 * @returns {boolean} returns.isPosting - Whether the post is currently in progress
 * @returns {string | undefined} returns.postError - Error message if the post failed
 * @returns {TResponse | undefined} returns.postData - The response data from the post
 * @returns {Function} returns.resetPostState - Function to reset the mutation state
 */
export function usePost<CacheKey extends Key, TRequest, TResponse, CustomError = unknown>(
  key: CacheKey,
  postEntity: (data: TRequest) => Promise<ExpectedResponse<TResponse>>
) {
  const mutator: MutationFetcher<TResponse, Key, TRequest> = (
    _: Key,
    { arg }: { arg: TRequest }
  ): Promise<TResponse> => postEntity(arg).then((res) => res.data);

  const { trigger, isMutating, error, data, reset } = useSWRMutation<
    TResponse,
    AxiosError<ErrResponse<CustomError>>,
    Key,
    TRequest
  >(key, mutator);

  useEffect(() => {
    if (error) {
      console.error('Error posting entity:', error);
    }
  }, [error]);

  return {
    triggerPost: trigger,
    isPosting: isMutating,
    postError: error ? error.response?.data.error : undefined,
    postData: data,
    resetPostState: reset,
  };
}

export function useGet<TResponse>(
  endpoint: string,
  getter: (endpoint: string) => Promise<ExpectedResponse<TResponse>>
) {
  const fetcher = (id: string) => getter(id).then((res) => res.data);

  const { data, error, isLoading } = useSWR<TResponse, AxiosError<ErrResponse>>(endpoint, fetcher);

  useEffect(() => {
    if (error) {
      console.error('Error fetching data:', error);
    }
  }, [error]);

  return {
    data,
    error: error?.response?.data?.error,
    isLoading,
  };
}

export function useGetPage<TResponse, QueryParams, CacheKey extends Key = Key>(
  key: CacheKey,
  pageGetter: (params: QueryParams) => Promise<ExpectedResponse<Page<TResponse>>>,
  params: QueryParams
) {
  const queryKey = [key, params];
  const fetcher = (params: QueryParams) => pageGetter(params).then((res) => res.data);
  const { data, error, isLoading, mutate } = useSWR<Page<TResponse>, AxiosError<ErrResponse>>(
    queryKey,
    () => fetcher(params)
  );
  useEffect(() => {
    if (error) {
      console.error('Error fetching page data:', error);
    }
  }, [error]);
  return {
    items: data?.content || [],
    pagination: data?.pageable,
    error: error?.response?.data?.error,
    isLoading,
    mutatePage: mutate,
  };
}

export function useGetBlob<
  TData = { fileName: string; blob: Blob },
  TError = AxiosError,
  CacheKey extends Key = Key,
>(key: CacheKey | null, fetcher: () => Promise<TData>) {
  const [blobUrl, setBlobUrl] = useState<string | undefined>();

  const { data, error, isLoading, mutate } = useSWR<TData, TError>(key, fetcher, {
    revalidateOnFocus: false,
  });

  useEffect(() => {
    if (data && typeof data === 'object' && 'blob' in data && data.blob instanceof Blob) {
      const url = window.URL.createObjectURL(data.blob);
      setBlobUrl(url);
      return () => {
        window.URL.revokeObjectURL(url);
        setBlobUrl(undefined);
      };
    }
  }, [data]);

  return {
    blobUrl,
    fileName:
      data && typeof data === 'object' && 'fileName' in data ? String(data.fileName) : undefined,
    error,
    isLoading,
    mutate,
  };
}

export function useGetWithTrigger<TResponse>(
  endpoint: string,
  getter: (endpoint: string) => Promise<ExpectedResponse<TResponse>>
) {
  const fetcher = (id: string) => getter(id).then((res) => res.data);

  const { data, error, isMutating, trigger, reset } = useSWRMutation<
    TResponse,
    AxiosError<ErrResponse>
  >(endpoint, fetcher);

  useEffect(() => {
    if (error) {
      console.error('Error fetching data:', error);
    }
  }, [error]);

  return {
    data,
    error: error?.response?.data?.error,
    isMutating,
    trigger,
    reset,
  };
}

export function useGetBlobWithTrigger<QueryParam, TError = AxiosError, CacheKey extends Key = Key>(
  key: CacheKey | null,
  fetcher: (params: QueryParam) => Promise<BlobData>
) {
  const [blobUrl, setBlobUrl] = useState<string | undefined>();

  const mutationFetcher = (_key: CacheKey, { arg }: { arg: QueryParam }) => {
    return fetcher(arg);
  };

  const { data, error, isMutating, trigger, reset } = useSWRMutation<
    BlobData,
    TError,
    Key,
    QueryParam
  >(key, mutationFetcher);

  useEffect(() => {
    let url: string | undefined;

    if (data?.blob instanceof Blob) {
      url = window.URL.createObjectURL(data.blob);
      setBlobUrl(url);
    }

    return () => {
      if (url) {
        window.URL.revokeObjectURL(url);
        setBlobUrl(undefined);
      }
    };
  }, [data]);

  return {
    blobUrl,
    fileName: data?.fileName,
    error,
    isMutating,
    trigger,
    reset,
  };
}


export const usePut = <TRequest, TResponse, CustomError = never>(
  putEntity: (id: string, data: TRequest) => Promise<ExpectedResponse<TResponse>>,
  id: string,
  cacheKeys: Key[] = []
) => {
  const { mutate } = useSWRConfig();

  const mutator: MutationFetcher<TResponse, string, TRequest> = (
    _,
    { arg }: { arg: TRequest }
  ): Promise<TResponse> =>
    putEntity(id, arg).then((res) => {
      cacheKeys.forEach((key) => mutate(key));
      return res.data;
    });
  const { trigger, isMutating, error, data, reset } = useSWRMutation<
    TResponse,
    AxiosError<ApiResponse<false, never, CustomError>>,
    string,
    TRequest
  >(id, mutator);
  useEffect(() => {
    if (error) {
      console.error('Error updating entity:', error);
    }
  }, [error]);
  return {
    triggerUpdate: trigger,
    isUpdating: isMutating,
    updateError: error ? error.response?.data.error : undefined,
    updateData: data,
    resetUpdateState: reset,
  };  
}