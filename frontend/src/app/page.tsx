import type { Metadata } from "next";
import { Suspense } from "react";
import { fetchBackendHealth } from "@/shared/api/server";
import styles from "./page.module.css";

/** M0 walking skeleton: proves Nginx → Next.js → Spring Boot → PostgreSQL works end to end. */
export const metadata: Metadata = {
  robots: { index: false, follow: false }, // placeholder page, not for search engines
};

export default function HomePage() {
  return (
    <section className={`container ${styles.hero}`}>
      <h1 className="display">Giữ nhịp, đi xa hơn</h1>
      <p className={styles.lead}>Cửa hàng đang được xây dựng.</p>
      <Suspense fallback={<p className={styles.status}>Đang kiểm tra hệ thống…</p>}>
        <BackendStatus />
      </Suspense>
    </section>
  );
}

const LABEL = {
  UP: "Hệ thống sẵn sàng",
  DOWN: "Hệ thống đang gặp sự cố",
  UNREACHABLE: "Không kết nối được tới máy chủ",
} as const;

/** Fresh on every request, so it streams inside Suspense (Cache Components). */
async function BackendStatus() {
  const status = await fetchBackendHealth();
  return (
    <p className={styles.status} data-status={status} role="status">
      <span className={styles.dot} aria-hidden="true" />
      {LABEL[status]}
    </p>
  );
}
