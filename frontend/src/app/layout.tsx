import type { Metadata } from "next";
import { Barlow_Condensed, Be_Vietnam_Pro } from "next/font/google";
import { SiteFooter } from "@/shared/ui/SiteFooter";
import { SiteHeader } from "@/shared/ui/SiteHeader";
import "./globals.css";

const display = Barlow_Condensed({
  variable: "--font-barlow",
  subsets: ["latin", "vietnamese"],
  weight: ["600", "700", "800"],
});

const sans = Be_Vietnam_Pro({
  variable: "--font-be-vietnam",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: { default: "Cadence — Giày và đồ thể thao", template: "%s | Cadence" },
  description: "Giày chạy bộ, đồ tập và lifestyle cho người Việt.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="vi" className={`${display.variable} ${sans.variable}`}>
      <body>
        <a className="skip-link" href="#main">Bỏ qua tới nội dung chính</a>
        <SiteHeader />
        <main id="main">{children}</main>
        <SiteFooter />
      </body>
    </html>
  );
}
