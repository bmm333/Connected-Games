import { describe, expect, it, vi } from 'vitest'
import { createMatchSseClient, type EventSourceLike } from './MatchSseClient'

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

function createEventSource() {
  let readyState = 0
  const source: EventSourceLike = {
    get readyState() {
      return readyState
    },
    onopen: null,
    onmessage: null,
    onerror: null,
    close: vi.fn(),
  }
  const factory = vi.fn(() => source)
  return {
    source,
    factory,
    setReadyState(value: number) {
      readyState = value
    },
  }
}

describe('MatchSseClient', () => {
  it('parses Match updates and reports retry/disconnect states', () => {
    const { source, factory, setReadyState } = createEventSource()
    const onMatch = vi.fn()
    const onStatus = vi.fn()
    const client = createMatchSseClient('https://stream.example.test', factory)
    const unsubscribe = client.subscribe(UUID, onMatch, onStatus)

    expect(factory).toHaveBeenCalledWith(
      `https://stream.example.test/api/match/tables/${UUID}/live`,
    )
    source.onopen?.(new Event('open'))
    source.onmessage?.({ data: JSON.stringify(match) } as MessageEvent<string>)
    setReadyState(0)
    source.onerror?.(new Event('error'))
    unsubscribe()

    expect(onMatch).toHaveBeenCalledWith(match)
    expect(onStatus.mock.calls.map(([status]) => status))
      .toEqual(['connecting', 'connected', 'retrying', 'disconnected'])
    expect(source.close).toHaveBeenCalledOnce()
  })

  it('discards malformed payloads and reports a terminal stream disconnection', () => {
    const { source, factory, setReadyState } = createEventSource()
    const onMatch = vi.fn()
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    const onStatus = vi.fn()
    createMatchSseClient('', factory).subscribe(UUID, onMatch, onStatus)

    source.onmessage?.({ data: '{"tableId":"bad"}' } as MessageEvent<string>)
    setReadyState(2)
    source.onerror?.(new Event('error'))

    expect(onMatch).not.toHaveBeenCalled()
    expect(warn).toHaveBeenCalledOnce()
    expect(onStatus).toHaveBeenLastCalledWith('disconnected')
    warn.mockRestore()
  })
})
