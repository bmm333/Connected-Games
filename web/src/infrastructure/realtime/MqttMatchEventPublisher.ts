import type { MatchEventPublisher } from '@domain/simulator'
import type { MqttClientService } from './MqttClientService'
import type { MqttConnectionOptions } from './MqttMessage'
import { parseEventsAcknowledged } from './MqttEventParser'

export function createMqttMatchEventPublisher(
  service: MqttClientService,
  connection: MqttConnectionOptions,
): MatchEventPublisher {
  return {
    connect: () => service.connect(connection),
    subscribeConnectionState: (onState) => service.subscribeConnectionState(onState),
    async publish(topic, event) {
      await service.publish(topic, JSON.stringify(event), { qos: 1, retain: false })
    },
    async subscribeAck(venueId, onAck) {
      const subscription = await service.subscribe(
        `venues/${encodeURIComponent(venueId)}/hub/ack`,
        (message) => {
          try {
            onAck(parseEventsAcknowledged(message.payload))
          } catch (error) {
            onAck({
              error: error instanceof Error ? error.message : 'Invalid MQTT acknowledgement',
            })
          }
        },
      )
      return subscription.unsubscribe
    },
  }
}
