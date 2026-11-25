import { describe, expect, it, vi } from 'vitest'
import { createMatchRestClient } from './MatchRestClient'

const UUID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'
const match = {
  id: UUID,
  tableId: UUID,
  venueId: UUID,
  gameTypeId: UUID,
  status: 'IN_PROGRESS',
  mode: 'INDIVIDUAL',
  pointsToWin: 5,
  sides: [{ side: 'RED', score: 2 }, { side: 'BLUE', score: 1 }],
  startedAt: '2026-11-22T10:00:00.000Z',
}

describe('MatchRestClient', () => {
  it('requests current match by tableId and validates the response', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(
      new Response(JSON.stringify(match), { status: 200 }),
    )
    const client = createMatchRestClient('https://api.example.test', fetcher)

    await expect(client.getCurrentMatch(UUID)).resolves.toEqual(match)
    expect(fetcher).toHaveBeenCalledWith(
      `https://api.example.test/api/match/tables/${UUID}/current-match`,
      { headers: { Accept: 'application/json' } },
    )
  })

  it('returns empty for a table without a current match and rejects server errors', async () => {
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(new Response('', { status: 404 }))
      .mockResolvedValueOnce(new Response('', { status: 503 }))
    const client = createMatchRestClient('', fetcher)

    await expect(client.getCurrentMatch(UUID)).resolves.toBeUndefined()
    await expect(client.getCurrentMatch(UUID)).rejects.toThrow('HTTP 503')
  })

  it('rejects malformed snapshots instead of leaking invalid data to the UI', async () => {
    const client = createMatchRestClient('', vi.fn<typeof fetch>().mockResolvedValue(
      new Response(JSON.stringify({ ...match, tableId: 'bad' }), { status: 200 }),
    ))

    await expect(client.getCurrentMatch(UUID)).rejects.toThrow('tableId')
  })
})
