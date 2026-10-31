import type {
  Actuator,
  ActuatorStateRepository,
  ActuatorValue,
  MqttClientService,
} from '@domain/simulator'

function validateValue(actuator: Actuator, value: ActuatorValue): void {
  if (actuator.type === 'switch' && typeof value !== 'boolean') {
    throw new Error(`Actuator ${actuator.id} expects a boolean value`)
  }
  if (actuator.type === 'slider') {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      throw new Error(`Actuator ${actuator.id} expects a finite number`)
    }
    if (actuator.min !== undefined && value < actuator.min) {
      throw new Error(`Actuator ${actuator.id} value is below its minimum`)
    }
    if (actuator.max !== undefined && value > actuator.max) {
      throw new Error(`Actuator ${actuator.id} value exceeds its maximum`)
    }
  }
}

export function createToggleActuatorStateUseCase(
  actuatorRepository: ActuatorStateRepository,
  mqttService: MqttClientService,
) {
  return async function toggleActuatorState(
    actuatorId: string,
    value: ActuatorValue,
  ): Promise<Actuator> {
    const actuator = await actuatorRepository.get(actuatorId)
    if (actuator === null) {
      throw new Error(`Actuator not found: ${actuatorId}`)
    }
    if (!actuator.enabled) {
      throw new Error(`Actuator is disabled: ${actuatorId}`)
    }

    validateValue(actuator, value)
    await mqttService.publish(
      `simulator/actuators/${encodeURIComponent(actuatorId)}/set`,
      { actuatorId, value },
      { qos: 1 },
    )

    return actuatorRepository.update(actuatorId, value)
  }
}
