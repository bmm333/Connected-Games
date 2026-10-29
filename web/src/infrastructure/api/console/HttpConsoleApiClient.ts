import type { DeviceCommand } from '@domain/console/Device'
import type { TableQuery } from '@domain/console/TableData'
import type { ConsoleApiClient } from './ConsoleApiClient'

async function request(
  baseUrl: string,
  path: string,
  init?: RequestInit,
): Promise<unknown> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers: { Accept: 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    throw new Error(`Console API request failed (HTTP ${response.status})`)
  }
  return response.status === 204 ? undefined : response.json()
}

function encodeQuery(query: TableQuery): string {
  const params = new URLSearchParams({
    page: String(query.page),
    pageSize: String(query.pageSize),
  })
  if (query.sort) {
    params.set('sort', query.sort.column)
    params.set('direction', query.sort.direction)
  }
  for (const filter of query.filters ?? []) {
    params.append(`filter[${filter.column}]`, filter.value)
  }
  return params.toString()
}

export function createHttpConsoleApiClient(
  baseUrl = import.meta.env.VITE_CONSOLE_API_URL ?? '/api/console',
): ConsoleApiClient {
  return {
    getOverview: () => request(baseUrl, '/overview'),
    getDevices: () => request(baseUrl, '/devices'),
    getTableData: (query) =>
      request(
        baseUrl,
        `/tables/${encodeURIComponent(query.table)}?${encodeQuery(query)}`,
      ),
    executeDeviceCommand: (deviceId: string, command: DeviceCommand) =>
      request(`${baseUrl}`, `/devices/${encodeURIComponent(deviceId)}/commands`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ command }),
      }).then(() => undefined),
  }
}
