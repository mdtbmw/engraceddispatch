"use client";
import Marquee from "react-fast-marquee";
import { ScrollReveal } from "~/components/Animations";

const BrandSection = () => {
  return (
    <div className="section dark-bg zubuz-section-padding4">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-brand-logo-content">
                <h3>
                  Trusted by hundreds of businesses and individuals using Engraced Dispatch across Benin City
                </h3>
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-7">
            <ScrollReveal variant="slideLeft">
              <Marquee speed="30" className="zubuz-brand-slider">
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_1.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_2.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_3.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_4.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_5.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_6.png" alt="" />
                </div>
              </Marquee>
              <Marquee
                speed="30"
                direction="right"
                className="zubuz-brand-slider"
                style={{marginTop:"25px"}}
              >
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_1.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_2.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_3.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_4.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_5.png" alt="" />
                </div>
                <div className="zubuz-brand-item">
                  <img src="/images/v1/b_6.png" alt="" />
                </div>
              </Marquee>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BrandSection;
