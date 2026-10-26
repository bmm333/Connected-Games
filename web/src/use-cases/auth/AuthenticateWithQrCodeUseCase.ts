import type { AuthRepository } from '@domain/auth/AuthRepository'
import type { SessionRepository } from '@domain/auth/SessionRepository'
import type { Session } from '@domain/auth/Session'

export type AuthenticateWithQrCode = (payload: string) => Promise<Session>

export function createAuthenticateWithQrCode(
  authRepository: AuthRepository,
  sessionRepository: SessionRepository,
): AuthenticateWithQrCode {
  return async (payload) => {
    const normalizedPayload = payload.trim()
    if (normalizedPayload.length === 0) {
      throw new Error('QR code payload cannot be empty')
    }

    const session = await authRepository.authenticateWithQrCode(
      normalizedPayload,
    )
    await sessionRepository.saveSession(session)
    return session
  }
}
