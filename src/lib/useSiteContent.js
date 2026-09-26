"use client";
import { useState, useEffect } from "react";
import { db } from "~/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";

const defaults = {
  heroTitle: "ESDispatch",
  heroSubtitle: "PREMIUM LOGISTICS & DISPATCH",
  heroDescription: "Experience the fastest, most reliable delivery network in Benin City. ESDispatch connects individuals and businesses with dedicated courier riders. Track your package live, enjoy seamless payments, and let us handle your logistics with unmatched professionalism.",
  aboutTitle: "Redefining Logistics in Benin City",
  aboutDescription: "ESDispatch was built with a singular focus: to eliminate the unreliability of traditional delivery services. We understand that whether you're a business sending crucial inventory, or an individual sending a package, your deliveries need to be secure, fast, and transparent.\n\nOur platform integrates live GPS tracking with a fleet of vetted, professional dispatch riders. From the moment you place an order, you have complete visibility over your package's journey.\n\nWith competitive pricing, no hidden fees, and dedicated customer support, ESDispatch is your trusted logistics partner in Edo State.",
  aboutMission: "To provide a seamless, transparent, and ultra-reliable logistics ecosystem for every business and individual in Nigeria.",
  aboutVision: "To become the undisputed benchmark for premium courier and dispatch operations across Nigeria.",
  servicesTitle: "Comprehensive Delivery Solutions",
  servicesDescription: "From urgent same-day express runs to bulk corporate logistics, our fleet is equipped to handle your exact needs with precision.",
  teamTitle: "Meet the ESDispatch Team",
  teamDescription: "Dedicated professionals committed to delivering excellence.",
  contactAddress: "17 Upper Adesuwa Road, GRA, Benin City, Edo State",
  contactEmail: "support@esdispatch.com",
  contactPhone: "+234 818 584 0000",
  socialTwitter: "https://twitter.com/esdispatch",
  socialFacebook: "https://facebook.com/esdispatch",
  socialInstagram: "https://instagram.com/esdispatch",
  socialLinkedin: "https://linkedin.com/company/esdispatch",
  socialGithub: "https://github.com/esdispatch",
  footerCopyright: "© 2026 ESDispatch. All rights reserved.",
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
