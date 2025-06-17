import { describe, it, expect } from 'vitest'
import {
  firstNameValidator,
  lastNameValidator,
  passwordValidator,
  assetNameValidator,
  categoryNameValidator,
  categoryPrefixValidator,
  dobValidator,
  joinedAtValidator,
} from '@/utils/validations/validators'

describe('Form Validators', () => {
  describe('firstNameValidator', () => {
    it('should validate correct first names', () => {
      expect(firstNameValidator.safeParse('John').success).toBe(true)
      expect(firstNameValidator.safeParse('Mary Jane').success).toBe(true)
      expect(firstNameValidator.safeParse('Anne Marie').success).toBe(true)
    })

    it('should reject invalid first names', () => {
      expect(firstNameValidator.safeParse('').success).toBe(false)
      expect(firstNameValidator.safeParse('John123').success).toBe(false)
      expect(firstNameValidator.safeParse('John@Doe').success).toBe(false)
      expect(firstNameValidator.safeParse('John_Doe').success).toBe(false)
    })
  })

  describe('lastNameValidator', () => {
    it('should validate correct last names', () => {
      expect(lastNameValidator.safeParse('Doe').success).toBe(true)
      expect(lastNameValidator.safeParse('Van Der Berg').success).toBe(true)
      expect(lastNameValidator.safeParse('Smith').success).toBe(true)
    })

    it('should reject invalid last names', () => {
      expect(lastNameValidator.safeParse('').success).toBe(false)
      expect(lastNameValidator.safeParse('Doe123').success).toBe(false)
      expect(lastNameValidator.safeParse('Smith@').success).toBe(false)
    })
  })

  describe('passwordValidator', () => {
    it('should validate strong passwords', () => {
      expect(passwordValidator.safeParse('Password123!').success).toBe(true)
      expect(passwordValidator.safeParse('MyStrongP@ss1').success).toBe(true)
      expect(passwordValidator.safeParse('Secure$Pass123').success).toBe(true)
    })

    it('should reject weak passwords', () => {
      expect(passwordValidator.safeParse('password').success).toBe(false) // No uppercase, no numbers, no special chars
      expect(passwordValidator.safeParse('PASSWORD').success).toBe(false) // No lowercase, no numbers, no special chars
      expect(passwordValidator.safeParse('Password').success).toBe(false) // No numbers, no special chars
      expect(passwordValidator.safeParse('Password123').success).toBe(false) // No special chars
      expect(passwordValidator.safeParse('Pass1!').success).toBe(false) // Too short (6 chars)
      expect(passwordValidator.safeParse('').success).toBe(false) // Empty
    })
  })

  describe('assetNameValidator', () => {
    it('should validate correct asset names', () => {
      expect(assetNameValidator.safeParse('Laptop Dell').success).toBe(true)
      expect(assetNameValidator.safeParse('Monitor 24 inch').success).toBe(true)
      expect(assetNameValidator.safeParse('iPhone 13').success).toBe(true)
    })

    it('should reject invalid asset names', () => {
      expect(assetNameValidator.safeParse('').success).toBe(false) // Too short
      expect(assetNameValidator.safeParse('Laptop@Dell').success).toBe(false) // Special characters
      expect(assetNameValidator.safeParse('Monitor_24').success).toBe(false) // Underscore
    })
  })

  describe('categoryNameValidator', () => {
    it('should validate correct category names', () => {
      expect(categoryNameValidator.safeParse('Laptop').success).toBe(true)
      expect(categoryNameValidator.safeParse('Mobile Phone').success).toBe(true)
      expect(categoryNameValidator.safeParse('Desktop Computer').success).toBe(true)
    })

    it('should reject invalid category names', () => {
      expect(categoryNameValidator.safeParse('').success).toBe(false) // Too short
      expect(categoryNameValidator.safeParse('Category@Name').success).toBe(false) // Special characters
      expect(categoryNameValidator.safeParse('Category_Name').success).toBe(false) // Underscore
    })
  })

  describe('categoryPrefixValidator', () => {
    it('should validate correct category prefixes', () => {
      expect(categoryPrefixValidator.safeParse('LA').success).toBe(true)
      expect(categoryPrefixValidator.safeParse('MO').success).toBe(true)
      expect(categoryPrefixValidator.safeParse('DE').success).toBe(true)
    })

    it('should reject invalid category prefixes', () => {
      expect(categoryPrefixValidator.safeParse('').success).toBe(false) // Empty
      expect(categoryPrefixValidator.safeParse('L').success).toBe(false) // Too short
      expect(categoryPrefixValidator.safeParse('LAP').success).toBe(false) // Too long
      expect(categoryPrefixValidator.safeParse('la').success).toBe(false) // Lowercase
      expect(categoryPrefixValidator.safeParse('L1').success).toBe(false) // Contains number
      expect(categoryPrefixValidator.safeParse('L@').success).toBe(false) // Special character
    })
  })

  describe('dobValidator', () => {
    it('should validate correct dates of birth', () => {
      // Valid adult DOB (over 18 years old)
      const validDOB = new Date()
      validDOB.setFullYear(validDOB.getFullYear() - 25)
      
      expect(dobValidator.safeParse(validDOB.toISOString().split('T')[0]).success).toBe(true)
    })

    it('should reject invalid dates of birth', () => {
      expect(dobValidator.safeParse('').success).toBe(false) // Empty
      expect(dobValidator.safeParse('invalid-date').success).toBe(false) // Invalid format
      
      // Future date
      const futureDate = new Date()
      futureDate.setFullYear(futureDate.getFullYear() + 1)
      expect(dobValidator.safeParse(futureDate.toISOString().split('T')[0]).success).toBe(false)
      
      // Too young (under 18)
      const youngDate = new Date()
      youngDate.setFullYear(youngDate.getFullYear() - 10)
      expect(dobValidator.safeParse(youngDate.toISOString().split('T')[0]).success).toBe(false)
    })
  })

  describe('joinedAtValidator', () => {
    it('should validate correct joined dates (weekdays)', () => {
      // Monday, January 2, 2023
      expect(joinedAtValidator.safeParse('2023-01-02').success).toBe(true)
      // Tuesday, January 3, 2023
      expect(joinedAtValidator.safeParse('2023-01-03').success).toBe(true)
    })

    it('should reject invalid joined dates', () => {
      expect(joinedAtValidator.safeParse('').success).toBe(false) // Empty
      expect(joinedAtValidator.safeParse('invalid-date').success).toBe(false) // Invalid format
      
      // Weekend dates
      // Saturday, January 7, 2023
      expect(joinedAtValidator.safeParse('2023-01-07').success).toBe(false)
      // Sunday, January 8, 2023
      expect(joinedAtValidator.safeParse('2023-01-08').success).toBe(false)
    })
  })
})
