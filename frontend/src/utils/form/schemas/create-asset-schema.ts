import { Asset, AssetState } from '@/types/asset';
import { assetNameValidator, installedDateValidator } from '@/utils/validations/validators';
import * as z from 'zod';

// export type CreateAssetFormRequest = Pick<
//   Asset,
//   'name' | 'categoryId' | 'specification' | 'installedDate' | 'state'
// >;

export type CreateAssetFormRequest = Pick<
  Asset<'full'>,
  'name' | 'categoryId' | 'specification' | 'installedDate' | 'state'
>;

export const defaultValues: CreateAssetFormRequest = {
  name: '',
  categoryId: 0,
  specification: '',
  installedDate: '',
  state: AssetState.AVAILABLE,
};

export const createAssetSchema = z.object({
  name: assetNameValidator,
  categoryId: z.number().min(1, { message: 'Category is required' }),
  specification: z.string().min(1, { message: 'Specification is required' }),
  installedDate: installedDateValidator,
  state: z.nativeEnum(AssetState),
});
