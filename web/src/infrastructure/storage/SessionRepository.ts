import type { SessionRepository as DomainSessionRepository } from '@domain/auth/SessionRepository'
import type { Session } from '@domain/auth/Session'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'

const SESSION_STORAGE_KEY = 'PARTITA_AUTH_SESSION_V1'

export function createSessionRepository(
  store: KeyValueStore,
): DomainSessionRepository {
  return {
    getStoredSession: () => store.get<Session>(SESSION_STORAGE_KEY),
    saveSession: (session) => store.set(SESSION_STORAGE_KEY, session),
    clearSession: () => store.remove(SESSION_STORAGE_KEY),
  }
}
