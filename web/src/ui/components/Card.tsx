import type { HTMLAttributes } from 'react'

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  elevated?: boolean
}

export function Card({
  elevated = false,
  className = '',
  ...props
}: CardProps) {
  return (
    <div
      className={`cg-card${elevated ? ' cg-card-elevated' : ''} ${className}`.trim()}
      {...props}
    />
  )
}
