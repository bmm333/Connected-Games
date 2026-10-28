import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'

interface AdminSectionShellProps {
  title: string
  description: string
  children: React.ReactNode
}

export function AdminSectionShell({
  title,
  description,
  children,
}: AdminSectionShellProps) {
  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-5">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.16em] text-[var(--cg-color-accent-label)]">
            Workspace venue
          </p>
          <h1 className="text-3xl font-bold tracking-tight md:text-4xl">{title}</h1>
          <p className="mt-2 max-w-2xl text-sm text-[var(--cg-color-label-secondary)]">
            {description}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/overview" className="cg-button cg-button-secondary">Panoramica</Link>
          <ThemeToggle />
        </div>
      </header>
      {children}
    </div>
  )
}

export function AdminMetric({
  label,
  value,
  detail,
}: {
  label: string
  value: string
  detail: string
}) {
  return (
    <Card elevated>
      <p className="text-sm text-[var(--cg-color-label-secondary)]">{label}</p>
      <p className="mt-3 text-3xl font-bold">{value}</p>
      <p className="mt-2 text-sm text-[var(--cg-color-status-success)]">{detail}</p>
    </Card>
  )
}
