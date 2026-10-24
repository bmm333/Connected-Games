import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'

export interface ConsoleDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
}

export function createConsoleDependencies(): ConsoleDependencies {
  const platformStore = createPlatformKeyValueStore()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_CONSOLE_V1',
    ),
  }
}
