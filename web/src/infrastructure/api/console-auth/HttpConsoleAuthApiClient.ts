import createClient from 'openapi-fetch'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'
import type { paths } from '@shared/api/generated/identity'
import type { ConsoleAuthApiClient } from './ConsoleAuthApiClient'

export function createHttpConsoleAuthApiClient(
  baseUrl = import.meta.env.VITE_IDENTITY_API_URL ?? '/api/identity',
): ConsoleAuthApiClient {
  const client = createClient<paths>({ baseUrl, credentials: 'include' })

  return {
    async login(payload: ConsoleLoginRequest) {
      const result = await client.POST('/sessions', { body: payload })
      if (result.error !== undefined) {
        throw new Error(`Identity login failed (HTTP ${result.response.status})`)
      }
      return result.data
    },
    async logout(accessToken) {
      const result = await client.DELETE('/sessions/current', {
        headers: accessToken
          ? { Authorization: `Bearer ${accessToken}` }
          : undefined,
      })
      if (result.error !== undefined) {
        throw new Error(`Identity logout failed (HTTP ${result.response.status})`)
      }
    },
    async getCurrentUser(accessToken) {
      const result = await client.GET('/users/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      if (result.error !== undefined) {
        throw new Error(
          `Identity session validation failed (HTTP ${result.response.status})`,
        )
      }
      return result.data
    },
    async refreshToken(refreshToken) {
      const result = await client.POST('/sessions/refresh', {
        body: { refreshToken },
      })
      if (result.error !== undefined) {
        throw new Error(
          `Identity token refresh failed (HTTP ${result.response.status})`,
        )
      }
      return result.data
    },
  }
}
