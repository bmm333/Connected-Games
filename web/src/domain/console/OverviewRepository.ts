import type { SystemOverview } from './SystemOverview'

export interface OverviewRepository {
  getLocalOverview(): Promise<SystemOverview>
}
