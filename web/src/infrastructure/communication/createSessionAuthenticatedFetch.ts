import { createAuthRepository } from '@infrastructure/api/AuthRepository'
import { createAuthenticatedFetch } from '@infrastructure/api/AuthenticatedFetch'
import { createSessionRepository } from '@infrastructure/storage/SessionRepository'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'

export function createSessionAuthenticatedFetch(store: KeyValueStore): typeof fetch {
  const sessions = createSessionRepository(store)
  const auth = createAuthRepository()

  return createAuthenticatedFetch({
    async getSession() {
      const session = await sessions.getStoredSession()
      if (session?.token.expiresAt && Date.parse(session.token.expiresAt) <= Date.now()) {
        await sessions.clearSession()
        return undefined
      }
      return session
    },
    refreshToken: (refreshToken) => auth.refreshToken(refreshToken),
    saveSession: (session) => sessions.saveSession(session),
    onUnauthorized() {
      void sessions.clearSession()
    },
  })
}
