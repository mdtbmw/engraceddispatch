"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { auth, db } from "~/lib/firebase";
import { signInWithEmailAndPassword } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";

const SignInForm = () => {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const snap = await getDoc(doc(db, "users", cred.user.uid));
      const role = snap.exists() ? snap.data().role : null;
      if (role === "admin" || role === "super_admin") {
        document.cookie = "admin_auth=true;path=/;max-age=86400;SameSite=Lax";
        router.push("/engdadmin");
      } else {
        router.push("/");
      }
    } catch (err) {
      if (err.code === "auth/user-not-found" || err.code === "auth/wrong-password" || err.code === "auth/invalid-credential") {
        setError("Invalid email or password.");
      } else if (err.code === "auth/too-many-requests") {
        setError("Too many attempts. Please try again later.");
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
          <h2>Welcome back to your delivery portal</h2>
        </div>
        <div className="zubuz-account-wrap">
          {error && <div className="zubuz-form-error" style={{ color: "#dc3545", textAlign: "center", marginBottom: "16px" }}>{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="zubuz-account-field">
              <label>Email address</label>
              <input type="email" placeholder="example@gmail.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="zubuz-account-field">
              <label>Password</label>
              <input type="password" placeholder="Enter Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <div className="zubuz-account-checkbox-wrap">
              <div className="zubuz-account-checkbox">
                <input type="checkbox" id="check" />
                <label htmlFor="check">Remember me</label>
              </div>
              <Link className="forgot-password" href="reset-password">
                Forgot password?
              </Link>
            </div>
            <button id="zubuz-account-btn" type="submit" disabled={loading}>
              <span>{loading ? "Signing in..." : "Sign in"}</span>
            </button>
            <div className="zubuz-or">
              <p>or</p>
            </div>
            <Link href="#" className="zubuz-connect-login">
              <img src="/images/icon/google.svg" alt="" />
              Sign in with Google
            </Link>
            <Link href="#" className="zubuz-connect-login">
              <img src="/images/icon/facebook.svg" alt="" />
              Sign in with Facebook
            </Link>
            <div className="zubuz-account-bottom">
              <p>
                Don&apos;t have an account? <Link href="sign-up">Sign up here</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignInForm;
