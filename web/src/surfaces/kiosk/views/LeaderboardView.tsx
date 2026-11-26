import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router'
import type { StatsRepository } from '@domain/stats/StatsRepository'
import { Badge, Card } from '@ui'

export function LeaderboardView({ stats }: { stats: StatsRepository }) {
  const [searchParams] = useSearchParams()
  const venueId = searchParams.get('venueId') ?? undefined

  const leaderboardQuery = useQuery({
    queryKey: ['stats', 'leaderboard', venueId],
    queryFn: () => stats.getLeaderboard({ venueId, limit: 20 }),
  })

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Classifica Torneo Live</h1>
          <p className="text-sm text-neutral-400">Classifica aggiornata in tempo reale</p>
        </div>
        <Link to="/" className="cg-button cg-button-secondary">Schermata attesa</Link>
      </div>

      <Card elevated className="overflow-hidden p-0">
        {leaderboardQuery.isPending ? (
          <p role="status" className="p-6 text-sm text-[var(--cg-color-label-secondary)]">Caricamento classifica…</p>
        ) : leaderboardQuery.isError ? (
          <p role="alert" className="p-6 text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare la classifica.</p>
        ) : leaderboardQuery.data.length === 0 ? (
          <p className="p-6 text-sm text-[var(--cg-color-label-secondary)]">Nessun risultato disponibile in classifica.</p>
        ) : (
          <table className="w-full text-left">
            <thead>
              <tr className="border-b border-neutral-800 text-neutral-400 text-xs uppercase tracking-wider">
                <th className="p-4">Pos</th>
                <th className="p-4">Giocatore</th>
                <th className="p-4 text-right">Vittorie</th>
                <th className="p-4 text-right">Punti</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-neutral-850">
              {leaderboardQuery.data.map((entry) => (
                <tr key={entry.participantId} className="hover:bg-neutral-800/40 transition">
                  <td className="p-4">
                    <Badge tone={entry.rank === 1 ? 'warning' : 'neutral'}>#{entry.rank}</Badge>
                  </td>
                  <td className="p-4 font-medium">{entry.displayName ?? entry.participantId}</td>
                  <td className="p-4 text-right font-mono">{entry.wins}</td>
                  <td className="p-4 text-right font-mono font-semibold text-emerald-400">
                    {entry.pointsFor}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
