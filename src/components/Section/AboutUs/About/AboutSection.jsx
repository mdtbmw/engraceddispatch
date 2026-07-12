"use client";
import { useSiteContent } from "~/lib/useSiteContent";

const AboutSection = () => {
  const { content } = useSiteContent();
  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-7">
              <h2>{content.aboutTitle}</h2>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <p>{content.aboutDescription}</p>
            </div>
          </div>
        </div>
        <div className="row">
          <div className="col-lg-4 col-md-4">
            <div className="zubuz-about-thumb">
              <img src="/images/about/about1.png" alt="" />
            </div>
            <div className="zubuz-about-thumb">
              <img src="/images/about/about1.png" alt="" />
            </div>
          </div>
          <div className="col-lg-4 col-md-4">
            <div className="zubuz-about-thumb">
              <img src="/images/about/about3.png" alt="" />
            </div>
          </div>
          <div className="col-lg-4 col-md-4">
            <div className="zubuz-about-thumb">
              <img src="/images/about/about4.png" alt="" />
            </div>
            <div className="zubuz-about-thumb">
              <img src="/images/about/about5.png" alt="" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutSection;
