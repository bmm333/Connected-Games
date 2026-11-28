import type {
  LeaderboardEntry,
  MatchResult,
  ParticipantMatch,
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
  function queryString(
    params: {
      venueId?: string
      gameTypeId?: string
      from?: string
      to?: string
      limit?: number
      offset?: number
    } = {},
  ): string {
    const search = new URLSearchParams()
    for (const key of ['venueId', 'gameTypeId', 'from', 'to', 'limit', 'offset'] as const) {
      const value = params[key]
      if (value !== undefined) search.set(key, String(value))
    }
    const query = search.toString()
    return query ? `?${query}` : ''
  }

  async function get<T>(path: string, operation: string): Promise<T>
  async function get<T>(
    path: string,
    operation: string,
    notFoundIsEmpty: true,
  ): Promise<T | undefined>
  async function get<T>(
    path: string,
    operation: string,
    notFoundIsEmpty = false,
  ): Promise<T | undefined> {
    const response = await fetcher(apiUrl(baseUrl, path), {
      headers: { Accept: 'application/json' },
    })
    if (notFoundIsEmpty && response.status === 404) return undefined
    if (!response.ok) {
      throw new Error(`Unable to load ${operation} (HTTP ${response.status})`)
    }
    return (await response.json()) as T
  }

  return {
    async getLeaderboard(params) {
      return get<LeaderboardEntry[]>(
        `/leaderboard${queryString(params)}`,
        'leaderboard',
      )
    },

    async getParticipantSummary(participantId, params) {
      return get<ParticipantSummary>(
        `/participants/${encodeURIComponent(participantId)}/summary${queryString(params)}`,
        'participant summary',
        true,
      )
    },

    async getParticipantMatches(participantId, params) {
      return get<ParticipantMatch[]>(
        `/participants/${encodeURIComponent(participantId)}/matches${queryString(params)}`,
        'participant matches',
      )
    },

    async getMatchResult(matchId) {
      return get<MatchResult>(`/matches/${encodeURIComponent(matchId)}`, 'match result', true)
    },

    async getVenueSummary(venueId, params) {
      return get<VenueStatsSummary>(
        `/venues/${encodeURIComponent(venueId)}/summary${queryString(params)}`,
        'venue summary',
        true,
      )
    },
  }
}
