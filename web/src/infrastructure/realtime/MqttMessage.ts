export type MqttQos = 0 | 1 | 2

export type MqttMessageDirection = 'inbound' | 'outbound'

export type MqttPayload =
  | string
  | number
  | boolean
  | null
  | Readonly<Record<string, unknown>>
  | readonly unknown[]

export interface MqttMessage {
  readonly id: string
  readonly topic: string
  readonly payload: MqttPayload
  readonly qos: MqttQos
  readonly retain: boolean
  readonly timestamp: Date
  readonly direction: MqttMessageDirection
}

export interface MqttConnectionOptions {
  readonly url: string
  readonly clientId: string
  readonly username?: string
  readonly password?: string
  readonly keepAliveSeconds?: number
  readonly reconnectPeriodMs?: number
  readonly clean?: boolean
}

export interface MqttPublishOptions {
  readonly qos?: MqttQos
  readonly retain?: boolean
}

export interface MqttSubscription {
  readonly unsubscribe: () => Promise<void>
}
