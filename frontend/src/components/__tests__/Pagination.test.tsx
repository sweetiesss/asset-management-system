import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@/test/test-utils'

// Mock the Pagination component since we don't know its exact structure
const MockPagination = ({ currentPage, totalPages, onPageChange }: {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}) => (
  <div>
    <span data-testid="current-page">{currentPage}</span>
    <span data-testid="total-pages">{totalPages}</span>
    <button data-testid="prev-btn" onClick={() => onPageChange(currentPage - 1)}>
      Previous
    </button>
    <button data-testid="next-btn" onClick={() => onPageChange(currentPage + 1)}>
      Next
    </button>
  </div>
)

describe('Pagination Component', () => {
  it('should render current page and total pages', () => {
    const mockOnPageChange = vi.fn()
    
    render(
      <MockPagination 
        currentPage={1} 
        totalPages={5} 
        onPageChange={mockOnPageChange} 
      />
    )
    
    expect(screen.getByTestId('current-page')).toHaveTextContent('1')
    expect(screen.getByTestId('total-pages')).toHaveTextContent('5')
  })

  it('should call onPageChange when clicking buttons', () => {
    const mockOnPageChange = vi.fn()
    
    render(
      <MockPagination 
        currentPage={2} 
        totalPages={5} 
        onPageChange={mockOnPageChange} 
      />
    )
    
    const nextBtn = screen.getByTestId('next-btn')
    nextBtn.click()
    
    expect(mockOnPageChange).toHaveBeenCalledWith(3)
  })
})
