import type { UserSession } from './UserSession'

export interface ConsoleLoginRequest {
  email: string
  password: string
  remember?: boolean
}

export interface AuthConsoleRepository {
  login(request: ConsoleLoginRequest): Promise<UserSession>
  logout(): Promise<void>
  getCurrentSession(): Promise<UserSession | undefined>
  refreshToken(refreshToken: string): Promise<UserSession>
}
