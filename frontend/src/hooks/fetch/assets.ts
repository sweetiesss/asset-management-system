import { EditAssetFormRequest } from '@/utils/form/schemas/edit-asset-schema';
import { useDelete, useGet, useGetPage, usePatch, usePost } from '.';

import { AssetService } from '@/services/AssetService';
import { Asset, AssetListParams } from '@/types/asset';
import { CreateAssetFormRequest } from '@/utils/form/schemas/create-asset-schema';
import { FieldError } from '@/types/dto';

export const usePatchAsset = (id: string) => {
  return usePatch<EditAssetFormRequest, Asset<'basic'>>(AssetService.patch, id, [
    `assets`,
    `${id}/edit-view`,
  ]);
};

export const useDeleteAsset = (id: string) => {
  return useDelete(AssetService.deleteAsset, id);
};

export const usePostAsset = () => {
  return usePost<string, CreateAssetFormRequest, Asset<'basic'>, FieldError[] | undefined>(
    '/assets',
    AssetService.createAsset
  );
};

export const useGetAssets = (params: AssetListParams) => {
  return useGetPage<Asset<'table'>, AssetListParams>('assets', AssetService.getAssets, params);
};

export const useGetAssetBasic = (id: string) => {
  const endpoint = `${id}/edit-view`;
  return useGet<Asset<'basic'>>(endpoint, AssetService.getBasic);
};

export const useGetAssetDetail = (id: string) => {
  const endpoint = `${id}`;
  return useGet<Asset<'detail'>>(endpoint, AssetService.getDetail);
};
