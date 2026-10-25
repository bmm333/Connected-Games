import { useEffect, useState, type FormEvent } from 'react'
import { ScanLine, ShieldCheck } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Button, Card, Input } from '@ui'
import type { DeviceCredentials, SurfaceType } from '@domain/auth/Session'
import type { QrScannerRepository } from '@domain/qr/QrScannerRepository'
import type { AuthenticateWithQrCode } from '@use-cases/auth/AuthenticateWithQrCodeUseCase'
import type { PairDevice } from '@use-cases/auth/PairDeviceUseCase'
import { useQrLogin } from './useQrLogin'

interface QrPageProps {
  surface: SurfaceType
  scanner: QrScannerRepository
  authenticateWithQrCode: AuthenticateWithQrCode
  pairDevice: PairDevice
  destination: string
}

export function QrPage({
  surface,
  scanner,
  authenticateWithQrCode,
  pairDevice,
  destination,
}: QrPageProps) {
  const navigate = useNavigate()
  const [code, setCode] = useState('')
  const [scannerAvailable, setScannerAvailable] = useState(false)
  const [scanError, setScanError] = useState<string | undefined>()
  const [deviceId, setDeviceId] = useState('')
  const isDeviceSurface = surface === 'player' || surface === 'kiosk'
  const login = useQrLogin(authenticateWithQrCode, pairDevice)

  useEffect(() => {
    let active = true
    void scanner.isAvailable().then((available) => {
      if (active) setScannerAvailable(available)
    })
    return () => {
      active = false
      void scanner.stop()
    }
  }, [scanner])

  useEffect(() => {
    if (login.isSuccess) {
      navigate(destination, { replace: true })
    }
  }, [destination, login.isSuccess, navigate])

  async function scanQrCode() {
    setScanError(undefined)
    try {
      setCode(await scanner.scan())
    } catch (error) {
      setScanError(error instanceof Error ? error.message : 'QR scan failed')
    }
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (isDeviceSurface) {
      const credentials: DeviceCredentials = {
        deviceId,
        surface,
      }
      login.mutate({ mode: 'pair', code, credentials })
      return
    }
    login.mutate({ mode: 'authenticate', code })
  }

  const error = scanError ?? (login.error instanceof Error ? login.error.message : undefined)

  return (
    <main className="min-h-screen flex items-center justify-center p-6">
      <section className="w-full max-w-md space-y-8">
        <header className="text-center space-y-3">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[var(--cg-color-accent-tinted)] text-[var(--cg-color-accent-label)]">
            <ShieldCheck size={28} />
          </div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--cg-color-accent-label)]">
            Connected Games
          </p>
          <h1 className="text-3xl font-bold tracking-tight">Accedi con QR Code</h1>
          <p className="text-sm text-neutral-400">
            Scansiona il codice per configurare rapidamente questa {surface}.
          </p>
        </header>

        <Card elevated className="space-y-6">
          <Button
            type="button"
            onClick={() => void scanQrCode()}
            disabled={!scannerAvailable || login.isLoading}
            className="flex min-h-44 w-full flex-col items-center justify-center gap-3 border-2 border-dashed border-[var(--cg-color-accent)] bg-[var(--cg-color-accent-tinted)] text-[var(--cg-color-accent-label)]"
          >
            <ScanLine size={42} />
            <span className="font-semibold">
              {scannerAvailable ? 'Inquadra il QR Code' : 'Fotocamera non disponibile'}
            </span>
            <span className="text-xs text-neutral-500">Puoi usare il codice manuale qui sotto</span>
          </Button>

          <form onSubmit={submit} className="space-y-4">
            <Input
              label="Codice di associazione"
              value={code}
              onChange={(event) => setCode(event.target.value)}
              placeholder="Inserisci il codice"
              autoComplete="one-time-code"
            />

            {isDeviceSurface && (
              <Input
                label="ID dispositivo"
                value={deviceId}
                onChange={(event) => setDeviceId(event.target.value)}
                placeholder={`ID ${surface}`}
              />
            )}

            {error !== undefined && (
              <p role="alert" className="rounded-lg bg-[color-mix(in_srgb,var(--cg-color-status-destructive)_16%,transparent)] px-3 py-2 text-sm text-[var(--cg-color-status-destructive)]">
                {error}
              </p>
            )}

            <Button
              type="submit"
              disabled={code.trim().length === 0 || login.isLoading}
              className="w-full"
            >
              {login.isLoading ? 'Autenticazione in corso...' : 'Continua'}
            </Button>
          </form>
        </Card>
      </section>
    </main>
  )
}
