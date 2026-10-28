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

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function requiredString(value: unknown, field: string): string {
  if (typeof value !== 'string' || value.trim().length === 0) {
    throw new Error(`Console auth response has invalid ${field}`)
  }
  return value
}

export function mapConsoleUserSession(value: unknown): UserSession {
  if (!isRecord(value) || !isRecord(value.user) || !isRecord(value.token)) {
    throw new Error('Console auth response has invalid session')
  }

  const role = value.role
  const activePermissions = value.permissions
  if (
    typeof role !== 'string' ||
    !roles.includes(role as Role) ||
    !Array.isArray(activePermissions) ||
    !activePermissions.every(
      (permission): permission is Permission =>
        typeof permission === 'string' &&
        permissions.includes(permission as Permission),
    )
  ) {
    throw new Error('Console auth response has invalid role or permissions')
  }

  return {
    user: {
      id: requiredString(value.user.id, 'user.id'),
      name: requiredString(value.user.name, 'user.name'),
      email: requiredString(value.user.email, 'user.email'),
      ...(value.user.avatar === undefined
        ? {}
        : { avatar: requiredString(value.user.avatar, 'user.avatar') }),
    },
    role: role as Role,
    permissions: activePermissions,
    token: {
      accessToken: requiredString(value.token.accessToken, 'token.accessToken'),
      ...(value.token.refreshToken === undefined
        ? {}
        : {
            refreshToken: requiredString(
              value.token.refreshToken,
              'token.refreshToken',
            ),
          }),
    },
    expiresAt: requiredString(value.expiresAt, 'expiresAt'),
  }
}
