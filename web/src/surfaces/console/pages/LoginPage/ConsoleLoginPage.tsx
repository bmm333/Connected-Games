import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { Button, Card, Input } from '@ui'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'
import { useConsoleAuth } from '../../auth/useConsoleAuth'

export function ConsoleLoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useConsoleAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState<string>()
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(undefined)
    setIsSubmitting(true)
    try {
      const request: ConsoleLoginRequest = { email, password, remember }
      await login(request)
      const from = (location.state as { from?: { pathname: string } } | null)
        ?.from?.pathname
      navigate(from ?? '/overview', { replace: true })
    } catch (loginError) {
      setError(
        loginError instanceof Error
          ? loginError.message
          : 'Autenticazione non riuscita',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center p-5 md:p-8">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-[24px] border border-[var(--cg-color-border-default)] bg-[var(--cg-color-bg-elevated)] shadow-[var(--cg-shadow-lg)] md:grid-cols-[1.05fr_0.95fr]">
        <section className="hidden bg-[var(--cg-color-accent)] p-10 text-white md:flex md:flex-col md:justify-between">
          <div>
            <div className="mb-12 flex items-center gap-3 text-lg font-bold">
              <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/20">P</span>
              Partita Console
            </div>
            <p className="max-w-sm text-4xl font-bold leading-tight">Tutto il locale, in un unico punto di controllo.</p>
            <p className="mt-5 max-w-sm text-sm leading-6 text-white/75">Monitora tavoli, dispositivi e attività operative con una vista chiara e sempre aggiornata.</p>
          </div>
          <p className="text-xs text-white/60">Accesso protetto · Connected Games</p>
        </section>
        <Card className="rounded-none border-0 p-7 shadow-none md:p-10">
          <header className="mb-8">
            <p className="text-sm font-semibold text-[var(--cg-color-accent-label)] md:hidden">Partita Console</p>
            <h1 className="mt-2 text-3xl font-bold tracking-tight">Bentornato</h1>
            <p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">Accedi con il tuo account operativo.</p>
          </header>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          <Input
            label="Password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={remember}
              onChange={(event) => setRemember(event.target.checked)}
            />
            Ricorda questa sessione
          </label>
          {error && <p role="alert" className="text-sm text-red-500">{error}</p>}
          <Button type="submit" disabled={isSubmitting} className="mt-3 w-full">
            {isSubmitting ? 'Accesso…' : 'Accedi'}
          </Button>
        </form>
        </Card>
      </div>
    </main>
  )
}
