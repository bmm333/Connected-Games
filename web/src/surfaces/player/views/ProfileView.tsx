import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'

export function ProfileView() {
  const isOnline = useNetworkStatus()

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-4 border-b border-neutral-800">
        <h1 className="text-2xl font-bold">Profilo Giocatore</h1>
        <Link
          to="/"
          className="px-3 py-1.5 text-xs font-medium rounded-lg bg-neutral-800 text-neutral-300 hover:bg-neutral-700 transition"
        >
          ← Torna alla Lobby
        </Link>
      </div>

      <div className="p-5 rounded-xl bg-neutral-900 border border-neutral-800 space-y-4">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-full bg-purple-600/20 border border-purple-500/40 flex items-center justify-center text-xl font-bold text-purple-300">
            P1
          </div>
          <div>
            <h2 className="text-lg font-semibold">Player One</h2>
            <p className="text-xs text-neutral-400">ID: usr_player_001</p>
          </div>
        </div>

        <div className="pt-4 border-t border-neutral-800 text-sm grid grid-cols-2 gap-4">
          <div>
            <span className="text-neutral-400 block text-xs">Stato Rete:</span>
            <span className={isOnline ? 'text-emerald-400 font-medium' : 'text-amber-400 font-medium'}>
              {isOnline ? 'Online (Sincronizzato)' : 'Offline (Cache Locale IDB)'}
            </span>
          </div>
          <div>
            <span className="text-neutral-400 block text-xs">Partite Giocate:</span>
            <span className="font-semibold text-neutral-200">12</span>
          </div>
        </div>
      </div>
    </div>
  )
}
