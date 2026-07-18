import { Inter, Raleway, Outfit } from "next/font/google";
import { SmoothScroll } from "~/components/Animations";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const raleway = Raleway({
  subsets: ["latin"],
  variable: "--font-raleway",
  display: "swap",
});

const outfit = Outfit({
  subsets: ["latin"],
  variable: "--font-outfit",
  display: "swap",
});

export const metadata = {
  title: {
    default: "Engraced Dispatch :: Premium Logistics & Dispatch",
    template: "%s | Engraced Dispatch",
  },
  description:
    "Premium logistics and courier delivery service in Benin City, Nigeria. Fast, reliable, and secure package delivery across the city.",
  keywords: [
    "delivery",
    "courier",
    "Benin City",
    "bike delivery",
    "package delivery",
    "Nigeria",
    "Engraced Dispatch",
    "logistics",
  ],
  authors: [{ name: "Engraced Dispatch" }],
  metadataBase: new URL(
    process.env.NEXT_PUBLIC_SITE_URL || "https://www.engraceddispatch.com"
  ),
  openGraph: {
    type: "website",
    locale: "en_NG",
    siteName: "Engraced Dispatch",
    title: "Engraced Dispatch :: Premium Logistics & Dispatch",
    description:
      "Premium logistics and courier delivery service in Benin City, Nigeria. Fast, reliable, and secure package delivery.",
  },
  twitter: {
    card: "summary_large_image",
    title: "Engraced Dispatch",
    description:
      "Premium logistics and courier delivery service in Benin City, Nigeria.",
  },
  robots: {
    index: true,
    follow: true,
  },
  icons: {
    icon: [{ url: "/favicon.svg", type: "image/svg+xml" }],
    apple: [{ url: "/favicon.svg" }],
  },
  manifest: "/manifest.json",
};

export default function MarketingLayout({ children }) {
  return (
    <div className={`${inter.variable} ${raleway.variable} ${outfit.variable}`}>
      <SmoothScroll />
      {children}
    </div>
  );
}
