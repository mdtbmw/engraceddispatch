"use client";

import Link from "next/link";
import { useState } from "react";
import { IoClose } from "react-icons/io5";

const SupportSection = () => {
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
    <div className="section zubuz-section-padding">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2 className="rt-mb-20">See how our delivery service works</h2>
          <p>
            Watch our quick demo to see how Dispatch gets your
            package from point A to point B safely and on time.
          </p>
        </div>
        <div className="zubuz-video-wrap m-0">
          <img src="/images/service/video-bg.png" alt="" />
          <button
            className="video-init zubuz-review-video zubuz-video-service"
            onClick={openPopup}
          >
            <img
              className="video-sevice"
              src="/images/service/play-btn.svg"
              alt=""
            />
            <div className="waves waves-2 wave-1"></div>
            <div className="waves waves-2 wave-2"></div>
            <div className="waves waves-2 wave-3"></div>
          </button>
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
    </div>
  );
};

export default SupportSection;
