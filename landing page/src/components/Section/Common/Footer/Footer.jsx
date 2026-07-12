"use client";
import Link from "next/link";
import {
  FaFacebookF,
  FaInstagram,
  FaLinkedin,
  FaTwitter,
  FaMapMarkerAlt,
  FaPhoneAlt,
  FaEnvelope,
} from "react-icons/fa";
import BrandLogo from "~/components/Ui/Logo/BrandLogo";
import { PlayStoreBadge, AppStoreBadge } from "~/components/Ui/StoreBadge";

const Footer = () => {
  const year = new Date().getFullYear();
  return (
    <footer className="zubuz-footer-section main-footer dark-bg" style={{ paddingTop: 80 }}>
      <div className="container">
        <div className="zubuz-footer-top">
          <div className="row">
            <div className="col-xl-4 col-lg-6 col-md-6">
              <div className="zubuz-footer-textarea" style={{ marginBottom: 32 }}>
                <BrandLogo dark />
                <p style={{ marginTop: 16, lineHeight: 1.7, color: "rgba(255,255,255,0.7)" }}>
                  Get the app and start sending packages across Benin City
                  with just a few taps. Fast, reliable, and trackable
                  delivery — right from your phone.
                </p>
                <div style={{ display: "flex", gap: 12, flexWrap: "wrap", marginTop: 20 }}>
                  <PlayStoreBadge dark />
                  <AppStoreBadge dark />
                </div>
              </div>
            </div>
            <div className="col-xl-2 col-lg-6 col-md-6">
              <div className="zubuz-footer-menu" style={{ marginBottom: 32 }}>
                <div className="zubuz-footer-title">
                  <p style={{ color: "#fff", fontWeight: 600, marginBottom: 16 }}>Quick Links</p>
                </div>
                <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
                  {[
                    { label: "Home", href: "/" },
                    { label: "About Us", href: "/about-us" },
                    { label: "Services", href: "/service" },

                    { label: "Blog", href: "/blog" },
                  ].map((link) => (
                    <li key={link.href} style={{ marginBottom: 10 }}>
                      <Link
                        href={link.href}
                        style={{
                          color: "rgba(255,255,255,0.7)",
                          textDecoration: "none",
                          fontSize: 15,
                          transition: "color 0.2s",
                        }}
                        onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
                        onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.7)")}
                      >
                        {link.label}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
            <div className="col-xl-3 col-lg-6 col-md-6">
              <div className="zubuz-footer-menu" style={{ marginBottom: 32 }}>
                <div className="zubuz-footer-title">
                  <p style={{ color: "#fff", fontWeight: 600, marginBottom: 16 }}>Services</p>
                </div>
                <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
                  {[
                    { label: "Bike Delivery", href: "/service" },
                    { label: "Courier Service", href: "/service" },
                    { label: "Same-Day Delivery", href: "/service" },
                    { label: "Bulk Delivery", href: "/service" },
                    { label: "Document Delivery", href: "/service" },
                  ].map((link) => (
                    <li key={link.label} style={{ marginBottom: 10 }}>
                      <Link
                        href={link.href}
                        style={{
                          color: "rgba(255,255,255,0.7)",
                          textDecoration: "none",
                          fontSize: 15,
                          transition: "color 0.2s",
                        }}
                        onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
                        onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.7)")}
                      >
                        {link.label}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
            <div className="col-xl-3 col-lg-6 col-md-6">
              <div className="zubuz-footer-menu" style={{ marginBottom: 32 }}>
                <div className="zubuz-footer-title">
                  <p style={{ color: "#fff", fontWeight: 600, marginBottom: 16 }}>Contact</p>
                </div>
                <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
                  <li style={{ marginBottom: 12, display: "flex", alignItems: "flex-start", gap: 10 }}>
                    <FaMapMarkerAlt style={{ color: "#F5A623", marginTop: 3, flexShrink: 0 }} size={14} />
                    <span style={{ color: "rgba(255,255,255,0.7)", fontSize: 15 }}>
                      No 18, Sakponba Road, Benin City, Edo State
                    </span>
                  </li>
                  <li style={{ marginBottom: 12, display: "flex", alignItems: "center", gap: 10 }}>
                    <FaPhoneAlt style={{ color: "#F5A623", flexShrink: 0 }} size={14} />
                    <Link
                      href="tel:+2348000000000"
                      style={{ color: "rgba(255,255,255,0.7)", textDecoration: "none", fontSize: 15 }}
                      onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
                      onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.7)")}
                    >
                      +234 800 000 0000
                    </Link>
                  </li>
                  <li style={{ marginBottom: 12, display: "flex", alignItems: "center", gap: 10 }}>
                    <FaEnvelope style={{ color: "#F5A623", flexShrink: 0 }} size={14} />
                    <Link
                      href="mailto:hello@dispatchapp.com"
                      style={{ color: "rgba(255,255,255,0.7)", textDecoration: "none", fontSize: 15 }}
                      onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
                      onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.7)")}
                    >
                      hello@dispatchapp.com
                    </Link>
                  </li>
                  <li style={{ marginTop: 16, display: "flex", gap: 12 }}>
                    {[
                      { icon: FaTwitter, url: "https://twitter.com/" },
                      { icon: FaFacebookF, url: "https://facebook.com/" },
                      { icon: FaInstagram, url: "https://www.instagram.com/" },
                      { icon: FaLinkedin, url: "https://www.linkedin.com/" },
                    ].map(({ icon: Icon, url }, i) => (
                      <a
                        key={i}
                        href={url}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          width: 36,
                          height: 36,
                          borderRadius: 8,
                          background: "rgba(255,255,255,0.1)",
                          color: "rgba(255,255,255,0.7)",
                          fontSize: 15,
                          transition: "all 0.2s",
                        }}
                        onMouseEnter={(e) => {
                          e.target.style.background = "#F5A623";
                          e.target.style.color = "#000";
                        }}
                        onMouseLeave={(e) => {
                          e.target.style.background = "rgba(255,255,255,0.1)";
                          e.target.style.color = "rgba(255,255,255,0.7)";
                        }}
                      >
                        <Icon />
                      </a>
                    ))}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div
          style={{
            borderTop: "1px solid rgba(255,255,255,0.1)",
            paddingTop: 24,
            paddingBottom: 24,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            flexWrap: "wrap",
            gap: 12,
          }}
        >
          <p style={{ color: "rgba(255,255,255,0.5)", fontSize: 14, margin: 0 }}>
            &copy;{year} Dispatch. All rights reserved.
          </p>
          <div style={{ display: "flex", gap: 20 }}>
            <Link href="/faq" style={{ color: "rgba(255,255,255,0.5)", fontSize: 14, textDecoration: "none" }}
              onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
              onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.5)")}
            >
              Privacy Policy
            </Link>
            <Link href="/faq" style={{ color: "rgba(255,255,255,0.5)", fontSize: 14, textDecoration: "none" }}
              onMouseEnter={(e) => (e.target.style.color = "#F5A623")}
              onMouseLeave={(e) => (e.target.style.color = "rgba(255,255,255,0.5)")}
            >
              Terms of Service
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
