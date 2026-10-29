import type { AuthConsoleRepository } from '@domain/console/auth/AuthConsoleRepository'
import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'
import type { ConsoleAuthApiClient } from './ConsoleAuthApiClient'
import { mapConsoleUserSession, mapCurrentUserSession } from './mappers'

export function createAuthConsoleRepository(
  client: ConsoleAuthApiClient,
  sessionStore: ConsoleSessionStore,
): AuthConsoleRepository {
  return {
    async login(request) {
      return mapConsoleUserSession(await client.login(request))
    },
    async logout() {
      const session = await sessionStore.get()
      await client.logout(session?.token.accessToken)
    },
    async getCurrentSession() {
      const storedSession = await sessionStore.get()
      if (storedSession === undefined) return undefined
      const currentUser = await client.getCurrentUser(
        storedSession.token.accessToken,
      )
      return mapCurrentUserSession(storedSession, currentUser)
    },
    async refreshToken(refreshToken) {
      return mapConsoleUserSession(await client.refreshToken(refreshToken))
    },
  }
}
