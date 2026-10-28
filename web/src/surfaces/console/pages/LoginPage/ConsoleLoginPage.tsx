import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { Button, Card, Input } from '@ui'
import type { ConsoleLoginRequest } from '@domain/console/auth/AuthConsoleRepository'
import { useConsoleAuth } from '../../auth/useConsoleAuth'

export function ConsoleLoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useConsoleAuth()
  const [method, setMethod] = useState<'credentials' | 'sso'>('credentials')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [provider, setProvider] = useState('')
  const [assertion, setAssertion] = useState('')
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState<string>()
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(undefined)
    setIsSubmitting(true)
    try {
      const request: ConsoleLoginRequest =
        method === 'credentials'
          ? { method, email, password, remember }
          : { method, provider, assertion, remember }
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
    <main className="flex min-h-screen items-center justify-center p-6">
      <Card elevated className="w-full max-w-md space-y-6">
        <header>
          <p className="text-sm font-semibold text-[var(--cg-color-accent-label)]">
            Partita Console
          </p>
          <h1 className="mt-2 text-2xl font-bold">Accedi alla Console</h1>
        </header>
        <div className="flex gap-2">
          <Button
            type="button"
            variant={method === 'credentials' ? 'primary' : 'secondary'}
            onClick={() => setMethod('credentials')}
          >
            Credenziali
          </Button>
          <Button
            type="button"
            variant={method === 'sso' ? 'primary' : 'secondary'}
            onClick={() => setMethod('sso')}
          >
            SSO
          </Button>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit}>
          {method === 'credentials' ? (
            <>
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
            </>
          ) : (
            <>
              <Input
                label="Provider SSO"
                value={provider}
                onChange={(event) => setProvider(event.target.value)}
                required
              />
              <Input
                label="Assertion"
                value={assertion}
                onChange={(event) => setAssertion(event.target.value)}
                required
              />
            </>
          )}
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={remember}
              onChange={(event) => setRemember(event.target.checked)}
            />
            Ricorda questa sessione
          </label>
          {error && <p role="alert" className="text-sm text-red-500">{error}</p>}
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Accesso…' : 'Accedi'}
          </Button>
        </form>
      </Card>
    </main>
  )
}
