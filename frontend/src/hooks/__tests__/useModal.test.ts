import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import useModal from '@/hooks/useModal'

describe('useModal', () => {
  it('should initialize with default value false', () => {
    const { result } = renderHook(() => useModal())

    expect(result.current.isModalVisible).toBe(false)
  })

  it('should initialize with provided default value', () => {
    const { result } = renderHook(() => useModal(true))

    expect(result.current.isModalVisible).toBe(true)
  })

  it('should open modal when openModal is called', () => {
    const { result } = renderHook(() => useModal())

    act(() => {
      result.current.openModal()
    })

    expect(result.current.isModalVisible).toBe(true)
  })

  it('should close modal when closeModal is called', () => {
    const { result } = renderHook(() => useModal(true))

    act(() => {
      result.current.closeModal()
    })

    expect(result.current.isModalVisible).toBe(false)
  })

  it('should set modal visibility when setModal is called', () => {
    const { result } = renderHook(() => useModal())

    act(() => {
      result.current.setModal(true)
    })

    expect(result.current.isModalVisible).toBe(true)

    act(() => {
      result.current.setModal(false)
    })

    expect(result.current.isModalVisible).toBe(false)
  })

  it('should toggle modal state correctly', () => {
    const { result } = renderHook(() => useModal())

    // Start with false
    expect(result.current.isModalVisible).toBe(false)

    // Open modal
    act(() => {
      result.current.openModal()
    })
    expect(result.current.isModalVisible).toBe(true)

    // Close modal
    act(() => {
      result.current.closeModal()
    })
    expect(result.current.isModalVisible).toBe(false)

    // Set to true
    act(() => {
      result.current.setModal(true)
    })
    expect(result.current.isModalVisible).toBe(true)
  })
})
