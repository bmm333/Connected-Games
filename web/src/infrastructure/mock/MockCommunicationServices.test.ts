import { describe, expect, it } from 'vitest'
import type { CommunicationServices, MatchEventEnvelope } from '@domain/simulator'
import { createCommunicationServices } from '@infrastructure/communication/createCommunicationServices'
import { communicationRoutes } from '@shared/communication/config'
import { createMockCommunicationServices } from './MockCommunicationServices'

const UUID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'
const COMPLETED_EVENT_ID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f23'

describe('communication service implementations', () => {
  it('exposes the same service interfaces in mock and real mode', () => {
    const mock: CommunicationServices = createCommunicationServices({
      useMocks: true,
      apiBaseUrl: '',
    })
    const real: CommunicationServices = createCommunicationServices({
      useMocks: false,
      apiBaseUrl: 'https://api.example.test',
      sseUrl: 'https://stream.example.test',
      mqttUrl: 'wss://broker.example.test/mqtt',
    })

    for (const services of [mock, real]) {
      expect(services.catalog.resolveTableByCode).toBeTypeOf('function')
      expect(services.matches.getCurrentMatch).toBeTypeOf('function')
      expect(services.matches.subscribe).toBeTypeOf('function')
      expect(services.matchEvents.publish).toBeTypeOf('function')
      expect(services.matchEvents.subscribeMatchCompleted).toBeTypeOf('function')
    }
  })

  it('shares mock MQTT match events with the mock Match stream and completion subscribers', async () => {
    const services = createMockCommunicationServices()
    const venues = await services.catalog.listVenues()
    const tables = await services.catalog.listTables(venues[0].id)
    const table = tables[0]
    const loaded: string[] = []
    let completedMatchId: string | undefined
    let completedEventId: string | undefined
    const unsubscribeStream = services.matches.subscribe(table.id, (match) => {
      loaded.push(match.status)
    }, () => undefined)
    const unsubscribeCompleted = await services.matchEvents.subscribeMatchCompleted((match) => {
      completedMatchId = match.matchId
      completedEventId = match.eventId
    })

    await services.matchEvents.connect()
    const started = createEvent(table.venueId, table.id, UUID, 1, 'MatchStarted', {
      gameTypeId: table.gameTypeId,
      mode: 'INDIVIDUAL',
      pointsToWin: 1,
      sides: [{ side: 'RED' }, { side: 'BLUE' }],
    })
    await services.matchEvents.publish(
      communicationRoutes.mqttMatchEvents(started.venueId, started.tableId, started.matchId),
      started,
    )
    const goal = createEvent(table.venueId, table.id, UUID, 2, 'GoalScored', { side: 'RED' })
    await services.matchEvents.publish(
      communicationRoutes.mqttMatchEvents(goal.venueId, goal.tableId, goal.matchId),
      goal,
    )
    const ended = createEvent(table.venueId, table.id, UUID, 3, 'MatchEnded', {
      reason: 'SCORE_REACHED',
      finalScore: [{ side: 'RED', score: 1 }, { side: 'BLUE', score: 0 }],
      winnerSide: 'RED',
    })
    await services.matchEvents.publish(
      communicationRoutes.mqttMatchEvents(ended.venueId, ended.tableId, ended.matchId),
      ended,
    )

    expect(await services.matches.getCurrentMatch(table.id)).toMatchObject({
      id: UUID,
      tableId: table.id,
      status: 'COMPLETED',
      winnerSide: 'RED',
    })
    expect(completedMatchId).toBe(UUID)
    expect(completedEventId).toBe(COMPLETED_EVENT_ID)
    expect(loaded).toContain('IN_PROGRESS')
    expect(loaded).toContain('COMPLETED')
    unsubscribeStream()
    await unsubscribeCompleted()
  })
})

function createEvent(
  venueId: string,
  tableId: string,
  matchId: string,
  seq: number,
  type: MatchEventEnvelope['type'],
  payload: Readonly<Record<string, unknown>>,
): MatchEventEnvelope {
  return {
    eventId: seq === 3 ? COMPLETED_EVENT_ID : UUID.slice(0, -2) + String(20 + seq),
    tenantId: UUID,
    venueId,
    tableId,
    matchId,
    seq,
    type,
    occurredAt: '2026-11-25T18:00:00.000Z',
    source: 'EMULATED',
    payload,
  }
}
