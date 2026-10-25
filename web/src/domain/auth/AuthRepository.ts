import type { DeviceCredentials, Session } from './Session'

export interface AuthRepository {
  authenticateWithQrCode(payload: string): Promise<Session>
  pairDevice(code: string, credentials: DeviceCredentials): Promise<Session>
}
