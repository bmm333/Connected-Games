import type {
  MqttClientService,
  MqttPayload,
  MqttPublishOptions,
} from '@domain/simulator'

const MAX_TOPIC_LENGTH = 256
const MAX_PAYLOAD_BYTES = 64 * 1024

function assertTopic(topic: string): void {
  if (
    topic.length === 0 ||
    topic.length > MAX_TOPIC_LENGTH ||
    topic.includes('#') ||
    topic.includes('+') ||
    topic.split('/').some((part) => part.length === 0)
  ) {
    throw new Error('Invalid MQTT publish topic')
  }
}

function serializedPayload(payload: MqttPayload): string {
  const serialized = typeof payload === 'string' ? payload : JSON.stringify(payload)
  if (serialized === undefined) {
    throw new Error('MQTT payload must be JSON serializable')
  }
  if (new TextEncoder().encode(serialized).byteLength > MAX_PAYLOAD_BYTES) {
    throw new Error('MQTT payload exceeds the 64 KiB limit')
  }
  return serialized
}

export function createPublishMqttEventUseCase(service: MqttClientService) {
  return async function publishMqttEvent(
    topic: string,
    payload: MqttPayload,
    options?: MqttPublishOptions,
  ): Promise<void> {
    assertTopic(topic)
    const serialized = serializedPayload(payload)
    await service.publish(topic, serialized, options)
  }
}

export { assertTopic }

export function assertSubscriptionTopic(topic: string): void {
  if (
    topic.length === 0 ||
    topic.length > MAX_TOPIC_LENGTH ||
    topic.split('/').some((part, index, parts) => {
      if (part === '#') return index !== parts.length - 1
      return part.includes('#') || part.includes('+') && part.length !== 1
    })
  ) {
    throw new Error('Invalid MQTT subscription topic')
  }
}
