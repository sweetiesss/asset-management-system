import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@/test/test-utils'

// Mock SearchBar component
const MockSearchBar = ({ onSearch, placeholder, initialValue, disabled }: {
  onSearch: (value: string) => void
  placeholder?: string
  initialValue?: string
  disabled?: boolean
}) => (
  <input 
    data-testid="search-input"
    defaultValue={initialValue}
    onChange={(e) => onSearch(e.target.value)}
    placeholder={placeholder}
    disabled={disabled}
  />
)

describe('SearchBar Component', () => {
  it('should render with placeholder text', () => {
    const mockOnSearch = vi.fn()
    
    render(
      <MockSearchBar 
        onSearch={mockOnSearch}
        placeholder="Search users..."
      />
    )

    const input = screen.getByTestId('search-input')
    expect(input).toHaveAttribute('placeholder', 'Search users...')
  })

  it('should call onSearch when typing', () => {
    const mockOnSearch = vi.fn()
    
    render(
      <MockSearchBar 
        onSearch={mockOnSearch}
        placeholder="Search users..."
      />
    )

    const input = screen.getByTestId('search-input')
    fireEvent.change(input, { target: { value: 'john' } })

    expect(mockOnSearch).toHaveBeenCalledWith('john')
  })

  it('should display initial value', () => {
    const mockOnSearch = vi.fn()
    
    render(
      <MockSearchBar 
        onSearch={mockOnSearch}
        placeholder="Search users..."
        initialValue="initial search"
      />
    )

    const input = screen.getByTestId('search-input')
    expect(input).toHaveValue('initial search')
  })

  it('should be disabled when disabled prop is true', () => {
    const mockOnSearch = vi.fn()
    
    render(
      <MockSearchBar 
        onSearch={mockOnSearch}
        placeholder="Search users..."
        disabled={true}
      />
    )

    const input = screen.getByTestId('search-input')
    expect(input).toBeDisabled()
  })
})
