import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'
import { Link } from 'react-router'

export function LobbyView() {
  const isOnline = useNetworkStatus()

  return (
    <div className="cg-mobile-page space-y-8">
      <header className="space-y-1">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">La tua arena</p>
            <h1 className="mt-1 text-3xl font-bold tracking-tight">Ciao, Giulia</h1>
            <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Tutto quello che ti serve per giocare.</p>
          </div>
          <Badge tone={isOnline ? 'success' : 'warning'}>{isOnline ? 'Online' : 'Offline'}</Badge>
        </div>
      </header>
      <Card elevated className="space-y-5">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">Partita in corso</p>
            <p className="mt-1 text-base font-semibold">Bar Belvedere · 350 m</p>
            <p className="text-xs text-[var(--cg-color-label-secondary)]">Calciobalilla · Tavolo 2</p>
          </div>
          <Badge tone="blue">LIVE</Badge>
        </div>
        <div className="flex items-end justify-between gap-4 border-y border-[var(--cg-color-border-subtle)] py-4">
          <div><p className="text-sm font-semibold">Fulmini · Rossi</p><p className="mt-1 text-xs text-[var(--cg-color-label-secondary)]">09:48 · Primo a 10</p></div>
          <strong className="text-5xl tracking-tight">7<span className="mx-1 text-[var(--cg-color-label-tertiary)]">—</span>5</strong>
        </div>
        <Link to="/match/quick-match" className="cg-button cg-button-primary block text-center">Rientra nella partita</Link>
      </Card>
      <div className="flex items-end justify-between gap-4"><div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-[var(--cg-color-accent-label)]">Scopri</p><h2 className="mt-1 text-xl font-bold">I tuoi locali preferiti</h2></div><Link to="/lobby" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">Vedi tutti</Link></div>
      <div className="grid gap-4 md:grid-cols-2">
        <Card elevated className="space-y-3">
          <div className="flex items-start justify-between gap-3"><div><p className="font-semibold">Bar Belvedere</p><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Roma · San Lorenzo</p></div><span className="cg-status-dot" /></div>
          <Badge tone="neutral">2 tavoli disponibili</Badge>
          <Link to="/table/BELV-02" className="cg-button cg-button-secondary block text-center">Vedi locale</Link>
        </Card>
        <Card elevated className="space-y-3"><div className="flex items-start justify-between gap-3"><div><p className="font-semibold">Coppa Aurora</p><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Sabato 24 ottobre · 20:30</p></div><Badge tone="neutral">Torneo</Badge></div><p className="text-sm text-[var(--cg-color-label-secondary)]">Iscriviti e sfida la community del quartiere.</p><Link to="/table/BELV-02" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">Scopri l'evento →</Link></Card>
      </div>
    </div>
  )
}
