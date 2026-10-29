import type { AuthConsoleRepository } from '@domain/console/auth/AuthConsoleRepository'
import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'

export type LogoutUseCase = () => Promise<void>

export function createLogoutUseCase(
  repository: AuthConsoleRepository,
  sessionStore: ConsoleSessionStore,
): LogoutUseCase {
  return async () => {
    await repository.logout()
    await sessionStore.clear()
  }
}
