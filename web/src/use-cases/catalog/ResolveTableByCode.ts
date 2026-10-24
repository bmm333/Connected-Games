import type { CatalogRepository } from '@domain/catalog/CatalogRepository'
import type { TableLookup } from '@domain/catalog/TableLookup'

export type ResolveTableByCode = (code: string) => Promise<TableLookup>

export function createResolveTableByCode(
  repository: CatalogRepository,
): ResolveTableByCode {
  return (code) => {
    const normalizedCode = code.trim()
    if (normalizedCode.length === 0) {
      throw new Error('Table code cannot be empty')
    }

    return repository.resolveTableByCode(normalizedCode)
  }
}
