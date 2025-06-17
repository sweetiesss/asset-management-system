import { Control, FieldValues, Path } from 'react-hook-form';
import { useState } from 'react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Popover, PopoverTrigger, PopoverContent } from '@/components/ui/popover';
import {
  Command,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
} from '@/components/ui/command';
import { FormField, FormItem, FormLabel, FormControl, FormMessage } from '@/components/ui/form';
import { FilterIcon } from 'lucide-react';
import TruncateWithTooltip from '@/utils/truncate-with-toolip';

export interface OptionType {
  label: string;
  value: string;
}

interface SelectMultipleDropdownProps<TFormValues extends FieldValues> {
  name: Path<TFormValues>;
  control: Control<TFormValues>;
  label: string;
  options: OptionType[];
  width?: string;
  className?: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  rules?: any;
}

const SelectMultipleDropdown = <TFormValues extends FieldValues>({
  name,
  control,
  label,
  options,
  width = 'w-[200px]',
  className,
  rules,
}: SelectMultipleDropdownProps<TFormValues>) => {
  const [open, setOpen] = useState(false);

  const allActualOptions = options.filter((o) => o.value !== 'all');

  return (
    <FormField
      control={control}
      name={name}
      rules={rules}
      render={({ field, fieldState: { error } }) => {
        const selectedValues: string[] = Array.isArray(field.value) ? field.value : [];

        const isAllSelected =
          allActualOptions.length > 0 &&
          allActualOptions.every((option) => selectedValues.includes(option.value));

        const toggleSelect = (optionValue: string) => {
          if (optionValue === 'all') {
            if (isAllSelected) {
              field.onChange([]);
            } else {
              field.onChange(allActualOptions.map((o) => o.value));
            }
          } else {
            const newSelected = selectedValues.includes(optionValue)
              ? selectedValues.filter((v) => v !== optionValue)
              : [...selectedValues, optionValue];

            const updatedSelection =
              newSelected.length === allActualOptions.length &&
              allActualOptions.every((o) => newSelected.includes(o.value))
                ? allActualOptions.map((o) => o.value)
                : newSelected;

            field.onChange(updatedSelection);
          }
        };

        const getDisplayLabel = () => {
          if (isAllSelected) return `${label}: All`;
          if (selectedValues.length === 0) return label;
          if (selectedValues.length === 1) {
            const selectedOption = options.find((o) => o.value === selectedValues[0]);
            return `${label}: ${selectedOption ? selectedOption.label : selectedValues[0]}`;
          }
          if (selectedValues.length > 1) {
            return `${label}: ${selectedValues.length} Selected`;
          }
          return label;
        };

        return (
          <FormItem className={cn('w-full', className)}>
            <FormLabel className='text-black'>{label}</FormLabel>
            <Popover open={open} onOpenChange={setOpen}>
              <PopoverTrigger asChild>
                <FormControl>
                  <Button
                    variant='outline'
                    role='combobox'
                    aria-expanded={open}
                    className={cn(
                      'flex items-center justify-between',
                      width,
                      error && 'border-red-500'
                    )}
                  >
                    <span className='truncate overflow-hidden'>{getDisplayLabel()}</span>
                    <FilterIcon className='ml-2 h-4 w-4 shrink-0 opacity-50' />
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent className='p-0' style={{ width: width }} align='start'>
                <Command>
                  <CommandInput placeholder='Search options...' />
                  <CommandList>
                    <CommandEmpty>No results found.</CommandEmpty>
                    <CommandGroup>
                      <CommandItem
                        value='all'
                        onSelect={() => toggleSelect('all')}
                        className='cursor-pointer'
                      >
                        <input
                          type='checkbox'
                          checked={isAllSelected}
                          readOnly
                          className='mr-2 h-4 w-4 accent-red-600'
                        />
                        <span>All</span>
                      </CommandItem>
                      {allActualOptions.map((option) => {
                        const isSelected = selectedValues.includes(option.value);
                        return (
                          <CommandItem
                            key={option.value}
                            value={option.value}
                            onSelect={() => toggleSelect(option.value)}
                            className='cursor-pointer'
                          >
                            <input
                              type='checkbox'
                              checked={isSelected}
                              readOnly
                              className='mr-2 h-4 w-4 accent-red-600'
                            />
                            <TruncateWithTooltip
                              text={option.label}
                              showTooltip={option.label.length > 15}
                            >
                              <span className='truncate'>{option.label}</span>
                            </TruncateWithTooltip>
                          </CommandItem>
                        );
                      })}
                    </CommandGroup>
                  </CommandList>
                </Command>
              </PopoverContent>
            </Popover>
            <FormMessage />
          </FormItem>
        );
      }}
    />
  );
};

export default SelectMultipleDropdown;
