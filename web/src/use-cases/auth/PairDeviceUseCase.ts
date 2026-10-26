import type { AuthRepository } from '@domain/auth/AuthRepository'
import type { SessionRepository } from '@domain/auth/SessionRepository'
import type {
  DeviceCredentials,
  Session,
} from '@domain/auth/Session'

export type PairDevice = (
  code: string,
  credentials: DeviceCredentials,
) => Promise<Session>

export function createPairDevice(
  authRepository: AuthRepository,
  sessionRepository: SessionRepository,
): PairDevice {
  return async (code, credentials) => {
    const normalizedCode = code.trim()
    if (normalizedCode.length === 0) {
      throw new Error('Pairing code cannot be empty')
    }

    if (credentials.deviceId.trim().length === 0) {
      throw new Error('Device ID cannot be empty')
    }

    if (credentials.surface !== 'player' && credentials.surface !== 'kiosk') {
      throw new Error('Device surface must be player or kiosk')
    }

    const session = await authRepository.pairDevice(
      normalizedCode,
      credentials,
    )
    await sessionRepository.saveSession(session)
    return session
  }
}
