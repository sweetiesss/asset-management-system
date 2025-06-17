import * as z from 'zod';
import { categoryNameValidator, categoryPrefixValidator } from '@/utils/validations/validators';

export type CreateCategoryFormRequest = {
  name: string;
  prefix: string;
};

export const defaultValues: CreateCategoryFormRequest = {
  name: '',
  prefix: '',
};

export const createCategorySchema = z.object({
  name: categoryNameValidator,
  prefix: categoryPrefixValidator,
});
