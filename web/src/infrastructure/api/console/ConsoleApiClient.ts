import type { DeviceCommand, Device } from '@domain/console/Device'
import type { SystemOverview } from '@domain/console/SystemOverview'
import type { TableData, TableQuery } from '@domain/console/TableData'

export interface ConsoleApiClient {
  getOverview(): Promise<unknown>
  getDevices(): Promise<unknown>
  getTableData(query: TableQuery): Promise<unknown>
  executeDeviceCommand(deviceId: string, command: DeviceCommand): Promise<void>
}

export interface ConsoleApiDto {
  overview: SystemOverview
  devices: Device[]
  tableData: TableData
}
