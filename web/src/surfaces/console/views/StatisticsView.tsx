import { useQuery } from '@tanstack/react-query'
import { Card } from '@ui'
import { useConsoleDependencies } from '../dependencies'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

export function StatisticsView() {
  const { catalog, stats } = useConsoleDependencies()

  const venuesQuery = useQuery({
    queryKey: ['catalog', 'venues'],
    queryFn: () => catalog.listVenues(),
  })

  const primaryVenueId = venuesQuery.data?.[0]?.id

  const summaryQuery = useQuery({
    queryKey: ['stats', 'venue', primaryVenueId],
    queryFn: () => stats.getVenueSummary(primaryVenueId!),
    enabled: Boolean(primaryVenueId),
  })

  const summary = summaryQuery.data

  return (
    <AdminSectionShell title="Statistiche" description="Analizza utilizzo, partite e performance del locale.">
      {venuesQuery.isPending || summaryQuery.isPending ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento statistiche…</p></Card>
      ) : venuesQuery.isError || summaryQuery.isError ? (
        <Card elevated><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare le statistiche del locale.</p></Card>
      ) : !summary ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessuna statistica disponibile per questo locale.</p></Card>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <AdminMetric label="Partite giocate" value={String(summary.matches)} detail="Partite completate" />
            <AdminMetric label="Punti totali" value={String(summary.totalPoints)} detail="Somma punti segnati" />
            <AdminMetric label="Durata media" value={`${summary.averageDurationSeconds}s`} detail="Per partita" />
          </div>

          {summary.byGameType.length > 0 && (
            <Card elevated>
              <h2 className="text-lg font-semibold">Dettaglio per tipologia di gioco</h2>
              <div className="mt-4 space-y-3">
                {summary.byGameType.map((gt) => (
                  <div key={gt.gameTypeId} className="flex items-center justify-between border-b border-[var(--cg-color-border-subtle)] pb-2 text-sm last:border-0">
                    <span className="font-mono text-xs">{gt.gameTypeId}</span>
                    <span>{gt.matches} partite · {gt.totalPoints} punti · media {gt.averageDurationSeconds}s</span>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </>
      )}
    </AdminSectionShell>
  )
}
