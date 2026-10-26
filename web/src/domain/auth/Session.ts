export type SurfaceType = 'console' | 'player' | 'kiosk'

export type SessionKind = 'guest' | 'user' | 'device'

export interface AuthToken {
  accessToken: string
  refreshToken?: string
  expiresAt?: string
}

export interface DeviceCredentials {
  deviceId: string
  surface: Exclude<SurfaceType, 'console'>
}

export interface Session {
  id: string
  kind: SessionKind
  token: AuthToken
  surface: SurfaceType
  device?: DeviceCredentials
  createdAt: string
}
