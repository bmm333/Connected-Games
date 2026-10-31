export function createUuidV7(): string {
  const timestamp = Date.now()
  const bytes = new Uint8Array(16)
  crypto.getRandomValues(bytes)

  const timestampHex = timestamp.toString(16).padStart(12, '0')
  const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0'))
    .join('')
  const randomPart = `${hex.slice(12, 16)}${hex.slice(16)}`
  const value = `${timestampHex}${randomPart}`

  return [
    value.slice(0, 8),
    value.slice(8, 12),
    `7${value.slice(13, 16)}`,
    `${(parseInt(value.slice(16, 18), 16) & 0x3f | 0x80).toString(16).padStart(2, '0')}${value.slice(18, 20)}`,
    value.slice(20, 32),
  ].join('-')
}
