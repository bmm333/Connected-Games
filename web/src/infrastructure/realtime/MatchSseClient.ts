import { communicationRoutes, apiUrl } from '@shared/communication/config'
import type { MatchSseClient } from '@domain/simulator'
import { parseMatch } from './MqttEventParser'

export interface EventSourceLike {
  readonly readyState: number
  onopen: ((event: Event) => void) | null
  onmessage: ((event: MessageEvent<string>) => void) | null
  onerror: ((event: Event) => void) | null
  close(): void
}

export type EventSourceFactory = (url: string) => EventSourceLike

export function createMatchSseClient(
  baseUrl: string,
  eventSourceFactory: EventSourceFactory = (url) => new EventSource(url),
): MatchSseClient {
  return {
    subscribe(tableId, onMatch, onStatus) {
      onStatus('connecting')
      let source: EventSourceLike
      try {
        const url = apiUrl(
          baseUrl,
          `${communicationRoutes.match}${communicationRoutes.matchLive(tableId)}`,
        )
        source = eventSourceFactory(url)
      } catch (error) {
        onStatus('error')
        throw error
      }
      source.onopen = () => onStatus('connected')
      source.onmessage = (event) => {
        try {
          onMatch(parseMatch(event.data))
        } catch (error) {
          console.warn('Discarding malformed match SSE payload', error)
        }
      }
      source.onerror = () => {
        onStatus(source.readyState === 2 ? 'disconnected' : 'retrying')
      }
      return () => {
        source.close()
        onStatus('disconnected')
      }
    },
  }
}
