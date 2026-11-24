import "server-only";

/**
 * Base URL of the backend for code running on the Next.js server.
 * Inside Docker it is the internal network address (architecture 5.2); only public APIs are called
 * from here, and no user cookie is ever forwarded.
 */
export function apiBaseUrl(): string {
  return process.env.API_INTERNAL_URL ?? "http://localhost:8080/api";
}

export type HealthStatus = "UP" | "DOWN" | "UNREACHABLE";

export async function fetchBackendHealth(): Promise<HealthStatus> {
  try {
    const response = await fetch(`${apiBaseUrl()}/actuator/health`, {
      signal: AbortSignal.timeout(3000),
    });
    if (!response.ok) return "DOWN";
    const body = (await response.json()) as { status?: string };
    return body.status === "UP" ? "UP" : "DOWN";
  } catch {
    return "UNREACHABLE";
  }
}
