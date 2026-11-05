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
  venue: {
    id: 'venue-1',
    name: 'Venue',
    city: 'Rome',
    type: 'PUBLIC',
    address: 'Via Roma 1',
    organizationId: 'org-1',
  },
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
  const topics: string[] = []
  const catalog: SimulatorCatalogRepository = {
    resolveTableByCode: vi.fn(),
    listVenues: vi.fn().mockResolvedValue([table.venue]),
    getVenue: vi.fn().mockResolvedValue(table.venue),
    listTables: vi.fn().mockResolvedValue([
      table.table,
      { ...table.table, id: 'table-2', gameTypeId: 'unknown-game' },
    ]),
    listGameTypes: vi.fn().mockResolvedValue([table.gameType]),
  }
  const publisher: MatchEventPublisher = {
    connect: vi.fn(),
    publish: vi.fn(async (topic, event) => {
      topics.push(topic)
      published.push(event)
    }),
    subscribeAck: vi.fn(),
  }
  return {
    simulator: createMatchSimulatorUseCase(catalog, publisher),
    published,
    topics,
  }
}

describe('MatchSimulatorUseCase', () => {
  it('lists the tables of a venue with their game type', async () => {
    const { simulator } = createSimulator()

    const tables = await simulator.listTables('venue-1')

    expect(tables).toEqual([table])
  })

  it('publishes on the match topic with seq from 1 and the venue organization as tenant', async () => {
    const { simulator, published, topics } = createSimulator()

    const started = await simulator.startMatch(table)
    await simulator.scoreGoal(table.table.id, 'BLUE')

    expect(topics).toEqual([
      `venues/venue-1/games/table-1/matches/${started.matchId}/events`,
      `venues/venue-1/games/table-1/matches/${started.matchId}/events`,
    ])
    expect(published.map((event) => event.seq)).toEqual([1, 2])
    expect(started).toMatchObject({
      tenantId: 'org-1',
      venueId: 'venue-1',
      gameId: 'table-1',
      source: 'EMULATED',
      payload: { mode: 'INDIVIDUAL', pointsToWin: 2 },
    })
  })

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
