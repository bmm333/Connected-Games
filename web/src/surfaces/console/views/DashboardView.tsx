import { Link } from 'react-router'

export function DashboardView() {
  const stats = [
    { label: 'Tornei Attivi', value: '4', change: '+1 oggi' },
    { label: 'Totem Connessi', value: '6/6', change: '100% online' },
    { label: 'Giocatori Registrati', value: '128', change: '+24 questa settimana' },
    { label: 'Match in Corso', value: '8', change: 'Latenza media 18ms' },
  ]

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Console di Amministrazione</h1>
        <p className="text-sm text-neutral-400">
          Controllo globale dell'ecosistema Connected Games
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {stats.map(s => (
          <div key={s.label} className="p-5 rounded-xl bg-neutral-900 border border-neutral-800">
            <span className="text-xs uppercase text-neutral-400 font-medium">{s.label}</span>
            <div className="text-3xl font-mono font-bold mt-2 text-white">{s.value}</div>
            <span className="text-xs text-purple-400 mt-1 block">{s.change}</span>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="p-6 rounded-xl bg-neutral-900 border border-neutral-800">
          <h2 className="text-xl font-semibold mb-2">Gestione Tornei</h2>
          <p className="text-sm text-neutral-400 mb-4">
            Crea nuovi gironi, gestisci i tabelloni e controlla l'avanzamento dei match.
          </p>
          <Link
            to="/tournaments"
            className="px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-sm font-medium transition inline-block"
          >
            Apri Gestore Tornei →
          </Link>
        </div>

        <div className="p-6 rounded-xl bg-neutral-900 border border-neutral-800">
          <h2 className="text-xl font-semibold mb-2">Monitoraggio Dispositivi</h2>
          <p className="text-sm text-neutral-400 mb-4">
            Stato dei totem Kiosk, sincronizzazione Capacitor Android e canali MQTT.
          </p>
          <Link
            to="/devices"
            className="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 text-white rounded-lg text-sm font-medium transition inline-block"
          >
            Verifica Dispositivi →
          </Link>
        </div>
      </div>
    </div>
  )
}
