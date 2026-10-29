import type { OverviewRepository } from '@domain/console/OverviewRepository'
import { createGetLocalOverviewUseCase } from './GetLocalOverviewUseCase'

describe('createGetLocalOverviewUseCase', () => {
  it('returns the overview supplied by the repository', async () => {
    const overview = {
      status: 'healthy' as const,
      metrics: {
        memoryUsagePercent: 42,
        throughputPerSecond: 12,
        activeAlerts: 0,
      },
      observedAt: '2026-10-27T20:00:00.000Z',
    }
    const repository: OverviewRepository = {
      getLocalOverview: vi.fn().mockResolvedValue(overview),
    }

    await expect(createGetLocalOverviewUseCase(repository)()).resolves.toEqual(
      overview,
    )
    expect(repository.getLocalOverview).toHaveBeenCalledOnce()
  })
})
