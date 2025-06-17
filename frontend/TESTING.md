# Frontend Testing Guide

This project uses **Vitest** with **React Testing Library** for comprehensive unit and integration testing.

## Testing Stack

- **Vitest**: Fast testing framework built on top of Vite
- **React Testing Library**: Testing utilities for React components
- **Jest DOM**: Custom matchers for DOM testing
- **User Event**: Simulates user interactions
- **Coverage**: V8 coverage provider for detailed test coverage reports

## Available Test Scripts

```bash
# Run tests in watch mode
npm run test

# Run tests once
npm run test:run

# Run tests with UI interface
npm run test:ui

# Run tests with coverage
npm run test:coverage
```

## Test Structure

```
src/
├── test/
│   ├── setup.ts           # Global test setup
│   └── test-utils.tsx     # Custom render functions and mocks
├── components/
│   └── __tests__/         # Component tests
├── hooks/
│   └── __tests__/         # Hook tests
├── services/
│   └── __tests__/         # Service/API tests
├── utils/
│   └── __tests__/         # Utility function tests
└── context/
    └── __tests__/         # Context tests
```

## Test Coverage Areas

### 🧩 **Components**
- **InputField**: Form input component with validation
- **SearchBar**: Search functionality with debouncing
- **Pagination**: Page navigation component
- **Modals**: Confirmation and action modals

### 🔧 **Hooks**
- **useModal**: Modal state management
- **useToggle**: Boolean state toggling
- **Custom hooks**: Form handling, API calls

### 🌐 **Services**
- **UserService**: User CRUD operations
- **AssetService**: Asset management
- **API error handling**: Centralized error management

### 🛠️ **Utilities**
- **dateUtils**: Date manipulation and validation
- **api**: Error handling and response processing
- **Form validation**: Schema validation utilities

### 🏗️ **Context & State**
- **UserContext**: Authentication and user state
- **Provider testing**: Context provider functionality

### 📱 **Integration**
- **App routing**: Route protection and navigation
- **Layout components**: Main and auth layouts
- **Protected routes**: Admin access control

## Testing Best Practices

### 1. **Component Testing**
```tsx
// Test user interactions, not implementation details
test('should call onSubmit when form is submitted', () => {
  const mockSubmit = vi.fn()
  render(<Form onSubmit={mockSubmit} />)
  
  fireEvent.click(screen.getByRole('button', { name: /submit/i }))
  expect(mockSubmit).toHaveBeenCalled()
})
```

### 2. **Hook Testing**
```tsx
// Test hook behavior and state changes
test('should toggle value when toggle is called', () => {
  const { result } = renderHook(() => useToggle())
  
  act(() => {
    result.current.toggle()
  })
  
  expect(result.current.value).toBe(true)
})
```

### 3. **Service Testing**
```tsx
// Mock HTTP calls and test error handling
test('should handle API errors gracefully', async () => {
  mockApi.get.mockRejectedValueOnce(new Error('Network error'))
  
  const result = await UserService.getUsers({})
  
  expect(result.success).toBe(false)
  expect(result.error).toBeDefined()
})
```

### 4. **Integration Testing**
```tsx
// Test component interactions with context
test('should render admin routes for admin users', () => {
  const mockUser = { roles: ['ADMIN'] }
  
  render(
    <UserProvider value={{ auth: mockUser }}>
      <App />
    </UserProvider>
  )
  
  expect(screen.getByTestId('admin-routes')).toBeInTheDocument()
})
```

## Coverage Thresholds

The project maintains **70% coverage** across:
- **Branches**: 70%
- **Functions**: 70%
- **Lines**: 70%
- **Statements**: 70%

## Mocking Strategy

### Global Mocks (setup.ts)
- React Router navigation
- Toast notifications
- SWR data fetching
- Window APIs

### Component-Specific Mocks
- UI library components
- External dependencies
- API calls

## Writing New Tests

### 1. **Create test file**
```bash
# Component test
src/components/__tests__/NewComponent.test.tsx

# Hook test
src/hooks/__tests__/useNewHook.test.ts

# Service test
src/services/__tests__/NewService.test.ts
```

### 2. **Use test utilities**
```tsx
import { render, screen } from '@/test/test-utils'
import { mockUser, mockAsset } from '@/test/test-utils'
```

### 3. **Follow naming conventions**
```tsx
describe('ComponentName', () => {
  it('should render without errors', () => {
    // Test implementation
  })
  
  it('should handle user interactions', () => {
    // Test implementation
  })
})
```

## Debugging Tests

### Watch Mode
```bash
npm run test
# Press 'p' to filter by filename
# Press 't' to filter by test name
```

### UI Mode
```bash
npm run test:ui
# Opens browser interface for test debugging
```

### Coverage Reports
```bash
npm run test:coverage
# Generates HTML coverage report in coverage/ directory
```

## CI/CD Integration

Tests are automatically run in the CI pipeline and must pass before merging. Coverage reports are generated and can be viewed in the pipeline artifacts.

### Required Checks
- ✅ All tests pass
- ✅ Coverage thresholds met
- ✅ No TypeScript errors
- ✅ ESLint passes

## Common Testing Patterns

### Testing Forms
```tsx
test('should validate required fields', async () => {
  render(<UserForm />)
  
  fireEvent.click(screen.getByRole('button', { name: /submit/i }))
  
  await waitFor(() => {
    expect(screen.getByText(/required/i)).toBeInTheDocument()
  })
})
```

### Testing Async Operations
```tsx
test('should show loading state during API call', async () => {
  render(<UserList />)
  
  expect(screen.getByTestId('loading-spinner')).toBeInTheDocument()
  
  await waitFor(() => {
    expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument()
  })
})
```

### Testing Error States
```tsx
test('should display error message on API failure', async () => {
  mockApi.get.mockRejectedValueOnce(new Error('Server error'))
  
  render(<UserList />)
  
  await waitFor(() => {
    expect(screen.getByText(/error.*occurred/i)).toBeInTheDocument()
  })
})
```

## Troubleshooting

### Common Issues

1. **Tests timeout**: Increase timeout in vitest.config.ts
2. **Module not found**: Check path aliases in vitest.config.ts
3. **React errors**: Ensure proper cleanup in useEffect hooks
4. **Mock issues**: Clear mocks between tests with `vi.clearAllMocks()`

### Performance Tips

- Use `screen.getByRole()` over `getByTestId()` when possible
- Mock heavy dependencies to speed up tests
- Use `vi.fn()` for function mocks instead of full module mocks
- Run specific test files during development: `npm run test -- UserService`

## Resources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- [Jest DOM Matchers](https://github.com/testing-library/jest-dom)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
