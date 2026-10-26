import { Link } from 'react-router'

export function TournamentManagerView() {
  const tournaments = [
    { id: 'trn-101', name: 'Coppa d’Autunno 2026', game: 'Air Hockey', players: 16, status: 'Quarti di finale' },
    { id: 'trn-102', name: 'Speed Runner Grand Prix', game: 'Arcade Sprint', players: 32, status: 'Iscrizioni aperte' },
  ]

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-3xl font-bold">Gestione Tornei</h1>
          <p className="text-sm text-neutral-400">Pianifica, avvia e monitora le competizioni</p>
        </div>
        <Link
          to="/"
          className="px-4 py-2 text-sm font-medium rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800 transition"
        >
          ← Dashboard
        </Link>
      </div>

      <div className="space-y-4">
        {tournaments.map(t => (
          <div key={t.id} className="p-5 rounded-xl bg-neutral-900 border border-neutral-800 flex justify-between items-center">
            <div>
              <span className="text-xs font-mono text-purple-400">{t.id}</span>
              <h2 className="text-lg font-semibold text-white">{t.name}</h2>
              <p className="text-xs text-neutral-400">Gioco: {t.game} • {t.players} partecipanti</p>
            </div>
            <span className="text-xs px-3 py-1 rounded-full bg-purple-500/10 text-purple-300 font-medium border border-purple-500/20">
              {t.status}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
