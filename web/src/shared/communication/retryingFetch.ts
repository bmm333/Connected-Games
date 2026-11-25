export interface RetryingFetchOptions {
  readonly fetcher?: typeof fetch
  readonly timeoutMs?: number
  readonly retries?: number
  readonly wait?: (milliseconds: number) => Promise<void>
}

export function createRetryingFetch(options: RetryingFetchOptions = {}): typeof fetch {
  const fetcher = options.fetcher ?? fetch
  const timeoutMs = options.timeoutMs ?? 5_000
  const retries = options.retries ?? 1
  const wait = options.wait ?? ((milliseconds) =>
    new Promise<void>((resolve) => setTimeout(resolve, milliseconds)))

  return async (input, init) => {
    for (let attempt = 0; ; attempt += 1) {
      const controller = new AbortController()
      const externalSignal = init?.signal
      const abortFromCaller = () => controller.abort(externalSignal?.reason)
      if (externalSignal?.aborted) abortFromCaller()
      else externalSignal?.addEventListener('abort', abortFromCaller, { once: true })
      const timeout = setTimeout(() => controller.abort(new Error('Request timed out')), timeoutMs)

      try {
        const response = await fetcher(input, { ...init, signal: controller.signal })
        if ((response.status >= 500 || response.status === 429) && attempt < retries) {
          await response.body?.cancel()
          await wait(150 * (attempt + 1))
          continue
        }
        return response
      } catch (error) {
        if (attempt >= retries || externalSignal?.aborted) throw error
        await wait(150 * (attempt + 1))
      } finally {
        clearTimeout(timeout)
        externalSignal?.removeEventListener('abort', abortFromCaller)
      }
    }
  }
}
