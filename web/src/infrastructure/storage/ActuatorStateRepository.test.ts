import { describe, expect, it } from 'vitest'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'
import type { Actuator } from '@domain/simulator'
import { createActuatorStateRepository } from './ActuatorStateRepository'

function store(): KeyValueStore {
  const values = new Map<string, unknown>()
  return {
    async get<T>(key: string) { return values.get(key) as T | undefined },
    async set<T>(key: string, value: T) { values.set(key, value) },
    async remove(key: string) { values.delete(key) },
  }
}

describe('ActuatorStateRepository', () => {
  it('updates the value and records a fresh timestamp', async () => {
    const repository = createActuatorStateRepository(store())
    const actuator: Actuator = {
      id: 'relay-1',
      name: 'Relay',
      type: 'switch',
      value: false,
      enabled: true,
      lastUpdated: new Date('2026-10-28T16:00:00.000Z'),
    }

    await repository.replace([actuator])
    const result = await repository.update('relay-1', true)

    expect(result.value).toBe(true)
    expect(result.lastUpdated.getTime()).toBeGreaterThan(actuator.lastUpdated.getTime())
  })

  it('fails clearly when an actuator does not exist', async () => {
    await expect(
      createActuatorStateRepository(store()).update('missing', true),
    ).rejects.toThrow('Actuator not found')
  })
})
