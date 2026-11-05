import { ScoreBoard } from '@match/ui/ScoreBoard'
import { MatchEventFeed, type MatchEvent } from '@match/ui/MatchEventFeed'
import { Badge } from '@ui'
import { useState } from 'react'
import { Link, useParams } from 'react-router'

export function MatchView() {
  const { id = 'live-match' } = useParams()
  const [events, setEvents] = useState<MatchEvent[]>([
    { id: 'start', time: '20:21', message: 'Partita avviata sul Tavolo 2', tone: 'system' },
    { id: 'ready', time: '20:22', message: 'Entrambe le squadre sono pronte', tone: 'info' },
  ])

  return (
    <div className="cg-mobile-page space-y-6">
      <div className="flex items-start justify-between gap-4 border-b border-[var(--cg-color-border-subtle)] pb-5">
        <div>
          <Badge tone="blue">Sessione live</Badge>
          <h1 className="mt-2 text-2xl font-bold">Stanza di gioco</h1>
          <p className="mt-1 text-xs text-[var(--cg-color-label-secondary)]">Sessione #{id}</p>
        </div>
        <Link to="/lobby" className="cg-button cg-button-secondary text-sm">← Lobby</Link>
      </div>
      <ScoreBoard
        initialHomePlayer="Giocatore 1"
        initialAwayPlayer="Giocatore 2"
        onPointScored={(player, score) => setEvents((current) => [
          {
            id: crypto.randomUUID(),
            time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' }),
            message: `${player} ha segnato · ${score}`,
            tone: 'score',
          },
          ...current,
        ])}
      />
      <MatchEventFeed events={events} />
      <p className="text-center text-xs text-[var(--cg-color-label-tertiary)]">I punti vengono sincronizzati automaticamente quando sei online.</p>
    </div>
  )
}
