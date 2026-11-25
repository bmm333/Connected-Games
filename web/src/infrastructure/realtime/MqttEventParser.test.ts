import { describe, expect, it } from 'vitest'
import {
  parseEventsAcknowledged,
  parseMatch,
  parseMatchCompleted,
  parseMatchStarted,
} from './MqttEventParser'

const UUID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'

describe('MqttEventParser', () => {
  it('parses MatchCompleted and preserves optional tournamentMatchId', () => {
    const payload = {
      eventId: UUID,
      tenantId: 'tenant-1',
      venueId: UUID,
      tableId: UUID,
      matchId: UUID,
      gameTypeId: UUID,
      tournamentMatchId: UUID,
      mode: 'TEAM',
      pointsToWin: 10,
      startedAt: '2026-11-22T10:00:00.000Z',
      endedAt: '2026-11-22T10:30:00.000Z',
      reason: 'SCORE_REACHED',
      winnerSide: 'RED',
      points: [{ seq: 1, side: 'RED', occurredAt: '2026-11-22T10:10:00.000Z' }],
      sides: [
        { side: 'RED', participantId: 'player-1', displayName: 'Red', score: 10 },
        { side: 'BLUE', displayName: 'Blue', score: 7 },
      ],
    }

    expect(parseMatchCompleted(JSON.stringify(payload))).toEqual(payload)
  })

  it('accepts MatchCompleted without tournamentMatchId', () => {
    const payload = {
      eventId: UUID,
      tenantId: 'tenant-1',
      venueId: UUID,
      tableId: UUID,
      matchId: UUID,
      gameTypeId: UUID,
      mode: 'INDIVIDUAL',
      pointsToWin: 5,
      startedAt: '2026-11-22T10:00:00.000Z',
      endedAt: '2026-11-22T10:30:00.000Z',
      reason: 'CANCELLED',
      points: [],
      sides: [
        { side: 'RED', score: 0 },
        { side: 'BLUE', score: 0 },
      ],
    }

    expect(parseMatchCompleted(payload).tournamentMatchId).toBeUndefined()
  })

  it('rejects malformed MatchCompleted payloads', () => {
    expect(() => parseMatchCompleted({ eventId: 'bad' })).toThrow('Invalid MatchCompleted payload')
  })

  it('parses MatchStarted envelopes and validates the contract fields', () => {
    expect(parseMatchStarted({
      eventId: UUID,
      tenantId: 'tenant-1',
      venueId: UUID,
      tableId: UUID,
      matchId: UUID,
      seq: 1,
      occurredAt: '2026-11-22T10:00:00.000Z',
      source: 'EMULATED',
      type: 'MatchStarted',
      payload: {
        gameTypeId: UUID,
        tournamentMatchId: UUID,
        mode: 'TEAM',
        pointsToWin: 10,
        sides: [{ side: 'RED' }, { side: 'BLUE', displayName: 'Blue' }],
      },
    })).toMatchObject({ gameTypeId: UUID, tournamentMatchId: UUID, mode: 'TEAM' })
    expect(() => parseMatchStarted({ type: 'MatchEnded', payload: {} })).toThrow('MatchStarted')
  })

  it('parses a Match snapshot while preserving table and tournament identifiers', () => {
    const match = {
      id: UUID,
      tableId: UUID,
      venueId: UUID,
      gameTypeId: UUID,
      tournamentMatchId: UUID,
      status: 'IN_PROGRESS',
      mode: 'INDIVIDUAL',
      pointsToWin: 5,
      sides: [{ side: 'RED', score: 2 }, { side: 'BLUE', score: 1 }],
      startedAt: '2026-11-22T10:00:00.000Z',
    }
    expect(parseMatch(JSON.stringify(match))).toEqual(match)
    expect(() => parseMatch({ ...match, tableId: 'invalid' })).toThrow('tableId')
  })

  it('parses acknowledgement payloads', () => {
    expect(parseEventsAcknowledged({
      venueId: 'venue-1',
      acknowledged: [UUID],
      processedAt: '2026-11-22T10:30:00.000Z',
    })).toEqual({
      venueId: 'venue-1',
      acknowledged: [UUID],
      rejected: undefined,
      processedAt: '2026-11-22T10:30:00.000Z',
    })
  })

  it('rejects malformed acknowledgement payloads', () => {
    expect(() => parseEventsAcknowledged({
      venueId: 'venue-1',
      acknowledged: ['bad'],
      processedAt: '2026-11-22T10:30:00.000Z',
    })).toThrow('Invalid MQTT payload')
  })
})
