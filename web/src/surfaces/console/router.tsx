import { HashRouter, Routes, Route } from 'react-router'
import { DashboardView } from './views/DashboardView'
import { TournamentManagerView } from './views/TournamentManagerView'
import { DeviceMonitorView } from './views/DeviceMonitorView'

/**
 * Dedicated router for the Console / Admin surface.
 */
export function ConsoleRouter() {
  return (
    <HashRouter>
      <main className="max-w-6xl mx-auto p-6 md:p-8 min-h-screen">
        <Routes>
          <Route path="/" element={<DashboardView />} />
          <Route path="/tournaments" element={<TournamentManagerView />} />
          <Route path="/devices" element={<DeviceMonitorView />} />
        </Routes>
      </main>
    </HashRouter>
  )
}
