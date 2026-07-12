"use client";
import Link from "next/link";
import { motion } from "framer-motion";
import { ScrollReveal, StaggerContainer, StaggerItem } from "~/components/Animations";
import ArrowRightIcon from "~/components/Ui/Icon/ArrowRight";
import blogDatas from "~/data/blogData.json";

const NewsSection = () => {

  return (
    <div className="section zubuz-section-padding2 light-bg">
      <div className="container">
        <ScrollReveal variant="slideUp">
          <div className="zubuz-section-title center">
            <h2>Latest from the Dispatch blog</h2>
          </div>
        </ScrollReveal>
        <StaggerContainer staggerDelay={0.12}>
          <div className="row">
            {blogDatas?.map((blog, index) => (
              <StaggerItem key={index} className="col-xl-4 col-lg-6">
                <motion.div
                  className="zubuz-blog-wrap"
                  whileHover={{ y: -8 }}
                  transition={{ duration: 0.3, ease: "easeOut" }}
                >
                  <Link href={blog?.link}>
                    <div className="zubuz-blog-thumb">
                      <motion.img
                        src={blog?.image}
                        alt=""
                        whileHover={{ scale: 1.05 }}
                        transition={{ duration: 0.4 }}
                      />
                      <div className="zubuz-blog-categorie">{blog?.category}</div>
                    </div>
                  </Link>
                  <div className="zubuz-blog-data">
                    <p>{blog?.date}</p>
                    <Link href={blog?.link}>
                      <h3>{blog?.title}</h3>
                    </Link>
                    <Link href={blog?.link} className="zubuz-blog-btn">
                      <ArrowRightIcon />
                    </Link>
                  </div>
                </motion.div>
              </StaggerItem>
            ))}
          </div>
        </StaggerContainer>
      </div>
    </div>
  );
};

export default NewsSection;
