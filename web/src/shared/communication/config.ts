export interface CommunicationConfig {
  readonly useMocks: boolean
  readonly apiBaseUrl: string
  readonly sseUrl?: string
  readonly mqttUrl?: string
}

export const communicationRoutes = {
  catalog: '/api/catalog',
  match: '/api/match',
  stats: '/api/stats',
  identity: '/api/identity',
  matchCurrent: (tableId: string) =>
    `/tables/${encodeURIComponent(tableId)}/current-match`,
  matchLive: (tableId: string) =>
    `/tables/${encodeURIComponent(tableId)}/live`,
  mqttMatchEvents: (venueId: string, tableId: string, matchId: string) =>
    `venues/${encodeURIComponent(venueId)}/tables/${encodeURIComponent(tableId)}/matches/${encodeURIComponent(matchId)}/events`,
  mqttHubAck: (venueId: string) =>
    `venues/${encodeURIComponent(venueId)}/hub/ack`,
  mqttMatchCompleted: 'platform/matches/completed',
} as const

function normalizeBaseUrl(value: string | undefined): string {
  return (value ?? '').replace(/\/+$/, '')
}

function parseUseMocks(value: string | undefined): boolean {
  if (value === undefined || value === '') return true
  if (value === 'true') return true
  if (value === 'false') return false
  throw new Error('VITE_USE_MOCKS must be "true" or "false"')
}

export function readCommunicationConfig(
  env: Record<string, string | undefined>,
): CommunicationConfig {
  return {
    useMocks: parseUseMocks(env.VITE_USE_MOCKS),
    apiBaseUrl: normalizeBaseUrl(env.VITE_API_BASE_URL),
    sseUrl: env.VITE_SSE_URL ? normalizeBaseUrl(env.VITE_SSE_URL) : undefined,
    mqttUrl: env.VITE_MQTT_URL || undefined,
  }
}

export const communicationConfig = readCommunicationConfig(import.meta.env)

export function apiUrl(baseUrl: string, path: string): string {
  return `${baseUrl}${path.startsWith('/') ? path : `/${path}`}`
}

