import { describe, expect, it, vi } from 'vitest'
import type { SimulatorCatalogRepository } from '@domain/catalog/CatalogRepository'
import type { MatchEventEnvelope, MatchEventPublisher, SimulatorTable } from '@domain/simulator'
import { createMatchSimulatorUseCase } from './MatchSimulatorUseCase'

const table: SimulatorTable = {
  table: {
    id: 'table-1',
    venueId: 'venue-1',
    name: 'Table 1',
    code: 'table-1',
    gameTypeId: 'game-1',
    status: 'FREE',
  },
  venue: { id: 'venue-1', name: 'Venue', city: 'Rome', type: 'PUBLIC' },
  gameType: {
    id: 'game-1',
    name: 'Foosball',
    mode: 'INDIVIDUAL',
    participantsPerSide: 1,
    pointsToWin: 2,
  },
}

function createSimulator() {
  const published: MatchEventEnvelope[] = []
  const catalog: SimulatorCatalogRepository = {
    resolveTableByCode: vi.fn(),
    listVenues: vi.fn().mockResolvedValue([table.venue]),
    listTables: vi.fn().mockResolvedValue([table.table]),
    listGameTypes: vi.fn().mockResolvedValue([table.gameType]),
  }
  const publisher: MatchEventPublisher = {
    publish: vi.fn(async (_topic, event) => {
      published.push(event)
    }),
    subscribeAck: vi.fn(),
  }
  return {
    simulator: createMatchSimulatorUseCase(catalog, publisher),
    published,
  }
}

describe('MatchSimulatorUseCase', () => {
  it('ends automatically at pointsToWin and rejects further goals', async () => {
    const { simulator, published } = createSimulator()

    const started = await simulator.startMatch(table)
    await simulator.scoreGoal(table.table.id, 'RED')
    const finalEvents = await simulator.scoreGoal(table.table.id, 'RED')

    expect(finalEvents.map((event) => event.type)).toEqual(['GoalScored', 'MatchEnded'])
    expect(finalEvents[1]).toMatchObject({
      type: 'MatchEnded',
      payload: {
        reason: 'SCORE_REACHED',
        winnerSide: 'RED',
      },
    })
    expect(started.eventId).toMatch(UUID_V7_PATTERN)
    expect(started.matchId).toMatch(UUID_V7_PATTERN)
    await expect(simulator.scoreGoal(table.table.id, 'BLUE'))
      .rejects.toThrow('No active match for this table')
    expect(published.map((event) => event.type)).toEqual([
      'MatchStarted',
      'GoalScored',
      'GoalScored',
      'MatchEnded',
    ])
  })

  it('does not include a winner for a cancelled match', async () => {
    const { simulator } = createSimulator()
    await simulator.startMatch(table)
    await simulator.scoreGoal(table.table.id, 'RED')

    const ended = await simulator.endMatch(table.table.id, 'CANCELLED')

    expect(ended.payload).toEqual({
      reason: 'CANCELLED',
      finalScore: [
        { side: 'RED', score: 1 },
        { side: 'BLUE', score: 0 },
      ],
    })
  })
})

const UUID_V7_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/
