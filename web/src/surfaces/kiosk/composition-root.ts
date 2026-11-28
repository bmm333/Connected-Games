import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'
import { createAuthDependencies } from '@surfaces/shared/auth-dependencies'
import { createCommunicationServices } from '@infrastructure/communication/createCommunicationServices'
import { createSessionAuthenticatedFetch } from '@infrastructure/communication/createSessionAuthenticatedFetch'
import type { MatchRestClient, MatchSseClient } from '@domain/simulator'
import type { StatsRepository } from '@domain/stats/StatsRepository'

type KioskMatchService = MatchRestClient & MatchSseClient

export interface KioskDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  auth: ReturnType<typeof createAuthDependencies>
  matches: KioskMatchService
  stats: StatsRepository
}

export function createKioskDependencies(): KioskDependencies {
  const platformStore = createPlatformKeyValueStore()
  const communication = createCommunicationServices(
    undefined,
    createSessionAuthenticatedFetch(platformStore),
  )

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_KIOSK_V1',
    ),
    auth: createAuthDependencies(platformStore),
    matches: communication.matches,
    stats: communication.stats,
  }
}
