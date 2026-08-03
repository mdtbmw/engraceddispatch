"use client";
import { useState, useEffect } from "react";
import { db } from "~/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";

const defaults = {
  heroTitle: "Engraced Dispatch",
  heroSubtitle: "Premium Logistics & Dispatch",
  heroDescription: "Experience the fastest, most reliable delivery network in Benin City. Engraced Dispatch connects individuals and businesses with dedicated courier riders. Track your package live, enjoy seamless payments, and let us handle your logistics with unmatched professionalism.",
  aboutTitle: "Redefining Logistics in Nigeria",
  aboutDescription: "Engraced Dispatch was built with a singular focus: to eliminate the unreliability of traditional delivery services. We understand that whether you're a business sending crucial inventory, or an individual sending a care package to a loved one, your deliveries need to be secure, fast, and transparent.\n\nOur platform integrates state-of-the-art live GPS tracking with a fleet of highly trained, professional dispatch riders. From the moment you place an order on our app, you have complete visibility over your package's journey across Benin City and beyond.\n\nWe pride ourselves on our premium service standards. With competitive pricing, no hidden fees, and dedicated customer support, Engraced Dispatch isn't just a delivery company—we are your trusted logistics partner. We are committed to closing the gap between you and your destination safely and on time.",
  aboutMission: "To provide a seamless, transparent, and ultra-reliable logistics ecosystem for every business and individual in Nigeria.",
  aboutVision: "To become the undisputed benchmark for premium courier and dispatch operations across West Africa.",
  servicesTitle: "Comprehensive Delivery Solutions",
  servicesDescription: "From urgent same-day express runs to bulk corporate logistics, our fleet is equipped to handle your exact needs with precision.",
  teamTitle: "Meet the Engraced Dispatch team",
  teamDescription: "Dedicated professionals committed to providing premium logistics services.",
  contactAddress: "No 18, Sakponba Road, Benin City, Edo State.",
  contactEmail: "hello@engraceddispatch.com",
  contactPhone: "+234 800 123 4567",
  socialTwitter: "https://twitter.com/engraceddispatch",
  socialFacebook: "https://facebook.com/engraceddispatch",
  socialInstagram: "https://instagram.com/engraceddispatch",
  socialLinkedin: "https://linkedin.com/company/engraceddispatch",
  socialGithub: "https://github.com/engraceddispatch",
  footerCopyright: "Engraced Dispatch. All rights reserved.",
  playStoreUrl: "",
  appStoreUrl: "",
};

export function useSiteContent() {
  const [content, setContent] = useState(defaults);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    const unsub = onSnapshot(doc(db, "site_content", "settings"), (snap) => {
      if (snap.exists()) {
        setContent({ ...defaults, ...snap.data() });
      }
      setLoading(false);
    });
    return unsub;
  }, []);
  return { content, loading };
}
