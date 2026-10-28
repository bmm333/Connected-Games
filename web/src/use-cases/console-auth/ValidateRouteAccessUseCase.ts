import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'
import type { Permission, Role } from '@domain/console/auth/Role'
import type { RolePermissionService } from '@domain/console/auth/RolePermissionService'

export interface RouteAccessRequirement {
  permission?: Permission
  roles?: Role[]
}

export type ValidateRouteAccessUseCase = (
  requirement: RouteAccessRequirement,
) => Promise<boolean>

export function createValidateRouteAccessUseCase(
  sessionStore: ConsoleSessionStore,
  rolePermissionService: RolePermissionService,
): ValidateRouteAccessUseCase {
  return async ({ permission, roles }) => {
    const session = await sessionStore.get()
    if (session === undefined) return false

    if (roles !== undefined && !roles.includes(session.role)) {
      return false
    }

    return permission === undefined
      ? true
      : rolePermissionService.hasPermission(session, permission)
  }
}
