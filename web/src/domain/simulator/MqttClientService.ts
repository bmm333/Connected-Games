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
