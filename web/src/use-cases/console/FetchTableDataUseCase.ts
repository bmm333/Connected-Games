import type { DataTableRepository } from '@domain/console/DataTableRepository'
import type { TableData, TableQuery } from '@domain/console/TableData'

export type FetchTableDataUseCase = (query: TableQuery) => Promise<TableData>

export function createFetchTableDataUseCase(
  repository: DataTableRepository,
): FetchTableDataUseCase {
  return (query) => {
    const table = query.table.trim()
    if (table.length === 0) {
      throw new Error('Table name cannot be empty')
    }

    if (!Number.isInteger(query.page) || query.page < 1) {
      throw new Error('Table page must be a positive integer')
    }

    if (
      !Number.isInteger(query.pageSize) ||
      query.pageSize < 1 ||
      query.pageSize > 100
    ) {
      throw new Error('Table page size must be an integer between 1 and 100')
    }

    return repository.fetchTableData({ ...query, table })
  }
}
