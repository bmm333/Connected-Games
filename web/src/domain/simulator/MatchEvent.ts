import type { GameType, Table, Venue } from '@domain/catalog/TableLookup'

export type MatchSide = 'RED' | 'BLUE'
export type MatchEventType = 'MatchStarted' | 'GoalScored' | 'MatchEnded'
export type MatchEndReason = 'SCORE_REACHED' | 'TIME_EXPIRED' | 'CANCELLED'

export interface MatchEventEnvelope {
  readonly eventId: string
  readonly tenantId: string
  readonly venueId: string
  readonly gameId: string
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
}
