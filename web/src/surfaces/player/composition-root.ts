import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import { createCommunicationServices } from '@infrastructure/communication/createCommunicationServices'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'
import {
  createResolveTableByCode,
  type ResolveTableByCode,
} from '@use-cases/catalog/ResolveTableByCode'
import { createAuthDependencies } from '@surfaces/shared/auth-dependencies'

export interface PlayerDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  auth: ReturnType<typeof createAuthDependencies>
  catalog: {
    resolveTableByCode: ResolveTableByCode
  }
}

export function createPlayerDependencies(): PlayerDependencies {
  const communication = createCommunicationServices()
  const platformStore = createPlatformKeyValueStore()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_V1',
    ),
    auth: createAuthDependencies(),
    catalog: {
      resolveTableByCode: createResolveTableByCode(communication.catalog),
    },
  }
}
