/* eslint-disable react/no-unescaped-entities */

"use client";

import Link from "next/link";

const IntegrationSectionTwo = () => {

  return (
    <>
      <div className="section zubuz-section-padding bg-light">
        <div className="container">
          <div className="zubuz-section-title zubuz-two-column-title">
            <div className="row">
              <div className="col-lg-8">
                <h2>Trusted by hundreds of customers</h2>
              </div>
              <div className="col-lg-4 d-flex align-items-center">
                <div className="zubuz-title-btn">
                  <Link className="zubuz-default-btn pill" href="integrations">
                    <span>Read more reviews</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
          <div className="zubuz-video-column">
            <div className="row">
              <div className="col-lg-6">
                <div className="zubuz-video-wrap">
                  <img src="/images/v3/video-bg.png" alt="" />
                </div>
              </div>
              <div className="col-lg-6 d-flex align-items-center">
                <div className="zubuz-testimonial-content">
                  <ul>
                    <li>
                      <img src="/images/v3/star.png" alt="" />
                    </li>
                    <li>
                      <img src="/images/v3/star.png" alt="" />
                    </li>
                    <li>
                      <img src="/images/v3/star.png" alt="" />
                    </li>
                    <li>
                      <img src="/images/v3/star.png" alt="" />
                    </li>
                    <li>
                      <img src="/images/v3/star.png" alt="" />
                    </li>
                  </ul>
                  <p>
                    "Our team's productivity grow up after implementing this
                    SaaS tool. The intuitive interface & seamless collaboration
                    features made a significant impact on our workflow.
                    Game-changer for our company efficiency!”
                  </p>
                  <div className="zubuz-testimonial-authors-wraps">
                    <div className="zubuz-testimonial-authors">
                      <p>Jonas Aly</p>
                      <span>Founder @ Sitemark</span>
                    </div>
                    <div className="zubuz-testimonial-author-logo">
                      <img src="/images/v2/b_v2_5.png" alt="" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default IntegrationSectionTwo;
