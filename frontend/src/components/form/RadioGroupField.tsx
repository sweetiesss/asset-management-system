import { Control, FieldValues, Path } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '../ui/form';
import { RadioGroup, RadioGroupItem } from '../ui/radio-group'; // or your re-exported UI wrapper

export type RadioOption = {
  value: string;
  label: string;
};

type RadioGroupFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
  options: RadioOption[];
  className?: string;
};

/**
 * RadioGroupField renders a radio button group integrated with react-hook-form.
 * It maps `options` into individual radio inputs and binds the selected value to form state.
 *
 * @param {Path<T>} props.name - The name of the field in the form schema
 * @param {Control<T>} props.control - The react-hook-form control object
 * @param {string} props.label - The group label
 * @param {RadioOption[]} props.options - Array of options with value/label
 */

export const RadioGroupField = <T extends FieldValues>({
  name,
  control,
  label,
  options,
  className,
}: RadioGroupFieldProps<T>) => {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel className='mb-2 block text-sm font-medium'>{label}</FormLabel>
          <FormControl>
            <RadioGroup
              onValueChange={field.onChange}
              value={field.value}
              className='flex flex-col space-y-2'
            >
              {options.map((option) => (
                <div key={option.value} className='flex items-center space-x-2'>
                  <RadioGroupItem
                    value={option.value}
                    id={`${name}-${option.value}`}
                    className='peer border-muted-foreground text-primary focus:ring-ring h-4 w-4 rounded-full border focus:ring-2 focus:outline-none'
                  />
                  <label
                    htmlFor={`${name}-${option.value}`}
                    className='text-sm leading-none font-normal peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
                  >
                    {option.label}
                  </label>
                </div>
              ))}
            </RadioGroup>
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
};
