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
import type { SimulatorCatalogRepository } from '@domain/catalog/CatalogRepository'
import type { MatchRestClient, MatchSseClient } from '@domain/simulator'
import type { StatsRepository } from '@domain/stats/StatsRepository'

export interface PlayerDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  auth: ReturnType<typeof createAuthDependencies>
  catalog: {
    resolveTableByCode: ResolveTableByCode
    listVenues: SimulatorCatalogRepository['listVenues']
    listTables: SimulatorCatalogRepository['listTables']
  }
  matches: MatchRestClient & MatchSseClient
  stats: StatsRepository
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
    auth: createAuthDependencies(platformStore),
    catalog: {
      resolveTableByCode: createResolveTableByCode(communication.catalog),
      listVenues: () => communication.catalog.listVenues(),
      listTables: (venueId: string) => communication.catalog.listTables(venueId),
    },
    matches: communication.matches,
    stats: communication.stats,
  }
}

/**
 * Lazy singleton — avoids creating multiple independent dependency trees
 * when several modules import the composition root at the top level.
 */
let _playerInstance: PlayerDependencies | undefined
export function getPlayerDependencies(): PlayerDependencies {
  return (_playerInstance ??= createPlayerDependencies())
}
