'use client'

import { useEffect, useState } from 'react'
import type { Alert } from '../lib/api'

function severityClass(severity: string): string {
  switch (severity) {
    case 'CRITICAL': return 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-400'
    case 'WARNING':  return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-950 dark:text-yellow-400'
    default:         return 'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-400'
  }
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

export default function AlertStream() {
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const es = new EventSource('/api/alerts/stream')

    es.addEventListener('alert', (e) => {
      try {
        const alert: Alert = JSON.parse(e.data)
        setAlerts(prev => [alert, ...prev].slice(0, 50))
      } catch { /* ignore malformed events */ }
    })

    es.onopen = () => setConnected(true)
    es.onerror = () => setConnected(false) // EventSource auto-reconnects

    return () => es.close()
  }, [])

  return (
    <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
      <div className="flex items-center justify-between border-b border-zinc-200 px-5 py-4 dark:border-zinc-800">
        <h2 className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">Live alerts</h2>
        <span className={`inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-xs font-medium ${
          connected
            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-400'
            : 'bg-zinc-100 text-zinc-500 dark:bg-zinc-800'
        }`}>
          <span className={`h-1.5 w-1.5 rounded-full ${connected ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
          {connected ? 'Connected' : 'Reconnecting…'}
        </span>
      </div>

      {alerts.length === 0 ? (
        <p className="px-5 py-8 text-center text-sm text-zinc-400">
          No alerts yet — new violations will appear here in real time.
        </p>
      ) : (
        <ul className="divide-y divide-zinc-100 dark:divide-zinc-800">
          {alerts.map(alert => (
            <li key={alert.id} className="flex items-start gap-3 px-5 py-3">
              <span className={`mt-0.5 shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${severityClass(alert.severity)}`}>
                {alert.severity}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs font-mono text-zinc-700 dark:text-zinc-300">
                  {alert.endpointId}
                </p>
                <p className="text-xs text-zinc-500">
                  {alert.metadata?.ruleType ?? 'UNKNOWN'} · {alert.metadata?.detail}
                </p>
              </div>
              <time className="shrink-0 text-xs text-zinc-400">
                {formatTime(alert.triggeredAt)}
              </time>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
