import type { KeyValueStore } from '@domain/storage/KeyValueStore'

export interface KeyValueStoreUseCases {
  load<T>(key: string): Promise<T | undefined>
  save<T>(key: string, value: T): Promise<void>
  remove(key: string): Promise<void>
}

export function createKeyValueStoreUseCases(
  store: KeyValueStore,
): KeyValueStoreUseCases {
  return {
    load: <T>(key: string) => store.get<T>(key),
    save: <T>(key: string, value: T) => store.set(key, value),
    remove: (key: string) => store.remove(key),
  }
}
