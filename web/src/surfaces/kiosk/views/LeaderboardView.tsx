import { Link } from 'react-router'
import { Badge, Card } from '@ui'

export function LeaderboardView() {
  const leaders = [
    { rank: 1, name: 'DragonSlayer', score: 2450 },
    { rank: 2, name: 'CyberViper', score: 2180 },
    { rank: 3, name: 'ShadowNinja', score: 1940 },
    { rank: 4, name: 'PixelMaster', score: 1720 },
  ]

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
        <table className="w-full text-left">
          <thead>
            <tr className="border-b border-neutral-800 text-neutral-400 text-xs uppercase tracking-wider">
              <th className="p-4">Pos</th>
              <th className="p-4">Giocatore</th>
              <th className="p-4 text-right">Punti</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-850">
            {leaders.map(l => (
              <tr key={l.rank} className="hover:bg-neutral-800/40 transition">
                <td className="p-4"><Badge tone={l.rank === 1 ? 'warning' : 'neutral'}>#{l.rank}</Badge></td>
                <td className="p-4 font-medium">{l.name}</td>
                <td className="p-4 text-right font-mono font-semibold text-emerald-400">
                  {l.score}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
