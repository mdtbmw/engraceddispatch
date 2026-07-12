"use client";
import { motion } from "framer-motion";

const variants = {
  fadeIn: {
    hidden: { opacity: 0 },
    visible: { opacity: 1, transition: { duration: 0.6 } },
  },
  slideUp: {
    hidden: { opacity: 0, y: 60 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } },
  },
  slideDown: {
    hidden: { opacity: 0, y: -60 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } },
  },
  slideLeft: {
    hidden: { opacity: 0, x: 60 },
    visible: { opacity: 1, x: 0, transition: { duration: 0.6, ease: "easeOut" } },
  },
  slideRight: {
    hidden: { opacity: 0, x: -60 },
    visible: { opacity: 1, x: 0, transition: { duration: 0.6, ease: "easeOut" } },
  },
  scaleIn: {
    hidden: { opacity: 0, scale: 0.8 },
    visible: { opacity: 1, scale: 1, transition: { duration: 0.5, ease: "easeOut" } },
  },
  zoomIn: {
    hidden: { opacity: 0, scale: 0.6 },
    visible: {
      opacity: 1,
      scale: 1,
      transition: { duration: 0.5, ease: "easeOut" },
    },
  },
  flipX: {
    hidden: { opacity: 0, rotateX: -90 },
    visible: {
      opacity: 1,
      rotateX: 0,
      transition: { duration: 0.6, ease: "easeOut" },
    },
  },
  none: {
    hidden: {},
    visible: {},
  },
};

export function ScrollReveal({
  children,
  className,
  variant = "slideUp",
  delay = 0,
  duration,
  once = true,
  distance = 60,
  style,
}) {
  const v = variants[variant] || variants.slideUp;
  const customVariant = {
    hidden: {
      ...v.hidden,
      ...(distance !== 60 && variant === "slideUp" ? { y: distance } : {}),
      ...(distance !== 60 && variant === "slideLeft" ? { x: distance } : {}),
      ...(distance !== 60 && variant === "slideRight" ? { x: -distance } : {}),
    },
    visible: {
      ...v.visible,
      ...(duration ? { transition: { ...v.visible.transition, duration } } : {}),
      transition: {
        ...v.visible.transition,
        ...(delay ? { delay } : {}),
        ...(duration ? { duration } : {}),
      },
    },
  };

  return (
    <motion.div
      className={className}
      variants={customVariant}
      initial="hidden"
      whileInView="visible"
      viewport={{ once, margin: "-50px" }}
      style={style}
    >
      {children}
    </motion.div>
  );
}

export function StaggerContainer({
  children,
  className,
  staggerDelay = 0.1,
  once = true,
}) {
  return (
    <motion.div
      className={className}
      initial="hidden"
      whileInView="visible"
      viewport={{ once, margin: "-50px" }}
      variants={{
        hidden: {},
        visible: {
          transition: {
            staggerChildren: staggerDelay,
          },
        },
      }}
    >
      {children}
    </motion.div>
  );
}

export function StaggerItem({ children, className, variant = "slideUp" }) {
  const v = variants[variant] || variants.slideUp;
  return (
    <motion.div className={className} variants={v}>
      {children}
    </motion.div>
  );
}

export function ParallaxSection({ children, className, speed = 0.3 }) {
  return (
    <div className={`parallax-wrapper ${className || ""}`}>
      <motion.div
        initial={{ y: 0 }}
        whileInView={{ y: 0 }}
        viewport={{ once: false }}
        style={{ position: "relative" }}
      >
        {children}
      </motion.div>
    </div>
  );
}

export function ScaleOnScroll({ children, className, threshold = 0.2 }) {
  return (
    <motion.div
      className={className}
      initial={{ scale: 0.9, opacity: 0 }}
      whileInView={{
        scale: 1,
        opacity: 1,
        transition: { duration: 0.7, ease: "easeOut" },
      }}
      viewport={{ once: true, amount: threshold }}
    >
      {children}
    </motion.div>
  );
}
