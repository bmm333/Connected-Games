import { describe, expect, it } from 'vitest'
import type { SimulationDataset } from '@domain/simulator'

describe('SimulationDataset', () => {
  it('supports all four simulator dataset shapes', () => {
    const datasets: SimulationDataset[] = [
      { type: 'devices', records: [] },
      { type: 'telemetry', records: [] },
      { type: 'scheduled-events', records: [] },
      { type: 'system-config', records: [] },
    ]

    expect(datasets.map((dataset) => dataset.type)).toEqual([
      'devices',
      'telemetry',
      'scheduled-events',
      'system-config',
    ])
  })
})
