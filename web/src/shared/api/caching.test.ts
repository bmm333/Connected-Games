import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  createApplicationQueryClient,
  STALE_TIME_DEFAULT,
  CACHE_TIME_24H,
} from '@shared/api/queryClient'
import { createIdbPersister } from '@shared/api/persister'
import * as idb from 'idb-keyval'
import type { PersistedClient } from '@tanstack/react-query-persist-client'

vi.mock('idb-keyval', () => ({
  get: vi.fn(),
  set: vi.fn(),
  del: vi.fn(),
}))

describe('Caching Strategy - Level 3: State Caching', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createApplicationQueryClient', () => {
    it('initializes with offlineFirst networkMode and 24h retention', () => {
      const client = createApplicationQueryClient()
      const defaultQueryOptions = client.getDefaultOptions().queries

      expect(defaultQueryOptions?.staleTime).toBe(STALE_TIME_DEFAULT)
      expect(defaultQueryOptions?.gcTime).toBe(CACHE_TIME_24H)
      expect(defaultQueryOptions?.networkMode).toBe('offlineFirst')
    })

    it('does not retry on 4xx client errors', () => {
      const client = createApplicationQueryClient()
      const retryFn = client.getDefaultOptions().queries?.retry as (
        count: number,
        err: unknown
      ) => boolean

      expect(retryFn(1, { status: 404 })).toBe(false)
      expect(retryFn(1, { status: 401 })).toBe(false)
      expect(retryFn(1, { status: 500 })).toBe(true)
      expect(retryFn(3, { status: 500 })).toBe(false)
    })
  })

  describe('createIdbPersister', () => {
    const mockClient: PersistedClient = {
      timestamp: Date.now(),
      buster: 'v1',
      clientState: {
        queries: [],
        mutations: [],
      },
    }

    it('saves query client state to IndexedDB', async () => {
      const persister = createIdbPersister('TEST_KEY')
      await persister.persistClient(mockClient)

      expect(idb.set).toHaveBeenCalledWith('TEST_KEY', mockClient)
    })

    it('restores query client state from IndexedDB', async () => {
      vi.mocked(idb.get).mockResolvedValueOnce(mockClient)
      const persister = createIdbPersister('TEST_KEY')
      const restored = await persister.restoreClient()

      expect(idb.get).toHaveBeenCalledWith('TEST_KEY')
      expect(restored).toEqual(mockClient)
    })

    it('clears query client state on removeClient', async () => {
      const persister = createIdbPersister('TEST_KEY')
      await persister.removeClient()

      expect(idb.del).toHaveBeenCalledWith('TEST_KEY')
    })
  })
})
