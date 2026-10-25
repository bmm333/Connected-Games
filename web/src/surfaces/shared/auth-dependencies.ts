import { createAuthRepository } from '@infrastructure/api/AuthRepository'
import { createQrScannerRepository } from '@infrastructure/qr/QrScannerRepository'
import { createSessionRepository } from '@infrastructure/storage/SessionRepository'
import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createAuthenticateWithQrCode } from '@use-cases/auth/AuthenticateWithQrCodeUseCase'
import { createGetStoredSession } from '@use-cases/auth/GetStoredSessionUseCase'
import { createPairDevice } from '@use-cases/auth/PairDeviceUseCase'

export function createAuthDependencies() {
  const store = createPlatformKeyValueStore()
  const authRepository = createAuthRepository()
  const sessionRepository = createSessionRepository(store)

  return {
    scanner: createQrScannerRepository(),
    authenticateWithQrCode: createAuthenticateWithQrCode(
      authRepository,
      sessionRepository,
    ),
    pairDevice: createPairDevice(authRepository, sessionRepository),
    getStoredSession: createGetStoredSession(sessionRepository),
  }
}
