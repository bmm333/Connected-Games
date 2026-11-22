import type { MatchCompleted } from '@domain/simulator'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isString(value: unknown): value is string {
  return typeof value === 'string' && value.length > 0
}

function isUuid(value: unknown): value is string {
  return isString(value) &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value)
}

function isDateTime(value: unknown): value is string {
  return isString(value) && !Number.isNaN(Date.parse(value))
}

function parseStringField(value: unknown, field: string): string {
  if (!isString(value)) throw new Error(`Invalid MQTT payload: ${field} must be a string`)
  return value
}

export function parseJsonObject(payload: unknown): Record<string, unknown> {
  if (typeof payload !== 'string') {
    if (isRecord(payload)) return payload
    throw new Error('Invalid MQTT payload: expected JSON')
  }
  try {
    const parsed: unknown = JSON.parse(payload)
    if (!isRecord(parsed)) throw new Error('Invalid MQTT payload: expected a JSON object')
    return parsed
  } catch (error) {
    throw new Error(
      error instanceof Error ? `Invalid MQTT payload: ${error.message}` : 'Invalid MQTT payload',
      { cause: error },
    )
  }
}

export function parseEventsAcknowledged(payload: unknown): {
  venueId: string
  acknowledged: string[]
  rejected?: readonly { eventId: string; reason: string }[]
  processedAt: string
} {
  const value = parseJsonObject(payload)
  const venueId = parseStringField(value.venueId, 'venueId')
  if (!Array.isArray(value.acknowledged) ||
    !value.acknowledged.every(isUuid)) {
    throw new Error('Invalid MQTT payload: acknowledged must contain UUID strings')
  }
  if (!isDateTime(value.processedAt)) {
    throw new Error('Invalid MQTT payload: processedAt must be a date-time')
  }

  const rejected = value.rejected === undefined
    ? undefined
    : value.rejected
  if (rejected !== undefined) {
    if (!Array.isArray(rejected) ||
      !rejected.every((item) => isRecord(item) &&
        isUuid(item.eventId) &&
        isString(item.reason))) {
      throw new Error('Invalid MQTT payload: rejected must contain eventId and reason')
    }
  }

  return {
    venueId,
    acknowledged: value.acknowledged,
    rejected: rejected as readonly { eventId: string; reason: string }[] | undefined,
    processedAt: value.processedAt,
  }
}

export function parseMatchCompleted(payload: unknown): MatchCompleted {
  const value = parseJsonObject(payload)
  const requiredUuidFields = ['eventId', 'venueId', 'tableId', 'matchId', 'gameTypeId'] as const
  for (const field of requiredUuidFields) {
    if (!isUuid(value[field])) throw new Error(`Invalid MatchCompleted payload: ${field} must be a UUID`)
  }
  if (!isString(value.tenantId)) throw new Error('Invalid MatchCompleted payload: tenantId must be a string')
  if (value.tournamentMatchId !== undefined && !isUuid(value.tournamentMatchId)) {
    throw new Error('Invalid MatchCompleted payload: tournamentMatchId must be a UUID')
  }
  if (value.mode !== 'INDIVIDUAL' && value.mode !== 'TEAM') {
    throw new Error('Invalid MatchCompleted payload: invalid mode')
  }
  if (!Number.isInteger(value.pointsToWin) || (value.pointsToWin as number) < 1) {
    throw new Error('Invalid MatchCompleted payload: invalid pointsToWin')
  }
  if (!isDateTime(value.startedAt) || !isDateTime(value.endedAt)) {
    throw new Error('Invalid MatchCompleted payload: invalid timestamps')
  }
  if (value.reason !== 'SCORE_REACHED' && value.reason !== 'TIME_EXPIRED' && value.reason !== 'CANCELLED') {
    throw new Error('Invalid MatchCompleted payload: invalid reason')
  }
  if (value.winnerSide !== undefined && value.winnerSide !== 'RED' && value.winnerSide !== 'BLUE') {
    throw new Error('Invalid MatchCompleted payload: invalid winnerSide')
  }
  if (!Array.isArray(value.sides) || value.sides.length < 2 ||
    !value.sides.every((side) => isRecord(side) &&
      (side.side === 'RED' || side.side === 'BLUE') &&
      Number.isInteger(side.score) && (side.score as number) >= 0 &&
      (side.participantId === undefined || isString(side.participantId)) &&
      (side.displayName === undefined || isString(side.displayName)))) {
    throw new Error('Invalid MatchCompleted payload: invalid sides')
  }

  return value as unknown as MatchCompleted
}
