import type { MatchEventPublisher } from '@domain/simulator'
import type { MqttClientService } from './MqttClientService'
import type { MqttConnectionOptions } from './MqttMessage'

export function createMqttMatchEventPublisher(
  service: MqttClientService,
  connection: MqttConnectionOptions,
): MatchEventPublisher {
  return {
    connect: () => service.connect(connection),
    async publish(topic, event) {
      await service.publish(topic, JSON.stringify(event), { qos: 1, retain: false })
    },
    async subscribeAck(venueId, onAck) {
      const subscription = await service.subscribe(
        `venues/${encodeURIComponent(venueId)}/hub/ack`,
        (message) => {
          try {
            onAck(JSON.parse(String(message.payload)) as unknown)
          } catch {
            onAck(message.payload)
          }
        },
      )
      return subscription.unsubscribe
    },
  }
}
