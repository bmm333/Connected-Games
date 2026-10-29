import type { OverviewRepository } from '@domain/console/OverviewRepository'
import { mapSystemOverview } from './mappers'
import type { ConsoleApiClient } from './ConsoleApiClient'

export function createOverviewRepository(
  client: ConsoleApiClient,
): OverviewRepository {
  return {
    async getLocalOverview() {
      return mapSystemOverview(await client.getOverview())
    },
  }
}
