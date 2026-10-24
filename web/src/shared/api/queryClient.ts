import { QueryClient } from '@tanstack/react-query'

/**
 * 24-hour persistence retention time.
 * Keeps data available for offline usage across application restarts.
 */
export const CACHE_TIME_24H = 1000 * 60 * 60 * 24

/**
 * Default stale time of 2 minutes for server queries.
 * Prevents over-fetching while ensuring fresh state during active navigation.
 */
export const STALE_TIME_DEFAULT = 1000 * 60 * 2

export function createApplicationQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: STALE_TIME_DEFAULT,
        gcTime: CACHE_TIME_24H,
        // In offline gaming, return cached data first without throwing network errors
        networkMode: 'offlineFirst',
        refetchOnWindowFocus: true,
        refetchOnReconnect: true,
        retry: (failureCount, error) => {
          // Do not retry on 4xx client errors (e.g. 401 unauthorized, 404 not found)
          if (error && typeof error === 'object' && 'status' in error) {
            const status = (error as { status: number }).status
            if (status >= 400 && status < 500) return false
          }
          return failureCount < 3
        },
      },
      mutations: {
        networkMode: 'offlineFirst',
        retry: 1,
      },
    },
  })
}

export const queryClient = createApplicationQueryClient()
