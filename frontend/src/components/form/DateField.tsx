import { Control, Controller, FieldValues, Path } from 'react-hook-form';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { FormLabel, FormMessage, FormItem } from '../ui/form';
import dayjs, { Dayjs } from 'dayjs';

type DateFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label: string;
  min?: string; // YYYY-MM-DD
  max?: string;
  required?: boolean;
};

export const DateField = <T extends FieldValues>({
  name,
  control,
  label,
  min,
  max,
  required = false,
}: DateFieldProps<T>) => {
  return (
    <FormItem className='flex flex-col'>
      <FormLabel>
        {label}
        {required ? <span className='text-red-500'>*</span> : null}
      </FormLabel>

      <Controller
        name={name}
        control={control}
        rules={{
          required: required ? 'Please enter a valid date' : false,
          validate: {
            validDate: (value) =>
              !value || dayjs(value, 'YYYY-MM-DD', true).isValid() || 'Invalid date format',
            withinRange: (value) => {
              if (!value) return true;
              const date = dayjs(value, 'YYYY-MM-DD', true);
              if (!date.isValid()) return 'Invalid date';
              if (min && date.isBefore(dayjs(min, 'YYYY-MM-DD'))) {
                return `Date must be on or after ${dayjs(min).format('DD/MM/YYYY')}`;
              }
              if (max && date.isAfter(dayjs(max, 'YYYY-MM-DD'))) {
                return `Date must be on or before ${dayjs(max).format('DD/MM/YYYY')}`;
              }
              return true;
            },
          },
        }}
        render={({ field, fieldState }) => (
          <DatePicker
            value={field.value ? dayjs(field.value, 'YYYY-MM-DD', true) : null}
            onChange={(date: Dayjs | null) => {
              if (date && date.isValid()) {
                field.onChange(date.format('YYYY-MM-DD'));
              } else {
                field.onChange('');
              }
            }}
            format='DD/MM/YYYY'
            reduceAnimations
            minDate={min ? dayjs(min, 'YYYY-MM-DD') : undefined}
            maxDate={max ? dayjs(max, 'YYYY-MM-DD') : undefined}
            slotProps={{
              textField: {
                fullWidth: true,
                error: !!fieldState.error,
                helperText: fieldState.error?.message,
                onKeyDown: (e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    const value = field.value;
                    if (value && !dayjs(value, ['DD/MM/YYYY', 'YYYY-MM-DD'], true).isValid()) {
                      field.onChange(''); // Clear invalid input on Enter
                    }
                    field.onBlur(); // Trigger validation
                  }
                },
                onBlur: () => {
                  const value = field.value;
                  if (value && !dayjs(value, ['DD/MM/YYYY', 'YYYY-MM-DD'], true).isValid()) {
                    field.onChange(''); // Clear invalid input on blur
                  }
                  field.onBlur(); // Trigger validation
                },
              },
            }}
          />
        )}
      />
      <FormMessage />
    </FormItem>
  );
};
