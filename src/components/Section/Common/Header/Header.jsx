"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import BrandLogo from "~/components/Ui/Logo/BrandLogo";
import { PlayStoreBadge, AppStoreBadge } from "~/components/Ui/StoreBadge";

const Header = ({ dark = false }) => {
  const [isActive, setIsActive] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 100);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const toggleMenu = () => {
    setIsActive(!isActive);
    document.querySelector(".menu-overlay")?.classList.toggle("active");
  };

  const closeMenu = () => {
    setIsActive(false);
    document.querySelector(".menu-overlay")?.classList.remove("active");
  };

  return (
    <motion.header
      className={`site-header site-header--menu-center zubuz-header-section ${dark ? "dark-bg white-menu" : "bg-white"} ${scrolled ? "sticky-menu" : ""}`}
      id="sticky-menu"
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <div className="container">
        <nav className="navbar site-navbar">
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
          >
            <BrandLogo dark={dark} />
          </motion.div>
          <div className="menu-block-wrapper">
            <div className="menu-overlay" onClick={closeMenu}></div>
            <nav
              className={`menu-block ${isActive ? "active" : ""}`}
              id="append-menu-header"
            >
              <div className="mobile-menu-head">
                <div
                  className="mobile-menu-close"
                  onClick={closeMenu}
                >
                  &times;
                </div>
              </div>
              <ul className="site-menu-main">
                <li className="nav-item">
                  <Link href="/" className="nav-link-item" onClick={closeMenu}>Home</Link>
                </li>
                <li className="nav-item">
                  <Link href="/about-us" className="nav-link-item" onClick={closeMenu}>About Us</Link>
                </li>
                <li className="nav-item">
                  <Link href="/service" className="nav-link-item" onClick={closeMenu}>Services</Link>
                </li>
                <li className="nav-item">
                  <Link href="/blog" className="nav-link-item" onClick={closeMenu}>Blog</Link>
                </li>
                <li className="nav-item">
                  <Link href="/contact-us" className="nav-link-item" onClick={closeMenu}>Contact</Link>
                </li>
              </ul>
            </nav>
          </div>

          <motion.div
            className="header-btn header-btn-l1 ms-auto d-none d-xl-inline-flex"
            initial={{ opacity: 0, x: 30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            style={{ gap: 8 }}
          >
            <PlayStoreBadge dark={dark} />
            <AppStoreBadge dark={dark} />
          </motion.div>
          <div
            className={`mobile-menu-trigger ${dark ? "" : "light"}`}
            onClick={toggleMenu}
          >
            <span></span>
          </div>
        </nav>
      </div>
    </motion.header>
  );
};

export default Header;
