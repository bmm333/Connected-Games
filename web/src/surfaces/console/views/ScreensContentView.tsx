import { Badge, Card } from '@ui'
import { AdminSectionShell } from './AdminSectionShell'

const screens = [
  ['Display tavolo 01', 'Lobby Calciobalilla', 'Online'],
  ['Display tavolo 02', 'Partita live', 'Online'],
  ['Monitor sala', 'Classifica torneo', 'Offline'],
]

export function ScreensContentView() {
  return (
    <AdminSectionShell title="Schermi e contenuti" description="Configura i contenuti mostrati sui display del locale.">
      <div className="grid gap-4 md:grid-cols-3">
        {screens.map(([name, content, status]) => (
          <Card elevated key={name} className="space-y-5">
            <div className="flex items-center justify-between"><h2 className="font-semibold">{name}</h2><Badge tone={status === 'Online' ? 'success' : 'warning'}>{status}</Badge></div>
            <div className="flex aspect-video items-center justify-center rounded-lg bg-[var(--cg-color-bg-grouped)] text-center text-sm text-[var(--cg-color-label-secondary)]">{content}</div>
            <button type="button" className="cg-button cg-button-secondary w-full">Configura contenuto</button>
          </Card>
        ))}
      </div>
    </AdminSectionShell>
  )
}
