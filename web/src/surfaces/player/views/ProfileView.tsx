import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'

export function ProfileView() {
  const isOnline = useNetworkStatus()

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
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-full border border-purple-500/40 bg-purple-600/20 text-xl font-bold text-purple-300">
            P1
          </div>
          <div>
            <h2 className="text-lg font-semibold">Giulia Ferri</h2>
            <p className="text-xs text-neutral-400">ID: usr_player_001</p>
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
            <span className="font-semibold text-neutral-200">12</span>
          </div>
        </div>
      </Card>
    </div>
  )
}
