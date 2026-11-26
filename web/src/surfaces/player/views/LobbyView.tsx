import { useQuery } from '@tanstack/react-query'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'
import { Link } from 'react-router'
import { getPlayerDependencies } from '../composition-root'

const dependencies = getPlayerDependencies()

export function LobbyView() {
  const isOnline = useNetworkStatus()

  const sessionQuery = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: () => dependencies.auth.getStoredSession(),
  })

  const venuesQuery = useQuery({
    queryKey: ['catalog', 'venues'],
    queryFn: () => dependencies.catalog.listVenues(),
  })

  const firstVenue = venuesQuery.data?.[0]
  const tablesQuery = useQuery({
    queryKey: ['catalog', 'tables', firstVenue?.id],
    queryFn: () => dependencies.catalog.listTables(firstVenue!.id),
    enabled: Boolean(firstVenue?.id),
  })

  const occupiedTable = tablesQuery.data?.find((t) => t.status === 'OCCUPIED') ?? tablesQuery.data?.[0]

  return (
    <div className="cg-mobile-page space-y-8">
      <header className="space-y-1">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">La tua arena</p>
            <h1 className="mt-1 text-3xl font-bold tracking-tight">
              {sessionQuery.data ? `Sessione ${sessionQuery.data.kind}` : 'Benvenuto'}
            </h1>
            <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Tutto quello che ti serve per giocare.</p>
          </div>
          <Badge tone={isOnline ? 'success' : 'warning'}>{isOnline ? 'Online' : 'Offline'}</Badge>
        </div>
      </header>

      {occupiedTable && (
        <Card elevated className="space-y-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">
                {occupiedTable.status === 'OCCUPIED' ? 'Partita in corso' : 'Tavolo disponibile'}
              </p>
              <p className="mt-1 text-base font-semibold">{firstVenue?.name}</p>
              <p className="text-xs text-[var(--cg-color-label-secondary)]">{occupiedTable.name}</p>
            </div>
            <Badge tone={occupiedTable.status === 'OCCUPIED' ? 'blue' : 'neutral'}>
              {occupiedTable.status}
            </Badge>
          </div>
          <Link to={`/match/${occupiedTable.id}`} className="cg-button cg-button-primary block text-center">
            Apri tavolo
          </Link>
        </Card>
      )}

      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">Scopri</p>
          <h2 className="mt-1 text-xl font-bold">Locali</h2>
        </div>
        <Link to="/" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">Scansiona QR</Link>
      </div>

      {venuesQuery.isPending ? (
        <Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento locali…</p></Card>
      ) : venuesQuery.isError ? (
        <Card><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare i locali.</p></Card>
      ) : venuesQuery.data.length === 0 ? (
        <Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessun locale disponibile.</p></Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {venuesQuery.data.map((venue) => (
            <Card elevated key={venue.id} className="space-y-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{venue.name}</p>
                  <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">{venue.city}</p>
                </div>
                <Badge tone="neutral">{venue.type}</Badge>
              </div>
              {venue.id === firstVenue?.id && occupiedTable && (
                <Link to={`/table/${occupiedTable.code}`} className="cg-button cg-button-secondary block text-center">
                  Vedi tavolo {occupiedTable.code}
                </Link>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
