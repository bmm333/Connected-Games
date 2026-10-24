import { HashRouter, Routes, Route } from 'react-router'
import { LobbyView } from './views/LobbyView'
import { MatchView } from './views/MatchView'
import { ProfileView } from './views/ProfileView'
import { TableLookupView } from './views/TableLookupView'

/**
 * Dedicated, lightweight router for the Player surface.
 * Uses HashRouter for robust behavior in Capacitor WebViews & PWA offline states.
 */
export function PlayerRouter() {
  return (
    <HashRouter>
      <main className="max-w-2xl mx-auto p-4 md:p-6 min-h-screen">
        <Routes>
          <Route path="/" element={<LobbyView />} />
          <Route path="/match/:id" element={<MatchView />} />
          <Route path="/profile" element={<ProfileView />} />
          <Route path="/table/:code" element={<TableLookupView />} />
        </Routes>
      </main>
    </HashRouter>
  )
}
