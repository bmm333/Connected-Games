import { describe, expect, it, vi } from 'vitest'
import type { MqttClientService } from '@domain/simulator'
import { createPublishMqttEventUseCase } from './PublishMqttEventUseCase'

describe('PublishMqttEventUseCase', () => {
  it('validates and publishes a serialized payload', async () => {
    const service: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      publish: vi.fn().mockResolvedValue(undefined),
      subscribe: vi.fn(),
      isConnected: vi.fn(),
    }

    await createPublishMqttEventUseCase(service)(
      'devices/device-1/events',
      { ready: true },
      { qos: 1 },
    )

    expect(service.publish).toHaveBeenCalledWith(
      'devices/device-1/events',
      '{"ready":true}',
      { qos: 1 },
    )
  })

  it('rejects wildcard publish topics', async () => {
    const service = { publish: vi.fn() } as unknown as MqttClientService

    await expect(
      createPublishMqttEventUseCase(service)('devices/+/events', {}),
    ).rejects.toThrow('Invalid MQTT publish topic')
  })
})
