const INGESTOR_URL = process.env.INGESTOR_URL ?? 'http://localhost:8081'
const ALERT_URL = process.env.ALERT_URL ?? 'http://localhost:8083'

export interface Endpoint {
  id: string
  serviceId: string
  url: string
  httpMethod: string
  headers: Record<string, string> | null
  timeoutMs: number
  intervalSecs: number
  active: boolean
  createdAt: string
}

export interface Alert {
  id: string
  endpointId: string
  slaRuleId: string
  status: string
  severity: string
  triggeredAt: string
  acknowledgedAt: string | null
  resolvedAt: string | null
  metadata: Record<string, string> | null
}

export async function fetchEndpoints(): Promise<Endpoint[]> {
  try {
    const res = await fetch(`${INGESTOR_URL}/endpoints`, {
      next: { revalidate: 15 },
    })
    if (!res.ok) return []
    return res.json()
  } catch {
    return []
  }
}

export async function fetchAlerts(status?: string): Promise<Alert[]> {
  try {
    const url = status
      ? `${ALERT_URL}/alerts?status=${status}`
      : `${ALERT_URL}/alerts`
    const res = await fetch(url, { next: { revalidate: 15 } })
    if (!res.ok) return []
    return res.json()
  } catch {
    return []
  }
}
