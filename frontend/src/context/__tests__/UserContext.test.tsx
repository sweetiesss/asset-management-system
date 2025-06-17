import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { UserProvider, useUser } from '@/context/UserContext'
import { UserService } from '@/services/UserService'

vi.mock('@/services/UserService')
vi.mock('react-toastify')

const mockUserService = vi.mocked(UserService)

// Mock user data that matches CurrentUserInfo interface
const mockUserInfo = {
  id: '1',
  staffCode: 'SD0001',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@nashtech.vn',
  username: 'johndoe',
  roles: ['ADMIN'],
  location: 'HCM',
  type: 'ADMIN',
  gender: 'MALE',
  joinedDate: '2023-01-01',
  status: 'ACTIVE',
  changePasswordRequired: false
}

const mockAuth = {
  accessToken: 'mock-access-token',
  id: '1',
  username: 'johndoe',
  roles: ['ADMIN'],
  changePasswordRequired: false
}

const TestComponent = () => {
  const { auth, isLoading, setAuth, getAccessToken } = useUser()
  
  return (
    <div>
      <div data-testid="loading">{isLoading ? 'Loading' : 'Not Loading'}</div>
      <div data-testid="auth">{auth ? JSON.stringify(auth) : 'No Auth'}</div>
      <button 
        data-testid="set-auth" 
        onClick={() => setAuth(mockAuth)}
      >
        Set Auth
      </button>
      <button 
        data-testid="clear-auth" 
        onClick={() => setAuth(null)}
      >
        Clear Auth
      </button>
      <div data-testid="token">{getAccessToken() || 'No Token'}</div>
    </div>
  )
}

describe('UserContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should initialize with loading state', async () => {
    mockUserService.refreshAccessToken.mockResolvedValueOnce({
      success: false,
      message: 'No refresh token',
      error: { message: 'Not authenticated', code: 'UNAUTHORIZED' }
    })

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    expect(screen.getByTestId('loading')).toHaveTextContent('Loading')
    
    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading')
    })
  })

  it('should set auth when user is authenticated', async () => {
    // Mock both refreshAccessToken and getMe
    mockUserService.refreshAccessToken.mockResolvedValueOnce({
      success: true,
      message: 'Token refreshed successfully',
      data: { accessToken: 'new-access-token' }
    })
    
    mockUserService.getMe.mockResolvedValueOnce({
      success: true,
      message: 'User retrieved successfully',
      data: mockUserInfo
    })

    // Mock localStorage with valid tokens
    localStorage.setItem('accessToken', 'valid-token')
    localStorage.setItem('refreshToken', 'valid-refresh-token')

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    await waitFor(() => {
      const authElement = screen.getByTestId('auth')
      expect(authElement.textContent).toContain(mockUserInfo.staffCode)
    }, { timeout: 3000 })
  })

  it('should allow setting auth manually', async () => {
    mockUserService.refreshAccessToken.mockResolvedValueOnce({
      success: false,
      message: 'No refresh token',
      error: { message: 'Not authenticated', code: 'UNAUTHORIZED' }
    })

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading')
    })

    const setAuthButton = screen.getByTestId('set-auth')
    setAuthButton.click()

    await waitFor(() => {
      const authElement = screen.getByTestId('auth')
      expect(authElement.textContent).toContain('mock-access-token')
    })
  })

  it('should allow clearing auth', async () => {
    mockUserService.refreshAccessToken.mockResolvedValueOnce({
      success: false,
      message: 'No refresh token',
      error: { message: 'Not authenticated', code: 'UNAUTHORIZED' }
    })

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading')
    })

    // Set auth first
    const setAuthButton = screen.getByTestId('set-auth')
    setAuthButton.click()

    await waitFor(() => {
      expect(screen.getByTestId('auth')).not.toHaveTextContent('No Auth')
    })

    // Clear auth
    const clearAuthButton = screen.getByTestId('clear-auth')
    clearAuthButton.click()

    await waitFor(() => {
      expect(screen.getByTestId('auth')).toHaveTextContent('No Auth')
    })
  })

  it('should return access token when auth is set', async () => {
    mockUserService.refreshAccessToken.mockResolvedValueOnce({
      success: false,
      message: 'No refresh token',
      error: { message: 'Not authenticated', code: 'UNAUTHORIZED' }
    })

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading')
    })

    // Set auth with token
    const setAuthButton = screen.getByTestId('set-auth')
    setAuthButton.click()

    // Wait for auth to be set and verify it contains the access token
    await waitFor(() => {
      const authElement = screen.getByTestId('auth')
      const authContent = authElement.textContent
      expect(authContent).toContain('mock-access-token')
      expect(authContent).toContain('accessToken')
    })
  })

  it('should handle getMe API error gracefully', async () => {
    mockUserService.refreshAccessToken.mockRejectedValueOnce(new Error('Network error'))

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading')
      expect(screen.getByTestId('auth')).toHaveTextContent('No Auth')
    })
  })
})
