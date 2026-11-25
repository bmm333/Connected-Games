import { describe, expect, it, vi } from 'vitest'
import { createRetryingFetch } from './retryingFetch'

describe('createRetryingFetch', () => {
  it('retries a transient server response once', async () => {
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(new Response('', { status: 503 }))
      .mockResolvedValueOnce(new Response('{}', { status: 200 }))
    const request = createRetryingFetch({
      fetcher,
      wait: async () => undefined,
    })

    const response = await request('/api/test')

    expect(response.status).toBe(200)
    expect(fetcher).toHaveBeenCalledTimes(2)
  })

  it('aborts a request at its timeout and surfaces the error', async () => {
    const fetcher = vi.fn<typeof fetch>((_input, init) => new Promise((_resolve, reject) => {
      init?.signal?.addEventListener('abort', () => reject(new Error('aborted')), { once: true })
    }))
    const request = createRetryingFetch({ fetcher, retries: 0, timeoutMs: 5 })

    await expect(request('/api/test')).rejects.toThrow('aborted')
  })
})
