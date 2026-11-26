import type {
  LeaderboardEntry,
  ParticipantSummary,
  StatsRepository,
  VenueStatsSummary,
} from '@domain/stats/StatsRepository'
import { apiUrl, communicationConfig, communicationRoutes } from '@shared/communication/config'
import { createRetryingFetch } from '@shared/communication/retryingFetch'

export function createStatsRepository(
  baseUrl = apiUrl(communicationConfig.apiBaseUrl, communicationRoutes.stats),
  fetcher: typeof fetch = createRetryingFetch(),
): StatsRepository {
  return {
    async getLeaderboard(params) {
      const search = new URLSearchParams()
      if (params?.venueId) search.set('venueId', params.venueId)
      if (params?.limit !== undefined) search.set('limit', String(params.limit))
      const qs = search.toString()
      const url = apiUrl(baseUrl, `/leaderboard${qs ? `?${qs}` : ''}`)
      const response = await fetcher(url, { headers: { Accept: 'application/json' } })
      if (!response.ok) {
        throw new Error(`Unable to load leaderboard (HTTP ${response.status})`)
      }
      return (await response.json()) as LeaderboardEntry[]
    },

    async getParticipantSummary(participantId) {
      const url = apiUrl(baseUrl, `/participants/${encodeURIComponent(participantId)}/summary`)
      const response = await fetcher(url, { headers: { Accept: 'application/json' } })
      if (response.status === 404) return undefined
      if (!response.ok) {
        throw new Error(`Unable to load participant summary (HTTP ${response.status})`)
      }
      return (await response.json()) as ParticipantSummary
    },

    async getVenueSummary(venueId) {
      const url = apiUrl(baseUrl, `/venues/${encodeURIComponent(venueId)}/summary`)
      const response = await fetcher(url, { headers: { Accept: 'application/json' } })
      if (response.status === 404) return undefined
      if (!response.ok) {
        throw new Error(`Unable to load venue summary (HTTP ${response.status})`)
      }
      return (await response.json()) as VenueStatsSummary
    },
  }
}
