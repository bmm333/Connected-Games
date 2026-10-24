import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'

export interface KioskDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
}

export function createKioskDependencies(): KioskDependencies {
  const platformStore = createPlatformKeyValueStore()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_KIOSK_V1',
    ),
  }
}
