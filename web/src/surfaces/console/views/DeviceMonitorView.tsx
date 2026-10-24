import { Link } from 'react-router'

export function DeviceMonitorView() {
  const devices = [
    { id: 'kiosk-01', type: 'Totem Touch 43"', status: 'Online', ip: '192.168.1.101', latency: '4ms' },
    { id: 'kiosk-02', type: 'Totem Touch 43"', status: 'Online', ip: '192.168.1.102', latency: '6ms' },
    { id: 'android-box-01', type: 'Capacitor Android TV', status: 'Online', ip: '192.168.1.150', latency: '12ms' },
  ]

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

      <div className="overflow-hidden rounded-xl border border-neutral-800 bg-neutral-900">
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
            {devices.map(d => (
              <tr key={d.id} className="hover:bg-neutral-800/40 transition">
                <td className="p-4 font-mono font-medium text-white">{d.id}</td>
                <td className="p-4 text-neutral-300">{d.type}</td>
                <td className="p-4 font-mono text-xs text-neutral-400">{d.ip}</td>
                <td className="p-4 font-mono text-xs text-purple-400">{d.latency}</td>
                <td className="p-4 text-right">
                  <span className="px-2 py-0.5 rounded text-xs font-semibold bg-emerald-500/10 text-emerald-400">
                    {d.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
