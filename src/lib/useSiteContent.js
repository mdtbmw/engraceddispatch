"use client";
import { useState, useEffect } from "react";
import { db } from "~/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";

const defaults = {
  heroTitle: "Primelink Energy Group",
  heroSubtitle: "Premium Oil & Gas Trading",
  heroDescription: "Primelink Energy Group is a trusted energy trading company specializing in the sourcing, marketing, and global distribution of unrefined crude oil and refined petroleum products.",
  aboutTitle: "About Us",
  aboutDescription: "Primelink Energy Group is a trusted energy trading company specializing in the sourcing, marketing, and global distribution of unrefined crude oil and refined petroleum products. We serve government agencies, private-sector refineries, industrial clients, and commercial buyers with reliable, efficient, and transparent energy supply solutions.\n\nOur product portfolio includes crude oil, heating oil, jet fuel (Jet A-1), diesel, gasoline, kerosene, and other refined petroleum products. Backed by a commitment to quality, compliance, and operational excellence, we ensure every transaction meets international industry standards while delivering value to our clients worldwide.\n\nAt Primelink Energy Group, we are dedicated to building long-term partnerships through integrity, professionalism, competitive pricing, and dependable supply chains that support the growing energy needs of businesses and governments across the globe.",
  aboutMission: "To deliver reliable, efficient, and transparent energy supply solutions.",
  aboutVision: "To become the most trusted energy trading partner globally.",
  servicesTitle: "Our Products & Services",
  servicesDescription: "We deal exclusively in petroleum products, including crude oil, heating oil, jet fuel (Jet A-1), diesel, gasoline, and kerosene.",
  teamTitle: "Meet the Primelink Energy Group team",
  teamDescription: "Dedicated professionals committed to providing premium energy trading services.",
  contactAddress: "No 18, Sakponba Road, Benin City, Edo State.",
  contactEmail: "hello@primelinkenergy.com",
  contactPhone: "+234 800 123 4567",
  socialTwitter: "https://twitter.com/primelinkenergy",
  socialFacebook: "https://facebook.com/primelinkenergy",
  socialInstagram: "https://instagram.com/primelinkenergy",
  socialLinkedin: "https://linkedin.com/company/primelinkenergy",
  socialGithub: "https://github.com/primelinkenergy",
  footerCopyright: "Primelink Energy Group. All rights reserved.",
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
