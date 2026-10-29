export type HealthStatus = 'healthy' | 'degraded' | 'unhealthy'

export interface LocalMetrics {
  memoryUsagePercent: number
  throughputPerSecond: number
  activeAlerts: number
}

export interface SystemOverview {
  status: HealthStatus
  metrics: LocalMetrics
  observedAt: string
}
