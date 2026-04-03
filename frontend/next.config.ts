import type { NextConfig } from 'next'

// Backend URLs are read at request time by Server Components via process.env.
// Set these in your environment or docker-compose:
//   INGESTOR_URL=http://ingestor-service:8080   (defaults to http://localhost:8081)
//   ALERT_URL=http://alert-service:8080          (defaults to http://localhost:8083)
const nextConfig: NextConfig = {}

export default nextConfig
