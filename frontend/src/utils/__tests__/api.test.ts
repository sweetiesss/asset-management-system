import { describe, it, expect, vi, beforeEach } from 'vitest'
import { AxiosError } from 'axios'
import { handleApiError } from '@/utils/api'
import { ErrorCode } from '@/configs/error-codes'
import { toast } from 'react-toastify'

vi.mock('react-toastify')

describe('handleApiError', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should handle AxiosError with response data', () => {
    const mockError = new AxiosError('Test error')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.USER_NOT_FOUND,
          message: 'User not found'
        }
      },
      status: 404,
      statusText: 'Not Found',
      headers: {},
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    const result = handleApiError(mockError)

    expect(result).toEqual({
      success: false,
      error: {
        code: ErrorCode.USER_NOT_FOUND,
        message: 'User not found'
      }
    })
  })

  it('should handle AxiosError without message and use predefined message', () => {
    const mockError = new AxiosError('Test error')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.INVALID_CREDENTIALS
        }
      },
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    const result = handleApiError(mockError)

    expect(result.success).toBe(false)
    expect(result.error?.code).toBe(ErrorCode.INVALID_CREDENTIALS)
    expect(result.error?.message).toBeDefined()
  })

  it('should handle regular Error instances', () => {
    const mockError = new Error('Regular error')

    const result = handleApiError(mockError)

    expect(result).toEqual({
      success: false,
      error: {
        message: 'Regular error',
        code: ErrorCode.UNKNOWN_ERROR
      }
    })
  })

  it('should handle unknown errors', () => {
    const mockError = 'Unknown error string'

    const result = handleApiError(mockError)

    expect(result.success).toBe(false)
    expect(result.error?.code).toBe(ErrorCode.UNKNOWN_ERROR)
    expect(result.error?.message).toBeDefined()
  })

  it('should show toast when showToast is true', () => {
    const mockError = new Error('Test error')

    handleApiError(mockError, true)

    expect(toast.error).toHaveBeenCalledWith('Test error')
  })

  it('should not show toast when showToast is false', () => {
    const mockError = new Error('Test error')

    handleApiError(mockError, false)

    expect(toast.error).not.toHaveBeenCalled()
  })

  it('should handle AxiosError without response data', () => {
    const mockError = new AxiosError('Network error')
    // No response property set

    const result = handleApiError(mockError)

    expect(result.success).toBe(false)
    expect(result.error?.code).toBe(ErrorCode.UNKNOWN_ERROR)
    expect(result.error?.message).toBe('Network error')
  })

  it('should handle AxiosError with empty response data', () => {
    const mockError = new AxiosError('Test error')
    mockError.response = {
      data: null,
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    const result = handleApiError(mockError)

    expect(result.success).toBe(false)
    expect(result.error?.code).toBe(ErrorCode.UNKNOWN_ERROR)
    expect(result.error?.message).toBe('Test error')
  })

  it('should handle TOKEN_EXPIRED error and redirect to login', () => {
    const originalLocation = window.location
    const mockLocation = {
      ...originalLocation,
      href: '',
      pathname: '/dashboard'
    }
    Object.defineProperty(window, 'location', {
      value: mockLocation,
      writable: true
    })

    const mockError = new AxiosError('Token expired')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.TOKEN_EXPIRED,
          message: 'Token has expired'
        }
      },
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    handleApiError(mockError)

    expect(mockLocation.href).toBe('/login')

    // Restore original location
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true
    })
  })

  it('should handle UNAUTHORIZED error and redirect to login', () => {
    const originalLocation = window.location
    const mockLocation = {
      ...originalLocation,
      href: '',
      pathname: '/users'
    }
    Object.defineProperty(window, 'location', {
      value: mockLocation,
      writable: true
    })

    const mockError = new AxiosError('Unauthorized')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.UNAUTHORIZED,
          message: 'Unauthorized access'
        }
      },
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    handleApiError(mockError)

    expect(mockLocation.href).toBe('/login')

    // Restore original location
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true
    })
  })

  it('should not redirect when already on login page', () => {
    const originalLocation = window.location
    const mockLocation = {
      ...originalLocation,
      href: '',
      pathname: '/login'
    }
    Object.defineProperty(window, 'location', {
      value: mockLocation,
      writable: true
    })

    const mockError = new AxiosError('Unauthorized')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.UNAUTHORIZED,
          message: 'Unauthorized access'
        }
      },
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    handleApiError(mockError)

    expect(mockLocation.href).toBe('')

    // Restore original location
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true
    })
  })

  it('should handle non-authentication errors without redirect', () => {
    const originalLocation = window.location
    const mockLocation = {
      ...originalLocation,
      href: '',
      pathname: '/dashboard'
    }
    Object.defineProperty(window, 'location', {
      value: mockLocation,
      writable: true
    })

    const mockError = new AxiosError('Validation error')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.VALIDATION_ERROR,
          message: 'Invalid input'
        }
      },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    handleApiError(mockError)

    expect(mockLocation.href).toBe('')

    // Restore original location
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true
    })
  })

  it('should show toast notification with error message', () => {
    const mockError = new AxiosError('API Error')
    mockError.response = {
      data: {
        success: false,
        error: {
          code: ErrorCode.BAD_REQUEST,
          message: 'Invalid request format'
        }
      },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      config: {} as any
    }

    handleApiError(mockError, true)

    expect(toast.error).toHaveBeenCalledWith('Invalid request format')
  })
})
