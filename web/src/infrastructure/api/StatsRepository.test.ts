import { describe, expect, it, vi } from 'vitest'
import { createStatsRepository } from './StatsRepository'

const UUID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'

describe('StatsRepository', () => {
  it('implements the participant match-history and match-result endpoints', async () => {
    const participantMatches = [{ matchId: UUID, venueId: UUID }]
    const matchResult = { matchId: UUID, points: [] }
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(new Response(JSON.stringify(participantMatches), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify(matchResult), { status: 200 }))
    const repository = createStatsRepository('https://api.example.test/api/stats', fetcher)

    await expect(repository.getParticipantMatches('player/1', {
      venueId: UUID,
      gameTypeId: UUID,
      from: '2026-01-01T00:00:00Z',
      to: '2026-02-01T00:00:00Z',
      limit: 10,
      offset: 20,
    })).resolves.toEqual(participantMatches)
    await expect(repository.getMatchResult(UUID)).resolves.toEqual(matchResult)

    expect(fetcher.mock.calls.map(([input]) => String(input))).toEqual([
      `https://api.example.test/api/stats/participants/player%2F1/matches?venueId=${UUID}&gameTypeId=${UUID}&from=2026-01-01T00%3A00%3A00Z&to=2026-02-01T00%3A00%3A00Z&limit=10&offset=20`,
      `https://api.example.test/api/stats/matches/${UUID}`,
    ])
    for (const [, init] of fetcher.mock.calls) {
      expect(new Headers(init?.headers).get('Accept')).toBe('application/json')
    }
  })

  it('keeps 404 semantics for missing match results and surfaces other errors', async () => {
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(new Response('', { status: 404 }))
      .mockResolvedValueOnce(new Response('', { status: 503 }))
    const repository = createStatsRepository('', fetcher)

    await expect(repository.getMatchResult(UUID)).resolves.toBeUndefined()
    await expect(repository.getMatchResult(UUID)).rejects.toThrow('HTTP 503')
  })
})
