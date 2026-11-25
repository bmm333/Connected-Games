import mqtt, {
  type IClientOptions,
  type IClientPublishOptions,
  type MqttClient,
} from 'mqtt'
import type { MqttConnectionStatus } from '@domain/simulator'
import type {
  MqttConnectionOptions,
  MqttMessage,
  MqttPayload,
  MqttPublishOptions,
  MqttSubscription,
} from './MqttMessage'

export interface MqttClientService {
  connect(options: MqttConnectionOptions): Promise<void>

  disconnect(): Promise<void>
  subscribeConnectionState(handler: (status: MqttConnectionStatus) => void): () => void

  publish(
    topic: string,
    payload: MqttPayload,
    options?: MqttPublishOptions,
  ): Promise<void>

  subscribe(
    topic: string,
    handler: (message: MqttMessage) => void,
  ): Promise<MqttSubscription>

  isConnected(): boolean
}

type MessageHandler = (message: MqttMessage) => void
const MAX_PENDING_PUBLISHES = 1_000
const CONNECTION_TIMEOUT_MS = 10_000

function validateTopic(topic: string): void {
  if (!topic || topic.length > 256) {
    throw new Error('Invalid MQTT topic')
  }
}

function encodePayload(payload: MqttPayload): string {
  if (typeof payload === 'string') return payload
  const encoded = JSON.stringify(payload)
  if (encoded === undefined) throw new Error('MQTT payload must be serializable')
  return encoded
}

export function createMqttClientService(): MqttClientService {
  let client: MqttClient | null = null
  let connectionAttempt: Promise<void> | null = null
  let connectionStatus: MqttConnectionStatus = 'offline'
  const handlers = new Map<string, Set<MessageHandler>>()
  const connectionStateHandlers = new Set<(status: MqttConnectionStatus) => void>()
  const emitConnectionState = (status: MqttConnectionStatus) => {
    connectionStatus = status
    connectionStateHandlers.forEach((handler) => handler(status))
  }
  const pendingPublishes: Array<{
    topic: string
    payload: string
    options: IClientPublishOptions
  }> = []

  const flushPending = () => {
    if (!client?.connected) return
    while (pendingPublishes.length > 0) {
      const pending = pendingPublishes.shift()
      if (pending) client.publish(pending.topic, pending.payload, pending.options)
    }
  }

  return {
    async connect(options: MqttConnectionOptions) {
      if (!options.url) {
        throw new Error('VITE_MQTT_URL is required for MQTT connections')
      }
      if (client?.connected) return
      if (connectionAttempt) return connectionAttempt
      if (client?.reconnecting) return
      if (client) {
        await new Promise<void>((resolve, reject) => {
          client?.end(true, {}, (error) => error ? reject(error) : resolve())
        })
        client = null
      }
      emitConnectionState('connecting')
      const clientOptions: IClientOptions = {
        clientId: options.clientId,
        username: options.username,
        password: options.password,
        keepalive: options.keepAliveSeconds ?? 30,
        reconnectPeriod: options.reconnectPeriodMs ?? 2_000,
        connectTimeout: CONNECTION_TIMEOUT_MS,
        clean: options.clean ?? true,
      }
      const mqttClient = mqtt.connect(options.url, clientOptions)
      client = mqttClient
      mqttClient.on('connect', () => {
        if (client !== mqttClient) return
        emitConnectionState('connected')
        flushPending()
      })
      mqttClient.on('reconnect', () => {
        if (client === mqttClient && connectionStatus !== 'error') emitConnectionState('reconnecting')
      })
      mqttClient.on('offline', () => {
        if (client === mqttClient && connectionStatus !== 'error') emitConnectionState('offline')
      })
      mqttClient.on('close', () => {
        if (client === mqttClient && connectionStatus !== 'error') emitConnectionState('offline')
      })
      mqttClient.on('error', () => {
        if (client === mqttClient) emitConnectionState('error')
      })
      mqttClient.on('message', (topic, rawPayload, packet) => {
        const message: MqttMessage = {
          id: crypto.randomUUID(),
          topic,
          payload: rawPayload.toString(),
          qos: packet.qos,
          retain: packet.retain,
          timestamp: new Date(),
          direction: 'inbound',
        }
        handlers.get(topic)?.forEach((handler) => handler(message))
      })
      connectionAttempt = new Promise<void>((resolve, reject) => {
        const timeout = setTimeout(() => {
          cleanup()
          connectionAttempt = null
          emitConnectionState('error')
          mqttClient.end(true)
          reject(new Error('MQTT connection timed out'))
        }, CONNECTION_TIMEOUT_MS)
        const onConnect = () => {
          cleanup()
          connectionAttempt = null
          resolve()
        }
        const onError = (error: Error) => {
          cleanup()
          connectionAttempt = null
          reject(error)
        }
        const cleanup = () => {
          clearTimeout(timeout)
          mqttClient.off('connect', onConnect)
          mqttClient.off('error', onError)
        }
        mqttClient.once('connect', onConnect)
        mqttClient.once('error', onError)
      })
      return connectionAttempt
    },
    async disconnect() {
      if (!client) return
      await new Promise<void>((resolve, reject) => {
        client?.end(false, {}, (error) => (error ? reject(error) : resolve()))
      })
      client = null
      emitConnectionState('offline')
    },
    async publish(topic, payload, options) {
      validateTopic(topic)
      const encoded = encodePayload(payload)
      const publishOptions: IClientPublishOptions = {
        qos: options?.qos ?? 0,
        retain: options?.retain ?? false,
      }
      if (!client?.connected) {
        if (pendingPublishes.length >= MAX_PENDING_PUBLISHES) {
          throw new Error('MQTT offline publish buffer is full')
        }
        pendingPublishes.push({ topic, payload: encoded, options: publishOptions })
        return
      }
      await new Promise<void>((resolve, reject) => {
        client?.publish(topic, encoded, publishOptions, (error) => {
          if (error) reject(error)
          else resolve()
        })
      })
    },
    async subscribe(topic, handler): Promise<MqttSubscription> {
      validateTopic(topic)
      if (!client?.connected) throw new Error('MQTT client is not connected')
      const topicHandlers = handlers.get(topic) ?? new Set<MessageHandler>()
      topicHandlers.add(handler)
      handlers.set(topic, topicHandlers)
      await new Promise<void>((resolve, reject) => {
        client?.subscribe(topic, { qos: 1 }, (error) => {
          if (error) reject(error)
          else resolve()
        })
      })
      return {
        unsubscribe: async () => {
          const currentHandlers = handlers.get(topic)
          currentHandlers?.delete(handler)
          if (currentHandlers?.size === 0) {
            handlers.delete(topic)
            await new Promise<void>((resolve, reject) => {
              client?.unsubscribe(topic, (error) => (error ? reject(error) : resolve()))
            })
          }
        },
      }
    },
    isConnected: () => client?.connected ?? false,
    subscribeConnectionState(handler) {
      connectionStateHandlers.add(handler)
      return () => connectionStateHandlers.delete(handler)
    },
  }
}
