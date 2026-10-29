import { Link, useLocation } from 'react-router'
import { BarChart3, Cpu, Gamepad2, LayoutDashboard, Monitor, Settings, Trophy, Users } from 'lucide-react'
import type { ReactNode } from 'react'
import { ThemeToggle } from './ThemeToggle'

export type AppSurface = 'console' | 'player' | 'kiosk'

interface NavItem {
  label: string
  to: string
  icon: typeof LayoutDashboard
}

const navigation: Record<AppSurface, NavItem[]> = {
  console: [
    { label: 'Panoramica', to: '/dashboard', icon: LayoutDashboard },
    { label: 'Tavoli', to: '/tables', icon: Gamepad2 },
    { label: 'Partite live', to: '/live', icon: Monitor },
    { label: 'Tornei', to: '/tournaments', icon: Trophy },
    { label: 'Dispositivi', to: '/devices', icon: Settings },
    { label: 'Simulator', to: '/simulator', icon: Cpu },
    { label: 'Statistiche', to: '/statistics', icon: BarChart3 },
  ],
  player: [
    { label: 'Lobby', to: '/lobby', icon: LayoutDashboard },
    { label: 'Profilo', to: '/profile', icon: Users },
    { label: 'Statistiche', to: '/statistics', icon: BarChart3 },
  ],
  kiosk: [
    { label: 'Inizio', to: '/attract', icon: LayoutDashboard },
    { label: 'Coda', to: '/queue', icon: Users },
    { label: 'Classifica', to: '/leaderboard', icon: Trophy },
  ],
}

interface AppShellProps {
  surface: AppSurface
  children: ReactNode
  title: string
  subtitle?: string
}

export function AppShell({ surface, children, title, subtitle }: AppShellProps) {
  const location = useLocation()
  if (location.pathname === '/') {
    return <>{children}</>
  }
  const items = navigation[surface]
  const isActive = (to: string) =>
    location.pathname === to || (to === '/dashboard' && location.pathname === '/')

  return (
    <div className={`cg-app-shell cg-app-shell-${surface}`}>
      <aside className="cg-sidebar">
        <Link to={surface === 'console' ? '/dashboard' : surface === 'player' ? '/lobby' : '/attract'} className="cg-brand">
          <span className="cg-brand-mark">P</span>
          <span>Partita</span>
        </Link>
        <nav className="cg-sidebar-nav" aria-label="Navigazione principale">
          {items.map(({ label, to, icon: Icon }) => (
            <Link key={to} to={to} className={`cg-nav-link${isActive(to) ? ' is-active' : ''}`}>
              <Icon size={18} />
              <span>{label}</span>
            </Link>
          ))}
        </nav>
        <div className="cg-sidebar-footer">
          <span className="cg-status-dot" />
          <span>Servizi operativi</span>
        </div>
      </aside>
      <div className="cg-main">
        <header className="cg-topbar">
          <div className="cg-topbar-copy">
            <span className="cg-eyebrow">{surface === 'console' ? 'Workspace venue' : surface === 'player' ? 'Player arena' : 'Arena display'}</span>
            <h1>{title}</h1>
            {subtitle && <p>{subtitle}</p>}
          </div>
          <div className="cg-topbar-actions">
            <span className="cg-online-label"><span className="cg-status-dot" /> Online</span>
            <ThemeToggle />
          </div>
        </header>
        <main className="cg-content">{children}</main>
      </div>
      <nav className="cg-mobile-nav" aria-label="Navigazione mobile">
        {items.slice(0, 4).map(({ label, to, icon: Icon }) => (
          <Link key={to} to={to} className={`cg-mobile-nav-link${isActive(to) ? ' is-active' : ''}`}>
            <Icon size={20} />
            <span>{label}</span>
          </Link>
        ))}
      </nav>
    </div>
  )
}
