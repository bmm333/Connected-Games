import type { Device, DeviceStatus, SurfaceType } from '@domain/console/Device'
import type { LocalMetrics, SystemOverview } from '@domain/console/SystemOverview'
import type {
  TableCellValue,
  TableColumn,
  TableData,
  TableRow,
} from '@domain/console/TableData'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function requiredString(value: unknown, field: string): string {
  if (typeof value !== 'string' || value.trim().length === 0) {
    throw new Error(`Console API response has invalid ${field}`)
  }
  return value
}

function requiredNumber(value: unknown, field: string): number {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    throw new Error(`Console API response has invalid ${field}`)
  }
  return value
}

function enumValue<T extends string>(
  value: unknown,
  values: readonly T[],
  field: string,
): T {
  if (typeof value !== 'string' || !values.includes(value as T)) {
    throw new Error(`Console API response has invalid ${field}`)
  }
  return value as T
}

function mapLocalMetrics(value: unknown): LocalMetrics {
  if (!isRecord(value)) throw new Error('Console API response has invalid metrics')
  return {
    memoryUsagePercent: requiredNumber(value.memoryUsagePercent, 'memoryUsagePercent'),
    throughputPerSecond: requiredNumber(value.throughputPerSecond, 'throughputPerSecond'),
    activeAlerts: requiredNumber(value.activeAlerts, 'activeAlerts'),
  }
}

export function mapSystemOverview(value: unknown): SystemOverview {
  if (!isRecord(value)) throw new Error('Console API response has invalid overview')
  return {
    status: enumValue(value.status, ['healthy', 'degraded', 'unhealthy'] as const, 'status'),
    metrics: mapLocalMetrics(value.metrics),
    observedAt: requiredString(value.observedAt, 'observedAt'),
  }
}

export function mapDevice(value: unknown): Device {
  if (!isRecord(value)) throw new Error('Console API response has invalid device')
  const batteryPercent =
    value.batteryPercent === undefined
      ? {}
      : { batteryPercent: requiredNumber(value.batteryPercent, 'batteryPercent') }
  const hardwareInfo =
    value.hardwareInfo === undefined
      ? {}
      : { hardwareInfo: mapHardwareInfo(value.hardwareInfo) }

  return {
    id: requiredString(value.id, 'device.id'),
    name: requiredString(value.name, 'device.name'),
    surfaceType: enumValue(
      value.surfaceType,
      ['kiosk', 'player', 'console'] as const satisfies readonly SurfaceType[],
      'device.surfaceType',
    ),
    status: enumValue(
      value.status,
      ['online', 'offline'] as const satisfies readonly DeviceStatus[],
      'device.status',
    ),
    lastPing: value.lastPing === null ? null : requiredString(value.lastPing, 'device.lastPing'),
    ...batteryPercent,
    ...hardwareInfo,
  }
}

function mapHardwareInfo(value: unknown): Record<string, string> {
  if (!isRecord(value)) throw new Error('Console API response has invalid hardwareInfo')
  return Object.fromEntries(
    Object.entries(value).map(([key, item]) => [key, requiredString(item, `hardwareInfo.${key}`)]),
  )
}

export function mapDevices(value: unknown): Device[] {
  if (!Array.isArray(value)) throw new Error('Console API response has invalid devices')
  return value.map(mapDevice)
}

function mapTableCellValue(value: unknown): TableCellValue {
  if (value === null || typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return value
  }
  throw new Error('Console API response has invalid table cell value')
}

export function mapTableData(value: unknown): TableData {
  if (!isRecord(value) || !Array.isArray(value.columns) || !Array.isArray(value.rows)) {
    throw new Error('Console API response has invalid table data')
  }

  const columns = value.columns.map((column): TableColumn => {
    if (!isRecord(column)) throw new Error('Console API response has invalid table column')
    return {
      key: requiredString(column.key, 'table column key'),
      label: requiredString(column.label, 'table column label'),
    }
  })

  const rows = value.rows.map((row): TableRow => {
    if (!isRecord(row) || !isRecord(row.values)) {
      throw new Error('Console API response has invalid table row')
    }
    return {
      id: requiredString(row.id, 'table row id'),
      values: Object.fromEntries(
        Object.entries(row.values).map(([key, item]) => [key, mapTableCellValue(item)]),
      ),
    }
  })

  return {
    columns,
    rows,
    total: requiredNumber(value.total, 'table total'),
  }
}
