import type { GameType, Table, Venue } from '@domain/catalog/TableLookup'

export type MatchSide = 'RED' | 'BLUE'
export type MatchEventType = 'MatchStarted' | 'GoalScored' | 'MatchEnded'
export type MatchEndReason = 'SCORE_REACHED' | 'TIME_EXPIRED' | 'CANCELLED'
export type MqttConnectionStatus = 'offline' | 'connecting' | 'connected' | 'reconnecting' | 'error'

export interface MatchStartedPayload {
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
  readonly sides: readonly {
    readonly side: MatchSide
    readonly participantId?: string
    readonly displayName?: string
    readonly score: number
  }[]
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
  subscribeConnectionState(onState: (status: MqttConnectionStatus) => void): () => void
}
