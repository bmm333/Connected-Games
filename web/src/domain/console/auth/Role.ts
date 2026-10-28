export type Role =
  | 'player'
  | 'venue_manager'
  | 'game_admin'
  | 'platform_admin'

export type Permission =
  | 'overview:view'
  | 'tables:read'
  | 'tables:write'
  | 'devices:read'
  | 'devices:write'
