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
      <header className="flex flex-wrap items-start justify-between gap-4 border-b border-[var(--cg-color-border-subtle)] pb-6">
        <div>
          <Link
            to="/dashboard"
            className="mb-3 inline-flex text-sm font-medium text-[var(--cg-color-accent-label)]"
          >
            ← Panoramica
          </Link>
          <h1 className="text-3xl font-bold tracking-tight">{title}</h1>
          <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">
            {description}
          </p>
        </div>
        <ThemeToggle />
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
