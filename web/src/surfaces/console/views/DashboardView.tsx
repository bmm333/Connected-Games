import { Link } from 'react-router'
import { Badge, Card, ThemeToggle } from '@ui'

export function DashboardView() {
  const stats = [
    { label: 'Tornei Attivi', value: '4', change: '+1 oggi' },
    { label: 'Totem Connessi', value: '6/6', change: '100% online' },
    { label: 'Giocatori Registrati', value: '128', change: '+24 questa settimana' },
    { label: 'Match in Corso', value: '8', change: 'Latenza media 18ms' },
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
        {stats.map(s => (
          <Card elevated key={s.label}>
            <span className="text-xs uppercase text-[var(--cg-color-label-secondary)] font-medium">{s.label}</span>
            <div className="text-3xl font-mono font-bold mt-2 text-[var(--cg-color-label-primary)]">{s.value}</div>
            <Badge tone="success" className="mt-1">{s.change}</Badge>
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
