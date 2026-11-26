import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'
import { getPlayerDependencies } from '../composition-root'

const dependencies = getPlayerDependencies()

export function ProfileView() {
  const isOnline = useNetworkStatus()

  const sessionQuery = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: () => dependencies.auth.getStoredSession(),
  })

  const session = sessionQuery.data

  const statsQuery = useQuery({
    queryKey: ['stats', 'participant', session?.id],
    queryFn: () => dependencies.stats.getParticipantSummary(session!.id),
    enabled: Boolean(session?.id),
  })

  return (
    <div className="cg-mobile-page space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[var(--cg-color-accent-label)]">
            Account
          </p>
          <h1 className="mt-1 text-2xl font-bold">Profilo</h1>
        </div>
        <Link to="/lobby" className="cg-button cg-button-secondary">
          Torna alla lobby
        </Link>
      </div>

      <Card elevated className="space-y-5">
        {sessionQuery.isPending ? (
          <p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento profilo…</p>
        ) : !session ? (
          <div className="space-y-3">
            <p className="text-sm text-[var(--cg-color-label-secondary)]">Nessuna sessione attiva.</p>
            <Link to="/" className="cg-button cg-button-primary inline-block">Scansiona QR per accedere</Link>
          </div>
        ) : (
          <>
            <div className="flex items-center gap-4">
              <div className="flex h-14 w-14 items-center justify-center rounded-full border border-purple-500/40 bg-purple-600/20 text-xl font-bold text-purple-300">
                {session.kind.slice(0, 2).toUpperCase()}
              </div>
              <div>
                <h2 className="text-lg font-semibold capitalize">Sessione {session.kind}</h2>
                <p className="text-xs text-neutral-400">ID: {session.id}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 border-t border-neutral-800 pt-4 text-sm">
              <div>
                <span className="mb-2 block text-xs text-neutral-400">Stato rete</span>
                <Badge tone={isOnline ? 'success' : 'warning'}>
                  {isOnline ? 'Online' : 'Offline'}
                </Badge>
              </div>
              <div>
                <span className="mb-2 block text-xs text-neutral-400">Partite giocate</span>
                <span className="font-semibold text-neutral-200">
                  {statsQuery.isPending ? '…' : (statsQuery.data?.matches ?? 0)}
                </span>
              </div>
            </div>
          </>
        )}
      </Card>
    </div>
  )
}
