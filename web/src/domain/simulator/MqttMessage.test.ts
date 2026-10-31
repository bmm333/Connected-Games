import { describe, expect, it } from 'vitest'
import type { MqttMessage } from '@domain/simulator'

describe('MqttMessage', () => {
  it('models inbound and outbound JSON-safe messages', () => {
    const message: MqttMessage = {
      id: 'message-1',
      topic: 'venues/venue-1/devices/device-1/telemetry',
      payload: { temperature: 22.5 },
      qos: 1,
      retain: false,
      timestamp: new Date('2026-10-28T16:00:00.000Z'),
      direction: 'inbound',
    }

    expect(message.payload).toEqual({ temperature: 22.5 })
    expect(message.direction).toBe('inbound')
  })
})
