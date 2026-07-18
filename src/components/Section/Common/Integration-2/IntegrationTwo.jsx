/* eslint-disable react/no-unescaped-entities */

"use client";

import Link from "next/link";
import { useState } from "react";
import { IoClose } from "react-icons/io5";

const IntegrationSectionTwo = () => {
  const [popup, setPopup] = useState(false);

  const openPopup = () => {
    setPopup(true);
    const iframe = document.getElementById("youtube-video");
    if (iframe) {
      iframe.src =
        "https://www.youtube.com/embed/SixdAQtWJQ8?si=TPxjQ04JgcZ5eEA9";
    }
  };

  const closePopup = () => {
    setPopup(false);
    const iframe = document.getElementById("youtube-video");
    if (iframe) {
      iframe.src = "";
    }
  };

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
                  <button
                    className="video-init zubuz-review-video"
                    onClick={openPopup}
                  >
                    <img src="/images/v3/play-btn.svg" alt="" />
                    <div className="waves wave-1"></div>
                    <div className="waves wave-2"></div>
                    <div className="waves wave-3"></div>
                  </button>
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
        <div
          className={`popup-video ${popup ? "popup" : "popdown"}`}
          onClick={() => setPopup(false)}
        >
          <div className="video-wrapper">
            {/* <ReactPlayer
            className="video"
            url="https://www.youtube.com/watch?v=LXb3EKWsInQ"
          ></ReactPlayer> */}
            <iframe
              width="100%"
              height="100%"
              className="video"
              src="https://www.youtube.com/embed/SixdAQtWJQ8?si=iDeCVOUMxo5bqmy9"
              title="YouTube video player"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              referrerpolicy="strict-origin-when-cross-origin"
              allowfullscreen
            ></iframe>
            <button className="close-button">
              <IoClose className="close-icon" />
            </button>
          </div>
        </div>
      </div>
      <div
        className={`popup-video ${popup ? "popup" : "popdown"}`}
        onClick={closePopup}
      >
        <div className="video-wrapper">
          <iframe
            id="youtube-video"
            className="video"
            width="100%"
            height="100%"
            src="https://www.youtube.com/embed/SixdAQtWJQ8?si=TPxjQ04JgcZ5eEA9"
            frameBorder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          ></iframe>

          <button className="close-button" onClick={closePopup}>
            <IoClose className="close-icon" />
          </button>
        </div>
      </div>
    </>
  );
};

export default IntegrationSectionTwo;
