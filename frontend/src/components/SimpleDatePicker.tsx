import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useState } from 'react';
import dayjs from 'dayjs';

type SimpleDatePickerProps = {
  label: string;
  value: string; // format: YYYY-MM-DD
  onChange: (val: string) => void;
  min?: string; // format: YYYY-MM-DD
  max?: string;
};

export function SimpleDatePicker({ label, value, onChange, min, max }: SimpleDatePickerProps) {
  const [internalValue, setInternalValue] = useState(value ? dayjs(value) : null);

  return (
    <DatePicker
      label={label}
      value={internalValue}
      format='DD/MM/YYYY'
      onChange={(date) => {
        if (date === null) {
          onChange('');
          setInternalValue(null);
        } else if (date.isValid()) {
          const formatted = date.format('YYYY-MM-DD');
          onChange(formatted);
          setInternalValue(date);
        }
      }}
      slotProps={{
        textField: {
          fullWidth: true,
          size: 'small',
          sx: {
            height: '36px',
            fontSize: '0.875rem',
            '& .MuiInputBase-root': {
              borderRadius: '0.375rem',
              height: '36px',
              fontSize: '0.875rem',
              padding: '0 12px',
              borderColor: '#D1D5DB',
            },
            '& input': {
              padding: '8px 0',
            },
          },
          onKeyDown: (e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
            }
          },
        },
      }}
      minDate={min ? dayjs(min) : undefined}
      maxDate={max ? dayjs(max) : undefined}
    />
  );
}
