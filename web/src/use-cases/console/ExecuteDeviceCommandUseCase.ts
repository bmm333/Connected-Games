import type { DeviceCommand } from '@domain/console/Device'
import type { DeviceRepository } from '@domain/console/DeviceRepository'

export type ExecuteDeviceCommandUseCase = (
  deviceId: string,
  command: DeviceCommand,
) => Promise<void>

export function createExecuteDeviceCommandUseCase(
  repository: DeviceRepository,
): ExecuteDeviceCommandUseCase {
  return (deviceId, command) => {
    const normalizedDeviceId = deviceId.trim()
    if (normalizedDeviceId.length === 0) {
      throw new Error('Device ID cannot be empty')
    }

    return repository.executeCommand(normalizedDeviceId, command)
  }
}
