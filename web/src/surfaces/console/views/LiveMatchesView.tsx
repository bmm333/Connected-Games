import { useQuery } from '@tanstack/react-query'
import type { Table } from '@domain/catalog/TableLookup'
import type { Match } from '@domain/simulator'
import { Badge, Card } from '@ui'
import { useConsoleDependencies } from '../dependencies'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

interface LiveTableMatch {
  readonly table: Table
  readonly match: Match
}

export function LiveMatchesView() {
  const { catalog, matches } = useConsoleDependencies()

  const liveQuery = useQuery({
    queryKey: ['console', 'live-matches'],
    queryFn: async (): Promise<readonly LiveTableMatch[]> => {
      const venues = await catalog.listVenues()
      const tablesByVenue = await Promise.all(venues.map((v) => catalog.listTables(v.id)))
      const allTables = tablesByVenue.flat()
      const matchResults = await Promise.all(
        allTables.map(async (table) => {
          const match = await matches.getCurrentMatch(table.id)
          return match && match.status === 'IN_PROGRESS' ? { table, match } : undefined
        }),
      )
      return matchResults.filter((item): item is LiveTableMatch => item !== undefined)
    },
    refetchInterval: 10_000,
  })

  const activeMatches = liveQuery.data ?? []

  return (
    <AdminSectionShell title="Partite live" description="Controlla in tempo reale le partite attive nel locale.">
      <div className="grid gap-4 sm:grid-cols-3">
        <AdminMetric label="Partite in corso" value={liveQuery.isPending ? '…' : String(activeMatches.length)} detail="Aggiornamento automatico" />
        <AdminMetric label="Stato feed" value={liveQuery.isError ? 'Errore' : 'Attivo'} detail="Polling ogni 10s" />
        <AdminMetric label="Tavoli monitorati" value={liveQuery.isPending ? '…' : String(activeMatches.length)} detail="Con partita attiva" />
      </div>

      {liveQuery.isPending ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento partite in corso…</p></Card>
      ) : liveQuery.isError ? (
        <Card elevated><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare le partite live.</p></Card>
      ) : activeMatches.length === 0 ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessuna partita in corso al momento.</p></Card>
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {activeMatches.map(({ table, match }) => {
            const red = match.sides.find((s) => s.side === 'RED')
            const blue = match.sides.find((s) => s.side === 'BLUE')
            return (
              <Card elevated key={match.id} className="space-y-5">
                <div className="flex items-center justify-between">
                  <span className="font-semibold">{table.name}</span>
                  <Badge tone="success">LIVE</Badge>
                </div>
                <div className="flex items-center justify-between text-lg">
                  <span>{red?.displayName ?? 'Rosso'}</span>
                  <strong>{red?.score ?? 0}</strong>
                </div>
                <div className="flex items-center justify-between text-lg">
                  <span>{blue?.displayName ?? 'Blu'}</span>
                  <strong>{blue?.score ?? 0}</strong>
                </div>
                <p className="text-sm text-[var(--cg-color-label-secondary)]">
                  Avviata alle {new Date(match.startedAt).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })}
                </p>
              </Card>
            )
          })}
        </div>
      )}
    </AdminSectionShell>
  )
}
