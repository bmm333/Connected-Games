import { HashRouter, Routes, Route } from 'react-router'
import { DashboardView } from './views/DashboardView'
import { TournamentManagerView } from './views/TournamentManagerView'
import { DeviceMonitorView } from './views/DeviceMonitorView'
import { ConsoleQrPage } from './pages/QrPage/QrPage'
import { TablesView } from './views/TablesView'
import { LiveMatchesView } from './views/LiveMatchesView'
import { AddHubView } from './views/AddHubView'
import { StatisticsView } from './views/StatisticsView'
import { ScreensContentView } from './views/ScreensContentView'
import { AppShell } from '@ui'

/**
 * Dedicated router for the Console / Admin surface.
 */
export function ConsoleRouter() {
  return (
    <HashRouter>
      <AppShell surface="console" title="Venue admin" subtitle="Controllo globale dell'ecosistema Connected Games">
        <Routes>
          <Route path="/" element={<ConsoleQrPage />} />
          <Route path="/dashboard" element={<DashboardView />} />
          <Route path="/tables" element={<TablesView />} />
          <Route path="/live" element={<LiveMatchesView />} />
          <Route path="/tournaments" element={<TournamentManagerView />} />
          <Route path="/devices" element={<DeviceMonitorView />} />
          <Route path="/devices/add" element={<AddHubView />} />
          <Route path="/statistics" element={<StatisticsView />} />
          <Route path="/screens" element={<ScreensContentView />} />
        </Routes>
      </AppShell>
    </HashRouter>
  )
}
