import { Badge, Card } from '@ui'
import { useLocalOverview } from '../hooks/useConsoleQueries'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

export function LocalOverviewView() {
  const overview = useLocalOverview()

  if (overview.isPending) {
    return <AdminSectionShell title="Local Overview" description="Stato operativo del nodo locale.">Caricamento…</AdminSectionShell>
  }
  if (overview.isError) {
    return <AdminSectionShell title="Local Overview" description="Stato operativo del nodo locale.">Impossibile caricare lo stato locale.</AdminSectionShell>
  }

  const { metrics } = overview.data
  const statusTone = overview.data.status === 'healthy' ? 'success' : overview.data.status === 'degraded' ? 'warning' : 'destructive'
  return (
    <AdminSectionShell title="Local Overview" description="Stato operativo del nodo locale.">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <AdminMetric label="Salute sistema" value={overview.data.status} detail={`Aggiornato ${new Date(overview.data.observedAt).toLocaleTimeString()}`} />
        <AdminMetric label="Memoria" value={`${metrics.memoryUsagePercent}%`} detail="Utilizzo corrente" />
        <AdminMetric label="Throughput" value={`${metrics.throughputPerSecond}/s`} detail="Eventi elaborati" />
        <AdminMetric label="Alert attivi" value={String(metrics.activeAlerts)} detail="Richiedono attenzione" />
      </div>
      <Card elevated className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold">Stato del nodo</h2>
          <p className="text-sm text-[var(--cg-color-label-secondary)]">Monitoraggio aggiornato automaticamente.</p>
        </div>
        <Badge tone={statusTone}>{overview.data.status}</Badge>
      </Card>
    </AdminSectionShell>
  )
}
