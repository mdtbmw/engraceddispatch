"use client";
import { useState } from "react";
import { db } from "~/lib/firebase";
import { collection, addDoc, Timestamp } from "firebase/firestore";

export default function Newsletter() {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState("idle");
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.includes("@")) return;
    setStatus("loading");
    try {
      await addDoc(collection(db, "subscribers"), { email, subscribedAt: Timestamp.now() });
      setStatus("success");
      setEmail("");
    } catch {
      setStatus("error");
    }
  };
  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-lg-6 text-center">
            <h2 className="mb-3" style={{ fontWeight: 800 }}>Stay Updated</h2>
            <p className="mb-4" style={{ opacity: 0.7 }}>Subscribe to our newsletter for delivery tips, promos, and service updates.</p>
            <form onSubmit={handleSubmit} className="d-flex gap-2 justify-content-center" style={{ maxWidth: 440, margin: "0 auto" }}>
              <input
                type="email" value={email} onChange={e => setEmail(e.target.value)} required
                placeholder="Your email address"
                className="form-control"
                style={{ borderRadius: 12, padding: "12px 16px", flex: 1, border: "1px solid rgba(0,0,0,0.1)", fontSize: 14 }}
                disabled={status === "loading"}
              />
              <button
                type="submit" disabled={status === "loading"}
                className="btn text-white"
                style={{ borderRadius: 12, padding: "12px 24px", fontWeight: 700, fontSize: 14, background: "#FFC542", color: "#111 !important", border: "none" }}
              >
                {status === "loading" ? "Subscribing..." : "Subscribe"}
              </button>
            </form>
            {status === "success" && <p className="mt-2" style={{ color: "green", fontSize: 13, fontWeight: 600 }}>Subscribed! Check your inbox.</p>}
            {status === "error" && <p className="mt-2" style={{ color: "red", fontSize: 13, fontWeight: 600 }}>Something went wrong. Try again.</p>}
          </div>
        </div>
      </div>
    </div>
  );
}
