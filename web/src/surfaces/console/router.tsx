import { HashRouter, Route, Routes } from 'react-router'
import { ConsoleShellLayout } from './components/ConsoleShellLayout'
import { RoleProtectedRoute } from './components/RoleProtectedRoute'
import { ConsoleLoginPage } from './pages/LoginPage/ConsoleLoginPage'
import { ConsoleQrPage } from './pages/QrPage/QrPage'
import { AddHubView } from './views/AddHubView'
import { DashboardView } from './views/DashboardView'
import { DeviceMonitorView } from './views/DeviceMonitorView'
import { LiveMatchesView } from './views/LiveMatchesView'
import { LocalOverviewView } from './views/LocalOverviewView'
import { ScreensContentView } from './views/ScreensContentView'
import { StatisticsView } from './views/StatisticsView'
import { SimulatorView } from './views/SimulatorView'
import { TablesView } from './views/TablesView'
import { TournamentManagerView } from './views/TournamentManagerView'

/**
 * Dedicated router for the Console / Admin surface.
 */
export function ConsoleRouter() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/" element={<ConsoleQrPage />} />
        <Route path="/login" element={<ConsoleLoginPage />} />
        <Route element={<ConsoleShellLayout />}>
          <Route
            path="/overview"
            element={
              <RoleProtectedRoute permission="overview:view">
                <LocalOverviewView />
              </RoleProtectedRoute>
            }
          />
          <Route
            path="/tables"
            element={
              <RoleProtectedRoute permission="tables:read">
                <TablesView />
              </RoleProtectedRoute>
            }
          />
          <Route
            path="/devices"
            element={
              <RoleProtectedRoute permission="devices:read">
                <DeviceMonitorView />
              </RoleProtectedRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <RoleProtectedRoute permission="overview:view">
                <DashboardView />
              </RoleProtectedRoute>
            }
          />
          <Route path="/live" element={<RoleProtectedRoute permission="tables:read"><LiveMatchesView /></RoleProtectedRoute>} />
          <Route path="/tournaments" element={<RoleProtectedRoute permission="tables:write"><TournamentManagerView /></RoleProtectedRoute>} />
          <Route path="/devices/add" element={<RoleProtectedRoute permission="devices:write"><AddHubView /></RoleProtectedRoute>} />
          <Route path="/statistics" element={<RoleProtectedRoute permission="overview:view"><StatisticsView /></RoleProtectedRoute>} />
          <Route path="/screens" element={<RoleProtectedRoute permission="overview:view"><ScreensContentView /></RoleProtectedRoute>} />
          <Route path="/simulator" element={<RoleProtectedRoute permission="overview:view"><SimulatorView /></RoleProtectedRoute>} />
        </Route>
      </Routes>
    </HashRouter>
  )
}
