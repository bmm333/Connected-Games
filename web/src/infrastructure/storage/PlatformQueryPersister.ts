import type {
  Persister,
  PersistedClient,
} from '@tanstack/react-query-persist-client'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'

export function createPlatformQueryPersister(
  store: KeyValueStore,
  key: string,
): Persister {
  return {
    persistClient: (client: PersistedClient) => store.set(key, client),
    restoreClient: () => store.get<PersistedClient>(key),
    removeClient: () => store.remove(key),
  }
}
