import type { SimulatorCatalogRepository } from '@domain/catalog/CatalogRepository'
import type { GameType, Table, TableLookup, Venue, VenueSummary } from '@domain/catalog/TableLookup'
import type { StatsRepository } from '@domain/stats/StatsRepository'
import type {
  Match,
  MatchCompleted,
  MatchEventEnvelope,
  MatchEventPublisher,
  MatchSide,
  MqttConnectionStatus,
  CommunicationServices,
} from '@domain/simulator'
import { communicationRoutes } from '@shared/communication/config'

const venue: Venue = {
  id: '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10',
  name: 'Arena Demo',
  city: 'Milano',
  type: 'PUBLIC',
  address: 'Via Demo 1',
  organizationId: '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f11',
}

const gameType: GameType = {
  id: '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f12',
  name: 'Calciobalilla',
  mode: 'INDIVIDUAL',
  participantsPerSide: 1,
  pointsToWin: 10,
}

const table: Table = {
  id: '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f13',
  venueId: venue.id,
  name: 'Tavolo Demo',
  code: 'DEMO01',
  gameTypeId: gameType.id,
  status: 'FREE',
}

const demoMatch: Match = {
  id: '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f14',
  tableId: table.id,
  venueId: venue.id,
  gameTypeId: gameType.id,
  status: 'IN_PROGRESS',
  mode: gameType.mode,
  pointsToWin: gameType.pointsToWin,
  sides: [
    { side: 'RED', displayName: 'Rossi', score: 7 },
    { side: 'BLUE', displayName: 'Blu', score: 5 },
  ],
  startedAt: '2026-11-25T18:00:00.000Z',
}

function createMockCatalog(): SimulatorCatalogRepository {
  const venueSummary: VenueSummary = {
    id: venue.id,
    name: venue.name,
    city: venue.city,
    type: venue.type,
  }
  const lookup: TableLookup = { table, venue: venueSummary, gameType }
  return {
    async resolveTableByCode(code) {
      if (code.trim().toUpperCase() !== table.code) {
        throw new Error('Table code was not found in the mock catalog')
      }
      return lookup
    },
    async listVenues() {
      return [venueSummary]
    },
    async getVenue(venueId) {
      if (venueId !== venue.id) throw new Error('Venue was not found in the mock catalog')
      return venue
    },
    async listTables(venueId) {
      if (venueId !== venue.id) throw new Error('Venue was not found in the mock catalog')
      return [table]
    },
    async listGameTypes() {
      return [gameType]
    },
  }
}

