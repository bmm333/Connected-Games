import { describe, expect, it, vi } from 'vitest'
import { createCatalogRepository } from './CatalogRepository'

const venueId = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'
const gameTypeId = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f12'
const tableId = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f13'

describe('CatalogRepository', () => {
  it('calls the Catalog contract paths using the configured API base URL', async () => {
    const fetcher = vi.fn<typeof fetch>(async (input) => {
      const url = String(input)
      const body = url.endsWith('/tables/by-code/DEMO01')
        ? {
          table: {
            id: tableId,
            venueId,
            name: 'Table',
            code: 'DEMO01',
            gameTypeId,
            status: 'FREE',
          },
          venue: { id: venueId, name: 'Venue', city: 'Rome', type: 'PUBLIC' },
          gameType: {
            id: gameTypeId,
            name: 'Foosball',
            mode: 'INDIVIDUAL',
            participantsPerSide: 1,
            pointsToWin: 5,
          },
        }
        : url.endsWith(`/venues/${venueId}`)
          ? {
            id: venueId,
            name: 'Venue',
            city: 'Rome',
            type: 'PUBLIC',
            address: 'Via Roma',
            organizationId: venueId,
          }
          : []
      return new Response(JSON.stringify(body), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    })
    const repository = createCatalogRepository('https://api.example.test/api/catalog', fetcher)

    await repository.resolveTableByCode('DEMO01')
    await repository.listVenues()
    await repository.getVenue(venueId)
    await repository.listTables(venueId)
    await repository.listGameTypes()

    expect(fetcher.mock.calls.map(([input]) =>
      input instanceof Request ? input.url : String(input),
    )).toEqual([
      'https://api.example.test/api/catalog/tables/by-code/DEMO01',
      'https://api.example.test/api/catalog/venues',
      `https://api.example.test/api/catalog/venues/${venueId}`,
      `https://api.example.test/api/catalog/venues/${venueId}/tables`,
      'https://api.example.test/api/catalog/game-types',
    ])
  })
})
