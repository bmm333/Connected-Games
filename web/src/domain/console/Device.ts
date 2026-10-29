export type SurfaceType = 'kiosk' | 'player' | 'console'
export type DeviceStatus = 'online' | 'offline'

export interface Device {
  id: string
  name: string
  surfaceType: SurfaceType
  status: DeviceStatus
  lastPing: string | null
  batteryPercent?: number
  hardwareInfo?: Record<string, string>
}

export type DeviceCommand = 'reboot' | 'sync' | 'lock'
