import type { Permission } from '@domain/console/auth/Role'
import type { ConsoleSessionStore } from '@domain/console/auth/ConsoleSessionStore'

export type GetUserPermissionsUseCase = () => Promise<Permission[]>

export function createGetUserPermissionsUseCase(
  sessionStore: ConsoleSessionStore,
): GetUserPermissionsUseCase {
  return async () => {
    const session = await sessionStore.get()
    return session?.permissions ?? []
  }
}
