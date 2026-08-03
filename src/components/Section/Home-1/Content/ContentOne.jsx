"use client";
import { ScrollReveal } from "~/components/Animations";

const ContentSectionOne = () => {
  return (
    <div className="section zubuz-section-padding2 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-thumb thumb-pr" style={{ borderRadius: '16px', overflow: 'hidden' }}>
                <img src="/images/v1/oil-tanker.jpg" alt="Oil Tanker" style={{ width: '100%', height: 'auto', objectFit: 'cover' }} />
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-7 d-flex align-items-center">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-default-content">
                <h2>Global Energy Sourcing & Logistics</h2>
                <p>
                  At Primelink Energy Group, we oversee the seamless flow of energy commodities. From sourcing unrefined crude oil to delivering refined petroleum products to global ports, our operations are built on efficiency and strict compliance.
                </p>
                <div className="zubuz-extara-mt">
                  <p>
                    <span className="font-semibold" style={{ color: '#F26522' }}>Maritime Logistics:</span> We partner with world-class shipping fleets to ensure the safe, timely delivery of crude oil and refined products across international waters.
                  </p>
                  <p>
                    <span className="font-semibold" style={{ color: '#F26522' }}>Supply Chain Integrity:</span> Every step of our process is monitored to guarantee product quality, prevent delays, and meet the specific energy needs of our global clients.
                  </p>
                </div>
              </div>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentSectionOne;
