import { Link } from 'react-router'

export function LeaderboardView() {
  const leaders = [
    { rank: 1, name: 'DragonSlayer', score: 2450 },
    { rank: 2, name: 'CyberViper', score: 2180 },
    { rank: 3, name: 'ShadowNinja', score: 1940 },
    { rank: 4, name: 'PixelMaster', score: 1720 },
  ]

  return (
    <div className="max-w-3xl mx-auto p-8 space-y-6">
      <div className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-3xl font-bold">Classifica Torneo Live</h1>
          <p className="text-sm text-neutral-400">Classifica aggiornata in tempo reale</p>
        </div>
        <Link
          to="/"
          className="px-4 py-2 text-sm font-medium rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800 transition"
        >
          ← Schermata Attesa
        </Link>
      </div>

      <div className="overflow-hidden rounded-xl border border-neutral-800 bg-neutral-900">
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
                <td className="p-4 font-mono font-bold text-purple-400">#{l.rank}</td>
                <td className="p-4 font-medium">{l.name}</td>
                <td className="p-4 text-right font-mono font-semibold text-emerald-400">
                  {l.score}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
