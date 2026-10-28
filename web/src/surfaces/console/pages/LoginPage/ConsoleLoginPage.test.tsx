import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import type { ConsoleDependencies } from '../../composition-root'
import { ConsoleDependenciesContext } from '../../dependencies'
import { createTestQueryClient, userEvent } from '../../../../test/test-utils'
import { ConsoleAuthProvider } from '../../auth/ConsoleAuthProvider'
import { ConsoleLoginPage } from './ConsoleLoginPage'

function renderLogin(login = vi.fn().mockResolvedValue(undefined)) {
  const authenticatedSession = {
    user: {
      id: 'operator-1',
      name: 'Operator',
      email: 'operator@example.com',
    },
    role: 'operator' as const,
    permissions: ['overview:view' as const],
    token: { accessToken: 'token' },
    expiresAt: '2027-01-01T00:00:00.000Z',
  }
  const dependencies = {
    consoleAuth: {
      sessionStore: { get: vi.fn().mockResolvedValue(undefined) },
      login: login.mockResolvedValue(authenticatedSession),
      logout: vi.fn(),
      getPermissions: vi.fn(),
      validateRouteAccess: vi.fn(),
      rolePermissionService: { hasPermission: vi.fn() },
    },
  } as unknown as ConsoleDependencies
  const queryClient = createTestQueryClient()

  return {
    login,
    ...render(
      <QueryClientProvider client={queryClient}>
        <ConsoleDependenciesContext.Provider value={dependencies}>
          <ConsoleAuthProvider>
            <MemoryRouter>
              <ConsoleLoginPage />
            </MemoryRouter>
          </ConsoleAuthProvider>
        </ConsoleDependenciesContext.Provider>
      </QueryClientProvider>,
    ),
  }
}

describe('ConsoleLoginPage', () => {
  it('submits credentials through the injected login use case', async () => {
    const { login } = renderLogin()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Email'), 'operator@example.com')
    await user.type(screen.getByLabelText('Password'), 'secret')
    await user.click(screen.getByRole('button', { name: 'Accedi' }))

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({
        method: 'credentials',
        email: 'operator@example.com',
        password: 'secret',
        remember: false,
      })
    })
  })
})
