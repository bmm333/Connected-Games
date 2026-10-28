import { Capacitor } from '@capacitor/core'
import { Network } from '@capacitor/network'
import type { MqttClientService } from '@domain/simulator'

export function attachNetworkRecovery(
  service: MqttClientService,
  reconnect: () => Promise<void>,
): () => void {
  if (!Capacitor.isNativePlatform()) return () => undefined

  const listener = Network.addListener('networkStatusChange', async ({ connected }) => {
    if (connected && !service.isConnected()) await reconnect()
  })

  return () => {
    void listener.then((handle) => handle.remove())
  }
}
