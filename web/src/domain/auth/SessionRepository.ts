import type { Session } from './Session'

export interface SessionRepository {
  getStoredSession(): Promise<Session | undefined>
  saveSession(session: Session): Promise<void>
  clearSession(): Promise<void>
}
