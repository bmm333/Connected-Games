import type {
  AuthConsoleRepository,
  ConsoleLoginRequest,
} from '@domain/console/auth/AuthConsoleRepository'
import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'
import type { UserSession } from '@domain/console/auth/UserSession'
import { validateUserSession } from './sessionValidation'

export type LoginWithRoleUseCase = (
  request: ConsoleLoginRequest,
) => Promise<UserSession>

export function createLoginWithRoleUseCase(
  repository: AuthConsoleRepository,
  sessionStore: ConsoleSessionStore,
): LoginWithRoleUseCase {
  return async (request) => {
    if (request.email.trim().length === 0 || request.password.length === 0) {
      throw new Error('Console email and password are required')
    }

    const session = validateUserSession(await repository.login(request))
    await sessionStore.save(session, request.remember)
    return session
  }
}
