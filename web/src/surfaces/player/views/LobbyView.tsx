import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'

export function LobbyView() {
  const isOnline = useNetworkStatus()

  return (
    <div className="cg-mobile-page space-y-5">
      <header className="space-y-1">
        <p className="text-xs text-[var(--cg-color-label-secondary)]">Partita · Player</p>
        <h1 className="text-2xl font-bold tracking-tight">Ciao, Giulia</h1>
        <p className="text-sm text-[var(--cg-color-label-secondary)]">In corso vicino a te</p>
      </header>
      <Card elevated className="space-y-4">
        <div className="flex items-center justify-between">
          <div><p className="text-sm font-semibold">Bar Belvedere · 350 m</p><p className="text-xs text-[var(--cg-color-label-secondary)]">Calciobalilla · Tavolo 2</p></div>
        <div className="flex items-center gap-3">
          <Badge tone="blue">In partita</Badge>
        </div>
        </div>
        <div className="flex items-end justify-between"><div><p className="text-sm">Fulmini · Rossi</p><p className="text-xs text-[var(--cg-color-label-secondary)]">09:48 · Primo a 10</p></div><strong className="text-5xl">7—5</strong></div>
        <Link to="/match/quick-match" className="cg-button cg-button-primary block text-center">Vai alla partita</Link>
      </Card>
      <div className="flex items-center justify-between"><h2 className="text-lg font-bold">I tuoi locali preferiti</h2><Link to="/lobby" className="text-sm text-[var(--cg-color-accent-label)]">Vedi tutti</Link></div>
      <div className="grid gap-4 md:grid-cols-2">
        <Card elevated className="space-y-3">
          <p className="font-semibold">Bar Belvedere</p><p className="text-sm text-[var(--cg-color-label-secondary)]">Roma · San Lorenzo</p><Badge tone={isOnline ? 'success' : 'warning'}>{isOnline ? 'Online' : 'Offline'}</Badge>
          <Link to="/table/BELV-02" className="cg-button cg-button-secondary block text-center">Vedi locale</Link>
        </Card>
        <Card elevated><p className="font-semibold">Coppa Aurora</p><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Sabato 24 ottobre · 20:30</p><Badge tone="neutral" className="mt-4">Iscrizioni aperte</Badge></Card>
      </div>
    </div>
  )
}
