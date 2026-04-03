export const dynamic = 'force-dynamic'

const ALERT_URL = process.env.ALERT_URL ?? 'http://localhost:8083'

export async function GET() {
  const upstream = await fetch(`${ALERT_URL}/alerts/stream`, {
    headers: { Accept: 'text/event-stream', 'Cache-Control': 'no-cache' },
  })

  return new Response(upstream.body, {
    headers: {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache',
      Connection: 'keep-alive',
    },
  })
}
