export type Role = 'admin' | 'operator' | 'manager' | 'auditor' | 'viewer'

export type Permission =
  | 'overview:view'
  | 'tables:read'
  | 'tables:write'
  | 'devices:read'
  | 'devices:write'
