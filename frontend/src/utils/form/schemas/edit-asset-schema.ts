import { Asset, AssetState } from '@/types/asset';
import { assetNameValidator } from '@/utils/validations/validators';
import * as z from 'zod';

export type EditAssetFormRequest = Pick<
  Asset<'full'>,
  'name' | 'categoryName' | 'specification' | 'installedDate' | 'state' | 'version'
>;

export const editAssetSchema = z
  .object({
    name: assetNameValidator,
    categoryName: z.string(),
    specification: z.string().min(1, { message: 'Specification is required' }),
    installedDate: z.string().min(1, { message: 'Installed date is required' }),
    state: z.nativeEnum(AssetState),
    version: z.number().int().min(0),
  })
  .refine((data) => {
    return data.installedDate && new Date(data.installedDate) <= new Date();
  });
