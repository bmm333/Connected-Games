import { Link } from 'react-router'
import { Badge, Card } from '@ui'

export function QueueView() {
  const queue = [
    { position: 1, name: 'Alessandro M.', status: 'In attesa' },
    { position: 2, name: 'Giulia R.', status: 'In attesa' },
    { position: 3, name: 'Marco V.', status: 'Pronto al tavolo' },
  ]

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Coda di Gioco Totem</h1>
          <p className="text-sm text-neutral-400">Postazione Kiosk #1 — Sala Principale</p>
        </div>
        <Link to="/" className="cg-button cg-button-secondary">Schermata attesa</Link>
      </div>

      <div className="space-y-3">
        {queue.map(item => (
          <Card elevated key={item.position} className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <span className="w-8 h-8 rounded-lg bg-purple-600/20 text-purple-400 font-bold flex items-center justify-center text-sm">
                #{item.position}
              </span>
              <span className="font-semibold text-lg">{item.name}</span>
            </div>
            <Badge tone={item.status === 'Pronto al tavolo' ? 'success' : 'neutral'}>{item.status}</Badge>
          </Card>
        ))}
      </div>
    </div>
  )
}
