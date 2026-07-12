/* eslint-disable react/no-unescaped-entities */
"use client";

import Accordion from "~/components/Ui/Accordion";

const accordionItems = [
  {
    question: "Q: What areas do you deliver to in Benin City?",
    answer:
      "We cover all major areas in Benin City including GRA, Ugbowo, Ekiosa, Oba Market, Ring Road, Aduwawa, and Ikpoba Hill. If your location is within the Benin City metro area, we can deliver to you.",
  },
  {
    question: "Q: How much does a delivery cost?",
    answer:
      "Our delivery fees start from just 500 Naira for short-distance drops within the same zone. Fees are calculated based on distance, package size, and delivery urgency. You get the exact price before confirming your order.",
  },
  {
    question: "Q: How do I track my package in real time?",
    answer:
      "Once your order is confirmed, you receive a unique tracking link via SMS. You can follow your rider&apos;s GPS location live on the map and see estimated arrival time directly from your phone.",
  },
  {
    question: "Q: How do I become a delivery rider?",
    answer:
      "Simply visit our office at No 18, Sakponba Road with your valid ID and driving license. We conduct a quick road test and if you pass, you can start riding and earning within 48 hours.",
  },
  {
    question:
      "Q: What if my package is damaged or lost?",
    answer:
      "Every package is insured during transit. If your item is damaged or lost, we offer full compensation. Simply report the issue within 24 hours and our support team will process your claim promptly.",
  },
];

const FAQSection = () => {
  return (
    <div className="section zubuz-section-padding2 white-bg">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Find all the answers to your confusion</h2>
        </div>
        <Accordion items={accordionItems} />
      </div>
    </div>
  );
};

export default FAQSection;
