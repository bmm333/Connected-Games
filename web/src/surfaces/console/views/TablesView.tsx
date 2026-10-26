import { Badge, Card } from '@ui'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

const tables = [
  { name: 'Tavolo 01', game: 'Calciobalilla', status: 'Disponibile', players: '0/4' },
  { name: 'Tavolo 02', game: 'Air Hockey', status: 'In partita', players: '2/2' },
  { name: 'Tavolo 03', game: 'Scacchi', status: 'Disponibile', players: '0/2' },
  { name: 'Tavolo 04', game: 'Freccette 501', status: 'Manutenzione', players: '—' },
]

export function TablesView() {
  return (
    <AdminSectionShell title="Tavoli" description="Gestisci le postazioni e lo stato dei giochi installati.">
      <div className="grid gap-4 sm:grid-cols-3">
        <AdminMetric label="Postazioni totali" value="12" detail="8 disponibili" />
        <AdminMetric label="In partita" value="4" detail="12 giocatori attivi" />
        <AdminMetric label="Utilizzo oggi" value="68%" detail="+12% rispetto a ieri" />
      </div>
      <Card elevated className="overflow-x-auto p-0">
        <table className="w-full min-w-[600px] text-left text-sm">
          <thead className="border-b border-[var(--cg-color-border-subtle)] text-[var(--cg-color-label-secondary)]">
            <tr><th className="p-4">Postazione</th><th className="p-4">Gioco</th><th className="p-4">Giocatori</th><th className="p-4 text-right">Stato</th></tr>
          </thead>
          <tbody>
            {tables.map((table) => (
              <tr key={table.name} className="border-b border-[var(--cg-color-border-subtle)] last:border-0">
                <td className="p-4 font-semibold">{table.name}</td><td className="p-4 text-[var(--cg-color-label-secondary)]">{table.game}</td><td className="p-4 font-mono">{table.players}</td>
                <td className="p-4 text-right"><Badge tone={table.status === 'Disponibile' ? 'success' : table.status === 'In partita' ? 'blue' : 'warning'}>{table.status}</Badge></td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </AdminSectionShell>
  )
}
