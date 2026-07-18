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
        }}>
        <div className="container">
          <motion.div
            className="zubuz-hero-content center position-relative"
            variants={containerVariants}
            initial="hidden"
            animate="visible"
          >
            <motion.h1 variants={itemVariants}>{content.heroTitle}</motion.h1>
            <motion.p variants={itemVariants}>{content.heroDescription}</motion.p>
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
            <motion.div
              className="zubuz-hero-shape"
              variants={itemVariants}
              animate={{
                y: [0, -15, 0],
                transition: { duration: 4, repeat: Infinity, ease: "easeInOut" },
              }}
            >
              <img src="/images/v1/shape.png" alt="" />
            </motion.div>
          </motion.div>
          <div className="zubuz-hero-bottom">
            <motion.div
              className="zubuz-hero-thumb"
              initial={{ opacity: 0, y: 80 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.3, ease: "easeOut" }}
            >
              <img src="/images/v1/hero-mocup1.png" alt="" />
            </motion.div>
            <motion.div
              className="zubuz-hero-card card1"
              initial={{ opacity: 0, scale: 0.5, x: -50 }}
              animate={{ opacity: 1, scale: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.6, ease: "easeOut" }}
            >
              <img src="/images/v1/h-card1.png" alt="" />
            </motion.div>
            <motion.div
              className="zubuz-hero-card card2"
              initial={{ opacity: 0, scale: 0.5, x: 50 }}
              animate={{ opacity: 1, scale: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.8, ease: "easeOut" }}
            >
              <img src="/images/v1/h-card2.png" alt="" />
            </motion.div>
            <motion.div
              className="zubuz-hero-card card3"
              initial={{ opacity: 0, scale: 0.5, y: 50 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 1.0, ease: "easeOut" }}
            >
              <img src="/images/v1/h-card4.png" alt="" />
            </motion.div>
            <motion.div
              className="zubuz-hero-card card4"
              initial={{ opacity: 0, scale: 0.5, y: -50 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 1.2, ease: "easeOut" }}
            >
              <img src="/images/v1/h-card3.png" alt="" />
            </motion.div>
          </div>
        </div>
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
