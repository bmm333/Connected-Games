import { createContext } from 'react'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'
import type { Permission } from '@domain/console/auth/Role'
import type { UserSession } from '@domain/console/auth/UserSession'

export interface ConsoleAuthContextValue {
  session: UserSession | undefined
  isLoading: boolean
  isAuthenticated: boolean
  login(request: ConsoleLoginRequest): Promise<UserSession>
  logout(): Promise<void>
  canAccess(permission: Permission): boolean
}

export const ConsoleAuthContext = createContext<
  ConsoleAuthContextValue | undefined
>(undefined)
