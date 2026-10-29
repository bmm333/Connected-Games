import { Link, useParams } from 'react-router'
import { useState } from 'react'
import { ScoreBoard } from '@match/ui/ScoreBoard'
import { MatchEventFeed, type MatchEvent } from '@match/ui/MatchEventFeed'

export function MatchView() {
  const { id = 'live-match' } = useParams()
  const [events, setEvents] = useState<MatchEvent[]>([
    { id: 'start', time: '20:21', message: 'Partita avviata sul Tavolo 2', tone: 'system' },
    { id: 'ready', time: '20:22', message: 'Entrambe le squadre sono pronte', tone: 'info' },
  ])

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

      <ScoreBoard
        initialHomePlayer="Giocatore 1"
        initialAwayPlayer="Giocatore 2"
        onPointScored={(player, score) => setEvents((current) => [
          { id: crypto.randomUUID(), time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' }), message: `${player} ha segnato · ${score}`, tone: 'score' },
          ...current,
        ])}
      />
      <MatchEventFeed events={events} />
    </div>
  )
}
