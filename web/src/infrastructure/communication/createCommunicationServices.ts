import { createUuidV7 } from '@domain/simulator'
import { createCatalogRepository } from '@infrastructure/api/CatalogRepository'
import { createMatchRestClient } from '@infrastructure/api/MatchRestClient'
import { createStatsRepository } from '@infrastructure/api/StatsRepository'
import { createMockCommunicationServices } from '@infrastructure/mock/MockCommunicationServices'
import { createMqttClientService } from '@infrastructure/realtime/MqttClientService'
import { createMqttMatchEventPublisher } from '@infrastructure/realtime/MqttMatchEventPublisher'
import { createMatchSseClient } from '@infrastructure/realtime/MatchSseClient'
import {
  apiUrl,
  communicationConfig,
  communicationRoutes,
  type CommunicationConfig,
} from '@shared/communication/config'
import type { CommunicationServices } from '@domain/simulator'

export function createCommunicationServices(
  config: CommunicationConfig = communicationConfig,
  authenticatedFetch?: typeof fetch,
): CommunicationServices {
  if (config.useMocks) return createMockCommunicationServices()
  if (!authenticatedFetch) {
    throw new Error('An authenticated fetch implementation is required in real mode')
  }

  const mqttUrl = config.mqttUrl
  const mqttClient = createMqttClientService()
  const matchEvents = createMqttMatchEventPublisher(mqttClient, {
    url: mqttUrl ?? '',
    clientId: `connected-games-${createUuidV7()}`,
  })

  return {
    catalog: createCatalogRepository(
      apiUrl(config.apiBaseUrl, communicationRoutes.catalog),
    ),
    matches: {
      ...createMatchRestClient(config.apiBaseUrl, authenticatedFetch),
      ...createMatchSseClient(config.sseUrl ?? config.apiBaseUrl, authenticatedFetch),
    },
    stats: createStatsRepository(
      apiUrl(config.apiBaseUrl, communicationRoutes.stats),
      authenticatedFetch,
    ),
    matchEvents: mqttUrl
      ? matchEvents
      : {
        ...matchEvents,
        async connect() {
          throw new Error('VITE_MQTT_URL is required when VITE_USE_MOCKS=false')
        },
      },
  }
}
