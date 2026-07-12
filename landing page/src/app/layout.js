import { Inter, Raleway, Outfit } from "next/font/google";
import "~/assets/css/bootstrap.min.css";
import "~/assets/css/app.css";
import "~/assets/css/main.css";
import "~/assets/css/react-adjustment.css";
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
    default: "Dispatch :: Fast Delivery in Benin City",
    template: "%s | Dispatch",
  },
  description:
    "Your trusted bike and courier delivery service in Benin City, Nigeria. Fast, reliable, and secure package delivery across the city.",
  keywords: [
    "delivery",
    "courier",
    "Benin City",
    "bike delivery",
    "package delivery",
    "Nigeria",
    "dispatch app",
  ],
  authors: [{ name: "Dispatch" }],
  metadataBase: new URL(
    process.env.NEXT_PUBLIC_SITE_URL || "https://www.dispatchapp.com"
  ),
  openGraph: {
    type: "website",
    locale: "en_NG",
    siteName: "Dispatch",
    title: "Dispatch :: Fast Delivery in Benin City",
    description:
      "Your trusted bike and courier delivery service in Benin City, Nigeria. Fast, reliable, and secure package delivery.",
  },
  twitter: {
    card: "summary_large_image",
    title: "Dispatch",
    description:
      "Fast, reliable bike and courier delivery in Benin City, Nigeria.",
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

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body
        className={`${inter.variable} ${raleway.variable} ${outfit.variable}`}
      >
        <SmoothScroll />
        {children}
      </body>
    </html>
  );
}
