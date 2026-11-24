/**
 * Liveness check for Docker's HEALTHCHECK: answers as soon as the Next.js server can serve requests.
 * It does not call the backend on purpose (the backend has its own health check), so a backend
 * outage does not make Docker restart the frontend too.
 */
export function GET() {
  return new Response("ok", {
    headers: { "Content-Type": "text/plain", "Cache-Control": "no-store" },
  });
}
