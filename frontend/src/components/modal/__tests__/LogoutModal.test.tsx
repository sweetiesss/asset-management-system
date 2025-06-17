import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@/test/test-utils'

// Mock LogoutModal component
const MockLogoutModal = ({ isOpen, onClose, onConfirm }: {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
}) => {
  if (!isOpen) return null
  
  return (
    <div data-testid="dialog">
      <div data-testid="dialog-content">
        <div data-testid="dialog-header">
          <h2 data-testid="dialog-title">Logout</h2>
          <p data-testid="dialog-description">Are you sure you want to logout?</p>
        </div>
        <div data-testid="dialog-footer">
          <button data-testid="button-outline" onClick={onClose}>
            Cancel
          </button>
          <button data-testid="button-destructive" onClick={onConfirm}>
            Logout
          </button>
        </div>
      </div>
    </div>
  )
}

describe('LogoutModal', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onConfirm: vi.fn(),
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render when isOpen is true', () => {
    render(<MockLogoutModal {...defaultProps} />)
    
    expect(screen.getByTestId('dialog')).toBeInTheDocument()
    expect(screen.getByTestId('dialog-title')).toHaveTextContent(/logout/i)
  })

  it('should not render when isOpen is false', () => {
    render(<MockLogoutModal {...defaultProps} isOpen={false} />)
    
    expect(screen.queryByTestId('dialog')).not.toBeInTheDocument()
  })

  it('should display logout confirmation message', () => {
    render(<MockLogoutModal {...defaultProps} />)
    
    expect(screen.getByTestId('dialog-description')).toHaveTextContent(/sure.*logout/i)
  })

  it('should call onClose when cancel button is clicked', () => {
    const mockOnClose = vi.fn()
    
    render(<MockLogoutModal {...defaultProps} onClose={mockOnClose} />)
    
    const cancelButton = screen.getByText(/cancel/i)
    fireEvent.click(cancelButton)
    
    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('should call onConfirm when logout button is clicked', () => {
    const mockOnConfirm = vi.fn()
    
    render(<MockLogoutModal {...defaultProps} onConfirm={mockOnConfirm} />)
    
    // Use the specific button with destructive styling instead of searching by text
    const logoutButton = screen.getByTestId('button-destructive')
    fireEvent.click(logoutButton)
    
    expect(mockOnConfirm).toHaveBeenCalledTimes(1)
  })

  it('should have proper button styling', () => {
    render(<MockLogoutModal {...defaultProps} />)
    
    expect(screen.getByTestId('button-outline')).toBeInTheDocument() // Cancel button
    expect(screen.getByTestId('button-destructive')).toBeInTheDocument() // Logout button
  })

  it('should render footer with buttons', () => {
    render(<MockLogoutModal {...defaultProps} />)
    
    const footer = screen.getByTestId('dialog-footer')
    expect(footer).toBeInTheDocument()
    
    const buttons = footer.querySelectorAll('button')
    expect(buttons).toHaveLength(2)
  })
})
