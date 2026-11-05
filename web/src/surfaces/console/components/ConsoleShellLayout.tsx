import { Link, Outlet, useLocation } from 'react-router'
import type { Permission } from '@domain/console/auth/Role'
import { Button, ThemeToggle } from '@ui'
import { useConsoleAuth } from '../auth/useConsoleAuth'

const navigation: Array<{
  label: string
  to: string
  permission: Permission
}> = [
  { label: 'Local Overview', to: '/overview', permission: 'overview:view' },
  { label: 'Tables', to: '/tables', permission: 'tables:read' },
  { label: 'Devices', to: '/devices', permission: 'devices:read' },
  { label: 'Live matches', to: '/live', permission: 'tables:read' },
  { label: 'Simulator', to: '/simulator', permission: 'overview:view' },
  { label: 'Statistics', to: '/statistics', permission: 'overview:view' },
]

export function ConsoleShellLayout() {
  const location = useLocation()
  const { session, canAccess, logout } = useConsoleAuth()
  const visibleNavigation = navigation.filter(({ permission }) =>
    canAccess(permission),
  )

  return (
    <div className="cg-app-shell cg-app-shell-console">
      <aside className="cg-sidebar">
        <Link to="/overview" className="cg-brand">
          <span className="cg-brand-mark">P</span>
          <span>Partita Console</span>
        </Link>
        <nav className="cg-sidebar-nav" aria-label="Navigazione Console">
          {visibleNavigation.map(({ label, to }) => (
            <Link
              key={to}
              to={to}
              className={`cg-nav-link${location.pathname === to ? ' is-active' : ''}`}
            >
              {label}
            </Link>
          ))}
        </nav>
        <div className="cg-sidebar-footer">
          <span className="cg-status-dot" />
          <span>{session?.role}</span>
        </div>
      </aside>
      <div className="cg-main">
        <header className="cg-topbar">
          <div className="cg-topbar-copy">
            <span className="cg-eyebrow">Workspace venue</span>
            <h1>Console operativa</h1>
          </div>
          <div className="cg-topbar-actions">
            <span aria-label="Utente corrente">
              {session?.user.avatar ? (
                <img
                  src={session.user.avatar}
                  alt=""
                  className="h-8 w-8 rounded-full"
                />
              ) : (
                session?.user.name
              )}
            </span>
            <ThemeToggle />
            <Button variant="ghost" onClick={() => void logout()}>
              Logout
            </Button>
          </div>
        </header>
        <main className="cg-content">
          <Outlet />
        </main>
      </div>
      <nav className="cg-mobile-nav" aria-label="Navigazione Console">
        {visibleNavigation.map(({ label, to }) => (
          <Link key={to} to={to} className={`cg-mobile-nav-link${location.pathname === to ? ' is-active' : ''}`}>
            <span>{label}</span>
          </Link>
        ))}
      </nav>
    </div>
  )
}
