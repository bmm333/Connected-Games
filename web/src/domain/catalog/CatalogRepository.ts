import type { GameType, Table, TableLookup, VenueSummary } from './TableLookup'

export interface CatalogRepository {
  resolveTableByCode(code: string): Promise<TableLookup>
}

export interface SimulatorCatalogRepository extends CatalogRepository {
  listVenues(): Promise<readonly VenueSummary[]>
  listTables(venueId: string): Promise<readonly Table[]>
  listGameTypes(): Promise<readonly GameType[]>
}
