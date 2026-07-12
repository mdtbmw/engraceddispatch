"use client";
import Link from "next/link";
import { motion } from "framer-motion";
import ArrowRightIcon from "../Icon/ArrowRight";

const BlogCard = ({ title, category, image, date, link }) => {
  return (
    <div className="col-xl-4 col-lg-6">
      <motion.div
        className="zubuz-blog-wrap"
        whileHover={{ y: -8 }}
        transition={{ duration: 0.3, ease: "easeOut" }}
      >
        <Link href={link}>
          <div className="zubuz-blog-thumb">
            <motion.img
              src={image}
              alt=""
              whileHover={{ scale: 1.05 }}
              transition={{ duration: 0.4 }}
            />
            <div className="zubuz-blog-categorie">{category}</div>
          </div>
        </Link>
        <div className="zubuz-blog-data">
          <p>{date}</p>
          <Link href={link}>
            <h3>{title}</h3>
          </Link>
          <Link href={link} className="zubuz-blog-btn">
            <ArrowRightIcon />
          </Link>
        </div>
      </motion.div>
    </div>
  );
};

export default BlogCard;
