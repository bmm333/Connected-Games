import type { GameType, Table, TableLookup, Venue, VenueSummary } from './TableLookup'

export interface CatalogRepository {
  resolveTableByCode(code: string): Promise<TableLookup>
}

export interface SimulatorCatalogRepository extends CatalogRepository {
  listVenues(): Promise<readonly VenueSummary[]>
  getVenue(venueId: string): Promise<Venue>
  listTables(venueId: string): Promise<readonly Table[]>
  listGameTypes(): Promise<readonly GameType[]>
}
