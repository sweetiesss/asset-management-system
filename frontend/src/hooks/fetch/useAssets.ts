import { AssetHistoryParams, AssetService } from '@/services/AssetService';
import useSWR, { } from 'swr';

export const useAssetHistory = (id: string, params?: AssetHistoryParams) => {
  const queryKey = ['assets', params];
  const { data, error, isLoading, mutate } = useSWR(queryKey, () =>
    AssetService.getAssetHistory(id, params)
  );
  return {
    history: data?.data?.content ?? [],
    pagination: data?.data?.pageable,
    isLoading,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};
