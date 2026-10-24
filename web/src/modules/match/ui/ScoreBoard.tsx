import { useState } from 'react'
import { MatchScore } from '@match/domain/Score'

interface ScoreBoardProps {
  initialHomePlayer: string
  initialAwayPlayer: string
}

export function ScoreBoard({ initialHomePlayer, initialAwayPlayer }: ScoreBoardProps) {
  const [matchScore, setMatchScore] = useState(() =>
    MatchScore.create(initialHomePlayer, initialAwayPlayer)
  )

  return (
    <section aria-label="tabellone punti" className="p-4 rounded-lg bg-neutral-900 text-white">
      <h2 className="text-xl font-bold mb-4">Punteggio Partita</h2>
      
      <div className="flex justify-between items-center gap-6">
        <div className="text-center">
          <p className="font-semibold">{matchScore.home.playerId}</p>
          <p data-testid="home-points" className="text-3xl font-mono">{matchScore.home.points}</p>
          <button
            type="button"
            onClick={() => setMatchScore(prev => prev.addPointToHome())}
            className="mt-2 px-3 py-1 bg-blue-600 rounded text-sm hover:bg-blue-500"
          >
            +1 Punto
          </button>
        </div>

        <span className="text-2xl font-light text-neutral-400">VS</span>

        <div className="text-center">
          <p className="font-semibold">{matchScore.away.playerId}</p>
          <p data-testid="away-points" className="text-3xl font-mono">{matchScore.away.points}</p>
          <button
            type="button"
            onClick={() => setMatchScore(prev => prev.addPointToAway())}
            className="mt-2 px-3 py-1 bg-red-600 rounded text-sm hover:bg-red-500"
          >
            +1 Punto
          </button>
        </div>
      </div>

      <div className="mt-4 text-center text-sm text-neutral-400">
        Leader attuale:{' '}
        <span data-testid="current-leader" className="font-semibold text-emerald-400">
          {matchScore.leader ?? 'Pareggio'}
        </span>
      </div>
    </section>
  )
}
