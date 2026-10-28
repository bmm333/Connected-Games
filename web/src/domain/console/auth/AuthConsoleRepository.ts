import type { UserSession } from './UserSession'

export type ConsoleLoginRequest =
  | {
      method: 'credentials'
      email: string
      password: string
      remember?: boolean
    }
  | {
      method: 'sso'
      provider: string
      assertion: string
      remember?: boolean
    }

export interface AuthConsoleRepository {
  login(request: ConsoleLoginRequest): Promise<UserSession>
  logout(): Promise<void>
  getCurrentSession(): Promise<UserSession | undefined>
  refreshToken(refreshToken: string): Promise<UserSession>
}
