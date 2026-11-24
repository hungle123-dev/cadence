import styles from "./SiteFooter.module.css";

/** Placeholder footer for M0; links to the content pages (FR-CNT-01) arrive in M7. */
export function SiteFooter() {
  return (
    <footer className={styles.footer}>
      <div className="container">
        <p className={styles.copy}>© Cadence. Dự án portfolio, không bán hàng thật.</p>
      </div>
    </footer>
  );
}
