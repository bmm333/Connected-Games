export interface QrScannerRepository {
  isAvailable(): Promise<boolean>
  scan(): Promise<string>
  stop(): Promise<void>
}
