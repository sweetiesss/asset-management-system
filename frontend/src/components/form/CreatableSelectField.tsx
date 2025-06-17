import type React from 'react';

import type { ReactNode } from 'react';
import { useState, useEffect } from 'react';
import { type Control, type FieldValues, type Path, useController } from 'react-hook-form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  SelectSeparator,
} from '../ui/select';
import { Button } from '../ui/button';
import { FormControl, FormItem, FormLabel, FormMessage } from '../ui/form';
import TruncateWithTooltip from '@/utils/truncate-with-toolip';

export type CreatableSelectItem = { value: string; label: string };

type RenderCreateFormProps = {
  onConfirm: (newItem: { value: string; label: string }) => void;
  onCancel: () => void;
};

type CreatableSelectFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
  items: CreatableSelectItem[];
  onCreate: (data: { value: string; label: string }) => Promise<CreatableSelectItem | null>;
  renderCreateForm: (props: RenderCreateFormProps) => ReactNode;
  maxVisibleItems?: number;
};

export function CreatableSelectField<T extends FieldValues>({
  name,
  control,
  label,
  items,
  onCreate,
  renderCreateForm,
  maxVisibleItems = 8,
}: CreatableSelectFieldProps<T>) {
  const {
    field,
    fieldState: { error },
  } = useController({ name, control });
  const [options, setOptions] = useState<CreatableSelectItem[]>(items);
  const [isCreating, setIsCreating] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const handleConfirm = async (data: { value: string; label: string }) => {
    try {
      const newItem = await onCreate(data);

      if (newItem) {
        setOptions((prev) => [...prev, newItem]);
        field.onChange(newItem.value);
        setIsCreating(false);
      }
    } catch (error) {
      console.error('Error creating new item:', error);
    }
  };

  const handleCancel = () => {
    setIsCreating(false);
  };

  useEffect(() => {
    if (field.value && options.length > 0) {
      const selectedOption = options.find((opt) => opt.value === field.value);
      if (selectedOption) {
        field.onChange(selectedOption.value);
      }
    }
  }, [options, field.value, field]);

  // Prevent keyboard events from bubbling up to the Select component
  const handleKeyDown = (e: React.KeyboardEvent) => {
    e.stopPropagation();
  };

  // Calculate max height (approximately 40px per item + padding)
  const maxHeight = maxVisibleItems * 40 + 60; // Extra padding for separator and create button

  return (
    <FormItem className='w-full'>
      <FormLabel className='text-black'>{label}</FormLabel>
      <Select
        value={field.value}
        onValueChange={(value) => {
          field.onChange(value);
        }}
        open={isOpen}
        onOpenChange={setIsOpen}
      >
        <FormControl>
          <SelectTrigger className='w-full'>
            <SelectValue placeholder='Select a category' />
          </SelectTrigger>
        </FormControl>

        <SelectContent className='bg-gray-50' style={{ maxHeight: `${maxHeight}px` }}>
          <div className='max-h-full overflow-y-auto'>
            {options.map((item) => (
              <SelectItem key={item.value} value={item.value}>
                <TruncateWithTooltip text={item.label}>
                  <span className='inline-block max-w-[320px] truncate'>{item.label}</span>
                </TruncateWithTooltip>
              </SelectItem>
            ))}

            <SelectSeparator />

            <div className='p-2' onKeyDown={handleKeyDown}>
              {isCreating ? (
                <div onClick={(e) => e.stopPropagation()}>
                  {renderCreateForm({
                    onConfirm: handleConfirm,
                    onCancel: handleCancel,
                  })}
                </div>
              ) : (
                <Button
                  variant='link'
                  className='h-5 p-0 italic'
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    setIsCreating(true);
                  }}
                >
                  Add new category
                </Button>
              )}
            </div>
          </div>
        </SelectContent>
      </Select>

      <FormMessage>{error?.message}</FormMessage>
    </FormItem>
  );
}
