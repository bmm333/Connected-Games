import type { KeyValueStore } from '@domain/storage/KeyValueStore'
import type {
  Actuator,
  ActuatorStateRepository as ActuatorStateRepositoryPort,
  ActuatorValue,
} from '@domain/simulator'

const STORAGE_KEY = 'SIMULATOR_ACTUATORS_V1'

export function createActuatorStateRepository(
  store: KeyValueStore,
): ActuatorStateRepositoryPort {
  async function read(): Promise<Actuator[]> {
    return (await store.get<Actuator[]>(STORAGE_KEY)) ?? []
  }

  return {
    list: read,
    async get(actuatorId) {
      return (await read()).find((actuator) => actuator.id === actuatorId) ?? null
    },
    async update(actuatorId, value: ActuatorValue) {
      const actuators = await read()
      const index = actuators.findIndex((actuator) => actuator.id === actuatorId)
      if (index < 0) throw new Error(`Actuator not found: ${actuatorId}`)
      const updated = { ...actuators[index], value, lastUpdated: new Date() }
      actuators[index] = updated
      await store.set(STORAGE_KEY, actuators)
      return updated
    },
    async replace(actuators) {
      await store.set(STORAGE_KEY, actuators)
    },
  }
}
