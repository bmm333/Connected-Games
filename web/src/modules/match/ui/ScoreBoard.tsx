import { useState } from 'react'
import { MatchScore } from '@match/domain/Score'

interface ScoreBoardProps {
  initialHomePlayer: string
  initialAwayPlayer: string
  onPointScored?: (player: string, score: string) => void
}

export function ScoreBoard({ initialHomePlayer, initialAwayPlayer, onPointScored }: ScoreBoardProps) {
  const [matchScore, setMatchScore] = useState(() =>
    MatchScore.create(initialHomePlayer, initialAwayPlayer)
  )

  return (
    <section aria-label="tabellone punti" className="overflow-hidden rounded-2xl border border-[var(--cg-color-border-subtle)] bg-[var(--cg-color-bg-elevated)] shadow-sm">
      <div className="flex items-center justify-between border-b border-[var(--cg-color-border-subtle)] px-5 py-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Live match</p>
          <h2 className="mt-1 text-xl font-bold">Punteggio Partita</h2>
        </div>
        <span className="inline-flex items-center gap-2 rounded-full bg-[color-mix(in_srgb,var(--cg-color-status-success)_16%,transparent)] px-3 py-1 text-xs font-bold text-[var(--cg-color-status-success)]">
          <span className="h-2 w-2 animate-pulse rounded-full bg-current" aria-hidden="true" /> LIVE
        </span>
      </div>

      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3 px-5 py-7 text-center sm:px-10">
        <div className="rounded-xl border border-[var(--cg-color-team-red)]/20 bg-[var(--cg-color-team-red)]/5 p-4">
          <div className="mx-auto mb-3 h-3 w-3 rounded-full bg-[var(--cg-color-team-red)]" aria-hidden="true" />
          <p className="font-semibold">{matchScore.home.playerId}</p>
          <p data-testid="home-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{matchScore.home.points}</p>
          <button
            type="button"
            onClick={() => setMatchScore(prev => {
              const next = prev.addPointToHome()
              onPointScored?.(initialHomePlayer, `${next.home.points}-${next.away.points}`)
              return next
            })}
            className="cg-button cg-button-secondary mt-4 w-full text-sm"
          >
            +1 Punto
          </button>
        </div>

        <div className="space-y-2">
          <span className="text-xs font-bold uppercase tracking-widest text-[var(--cg-color-label-tertiary)]">VS</span>
          <p className="font-mono text-sm font-semibold text-[var(--cg-color-label-secondary)]">09:48</p>
          <p className="text-xs text-[var(--cg-color-label-tertiary)]">Set 1 · Primo a 10</p>
        </div>

        <div className="rounded-xl border border-[var(--cg-color-team-blue)]/20 bg-[var(--cg-color-team-blue)]/5 p-4">
          <div className="mx-auto mb-3 h-3 w-3 rounded-full bg-[var(--cg-color-team-blue)]" aria-hidden="true" />
          <p className="font-semibold">{matchScore.away.playerId}</p>
          <p data-testid="away-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{matchScore.away.points}</p>
          <button
            type="button"
            onClick={() => setMatchScore(prev => {
              const next = prev.addPointToAway()
              onPointScored?.(initialAwayPlayer, `${next.home.points}-${next.away.points}`)
              return next
            })}
            className="cg-button cg-button-secondary mt-4 w-full text-sm"
          >
            +1 Punto
          </button>
        </div>
      </div>

      <div className="grid grid-cols-3 border-t border-[var(--cg-color-border-subtle)] bg-[var(--cg-color-bg-grouped)] px-5 py-4 text-center text-xs">
        <div><p className="text-[var(--cg-color-label-tertiary)]">Stato</p><p className="mt-1 font-semibold text-[var(--cg-color-status-success)]">In corso</p></div>
        <div><p className="text-[var(--cg-color-label-tertiary)]">Leader</p><p data-testid="current-leader" className="mt-1 font-semibold">{matchScore.leader ?? 'Pareggio'}</p></div>
        <div><p className="text-[var(--cg-color-label-tertiary)]">Obiettivo</p><p className="mt-1 font-semibold">10 punti</p></div>
      </div>
    </section>
  )
}
