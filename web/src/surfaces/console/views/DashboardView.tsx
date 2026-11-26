import { Link } from 'react-router'
import { Badge, Card, ThemeToggle } from '@ui'
import { useDevicesList, useLocalOverview } from '../hooks/useConsoleQueries'

export function DashboardView() {
  const overview = useLocalOverview()
  const devices = useDevicesList()

  const totalDevices = devices.data?.length ?? 0
  const onlineDevices = devices.data?.filter((d) => d.status === 'online').length ?? 0

  const stats = [
    {
      label: 'Stato Sistema',
      value: overview.isPending ? '…' : overview.isError ? 'Errore' : overview.data.status,
      change: overview.data ? `Aggiornato ${new Date(overview.data.observedAt).toLocaleTimeString('it-IT')}` : 'In attesa',
    },
    {
      label: 'Dispositivi Online',
      value: devices.isPending ? '…' : devices.isError ? '—' : `${onlineDevices}/${totalDevices}`,
      change: totalDevices > 0 ? `${Math.round((onlineDevices / totalDevices) * 100)}% attivi` : 'Nessun dispositivo',
    },
    {
      label: 'Throughput',
      value: overview.data ? `${overview.data.metrics.throughputPerSecond}/s` : '—',
      change: overview.data ? `Memoria ${overview.data.metrics.memoryUsagePercent}%` : '—',
    },
    {
      label: 'Alert Attivi',
      value: overview.data ? String(overview.data.metrics.activeAlerts) : '—',
      change: overview.data?.metrics.activeAlerts === 0 ? 'Tutto regolare' : 'Verifica richiesta',
    },
  ]

  return (
    <div className="space-y-8">
      <header className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Console di Amministrazione</h1>
          <p className="text-sm text-[var(--cg-color-label-secondary)]">
            Controllo globale dell'ecosistema Connected Games
          </p>
        </div>
        <ThemeToggle />
      </header>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {stats.map((s) => (
          <Card elevated key={s.label}>
            <span className="text-xs uppercase text-[var(--cg-color-label-secondary)] font-medium">{s.label}</span>
            <div className="text-3xl font-mono font-bold mt-2 text-[var(--cg-color-label-primary)]">{s.value}</div>
            <Badge tone="neutral" className="mt-1">{s.change}</Badge>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card elevated>
          <h2 className="text-xl font-semibold mb-2">Gestione Tornei</h2>
          <p className="text-sm text-[var(--cg-color-label-secondary)] mb-4">
            Crea nuovi gironi, gestisci i tabelloni e controlla l'avanzamento dei match.
          </p>
          <Link
            to="/tournaments"
            className="cg-button cg-button-primary inline-flex items-center"
          >
            Apri Gestore Tornei →
          </Link>
        </Card>

        <Card elevated>
          <h2 className="text-xl font-semibold mb-2">Monitoraggio Dispositivi</h2>
          <p className="text-sm text-[var(--cg-color-label-secondary)] mb-4">
            Stato dei totem Kiosk, sincronizzazione Capacitor Android e canali MQTT.
          </p>
          <Link
            to="/devices"
            className="cg-button cg-button-secondary inline-flex items-center"
          >
            Verifica Dispositivi →
          </Link>
        </Card>
      </div>
    </div>
  )
}
