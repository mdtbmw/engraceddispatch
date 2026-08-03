"use client";

import Link from "next/link";

const SupportSection = () => {

  return (
    <div className="section zubuz-section-padding">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2 className="rt-mb-20">See how our delivery service works</h2>
          <p>
            Watch our quick demo to see how Engraced Dispatch gets your
            package from point A to point B safely and on time.
          </p>
        </div>
        <div className="zubuz-video-wrap m-0">
          <img src="/images/service/video-bg.png" alt="" style={{ borderRadius: '16px' }} />
        </div>
      </div>
    </div>
  );
};

export default SupportSection;
