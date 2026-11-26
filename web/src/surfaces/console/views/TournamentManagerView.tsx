import { Link } from 'react-router'
import { Card } from '@ui'

export function TournamentManagerView() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-3xl font-bold">Gestione Tornei</h1>
          <p className="text-sm text-neutral-400">Pianifica, avvia e monitora le competizioni</p>
        </div>
        <Link
          to="/"
          className="px-4 py-2 text-sm font-medium rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800 transition"
        >
          ← Dashboard
        </Link>
      </div>

      <Card elevated>
        <p className="text-sm text-[var(--cg-color-label-secondary)]">
          Nessun torneo attivo o configurato al momento.
        </p>
      </Card>
    </div>
  )
}
