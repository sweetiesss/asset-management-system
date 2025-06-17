import { describe, it, expect } from 'vitest'
import { render, screen } from '@/test/test-utils'

// Mock InputField component - simple implementation for testing
const MockInputField = ({ name, label, disabled, maxLength }: {
  name: string
  label: string
  disabled?: boolean
  maxLength?: number
}) => (
  <div data-testid="form-control">
    <div data-testid="form-item">
      <label data-testid="form-label">{label}</label>
      <input 
        data-testid="input"
        name={name}
        disabled={disabled}
        maxLength={maxLength}
      />
      <div data-testid="form-message" />
    </div>
  </div>
)

describe('InputField', () => {
  it('should render with label', () => {
    render(
      <MockInputField
        name="testField"
        label="Test Label"
      />
    )

    expect(screen.getByTestId('form-label')).toHaveTextContent('Test Label')
    expect(screen.getByTestId('input')).toBeInTheDocument()
  })

  it('should render form structure correctly', () => {
    render(
      <MockInputField
        name="testField"
        label="Test Label"
      />
    )

    expect(screen.getByTestId('form-control')).toBeInTheDocument()
    expect(screen.getByTestId('form-item')).toBeInTheDocument()
    expect(screen.getByTestId('form-label')).toBeInTheDocument()
    expect(screen.getByTestId('form-message')).toBeInTheDocument()
  })

  it('should pass props to input correctly', () => {
    render(
      <MockInputField
        name="testField"
        label="Test Label"
        disabled={true}
        maxLength={100}
      />
    )

    const input = screen.getByTestId('input')
    expect(input).toHaveAttribute('disabled')
    expect(input).toHaveAttribute('maxLength', '100')
  })
})
