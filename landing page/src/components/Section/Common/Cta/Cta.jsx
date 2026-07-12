"use client";
import Link from "next/link";
import { motion } from "framer-motion";
import { ScrollReveal } from "~/components/Animations";
import { PlayStoreBadge, AppStoreBadge } from "~/components/Ui/StoreBadge";

const Cta = () => {
  return (
    <div className="zubuz-cta-section blue-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-7 order-lg-2 d-flex align-items-center">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-default-content light">
                <h2>Download Dispatch &amp; start delivering</h2>
                <p>
                  Get the Dispatch app to send packages, track
                  deliveries in real time, and manage everything from your
                  phone. Available on iOS and Android.
                </p>
                <div className="zubuz-extara-mt">
                  <div
                    style={{
                      display: "flex",
                      gap: 16,
                      flexWrap: "wrap",
                    }}
                  >
                    <PlayStoreBadge dark />
                    <AppStoreBadge dark />
                  </div>
                </div>
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-5">
            <ScrollReveal variant="slideRight">
              <motion.div
                className="zubuz-cta-thumb"
                whileHover={{ scale: 1.03 }}
                transition={{ duration: 0.3 }}
              >
                <img src="/images/v1/cta-mocup.png" alt="" />
              </motion.div>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cta;
