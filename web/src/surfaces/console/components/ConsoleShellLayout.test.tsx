import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { ConsoleAuthContext } from '../auth/ConsoleAuthContext'
import type { ConsoleAuthContextValue } from '../auth/ConsoleAuthContext'
import { ConsoleShellLayout } from './ConsoleShellLayout'
import { ThemeProvider } from '@ui'

describe('ConsoleShellLayout', () => {
  it('shows only navigation entries allowed by the active session', async () => {
    const auth: ConsoleAuthContextValue = {
      session: {
        user: { id: 'operator-1', name: 'Operator', email: 'operator@example.com' },
        role: 'venue_manager',
        permissions: ['overview:view', 'tables:read'],
        token: { accessToken: 'token' },
        expiresAt: '2027-01-01T00:00:00.000Z',
      },
      isLoading: false,
      isAuthenticated: true,
      login: vi.fn(),
      logout: vi.fn(),
      canAccess: (permission) => permission !== 'devices:read',
    }

    render(
      <ConsoleAuthContext.Provider value={auth}>
        <ThemeProvider>
          <MemoryRouter initialEntries={['/overview']}>
            <Routes>
              <Route element={<ConsoleShellLayout />}>
                <Route path="/overview" element={<p>Overview content</p>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </ThemeProvider>
      </ConsoleAuthContext.Provider>,
    )

    await waitFor(() => expect(screen.getAllByText('Local Overview')).not.toHaveLength(0))
    expect(screen.getAllByText('Tables')).not.toHaveLength(0)
    expect(screen.queryByText('Devices')).not.toBeInTheDocument()
  })
})
