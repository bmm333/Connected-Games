import { Link } from 'react-router'
import { Badge, Button, Card } from '@ui'
import { useDevicesList, useExecuteDeviceCommand } from '../hooks/useConsoleQueries'

export function DeviceMonitorView() {
  const devices = useDevicesList()
  const command = useExecuteDeviceCommand()

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center pb-4 border-b border-neutral-800">
        <div>
          <h1 className="text-3xl font-bold">Monitoraggio Hardware & Dispositivi</h1>
          <p className="text-sm text-neutral-400">Verifica lo stato delle postazioni fisiche</p>
        </div>
        <Link
          to="/"
          className="px-4 py-2 text-sm font-medium rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800 transition"
        >
          ← Dashboard
        </Link>
      </div>

      {devices.isPending ? <p>Caricamento…</p> : devices.isError ? <p>Impossibile caricare i dispositivi.</p> : <Card elevated className="overflow-hidden p-0">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-neutral-800 text-neutral-400 text-xs uppercase tracking-wider">
              <th className="p-4">ID Dispositivo</th>
              <th className="p-4">Tipologia</th>
              <th className="p-4">Indirizzo IP</th>
              <th className="p-4">Latenza</th>
              <th className="p-4 text-right">Stato</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-850">
            {devices.data.map(d => (
              <tr key={d.id} className="hover:bg-neutral-800/40 transition">
                <td className="p-4 font-mono font-medium text-white">{d.id}</td>
                <td className="p-4 text-neutral-300">{d.surfaceType}</td>
                <td className="p-4 font-mono text-xs text-neutral-400">{d.hardwareInfo?.ip ?? '—'}</td>
                <td className="p-4 font-mono text-xs text-purple-400">{d.lastPing ?? 'Mai'}</td>
                <td className="p-4 text-right">
                  <Badge tone={d.status === 'online' ? 'success' : 'destructive'}>{d.status}</Badge>
                  <Button className="ml-2" variant="secondary" disabled={command.isPending} onClick={() => command.mutate({ deviceId: d.id, command: 'sync' })}>Sync</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>}
    </div>
  )
}
