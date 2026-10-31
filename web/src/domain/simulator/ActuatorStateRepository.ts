import type { Actuator, ActuatorValue } from './Actuator'

export interface ActuatorStateRepository {
  list(): Promise<readonly Actuator[]>

  get(actuatorId: string): Promise<Actuator | null>

  update(actuatorId: string, value: ActuatorValue): Promise<Actuator>

  replace(actuators: readonly Actuator[]): Promise<void>
}
