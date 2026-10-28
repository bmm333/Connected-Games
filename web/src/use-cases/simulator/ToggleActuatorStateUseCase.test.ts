import { describe, expect, it, vi } from 'vitest'
import type {
  Actuator,
  ActuatorStateRepository,
  MqttClientService,
} from '@domain/simulator'
import { createToggleActuatorStateUseCase } from './ToggleActuatorStateUseCase'

const actuator: Actuator = {
  id: 'relay-1',
  name: 'Relay',
  type: 'switch',
  value: false,
  enabled: true,
  lastUpdated: new Date(),
}

describe('ToggleActuatorStateUseCase', () => {
  it('publishes before persisting the new state', async () => {
    const repository: ActuatorStateRepository = {
      list: vi.fn(),
      get: vi.fn().mockResolvedValue(actuator),
      update: vi.fn().mockImplementation(async (_id, value) => ({ ...actuator, value })),
      replace: vi.fn(),
    }
    const mqtt: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      publish: vi.fn().mockResolvedValue(undefined),
      subscribe: vi.fn(),
      isConnected: vi.fn(),
    }

    const result = await createToggleActuatorStateUseCase(repository, mqtt)('relay-1', true)

    expect(mqtt.publish).toHaveBeenCalledWith(
      'simulator/actuators/relay-1/set',
      { actuatorId: 'relay-1', value: true },
      { qos: 1 },
    )
    expect(repository.update).toHaveBeenCalledWith('relay-1', true)
    expect(result.value).toBe(true)
  })

  it('does not persist when MQTT publishing fails', async () => {
    const repository: ActuatorStateRepository = {
      list: vi.fn(),
      get: vi.fn().mockResolvedValue(actuator),
      update: vi.fn(),
      replace: vi.fn(),
    }
    const mqtt: MqttClientService = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      publish: vi.fn().mockRejectedValue(new Error('broker unavailable')),
      subscribe: vi.fn(),
      isConnected: vi.fn(),
    }

    await expect(
      createToggleActuatorStateUseCase(repository, mqtt)('relay-1', true),
    ).rejects.toThrow('broker unavailable')
    expect(repository.update).not.toHaveBeenCalled()
  })
})
