import type { AuthConsoleRepository } from '@domain/console/auth/AuthConsoleRepository'
import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'

export function createAuthenticatedFetch(
  repository: AuthConsoleRepository,
  sessionStore: ConsoleSessionStore,
  onUnauthorized: () => void,
): typeof fetch {
  return async (input, init) => {
    const session = await sessionStore.get()
    const headers = new Headers(init?.headers)
    if (session?.token.accessToken !== undefined) {
      headers.set('Authorization', `Bearer ${session.token.accessToken}`)
    }

    let response = await fetch(input, { ...init, headers })
    if (response.status !== 401 || session?.token.refreshToken === undefined) {
      if (response.status === 401 || response.status === 403) onUnauthorized()
      return response
    }

    let refreshed
    try {
      refreshed = await repository.refreshToken(session.token.refreshToken)
    } catch (error) {
      onUnauthorized()
      throw error
    }
    await sessionStore.save(refreshed, true)
    headers.set('Authorization', `Bearer ${refreshed.token.accessToken}`)
    response = await fetch(input, { ...init, headers })
    if (response.status === 401 || response.status === 403) onUnauthorized()
    return response
  }
}
