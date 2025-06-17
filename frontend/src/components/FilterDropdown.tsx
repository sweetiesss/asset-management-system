import TruncateWithTooltip from '@/utils/truncate-with-toolip';
import { Filter as FilterIcon } from 'lucide-react';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { useState } from 'react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
interface FilterDropdownProps {
  label: string;
  options: string[];
  selected: string[];
  onChange: (value: string[]) => void;
  width?: string;
}

interface FilterDropdownProps {
  label: string;
  options: string[];
  selected: string[];
  onChange: (selected: string[]) => void;
  width?: string;
  className?: string;
}

const FilterDropdown: React.FC<FilterDropdownProps> = ({
  label,
  options,
  selected,
  onChange,
  width = 'w-[250px]', // Increased default width to better accommodate labels
  className,
}) => {
  const [open, setOpen] = useState(false);

  // Exclude 'All' for selection logic
  const allOptions = options.filter((o) => o !== 'All');
  const isAllSelected = allOptions.length > 0 && allOptions.every((option) => selected.includes(option));

  /**
   * Toggles the selection of an option or 'All' options.
   */
  const toggleSelect = (value: string) => {
    if (value === 'All') {
      onChange(isAllSelected ? [] : allOptions);
    } else {
      const newSelected = selected.includes(value)
        ? selected.filter((v) => v !== value)
        : [...selected, value];
      onChange(newSelected);
    }
  };


  const getDisplayLabel = () => {
    const maxVisible = 2; 

    if (isAllSelected) return 'All';
    if (selected.length === 0) return label;

    const visibleItems = selected.slice(0, maxVisible);
    const remainingCount = selected.length - maxVisible;

    const text =
      remainingCount > 0
        ? `${visibleItems.join(', ')}, +${remainingCount}`
        : visibleItems.join(', ');
    
    return text;
  };

  const displayLabel = getDisplayLabel();

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant='outline'
          role='combobox'
          aria-expanded={open}
          className={cn('justify-between', className)}
          style={{ width: width }}
        >
          {/* TruncateWithTooltip handles long text in the button label */}
          <TruncateWithTooltip text={displayLabel}>
            <span className='truncate'>{displayLabel}</span>
          </TruncateWithTooltip>
          <FilterIcon className='ml-2 h-4 w-4 shrink-0 opacity-50' />
        </Button>
      </PopoverTrigger>

      <PopoverContent className='p-0' style={{ width: width }} align='start'>
        <Command>
          <CommandInput placeholder='Search...' />
          <CommandList>
            <CommandEmpty>No results found.</CommandEmpty>
            <CommandGroup>
              {/* 'All' option */}
              <CommandItem
                value='All'
                onSelect={() => toggleSelect('All')}
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

              {/* Other options */}
              {allOptions.map((option) => {
                const isSelected = selected.includes(option);
                return (
                  <CommandItem
                    key={option}
                    value={option}
                    onSelect={() => toggleSelect(option)}
                    className='cursor-pointer'
                  >
                    <input
                      type='checkbox'
                      checked={isSelected}
                      readOnly
                      className='mr-2 h-4 w-4 accent-red-600'
                    />
                    {/* TruncateWithTooltip for each dropdown item */}
                    <TruncateWithTooltip text={option} showTooltip={option.length > 30}>
                       <span className='truncate'>{option}</span>
                    </TruncateWithTooltip>
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};

export default FilterDropdown;