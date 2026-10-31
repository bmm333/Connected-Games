export type ActuatorType = 'switch' | 'slider' | 'trigger'

export type ActuatorValue = boolean | number | string

export interface Actuator {
  readonly id: string
  readonly name: string
  readonly type: ActuatorType
  readonly value: ActuatorValue
  readonly lastUpdated: Date
  readonly enabled: boolean
  readonly min?: number
  readonly max?: number
  readonly step?: number
}
