import type { CatalogRepository } from '@domain/catalog/CatalogRepository'
import { createResolveTableByCode } from './ResolveTableByCode'

describe('createResolveTableByCode', () => {
  it('normalizes the code before calling the repository', async () => {
    const repository: CatalogRepository = {
      resolveTableByCode: vi.fn().mockResolvedValue({
        table: {
          id: 'table-1',
          venueId: 'venue-1',
          name: 'Table 1',
          code: 'ABC',
          gameTypeId: 'game-1',
          status: 'FREE',
        },
        venue: {
          id: 'venue-1',
          name: 'Venue',
          city: 'City',
          type: 'PUBLIC',
        },
        gameType: {
          id: 'game-1',
          name: 'Game',
          mode: 'INDIVIDUAL',
          participantsPerSide: 1,
          pointsToWin: 10,
        },
      }),
    }

    await createResolveTableByCode(repository)('  ABC  ')

    expect(repository.resolveTableByCode).toHaveBeenCalledWith('ABC')
  })

  it('rejects an empty code without calling the repository', async () => {
    const repository: CatalogRepository = {
      resolveTableByCode: vi.fn(),
    }

    expect(() => createResolveTableByCode(repository)('  ')).toThrow(
      'Table code cannot be empty',
    )
    expect(repository.resolveTableByCode).not.toHaveBeenCalled()
  })
})
