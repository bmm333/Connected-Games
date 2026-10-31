import type {
  MqttClientService,
  MqttMessage,
  MqttSubscription,
} from '@domain/simulator'
import { assertSubscriptionTopic } from './PublishMqttEventUseCase'

export function createSubscribeMqttTopicsUseCase(service: MqttClientService) {
  return async function subscribeMqttTopics(
    topics: readonly string[],
    onMessage: (message: MqttMessage) => void,
  ): Promise<MqttSubscription> {
    if (topics.length === 0) {
      throw new Error('At least one MQTT topic is required')
    }

    topics.forEach(assertSubscriptionTopic)
    const subscriptions = await Promise.all(
      topics.map((topic) => service.subscribe(topic, onMessage)),
    )

    return {
      unsubscribe: async () => {
        await Promise.all(subscriptions.map((subscription) => subscription.unsubscribe()))
      },
    }
  }
}
