import { communicationRoutes, apiUrl } from '@shared/communication/config'
import { createRetryingFetch } from '@shared/communication/retryingFetch'
import type { Match, MatchRestClient } from '@domain/simulator'
import { parseMatch } from '@infrastructure/realtime/MqttEventParser'

export function createMatchRestClient(
  baseUrl: string,
  fetcher: typeof fetch = createRetryingFetch(),
): MatchRestClient {
  return {
    async getCurrentMatch(tableId): Promise<Match | undefined> {
      const response = await fetcher(
        apiUrl(baseUrl, `${communicationRoutes.match}${communicationRoutes.matchCurrent(tableId)}`),
        { headers: { Accept: 'application/json' } },
      )
      if (response.status === 404) return undefined
      if (!response.ok) {
        throw new Error(`Unable to load current match (HTTP ${response.status})`)
      }
      return parseMatch(await response.json())
    },
  }
}
