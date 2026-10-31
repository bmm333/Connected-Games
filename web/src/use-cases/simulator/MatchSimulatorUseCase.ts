import type { SimulatorCatalogRepository } from '@domain/catalog/CatalogRepository'
import type {
  MatchEndReason,
  MatchEventEnvelope,
  MatchEventPublisher,
  MatchSide,
  SimulatorTable,
} from '@domain/simulator'
import { createUuidV7 } from '@domain/simulator'

interface MatchState {
  readonly table: SimulatorTable
  readonly matchId: string
  seq: number
  redScore: number
  blueScore: number
}

export interface MatchSimulatorUseCase {
  listVenues(): ReturnType<SimulatorCatalogRepository['listVenues']>
  listTables(venueId: string): Promise<readonly SimulatorTable[]>
  startMatch(table: SimulatorTable): Promise<MatchEventEnvelope>
  scoreGoal(tableId: string, side: MatchSide): Promise<readonly MatchEventEnvelope[]>
  endMatch(tableId: string, reason?: MatchEndReason): Promise<MatchEventEnvelope>
  subscribeAcks(venueId: string, onAck: (payload: unknown) => void): Promise<() => Promise<void>>
}

const topicFor = (table: SimulatorTable, matchId: string) =>
  `venues/${encodeURIComponent(table.venue.id)}/games/${encodeURIComponent(table.table.id)}/matches/${encodeURIComponent(matchId)}/events`

export function createMatchSimulatorUseCase(
  catalog: SimulatorCatalogRepository,
  publisher: MatchEventPublisher,
  tenantId = import.meta.env.VITE_TENANT_ID ?? 'local-tenant',
): MatchSimulatorUseCase {
  const matches = new Map<string, MatchState>()

  function createEvent(state: MatchState, type: MatchEventEnvelope['type'], payload: Readonly<Record<string, unknown>>) {
    state.seq += 1
    return {
      eventId: createUuidV7(),
      tenantId,
      venueId: state.table.venue.id,
      gameId: state.table.table.id,
      matchId: state.matchId,
      seq: state.seq,
      type,
      occurredAt: new Date().toISOString(),
      source: 'EMULATED' as const,
      payload,
    }
  }

  async function publish(state: MatchState, event: MatchEventEnvelope) {
    await publisher.publish(topicFor(state.table, state.matchId), event)
    return event
  }

  return {
    listVenues: () => catalog.listVenues(),
    async listTables(venueId) {
      const [venues, tables, gameTypes] = await Promise.all([
        catalog.listVenues(),
        catalog.listTables(venueId),
        catalog.listGameTypes(),
      ])
      const gameTypesById = new Map(gameTypes.map((gameType) => [gameType.id, gameType]))
      const venue = venues.find((item) => item.id === venueId)
      if (!venue) throw new Error(`Venue not found: ${venueId}`)
      return tables.flatMap((table) => {
        const gameType = gameTypesById.get(table.gameTypeId)
        return gameType ? [{ table, venue, gameType }] : []
      })
    },
    async startMatch(table) {
      const state: MatchState = {
        table,
        matchId: createUuidV7(),
        seq: 0,
        redScore: 0,
        blueScore: 0,
      }
      matches.set(table.table.id, state)
      return publish(state, createEvent(state, 'MatchStarted', {
        mode: table.gameType.mode,
        pointsToWin: table.gameType.pointsToWin,
        sides: [
          { side: 'RED', displayName: 'Red' },
          { side: 'BLUE', displayName: 'Blue' },
        ],
      }))
    },
    async scoreGoal(tableId, side) {
      const state = matches.get(tableId)
      if (!state) throw new Error('No active match for this table')
      if (side === 'RED') state.redScore += 1
      else state.blueScore += 1
      const events = [await publish(state, createEvent(state, 'GoalScored', { side }))]
      if (state.redScore >= state.table.gameType.pointsToWin ||
        state.blueScore >= state.table.gameType.pointsToWin) {
        events.push(await finishMatch(state, 'SCORE_REACHED'))
      }
      return events
    },
    async endMatch(tableId, reason = 'CANCELLED') {
      const state = matches.get(tableId)
      if (!state) throw new Error('No active match for this table')
      return finishMatch(state, reason)
    },
    subscribeAcks: (venueId, onAck) => publisher.subscribeAck(venueId, onAck),
  }

  async function finishMatch(state: MatchState, reason: MatchEndReason) {
      const winnerSide = state.redScore === state.blueScore
        ? undefined
        : state.redScore > state.blueScore ? 'RED' : 'BLUE'
      const event = createEvent(state, 'MatchEnded', {
        reason,
        finalScore: [
          { side: 'RED', score: state.redScore },
          { side: 'BLUE', score: state.blueScore },
        ],
        ...(reason !== 'CANCELLED' && winnerSide ? { winnerSide } : {}),
      })
      matches.delete(state.table.table.id)
      return publish(state, event)
  }
}
