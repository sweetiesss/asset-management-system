import { Input } from '@/components/ui/input';
import { Search } from 'lucide-react';
import { useEffect, useState } from 'react';

interface SearchBarProps extends React.HTMLAttributes<HTMLDivElement> {
  defaultValue?: string;
  onSearch?: (keyword: string) => void;
  placeholder?: string;
  readOnly?: boolean;
}

const SearchBar: React.FC<SearchBarProps> = ({ defaultValue = '', readOnly, onSearch, placeholder, ...props }) => {
  const [inputValue, setInputValue] = useState(defaultValue);

  const handleSearch = () => {
    onSearch?.(inputValue.trim());
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  useEffect(() => {
    setInputValue(defaultValue);
  }, [defaultValue]);

  return (
    <div className='flex items-center justify-between w-full' {...props}>
      <div className='relative w-full'>
        <Input
          type='text'
          placeholder={placeholder}
          className='w-full pl-4 pr-8 border-gray-300'
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          readOnly={readOnly || false}
        />

        <div
          onClick={handleSearch}
          className='absolute p-1 text-gray-500 -translate-y-1/2 rounded-full cursor-pointer top-1/2 right-2 hover:bg-gray-100 hover:text-gray-800'
        >
          <Search size={16} />
        </div>
      </div>
    </div>
  );
};

export default SearchBar;
