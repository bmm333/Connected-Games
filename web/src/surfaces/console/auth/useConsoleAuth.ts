import { useContext } from 'react'
import { ConsoleAuthContext } from './ConsoleAuthContext'

export function useConsoleAuth() {
  const context = useContext(ConsoleAuthContext)
  if (context === undefined) {
    throw new Error('Console auth context is not available')
  }
  return context
}
