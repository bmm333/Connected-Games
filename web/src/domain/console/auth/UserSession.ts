import type { Permission, Role } from './Role'
import type { User } from './User'

export interface UserSessionToken {
  accessToken: string
  refreshToken?: string
}

export interface UserSession {
  user: User
  role: Role
  permissions: Permission[]
  token: UserSessionToken
  expiresAt: string
}
