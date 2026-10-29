import type { TableData, TableQuery } from './TableData'

export interface DataTableRepository {
  fetchTableData(query: TableQuery): Promise<TableData>
}
