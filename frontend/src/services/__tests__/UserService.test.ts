import { describe, it, expect } from 'vitest'
import { UserService } from '@/services/UserService'

describe('UserService', () => {
  it('should have getMe method', () => {
    expect(typeof UserService.getMe).toBe('function')
  })

  it('should have getUsers method', () => {
    expect(typeof UserService.getUsers).toBe('function')
  })

  it('should have createUser method', () => {
    expect(typeof UserService.createUser).toBe('function')
  })

  it('should have updateUser method', () => {
    expect(typeof UserService.updateUser).toBe('function')
  })

  it('should have login method', () => {
    expect(typeof UserService.login).toBe('function')
  })

  it('should have logout method', () => {
    expect(typeof UserService.logout).toBe('function')
  })

  it('should have refreshAccessToken method', () => {
    expect(typeof UserService.refreshAccessToken).toBe('function')
  })

  it('should have getUser method', () => {
    expect(typeof UserService.getUser).toBe('function')
  })
})
