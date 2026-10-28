import type { Permission, Role } from '@domain/console/auth/Role'
import type { UserSession } from '@domain/console/auth/UserSession'

const roles: readonly Role[] = [
  'admin',
  'operator',
  'manager',
  'auditor',
  'viewer',
]

const permissions: readonly Permission[] = [
  'overview:view',
  'tables:read',
  'tables:write',
  'devices:read',
  'devices:write',
]

function isNonEmptyString(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0
}

function isRole(value: unknown): value is Role {
  return typeof value === 'string' && roles.includes(value as Role)
}

function isPermission(value: unknown): value is Permission {
  return typeof value === 'string' && permissions.includes(value as Permission)
}

export function validateUserSession(session: UserSession): UserSession {
  if (
    !session ||
    !session.user ||
    !isNonEmptyString(session.user.id) ||
    !isNonEmptyString(session.user.name) ||
    !isNonEmptyString(session.user.email) ||
    !isRole(session.role) ||
    !session.token ||
    !isNonEmptyString(session.token.accessToken) ||
    !Array.isArray(session.permissions) ||
    !session.permissions.every(isPermission) ||
    !isNonEmptyString(session.expiresAt)
  ) {
    throw new Error('Console authentication returned an invalid session')
  }

  const expiresAt = Date.parse(session.expiresAt)
  if (!Number.isFinite(expiresAt) || expiresAt <= Date.now()) {
    throw new Error('Console authentication returned an expired session')
  }

  return session
}
