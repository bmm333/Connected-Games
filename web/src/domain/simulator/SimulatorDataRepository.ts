import type {
  SimulationDatasetName,
  SimulationRecordMap,
} from './SimulationDataset'

export interface SimulatorDataRepository {
  list<K extends SimulationDatasetName>(
    dataset: K,
  ): Promise<readonly SimulationRecordMap[K][]>

  get<K extends SimulationDatasetName>(
    dataset: K,
    recordId: string,
  ): Promise<SimulationRecordMap[K] | null>

  create<K extends SimulationDatasetName>(
    dataset: K,
    record: SimulationRecordMap[K],
  ): Promise<SimulationRecordMap[K]>

  update<K extends SimulationDatasetName>(
    dataset: K,
    recordId: string,
    record: Partial<SimulationRecordMap[K]>,
  ): Promise<SimulationRecordMap[K]>

  remove(dataset: SimulationDatasetName, recordId: string): Promise<void>

  replace<K extends SimulationDatasetName>(
    dataset: K,
    records: readonly SimulationRecordMap[K][],
  ): Promise<void>
}
