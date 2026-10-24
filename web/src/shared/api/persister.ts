import { get, set, del } from 'idb-keyval'
import type { Persister, PersistedClient } from '@tanstack/react-query-persist-client'

export const IDB_CACHE_KEY = 'PARTITA_QUERY_CACHE_V1'

/**
 * Creates an IndexedDB persister for TanStack Query.
 * Enables reliable offline cache restoration without the 5MB localStorage limit.
 */
export function createIdbPersister(key = IDB_CACHE_KEY): Persister {
  return {
    persistClient: async (client: PersistedClient): Promise<void> => {
      try {
        await set(key, client)
      } catch {
        // Fallback or quota handling - safe failure in private browsing mode
      }
    },
    restoreClient: async (): Promise<PersistedClient | undefined> => {
      try {
        return await get<PersistedClient>(key)
      } catch {
        return undefined
      }
    },
    removeClient: async (): Promise<void> => {
      try {
        await del(key)
      } catch {
        // Safe ignore
      }
    },
  }
}

export const idbPersister = createIdbPersister()
