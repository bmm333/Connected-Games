import { Capacitor } from '@capacitor/core'
import { Preferences } from '@capacitor/preferences'
import type { ConsoleSessionStore as DomainConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'
import type { UserSession } from '@domain/console/auth/UserSession'

const PERSISTENT_KEY = 'PARTITA_CONSOLE_SESSION_V1'
const SESSION_KEY = 'PARTITA_CONSOLE_SESSION_SESSION_V1'

function parseSession(value: string | null): UserSession | undefined {
  if (value === null) return undefined
  try {
    return JSON.parse(value) as UserSession
  } catch {
    throw new Error('Stored console session is invalid JSON')
  }
}

export function createConsoleSessionStore(): DomainConsoleSessionStore {
  if (Capacitor.isNativePlatform()) {
    return {
      async get() {
        const persistent = await Preferences.get({ key: PERSISTENT_KEY })
        const session = await Preferences.get({ key: SESSION_KEY })
        return parseSession(persistent.value ?? session.value)
      },
      async save(session, remember = false) {
        const key = remember ? PERSISTENT_KEY : SESSION_KEY
        const otherKey = remember ? SESSION_KEY : PERSISTENT_KEY
        await Preferences.set({ key, value: JSON.stringify(session) })
        await Preferences.remove({ key: otherKey })
      },
      async clear() {
        await Preferences.remove({ key: PERSISTENT_KEY })
        await Preferences.remove({ key: SESSION_KEY })
      },
    }
  }

  return {
    async get() {
      return (
        parseSession(globalThis.localStorage.getItem(PERSISTENT_KEY)) ??
        parseSession(globalThis.sessionStorage.getItem(SESSION_KEY))
      )
    },
    async save(session, remember = false) {
      const serialized = JSON.stringify(session)
      if (remember) {
        globalThis.localStorage.setItem(PERSISTENT_KEY, serialized)
        globalThis.sessionStorage.removeItem(SESSION_KEY)
      } else {
        globalThis.sessionStorage.setItem(SESSION_KEY, serialized)
        globalThis.localStorage.removeItem(PERSISTENT_KEY)
      }
    },
    async clear() {
      globalThis.localStorage.removeItem(PERSISTENT_KEY)
      globalThis.sessionStorage.removeItem(SESSION_KEY)
    },
  }
}
