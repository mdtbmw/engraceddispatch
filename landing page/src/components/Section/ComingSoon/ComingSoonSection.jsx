"use client";

import { useEffect, useState } from "react";
import { FaFacebookF, FaGithub, FaInstagram, FaLinkedin, FaTwitter } from "react-icons/fa";

const ComingSoonSection = () => {
  const [partyTime, setPartyTime] = useState(false);
  const [days, setDays] = useState(0);
  const [hours, setHours] = useState(0);
  const [minutes, setMinutes] = useState(0);
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const target = new Date("12/31/2026 23:59:59");

    const interval = setInterval(() => {
      const now = new Date();
      const difference = target.getTime() - now.getTime();

      const d = Math.floor(difference / (1000 * 60 * 60 * 24));
      setDays(d);

      const h = Math.floor(
        (difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
      );
      setHours(h);

      const m = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
      setMinutes(m);

      const s = Math.floor((difference % (1000 * 60)) / 1000);
      setSeconds(s);

      if (d <= 0 && h <= 0 && m <= 0 && s <= 0) {
        setPartyTime(true);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  return (
    <>
      <div className="section zubuz-extra-section">
        <div className="container">
          <div className="zubuz-section-title center">
            <h2>Dispatch is launching soon!</h2>
          </div>
          <div className="zubuz-countdown-wrap">
            <div className="zubuz-countdown-item">
              <div className="countdown-days">
                <div className="number">{days}</div>
              </div>
              <p>Days</p>
            </div>
            <div className="zubuz-countdown-item">
              <div className="countdown-hours">
                <div className="number">{hours}</div>
              </div>
              <p>Hours</p>
            </div>
            <div className="zubuz-countdown-item">
              <div className="countdown-minutes">
                <div className="number">{minutes}</div>
              </div>
              <p>Minutes</p>
            </div>
            <div className="zubuz-countdown-item">
              <div className="countdown-seconds">
                <div className="number">{seconds}</div>
              </div>
              <p>Seconds</p>
            </div>
          </div>
          <form action="#">
            <div className="zubuz-coming-newsletter">
              <input type="email" placeholder="Enter your email" />
              <button type="submit" id="zubuz-notified-btn">
                Get Notified
              </button>
              <p>
                Be the first to know when we launch. No spam, just deliveries.
              </p>
            </div>
          </form>
        </div>
      </div>
      <footer class="zubuz-footer-section main-footer">
        <div class="container">
          <div class="zubuz-footer-bottom border-0">
            <div class="zubuz-social-icon order-md-2">
              <ul>
                <li>
                  <a href="https://twitter.com/" target="_blank">
                    <FaTwitter />
                  </a>
                </li>
                <li>
                  <a href="https://facebook.com/" target="_blank">
                    <FaFacebookF />
                  </a>
                </li>
                <li>
                  <a href="https://www.instagram.com/" target="_blank">
                    <FaInstagram />
                  </a>
                </li>
                <li>
                  <a href="https://www.linkedin.com/" target="_blank">
                    <FaLinkedin />
                  </a>
                </li>
                <li>
                  <a href="https://github.com/" target="_blank">
                    <FaGithub />
                  </a>
                </li>
              </ul>
            </div>
            <div class="zubuz-copywright">
              <p> &copy;Copyright 2024, Dispatch. All rights reserved.</p>
            </div>
          </div>
        </div>
      </footer>
    </>
  );
};

export default ComingSoonSection;
