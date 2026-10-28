import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'

export interface ConsoleAuthApiClient {
  login(request: ConsoleLoginRequest): Promise<unknown>
  logout(accessToken?: string): Promise<void>
  getCurrentUser(accessToken: string): Promise<unknown>
  refreshToken(refreshToken: string): Promise<unknown>
}
