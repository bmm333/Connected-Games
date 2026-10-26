import { QrPage as SharedQrPage } from '@surfaces/shared/pages/QrPage/QrPage'
import { createConsoleDependencies } from '../../composition-root'

const dependencies = createConsoleDependencies()

export function ConsoleQrPage() {
  return (
    <SharedQrPage
      surface="console"
      destination="/dashboard"
      scanner={dependencies.auth.scanner}
      authenticateWithQrCode={dependencies.auth.authenticateWithQrCode}
      pairDevice={dependencies.auth.pairDevice}
    />
  )
}
