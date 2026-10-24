import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'

export function LobbyView() {
  const isOnline = useNetworkStatus()

  return (
    <div className="space-y-6">
      <header className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Player Arena</h1>
          <p className="text-sm text-neutral-400">Seleziona una lobby per avviare la partita</p>
        </div>
        <span
          className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
            isOnline ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'
          }`}
        >
          {isOnline ? 'Connesso' : 'Offline'}
        </span>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="p-5 rounded-xl bg-neutral-900 border border-neutral-800 hover:border-purple-500/50 transition">
          <h2 className="text-lg font-semibold mb-2">Match Rapido 1v1</h2>
          <p className="text-sm text-neutral-400 mb-4">
            Avvia un testa a testa immediato sincronizzato in tempo reale.
          </p>
          <Link
            to="/match/quick-match"
            className="inline-flex items-center justify-center px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-sm font-medium transition"
          >
            Entra nel Match
          </Link>
        </div>

        <div className="p-5 rounded-xl bg-neutral-900 border border-neutral-800">
          <h2 className="text-lg font-semibold mb-2">Torneo Attivo</h2>
          <p className="text-sm text-neutral-400 mb-4">
            Consulta il tabellone e le prossime sfide in programma.
          </p>
          <Link
            to="/profile"
            className="inline-flex items-center justify-center px-4 py-2 bg-neutral-800 hover:bg-neutral-700 text-white rounded-lg text-sm font-medium transition"
          >
            Il Mio Profilo
          </Link>
        </div>
      </div>
    </div>
  )
}
