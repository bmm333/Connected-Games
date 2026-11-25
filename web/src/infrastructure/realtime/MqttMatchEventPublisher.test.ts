import { describe, expect, it, vi } from 'vitest'
import type { MqttClientService } from './MqttClientService'
import type { MqttMessage } from './MqttMessage'
import { createMqttMatchEventPublisher } from './MqttMatchEventPublisher'

const UUID = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'
const completed = {
  eventId: UUID,
  tenantId: 'tenant',
  venueId: UUID,
  tableId: UUID,
  matchId: UUID,
  gameTypeId: UUID,
  mode: 'INDIVIDUAL',
  pointsToWin: 1,
  startedAt: '2026-11-25T18:00:00.000Z',
  endedAt: '2026-11-25T18:01:00.000Z',
  reason: 'SCORE_REACHED',
  sides: [{ side: 'RED', score: 1 }, { side: 'BLUE', score: 0 }],
  points: [{ seq: 1, side: 'RED', occurredAt: '2026-11-25T18:00:30.000Z' }],
}

function message(topic: string, payload: string): MqttMessage {
  return {
    id: UUID,
    topic,
    payload,
    qos: 1,
    retain: false,
    timestamp: new Date(),
    direction: 'inbound',
  }
}

describe('MqttMatchEventPublisher', () => {
  it('parses MatchCompleted contract messages and discards malformed payloads', async () => {
    let handler: ((message: MqttMessage) => void) | undefined
    const service: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      subscribeConnectionState: vi.fn(() => () => undefined),
      publish: vi.fn(),
      subscribe: vi.fn(async (_topic: string, callback: (message: MqttMessage) => void) => {
        handler = callback
        return { unsubscribe: async () => undefined }
      }),
      isConnected: vi.fn(() => true),
    }
    const publisher = createMqttMatchEventPublisher(service, {
      url: 'wss://broker.example.test/mqtt',
      clientId: 'test-client',
    })
    const onMatch = vi.fn()
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    await publisher.subscribeMatchCompleted(onMatch)

    expect(service.subscribe).toHaveBeenCalledWith(
      'platform/matches/completed',
      expect.any(Function),
    )
    handler?.(message('platform/matches/completed', JSON.stringify(completed)))
    handler?.(message('platform/matches/completed', '{"tableId":"bad"}'))

    expect(onMatch).toHaveBeenCalledOnce()
    expect(onMatch).toHaveBeenCalledWith(completed)
    expect(warn).toHaveBeenCalledOnce()
    warn.mockRestore()
  })

  it('subscribes to the venue acknowledgement topic from AsyncAPI', async () => {
    const service: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      subscribeConnectionState: vi.fn(() => () => undefined),
      publish: vi.fn(),
      subscribe: vi.fn(async () => ({ unsubscribe: async () => undefined })),
      isConnected: vi.fn(() => true),
    }
    const publisher = createMqttMatchEventPublisher(service, {
      url: 'wss://broker.example.test/mqtt',
      clientId: 'test-client',
    })

    await publisher.subscribeAck(UUID, vi.fn())

    expect(service.subscribe).toHaveBeenCalledWith(
      `venues/${UUID}/hub/ack`,
      expect.any(Function),
    )
  })
})
