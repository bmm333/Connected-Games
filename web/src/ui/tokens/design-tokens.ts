export const designTokens = {
  color: {
    accent: 'var(--cg-color-accent)',
    accentLabel: 'var(--cg-color-accent-label)',
    background: {
      primary: 'var(--cg-color-bg-primary)',
      grouped: 'var(--cg-color-bg-grouped)',
      elevated: 'var(--cg-color-bg-elevated)',
      secondaryElevated: 'var(--cg-color-bg-secondary-elevated)',
    },
    label: {
      primary: 'var(--cg-color-label-primary)',
      secondary: 'var(--cg-color-label-secondary)',
      tertiary: 'var(--cg-color-label-tertiary)',
      onAccent: 'var(--cg-color-label-on-accent)',
    },
    border: {
      default: 'var(--cg-color-border-default)',
      subtle: 'var(--cg-color-border-subtle)',
    },
    team: {
      red: 'var(--cg-color-team-red)',
      blue: 'var(--cg-color-team-blue)',
    },
    status: {
      success: 'var(--cg-color-status-success)',
      warning: 'var(--cg-color-status-warning)',
      destructive: 'var(--cg-color-status-destructive)',
    },
  },
  radius: {
    sm: 'var(--cg-radius-sm)',
    md: 'var(--cg-radius-md)',
    lg: 'var(--cg-radius-lg)',
  },
  space: {
    1: 'var(--cg-space-1)',
    2: 'var(--cg-space-2)',
    3: 'var(--cg-space-3)',
    4: 'var(--cg-space-4)',
    5: 'var(--cg-space-5)',
    6: 'var(--cg-space-6)',
  },
} as const
