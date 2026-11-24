import Link from "next/link";
import styles from "./SiteHeader.module.css";

/** Placeholder header for M0; navigation, search and cart arrive in M1 and M2. */
export function SiteHeader() {
  return (
    <header className={styles.header}>
      <div className={`container ${styles.inner}`}>
        <Link href="/" className={styles.logo} aria-label="Cadence, về trang chủ">
          Cadence
        </Link>
      </div>
    </header>
  );
}
