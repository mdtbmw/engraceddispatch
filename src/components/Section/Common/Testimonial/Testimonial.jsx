"use client";
import Link from "next/link";
import { useEffect, useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ScrollReveal } from "~/components/Animations";
import { useTestimonials } from "~/lib/useCmsData";

const StarRating = () => (
  <div className="zubuz-testimonial-rating">
    <ul>
      {[...Array(5)].map((_, i) => (
        <li key={i}><img src="/images/icon/star-green.svg" alt="" /></li>
      ))}
    </ul>
  </div>
);

const TestimonialSection = ({ button = "true" }) => {
  const { items: testimonials } = useTestimonials();
  const [current, setCurrent] = useState(0);
  const [direction, setDirection] = useState(1);

  const next = useCallback(() => {
    setDirection(1);
    setCurrent((prev) => (prev + 1) % testimonials.length);
  }, []);

  const prev = useCallback(() => {
    setDirection(-1);
    setCurrent((prev) => (prev - 1 + testimonials.length) % testimonials.length);
  }, []);

  useEffect(() => {
    if (!testimonials.length) return;
    const timer = setInterval(next, 5000);
    return () => clearInterval(timer);
  }, [next]);

  if (!testimonials.length) return null;

  const raw = testimonials[current];
  const t = raw ? { ...raw, author: { name: raw.authorName, role: raw.authorRole, avatar: raw.authorAvatar } } : {};

  const variants = {
    enter: (dir) => ({ x: dir > 0 ? 200 : -200, opacity: 0 }),
    center: { x: 0, opacity: 1 },
    exit: (dir) => ({ x: dir > 0 ? -200 : 200, opacity: 0 }),
  };

  return (
    <div className="section zubuz-section-padding2 light-bg">
      <div className="container">
        <ScrollReveal variant="slideUp">
          <div className="zubuz-section-title center">
            <h2>What Engraced Dispatch customers say</h2>
          </div>
        </ScrollReveal>

        <div className="zubuz-testimonial-carousel">
          <div className="zubuz-testimonial-carousel-inner">
            <AnimatePresence mode="wait" custom={direction}>
              <motion.div
                key={current}
                custom={direction}
                variants={variants}
                initial="enter"
                animate="center"
                exit="exit"
                transition={{ duration: 0.4, ease: "easeInOut" }}
                className="zubuz-testimonial-carousel-card"
              >
                <div className="zubuz-testimonial-carousel-content">
                  <StarRating />
                  <h3>{t.title}</h3>
                  <p className="zubuz-testimonial-quote">&ldquo;{t.description}&rdquo;</p>
                  <div className="zubuz-testimonial-carousel-author">
                    <div className="zubuz-testimonial-author-thumb">
                      <img src={t.author?.avatar} alt="" />
                    </div>
                    <div className="zubuz-testimonial-author-data">
                      <span>{t.author?.name}</span>
                      <p>{t.author?.role}</p>
                    </div>
                  </div>
                </div>
              </motion.div>
            </AnimatePresence>
          </div>

          <div className="zubuz-testimonial-dots">
            {testimonials.map((_, i) => (
              <button
                key={i}
                className={`zubuz-testimonial-dot ${i === current ? "active" : ""}`}
                onClick={() => { setDirection(i > current ? 1 : -1); setCurrent(i); }}
                aria-label={`Go to review ${i + 1}`}
              />
            ))}
          </div>

          <button className="zubuz-testimonial-arrow left" onClick={prev} aria-label="Previous">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M12.5 15L7.5 10L12.5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
          </button>
          <button className="zubuz-testimonial-arrow right" onClick={next} aria-label="Next">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M7.5 15L12.5 10L7.5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
          </button>
        </div>

        {button === "true" && (
          <ScrollReveal variant="fadeIn" delay={0.3}>
            <div className="zubuz-testimonial-btn">
              <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                <Link className="zubuz-default-btn" href="testimonials">
                  <span>View All Reviews</span>
                </Link>
              </motion.div>
            </div>
          </ScrollReveal>
        )}
      </div>
    </div>
  );
};

export default TestimonialSection;