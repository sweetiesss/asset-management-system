import type { Category } from '@/types/asset';
import { authApi } from '@/configs/axios';
import type { Response } from '@/types/dto';
import { handleApiError } from '@/utils/api';
import type { CreateCategoryFormRequest } from '@/utils/form/schemas/create-category.schema';
import { getEntity } from '.';

export const CategoryService = {
  /**
   * Get all categories
   * @returns Promise with categories array or error
   */
  getCategories: async (): Promise<Response<Category[]>> => {
    try {
      const response = await authApi.get<Response<Category[]>>('/categories');
      return response.data;
    } catch (error: unknown) {
      return handleApiError<Category[]>(error);
    }
  },

  /**
   * Create a new category
   * @param data Category data
   * @returns Promise with created category or error
   */
  createCategory: async (data: CreateCategoryFormRequest): Promise<Response<Category>> => {
    try {
      const response = await authApi.post<Response<Category>>('/categories', data);
      return response.data;
    } catch (error: unknown) {
      return handleApiError<Category>(error);
    }
  },

  getCategoryList: async () => {
    return await getEntity<Category[]>('/categories');
  },
};
