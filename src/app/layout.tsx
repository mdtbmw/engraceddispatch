import React from "react";
import type { Metadata } from "next";
import "./globals.css";
import "~/assets/css/app.css";
import "~/assets/css/main.css";
import "~/assets/css/react-adjustment.css";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Primelink Energy Group — Premium Oil & Gas Trading",
  description: "Global energy trading company specializing in unrefined crude oil and refined petroleum products.",
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
