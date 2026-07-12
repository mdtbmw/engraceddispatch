"use client";
import { motion } from "framer-motion";

export function MotionButton({ children, className, href, ...props }) {
  const Component = href ? motion.a : motion.button;
  return (
    <Component
      className={className}
      href={href}
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.95 }}
      transition={{ type: "spring", stiffness: 400, damping: 17 }}
      {...props}
    >
      {children}
    </Component>
  );
}

export function MotionIcon({ children, className }) {
  return (
    <motion.span
      className={className}
      whileHover={{ rotate: [0, -10, 10, -10, 0], transition: { duration: 0.5 } }}
    >
      {children}
    </motion.span>
  );
}
