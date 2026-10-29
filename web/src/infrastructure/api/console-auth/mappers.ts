import type { components } from '@shared/api/generated/identity'
import type { Permission, Role } from '@domain/console/auth/Role'
import type { UserSession } from '@domain/console/auth/UserSession'

type IdentityUser = components['schemas']['User']
type IdentitySession = components['schemas']['UserSession']
type IdentityRole = components['schemas']['Role']

const roleMap: Record<IdentityRole, Role> = {
  PLAYER: 'player',
  VENUE_MANAGER: 'venue_manager',
  GAME_ADMIN: 'game_admin',
  PLATFORM_ADMIN: 'platform_admin',
}

const permissionsByRole: Record<Role, Permission[]> = {
  player: [],
  venue_manager: ['overview:view', 'tables:read', 'devices:read', 'devices:write'],
  game_admin: ['overview:view', 'tables:read', 'tables:write', 'devices:read'],
  platform_admin: [
    'overview:view',
    'tables:read',
    'tables:write',
    'devices:read',
    'devices:write',
  ],
}

function mapUser(user: IdentityUser) {
  return {
    id: user.id,
    name: user.displayName,
    email: user.email,
  }
}

function roleFromUser(user: IdentityUser): Role {
  const priority: IdentityRole[] = [
    'PLATFORM_ADMIN',
    'GAME_ADMIN',
    'VENUE_MANAGER',
    'PLAYER',
  ]
  const role = priority.find((candidate) =>
    user.roles.some((assignment) => assignment.role === candidate),
  )
  if (role === undefined) {
    throw new Error('Identity response did not contain a Console role')
  }
  return roleMap[role]
}

export function permissionsForRole(role: Role): Permission[] {
  return [...permissionsByRole[role]]
}

export function mapConsoleUserSession(value: unknown): UserSession {
  const session = value as IdentitySession
  if (session.kind !== 'USER') {
    throw new Error('Identity response is not a user session')
  }

  const role = roleFromUser(session.user)
  return {
    user: mapUser(session.user),
    role,
    permissions: permissionsForRole(role),
    token: {
      accessToken: session.accessToken,
      refreshToken: session.refreshToken,
    },
    expiresAt: session.expiresAt,
  }
}

export function mapCurrentUserSession(
  storedSession: UserSession,
  value: unknown,
): UserSession {
  const user = value as IdentityUser
  const role = roleFromUser(user)
  return {
    ...storedSession,
    user: mapUser(user),
    role,
    permissions: permissionsForRole(role),
  }
}
