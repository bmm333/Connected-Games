import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'
import { createAuthDependencies } from '@surfaces/shared/auth-dependencies'
import { createCommunicationServices } from '@infrastructure/communication/createCommunicationServices'
import type { MatchRestClient, MatchSseClient } from '@domain/simulator'

type KioskMatchService = MatchRestClient & MatchSseClient

export interface KioskDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  auth: ReturnType<typeof createAuthDependencies>
  matches: KioskMatchService
}

export function createKioskDependencies(): KioskDependencies {
  const platformStore = createPlatformKeyValueStore()
  const communication = createCommunicationServices()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_KIOSK_V1',
    ),
    auth: createAuthDependencies(),
    matches: communication.matches,
  }
}
