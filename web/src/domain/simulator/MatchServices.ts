import type { SimulatorCatalogRepository } from '../catalog/CatalogRepository'
import type { StatsRepository } from '../stats/StatsRepository'
import type { Match, MatchEventPublisher } from './MatchEvent'

export type MatchStreamStatus = 'connecting' | 'connected' | 'disconnected' | 'retrying' | 'error'

export interface MatchRestClient {
  getCurrentMatch(tableId: string): Promise<Match | undefined>
}

export interface MatchSseClient {
  subscribe(
    tableId: string,
    onMatch: (match: Match) => void,
    onStatus: (status: MatchStreamStatus) => void,
  ): () => void
}

export interface CommunicationServices {
  readonly catalog: SimulatorCatalogRepository
  readonly matches: MatchRestClient & MatchSseClient
  readonly matchEvents: MatchEventPublisher
  readonly stats: StatsRepository
}

