const INGESTOR_URL = process.env.INGESTOR_URL ?? 'http://localhost:8081'
const ALERT_URL    = process.env.ALERT_URL    ?? 'http://localhost:8083'
const PROCESSOR_URL = process.env.PROCESSOR_URL ?? 'http://localhost:8082'

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

export interface RuleCompliance {
  ruleId: string
  ruleType: string
  slaTarget: number | null
  thresholdValue: number
  thresholdUnit: string
  measuredValue: number
  measuredUnit: string
  compliant: boolean
  incidentCount: number
  downtimeMinutes: number
}

export interface EndpointSlaReport {
  endpointId: string
  url: string
  totalChecks: number
  successfulChecks: number
  availabilityPct: number
  rules: RuleCompliance[]
}

export interface SlaReport {
  month: string
  generatedAt: string
  endpoints: EndpointSlaReport[]
}

export interface PollHealth {
  endpointId: string
  checkedAt: string
  success: boolean
  statusCode: number | null
  latencyMs: number | null
}

export interface DeadLetter {
  id: string
  topic: string
  partitionN: number
  offsetN: number
  failedAt: string
  errorClass: string | null
  errorMsg: string | null
  payload: string | null
}

export async function fetchPollHealth(endpointId: string): Promise<PollHealth | null> {
  try {
    const res = await fetch(`${INGESTOR_URL}/endpoints/${endpointId}/health`, {
      next: { revalidate: 15 },
    })
    if (res.status === 204 || !res.ok) return null
    return res.json()
  } catch {
    return null
  }
}

export async function fetchDeadLetters(limit = 50): Promise<DeadLetter[]> {
  try {
    const res = await fetch(`${PROCESSOR_URL}/dead-letters?limit=${limit}`, {
      next: { revalidate: 30 },
    })
    if (!res.ok) return []
    return res.json()
  } catch {
    return []
  }
}

export async function fetchSlaReport(month?: string): Promise<SlaReport | null> {
  try {
    const url = month
      ? `${ALERT_URL}/reports/sla?month=${month}`
      : `${ALERT_URL}/reports/sla`
    const res = await fetch(url, { next: { revalidate: 60 } })
    if (!res.ok) return null
    return res.json()
  } catch {
    return null
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
