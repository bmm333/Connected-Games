import { act, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import type {
  Match,
  MatchRestClient,
  MatchSseClient,
  MatchStreamStatus,
} from '@domain/simulator'
import { ThemeProvider } from '@ui'
import { LiveDisplayView } from './LiveDisplayView'

const tableId = '018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f10'
const match: Match = {
  id: tableId,
  tableId,
  venueId: tableId,
  gameTypeId: tableId,
  status: 'IN_PROGRESS',
  mode: 'INDIVIDUAL',
  pointsToWin: 5,
  sides: [
    { side: 'RED', displayName: 'Rossi', score: 2 },
    { side: 'BLUE', displayName: 'Blu', score: 1 },
  ],
  startedAt: '2026-11-25T18:00:00.000Z',
}

function renderLiveDisplay(
  matches: MatchRestClient & MatchSseClient,
  initialEntry = `/?tableId=${tableId}`,
) {
  return render(
    <ThemeProvider>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route path="/" element={<LiveDisplayView matches={matches} />} />
        </Routes>
      </MemoryRouter>
    </ThemeProvider>,
  )
}

describe('LiveDisplayView', () => {
  it('loads the table snapshot and updates the score from the stream', async () => {
    let updateMatch: ((value: Match) => void) | undefined
    const matches: MatchRestClient & MatchSseClient = {
      getCurrentMatch: vi.fn().mockResolvedValue(match),
      subscribe: vi.fn((
        _tableId: string,
        onMatch: (value: Match) => void,
        onStatus: (status: MatchStreamStatus) => void,
      ) => {
        updateMatch = onMatch
        onStatus('connected')
        return vi.fn()
      }),
    }

    renderLiveDisplay(matches)

    expect(await screen.findByText('Rossi')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('LIVE')).toBeInTheDocument()
    act(() => {
      updateMatch?.({ ...match, sides: [{ ...match.sides[0], score: 3 }, match.sides[1]] })
    })
    await waitFor(() => expect(screen.getByText('3')).toBeInTheDocument())
    expect(matches.getCurrentMatch).toHaveBeenCalledWith(tableId)
  })

  it('shows an explicit disconnection and error state without crashing', async () => {
    const matches: MatchRestClient & MatchSseClient = {
      getCurrentMatch: vi.fn().mockRejectedValue(new Error('API unavailable')),
      subscribe: vi.fn((
        _tableId: string,
        _onMatch: (value: Match) => void,
        onStatus: (status: MatchStreamStatus) => void,
      ) => {
        onStatus('disconnected')
        return vi.fn()
      }),
    }

    renderLiveDisplay(matches)

    expect(await screen.findByRole('alert')).toHaveTextContent('API unavailable')
    expect(screen.getByText('Disconnesso')).toBeInTheDocument()
  })

  it('shows an empty state when no table is selected', () => {
    const matches: MatchRestClient & MatchSseClient = {
      getCurrentMatch: vi.fn(),
      subscribe: vi.fn(),
    }

    renderLiveDisplay(matches, '/')

    expect(screen.getByText(/Nessun tavolo|tableId/)).toBeInTheDocument()
    expect(matches.getCurrentMatch).not.toHaveBeenCalled()
  })
})
