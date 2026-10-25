import type { InputHTMLAttributes } from 'react'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

export function Input({ label, error, id, ...props }: InputProps) {
  const inputId = id ?? (label ? `cg-input-${label.toLowerCase().replaceAll(' ', '-')}` : undefined)

  return (
    <label className="cg-field" htmlFor={inputId}>
      {label && <span className="cg-field-label">{label}</span>}
      <input
        id={inputId}
        className={`cg-input${error ? ' cg-input-error' : ''}`}
        aria-invalid={error ? true : undefined}
        {...props}
      />
      {error && <span className="cg-field-error" role="alert">{error}</span>}
    </label>
  )
}
