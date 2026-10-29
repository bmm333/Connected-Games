import type { Device } from '@domain/console/Device'
import type { DeviceRepository } from '@domain/console/DeviceRepository'

export type GetDevicesStatusUseCase = () => Promise<Device[]>

export function createGetDevicesStatusUseCase(
  repository: DeviceRepository,
): GetDevicesStatusUseCase {
  return () => repository.getDevicesStatus()
}
