'use client';

import useSWR from 'swr';
import { useState, useCallback } from 'react';
import { CategoryService } from '@/services/CategoryService';
import type { CreateCategoryFormRequest } from '@/utils/form/schemas/create-category.schema';
import { useApiError } from '@/hooks/useApiError';
import type { Category } from '@/types/asset';
import type { Response } from '@/types/dto';

const CATEGORIES_KEY = '/categories';

/**
 * Hook for managing categories data and operations
 * Provides methods for fetching, creating, updating, and deleting categories
 */
export const useCategories = () => {
  const [isCreating, setIsCreating] = useState(false);
  const { setApiError, clearErrors } = useApiError();

  // Fetch categories using SWR
  const {
    data,
    error,
    isLoading,
    mutate: revalidateCategories,
  } = useSWR(CATEGORIES_KEY, async () => {
    const response = await CategoryService.getCategories();
    if (response.success) {
      return response.data || [];
    }
    setApiError(response);
    throw new Error(response.error?.message || 'Failed to load categories');
  });

  /**
   * Create a new category
   * @param category Category data
   * @returns Promise with API response
   */
  const createCategory = useCallback(
    async (category: CreateCategoryFormRequest): Promise<Response<Category>> => {
      clearErrors();
      setIsCreating(true);
      try {
        const response = await CategoryService.createCategory(category);

        if (response.success) {
          await revalidateCategories();
        } else {
          setApiError(response);
        }

        return response;
      } catch (error) {
        console.error('Error creating category:', error);
        throw error;
      } finally {
        setIsCreating(false);
      }
    },
    [clearErrors, revalidateCategories, setApiError]
  );

  return {
    data: data || [],
    isLoading,
    error,
    isError: !!error,
    createCategory,
    isCreating,
    revalidateCategories,
  };
};
