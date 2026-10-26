import type { HTMLAttributes } from 'react'

type BadgeTone = 'neutral' | 'success' | 'warning' | 'destructive' | 'red' | 'blue'

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: BadgeTone
}

export function Badge({
  tone = 'neutral',
  className = '',
  ...props
}: BadgeProps) {
  return (
    <span className={`cg-badge cg-badge-${tone} ${className}`.trim()} {...props} />
  )
}
