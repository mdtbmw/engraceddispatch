/* eslint-disable react/no-unescaped-entities */

import Link from "next/link";

const PortfolioDetailsSection = () => {
  return (
    <div className="section zubuz-section-padding">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-7">
              <h2>Same-day delivery for Oba Market</h2>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <p>
                Dispatch partnered with Oba Market traders to provide
                same-day delivery service across Benin City, helping vendors
                reach customers faster than ever before.
              </p>
            </div>
          </div>
        </div>
        <div className="zubuz-portfolio-details-thumb">
          <img src="/images/portfolio/p_1.png" alt="" />
        </div>
        <div className="zubuz-portfolio-info">
          <div className="zubuz-portfolio-info-item">
            <p>Client</p>
            <h3>Oba Market Traders Association</h3>
          </div>
          <div className="zubuz-portfolio-info-item">
            <p>Services:</p>
            <h3>Same-Day Delivery</h3>
          </div>
          <div className="zubuz-portfolio-info-item">
            <p>Duration:</p>
            <h3>3 Months (Ongoing)</h3>
          </div>
          <div className="zubuz-portfolio-info-item">
            <p>Website</p>
            <Link href="">
              <h3>
                Live preview <img src="/images/icon/arrow-right.svg" alt="" />
              </h3>
            </Link>
          </div>
        </div>
        <div className="row">
          <div className="col-lg-8">
            <div className="zubuz-portfolio-details-content-wrap">
              <div className="zubuz-portfolio-details-content">
                <h2>Project overview:</h2>
                <p>
                  Oba Market is one of the busiest commercial hubs in Benin
                  City, with hundreds of vendors needing reliable delivery
                  services for their customers. Dispatch
                  deployed a dedicated fleet of riders to cover all deliveries
                  from the market zone.
                </p>
              </div>
              <div className="zubuz-portfolio-details-content">
                <h2>Project execution:</h2>
                <p>
                  Our team deployed 15 dedicated riders to the Oba Market zone,
                  operating between 8 AM and 6 PM daily. Each rider was equipped
                  with GPS trackers and the Dispatch app for
                  real-time order management.
                </p>
                <h4>Key execution steps:</h4>
                <ul>
                  <li>
                    Step 1: Rider deployment and zone mapping across Oba Market
                    sections
                  </li>
                  <li>Step 2: Vendor onboarding and training on the app</li>
                  <li>Step 3: Route optimisation for faster pickups</li>
                  <li>Step 4: Real-time tracking for all deliveries</li>
                  <li>Step 5: Customer feedback and service refinement</li>
                </ul>
              </div>
              <div className="zubuz-portfolio-details-content">
                <h2>Project results:</h2>
                <p>
                  Within three months, we delivered over 2,500 packages from Oba
                  Market with a 98% on-time delivery rate. Vendor sales increased
                  by 35% as customers gained confidence in fast, reliable
                  delivery. The partnership continues to expand.
                </p>
              </div>
            </div>
          </div>
          <div className="col-lg-4">
            <div className="zubuz-portfolio-details-thumb-wrap">
              <div className="zubuz-portfolio-details-thumb-item">
                <img src="/images/portfolio/p-details1.png" alt="" />
              </div>
              <div className="zubuz-portfolio-details-thumb-item">
                <img src="/images/portfolio/p-details2.png" alt="" />
              </div>
              <div className="zubuz-portfolio-details-thumb-item">
                <img src="/images/portfolio/p-details3.png" alt="" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PortfolioDetailsSection;
