"use client";
import { ScrollReveal } from "~/components/Animations";

const ContentSectionOne = () => {
  return (
    <div className="section zubuz-section-padding2 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-thumb thumb-pr">
                <img src="/images/v1/mocup01.png" alt="" />
                <div className="zubuz-thumb-card">
                  <img src="/images/v1/card1.png" alt="" />
                </div>
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-7 d-flex align-items-center">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-default-content">
                <h2>Order delivery from the app</h2>
                <p>
                  Whether it is a document, a care package, or business inventory,
                  you can book, track, and pay for deliveries right from your
                  phone. No phone calls needed — just a few taps.
                </p>
                <div className="zubuz-extara-mt">
                  <p>
                    <span className="font-semibold">Same-Day Delivery:</span> Need it
                    there fast? Request a rider through the app and get
                    same-day delivery anywhere in Benin City.
                  </p>
                  <p>
                    <span className="font-semibold">Live Tracking:</span> Watch your
                    package move in real time from pickup to drop-off — no more
                    guessing where your rider is.
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
