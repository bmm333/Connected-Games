import { createContext, useContext } from 'react'
import type { ConsoleDependencies } from './composition-root'

export const ConsoleDependenciesContext =
  createContext<ConsoleDependencies | undefined>(undefined)

export function useConsoleDependencies(): ConsoleDependencies {
  const dependencies = useContext(ConsoleDependenciesContext)
  if (dependencies === undefined) {
    throw new Error('Console dependencies are not available')
  }
  return dependencies
}
