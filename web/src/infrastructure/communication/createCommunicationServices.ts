import { createUuidV7 } from '@domain/simulator'
import { createCatalogRepository } from '@infrastructure/api/CatalogRepository'
import { createMatchRestClient } from '@infrastructure/api/MatchRestClient'
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
): CommunicationServices {
  if (config.useMocks) return createMockCommunicationServices()

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
      ...createMatchRestClient(config.apiBaseUrl),
      ...createMatchSseClient(config.sseUrl ?? config.apiBaseUrl),
    },
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
