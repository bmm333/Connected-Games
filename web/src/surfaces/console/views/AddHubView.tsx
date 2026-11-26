import { useState } from 'react'
import { Button, Card, Input } from '@ui'
import { apiUrl, communicationConfig, communicationRoutes } from '@shared/communication/config'
import { useConsoleDependencies } from '../dependencies'
import { AdminSectionShell } from './AdminSectionShell'

export function AddHubView() {
  const { consoleAuth } = useConsoleDependencies()
  const [venueId, setVenueId] = useState('')
  const [tableId, setTableId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [pairingCode, setPairingCode] = useState<{ code: string; expiresAt: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setPairingCode(null)
    setSubmitting(true)
    try {
      const session = await consoleAuth.getCurrentSession()
      const baseUrl = import.meta.env.VITE_IDENTITY_API_URL ?? apiUrl(communicationConfig.apiBaseUrl, communicationRoutes.identity)
      const response = await fetch(`${baseUrl}/devices/pairing-codes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(session?.token.accessToken ? { Authorization: `Bearer ${session.token.accessToken}` } : {}),
        },
        body: JSON.stringify({ venueId, tableId, surface: 'KIOSK' }),
      })
      if (!response.ok) {
        throw new Error(`Generazione codice non riuscita (HTTP ${response.status})`)
      }
      const data = (await response.json()) as { code: string; expiresAt: string }
      setPairingCode(data)
    } catch (reason: unknown) {
      setError(reason instanceof Error ? reason.message : 'Errore durante la registrazione del dispositivo')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AdminSectionShell title="Aggiungi hub" description="Collega un nuovo hub o display al locale generando un codice di associazione.">
      <Card elevated className="mx-auto max-w-2xl">
        <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)}>
          <Input label="ID Locale (UUID)" value={venueId} onChange={(e) => setVenueId(e.target.value)} placeholder="018f..." required />
          <Input label="ID Tavolo (UUID)" value={tableId} onChange={(e) => setTableId(e.target.value)} placeholder="018f..." required />
          {error && <p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">{error}</p>}
          {pairingCode && (
            <p role="status" className="text-sm text-[var(--cg-color-status-success)]">
              Codice generato: <strong className="font-mono">{pairingCode.code}</strong> (scade il {new Date(pairingCode.expiresAt).toLocaleTimeString('it-IT')})
            </p>
          )}
          <Button type="submit" className="w-full" disabled={submitting}>
            {submitting ? 'Generazione…' : 'Genera codice di associazione'}
          </Button>
        </form>
      </Card>
    </AdminSectionShell>
  )
}
