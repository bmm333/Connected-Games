import type { GameType, Table, Venue } from '@domain/catalog/TableLookup'

export type MatchSide = 'RED' | 'BLUE'
export type MatchEventType = 'MatchStarted' | 'GoalScored' | 'MatchEnded'
export type MatchEndReason = 'SCORE_REACHED' | 'TIME_EXPIRED' | 'CANCELLED'
export type MqttConnectionStatus = 'offline' | 'connecting' | 'connected' | 'reconnecting' | 'error'
export type MatchStatus = 'WAITING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface MatchStarted {
  readonly gameTypeId: string
  readonly tournamentMatchId?: string
  readonly mode: 'INDIVIDUAL' | 'TEAM'
  readonly pointsToWin: number
  readonly sides: readonly {
    readonly side: MatchSide
    readonly participantId?: string
    readonly displayName?: string
  }[]
}

export type MatchStartedPayload = MatchStarted

export interface MatchCompleted {
  readonly eventId: string
  readonly tenantId: string
  readonly venueId: string
  readonly tableId: string
  readonly matchId: string
  readonly gameTypeId: string
  readonly tournamentMatchId?: string
  readonly mode: 'INDIVIDUAL' | 'TEAM'
  readonly pointsToWin: number
  readonly startedAt: string
  readonly endedAt: string
  readonly reason: MatchEndReason
  readonly winnerSide?: MatchSide
  readonly points: readonly {
    readonly seq: number
    readonly side: MatchSide
    readonly occurredAt: string
  }[]
  readonly sides: readonly {
    readonly side: MatchSide
    readonly participantId?: string
    readonly displayName?: string
    readonly score: number
  }[]
}

export interface Match {
  readonly id: string
  readonly tableId: string
  readonly venueId: string
  readonly gameTypeId: string
  readonly tournamentMatchId?: string
  readonly status: MatchStatus
  readonly mode: 'INDIVIDUAL' | 'TEAM'
  readonly pointsToWin: number
  readonly sides: readonly {
    readonly side: MatchSide
    readonly participantId?: string
    readonly displayName?: string
    readonly score: number
  }[]
  readonly winnerSide?: MatchSide
  readonly startedAt: string
  readonly endedAt?: string
}

export interface MatchEventEnvelope {
  readonly eventId: string
  readonly tenantId: string
  readonly venueId: string
  readonly tableId: string
  readonly matchId: string
  readonly seq: number
  readonly type: MatchEventType
  readonly occurredAt: string
  readonly source: 'EMULATED'
  readonly payload: Readonly<Record<string, unknown>>
}

export interface SimulatorTable {
  readonly table: Table
  readonly venue: Venue
  readonly gameType: GameType
}

export interface MatchEventPublisher {
  connect(): Promise<void>
  publish(topic: string, event: MatchEventEnvelope): Promise<void>
  subscribeAck(venueId: string, onAck: (payload: unknown) => void): Promise<() => Promise<void>>
  subscribeMatchCompleted(onMatch: (match: MatchCompleted) => void): Promise<() => Promise<void>>
  subscribeConnectionState(onState: (status: MqttConnectionStatus) => void): () => void
}
