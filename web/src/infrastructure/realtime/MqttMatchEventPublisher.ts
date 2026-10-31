import type {
  MatchEventEnvelope,
  MatchEventPublisher,
  MqttClientService,
} from '@domain/simulator'

export function createMqttMatchEventPublisher(
  service: MqttClientService,
): MatchEventPublisher {
  return {
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

export type { MatchEventEnvelope }
