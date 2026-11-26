export interface Totals {
  readonly matches: number
  readonly wins: number
  readonly losses: number
  readonly draws: number
  readonly pointsFor: number
  readonly pointsAgainst: number
}

export interface LeaderboardEntry extends Totals {
  readonly rank: number
  readonly participantId: string
  readonly displayName?: string
}

export interface GameTypeTotals extends Totals {
  readonly gameTypeId: string
}

export interface ParticipantSummary extends Totals {
  readonly participantId: string
  readonly displayName?: string
  readonly lastMatchAt?: string
  readonly byGameType: readonly GameTypeTotals[]
}

export interface VenueGameTypeSummary {
  readonly gameTypeId: string
  readonly matches: number
  readonly totalPoints: number
  readonly averageDurationSeconds: number
}

export interface VenueStatsSummary {
  readonly venueId: string
  readonly matches: number
  readonly totalPoints: number
  readonly averageDurationSeconds: number
  readonly lastMatchAt?: string
  readonly byGameType: readonly VenueGameTypeSummary[]
}

export interface StatsRepository {
  getLeaderboard(params?: { venueId?: string; limit?: number }): Promise<readonly LeaderboardEntry[]>
  getParticipantSummary(participantId: string): Promise<ParticipantSummary | undefined>
  getVenueSummary(venueId: string): Promise<VenueStatsSummary | undefined>
}
