import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'
import type { RolePermissionService } from '@domain/console/auth/RolePermissionService'
import type { UserSession } from '@domain/console/auth/UserSession'
import { createValidateRouteAccessUseCase } from './ValidateRouteAccessUseCase'

const session = (role: UserSession['role'], permissions: UserSession['permissions']): UserSession => ({
  user: { id: 'user-1', name: 'Operator', email: 'operator@example.com' },
  role,
  permissions,
  token: { accessToken: 'access-token' },
  expiresAt: '2027-01-01T00:00:00.000Z',
})

function createSubject(currentSession: UserSession | undefined) {
  const store: ConsoleSessionStore = {
    get: vi.fn().mockResolvedValue(currentSession),
    save: vi.fn(),
    clear: vi.fn(),
  }
  const permissionService: RolePermissionService = {
    hasPermission: vi.fn((activeSession, permission) =>
      activeSession.permissions.includes(permission),
    ),
  }
  return {
    validate: createValidateRouteAccessUseCase(store, permissionService),
    permissionService,
  }
}

describe('createValidateRouteAccessUseCase', () => {
  it('allows an admin to access device management', async () => {
    const { validate } = createSubject(session('admin', ['devices:write']))

    await expect(validate({ permission: 'devices:write' })).resolves.toBe(true)
  })

  it('denies an operator without the required device permission', async () => {
    const { validate } = createSubject(session('operator', ['overview:view']))

    await expect(validate({ permission: 'devices:write' })).resolves.toBe(false)
  })

  it('denies a role not included in the route requirement', async () => {
    const { validate } = createSubject(session('operator', ['tables:read']))

    await expect(
      validate({ roles: ['admin'], permission: 'tables:read' }),
    ).resolves.toBe(false)
  })

  it('denies access when there is no active session', async () => {
    const { validate } = createSubject(undefined)

    await expect(validate({ permission: 'overview:view' })).resolves.toBe(false)
  })
})
