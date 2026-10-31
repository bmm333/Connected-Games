import type { KeyValueStore } from '@domain/storage/KeyValueStore'
import type {
  SimulationDatasetName,
  SimulationRecordMap,
  SimulatorDataRepository,
} from '@domain/simulator'

const DATASET_KEYS: Record<SimulationDatasetName, string> = {
  devices: 'SIMULATOR_DATASET_DEVICES_V1',
  telemetry: 'SIMULATOR_DATASET_TELEMETRY_V1',
  'scheduled-events': 'SIMULATOR_DATASET_SCHEDULED_EVENTS_V1',
  'system-config': 'SIMULATOR_DATASET_SYSTEM_CONFIG_V1',
}

function hydrate<K extends SimulationDatasetName>(
  dataset: K,
  record: SimulationRecordMap[K],
): SimulationRecordMap[K] {
  if (dataset === 'devices') {
    const device = record as SimulationRecordMap['devices']
    return { ...device, lastSeen: new Date(device.lastSeen) } as SimulationRecordMap[K]
  }
  if (dataset === 'telemetry') {
    const telemetry = record as SimulationRecordMap['telemetry']
    return { ...telemetry, recordedAt: new Date(telemetry.recordedAt) } as SimulationRecordMap[K]
  }
  if (dataset === 'scheduled-events') {
    const event = record as SimulationRecordMap['scheduled-events']
    return { ...event, scheduledAt: new Date(event.scheduledAt) } as SimulationRecordMap[K]
  }
  const config = record as SimulationRecordMap['system-config']
  return { ...config, updatedAt: new Date(config.updatedAt) } as SimulationRecordMap[K]
}

function recordKey<K extends SimulationDatasetName>(
  dataset: K,
  record: SimulationRecordMap[K],
): string {
  if (dataset === 'system-config') {
    return (record as SimulationRecordMap['system-config']).key
  }
  return (record as Exclude<SimulationRecordMap[K], SimulationRecordMap['system-config']>).id
}

export function createSimulatorDataRepository(
  store: KeyValueStore,
): SimulatorDataRepository {
  async function read<K extends SimulationDatasetName>(
    dataset: K,
  ): Promise<SimulationRecordMap[K][]> {
    const records = await store.get<SimulationRecordMap[K][]>(DATASET_KEYS[dataset])
    return (records ?? []).map((record) => hydrate(dataset, record))
  }

  return {
    list: read,
    async get(dataset, recordId) {
      return (await read(dataset)).find((record) => recordKey(dataset, record) === recordId) ?? null
    },
    async create(dataset, record) {
      const records = await read(dataset)
      const key = recordKey(dataset, record)
      if (records.some((item) => recordKey(dataset, item) === key)) {
        throw new Error(`Record already exists: ${key}`)
      }
      await store.set(DATASET_KEYS[dataset], [...records, record])
      return record
    },
    async update(dataset, recordId, patch) {
      const records = await read(dataset)
      const index = records.findIndex((record) => recordKey(dataset, record) === recordId)
      if (index < 0) throw new Error(`Record not found: ${recordId}`)
      const updated = { ...records[index], ...patch } as SimulationRecordMap[typeof dataset]
      records[index] = updated
      await store.set(DATASET_KEYS[dataset], records)
      return updated
    },
    async remove(dataset, recordId) {
      const records = await read(dataset)
      const filtered = records.filter((record) => recordKey(dataset, record) !== recordId)
      if (filtered.length === records.length) throw new Error(`Record not found: ${recordId}`)
      await store.set(DATASET_KEYS[dataset], filtered)
    },
    async replace(dataset, records) {
      await store.set(DATASET_KEYS[dataset], records)
    },
  }
}
