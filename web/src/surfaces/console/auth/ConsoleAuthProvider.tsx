import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import type { ReactNode } from 'react'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'
import type { UserSession } from '@domain/console/auth/UserSession'
import { useConsoleDependencies } from '../dependencies'
import {
  ConsoleAuthContext,
  type ConsoleAuthContextValue,
} from './ConsoleAuthContext'

export function ConsoleAuthProvider({ children }: { children: ReactNode }) {
  const { consoleAuth } = useConsoleDependencies()
  const queryClient = useQueryClient()
  const sessionQuery = useQuery({
    queryKey: ['console-auth', 'session'],
    queryFn: async () => (await consoleAuth.getCurrentSession()) ?? null,
    staleTime: 30_000,
    retry: false,
  })
  const loginMutation = useMutation<UserSession, Error, ConsoleLoginRequest>({
    mutationFn: (request) => consoleAuth.login(request),
    onSuccess: (session) => {
      queryClient.setQueryData(['console-auth', 'session'], session)
    },
  })
  const logoutMutation = useMutation<void, Error, void>({
    mutationFn: () => consoleAuth.logout(),
    onSuccess: () => {
      queryClient.setQueryData(['console-auth', 'session'], null)
    },
  })

  const value: ConsoleAuthContextValue = {
    session: sessionQuery.data ?? undefined,
    isLoading: sessionQuery.isPending,
    isAuthenticated: sessionQuery.data !== undefined && sessionQuery.data !== null,
    login: (request) => loginMutation.mutateAsync(request),
    logout: () => logoutMutation.mutateAsync(),
    canAccess: (permission) =>
      sessionQuery.data !== undefined &&
      sessionQuery.data !== null &&
      consoleAuth.rolePermissionService.hasPermission(
        sessionQuery.data,
        permission,
      ),
  }

  return (
    <ConsoleAuthContext.Provider value={value}>
      {children}
    </ConsoleAuthContext.Provider>
  )
}
