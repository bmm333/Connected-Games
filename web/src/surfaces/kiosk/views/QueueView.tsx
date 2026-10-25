import { Link } from 'react-router'

export function QueueView() {
  const queue = [
    { position: 1, name: 'Alessandro M.', status: 'In attesa' },
    { position: 2, name: 'Giulia R.', status: 'In attesa' },
    { position: 3, name: 'Marco V.', status: 'Pronto al tavolo' },
  ]

  return (
    <div className="max-w-3xl mx-auto p-8 space-y-6">
      <div className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-3xl font-bold">Coda di Gioco Totem</h1>
          <p className="text-sm text-neutral-400">Postazione Kiosk #1 — Sala Principale</p>
        </div>
        <Link
          to="/"
          className="px-4 py-2 text-sm font-medium rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800 transition"
        >
          ← Schermata Attesa
        </Link>
      </div>

      <div className="space-y-3">
        {queue.map(item => (
          <div
            key={item.position}
            className="flex items-center justify-between p-4 rounded-xl bg-neutral-900 border border-neutral-800"
          >
            <div className="flex items-center gap-4">
              <span className="w-8 h-8 rounded-lg bg-purple-600/20 text-purple-400 font-bold flex items-center justify-center text-sm">
                #{item.position}
              </span>
              <span className="font-semibold text-lg">{item.name}</span>
            </div>
            <span className="text-xs uppercase tracking-wider px-3 py-1 rounded-full bg-neutral-800 text-neutral-400">
              {item.status}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
