import { describe, it, expect } from 'vitest'
import capitalizeEachWord from '@/utils/capitilizeUtils'

describe('capitalizeEachWord', () => {
  it('should capitalize each word in a sentence', () => {
    expect(capitalizeEachWord('hello world')).toBe('Hello World')
    expect(capitalizeEachWord('john doe smith')).toBe('John Doe Smith')
  })

  it('should handle single words', () => {
    expect(capitalizeEachWord('hello')).toBe('Hello')
    expect(capitalizeEachWord('HELLO')).toBe('Hello')
    expect(capitalizeEachWord('hELLo')).toBe('Hello')
  })

  it('should handle empty strings', () => {
    expect(capitalizeEachWord('')).toBe('')
  })

  it('should handle strings with extra spaces', () => {
    expect(capitalizeEachWord('  hello   world  ')).toBe('Hello World')
    expect(capitalizeEachWord(' john  doe ')).toBe('John Doe')
  })

  it('should handle strings with multiple spaces between words', () => {
    expect(capitalizeEachWord('hello     world')).toBe('Hello World')
    expect(capitalizeEachWord('john    doe    smith')).toBe('John Doe Smith')
  })

  it('should handle strings with tabs and newlines', () => {
    expect(capitalizeEachWord('hello\tworld')).toBe('Hello World')
    expect(capitalizeEachWord('hello\nworld')).toBe('Hello World')
    expect(capitalizeEachWord('hello\r\nworld')).toBe('Hello World')
  })

  it('should handle special characters', () => {
    expect(capitalizeEachWord("o'connor")).toBe("O'connor")
    expect(capitalizeEachWord('jean-claude')).toBe('Jean-claude')
  })

  it('should handle numbers and mixed content', () => {
    expect(capitalizeEachWord('hello 123 world')).toBe('Hello 123 World')
    expect(capitalizeEachWord('john 2nd avenue')).toBe('John 2nd Avenue')
  })
})
