import { Navigate, useLocation } from 'react-router'
import type { Permission, Role } from '@domain/console/auth/Role'
import { useConsoleAuth } from '../auth/useConsoleAuth'

interface RoleProtectedRouteProps {
  permission?: Permission
  roles?: Role[]
  children: React.ReactNode
}

export function RoleProtectedRoute({
  permission,
  roles,
  children,
}: RoleProtectedRouteProps) {
  const location = useLocation()
  const { session, isLoading, canAccess } = useConsoleAuth()

  if (isLoading) {
    return <p className="p-8">Verifica sessione…</p>
  }
  if (session === undefined) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (
    (roles !== undefined && !roles.includes(session.role)) ||
    (permission !== undefined && !canAccess(permission))
  ) {
    return (
      <section className="mx-auto max-w-xl space-y-3 p-8 text-center">
        <p className="text-sm font-semibold text-[var(--cg-color-status-danger)]">
          Errore 403
        </p>
        <h1 className="text-2xl font-bold">Accesso negato</h1>
        <p className="text-[var(--cg-color-label-secondary)]">
          Il ruolo corrente non dispone dei permessi richiesti.
        </p>
      </section>
    )
  }
  return <>{children}</>
}
