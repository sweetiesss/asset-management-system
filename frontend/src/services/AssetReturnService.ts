import { ExpectedResponse } from '@/types/dto';
import { getEntities, postEntity } from '.';
import { AssetReturn } from '@/types/asset';
import { AssetReturnType } from '@/types/assetReturn';
import { AssetReturnListParams } from '@/types/assetReturn';
import { Response } from '@/types/dto';
import { handleApiError } from '@/utils/api';
import { authApi } from '@/configs/axios';

export const AssetReturnService = {
  createReturnRequest: async (assignmentId: string): Promise<ExpectedResponse<AssetReturn>> => {
    const endpoint = `assignments/${assignmentId}/asset-returns`;
    return postEntity<AssetReturn, object>(endpoint, {});
  },

  getAssetReturns: async (params: AssetReturnListParams) => {
    const endpoint = '/asset-returns';
    return getEntities<AssetReturnType<'table'>, AssetReturnListParams>(endpoint, params);
  },

  updateAssetReturnState: async (
    id: string,
    state: string
  ): Promise<Response<AssetReturn>> => {
    try {
      const response = await authApi.patch<Response<AssetReturn>>(
        `/asset-returns/${id}`,
        { state }
      );
      return response.data;
    } catch (error: unknown) {
      return handleApiError<AssetReturn>(error);
    }
  },
};
