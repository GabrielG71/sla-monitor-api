import { fetchSlaReport, type RuleCompliance } from '../lib/api'

function complianceBadge(compliant: boolean) {
  return compliant
    ? <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 dark:bg-emerald-950 dark:text-emerald-400">Compliant</span>
    : <span className="rounded-full bg-red-50 px-2 py-0.5 text-xs font-medium text-red-700 dark:bg-red-950 dark:text-red-400">Non-compliant</span>
}

function measuredLabel(r: RuleCompliance): string {
  if (r.ruleType === 'LATENCY') return `${r.measuredValue.toFixed(0)} ms (p95)`
  return `${r.measuredValue.toFixed(2)}%`
}

function targetLabel(r: RuleCompliance): string {
  if (r.slaTarget != null) return `≥ ${r.slaTarget}%`
  if (r.ruleType === 'LATENCY') return `≤ ${r.thresholdValue} ms`
  return `≤ ${r.thresholdValue}%`
}

export default async function ReportsPage({
  searchParams,
}: {
  searchParams: Promise<{ month?: string }>
}) {
  const { month } = await searchParams
  const report = await fetchSlaReport(month)
  const displayMonth = month ?? new Date(new Date().setMonth(new Date().getMonth() - 1))
    .toISOString().slice(0, 7)

  return (
    <main className="min-h-screen bg-zinc-50 dark:bg-zinc-950 px-6 py-8">
      <div className="mx-auto max-w-6xl space-y-8">

        <div className="flex items-center justify-between">
          <div>
            <a href="/" className="text-sm text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200">← Dashboard</a>
            <h1 className="mt-2 text-2xl font-semibold text-zinc-900 dark:text-zinc-50">
              SLA Compliance Report
            </h1>
            <p className="mt-1 text-sm text-zinc-500">Period: {report?.month ?? displayMonth}</p>
          </div>
          <a
            href={`http://localhost:8083/reports/sla?month=${report?.month ?? displayMonth}&format=csv`}
            className="rounded-lg border border-zinc-200 bg-white px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-300"
          >
            Export CSV
          </a>
        </div>

        {!report || report.endpoints.length === 0 ? (
          <div className="rounded-lg border border-zinc-200 bg-white px-5 py-12 text-center text-sm text-zinc-400 dark:border-zinc-800 dark:bg-zinc-900">
            No data for this period. Checks are stored after the first polling cycle.
          </div>
        ) : (
          <div className="space-y-6">
            {report.endpoints.map(endpoint => (
              <div key={endpoint.endpointId} className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
                <div className="border-b border-zinc-200 px-5 py-4 dark:border-zinc-800">
                  <p className="font-mono text-sm text-zinc-800 dark:text-zinc-200">{endpoint.url}</p>
                  <div className="mt-1 flex gap-4 text-xs text-zinc-500">
                    <span>{endpoint.totalChecks.toLocaleString()} checks</span>
                    <span>{endpoint.availabilityPct.toFixed(3)}% available</span>
                  </div>
                </div>

                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-zinc-100 text-left text-xs uppercase tracking-wide text-zinc-500 dark:border-zinc-800">
                      <th className="px-5 py-3 font-medium">Rule</th>
                      <th className="px-5 py-3 font-medium">Target</th>
                      <th className="px-5 py-3 font-medium">Measured</th>
                      <th className="px-5 py-3 font-medium">Incidents</th>
                      <th className="px-5 py-3 font-medium">Downtime</th>
                      <th className="px-5 py-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                    {endpoint.rules.map(rule => (
                      <tr key={rule.ruleId} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                        <td className="px-5 py-3">
                          <span className="rounded bg-zinc-100 px-1.5 py-0.5 font-mono text-xs text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400">
                            {rule.ruleType}
                          </span>
                        </td>
                        <td className="px-5 py-3 text-zinc-600 dark:text-zinc-400 text-xs">{targetLabel(rule)}</td>
                        <td className="px-5 py-3 font-medium text-zinc-800 dark:text-zinc-200 text-xs">{measuredLabel(rule)}</td>
                        <td className="px-5 py-3 text-zinc-600 dark:text-zinc-400 text-xs">{rule.incidentCount}</td>
                        <td className="px-5 py-3 text-zinc-600 dark:text-zinc-400 text-xs">
                          {rule.downtimeMinutes > 0 ? `${rule.downtimeMinutes} min` : '—'}
                        </td>
                        <td className="px-5 py-3">{complianceBadge(rule.compliant)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))}
          </div>
        )}

      </div>
    </main>
  )
}
