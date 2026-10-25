import { Link, useParams } from 'react-router'
import { ScoreBoard } from '@match/ui/ScoreBoard'

export function MatchView() {
  const { id = 'live-match' } = useParams()

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-4 border-b border-neutral-800">
        <div>
          <span className="text-xs font-mono text-purple-400">SESSIONE: #{id}</span>
          <h1 className="text-2xl font-bold">Stanza di Gioco</h1>
        </div>
        <Link
          to="/"
          className="px-3 py-1.5 text-xs font-medium rounded-lg bg-neutral-800 text-neutral-300 hover:bg-neutral-700 transition"
        >
          ← Torna alla Lobby
        </Link>
      </div>

      <ScoreBoard initialHomePlayer="Giocatore 1" initialAwayPlayer="Giocatore 2" />
    </div>
  )
}
