import mqtt, {
  type IClientOptions,
  type IClientPublishOptions,
  type MqttClient,
} from 'mqtt'
import type {
  MqttClientService as MqttClientServicePort,
  MqttConnectionOptions,
  MqttMessage,
  MqttPayload,
  MqttSubscription,
} from '@domain/simulator'

type MessageHandler = (message: MqttMessage) => void
const MAX_PENDING_PUBLISHES = 1_000

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

export function createMqttClientService(): MqttClientServicePort {
  let client: MqttClient | null = null
  const handlers = new Map<string, Set<MessageHandler>>()
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
      if (client?.connected) return
      const clientOptions: IClientOptions = {
        clientId: options.clientId,
        username: options.username,
        password: options.password,
        keepalive: options.keepAliveSeconds ?? 30,
        reconnectPeriod: options.reconnectPeriodMs ?? 2_000,
        clean: options.clean ?? true,
      }
      client = mqtt.connect(options.url, clientOptions)
      client.on('connect', flushPending)
      client.on('message', (topic, rawPayload, packet) => {
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
      await new Promise<void>((resolve, reject) => {
        const onConnect = () => {
          cleanup()
          resolve()
        }
        const onError = (error: Error) => {
          cleanup()
          reject(error)
        }
        const cleanup = () => {
          client?.off('connect', onConnect)
          client?.off('error', onError)
        }
        client?.once('connect', onConnect)
        client?.once('error', onError)
      })
    },
    async disconnect() {
      if (!client) return
      await new Promise<void>((resolve, reject) => {
        client?.end(false, {}, (error) => (error ? reject(error) : resolve()))
      })
      client = null
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
  }
}
