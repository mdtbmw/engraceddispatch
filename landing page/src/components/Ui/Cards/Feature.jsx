"use client";
import { motion } from "framer-motion";

const FeatureCard = ({ title, icon, description }) => {
  return (
    <motion.div
      className="zubuz-iconbox-wrap center"
      whileHover={{
        y: -8,
        boxShadow: "0 20px 60px rgba(245, 166, 35, 0.15)",
        transition: { duration: 0.3, ease: "easeOut" },
      }}
    >
      <motion.div
        className="zubuz-iconbox-icon"
        whileHover={{ scale: 1.1, rotate: [0, -5, 5, -5, 0] }}
        transition={{ duration: 0.4 }}
      >
        <img src={icon} alt={title} />
      </motion.div>
      <div className="zubuz-iconbox-data">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </motion.div>
  );
};

export default FeatureCard;
