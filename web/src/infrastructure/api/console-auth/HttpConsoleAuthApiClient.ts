import type { ConsoleAuthApiClient } from './ConsoleAuthApiClient'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'

async function request(
  baseUrl: string,
  path: string,
  init?: RequestInit,
): Promise<unknown> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...init,
    credentials: 'include',
    headers: { Accept: 'application/json', ...init?.headers },
  })

  if (!response.ok) {
    throw new Error(`Console auth request failed (HTTP ${response.status})`)
  }

  return response.status === 204 ? undefined : response.json()
}

export function createHttpConsoleAuthApiClient(
  baseUrl = import.meta.env.VITE_CONSOLE_AUTH_API_URL ?? '/api/console/auth',
): ConsoleAuthApiClient {
  return {
    login: (payload: ConsoleLoginRequest) =>
      request(baseUrl, '/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      }),
    logout: (accessToken) =>
      request(baseUrl, '/logout', {
        method: 'POST',
        headers: accessToken
          ? { Authorization: `Bearer ${accessToken}` }
          : undefined,
      }).then(() => undefined),
    getCurrentSession: async (accessToken) => {
      try {
        return await request(baseUrl, '/session', {
          headers: { Authorization: `Bearer ${accessToken}` },
        })
      } catch (error) {
        if (error instanceof Error && error.message.includes('(HTTP 401)')) {
          return undefined
        }
        throw error
      }
    },
    refreshToken: (refreshToken) =>
      request(baseUrl, '/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      }),
  }
}
