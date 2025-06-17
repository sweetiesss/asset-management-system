import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { isDate, calculateAge, isDateInFuture, formatDate, isTodayOrFuture } from '@/utils/dateUtils'

describe('dateUtils', () => {
  // Mock the current date to make tests deterministic
  const mockDate = new Date('2024-06-15T10:00:00.000Z') // Fixed date: June 15, 2024
  
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(mockDate)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('isDate', () => {
    it('should return true for valid Date objects', () => {
      const validDate = new Date('2023-01-01')
      expect(isDate(validDate)).toBe(true)
    })

    it('should return false for invalid Date objects', () => {
      const invalidDate = new Date('invalid-date')
      expect(isDate(invalidDate)).toBe(false)
    })

    it('should return false for non-Date objects', () => {
      // Test with actual wrong types instead of using `any`
      expect(isDate('2023-01-01' as unknown as Date)).toBe(false)
      expect(isDate(null as unknown as Date)).toBe(false)
      expect(isDate(undefined as unknown as Date)).toBe(false)
      expect(isDate(123 as unknown as Date)).toBe(false)
      expect(isDate({} as unknown as Date)).toBe(false)
      expect(isDate([] as unknown as Date)).toBe(false)
    })
  })

  describe('calculateAge', () => {
    it('should calculate age correctly for a past date', () => {
      // Person born on January 1, 1990
      // Current date is June 15, 2024, so they should be 34
      const birthDate = new Date('1990-01-01')
      const age = calculateAge(birthDate)
      
      expect(age).toBe(34)
    })

    it('should calculate age correctly when birthday has not occurred this year', () => {
      // Person born on December 25, 1990 (birthday hasn't occurred in 2024 yet)
      // Current date is June 15, 2024, so they should be 33
      const birthDate = new Date('1990-12-25')
      const age = calculateAge(birthDate)
      
      expect(age).toBe(33)
    })

    it('should calculate age correctly when birthday has occurred this year', () => {
      // Person born on March 10, 1995 (birthday already occurred in 2024)
      // Current date is June 15, 2024, so they should be 29
      const birthDate = new Date('1995-03-10')
      const age = calculateAge(birthDate)
      
      expect(age).toBe(29)
    })

    it('should handle same day birthday correctly', () => {
      // Person born on June 15, 1985 (exact same day and month)
      // Current date is June 15, 2024, so they should be exactly 39
      const birthDate = new Date('1985-06-15')
      const age = calculateAge(birthDate)
      
      expect(age).toBe(39)
    })

    it('should handle edge case when birthday is tomorrow', () => {
      // Person born on June 16, 1990 (birthday is tomorrow)
      // Current date is June 15, 2024, so they should be 33
      const birthDate = new Date('1990-06-16')
      const age = calculateAge(birthDate)
      
      expect(age).toBe(33)
    })
  })

  describe('isDateInFuture', () => {
    it('should return true for future dates', () => {
      const futureDate = new Date('2024-12-25') // Christmas 2024
      expect(isDateInFuture(futureDate)).toBe(true)
    })

    it('should return false for past dates', () => {
      const pastDate = new Date('2024-01-01') // New Year 2024
      expect(isDateInFuture(pastDate)).toBe(false)
    })

    it('should return false for current date/time', () => {
      const now = new Date('2024-06-15T10:00:00.000Z') // Exact current time
      expect(isDateInFuture(now)).toBe(false)
    })

    it('should return true for dates with same day but later time', () => {
      const laterToday = new Date('2024-06-15T15:00:00.000Z') // Later today
      expect(isDateInFuture(laterToday)).toBe(true)
    })
  })

  describe('formatDate', () => {
    it('should format date string correctly', () => {
      const dateString = '2023-12-25'
      const formatted = formatDate(dateString)
      
      expect(formatted).toBe('25/12/2023')
    })

    it('should handle single digit months and days', () => {
      const dateString = '2023-01-05'
      const formatted = formatDate(dateString)
      
      expect(formatted).toBe('05/01/2023')
    })

    it('should return empty string for empty input', () => {
      expect(formatDate('')).toBe('')
    })

    it('should return empty string for falsy inputs', () => {
      // Use proper type assertions for testing edge cases
      expect(formatDate(null as unknown as string)).toBe('')
      expect(formatDate(undefined as unknown as string)).toBe('')
    })
  })

  describe('isTodayOrFuture', () => {
    it('should return true for today', () => {
      const today = new Date('2024-06-15')
      expect(isTodayOrFuture(today)).toBe(true)
    })

    it('should return true for future dates', () => {
      const future = new Date('2024-06-16')
      expect(isTodayOrFuture(future)).toBe(true)
    })

    it('should return false for past dates', () => {
      const past = new Date('2024-06-14')
      expect(isTodayOrFuture(past)).toBe(false)
    })

    it('should ignore time when comparing dates', () => {
      const todayEvening = new Date('2024-06-15T23:59:59.999Z')
      expect(isTodayOrFuture(todayEvening)).toBe(true)
    })
  })
})
