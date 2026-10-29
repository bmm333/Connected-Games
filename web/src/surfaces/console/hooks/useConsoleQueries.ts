import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { TableQuery } from '@domain/console/TableData'
import { useConsoleDependencies } from '../dependencies'

export function useLocalOverview() {
  const { overview } = useConsoleDependencies()
  return useQuery({
    queryKey: ['console', 'overview'],
    queryFn: overview,
    staleTime: 15_000,
    refetchInterval: 30_000,
  })
}

export function useDataTable(query: TableQuery) {
  const { tableData } = useConsoleDependencies()
  return useQuery({
    queryKey: ['console', 'table', query],
    queryFn: () => tableData(query),
    staleTime: 30_000,
    placeholderData: (previous) => previous,
  })
}

export function useDevicesList() {
  const { devices } = useConsoleDependencies()
  return useQuery({
    queryKey: ['console', 'devices'],
    queryFn: devices,
    staleTime: 10_000,
    refetchInterval: 15_000,
  })
}

export function useExecuteDeviceCommand() {
  const { executeDeviceCommand } = useConsoleDependencies()
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ deviceId, command }: Parameters<typeof executeDeviceCommand> extends [
      string,
      infer Command,
    ]
      ? { deviceId: string; command: Command }
      : never) => executeDeviceCommand(deviceId, command),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['console', 'devices'] }),
  })
}
