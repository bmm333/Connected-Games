import { useQuery } from '@tanstack/react-query'
import type { TableLookup } from '@domain/catalog/TableLookup'
import { createPlayerDependencies } from '../composition-root'

const dependencies = createPlayerDependencies()

export function useResolveTableByCode(code: string) {
  return useQuery<TableLookup>({
    queryKey: ['catalog', 'table', code],
    queryFn: () => dependencies.catalog.resolveTableByCode(code),
    enabled: code.trim().length > 0,
  })
}
