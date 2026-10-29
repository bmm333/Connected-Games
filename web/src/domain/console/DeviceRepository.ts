import type { Device, DeviceCommand } from './Device'

export interface DeviceRepository {
  getDevicesStatus(): Promise<Device[]>
  executeCommand(deviceId: string, command: DeviceCommand): Promise<void>
}
