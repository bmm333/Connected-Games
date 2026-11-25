import { describe, expect, it } from 'vitest'
import { apiUrl, communicationRoutes, readCommunicationConfig } from './config'

describe('communication config', () => {
  it('defaults to mock mode and a relative API base', () => {
    expect(readCommunicationConfig({})).toEqual({
      useMocks: true,
      apiBaseUrl: '',
      sseUrl: undefined,
      mqttUrl: undefined,
    })
  })

  it('reads real service URLs and centralizes contract routes', () => {
    const config = readCommunicationConfig({
      VITE_USE_MOCKS: 'false',
      VITE_API_BASE_URL: 'https://api.example.test/',
      VITE_SSE_URL: 'https://stream.example.test/',
      VITE_MQTT_URL: 'wss://broker.example.test/mqtt',
    })
    expect(config).toEqual({
      useMocks: false,
      apiBaseUrl: 'https://api.example.test',
      sseUrl: 'https://stream.example.test',
      mqttUrl: 'wss://broker.example.test/mqtt',
    })
    expect(apiUrl(config.apiBaseUrl, communicationRoutes.match)).toBe('https://api.example.test/api/match')
    expect(communicationRoutes.mqttMatchEvents('v 1', 't/2', 'm 3'))
      .toBe('venues/v%201/tables/t%2F2/matches/m%203/events')
    expect(communicationRoutes.mqttMatchCompleted).toBe('platform/matches/completed')
  })

  it('rejects invalid mock flags instead of silently selecting a mode', () => {
    expect(() => readCommunicationConfig({ VITE_USE_MOCKS: 'yes' }))
      .toThrow('VITE_USE_MOCKS must be "true" or "false"')
  })
})
