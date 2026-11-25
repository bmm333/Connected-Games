import type { MatchEventPublisher } from '@domain/simulator'
import type { MqttClientService } from './MqttClientService'
import type { MqttConnectionOptions } from './MqttMessage'
import { communicationRoutes } from '@shared/communication/config'
import { parseEventsAcknowledged, parseMatchCompleted } from './MqttEventParser'

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
        communicationRoutes.mqttHubAck(venueId),
        (message) => {
          try {
            onAck(parseEventsAcknowledged(message.payload))
          } catch (error) {
            console.warn('Discarding malformed MQTT acknowledgement', error)
          }
        },
      )
      return subscription.unsubscribe
    },
    async subscribeMatchCompleted(onMatch) {
      const subscription = await service.subscribe(
        communicationRoutes.mqttMatchCompleted,
        (message) => {
          try {
            onMatch(parseMatchCompleted(message.payload))
          } catch (error) {
            console.warn('Discarding malformed MatchCompleted MQTT payload', error)
          }
        },
      )
      return subscription.unsubscribe
    },
  }
}
