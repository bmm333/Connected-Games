import type { SessionRepository } from '@domain/auth/SessionRepository'
import type { Session } from '@domain/auth/Session'

export type GetStoredSession = () => Promise<Session | undefined>

export function createGetStoredSession(
  sessionRepository: SessionRepository,
): GetStoredSession {
  return async () => {
    const session = await sessionRepository.getStoredSession()
    if (session === undefined) {
      return undefined
    }

    const expiresAt = session.token.expiresAt
    if (expiresAt !== undefined && Date.parse(expiresAt) <= Date.now()) {
      await sessionRepository.clearSession()
      return undefined
    }

    return session
  }
}
