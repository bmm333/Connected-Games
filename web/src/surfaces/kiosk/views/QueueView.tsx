import { Link, useSearchParams } from 'react-router'
import { Card } from '@ui'

export function QueueView() {
  const [searchParams] = useSearchParams()
  const tableId = searchParams.get('tableId')

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Coda di Gioco Totem</h1>
          <p className="text-sm text-neutral-400">
            {tableId ? `Tavolo ${tableId}` : 'Postazione Kiosk'}
          </p>
        </div>
        <Link to="/" className="cg-button cg-button-secondary">Schermata attesa</Link>
      </div>

      <Card elevated>
        <p className="text-sm text-[var(--cg-color-label-secondary)]">
          Nessun giocatore in coda al momento. Scansiona il QR code sul tavolo per iniziare una partita.
        </p>
      </Card>
    </div>
  )
}
