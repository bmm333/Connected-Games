import { createAuthRepository } from '@infrastructure/api/AuthRepository'
import { createQrScannerRepository } from '@infrastructure/qr/QrScannerRepository'
import { createSessionRepository } from '@infrastructure/storage/SessionRepository'
import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createAuthenticateWithQrCode } from '@use-cases/auth/AuthenticateWithQrCodeUseCase'
import { createGetStoredSession } from '@use-cases/auth/GetStoredSessionUseCase'
import { createPairDevice } from '@use-cases/auth/PairDeviceUseCase'
import type { KeyValueStore } from '@domain/storage/KeyValueStore'

/**
 * @param sharedStore — optional KV store to reuse instead of creating a
 *   redundant second instance. When omitted a fresh store is created so
 *   call-sites that do not have one yet keep working.
 */
export function createAuthDependencies(sharedStore?: KeyValueStore) {
  const store = sharedStore ?? createPlatformKeyValueStore()
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
