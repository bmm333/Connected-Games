import type {
  SimulationDatasetName,
  SimulationRecordMap,
  SimulatorDataRepository,
} from '@domain/simulator'

export type SimulationTables = {
  [K in SimulationDatasetName]: readonly SimulationRecordMap[K][]
}

export function createFetchSimulationTablesUseCase(
  repository: SimulatorDataRepository,
) {
  return async function fetchSimulationTables(): Promise<SimulationTables> {
    const [devices, telemetry, scheduledEvents, systemConfig] =
      await Promise.all([
        repository.list('devices'),
        repository.list('telemetry'),
        repository.list('scheduled-events'),
        repository.list('system-config'),
      ])

    return { devices, telemetry, 'scheduled-events': scheduledEvents, 'system-config': systemConfig }
  }
}
