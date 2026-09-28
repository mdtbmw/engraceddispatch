"use client";
import { useState } from "react";
import { auth } from "~/lib/firebase";
import { getApps } from "firebase/app";
import { sendPasswordResetEmail } from "firebase/auth";
import { getFunctions, httpsCallable } from "firebase/functions";

const ResetPasswordForm = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState("");

  const sendViaBrandedEmail = async () => {
    const app = getApps()[0];
    if (!app) return false;
    try {
      const requestReset = httpsCallable(getFunctions(app), "requestPasswordReset");
      await requestReset({ email });
      return true;
    } catch (err) {
      return false;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    const brandedSent = await sendViaBrandedEmail();
    if (brandedSent) {
      setSent(true);
      setLoading(false);
      return;
    }

    try {
      await sendPasswordResetEmail(auth, email);
      setSent(true);
    } catch (err) {
      if (err.code === "auth/user-not-found") {
        setError("No account found with this email.");
      } else if (err.code === "auth/too-many-requests") {
        setError("Too many attempts. Please try again later.");
      } else {
        setError("We could not send the reset email right now. Please try again shortly.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="section zubuz-extra-section">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Reset your delivery portal password</h2>
        </div>
        <div className="zubuz-account-wrap">
          {error && <div style={{ color: "#dc3545", textAlign: "center", marginBottom: "16px" }}>{error}</div>}
          {sent ? (
            <div style={{ textAlign: "center", padding: "40px 0" }}>
              <h3 style={{ color: "#FFC542", marginBottom: "12px" }}>Check your email</h3>
              <p>A password reset link has been sent to <strong>{email}</strong>.</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="zubuz-account-field">
                <label>Email address</label>
                <input type="email" placeholder="example@gmail.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              <button id="zubuz-account-btn" type="submit" disabled={loading}>
                <span>{loading ? "Sending..." : "Send reset link"}</span>
              </button>
              <div className="zubuz-account-bottom m-0">
                <p>
                  If you didn&rsquo;t request a password reset link, please ignore this email.
                </p>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordForm;
