import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'

export function AttractView() {
  return (
    <div className="flex min-h-[78vh] flex-col items-center justify-center space-y-8 p-6 text-center sm:p-8">
      <div className="absolute right-6 top-6"><ThemeToggle /></div>
      <div className="space-y-4">
        <span className="cg-badge cg-badge-neutral">
          Arena Gaming Kiosk
        </span>
        <h1 className="max-w-3xl text-5xl font-extrabold tracking-tight text-[var(--cg-color-label-primary)] md:text-7xl">
          Tocca per Giocare
        </h1>
        <p className="text-lg text-[var(--cg-color-label-secondary)] max-w-md mx-auto">
          Inquadra il QR Code con il tuo smartphone o iscriviti direttamente al totem per partecipare al torneo.
        </p>
      </div>

      <div className="flex w-full max-w-md flex-col gap-3 sm:flex-row">
        <Link
          to="/queue"
          className="cg-button cg-button-primary inline-flex items-center"
        >
          Unisciti alla Coda
        </Link>
        <Link
          to="/leaderboard"
          className="cg-button cg-button-secondary inline-flex items-center"
        >
          Classifica Live
        </Link>
      </div>
      <Card className="max-w-md text-sm text-[var(--cg-color-label-secondary)]">
        <p className="font-semibold text-[var(--cg-color-label-primary)]">Pronto per la prossima sfida?</p>
        <p className="mt-1">Usa il QR Code o unisciti alla coda direttamente dal totem.</p>
      </Card>
    </div>
  )
}
