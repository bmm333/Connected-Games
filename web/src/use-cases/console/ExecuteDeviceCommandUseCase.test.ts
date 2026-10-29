import type { DeviceRepository } from '@domain/console/DeviceRepository'
import { createExecuteDeviceCommandUseCase } from './ExecuteDeviceCommandUseCase'

describe('createExecuteDeviceCommandUseCase', () => {
  it('normalizes the device id before sending the command', async () => {
    const repository: DeviceRepository = {
      getDevicesStatus: vi.fn(),
      executeCommand: vi.fn().mockResolvedValue(undefined),
    }

    await createExecuteDeviceCommandUseCase(repository)(' device-1 ', 'sync')

    expect(repository.executeCommand).toHaveBeenCalledWith('device-1', 'sync')
  })

  it('rejects an empty device id without calling the repository', async () => {
    const repository: DeviceRepository = {
      getDevicesStatus: vi.fn(),
      executeCommand: vi.fn(),
    }

    expect(() =>
      createExecuteDeviceCommandUseCase(repository)('  ', 'reboot'),
    ).toThrow('Device ID cannot be empty')
    expect(repository.executeCommand).not.toHaveBeenCalled()
  })
})
