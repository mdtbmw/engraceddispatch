"use client";
import { useState, useEffect } from "react";
import { db } from "~/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";

const defaults = {
  heroTitle: "Engraced Dispatch",
  heroSubtitle: "Premium Logistics & Dispatch",
  heroDescription: "Engraced Dispatch brings fast, reliable bike and courier delivery services to Benin City. Track your package in real-time and enjoy seamless deliveries across the city.",
  aboutTitle: "Our journey with Engraced Dispatch",
  aboutDescription: "We connect people and businesses across Benin City with fast, reliable courier services. Built to close the gap between you and your destination.",
  aboutMission: "To make delivery seamless for every business and individual in Benin City.",
  aboutVision: "To become the most trusted logistics platform in Nigeria.",
  servicesTitle: "Engraced Dispatch delivery services",
  servicesDescription: "From same-day express to bulk logistics, we have a service tailored to your needs.",
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
