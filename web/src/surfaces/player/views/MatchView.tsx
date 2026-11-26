import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router'
import type { Match, MatchStreamStatus } from '@domain/simulator'
import { MatchEventFeed, type MatchEvent } from '@match/ui/MatchEventFeed'
import { Badge, Card } from '@ui'
import { getPlayerDependencies } from '../composition-root'

const dependencies = getPlayerDependencies()

function formatTime(iso?: string): string {
  const date = iso ? new Date(iso) : new Date()
  return date.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
}

export function MatchView() {
  const { id = '' } = useParams()
  const [match, setMatch] = useState<Match | undefined>(undefined)
  const [loading, setLoading] = useState(Boolean(id))
  const [error, setError] = useState<string | undefined>(undefined)
  const [streamStatus, setStreamStatus] = useState<MatchStreamStatus>(id ? 'connecting' : 'disconnected')
  const [events, setEvents] = useState<MatchEvent[]>([])
  const previousScoresRef = useRef<{ red: number; blue: number } | null>(null)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    let streamUpdated = false
    previousScoresRef.current = null

    const recordMatchUpdate = (nextMatch: Match) => {
      const redScore = nextMatch.sides.find((s) => s.side === 'RED')?.score ?? 0
      const blueScore = nextMatch.sides.find((s) => s.side === 'BLUE')?.score ?? 0
      const redName = nextMatch.sides.find((s) => s.side === 'RED')?.displayName ?? 'Rosso'
      const blueName = nextMatch.sides.find((s) => s.side === 'BLUE')?.displayName ?? 'Blu'

      setEvents((current) => {
        const prev = previousScoresRef.current
        if (!prev) {
          previousScoresRef.current = { red: redScore, blue: blueScore }
          return [
            {
              id: `init-${nextMatch.id}`,
              time: formatTime(nextMatch.startedAt),
              message: `Partita ${nextMatch.status === 'IN_PROGRESS' ? 'in corso' : nextMatch.status.toLowerCase()} · ${redName} vs ${blueName}`,
              tone: 'system',
            },
          ]
        }

        const updates: MatchEvent[] = []
        if (redScore > prev.red) {
          updates.push({
            id: crypto.randomUUID(),
            time: formatTime(),
            message: `${redName} ha segnato · ${redScore}-${blueScore}`,
            tone: 'score',
          })
        }
        if (blueScore > prev.blue) {
          updates.push({
            id: crypto.randomUUID(),
            time: formatTime(),
            message: `${blueName} ha segnato · ${redScore}-${blueScore}`,
            tone: 'score',
          })
        }
        previousScoresRef.current = { red: redScore, blue: blueScore }
        return updates.length > 0 ? [...updates, ...current] : current
      })
    }

    const unsubscribe = dependencies.matches.subscribe(
      id,
      (nextMatch) => {
        streamUpdated = true
        if (cancelled) return
        setMatch(nextMatch)
        setLoading(false)
        setError(undefined)
        recordMatchUpdate(nextMatch)
      },
      (status) => {
        if (!cancelled) setStreamStatus(status)
      },
    )

    void dependencies.matches.getCurrentMatch(id)
      .then((currentMatch) => {
        if (cancelled) return
        if (!streamUpdated && currentMatch) {
          setMatch(currentMatch)
          recordMatchUpdate(currentMatch)
        }
        setLoading(false)
      })
      .catch((reason: unknown) => {
        if (!cancelled) {
          setLoading(false)
          setError(reason instanceof Error ? reason.message : 'Impossibile caricare la partita')
        }
      })

    return () => {
      cancelled = true
      unsubscribe()
    }
  }, [id])

  const red = match?.sides.find((s) => s.side === 'RED')
  const blue = match?.sides.find((s) => s.side === 'BLUE')
  const isFinal = match?.status === 'COMPLETED' || match?.status === 'CANCELLED'

  return (
    <div className="cg-mobile-page space-y-6">
      <div className="flex items-start justify-between gap-4 border-b border-[var(--cg-color-border-subtle)] pb-5">
        <div>
          <Badge tone={streamStatus === 'connected' ? 'blue' : 'warning'}>
            {streamStatus === 'connected' ? 'Sessione live' : streamStatus}
          </Badge>
          <h1 className="mt-2 text-2xl font-bold">Stanza di gioco</h1>
          <p className="mt-1 text-xs text-[var(--cg-color-label-secondary)]">
            {id ? `Tavolo #${id}` : 'Nessun tavolo selezionato'}
          </p>
        </div>
        <Link to="/lobby" className="cg-button cg-button-secondary text-sm">← Lobby</Link>
      </div>

      {loading ? (
        <Card elevated><p role="status" className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento partita…</p></Card>
      ) : error ? (
        <Card elevated><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">{error}</p></Card>
      ) : !match ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessuna partita attiva su questo tavolo.</p></Card>
      ) : (
        <Card elevated className="space-y-5">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">
                {isFinal ? 'Risultato finale' : 'Punteggio live'}
              </p>
              <h2 className="mt-1 text-xl font-bold">Primo a {match.pointsToWin}</h2>
            </div>
            <Badge tone={isFinal ? 'neutral' : 'success'}>
              {isFinal ? 'Conclusa' : 'In corso'}
            </Badge>
          </div>
          <div className="flex items-center justify-between gap-6">
            <div className="text-center flex-1">
              <p className="font-semibold">{red?.displayName ?? 'Rosso'}</p>
              <p data-testid="home-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{red?.score ?? 0}</p>
            </div>
            <span className="text-2xl font-light text-[var(--cg-color-label-tertiary)]">VS</span>
            <div className="text-center flex-1">
              <p className="font-semibold">{blue?.displayName ?? 'Blu'}</p>
              <p data-testid="away-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{blue?.score ?? 0}</p>
            </div>
          </div>
        </Card>
      )}

      {events.length > 0 && <MatchEventFeed events={events} />}
      <p className="text-center text-xs text-[var(--cg-color-label-tertiary)]">
        I punti vengono sincronizzati automaticamente quando sei online.
      </p>
    </div>
  )
}
