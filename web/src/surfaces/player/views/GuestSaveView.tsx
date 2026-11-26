import { useState } from 'react'
import { Link } from 'react-router'
import { Button, Card, Input } from '@ui'
import { apiUrl, communicationConfig, communicationRoutes } from '@shared/communication/config'
import { getPlayerDependencies } from '../composition-root'

const dependencies = getPlayerDependencies()

export function GuestSaveView() {
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setSaved(false)
    setSubmitting(true)
    try {
      const session = await dependencies.auth.getStoredSession()
      const baseUrl = import.meta.env.VITE_IDENTITY_API_URL ?? apiUrl(communicationConfig.apiBaseUrl, communicationRoutes.identity)
      const response = await fetch(`${baseUrl}/users`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(session?.token.accessToken ? { Authorization: `Bearer ${session.token.accessToken}` } : {}),
        },
        body: JSON.stringify({ displayName, email, password }),
      })
      if (!response.ok) {
        throw new Error(`Registrazione non riuscita (HTTP ${response.status})`)
      }
      setSaved(true)
    } catch (reason: unknown) {
      setError(reason instanceof Error ? reason.message : 'Errore durante il salvataggio del profilo')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <header>
        <Link to="/lobby" className="text-sm text-[var(--cg-color-accent-label)]">← Lobby</Link>
        <h1 className="mt-3 text-2xl font-bold">Salva il tuo profilo</h1>
        <p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">Conserva risultati e statistiche anche su altri dispositivi.</p>
      </header>
      <Card elevated>
        <form className="space-y-4" onSubmit={(event) => void handleSubmit(event)}>
          <Input label="Nome" value={displayName} onChange={(e) => setDisplayName(e.target.value)} placeholder="Come vuoi essere chiamato?" required />
          <Input label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.it" required />
          <Input label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Almeno 8 caratteri" required />
          {error && <p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">{error}</p>}
          {saved && <p role="status" className="text-sm text-[var(--cg-color-status-success)]">Profilo salvato correttamente.</p>}
          <Button type="submit" className="w-full" disabled={submitting}>
            {submitting ? 'Salvataggio…' : 'Salva e continua'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
