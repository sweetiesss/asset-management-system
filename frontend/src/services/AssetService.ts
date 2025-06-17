import { authApi } from '@/configs/axios';
import { ExpectedResponse, type Response } from '@/types/dto';
import type { Page } from '@/types/type';
import type { Asset, AssetHistory, AssetListParams } from '@/types/asset';
import { handleApiError } from '@/utils/api';
import type { CreateAssetFormRequest } from '@/utils/form/schemas/create-asset-schema';
import { EditAssetFormRequest } from '@/utils/form/schemas/edit-asset-schema';
import { AxiosResponse } from 'axios';
import { deleteEntity, getEntities, getEntity, patchEntity, postEntity } from '.';

export interface AssetHistoryParams {
  page?: number;
  size?: number;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
}

export const AssetService = {
  /**
   * Create a new asset
   * @param data Asset data
   * @returns Promise with created asset or error
   */
  createAsset: async (data: CreateAssetFormRequest) => {
    const endpoint = 'assets';
    return postEntity<Asset<'basic'>, CreateAssetFormRequest>(endpoint, data);
  },

  get: async (id: string, mode: 'edit-view' | 'detail'): Promise<Response<Asset<'basic'>>> => {
    try {
      let response: AxiosResponse<Response<Asset<'basic'>>>;
      switch (mode) {
        case 'edit-view':
          response = await authApi.get<Response<Asset<'basic'>>>(`/assets/${id}/edit-view`);
          break;
        case 'detail':
          response = await authApi.get<Response<Asset<'detail'>>>(`/assets/${id}`);
          break;
        default:
          throw new Error('Invalid mode specified for asset retrieval');
      }

      return response.data;
    } catch (error: unknown) {
      return handleApiError<Asset<'basic'>>(error);
    }
  },

  patch: (id: string, asset: EditAssetFormRequest) => {
    const endpoint = 'assets';
    const postData: Omit<EditAssetFormRequest, 'categoryName'> = {
      name: asset.name,
      specification: asset.specification,
      installedDate: asset.installedDate,
      state: asset.state,
      version: asset.version,
    };
    return patchEntity<Asset<'basic'>, Omit<EditAssetFormRequest, 'categoryName'>>(
      endpoint,
      postData,
      id
    );
  },

  getAssets: async (params: AssetListParams): Promise<ExpectedResponse<Page<Asset<'table'>>>> => {
    const endpoint = 'assets';
    return getEntities(endpoint, params);
  },

  getAssetHistory: async (
    id: string,
    params?: AssetHistoryParams
  ): Promise<Response<Page<AssetHistory>>> => {
    try {
      const response = await authApi.get<Response<Page<AssetHistory>>>(
        `/assets/${id}/assignments`,
        { params }
      );
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  deleteAsset: async (id: string): Promise<ExpectedResponse<void>> => {
    const endpoint = 'assets';
    return deleteEntity(endpoint, id);
  },

  getDetail: async (id: string): Promise<ExpectedResponse<Asset<'detail'>>> => {
    const endpoint = `assets/${id}`;
    return getEntity<Asset<'detail'>>(endpoint);
  },

  getBasic: async (id: string): Promise<ExpectedResponse<Asset<'basic'>>> => {
    const endpoint = `assets/${id}`;
    return getEntity<Asset<'basic'>>(endpoint);
  },
};
