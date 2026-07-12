"use client";
import { motion } from "framer-motion";

const TestimonialCard = ({ title, description, avatar, name, role }) => {
  return (
    <motion.div
      className="zubuz-testimonial-wrap"
      whileHover={{
        y: -5,
        boxShadow: "0 15px 50px rgba(245, 166, 35, 0.15)",
        transition: { duration: 0.3 },
      }}
    >
      <div className="zubuz-testimonial-rating">
        <ul>
          <li><img src="/images/icon/star-green.svg" alt="" /></li>
          <li><img src="/images/icon/star-green.svg" alt="" /></li>
          <li><img src="/images/icon/star-green.svg" alt="" /></li>
          <li><img src="/images/icon/star-green.svg" alt="" /></li>
          <li><img src="/images/icon/star-green.svg" alt="" /></li>
        </ul>
      </div>
      <div className="zubuz-testimonial-data">
        <h3>{title}</h3>
        <p>&ldquo;{description}&rdquo;</p>
      </div>
      <div className="zubuz-testimonial-author">
        <div className="zubuz-testimonial-author-thumb">
          <img src={avatar} alt="" />
        </div>
        <div className="zubuz-testimonial-author-data">
          <span>{name}</span>
          <p>{role}</p>
        </div>
      </div>
    </motion.div>
  );
};

export default TestimonialCard;
