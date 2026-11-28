import { communicationRoutes, apiUrl } from '@shared/communication/config'
import type { MatchSseClient } from '@domain/simulator'
import { parseMatch } from './MqttEventParser'

const RECONNECT_DELAY_MS = 1_000

function wait(milliseconds: number, signal: AbortSignal): Promise<void> {
  return new Promise((resolve) => {
    if (signal.aborted) {
      resolve()
      return
    }
    const timer = setTimeout(() => {
      signal.removeEventListener('abort', onAbort)
      resolve()
    }, milliseconds)
    function onAbort() {
      clearTimeout(timer)
      signal.removeEventListener('abort', onAbort)
      resolve()
    }
    signal.addEventListener('abort', onAbort, { once: true })
  })
}

function dispatchData(data: string[], onMatch: (match: ReturnType<typeof parseMatch>) => void) {
  if (data.length === 0) return
  try {
    onMatch(parseMatch(data.join('\n')))
  } catch (error) {
    console.warn('Discarding malformed match SSE payload', error)
  }
}

async function consumeStream(
  body: ReadableStream<Uint8Array>,
  signal: AbortSignal,
  onMatch: (match: ReturnType<typeof parseMatch>) => void,
): Promise<void> {
  const reader = body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let data: string[] = []

  function processLine(line: string) {
    if (line === '') {
      dispatchData(data, onMatch)
      data = []
    } else if (line.startsWith('data:')) {
      data.push(line.slice(5).replace(/^ /, ''))
    }
  }

  try {
    while (!signal.aborted) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      let newline = buffer.indexOf('\n')
      while (newline >= 0) {
        processLine(buffer.slice(0, newline).replace(/\r$/, ''))
        buffer = buffer.slice(newline + 1)
        newline = buffer.indexOf('\n')
      }
    }
    buffer += decoder.decode()
    if (buffer.length > 0) processLine(buffer.replace(/\r$/, ''))
    dispatchData(data, onMatch)
  } finally {
    reader.releaseLock()
  }
}

export function createMatchSseClient(
  baseUrl: string,
  fetcher: typeof fetch = fetch,
): MatchSseClient {
  return {
    subscribe(tableId, onMatch, onStatus) {
      const url = apiUrl(
        baseUrl,
        `${communicationRoutes.match}${communicationRoutes.matchLive(tableId)}`,
      )
      const controller = new AbortController()
      let disposed = false

      async function connect() {
        let isFirstAttempt = true
        while (!disposed) {
          onStatus(isFirstAttempt ? 'connecting' : 'retrying')
          isFirstAttempt = false
          try {
            const response = await fetcher(url, {
              headers: { Accept: 'text/event-stream' },
              signal: controller.signal,
            })
            if (!response.ok) {
              if (response.status >= 400 && response.status < 500) {
                onStatus('error')
                return
              }
              throw new Error(`Match SSE connection failed (HTTP ${response.status})`)
            }
            if (!response.body) {
              throw new Error('Match SSE response did not contain a stream')
            }

            onStatus('connected')
            await consumeStream(response.body, controller.signal, onMatch)
            if (!disposed) onStatus('retrying')
          } catch (error) {
            if (controller.signal.aborted) return
            console.warn('Match SSE connection failed; reconnecting', error)
            onStatus('retrying')
          }
          await wait(RECONNECT_DELAY_MS, controller.signal)
        }
      }

      void connect()
      return () => {
        disposed = true
        controller.abort()
        onStatus('disconnected')
      }
    },
  }
}
