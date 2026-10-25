import { Capacitor } from '@capacitor/core'
import { Preferences } from '@capacitor/preferences'
import { del, get, set } from 'idb-keyval'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'

export function createPlatformKeyValueStore(): KeyValueStore {
  if (Capacitor.isNativePlatform()) {
    return {
      async get<T>(key: string) {
        const result = await Preferences.get({ key })
        if (result.value === null) return undefined

        return JSON.parse(result.value) as T
      },
      async set<T>(key: string, value: T) {
        await Preferences.set({ key, value: JSON.stringify(value) })
      },
      remove: async (key: string) => {
        await Preferences.remove({ key })
      },
    }
  }

  return {
    async get<T>(key: string) {
      return get<T>(key)
    },
    async set<T>(key: string, value: T) {
      await set(key, value)
    },
    async remove(key: string) {
      await del(key)
    },
  }
}
