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
import { LocalOverviewView } from './views/LocalOverviewView'
import { ConsoleShellLayout } from './components/ConsoleShellLayout'
import { RoleProtectedRoute } from './components/RoleProtectedRoute'
import { ConsoleLoginPage } from './pages/LoginPage/ConsoleLoginPage'

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
        </Route>
      </Routes>
    </HashRouter>
  )
}
