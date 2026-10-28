import { Link } from 'react-router'
import { useNetworkStatus } from '@shared/offline/useNetworkStatus'
import { Badge, Card } from '@ui'

export function LobbyView() {
  const isOnline = useNetworkStatus()

  return (
    <div className="cg-mobile-page space-y-6">
      <header className="space-y-2">
        <div className="flex items-center justify-between gap-3">
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Player · Lobby</p>
          <Badge tone={isOnline ? 'success' : 'warning'}>{isOnline ? 'Connesso' : 'Offline'}</Badge>
        </div>
        <h1 className="text-3xl font-bold tracking-tight">Sei pronto, Giulia?</h1>
        <p className="text-sm text-[var(--cg-color-label-secondary)]">La tua prossima partita sta per iniziare.</p>
      </header>

      <Card elevated className="relative overflow-hidden border-[var(--cg-color-accent)]/30 bg-[var(--cg-color-accent-tinted)] p-5 sm:p-6">
        <div className="absolute -right-12 -top-16 h-40 w-40 rounded-full bg-[var(--cg-color-accent)]/10" aria-hidden="true" />
        <div className="relative space-y-5">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold">Bar Belvedere · 350 m</p>
              <p className="mt-1 text-xs text-[var(--cg-color-label-secondary)]">Calciobalilla · Tavolo 2</p>
            </div>
            <Badge tone="blue">In arrivo</Badge>
          </div>
          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3 text-center">
            <div>
              <div className="mx-auto mb-2 h-3 w-3 rounded-full bg-[var(--cg-color-team-red)]" />
              <p className="font-semibold">Fulmini</p>
              <p className="text-xs text-[var(--cg-color-label-secondary)]">Rossi</p>
            </div>
            <div className="text-xs font-bold uppercase tracking-widest text-[var(--cg-color-label-tertiary)]">VS</div>
            <div>
              <div className="mx-auto mb-2 h-3 w-3 rounded-full bg-[var(--cg-color-team-blue)]" />
              <p className="font-semibold">Blue Crew</p>
              <p className="text-xs text-[var(--cg-color-label-secondary)]">Blu</p>
            </div>
          </div>
          <div className="flex items-center justify-between rounded-xl bg-[var(--cg-color-bg-elevated)]/70 px-4 py-3">
            <div>
              <p className="text-xs text-[var(--cg-color-label-secondary)]">Inizio previsto</p>
              <p className="mt-1 font-semibold">Oggi · 20:30</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-[var(--cg-color-label-secondary)]">Mancano</p>
              <p className="mt-1 font-mono text-xl font-bold text-[var(--cg-color-accent-label)]">00:42</p>
            </div>
          </div>
          <div className="flex items-center justify-between gap-3 text-xs text-[var(--cg-color-label-secondary)]">
            <span>Preparazione dispositivo</span>
            <span className="font-semibold text-[var(--cg-color-status-success)]">Pronto</span>
          </div>
          <div className="h-1.5 overflow-hidden rounded-full bg-[var(--cg-color-bg-elevated)]">
            <div className="h-full w-4/5 rounded-full bg-[var(--cg-color-status-success)]" />
          </div>
          <Link to="/match/quick-match" className="cg-button cg-button-primary block text-center">Apri partita</Link>
        </div>
      </Card>

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold">I tuoi locali preferiti</h2>
          <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Partite e disponibilità vicino a te</p>
        </div>
        <Link to="/lobby" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">Vedi tutti</Link>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <Card elevated className="space-y-3">
          <div className="flex items-center justify-between"><p className="font-semibold">Bar Belvedere</p><Badge tone="success">Aperto</Badge></div>
          <p className="text-sm text-[var(--cg-color-label-secondary)]">Roma · San Lorenzo</p>
          <p className="text-xs text-[var(--cg-color-label-tertiary)]">3 tavoli disponibili · 8 min a piedi</p>
          <Link to="/table/BELV-02" className="cg-button cg-button-secondary block text-center">Vedi locale</Link>
        </Card>
        <Card elevated className="space-y-3"><div className="flex items-center justify-between"><p className="font-semibold">Coppa Aurora</p><Badge tone="neutral">Evento</Badge></div><p className="text-sm text-[var(--cg-color-label-secondary)]">Sabato 24 ottobre · 20:30</p><p className="text-xs text-[var(--cg-color-label-tertiary)]">16 partecipanti già iscritti</p><Link to="/lobby" className="cg-button cg-button-secondary block text-center">Scopri evento</Link></Card>
      </div>
    </div>
  )
}
