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

export interface StatsFilters {
  readonly venueId?: string
  readonly gameTypeId?: string
  readonly from?: string
  readonly to?: string
}

export interface StatsPagination {
  readonly limit?: number
  readonly offset?: number
}

export interface ParticipantMatch {
  readonly matchId: string
  readonly venueId: string
  readonly tableId: string
  readonly gameTypeId: string
  readonly tournamentMatchId?: string
  readonly endedAt: string
  readonly durationSeconds: number
  readonly side: 'RED' | 'BLUE'
  readonly result: 'WIN' | 'LOSS' | 'DRAW'
  readonly pointsFor: number
  readonly pointsAgainst: number
  readonly opponentParticipantId?: string
  readonly opponentDisplayName?: string
}

export interface MatchResult {
  readonly matchId: string
  readonly venueId: string
  readonly tableId: string
  readonly gameTypeId: string
  readonly tournamentMatchId?: string
  readonly mode: 'INDIVIDUAL' | 'TEAM'
  readonly pointsToWin: number
  readonly reason: 'SCORE_REACHED' | 'TIME_EXPIRED'
  readonly winnerSide?: 'RED' | 'BLUE'
  readonly startedAt: string
  readonly endedAt: string
  readonly durationSeconds: number
  readonly sides: readonly {
    readonly side: 'RED' | 'BLUE'
    readonly participantId?: string
    readonly displayName?: string
    readonly score: number
  }[]
  readonly points: readonly {
    readonly seq: number
    readonly side: 'RED' | 'BLUE'
    readonly occurredAt: string
    readonly redScore: number
    readonly blueScore: number
  }[]
  readonly leadChanges: number
  readonly longestStreak?: {
    readonly side: 'RED' | 'BLUE'
    readonly length: number
  }
}

export interface StatsRepository {
  getLeaderboard(
    params?: StatsFilters & Pick<StatsPagination, 'limit'>,
  ): Promise<readonly LeaderboardEntry[]>
  getParticipantSummary(
    participantId: string,
    params?: StatsFilters,
  ): Promise<ParticipantSummary | undefined>
  getParticipantMatches(
    participantId: string,
    params?: StatsFilters & StatsPagination,
  ): Promise<readonly ParticipantMatch[]>
  getMatchResult(matchId: string): Promise<MatchResult | undefined>
  getVenueSummary(
    venueId: string,
    params?: Omit<StatsFilters, 'venueId'>,
  ): Promise<VenueStatsSummary | undefined>
}
