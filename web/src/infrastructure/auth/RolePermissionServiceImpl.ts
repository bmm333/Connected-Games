import type { Permission } from '@domain/console/auth/Role'
import type { RolePermissionService } from '@domain/console/auth/RolePermissionService'

export const rolePermissionService: RolePermissionService = {
  hasPermission(session, permission: Permission) {
    return session.permissions.includes(permission)
  },
}
