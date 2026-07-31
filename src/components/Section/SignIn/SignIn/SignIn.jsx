"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { auth, db } from "~/lib/firebase";
import { signInWithEmailAndPassword, GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import { doc, getDoc, setDoc } from "firebase/firestore";

const SignInForm = () => {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [googleLoading, setGoogleLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const snap = await getDoc(doc(db, "users", cred.user.uid));
      const role = snap.exists() ? snap.data().role : null;
      if (role === "admin" || role === "super_admin" || role === "dispatcher") {
        const token = await cred.user.getIdToken();
        document.cookie = `admin_token=${token};path=/;max-age=86400;SameSite=Strict;Secure`;
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
        setError("Sign in failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setError("");
    setGoogleLoading(true);
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      const result = await signInWithPopup(auth, provider);
      const userRef = doc(db, "users", result.user.uid);
      const snap = await getDoc(userRef);

      let userRole = "customer";

      if (!snap.exists()) {
        const newUserProfile = {
          uid: result.user.uid,
          name: result.user.displayName || "Customer",
          email: result.user.email || "",
          phone: result.user.phoneNumber || "",
          role: "customer",
          status: "active",
          isOnline: false,
          rating: 5.0,
          deliveryCount: 0,
          walletBalance: 0,
          loyaltyPoints: 0,
          photoUrl: result.user.photoURL || "",
          createdAt: new Date().toISOString(),
        };
        await setDoc(userRef, newUserProfile);
      } else {
        userRole = snap.data().role || "customer";
      }

      if (userRole === "admin" || userRole === "super_admin" || userRole === "dispatcher") {
        const token = await result.user.getIdToken();
        document.cookie = `admin_token=${token};path=/;max-age=86400;SameSite=Strict;Secure`;
        router.push("/engdadmin");
      } else {
        router.push("/");
      }
    } catch (err) {
      if (err.code === "auth/popup-closed-by-user") {
        setError("Sign in with Google was cancelled.");
      } else if (err.code === "auth/popup-blocked") {
        setError("Browser pop-up blocked. Please enable pop-ups for this site and try again.");
      } else if (err.code === "auth/account-exists-with-different-credential") {
        setError("An account already exists with this email address using a different sign-in method.");
      } else {
        setError(err.message || "Google sign in failed. Please try again.");
      }
    } finally {
      setGoogleLoading(false);
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
              <Link className="forgot-password" href="/reset-password">
                Forgot password?
              </Link>
            </div>
            <button id="zubuz-account-btn" type="submit" disabled={loading || googleLoading}>
              <span>{loading ? "Signing in..." : "Sign in"}</span>
            </button>
            <div className="zubuz-or">
              <p>or</p>
            </div>
            <button 
              type="button" 
              className="zubuz-connect-login" 
              style={{ width: "100%", border: "none", background: "transparent", cursor: "pointer" }}
              onClick={handleGoogleSignIn}
              disabled={loading || googleLoading}
            >
              <img src="/images/icon/google.svg" alt="" />
              <span>{googleLoading ? "Connecting to Google..." : "Sign in with Google"}</span>
            </button>
            <Link href="#" className="zubuz-connect-login">
              <img src="/images/icon/facebook.svg" alt="" />
              Sign in with Facebook
            </Link>
            <div className="zubuz-account-bottom">
              <p>
                Don&apos;t have an account? <Link href="/sign-up">Sign up here</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignInForm;
