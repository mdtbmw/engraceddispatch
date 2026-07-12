"use client";
import { motion } from "framer-motion";
import Icon from "~/components/Ui/Icon";

const FeatureCard = ({ title, icon, description }) => {
  const isImage = icon && icon.startsWith("/");
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
        {isImage ? (
          <img src={icon} alt={title} />
        ) : (
          <Icon name={icon} size={32} />
        )}
      </motion.div>
      <div className="zubuz-iconbox-data">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </motion.div>
  );
};

export default FeatureCard;
