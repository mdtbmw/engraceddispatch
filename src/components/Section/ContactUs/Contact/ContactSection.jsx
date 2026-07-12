"use client";
import Link from "next/link";
import { useState } from "react";
import { FaTwitter, FaFacebookF, FaLinkedin, FaGithub } from "react-icons/fa";
import { db } from "~/lib/firebase";
import { collection, addDoc, serverTimestamp } from "firebase/firestore";
import { useSiteContent } from "~/lib/useSiteContent";

const ContactSection = () => {
  const { content } = useSiteContent();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [address, setAddress] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await addDoc(collection(db, "contacts"), {
        name,
        email,
        address,
        message,
        createdAt: serverTimestamp(),
        status: "new",
      });
      await addDoc(collection(db, "notifications"), {
        type: "contact",
        title: "New contact form submission",
        body: `${name} (${email}) sent a message`,
        read: false,
        createdAt: serverTimestamp(),
      });
      setDone(true);
      setName(""); setEmail(""); setAddress(""); setMessage("");
    } catch (err) {
      setError("Failed to send. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="section zubuz-section-padding2 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-6">
            <div className="zubuz-default-content m-right">
              <h2>Contact our dispatch team</h2>
              <p>
                Need a delivery or want to partner with us? Reach out to the
                Engraced Dispatch team and we&apos;ll get back to you
                within minutes.
              </p>
              <div className="zubuz-extara-mt">
                <div className="zubuz-iconbox-wrap-left d-block">
                  <div className="zubuz-iconbox-data data-small">
                    <span>Office Location:</span>
                    <p>{content.contactAddress}</p>
                  </div>
                </div>
                <div className="zubuz-iconbox-wrap-left d-block">
                  <div className="zubuz-iconbox-data data-small">
                    <span>Social Media:</span>
                    <div className="zubuz-social-icon social-box">
                      <ul>
                        <li><Link href={content.socialTwitter}><FaTwitter /></Link></li>
                        <li><Link href={content.socialFacebook}><FaFacebookF /></Link></li>
                        <li><Link href={content.socialLinkedin}><FaLinkedin /></Link></li>
                        <li><Link href={content.socialGithub}><FaGithub /></Link></li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-lg-6">
            <div className="zubuz-form-wrap">
              <h3>Send us a message</h3>
              {done ? (
                <div style={{ textAlign: "center", padding: "40px 0" }}>
                  <h4 style={{ color: "#FFC542", marginBottom: "12px" }}>Message sent!</h4>
                  <p>We&apos;ll get back to you within 24 hours.</p>
                  <button className="zubuz-btn" onClick={() => setDone(false)} style={{ marginTop: "16px" }}>Send another</button>
                </div>
              ) : (
                <form onSubmit={handleSubmit}>
                  {error && <p style={{ color: "#dc3545", marginBottom: "12px" }}>{error}</p>}
                  <div className="row">
                    <div className="col-lg-6">
                      <div className="zubuz-main-form">
                        <input type="text" placeholder="Full Name*" value={name} onChange={(e) => setName(e.target.value)} required />
                      </div>
                    </div>
                    <div className="col-lg-6">
                      <div className="zubuz-main-form">
                        <input type="email" placeholder="Email Address*" value={email} onChange={(e) => setEmail(e.target.value)} required />
                      </div>
                    </div>
                  </div>
                  <div className="zubuz-main-form">
                    <input type="text" placeholder="Pickup &amp; Delivery Address" value={address} onChange={(e) => setAddress(e.target.value)} />
                  </div>
                  <div className="zubuz-main-form">
                    <textarea name="textarea" placeholder="Package description (size, weight, special instructions)" value={message} onChange={(e) => setMessage(e.target.value)} required></textarea>
                  </div>
                  <button id="zubuz-submit-btn" type="submit" disabled={loading}>
                    <span>{loading ? "Sending..." : "Request Delivery"}</span>
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactSection;