function createMockMatchServices(): CommunicationServices['matches'] & {
  applyEvent(event: MatchEventEnvelope): void
  subscribeCompleted(listener: (match: MatchCompleted) => void): () => void
} {
  const matches = new Map<string, Match>([[demoMatch.tableId, demoMatch]])
  const listeners = new Map<string, Set<(match: Match) => void>>()
  const completedListeners = new Set<(match: MatchCompleted) => void>()
  const pointsByTable = new Map<string, MatchCompleted['points'][number][]>()

  function emit(tableId: string, match: Match) {
    matches.set(tableId, match)
    listeners.get(tableId)?.forEach((listener) => listener(match))
  }

  return {
    async getCurrentMatch(tableId) {
      return matches.get(tableId)
    },
    subscribe(tableId, onMatch, onStatus) {
      const callbacks = listeners.get(tableId) ?? new Set<(match: Match) => void>()
      callbacks.add(onMatch)
      listeners.set(tableId, callbacks)
      onStatus('connecting')
      queueMicrotask(() => {
        if (!callbacks.has(onMatch)) return
        onStatus('connected')
        const current = matches.get(tableId)
        if (current) onMatch(current)
      })
      return () => {
        callbacks.delete(onMatch)
        if (callbacks.size === 0) listeners.delete(tableId)
        onStatus('disconnected')
      }
    },
    applyEvent(event) {
      const previous = matches.get(event.tableId)
      if (event.type === 'MatchStarted') {
        pointsByTable.set(event.tableId, [])
        const sides = Array.isArray(event.payload.sides)
          ? event.payload.sides.map((side) => {
            const sideData = side as { side: MatchSide; displayName?: string; participantId?: string }
            return { ...sideData, score: 0 }
          })
          : []
        emit(event.tableId, {
          id: event.matchId,
          tableId: event.tableId,
          venueId: event.venueId,
          gameTypeId: String(event.payload.gameTypeId),
          ...(typeof event.payload.tournamentMatchId === 'string'
            ? { tournamentMatchId: event.payload.tournamentMatchId }
            : {}),
          status: 'IN_PROGRESS',
          mode: event.payload.mode === 'TEAM' ? 'TEAM' : 'INDIVIDUAL',
          pointsToWin: Number(event.payload.pointsToWin),
          sides,
          startedAt: event.occurredAt,
        })
      } else if (previous && event.type === 'GoalScored') {
        const scoredSide = event.payload.side
        if (scoredSide !== 'RED' && scoredSide !== 'BLUE') return
        const sides = previous.sides.map((side) => ({
          ...side,
          score: side.score + (side.side === scoredSide ? 1 : 0),
        }))
        const points = pointsByTable.get(event.tableId) ?? []
        points.push({ seq: points.length + 1, side: scoredSide, occurredAt: event.occurredAt })
        pointsByTable.set(event.tableId, points)
        emit(event.tableId, { ...previous, sides })
      } else if (previous && event.type === 'MatchEnded') {
        const reason = event.payload.reason
        const winnerSide = event.payload.winnerSide
        const finalMatch: Match = {
          ...previous,
          status: reason === 'CANCELLED' ? 'CANCELLED' : 'COMPLETED',
          ...(winnerSide === 'RED' || winnerSide === 'BLUE' ? { winnerSide } : {}),
          endedAt: event.occurredAt,
        }
        emit(event.tableId, finalMatch)
        const completed: MatchCompleted = {
          eventId: event.eventId,
          tenantId: event.tenantId,
          venueId: event.venueId,
          tableId: event.tableId,
          matchId: event.matchId,
          gameTypeId: previous.gameTypeId,
          ...(previous.tournamentMatchId
            ? { tournamentMatchId: previous.tournamentMatchId }
            : {}),
          mode: previous.mode,
          pointsToWin: previous.pointsToWin,
          startedAt: previous.startedAt,
          endedAt: event.occurredAt,
          reason: reason === 'SCORE_REACHED' || reason === 'TIME_EXPIRED'
            ? reason
            : 'CANCELLED',
          ...(winnerSide === 'RED' || winnerSide === 'BLUE' ? { winnerSide } : {}),
          sides: finalMatch.sides,
          points: [...(pointsByTable.get(event.tableId) ?? [])],
        }
        completedListeners.forEach((listener) => listener(completed))
      }
    },
    subscribeCompleted(listener) {
      completedListeners.add(listener)
      return () => completedListeners.delete(listener)
    },
  }
}

function createMockMatchEvents(
  matches: ReturnType<typeof createMockMatchServices>,
): MatchEventPublisher {
  let connectionStatus: MqttConnectionStatus = 'offline'
  const connectionListeners = new Set<(status: MqttConnectionStatus) => void>()
  const ackListeners = new Map<string, Set<(payload: unknown) => void>>()
  const emitConnection = (status: MqttConnectionStatus) => {
    connectionStatus = status
    connectionListeners.forEach((listener) => listener(status))
  }

  return {
    async connect() {
      emitConnection('connecting')
      await Promise.resolve()
      emitConnection('connected')
    },
    async publish(topic, event) {
      const expectedTopic = communicationRoutes.mqttMatchEvents(
        event.venueId,
        event.tableId,
        event.matchId,
      )
      if (topic !== expectedTopic) throw new Error('Mock MQTT topic does not match the event identifiers')
      matches.applyEvent(event)
      const listeners = ackListeners.get(event.venueId)
      listeners?.forEach((listener) => listener({
        venueId: event.venueId,
        acknowledged: [event.eventId],
        processedAt: new Date().toISOString(),
      }))
    },
    async subscribeAck(venueId, onAck) {
      const listeners = ackListeners.get(venueId) ?? new Set<(payload: unknown) => void>()
      listeners.add(onAck)
      ackListeners.set(venueId, listeners)
      return async () => {
        listeners.delete(onAck)
        if (listeners.size === 0) ackListeners.delete(venueId)
      }
    },
    async subscribeMatchCompleted(onMatch) {
      const unsubscribe = matches.subscribeCompleted(onMatch)
      return async () => unsubscribe()
    },
    subscribeConnectionState(onState) {
      connectionListeners.add(onState)
      onState(connectionStatus)
      return () => connectionListeners.delete(onState)
    },
  }
}

function createMockStats(): StatsRepository {
  return {
    async getLeaderboard() {
      return []
    },
    async getParticipantSummary() {
      return undefined
    },
    async getVenueSummary() {
      return undefined
    },
  }
}

export function createMockCommunicationServices(): CommunicationServices {
  const matches = createMockMatchServices()
  return {
    catalog: createMockCatalog(),
    matches,
    matchEvents: createMockMatchEvents(matches),
    stats: createMockStats(),
  }
}
