import type { UserSession } from './UserSession'

export interface ConsoleSessionStore {
  get(): Promise<UserSession | undefined>
  save(session: UserSession, remember?: boolean): Promise<void>
  clear(): Promise<void>
}
