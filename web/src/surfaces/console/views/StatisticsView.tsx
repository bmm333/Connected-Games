import { Card } from '@ui'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

export function StatisticsView() {
  return (
    <AdminSectionShell title="Statistiche" description="Analizza utilizzo, partite e performance del locale.">
      <div className="grid gap-4 sm:grid-cols-3">
        <AdminMetric label="Partite giocate" value="1.284" detail="+18% questa settimana" />
        <AdminMetric label="Tempo di gioco" value="246h" detail="+9% questa settimana" />
        <AdminMetric label="Giocatori unici" value="438" detail="+24 nuovi giocatori" />
      </div>
      <Card elevated>
        <h2 className="text-lg font-semibold">Attività ultimi 7 giorni</h2>
        <div className="mt-8 flex h-48 items-end gap-3 border-b border-[var(--cg-color-border-subtle)]">
          {[42, 65, 52, 80, 72, 94, 68].map((height, index) => <div key={index} className="flex-1 rounded-t-md bg-[var(--cg-color-accent)]" style={{ height: `${height}%` }} />)}
        </div>
        <div className="mt-3 flex justify-between text-xs text-[var(--cg-color-label-tertiary)]">{['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'].map((day) => <span key={day}>{day}</span>)}</div>
      </Card>
    </AdminSectionShell>
  )
}
