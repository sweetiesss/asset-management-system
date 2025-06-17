import { mutate } from "swr"

export const mutateKeyContain = (subKey: string) => {
    mutate((key) => {
        if (Array.isArray(key)) {
          return key.some(part => typeof part === 'string' && part.includes(subKey))
        }
        return typeof key === 'string' && key.includes(subKey)
      })
}