import { Link } from 'react-router'
import { Badge, Card, ThemeToggle } from '@ui'

export function LiveDisplayView() {
  const pathState = window.location.hash.split('/')[1]
  const final = pathState === 'final'
  const offline = pathState === 'offline'
  return (
    <main className="min-h-[78vh] p-6 sm:p-10">
      <header className="flex items-start justify-between gap-4"><div><p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Partita · Tavolo 02</p><h1 className="mt-2 text-3xl font-bold sm:text-5xl">{final ? 'Finale' : 'In diretta'}</h1></div><div className="flex items-center gap-3"><Badge tone={offline ? 'warning' : final ? 'neutral' : 'success'}>{offline ? 'Offline' : final ? 'Terminata' : 'LIVE'}</Badge><ThemeToggle /></div></header>
      <Card elevated className="mx-auto mt-12 max-w-4xl p-6 sm:mt-16 sm:p-14"><p className="text-center text-sm text-[var(--cg-color-label-secondary)]">{offline ? 'Ultimo punteggio sincronizzato' : 'Calciobalilla · Arena principale'}</p><div className="mt-10 grid grid-cols-[1fr_auto_1fr] items-center gap-3 text-center sm:gap-12"><div><p className="text-lg font-semibold sm:text-3xl">Rossi</p><strong className="mt-3 block text-7xl tracking-tight sm:text-9xl">{final ? '10' : '7'}</strong></div><span className="text-3xl text-[var(--cg-color-label-tertiary)]">—</span><div><p className="text-lg font-semibold sm:text-3xl">Blu</p><strong className="mt-3 block text-7xl tracking-tight sm:text-9xl">{final ? '7' : '5'}</strong></div></div><div className="mt-8 flex justify-center"><Badge tone={final ? 'neutral' : 'blue'}>{final ? 'Partita conclusa' : 'Primo a 10 punti'}</Badge></div></Card>
      <Link to="/" className="mx-auto mt-8 block w-fit text-sm text-[var(--cg-color-accent-label)]">← Torna alla schermata iniziale</Link>
    </main>
  )
}
