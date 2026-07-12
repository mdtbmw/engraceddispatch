import React from "react";
import type { Metadata } from "next";
import "./globals.css";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Engraced Dispatch — Premium Logistics & Dispatch",
  description: "Premium Logistics & Dispatch Operations Management",
  icons: {
    icon: [
      { url: "https://engraceddispatch-ffba4.web.app/favicon.svg", type: "image/svg+xml" },
      { url: "https://engraceddispatch-ffba4.web.app/favicon.ico", sizes: "32x32" },
    ],
    apple: "https://engraceddispatch-ffba4.web.app/apple-touch-icon.png",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body suppressHydrationWarning className="min-h-screen antialiased">
        {children}
      </body>
    </html>
  );
}
