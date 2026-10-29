import type { DataTableRepository } from '@domain/console/DataTableRepository'
import type { TableQuery } from '@domain/console/TableData'
import { mapTableData } from './mappers'
import type { ConsoleApiClient } from './ConsoleApiClient'

export function createDataTableRepository(
  client: ConsoleApiClient,
): DataTableRepository {
  return {
    async fetchTableData(query: TableQuery) {
      return mapTableData(await client.getTableData(query))
    },
  }
}
