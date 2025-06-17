import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useToggle } from '@/hooks/useToggle'

describe('useToggle', () => {
  it('should initialize with default value false', () => {
    const { result } = renderHook(() => useToggle())

    expect(result.current.value).toBe(false)
  })

  it('should initialize with provided initial value', () => {
    const { result } = renderHook(() => useToggle(true))

    expect(result.current.value).toBe(true)
  })

  it('should toggle value when toggle is called', () => {
    const { result } = renderHook(() => useToggle(false))

    act(() => {
      result.current.toggle()
    })

    expect(result.current.value).toBe(true)

    act(() => {
      result.current.toggle()
    })

    expect(result.current.value).toBe(false)
  })

  it('should set value to true when setTrue is called', () => {
    const { result } = renderHook(() => useToggle(false))

    act(() => {
      result.current.setTrue()
    })

    expect(result.current.value).toBe(true)

    // Should remain true even if called again
    act(() => {
      result.current.setTrue()
    })

    expect(result.current.value).toBe(true)
  })

  it('should set value to false when setFalse is called', () => {
    const { result } = renderHook(() => useToggle(true))

    act(() => {
      result.current.setFalse()
    })

    expect(result.current.value).toBe(false)

    // Should remain false even if called again
    act(() => {
      result.current.setFalse()
    })

    expect(result.current.value).toBe(false)
  })

  it('should set specific value when setValue is called', () => {
    const { result } = renderHook(() => useToggle())

    act(() => {
      result.current.setValue(true)
    })

    expect(result.current.value).toBe(true)

    act(() => {
      result.current.setValue(false)
    })

    expect(result.current.value).toBe(false)
  })

  it('should provide stable function references', () => {
    const { result, rerender } = renderHook(() => useToggle())

    const firstRender = {
      toggle: result.current.toggle,
      setTrue: result.current.setTrue,
      setFalse: result.current.setFalse,
      setValue: result.current.setValue,
    }

    rerender()

    expect(result.current.toggle).toBe(firstRender.toggle)
    expect(result.current.setTrue).toBe(firstRender.setTrue)
    expect(result.current.setFalse).toBe(firstRender.setFalse)
    expect(result.current.setValue).toBe(firstRender.setValue)
  })

  it('should work with multiple toggles in sequence', () => {
    const { result } = renderHook(() => useToggle())

    // Initial state
    expect(result.current.value).toBe(false)

    // Toggle sequence: false -> true -> false -> true
    act(() => {
      result.current.toggle()
    })
    expect(result.current.value).toBe(true)

    act(() => {
      result.current.toggle()
    })
    expect(result.current.value).toBe(false)

    act(() => {
      result.current.setTrue()
    })
    expect(result.current.value).toBe(true)

    act(() => {
      result.current.setFalse()
    })
    expect(result.current.value).toBe(false)
  })
})
