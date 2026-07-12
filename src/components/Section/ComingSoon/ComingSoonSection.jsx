"use client";
import { useEffect, useState } from "react";
import { FaFacebookF, FaGithub, FaInstagram, FaLinkedin, FaTwitter } from "react-icons/fa";
import { db } from "~/lib/firebase";
import { collection, addDoc, serverTimestamp } from "firebase/firestore";

const ComingSoonSection = () => {
  const [partyTime, setPartyTime] = useState(false);
  const [days, setDays] = useState(0);
  const [hours, setHours] = useState(0);
  const [minutes, setMinutes] = useState(0);
  const [seconds, setSeconds] = useState(0);
  const [email, setEmail] = useState("");
  const [subscribed, setSubscribed] = useState(false);
  const [subLoading, setSubLoading] = useState(false);

  useEffect(() => {
    const target = new Date("12/31/2026 23:59:59");
    const interval = setInterval(() => {
      const now = new Date();
      const difference = target.getTime() - now.getTime();
      setDays(Math.floor(difference / (1000 * 60 * 60 * 24)));
      setHours(Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
      setMinutes(Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60)));
      setSeconds(Math.floor((difference % (1000 * 60)) / 1000));
      if (difference <= 0) setPartyTime(true);
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleSubscribe = async (e) => {
    e.preventDefault();
    setSubLoading(true);
    try {
      await addDoc(collection(db, "subscribers"), {
        email,
        createdAt: serverTimestamp(),
        source: "coming-soon",
      });
      setSubscribed(true);
      setEmail("");
    } catch {
      // silent fail
    } finally {
      setSubLoading(false);
    }
  };

  return (
    <>
      <div className="section zubuz-extra-section">
        <div className="container">
          <div className="zubuz-section-title center">
            <h2>Engraced Dispatch is launching soon!</h2>
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
          {subscribed ? (
            <div style={{ textAlign: "center", padding: "20px" }}>
              <h4 style={{ color: "#FFC542" }}>You&apos;re on the list!</h4>
              <p>We&apos;ll notify you when we launch.</p>
            </div>
          ) : (
            <form onSubmit={handleSubscribe}>
              <div className="zubuz-coming-newsletter">
                <input type="email" placeholder="Enter your email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                <button type="submit" id="zubuz-notified-btn" disabled={subLoading}>
                  {subLoading ? "Subscribing..." : "Get Notified"}
                </button>
                <p>Be the first to know when we launch. No spam, just deliveries.</p>
              </div>
            </form>
          )}
        </div>
      </div>
      <footer className="zubuz-footer-section main-footer">
        <div className="container">
          <div className="zubuz-footer-bottom border-0">
            <div className="zubuz-social-icon order-md-2">
              <ul>
                <li><a href="https://twitter.com/" target="_blank"><FaTwitter /></a></li>
                <li><a href="https://facebook.com/" target="_blank"><FaFacebookF /></a></li>
                <li><a href="https://www.instagram.com/" target="_blank"><FaInstagram /></a></li>
                <li><a href="https://www.linkedin.com/" target="_blank"><FaLinkedin /></a></li>
                <li><a href="https://github.com/" target="_blank"><FaGithub /></a></li>
              </ul>
            </div>
            <div className="zubuz-copywright">
              <p>&copy;Copyright 2024, Engraced Dispatch. All rights reserved.</p>
            </div>
          </div>
        </div>
      </footer>
    </>
  );
};

export default ComingSoonSection;
