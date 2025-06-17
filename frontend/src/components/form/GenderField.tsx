import { Control, FieldValues, Path } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '../ui/form';
import { RadioGroup, RadioGroupItem } from '../ui/radio-group';
import { Gender } from '@/types/type';

type GenderFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
};

/**
 * GenderField is a form component for selecting a gender option
 * (Male or Female) using radio buttons within a `react-hook-form` context.
 *
 * It binds the selection to the form's state using the provided `control` and `name`.
 *
 * @param {Path<T>} props.name - The name of the field in the form schema
 * @param {Control<T>} props.control - The control object from react-hook-form
 * @param {string} props.label - The label displayed above the radio group (defaults to 'Gender' if not provided)
 */


export const GenderField = <T extends FieldValues>({
  name,
  control,
  label,
}: GenderFieldProps<T>) => {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className='space-y-3'>
          <FormLabel>{label || 'Gender'}</FormLabel>
          <FormControl>
            <RadioGroup
              onValueChange={field.onChange}
              className='flex gap-6'
              value={field.value}
            >
              <div className='flex items-center space-x-2'>
                <RadioGroupItem value={Gender.MALE.toString()} id='male' />
                <label htmlFor='male'>Male</label>
              </div>
              <div className='flex items-center space-x-2'>
                <RadioGroupItem value={Gender.FEMALE.toString()} id='female' />
                <label htmlFor='female'>Female</label>
              </div>
            </RadioGroup>
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
};
