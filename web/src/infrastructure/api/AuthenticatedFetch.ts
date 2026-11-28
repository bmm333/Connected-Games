export interface AuthenticatedToken {
  readonly accessToken: string
  readonly refreshToken?: string
}

export interface AuthenticatedSession {
  readonly token: AuthenticatedToken
}

export interface AuthenticatedFetchOptions<TSession extends AuthenticatedSession> {
  readonly getSession: () => Promise<TSession | undefined>
  readonly refreshToken?: (refreshToken: string) => Promise<AuthenticatedToken>
  readonly saveSession?: (session: TSession) => Promise<void>
  readonly onUnauthorized: () => void
  readonly fetcher?: typeof fetch
}

export function createAuthenticatedFetch<TSession extends AuthenticatedSession>(
  options: AuthenticatedFetchOptions<TSession>,
): typeof fetch {
  const fetcher = options.fetcher ?? fetch
  return async (input, init) => {
    const session = await options.getSession()
    const headers = new Headers(init?.headers)
    if (session?.token.accessToken !== undefined) {
      headers.set('Authorization', `Bearer ${session.token.accessToken}`)
    }

    let response = await fetcher(input, { ...init, headers })
    if (
      response.status !== 401 ||
      session?.token.refreshToken === undefined ||
      options.refreshToken === undefined ||
      options.saveSession === undefined
    ) {
      if (response.status === 401 || response.status === 403) options.onUnauthorized()
      return response
    }

    let token: AuthenticatedToken
    try {
      token = await options.refreshToken(session.token.refreshToken)
    } catch (error) {
      options.onUnauthorized()
      throw error
    }
    const refreshed = {
      ...session,
      token: { ...session.token, ...token },
    } as TSession
    await options.saveSession(refreshed)
    const retryHeaders = new Headers(headers)
    retryHeaders.set('Authorization', `Bearer ${token.accessToken}`)
    response = await fetcher(input, { ...init, headers: retryHeaders })
    if (response.status === 401 || response.status === 403) options.onUnauthorized()
    return response
  }
}
