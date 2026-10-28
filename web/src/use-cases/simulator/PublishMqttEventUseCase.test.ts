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

  it('rejects empty path segments and oversized payloads', async () => {
    const service = { publish: vi.fn() } as unknown as MqttClientService
    const publish = createPublishMqttEventUseCase(service)

    await expect(publish('devices//events', {})).rejects.toThrow('Invalid MQTT publish topic')
    await expect(publish('devices/events', 'x'.repeat(64 * 1024 + 1)))
      .rejects.toThrow('MQTT payload exceeds the 64 KiB limit')
    expect(service.publish).not.toHaveBeenCalled()
  })

  it('propagates broker failures', async () => {
    const service: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      publish: vi.fn().mockRejectedValue(new Error('broker unavailable')),
      subscribe: vi.fn(),
      isConnected: vi.fn(),
    }

    await expect(
      createPublishMqttEventUseCase(service)('devices/events', { ready: true }),
    ).rejects.toThrow('broker unavailable')
  })
})
