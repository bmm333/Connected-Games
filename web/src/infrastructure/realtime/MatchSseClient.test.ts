import { describe, expect, it, vi } from 'vitest'
import { createMatchSseClient } from './MatchSseClient'

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

function sseResponse(data: string): Response {
  return new Response(new ReadableStream({
    start(controller) {
      controller.enqueue(new TextEncoder().encode(data))
      controller.close()
    },
  }), {
    status: 200,
    headers: { 'Content-Type': 'text/event-stream' },
  })
}

describe('MatchSseClient', () => {
  it('sends an authenticated-fetch-compatible request and parses Match updates', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(
      sseResponse(`data: ${JSON.stringify(match)}\n\n`),
    )
    const onMatch = vi.fn()
    const onStatus = vi.fn()
    const client = createMatchSseClient('https://stream.example.test', fetcher)
    const unsubscribe = client.subscribe(UUID, onMatch, onStatus)

    await vi.waitFor(() => expect(onMatch).toHaveBeenCalledWith(match))
    expect(fetcher).toHaveBeenCalledWith(
      `https://stream.example.test/api/match/tables/${UUID}/live`,
      expect.objectContaining({
        headers: { Accept: 'text/event-stream' },
        signal: expect.any(AbortSignal),
      }),
    )
    unsubscribe()

    expect(onStatus.mock.calls.map(([status]) => status))
      .toEqual(['connecting', 'connected', 'retrying', 'disconnected'])
  })

  it('discards malformed payloads and reports client errors', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(
      sseResponse('data: {"tableId":"bad"}\n\n'),
    )
    const onMatch = vi.fn()
    const onStatus = vi.fn()
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    const unsubscribe = createMatchSseClient('', fetcher)
      .subscribe(UUID, onMatch, onStatus)

    await vi.waitFor(() => expect(warn).toHaveBeenCalledOnce())
    expect(onMatch).not.toHaveBeenCalled()
    unsubscribe()
    warn.mockRestore()

    const unauthorizedStatus = vi.fn()
    const unauthorizedFetch = vi.fn<typeof fetch>().mockResolvedValue(
      new Response('', { status: 401 }),
    )
    const stopUnauthorized = createMatchSseClient('', unauthorizedFetch)
      .subscribe(UUID, vi.fn(), unauthorizedStatus)

    await vi.waitFor(() => expect(unauthorizedStatus).toHaveBeenLastCalledWith('error'))
    stopUnauthorized()
  })
})
