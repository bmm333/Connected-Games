export type TableCellValue = string | number | boolean | null

export interface TableColumn {
  key: string
  label: string
}

export interface TableRow {
  id: string
  values: Record<string, TableCellValue>
}

export interface TableData {
  columns: TableColumn[]
  rows: TableRow[]
  total: number
}

export type TableSortDirection = 'asc' | 'desc'

export interface TableSort {
  column: string
  direction: TableSortDirection
}

export interface TableFilter {
  column: string
  value: string
}

export interface TableQuery {
  table: string
  page: number
  pageSize: number
  sort?: TableSort
  filters?: TableFilter[]
}
