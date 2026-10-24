import type { TableLookup } from './TableLookup'

export interface CatalogRepository {
  resolveTableByCode(code: string): Promise<TableLookup>
}
