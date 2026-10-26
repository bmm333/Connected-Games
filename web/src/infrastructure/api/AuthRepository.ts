import type { AuthRepository as DomainAuthRepository } from '@domain/auth/AuthRepository'
import type {
  DeviceCredentials,
  Session,
  SurfaceType,
} from '@domain/auth/Session'

interface AuthTokenDto {
  accessToken?: string
  access_token?: string
  refreshToken?: string
  refresh_token?: string
  expiresAt?: string
  expires_at?: string
}

interface SessionDto extends AuthTokenDto {
  id?: string
  sessionId?: string
  session_id?: string
  kind?: Session['kind']
  surface?: SurfaceType
  device?: DeviceCredentials
  createdAt?: string
  created_at?: string
  token?: AuthTokenDto
}

function mapSession(dto: SessionDto): Session {
  const token = dto.token ?? dto
  const accessToken = token.accessToken ?? token.access_token
  if (accessToken === undefined || accessToken.trim().length === 0) {
    throw new Error('Authentication response did not contain an access token')
  }

  return {
    id: dto.id ?? dto.sessionId ?? dto.session_id ?? crypto.randomUUID(),
    kind: dto.kind ?? (dto.device === undefined ? 'guest' : 'device'),
    token: {
      accessToken,
      ...(token.refreshToken ?? token.refresh_token
        ? { refreshToken: token.refreshToken ?? token.refresh_token }
        : {}),
      ...(token.expiresAt ?? token.expires_at
        ? { expiresAt: token.expiresAt ?? token.expires_at }
        : {}),
    },
    surface: dto.surface ?? 'console',
    ...(dto.device === undefined ? {} : { device: dto.device }),
    createdAt: dto.createdAt ?? dto.created_at ?? new Date().toISOString(),
  }
}

async function postJson(
  url: string,
  body: unknown,
): Promise<SessionDto> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw new Error(`Authentication request failed (HTTP ${response.status})`)
  }

  return (await response.json()) as SessionDto
}

export function createAuthRepository(
  baseUrl = import.meta.env.VITE_AUTH_API_URL ?? '/api/auth',
): DomainAuthRepository {
  return {
    async authenticateWithQrCode(payload) {
      return mapSession(await postJson(`${baseUrl}/qr`, { payload }))
    },
    async pairDevice(code, credentials) {
      return mapSession(
        await postJson(`${baseUrl}/devices/pair`, { code, credentials }),
      )
    },
  }
}
