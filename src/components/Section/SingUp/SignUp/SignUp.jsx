"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { auth, db } from "~/lib/firebase";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { doc, setDoc } from "firebase/firestore";

const SignUpForm = () => {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!agree) {
      setError("You must agree to the Terms of Service.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const cred = await createUserWithEmailAndPassword(auth, email, password);
      await setDoc(doc(db, "users", cred.user.uid), {
        uid: cred.user.uid,
        name,
        email,
        phone: "",
        role: "customer",
        status: "active",
        isOnline: false,
        rating: 0,
        deliveryCount: 0,
        walletBalance: 0,
        loyaltyPoints: 0,
        photoUrl: "",
        createdAt: new Date().toISOString(),
      });
      router.push("/sign-in");
    } catch (err) {
      if (err.code === "auth/email-already-in-use") {
        setError("An account with this email already exists.");
      } else if (err.code === "auth/weak-password") {
        setError("Password should be at least 6 characters.");
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="section zubuz-extra-section">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Create your delivery account</h2>
        </div>
        <div className="zubuz-account-wrap">
          {error && <div className="zubuz-form-error" style={{ color: "#dc3545", textAlign: "center", marginBottom: "16px" }}>{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="zubuz-account-field">
              <label>Full name</label>
              <input type="text" placeholder="Adam Smith" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="zubuz-account-field">
              <label>Email address</label>
              <input type="email" placeholder="example@gmail.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="zubuz-account-field">
              <label>Password</label>
              <input type="password" placeholder="Enter Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <div className="zubuz-account-checkbox">
              <input type="checkbox" id="check" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
              <label htmlFor="check">I agree to the Terms of Service and Privacy Policy</label>
            </div>
            <button id="zubuz-account-btn" type="submit" disabled={loading}>
              <span>{loading ? "Creating account..." : "Create account"}</span>
            </button>
            <div className="zubuz-or">
              <p>or</p>
            </div>
            <Link href="#" className="zubuz-connect-login">
              <img src="/images/icon/google.svg" alt="" />
              Sign up with Google
            </Link>
            <Link href="#" className="zubuz-connect-login">
              <img src="/images/icon/facebook.svg" alt="" />
              Sign up with Facebook
            </Link>
            <div className="zubuz-account-bottom">
              <p>
                Already have an account? <Link href="sign-in">Log in here</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUpForm;
