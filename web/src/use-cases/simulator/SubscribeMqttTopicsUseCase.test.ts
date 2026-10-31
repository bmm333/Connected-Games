import { describe, expect, it, vi } from 'vitest'
import type { MqttClientService, MqttMessage } from '@domain/simulator'
import { createSubscribeMqttTopicsUseCase } from './SubscribeMqttTopicsUseCase'

function service(): MqttClientService {
  return {
    connect: vi.fn(),
    disconnect: vi.fn(),
    publish: vi.fn(),
    subscribe: vi.fn().mockResolvedValue({ unsubscribe: vi.fn() }),
    isConnected: vi.fn(),
  }
}

describe('SubscribeMqttTopicsUseCase', () => {
  it('subscribes to valid wildcard topics and unsubscribes all handlers', async () => {
    const mqtt = service()
    const onMessage = vi.fn<(message: MqttMessage) => void>()
    const unsubscribe = vi.fn().mockResolvedValue(undefined)
    vi.mocked(mqtt.subscribe).mockResolvedValue({ unsubscribe })

    const subscription = await createSubscribeMqttTopicsUseCase(mqtt)([
      'venues/+/events',
      'venues/#',
    ], onMessage)

    expect(mqtt.subscribe).toHaveBeenCalledTimes(2)
    await subscription.unsubscribe()
    expect(unsubscribe).toHaveBeenCalledTimes(2)
  })

  it('rejects malformed wildcard topics', async () => {
    const mqtt = service()

    await expect(
      createSubscribeMqttTopicsUseCase(mqtt)(['venues/#/events'], vi.fn()),
    ).rejects.toThrow('Invalid MQTT subscription topic')
    expect(mqtt.subscribe).not.toHaveBeenCalled()
  })

  it('requires at least one topic', async () => {
    await expect(
      createSubscribeMqttTopicsUseCase(service())([], vi.fn()),
    ).rejects.toThrow('At least one MQTT topic is required')
  })
})
