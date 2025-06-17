'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';

import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Check, X } from 'lucide-react';

import {
  type CreateCategoryFormRequest,
  createCategorySchema,
  defaultValues as categoryDefaultValues,
} from '@/utils/form/schemas/create-category.schema';
import { RANGE } from '@/configs/constants';
import { ErrorCode } from '@/configs/error-codes';

type Props = {
  onSubmit: (data: CreateCategoryFormRequest) => void;
  onCancel: () => void;
  initialData?: Partial<CreateCategoryFormRequest>;
  apiError?: { code: string; message: string } | null;
};

/**
 * Form component for creating or updating a category
 * Handles validation and displays API errors
 */
export function CategoryForm({ onSubmit, onCancel, initialData, apiError }: Props) {
  const form = useForm<CreateCategoryFormRequest>({
    resolver: zodResolver(createCategorySchema),
    defaultValues: initialData
      ? { ...categoryDefaultValues, ...initialData }
      : categoryDefaultValues,
    mode: 'onChange',
  });

  const { formState } = form;
  const hasErrors = Object.keys(formState.errors).length > 0;

  const handleSubmit = form.handleSubmit((data) => {
    onSubmit(data);
  });

  // Display API errors if they exist
  useEffect(() => {
    if (apiError) {
      switch (apiError.code) {
        case ErrorCode.CATEGORY_NAME_ALREADY_EXISTS:
          form.setError('name', {
            type: 'manual',
            message: apiError.message || 'Category name already exists',
          });
          break;
        case ErrorCode.CATEGORY_PREFIX_ALREADY_EXISTS:
          form.setError('prefix', {
            type: 'manual',
            message: apiError.message || 'Category prefix already exists',
          });
          break;
        case ErrorCode.VALIDATION_ERROR:
          // Handle general validation errors
          if (apiError.message) {
            // If the message contains specific field information, try to set the appropriate error
            if (apiError.message.toLowerCase().includes('name')) {
              form.setError('name', {
                type: 'manual',
                message: apiError.message,
              });
            } else if (apiError.message.toLowerCase().includes('prefix')) {
              form.setError('prefix', {
                type: 'manual',
                message: apiError.message,
              });
            }
          }
          break;
      }
    }
  }, [apiError, form]);

  return (
    <div className='w-full' style={{ height: '40px' }}>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          e.stopPropagation();
          handleSubmit(e);
        }}
        className='flex h-full items-center gap-2'
      >
        <div className='relative h-full'>
          <Input
            {...form.register('name')}
            placeholder='Category name'
            maxLength={RANGE.CATEGORY_NAME.MAX}
            className={`h-full w-[180px] ${
              formState.errors.name ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''
            }`}
            autoFocus
          />
          {formState.errors.name && (
            <div className='absolute mt-1 text-xs text-red-500'>
              {formState.errors.name.message}
            </div>
          )}
        </div>

        <div className='relative h-full'>
          <Input
            {...form.register('prefix', {
              setValueAs: (v: string) => v.toUpperCase(),
            })}
            placeholder='XX'
            maxLength={RANGE.CATEGORY_PREFIX.LENGTH}
            className={`h-full w-[60px] uppercase ${
              formState.errors.prefix
                ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
                : ''
            }`}
          />
          {formState.errors.prefix && (
            <div className='absolute mt-1 text-xs text-red-500'>
              {formState.errors.prefix.message}
            </div>
          )}
        </div>

        <Button
          type='submit'
          size='icon'
          variant='ghost'
          className={`h-full ${hasErrors ? 'text-gray-400' : 'text-green-600'}`}
          disabled={hasErrors || formState.isSubmitting}
        >
          <Check className='h-5 w-5' />
        </Button>

        <Button type='button' size='icon' variant='ghost' className='h-full' onClick={onCancel}>
          <X className='h-5 w-5' />
        </Button>
      </form>
    </div>
  );
}
