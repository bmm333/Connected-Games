import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createCatalogRepository } from '@infrastructure/api/CatalogRepository'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'
import {
  createResolveTableByCode,
  type ResolveTableByCode,
} from '@use-cases/catalog/ResolveTableByCode'

export interface PlayerDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  catalog: {
    resolveTableByCode: ResolveTableByCode
  }
}

export function createPlayerDependencies(): PlayerDependencies {
  const catalogRepository = createCatalogRepository()
  const platformStore = createPlatformKeyValueStore()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_V1',
    ),
    catalog: {
      resolveTableByCode: createResolveTableByCode(catalogRepository),
    },
  }
}
