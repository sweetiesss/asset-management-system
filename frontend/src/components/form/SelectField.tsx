import { Control, FieldValues, Path } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '../ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';

export type SelectItem = {
  value: string;
  label: string;
};

type SelectFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
  items: SelectItem[];
  defaultValue?: string;
  placeholder?: string;
  disabled?: boolean;
};

/**
 * SelectField is a generic form component that renders a dropdown
 * select menu integrated with `react-hook-form` for form state and validation.
 *
 * It displays a list of selectable items provided via the `items` prop and
 * synchronizes the selected value with the form's state.
 *
 * @param {Path<T>} props.name - The name of the field in the form schema
 * @param {Control<T>} props.control - The control object from react-hook-form
 * @param {string} props.label - The label displayed above the select dropdown
 * @param {SelectItem[]} props.items - An array of items to populate the select menu
 * @param {string} [props.defaultValue] - Optional default selected value for the dropdown
 */

export const SelectField = <T extends FieldValues>({
  name,
  control,
  label,
  items,
  defaultValue,
  placeholder,
  disabled
}: SelectFieldProps<T>) => {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className='w-full'>
          <FormLabel className='text-black'>{label}</FormLabel>
          <Select onValueChange={field.onChange} defaultValue={defaultValue} disabled={disabled} value={field.value}>
            <FormControl>
              <SelectTrigger className='w-full'>
                <SelectValue placeholder={placeholder} />
              </SelectTrigger>
            </FormControl>
            
            <SelectContent>
              {items.map((item) => (
                <SelectItem key={item.value} value={item.value}>
                  {item.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );
};
