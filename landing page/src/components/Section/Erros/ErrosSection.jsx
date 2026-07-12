"use client";

import Link from "next/link";

const ErrosSection = () => {
  return (
    <div className="section zubuz-extra-section">
      <div className="container">
        <div className="zubuz-errors-wrap">
          <div className="zubuz-errors-thumb">
            <img src="/images/about/404.png" alt="" />
          </div>
          <h2>Oops! Looks like you took a wrong turn</h2>
          <div className="zubuz-extara-mt">
            <Link className="zubuz-default-btn" href="/">
              <span>Back To Home</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ErrosSection;
