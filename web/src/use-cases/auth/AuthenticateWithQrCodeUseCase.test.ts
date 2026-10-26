import type { AuthRepository } from '@domain/auth/AuthRepository'
import type { SessionRepository } from '@domain/auth/SessionRepository'
import { createAuthenticateWithQrCode } from './AuthenticateWithQrCodeUseCase'

const session = {
  id: 'session-1',
  kind: 'guest' as const,
  token: { accessToken: 'access-token' },
  surface: 'console' as const,
  createdAt: '2026-10-25T00:00:00.000Z',
}

describe('createAuthenticateWithQrCode', () => {
  it('normalizes the payload, authenticates, and stores the session', async () => {
    const authRepository: AuthRepository = {
      authenticateWithQrCode: vi.fn().mockResolvedValue(session),
      pairDevice: vi.fn(),
    }
    const sessionRepository: SessionRepository = {
      getStoredSession: vi.fn(),
      saveSession: vi.fn().mockResolvedValue(undefined),
      clearSession: vi.fn(),
    }

    const result = await createAuthenticateWithQrCode(
      authRepository,
      sessionRepository,
    )('  qr-payload  ')

    expect(result).toEqual(session)
    expect(authRepository.authenticateWithQrCode).toHaveBeenCalledWith(
      'qr-payload',
    )
    expect(sessionRepository.saveSession).toHaveBeenCalledWith(session)
  })

  it('rejects an empty payload without calling dependencies', async () => {
    const authRepository: AuthRepository = {
      authenticateWithQrCode: vi.fn(),
      pairDevice: vi.fn(),
    }
    const sessionRepository: SessionRepository = {
      getStoredSession: vi.fn(),
      saveSession: vi.fn(),
      clearSession: vi.fn(),
    }

    await expect(
      createAuthenticateWithQrCode(authRepository, sessionRepository)('  '),
    ).rejects.toThrow('QR code payload cannot be empty')

    expect(authRepository.authenticateWithQrCode).not.toHaveBeenCalled()
    expect(sessionRepository.saveSession).not.toHaveBeenCalled()
  })
})
