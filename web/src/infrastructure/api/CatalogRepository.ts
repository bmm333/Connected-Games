import createClient from 'openapi-fetch'
import type { CatalogRepository } from '@domain/catalog/CatalogRepository'
import type { TableLookup } from '@domain/catalog/TableLookup'
import type { paths } from './generated/catalog'

type TableLookupDto = paths['/tables/by-code/{code}']['get']['responses'][200]['content']['application/json']

function mapTableLookup(dto: TableLookupDto): TableLookup {
  return {
    table: { ...dto.table },
    venue: { ...dto.venue },
    gameType: { ...dto.gameType },
  }
}

export function createCatalogRepository(
  baseUrl = import.meta.env.VITE_API_URL ?? '/api/catalog',
): CatalogRepository {
  const client = createClient<paths>({ baseUrl })

  return {
    async resolveTableByCode(code) {
      const result = await client.GET('/tables/by-code/{code}', {
        params: { path: { code } },
      })

      if (result.error !== undefined) {
        throw new Error(
          `Unable to resolve table code (HTTP ${result.response.status})`,
        )
      }

      return mapTableLookup(result.data)
    },
  }
}
