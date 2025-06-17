import { AssetReturnListParams } from '@/types/assetReturn';
import { useGetPage } from '.';
import { AssetReturnService } from '@/services/AssetReturnService';

export const useGetAssetReturns = (params: AssetReturnListParams) => {
  return useGetPage('asset-returns', AssetReturnService.getAssetReturns, params);
};
