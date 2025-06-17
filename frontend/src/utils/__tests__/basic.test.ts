import { describe, it, expect } from 'vitest'

// Simple utility function tests that don't depend on external components
describe('Basic Math Utils', () => {
  const add = (a: number, b: number) => a + b
  const multiply = (a: number, b: number) => a * b
  
  it('should add two numbers correctly', () => {
    expect(add(2, 3)).toBe(5)
    expect(add(-1, 1)).toBe(0)
    expect(add(0, 0)).toBe(0)
  })

  it('should multiply two numbers correctly', () => {
    expect(multiply(2, 3)).toBe(6)
    expect(multiply(-1, 1)).toBe(-1)
    expect(multiply(0, 5)).toBe(0)
  })
})

// Test string utilities
describe('String Utils', () => {
  const capitalize = (str: string) => str.charAt(0).toUpperCase() + str.slice(1)
  
  it('should capitalize first letter', () => {
    expect(capitalize('hello')).toBe('Hello')
    expect(capitalize('world')).toBe('World')
    expect(capitalize('')).toBe('')
    expect(capitalize('a')).toBe('A')
  })
})

// Test array utilities
describe('Array Utils', () => {
  const unique = (arr: number[]) => [...new Set(arr)]
  
  it('should remove duplicates from array', () => {
    expect(unique([1, 2, 2, 3])).toEqual([1, 2, 3])
    expect(unique([1, 1, 1])).toEqual([1])
    expect(unique([])).toEqual([])
    expect(unique([1, 2, 3])).toEqual([1, 2, 3])
  })
})
