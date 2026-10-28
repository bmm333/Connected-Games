import type {
  ConsoleLoginRequest,
} from '@domain/console/auth/AuthConsoleRepository'

export interface ConsoleAuthApiClient {
  login(request: ConsoleLoginRequest): Promise<unknown>
  logout(accessToken?: string): Promise<void>
  getCurrentSession(accessToken: string): Promise<unknown | undefined>
  refreshToken(refreshToken: string): Promise<unknown>
}
