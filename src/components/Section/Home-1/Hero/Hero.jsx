"use client";
import Link from "next/link";
import { useState } from "react";
import { IoClose } from "react-icons/io5";
import { motion, AnimatePresence } from "framer-motion";
import { ScrollReveal } from "~/components/Animations";
import { PlayStoreBadge, AppStoreBadge } from "~/components/Ui/StoreBadge";
import { useSiteContent } from "~/lib/useSiteContent";

const HeroSection = () => {
  const { content } = useSiteContent();
  const [popup, setPopup] = useState(false);

  const openPopup = () => {
    setPopup(true);
    const iframe = document.getElementById("youtube-video");
    if (iframe) {
      iframe.src = "https://www.youtube.com/embed/SixdAQtWJQ8?si=TPxjQ04JgcZ5eEA9";
    }
  };

  const closePopup = () => {
    setPopup(false);
    const iframe = document.getElementById("youtube-video");
    if (iframe) {
      iframe.src = "";
    }
  };

  const containerVariants = {
    hidden: {},
    visible: {
      transition: {
        staggerChildren: 0.15,
        delayChildren: 0.2,
      },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.6, ease: "easeOut" },
    },
  };

  return (
    <>
      <div
        className="zubuz-hero-section white-bg"
        style={{
          backgroundImage: "url(/images/v1/hero-shape1.png), url(/images/v1/background.png)",
          backgroundSize: "auto, cover",
          backgroundPosition: "center bottom, center",
          backgroundRepeat: "no-repeat",
          position: "relative",
          overflow: "hidden",
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
        }}>
        <div className="container" style={{ position: "relative", zIndex: 2 }}>
          <motion.div
            className="zubuz-hero-content center position-relative"
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            style={{ position: "relative", zIndex: 3 }}
          >
            <motion.h1 variants={itemVariants} style={{ fontSize: "clamp(2rem, 5vw, 3.5rem)", lineHeight: 1.15 }}>
              <span style={{ display: "block", fontWeight: 300, letterSpacing: "-0.02em", color: "#6B7280" }}>Your Remote Career</span>
              <span style={{ display: "block", fontWeight: 900, background: "linear-gradient(135deg, #FFC542, #F59E0B)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent", fontSize: "clamp(2.4rem, 6vw, 4.2rem)" }}>Starts Here</span>
            </motion.h1>
            <motion.p variants={itemVariants} style={{ maxWidth: 600, margin: "16px auto 0", color: "#6B7280", fontSize: "clamp(0.9rem, 1.4vw, 1.1rem)" }}>{content.heroDescription}</motion.p>
            <motion.div className="zubuz-hero-btn-wrap center" variants={itemVariants}>
              <Link className="zubuz-default-btn" href="contact-us">
                <span>Send a Package</span>
              </Link>
              <motion.button
                className="zubuz-default-btn"
                onClick={openPopup}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                style={{ background: "transparent", border: "2px solid #FFC542", color: "#FFC542" }}
              >
                <span>See How It Works</span>
              </motion.button>
            </motion.div>
            <motion.div
              className="zubuz-hero-app-badges"
              variants={itemVariants}
              style={{
                display: "flex",
                justifyContent: "center",
                gap: 16,
                marginTop: 32,
                flexWrap: "wrap",
              }}
            >
              <PlayStoreBadge href={content.playStoreUrl || "#"} />
              <AppStoreBadge href={content.appStoreUrl || "#"} />
            </motion.div>
          </motion.div>
        </div>

        <div style={{
          position: "absolute",
          inset: 0,
          zIndex: 1,
          display: "flex",
          alignItems: "center",
          justifyContent: "flex-end",
          pointerEvents: "none",
        }}>
          <motion.div
            initial={{ opacity: 0, x: 120 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 1, delay: 0.4, ease: "easeOut" }}
            style={{
              width: "clamp(300px, 45vw, 600px)",
              marginRight: "clamp(10px, 3vw, 60px)",
              filter: "drop-shadow(0 20px 60px rgba(0,0,0,0.12))",
            }}
          >
            <img src="/images/v1/hero-mocup1.png" alt style={{ width: "100%", height: "auto", display: "block" }} />
          </motion.div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 1.0, ease: "easeOut" }}
          style={{
            position: "absolute",
            right: "clamp(10px, 4vw, 80px)",
            top: "50%",
            transform: "translateY(-55%)",
            zIndex: 4,
            pointerEvents: "auto",
          }}
        >
          <motion.div
            animate={{ y: [0, -12, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
            style={{
              background: "#fff",
              borderRadius: 16,
              padding: "18px 22px",
              boxShadow: "0 12px 40px rgba(255, 197, 66, 0.25), 0 4px 12px rgba(0,0,0,0.08)",
              border: "1px solid rgba(255, 197, 66, 0.3)",
              minWidth: 180,
              backdropFilter: "blur(8px)",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
              <div style={{ width: 32, height: 32, borderRadius: 8, background: "#FFC542", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16 }}>📦</div>
              <div>
                <div style={{ fontSize: 11, color: "#9CA3AF", fontWeight: 500, textTransform: "uppercase", letterSpacing: "0.05em" }}>Today's Shipments</div>
                <div style={{ fontSize: 20, fontWeight: 800, color: "#111" }}>1,247</div>
              </div>
            </div>
            <div style={{ height: 4, borderRadius: 2, background: "#E5E7EB", overflow: "hidden" }}>
              <motion.div
                initial={{ width: "0%" }}
                animate={{ width: "76%" }}
                transition={{ duration: 1.5, delay: 1.5, ease: "easeOut" }}
                style={{ height: "100%", borderRadius: 2, background: "linear-gradient(90deg, #FFC542, #F59E0B)" }}
              />
            </div>
            <div style={{ fontSize: 10, color: "#9CA3AF", marginTop: 4, textAlign: "right" }}>76% capacity</div>
          </motion.div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 1.3, ease: "easeOut" }}
          style={{
            position: "absolute",
            left: "clamp(10px, 3vw, 50px)",
            top: "58%",
            transform: "translateY(-50%)",
            zIndex: 4,
            pointerEvents: "auto",
          }}
        >
          <motion.div
            animate={{ y: [0, 10, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
            style={{
              background: "#fff",
              borderRadius: 16,
              padding: "16px 20px",
              boxShadow: "0 12px 40px rgba(0,0,0,0.1), 0 4px 12px rgba(0,0,0,0.06)",
              border: "1px solid rgba(0,0,0,0.05)",
              minWidth: 160,
              backdropFilter: "blur(8px)",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6 }}>
              <div style={{ width: 32, height: 32, borderRadius: 8, background: "#10B981", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16 }}>⭐</div>
              <div>
                <div style={{ fontSize: 11, color: "#9CA3AF", fontWeight: 500, textTransform: "uppercase", letterSpacing: "0.05em" }}>Driver Rating</div>
                <div style={{ fontSize: 20, fontWeight: 800, color: "#111" }}>4.9</div>
              </div>
            </div>
            <div style={{ display: "flex", gap: 3 }}>
              {[1,2,3,4,5].map(i => (
                <span key={i} style={{ color: i <= 4 ? "#FFC542" : "#D1D5DB", fontSize: 12 }}>★</span>
              ))}
              <span style={{ fontSize: 10, color: "#9CA3AF", marginLeft: 4 }}>(2.4k)</span>
            </div>
          </motion.div>
        </motion.div>
      </div>
      <AnimatePresence>
        {popup && (
          <motion.div
            className="popup-video popup"
            onClick={closePopup}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
          >
            <motion.div
              className="video-wrapper"
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.8, opacity: 0 }}
              transition={{ duration: 0.3 }}
              onClick={(e) => e.stopPropagation()}
            >
              <iframe
                id="youtube-video"
                className="video"
                width="100%"
                height="100%"
                src="https://www.youtube.com/embed/SixdAQtWJQ8?si=TPxjQ04JgcZ5eEA9"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              ></iframe>
              <motion.button
                className="close-button"
                onClick={closePopup}
                whileHover={{ scale: 1.1, rotate: 90 }}
                whileTap={{ scale: 0.9 }}
              >
                <IoClose className="close-icon" />
              </motion.button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default HeroSection;
