/* eslint-disable react/no-unescaped-entities */

"use client";

import Accordion from "react-bootstrap/Accordion";

const FaqSection = () => {
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Delivery questions answered</h2>
        </div>
        <div className="zubuz-accordion-wrap zubuz-accordion-wrap2">
          <Accordion defaultActiveKey="0" flush>
            <Accordion.Item eventKey="0">
              <Accordion.Header>
                Q: What areas do you deliver to in Benin City?
              </Accordion.Header>
              <Accordion.Body>
                We cover all major areas in Benin City including GRA, Ugbowo,
                Ekiosa, Oba Market, Ring Road, Aduwawa, and Ikpoba Hill. If
                your location is within the Benin City metro area, we can
                deliver to you.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="1">
              <Accordion.Header>
                Q: How much does a delivery cost?
              </Accordion.Header>
              <Accordion.Body>
                Our delivery fees start from just 500 Naira for short-distance
                drops within the same zone. Fees are calculated based on
                distance, package size, and delivery urgency. You get the exact
                price before confirming your order.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="2">
              <Accordion.Header>
                Q: How do I track my package in real time?
              </Accordion.Header>
              <Accordion.Body>
                Once your order is confirmed, you receive a unique tracking link
                via SMS. You can follow your rider&apos;s GPS location live on
                the map and see estimated arrival time directly from your phone.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="3">
              <Accordion.Header>
                Q: How do I become a delivery rider?
              </Accordion.Header>
              <Accordion.Body>
                Simply visit our office at No 18, Sakponba Road with your valid
                ID and driving license. We conduct a quick road test and if you
                pass, you can start riding and earning within 48 hours.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="4">
              <Accordion.Header>
                Q: What if my package is damaged or lost?
              </Accordion.Header>
              <Accordion.Body>
                Every package is insured during transit. If your item is damaged
                or lost, we offer full compensation. Simply report the issue
                within 24 hours and our support team will process your claim
                promptly.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="5">
              <Accordion.Header>
                Q: Can I schedule a delivery for a later time?
              </Accordion.Header>
              <Accordion.Body>
                Yes, you can schedule same-day or next-day deliveries at your
                preferred time. Select the scheduled delivery option when
                placing your order and pick the time slot that works best for
                you.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="6">
              <Accordion.Header>
                Q: How do I pay for my delivery?
              </Accordion.Header>
              <Accordion.Body>
                We accept cash on delivery, bank transfers, and mobile payments
                via Opay and Palmpay. For regular business clients, we also
                offer weekly invoicing and corporate account options.
              </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="7">
              <Accordion.Header>
                Q: What items can I send through your service?
              </Accordion.Header>
              <Accordion.Body>
                You can send documents, food, groceries, electronics, gifts, and
                most household items. Restricted items include flammable
                materials, firearms, and perishables without proper packaging.
                Contact us if unsure about your item.
              </Accordion.Body>
            </Accordion.Item>
          </Accordion>
        </div>
      </div>
    </div>
  );
};

export default FaqSection;
