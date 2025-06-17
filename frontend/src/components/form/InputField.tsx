import { Control, FieldValues, Path } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '../ui/form';
import { Input } from '../ui/input';

type InputFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
  multiline?: boolean;
  rows?: number;
  maxLength?: number;
  minLength?: number;
  disabled?: boolean;
};

/**
 * InputField is a form component that renders a text input
 * integrated with `react-hook-form` for form state and validation management.
 *
 * It binds the input's value to the form using the provided `control` and `name`.
 *
 * @param {Path<T>} props.name - The name of the field in the form schema
 * @param {Control<T>} props.control - The control object from react-hook-form
 * @param {string} props.label - The label displayed above the input field
 */

export const InputField = <T extends FieldValues>({
  name,
  control,
  label,
  maxLength,
  minLength,
  disabled,
  multiline = false,
  rows,
}: InputFieldProps<T>) => {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            {multiline ? (
              <textarea
                {...field}
                rows={rows ?? 4}
                maxLength={maxLength}
                minLength={minLength}
                disabled={disabled}
                className='border-input bg-background placeholder:text-muted-foreground focus-visible:ring-ring w-full rounded-md border px-3 py-2 text-sm shadow-sm focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50'
              />
            ) : (
              <Input {...field} maxLength={maxLength} minLength={minLength} disabled={disabled} />
            )}
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
};
