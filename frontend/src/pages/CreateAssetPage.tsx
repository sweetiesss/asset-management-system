import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import { InputField } from '@/components/form/InputField';
import { DateField } from '@/components/form/DateField';
import { RadioGroupField, type RadioOption } from '@/components/form/RadioGroupField';
import { CreatableSelectField } from '@/components/form/CreatableSelectField';
import { CategoryForm } from '@/components/form/CategoryForm';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { toast } from 'react-toastify';
import { useCategories } from '@/hooks/fetch/useCategories';
import {
  type CreateAssetFormRequest,
  createAssetSchema,
  defaultValues as assetDefaultValues,
} from '@/utils/form/schemas/create-asset-schema';
import type { CreateCategoryFormRequest } from '@/utils/form/schemas/create-category.schema';
import { AssetStateHandler } from '@/types/asset';
import { RANGE } from '@/configs/constants';
import { DataDependencyWrapper } from '@/components/layouts/DataDependencyWrapper';
import { useApiError } from '@/hooks/useApiError';
import { ErrorCode, getErrorMessage } from '@/configs/error-codes';
import { usePostAsset } from '@/hooks/fetch/assets';
import Spinner from '@/components/Spinner';


export function CreateAssetPage() {
  const navigate = useNavigate();

  const { error: apiError, clearErrors } = useApiError();
  const { triggerPost, isPosting, postError } = usePostAsset();
  // Category-specific error handling
  const [categoryApiError, setCategoryApiError] = useState<{
    code: string;
    message: string;
  } | null>(null);

  const {
    data: categories = [],
    isLoading: isCategoriesLoading,
    isError: isCategoriesError,
    error: categoriesError,
    createCategory,
    isCreating: isCreatingCategory,
  } = useCategories();



  const form = useForm<CreateAssetFormRequest>({
    resolver: zodResolver(createAssetSchema),
    defaultValues: assetDefaultValues,
    mode: 'onChange',
  });

  const isFormValid = form.formState.isValid;

  const assetStateRadioItems: RadioOption[] = useMemo(() => {
    return AssetStateHandler.getOptions('create');
  }, []);

  /**
   * Handle category creation
   * @param data Category form data
   * @returns Created category or null if error
   */
  const handleCreateCategory = async (data: CreateCategoryFormRequest) => {
    setCategoryApiError(null);
    try {
      const response = await createCategory(data);

      if (response.success && response.data) {
        toast(response.message || 'Category created successfully', {
          type: 'success',
        });

        return {
          value: response.data.id,
          label: response.data.name,
        };
      }

      if (!response.success && response.error) {
        setCategoryApiError({
          code: response.error.code || ErrorCode.UNKNOWN_ERROR,
          message: response.error.message || getErrorMessage(response.error.code || ''),
        });

        toast(response.error.message || getErrorMessage(response.error.code || ''), {
          type: 'error',
        });
      }

      return null;
    } catch (error) {
      console.error('Error creating category:', error);
      return null;
    }
  };

  const onSubmit = async (data: CreateAssetFormRequest) => {
    setCategoryApiError(null);
    clearErrors();
    const newAsset = await triggerPost(data);
    toast(`Asset ${newAsset.name} created successfully`, {
      type: 'success',
    });
    navigate('/assets', { replace: true, state: { updatedAsset: newAsset } });

  };

  useEffect(() => {
    const errors = postError?.details;
    if (errors) {
      errors.forEach((error) => {
        form.setError(error.field as keyof CreateAssetFormRequest, {
          type: 'manual',
          message: error.message,
        });
      });
    }
  }, [postError]);

  return (
    <DataDependencyWrapper
      isLoading={isCategoriesLoading}
      isError={isCategoriesError}
      errorMessage={categoriesError?.message || 'Failed to load categories. Please try again.'}
      fallbackPath='/assets'
      loadingMessage='Loading categories...'
    >
      <div className='mx-auto max-w-md rounded-lg bg-white p-6 shadow-sm'>
        <h1 className='text-primary mb-5 text-2xl font-bold'>Create New Asset</h1>
        <span className='block w-full text-right text-sm text-red-500'>
          *All fields are required
        </span>

        {apiError && (
          <div className='mb-4 rounded-md bg-red-50 p-3 text-sm text-red-600'>{apiError}</div>
        )}

        <Form {...form}>
          <form
            className='space-y-4'
            onKeyDown={(e) => {
              if (e.key === 'Enter' && e.target !== e.currentTarget) {
                // Only prevent if Enter is pressed on a form field, not the form itself
                e.preventDefault();
              }
            }}
          >
            <InputField
              name='name'
              label='Name'
              control={form.control}
              maxLength={RANGE.ASSET_NAME.MAX}
              minLength={RANGE.ASSET_NAME.MIN}
            />

            <CreatableSelectField
              name='categoryId'
              label='Category'
              control={form.control}
              items={categories.map((cat) => ({
                value: cat.id,
                label: cat.name,
              }))}
              onCreate={async (data) => {
                const result = await handleCreateCategory({
                  name: data.label,
                  prefix: data.value,
                });
                return result;
              }}
              renderCreateForm={({ onConfirm, onCancel }) => (
                <CategoryForm
                  onSubmit={(data) => {
                    onConfirm({
                      label: data.name,
                      value: data.prefix,
                    });
                  }}
                  onCancel={() => {
                    setCategoryApiError(null);
                    onCancel();
                  }}
                  apiError={categoryApiError}
                />
              )}
            />

            <InputField
              name='specification'
              label='Specification'
              control={form.control}
              maxLength={RANGE.ASSET_SPECIFICATION.MAX}
              multiline
            />

            <DateField
              name='installedDate'
              label='Installed Date'
              control={form.control}
              max={new Date().toISOString().split('T')[0]}
            />

            <RadioGroupField
              name='state'
              label='State'
              control={form.control}
              options={assetStateRadioItems}
            />

            <div className='flex justify-end gap-4 pt-4'>
              <Button
                type='button'
                onClick={form.handleSubmit(onSubmit)}
                disabled={!isFormValid || isPosting || isCreatingCategory}
                className='bg-primary hover:bg-primary-600 cursor-pointer text-white'
              >
                {isPosting ? (
                  <div className='flex gap-2'>
                    <Spinner/>
                    Saving...
                  </div>
                ) : (
                  'Save'
                )}
              </Button>
              <Button type='button' variant='outline' onClick={() => navigate(-1)}>
                Cancel
              </Button>
            </div>
          </form>
        </Form>
      </div>
    </DataDependencyWrapper>
  );
}
