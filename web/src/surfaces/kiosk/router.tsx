import { HashRouter, Routes, Route } from 'react-router'
import { AttractView } from './views/AttractView'
import { QueueView } from './views/QueueView'
import { LeaderboardView } from './views/LeaderboardView'

/**
 * Dedicated, lightweight router for the Kiosk/Totem surface.
 */
export function KioskRouter() {
  return (
    <HashRouter>
      <main className="min-h-screen">
        <Routes>
          <Route path="/" element={<AttractView />} />
          <Route path="/queue" element={<QueueView />} />
          <Route path="/leaderboard" element={<LeaderboardView />} />
        </Routes>
      </main>
    </HashRouter>
  )
}
