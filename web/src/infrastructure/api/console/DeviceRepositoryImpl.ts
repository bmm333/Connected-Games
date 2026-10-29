import type { DeviceRepository } from '@domain/console/DeviceRepository'
import type { DeviceCommand } from '@domain/console/Device'
import { mapDevices } from './mappers'
import type { ConsoleApiClient } from './ConsoleApiClient'

export function createDeviceRepository(
  client: ConsoleApiClient,
): DeviceRepository {
  return {
    async getDevicesStatus() {
      return mapDevices(await client.getDevices())
    },
    executeCommand(deviceId: string, command: DeviceCommand) {
      return client.executeDeviceCommand(deviceId, command)
    },
  }
}
