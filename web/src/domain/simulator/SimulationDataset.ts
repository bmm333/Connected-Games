export type SimulationDatasetName =
  | 'devices'
  | 'telemetry'
  | 'scheduled-events'
  | 'system-config'

export interface DeviceRecord {
  readonly id: string
  readonly name: string
  readonly type: string
  readonly status: 'online' | 'offline' | 'warning'
  readonly firmwareVersion?: string
  readonly lastSeen: Date
}

export interface TelemetryRecord {
  readonly id: string
  readonly deviceId: string
  readonly metric: string
  readonly value: number | string | boolean
  readonly unit?: string
  readonly recordedAt: Date
}

export interface ScheduledEventRecord {
  readonly id: string
  readonly topic: string
  readonly payload: Readonly<Record<string, unknown>>
  readonly scheduledAt: Date
  readonly status: 'pending' | 'sent' | 'failed' | 'cancelled'
}

export interface SystemConfigRecord {
  readonly key: string
  readonly value: string | number | boolean
  readonly category: string
  readonly editable: boolean
  readonly updatedAt: Date
}

export interface SimulationRecordMap {
  devices: DeviceRecord
  telemetry: TelemetryRecord
  'scheduled-events': ScheduledEventRecord
  'system-config': SystemConfigRecord
}

export type SimulationDataset = {
  [K in SimulationDatasetName]: {
    readonly type: K
    readonly records: readonly SimulationRecordMap[K][]
  }
}[SimulationDatasetName]
