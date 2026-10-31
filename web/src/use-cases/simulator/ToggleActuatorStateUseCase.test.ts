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

  it('rejects invalid switch and slider values before publishing', async () => {
    const mqtt = {
      connect: vi.fn(),
      disconnect: vi.fn(),
      publish: vi.fn(),
      subscribe: vi.fn(),
      isConnected: vi.fn(),
    } as unknown as MqttClientService
    const repository: ActuatorStateRepository = {
      list: vi.fn(),
      get: vi.fn().mockResolvedValue({
        ...actuator,
        type: 'slider',
        min: 0,
        max: 100,
      }),
      update: vi.fn(),
      replace: vi.fn(),
    }

    await expect(
      createToggleActuatorStateUseCase(repository, mqtt)('relay-1', 101),
    ).rejects.toThrow('exceeds its maximum')
    expect(mqtt.publish).not.toHaveBeenCalled()
  })

  it('rejects missing and disabled actuators', async () => {
    const repository: ActuatorStateRepository = {
      list: vi.fn(),
      get: vi.fn().mockResolvedValue(null),
      update: vi.fn(),
      replace: vi.fn(),
    }
    const mqtt = { publish: vi.fn() } as unknown as MqttClientService
    const useCase = createToggleActuatorStateUseCase(repository, mqtt)

    await expect(useCase('missing', true)).rejects.toThrow('Actuator not found')
    vi.mocked(repository.get).mockResolvedValue({ ...actuator, enabled: false })
    await expect(useCase('relay-1', true)).rejects.toThrow('Actuator is disabled')
  })
})
