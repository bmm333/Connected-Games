import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import type { ConsoleDependencies } from '../composition-root'
import { ConsoleDependenciesContext } from '../dependencies'
import { createTestQueryClient } from '../../../test/test-utils'
import { ConsoleAuthProvider } from '../auth/ConsoleAuthProvider'
import { ConsoleShellLayout } from './ConsoleShellLayout'
import { ThemeProvider } from '@ui'

describe('ConsoleShellLayout', () => {
  it('shows only navigation entries allowed by the active session', async () => {
    const dependencies = {
      consoleAuth: {
        sessionStore: {
          get: vi.fn().mockResolvedValue({
            user: {
              id: 'operator-1',
              name: 'Operator',
              email: 'operator@example.com',
            },
            role: 'operator',
            permissions: ['overview:view', 'tables:read'],
            token: { accessToken: 'token' },
            expiresAt: '2027-01-01T00:00:00.000Z',
          }),
        },
        login: vi.fn(),
        logout: vi.fn(),
        getPermissions: vi.fn(),
        validateRouteAccess: vi.fn(),
        rolePermissionService: {
          hasPermission: (session: { permissions: string[] }, permission: string) =>
            session.permissions.includes(permission),
        },
      },
    } as unknown as ConsoleDependencies
    const queryClient = createTestQueryClient()

    render(
      <QueryClientProvider client={queryClient}>
        <ConsoleDependenciesContext.Provider value={dependencies}>
          <ThemeProvider>
            <ConsoleAuthProvider>
              <MemoryRouter initialEntries={['/overview']}>
                <Routes>
                  <Route element={<ConsoleShellLayout />}>
                    <Route path="/overview" element={<p>Overview content</p>} />
                  </Route>
                </Routes>
              </MemoryRouter>
            </ConsoleAuthProvider>
          </ThemeProvider>
        </ConsoleDependenciesContext.Provider>
      </QueryClientProvider>,
    )

    await waitFor(() => {
      expect(screen.getByText('Local Overview')).toBeInTheDocument()
    })
    expect(screen.getByText('Tables')).toBeInTheDocument()
    expect(screen.queryByText('Devices')).not.toBeInTheDocument()
  })
})
