"use client";

import Link from "next/link";
import { useState } from "react";

const PricingSection = () => {
  const [isMonthly, setIsMonthly] = useState(false);
  const priceToggole = () => {
    setIsMonthly(!isMonthly);
  };
  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Delivery plans for every budget</h2>
        </div>
        <div className="pricing-btn">
          <div className="toggle-btn">
            <label>Monthly </label>
            <input
              className="form-check-input btn-toggle price-deck-trigger"
              type="checkbox"
              id="flexSwitchCheckDefault"
              data-pricing-trigger
              data-target="#table-price-value"
              //   checked
              onClick={priceToggole}
            />
            <label>Annually</label>
          </div>
        </div>
        <div
          className="row zubuz-pricing-four-column"
          id="table-price-value"
          data-pricing-dynamic
          data-value-active="monthly"
        >
          <div className="col-xl-3 col-md-6">
            <div className="zubuz-pricing-wrap">
              <div className="zubuz-pricing-header">
                <h5>Basic</h5>
              </div>
              <div className="zubuz-pricing-price">
                <h2>₦</h2>
                <div
                  className="zubuz-price dynamic-value"
                >
                  {isMonthly ? 1000 : 800}
                </div>
                <p
                  className="dynamic-value"
                >
                  /{isMonthly ? "delivery" : "delivery"}
                </p>
              </div>
              <div className="zubuz-pricing-description">
                <p>For occasional personal deliveries within Benin City</p>
              </div>
              <div className="zubuz-pricing-body">
                <p>Basic plan includes:</p>
                <ul>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Single package delivery
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Up to 5 km distance
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Standard delivery time
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    SMS notifications
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Pay on delivery
                  </li>
                </ul>
              </div>
              <Link className="zubuz-pricing-btn" href="contact-us">
                Book Now
              </Link>
            </div>
          </div>
          <div className="col-xl-3 col-md-6">
            <div className="zubuz-pricing-wrap">
              <div className="zubuz-pricing-header">
                <h5>Standard</h5>
              </div>
              <div className="zubuz-pricing-price">
                <h2>₦</h2>
                <div
                  className="zubuz-price dynamic-value"
                >
                  {isMonthly ? 2500 : 2000}
                </div>
                <p
                  className="dynamic-value"
                >
                  /{isMonthly ? "delivery" : "delivery"}
                </p>
              </div>
              <div className="zubuz-pricing-description">
                <p>For regular deliveries and small business packages</p>
              </div>
              <div className="zubuz-pricing-body">
                <p>Standard plan includes:</p>
                <ul>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Up to 3 packages
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Up to 15 km distance
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Real-time GPS tracking
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Priority delivery
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Free pickup
                  </li>
                </ul>
              </div>
              <Link className="zubuz-pricing-btn" href="contact-us">
                Book Now
              </Link>
            </div>
          </div>
          <div className="col-xl-3 col-md-6">
            <div className="zubuz-pricing-wrap active">
              <div className="zubuz-pricing-header">
                <h5>Business</h5>
              </div>
              <div className="zubuz-pricing-price">
                <h2>₦</h2>
                <div
                  className="zubuz-price dynamic-value"
                >
                  {isMonthly ? 5000 : 4500}
                </div>
                <p
                  className="dynamic-value"
                >
                  /{isMonthly ? "delivery" : "delivery"}
                </p>
              </div>
              <div className="zubuz-pricing-description">
                <p>For businesses with frequent delivery needs</p>
              </div>
              <div className="zubuz-pricing-body">
                <p>Business plan includes:</p>
                <ul>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Up to 10 daily deliveries
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Unlimited distance
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Package insurance
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Dedicated account manager
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Monthly invoicing
                  </li>
                </ul>
              </div>
              <Link className="zubuz-pricing-btn active" href="contact-us">
                Book Now
              </Link>
            </div>
          </div>
          <div className="col-xl-3 col-md-6">
            <div className="zubuz-pricing-wrap">
              <div className="zubuz-pricing-header">
                <h5>Enterprise</h5>
              </div>
              <div className="zubuz-pricing-price">
                <h2>₦</h2>
                <div
                  className="zubuz-price dynamic-value"
                >
                  Custom
                </div>
                <p
                  className="dynamic-value"
                >
                  /quote
                </p>
              </div>
              <div className="zubuz-pricing-description">
                <p>Tailored logistics for large organisations</p>
              </div>
              <div className="zubuz-pricing-body">
                <p>Enterprise plan includes:</p>
                <ul>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Unlimited daily deliveries
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Dedicated rider fleet
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Custom reporting dashboard
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    Priority SLA agreement
                  </li>
                  <li>
                    <img src="/images/v3/check.png" alt="" />
                    24/7 dedicated support
                  </li>
                </ul>
              </div>
              <Link className="zubuz-pricing-btn" href="contact-us">
                Contact Us
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PricingSection;
