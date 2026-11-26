import { Badge, Card } from '@ui'
import { useDevicesList } from '../hooks/useConsoleQueries'
import { AdminSectionShell } from './AdminSectionShell'

export function ScreensContentView() {
  const devices = useDevicesList()

  return (
    <AdminSectionShell title="Schermi e contenuti" description="Configura i contenuti mostrati sui display del locale.">
      {devices.isPending ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento display…</p></Card>
      ) : devices.isError ? (
        <Card elevated><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare i display.</p></Card>
      ) : devices.data.length === 0 ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessun display associato al momento.</p></Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-3">
          {devices.data.map((device) => (
            <Card elevated key={device.id} className="space-y-5">
              <div className="flex items-center justify-between">
                <h2 className="font-semibold font-mono text-sm">{device.id}</h2>
                <Badge tone={device.status === 'online' ? 'success' : 'warning'}>{device.status}</Badge>
              </div>
              <div className="flex aspect-video items-center justify-center rounded-lg bg-[var(--cg-color-bg-grouped)] text-center text-sm text-[var(--cg-color-label-secondary)]">
                Surface: {device.surfaceType}
              </div>
            </Card>
          ))}
        </div>
      )}
    </AdminSectionShell>
  )
}
