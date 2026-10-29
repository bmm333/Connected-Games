import type { Permission } from './Role'
import type { UserSession } from './UserSession'

export interface RolePermissionService {
  hasPermission(session: UserSession, permission: Permission): boolean
}
