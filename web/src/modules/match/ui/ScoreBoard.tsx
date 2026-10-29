import { MatchScore } from '@match/domain/Score'
import { useState } from 'react'

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
    <section aria-label="tabellone punti" className="cg-card cg-card-elevated">
      <div className="mb-5 flex items-center justify-between gap-4">
        <div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">Punteggio live</p><h2 className="mt-1 text-xl font-bold">Punteggio Partita</h2></div>
        <span className="cg-badge cg-badge-success">In corso</span>
      </div>
      
      <div className="flex justify-between items-center gap-6">
        <div className="text-center">
          <p className="font-semibold">{matchScore.home.playerId}</p>
          <p data-testid="home-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{matchScore.home.points}</p>
          <button
            type="button"
            onClick={() => {
              const next = matchScore.addPointToHome()
              setMatchScore(next)
              onPointScored?.(initialHomePlayer, `${next.home.points}-${next.away.points}`)
            }}
            className="cg-button cg-button-secondary mt-2 text-sm"
          >
            +1 Punto
          </button>
        </div>

        <span className="text-2xl font-light text-[var(--cg-color-label-tertiary)]">VS</span>

        <div className="text-center">
          <p className="font-semibold">{matchScore.away.playerId}</p>
          <p data-testid="away-points" className="mt-2 text-6xl font-black leading-none tracking-tight">{matchScore.away.points}</p>
          <button
            type="button"
            onClick={() => {
              const next = matchScore.addPointToAway()
              setMatchScore(next)
              onPointScored?.(initialAwayPlayer, `${next.home.points}-${next.away.points}`)
            }}
            className="cg-button cg-button-secondary mt-2 text-sm"
          >
            +1 Punto
          </button>
        </div>
      </div>

      <div className="mt-5 border-t border-[var(--cg-color-border-subtle)] pt-4 text-center text-sm text-[var(--cg-color-label-secondary)]">
        Leader attuale:{' '}
        <span data-testid="current-leader" className="font-semibold text-[var(--cg-color-status-success)]">
          {matchScore.leader ?? 'Pareggio'}
        </span>
      </div>
    </section>
  )
}
