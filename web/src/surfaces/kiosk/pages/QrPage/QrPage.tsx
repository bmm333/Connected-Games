import { QrPage as SharedQrPage } from '@surfaces/shared/pages/QrPage/QrPage'
import { createKioskDependencies } from '../../composition-root'

const dependencies = createKioskDependencies()

export function KioskQrPage() {
  return (
    <SharedQrPage
      surface="kiosk"
      destination="/attract"
      scanner={dependencies.auth.scanner}
      authenticateWithQrCode={dependencies.auth.authenticateWithQrCode}
      pairDevice={dependencies.auth.pairDevice}
    />
  )
}
