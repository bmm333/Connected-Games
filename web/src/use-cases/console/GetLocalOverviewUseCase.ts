import type { OverviewRepository } from '@domain/console/OverviewRepository'
import type { SystemOverview } from '@domain/console/SystemOverview'

export type GetLocalOverviewUseCase = () => Promise<SystemOverview>

export function createGetLocalOverviewUseCase(
  repository: OverviewRepository,
): GetLocalOverviewUseCase {
  return () => repository.getLocalOverview()
}
