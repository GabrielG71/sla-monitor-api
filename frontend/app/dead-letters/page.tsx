import { fetchDeadLetters } from '../lib/api'

export default async function DeadLettersPage() {
  const deadLetters = await fetchDeadLetters(100)

  return (
    <main className="min-h-screen bg-zinc-50 dark:bg-zinc-950 px-6 py-8">
      <div className="mx-auto max-w-6xl space-y-8">

        <div>
          <a href="/" className="text-sm text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200">← Dashboard</a>
          <h1 className="mt-2 text-2xl font-semibold text-zinc-900 dark:text-zinc-50">Dead Letters</h1>
          <p className="mt-1 text-sm text-zinc-500">
            Messages from <code className="rounded bg-zinc-100 px-1 py-0.5 text-xs dark:bg-zinc-800">raw-checks.DLT</code> that failed processing after all retries.
          </p>
        </div>

        {deadLetters.length === 0 ? (
          <div className="rounded-lg border border-zinc-200 bg-white px-5 py-12 text-center text-sm text-zinc-400 dark:border-zinc-800 dark:bg-zinc-900">
            No dead letters — all messages are processing successfully.
          </div>
        ) : (
          <div className="space-y-3">
            {deadLetters.map(dl => (
              <div key={dl.id} className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
                <div className="flex items-center justify-between border-b border-zinc-100 px-5 py-3 dark:border-zinc-800">
                  <div className="flex items-center gap-3">
                    <span className="rounded bg-red-50 px-1.5 py-0.5 font-mono text-xs text-red-700 dark:bg-red-950 dark:text-red-400">
                      {dl.topic} p{dl.partitionN} @{dl.offsetN}
                    </span>
                    <span className="text-xs text-zinc-500">
                      {new Date(dl.failedAt).toLocaleString()}
                    </span>
                  </div>
                </div>
                {dl.errorClass && (
                  <div className="px-5 py-2 border-b border-zinc-100 dark:border-zinc-800">
                    <p className="font-mono text-xs text-red-600 dark:text-red-400">{dl.errorClass}</p>
                    {dl.errorMsg && (
                      <p className="mt-0.5 text-xs text-zinc-500 line-clamp-2">{dl.errorMsg}</p>
                    )}
                  </div>
                )}
                {dl.payload && (
                  <pre className="overflow-x-auto px-5 py-3 text-xs text-zinc-600 dark:text-zinc-400 leading-relaxed">
                    {dl.payload}
                  </pre>
                )}
              </div>
            ))}
          </div>
        )}

      </div>
    </main>
  )
}
