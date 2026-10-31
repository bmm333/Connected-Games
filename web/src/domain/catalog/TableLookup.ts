export interface VenueSummary {
  readonly id: string
  readonly name: string
  readonly city: string
  readonly type: 'PUBLIC' | 'PRIVATE'
}

export interface Venue extends VenueSummary {
  readonly address: string
  readonly organizationId: string
}

export interface Table {
  readonly id: string
  readonly venueId: string
  readonly name: string
  readonly code: string
  readonly gameTypeId: string
  readonly status: 'FREE' | 'OCCUPIED' | 'OFFLINE' | 'MAINTENANCE'
}

export interface GameType {
  readonly id: string
  readonly name: string
  readonly mode: 'INDIVIDUAL' | 'TEAM'
  readonly participantsPerSide: number
  readonly pointsToWin: number
}

export interface TableLookup {
  readonly table: Table
  readonly venue: VenueSummary
  readonly gameType: GameType
}
