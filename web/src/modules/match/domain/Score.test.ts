import { describe, it, expect } from 'vitest'
import { MatchScore } from '@match/domain/Score'

describe('MatchScore Domain Entity', () => {
  it('creates initial score with 0 points for both players', () => {
    const match = MatchScore.create('player-1', 'player-2')

    expect(match.home).toEqual({ playerId: 'player-1', points: 0 })
    expect(match.away).toEqual({ playerId: 'player-2', points: 0 })
    expect(match.leader).toBeNull()
  })

  it('throws an error if players have the same ID', () => {
    expect(() => MatchScore.create('player-1', 'player-1')).toThrow(
      'A player cannot play against themselves'
    )
  })

  it('increments home score and computes leader correctly', () => {
    const match = MatchScore.create('player-1', 'player-2')
    const updated = match.addPointToHome()

    expect(updated.home.points).toBe(1)
    expect(updated.away.points).toBe(0)
    expect(updated.leader).toBe('player-1')
  })

  it('increments away score and detects leader change', () => {
    const match = MatchScore.create('player-1', 'player-2')
      .addPointToAway()
      .addPointToAway()

    expect(match.home.points).toBe(0)
    expect(match.away.points).toBe(2)
    expect(match.leader).toBe('player-2')
  })
})
