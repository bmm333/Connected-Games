import { describe, expect, it } from 'vitest'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'
import type {
  DeviceRecord,
  SystemConfigRecord,
} from '@domain/simulator'
import { createSimulatorDataRepository } from './SimulatorDataRepository'

function store(initial: Record<string, unknown> = {}): KeyValueStore {
  const values = new Map(Object.entries(initial))
  return {
    async get<T>(key: string) {
      return values.get(key) as T | undefined
    },
    async set<T>(key: string, value: T) {
      values.set(key, value)
    },
    async remove(key: string) {
      values.delete(key)
    },
  }
}

describe('SimulatorDataRepository', () => {
  it('persists device records and restores date fields', async () => {
    const repository = createSimulatorDataRepository(store())
    const device: DeviceRecord = {
      id: 'device-1',
      name: 'Hub',
      type: 'hub',
      status: 'online',
      lastSeen: new Date('2026-10-28T16:00:00.000Z'),
    }

    await repository.create('devices', device)
    const result = await repository.get('devices', 'device-1')

    expect(result).not.toBeNull()
    expect(result?.lastSeen).toBeInstanceOf(Date)
    expect(result?.lastSeen.toISOString()).toBe('2026-10-28T16:00:00.000Z')
  })

  it('uses key instead of id for system configuration records', async () => {
    const repository = createSimulatorDataRepository(store())
    const config: SystemConfigRecord = {
      key: 'mqtt.url',
      value: 'wss://localhost:9001',
      category: 'mqtt',
      editable: true,
      updatedAt: new Date('2026-10-28T16:00:00.000Z'),
    }

    await repository.create('system-config', config)
    await repository.update('system-config', 'mqtt.url', { value: 'wss://broker' })

    expect(await repository.get('system-config', 'mqtt.url')).toMatchObject({
      key: 'mqtt.url',
      value: 'wss://broker',
    })
  })

  it('rejects duplicate and missing records', async () => {
    const repository = createSimulatorDataRepository(store())
    const record: DeviceRecord = {
      id: 'device-1',
      name: 'Hub',
      type: 'hub',
      status: 'offline',
      lastSeen: new Date(),
    }

    await repository.create('devices', record)
    await expect(repository.create('devices', record)).rejects.toThrow('Record already exists')
    await expect(repository.remove('devices', 'missing')).rejects.toThrow('Record not found')
  })
})
