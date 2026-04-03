import { fetchEndpoints, fetchAlerts, type Alert } from './lib/api'
import AlertStream from './components/AlertStream'

function severityClass(severity: string): string {
  switch (severity) {
    case 'CRITICAL': return 'bg-red-100 text-red-700'
    case 'WARNING':  return 'bg-yellow-100 text-yellow-700'
    default:         return 'bg-blue-100 text-blue-700'
  }
}

function AlertBadge({ alerts }: { alerts: Alert[] }) {
  if (alerts.length === 0) return <span className="text-sm text-zinc-400">—</span>

  const highest = alerts.reduce((prev, curr) => {
    const order = ['INFO', 'WARNING', 'CRITICAL']
    return order.indexOf(curr.severity) > order.indexOf(prev.severity) ? curr : prev
  })

  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${severityClass(highest.severity)}`}>
      {alerts.length} open · {highest.severity}
    </span>
  )
}

export default async function DashboardPage() {
  const [endpoints, openAlerts] = await Promise.all([
    fetchEndpoints(),
    fetchAlerts('OPEN'),
  ])

  const alertsByEndpoint = new Map<string, Alert[]>()
  for (const alert of openAlerts) {
    const list = alertsByEndpoint.get(alert.endpointId) ?? []
    list.push(alert)
    alertsByEndpoint.set(alert.endpointId, list)
  }

  const activeCount = endpoints.filter(e => e.active).length

  return (
    <main className="min-h-screen bg-zinc-50 dark:bg-zinc-950 px-6 py-8">
      <div className="mx-auto max-w-6xl space-y-8">

        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-zinc-900 dark:text-zinc-50">SLA Monitor</h1>
            <p className="mt-1 text-sm text-zinc-500">Endpoint availability and performance dashboard</p>
          </div>
          <a
            href="/reports"
            className="rounded-lg border border-zinc-200 bg-white px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-300"
          >
            SLA Reports
          </a>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-4">
          <div className="rounded-lg border border-zinc-200 bg-white px-5 py-4 dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-sm text-zinc-500">Endpoints</p>
            <p className="mt-1 text-2xl font-semibold text-zinc-900 dark:text-zinc-50">{endpoints.length}</p>
          </div>
          <div className="rounded-lg border border-zinc-200 bg-white px-5 py-4 dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-sm text-zinc-500">Active</p>
            <p className="mt-1 text-2xl font-semibold text-emerald-600">{activeCount}</p>
          </div>
          <div className="rounded-lg border border-zinc-200 bg-white px-5 py-4 dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-sm text-zinc-500">Open alerts</p>
            <p className={`mt-1 text-2xl font-semibold ${openAlerts.length > 0 ? 'text-red-600' : 'text-zinc-900 dark:text-zinc-50'}`}>
              {openAlerts.length}
            </p>
          </div>
        </div>

        {/* Live alert stream */}
        <AlertStream />

        {/* Endpoints table */}
        <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
          <div className="border-b border-zinc-200 px-5 py-4 dark:border-zinc-800">
            <h2 className="text-sm font-semibold text-zinc-700 dark:text-zinc-300">Monitored endpoints</h2>
          </div>

          {endpoints.length === 0 ? (
            <div className="px-5 py-12 text-center text-sm text-zinc-400">
              No endpoints registered. Use <code className="rounded bg-zinc-100 px-1 py-0.5 dark:bg-zinc-800">POST /endpoints</code> on the ingestor service to add one.
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-zinc-100 dark:border-zinc-800 text-left text-xs text-zinc-500 uppercase tracking-wide">
                  <th className="px-5 py-3 font-medium">URL</th>
                  <th className="px-5 py-3 font-medium">Method</th>
                  <th className="px-5 py-3 font-medium">Interval</th>
                  <th className="px-5 py-3 font-medium">Timeout</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 font-medium">Open alerts</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                {endpoints.map(endpoint => (
                  <tr key={endpoint.id} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                    <td className="px-5 py-3 font-mono text-xs text-zinc-800 dark:text-zinc-200 max-w-xs truncate">
                      {endpoint.url}
                    </td>
                    <td className="px-5 py-3">
                      <span className="rounded bg-zinc-100 px-1.5 py-0.5 font-mono text-xs text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400">
                        {endpoint.httpMethod}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-zinc-600 dark:text-zinc-400">{endpoint.intervalSecs}s</td>
                    <td className="px-5 py-3 text-zinc-600 dark:text-zinc-400">{endpoint.timeoutMs}ms</td>
                    <td className="px-5 py-3">
                      {endpoint.active ? (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 dark:bg-emerald-950 dark:text-emerald-400">
                          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-zinc-100 px-2 py-0.5 text-xs font-medium text-zinc-500 dark:bg-zinc-800">
                          <span className="h-1.5 w-1.5 rounded-full bg-zinc-400" />
                          Inactive
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-3">
                      <AlertBadge alerts={alertsByEndpoint.get(endpoint.id) ?? []} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </div>
    </main>
  )
}
