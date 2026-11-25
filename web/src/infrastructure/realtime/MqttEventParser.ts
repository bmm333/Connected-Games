import type { Match, MatchCompleted, MatchStarted } from '@domain/simulator'

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

function parseSides(
  value: unknown,
  field: string,
  requireScore: boolean,
): readonly Record<string, unknown>[] {
  if (!Array.isArray(value) || value.length < 2 ||
    !value.every((side) => isRecord(side) &&
      (side.side === 'RED' || side.side === 'BLUE') &&
      (!requireScore || (Number.isInteger(side.score) && (side.score as number) >= 0)) &&
      (side.participantId === undefined || isString(side.participantId)) &&
      (side.displayName === undefined || isString(side.displayName)))) {
    throw new Error(`Invalid match payload: invalid ${field}`)
  }
  return value as readonly Record<string, unknown>[]
}

export function parseMatch(payload: unknown): Match {
  const value = parseJsonObject(payload)
  for (const field of ['id', 'tableId', 'venueId', 'gameTypeId'] as const) {
    if (!isUuid(value[field])) throw new Error(`Invalid Match payload: ${field} must be a UUID`)
  }
  if (value.tournamentMatchId !== undefined && !isUuid(value.tournamentMatchId)) {
    throw new Error('Invalid Match payload: tournamentMatchId must be a UUID')
  }
  if (!['WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].includes(String(value.status))) {
    throw new Error('Invalid Match payload: invalid status')
  }
  if (value.mode !== 'INDIVIDUAL' && value.mode !== 'TEAM') {
    throw new Error('Invalid Match payload: invalid mode')
  }
  if (!Number.isInteger(value.pointsToWin) || (value.pointsToWin as number) < 1) {
    throw new Error('Invalid Match payload: invalid pointsToWin')
  }
  if (!isDateTime(value.startedAt) ||
    (value.endedAt !== undefined && !isDateTime(value.endedAt))) {
    throw new Error('Invalid Match payload: invalid timestamps')
  }
  if (value.winnerSide !== undefined && value.winnerSide !== 'RED' && value.winnerSide !== 'BLUE') {
    throw new Error('Invalid Match payload: invalid winnerSide')
  }
  const sides = parseSides(value.sides, 'sides', true)
  return { ...value, sides } as unknown as Match
}

export function parseMatchStarted(payload: unknown): MatchStarted {
  const envelope = parseJsonObject(payload)
  if (envelope.type !== 'MatchStarted') {
    throw new Error('Invalid MatchStarted payload: expected MatchStarted event')
  }
  for (const field of ['eventId', 'venueId', 'tableId', 'matchId'] as const) {
    if (!isUuid(envelope[field])) {
      throw new Error(`Invalid MatchStarted payload: ${field} must be a UUID`)
    }
  }
  if (!isString(envelope.tenantId) ||
    !Number.isInteger(envelope.seq) || (envelope.seq as number) < 1 ||
    !isDateTime(envelope.occurredAt) ||
    !['CAMERA', 'IR', 'BUTTON', 'EMULATED'].includes(String(envelope.source))) {
    throw new Error('Invalid MatchStarted payload: invalid event envelope')
  }
  const value = parseJsonObject(envelope.payload)
  if (!isUuid(value.gameTypeId)) {
    throw new Error('Invalid MatchStarted payload: gameTypeId must be a UUID')
  }
  if (value.tournamentMatchId !== undefined && !isUuid(value.tournamentMatchId)) {
    throw new Error('Invalid MatchStarted payload: tournamentMatchId must be a UUID')
  }
  if (value.mode !== 'INDIVIDUAL' && value.mode !== 'TEAM') {
    throw new Error('Invalid MatchStarted payload: invalid mode')
  }
  if (!Number.isInteger(value.pointsToWin) || (value.pointsToWin as number) < 1) {
    throw new Error('Invalid MatchStarted payload: invalid pointsToWin')
  }
  const sides = parseSides(value.sides, 'sides', false)
  return { ...value, sides } as unknown as MatchStarted
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
  const sides = parseSides(value.sides, 'sides', true)
  if (!Array.isArray(value.points) ||
    !value.points.every((point) => isRecord(point) &&
      Number.isInteger(point.seq) && (point.seq as number) >= 1 &&
      (point.side === 'RED' || point.side === 'BLUE') &&
      isDateTime(point.occurredAt))) {
    throw new Error('Invalid MatchCompleted payload: invalid points')
  }

  return { ...value, sides } as unknown as MatchCompleted
}
