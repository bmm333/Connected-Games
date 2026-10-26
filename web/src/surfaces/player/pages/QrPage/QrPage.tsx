import { QrPage as SharedQrPage } from '@surfaces/shared/pages/QrPage/QrPage'
import { createPlayerDependencies } from '../../composition-root'

const dependencies = createPlayerDependencies()

export function PlayerQrPage() {
  return (
    <SharedQrPage
      surface="player"
      destination="/lobby"
      scanner={dependencies.auth.scanner}
      authenticateWithQrCode={dependencies.auth.authenticateWithQrCode}
      pairDevice={dependencies.auth.pairDevice}
    />
  )
}
