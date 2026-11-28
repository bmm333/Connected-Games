import { describe, expect, it, vi } from 'vitest'
import { createAuthenticatedFetch } from './AuthenticatedFetch'

describe('AuthenticatedFetch', () => {
  it('attaches a bearer token and refreshes once after an unauthorized response', async () => {
    const session = {
      id: 'session',
      token: { accessToken: 'expired', refreshToken: 'refresh' },
    }
    const getSession = vi.fn().mockResolvedValue(session)
    const refreshToken = vi.fn().mockResolvedValue({
      accessToken: 'renewed',
      refreshToken: 'rotated',
    })
    const saveSession = vi.fn()
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(new Response('', { status: 401 }))
      .mockResolvedValueOnce(new Response('ok', { status: 200 }))
    const authenticatedFetch = createAuthenticatedFetch({
      getSession,
      refreshToken,
      saveSession,
      onUnauthorized: vi.fn(),
      fetcher,
    })

    await authenticatedFetch('/api/stats/leaderboard')

    expect(new Headers(fetcher.mock.calls[0][1]?.headers).get('Authorization'))
      .toBe('Bearer expired')
    expect(new Headers(fetcher.mock.calls[1][1]?.headers).get('Authorization'))
      .toBe('Bearer renewed')
    expect(saveSession).toHaveBeenCalledWith({
      ...session,
      token: { accessToken: 'renewed', refreshToken: 'rotated' },
    })
  })
})
