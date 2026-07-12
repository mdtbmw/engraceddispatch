"use client";
import Link from "next/link";
import Accordion from "react-bootstrap/Accordion";
import { motion } from "framer-motion";
import { ScrollReveal, StaggerContainer, StaggerItem } from "~/components/Animations";

const Faq = () => {
  return (
    <div className="scetion zubuz-section-padding2 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-7 order-lg-2">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-default-content">
                <h2>Everything you need to know</h2>
                <p>
                  We make delivery simple and stress-free. Whether you are sending
                  documents, gifts, or business supplies, our team ensures a
                  smooth experience from start to finish.
                </p>
                <p>
                  No complicated processes. Just book, pay, and watch your package
                  get delivered. Contact us for any special requirements.
                </p>
                <div className="zubuz-extara-mt">
                  <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                    <Link className="zubuz-default-btn" href="contact-us">
                      <span>Book a Delivery</span>
                    </Link>
                  </motion.div>
                </div>
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-5">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-accordion-wrap">
                <Accordion defaultActiveKey="0" flush>
                  <Accordion.Item eventKey="0">
                    <Accordion.Header>How do I book a delivery?</Accordion.Header>
                    <Accordion.Body>
                      You can book a delivery by calling our hotline, sending a
                      WhatsApp message, or using our online booking form. We will
                      assign a rider to pick up your package.
                    </Accordion.Body>
                  </Accordion.Item>
                  <Accordion.Item eventKey="1">
                    <Accordion.Header>What areas do you cover?</Accordion.Header>
                    <Accordion.Body>
                      We cover all major areas in Benin City including GRA, Uselu,
                      Ekosodin, Oregbeni, New Benin, and surrounding suburbs.
                    </Accordion.Body>
                  </Accordion.Item>
                  <Accordion.Item eventKey="2">
                    <Accordion.Header>How much does delivery cost?</Accordion.Header>
                    <Accordion.Body>
                      Pricing depends on distance and package size. Our rates start
                      from as low as ₦1,000 for local deliveries. Contact us for a
                      quote.
                    </Accordion.Body>
                  </Accordion.Item>
                </Accordion>
              </div>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Faq;
