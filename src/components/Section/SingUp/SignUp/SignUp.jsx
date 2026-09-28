"use client";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { auth, db } from "~/lib/firebase";
import { createUserWithEmailAndPassword, GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { useEffect } from "react";
import { normalizePhoneNumber, phoneIndexKey, isValidNigerianPhone } from "~/lib/phoneUtils";

const SignUpForm = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [error, setError] = useState("");
  const [phoneError, setPhoneError] = useState("");

  // Real-time debounced phone uniqueness validation
  useEffect(() => {
    const raw = (phone || "").trim();
    const docKey = phoneIndexKey(raw);
    if (docKey.length >= 10) {
      const timer = setTimeout(async () => {
        try {
          const snap = await getDoc(doc(db, "phone_indices", docKey));
          if (snap.exists()) {
            setPhoneError("This phone number is already registered to another account.");
          } else {
            setPhoneError("");
          }
        } catch (_) {
          setPhoneError("");
        }
      }, 400);
      return () => clearTimeout(timer);
    } else {
      setPhoneError("");
    }
  }, [phone]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!agree) {
      setError("You must agree to the Terms of Service.");
      return;
    }
    const normPhone = normalizePhoneNumber(phone);
    const docKey = phoneIndexKey(phone);
    if (!normPhone || docKey.length < 10) {
      setError("Please enter a valid phone number (at least 10 digits).");
      return;
    }
    if (phoneError) {
      setError(phoneError);
      return;
    }

    setError("");
    setLoading(true);
    try {
      // 1. Authoritative check on phone_indices
      const phoneSnap = await getDoc(doc(db, "phone_indices", docKey));
      if (phoneSnap.exists()) {
        setError("This phone number is already registered to another account.");
        setLoading(false);
        return;
      }

      // 2. Create Authentication credentials
      const cred = await createUserWithEmailAndPassword(auth, email, password);

      // 3. Atomically claim unique phone index
      await setDoc(doc(db, "phone_indices", docKey), {
        uid: cred.user.uid,
        phone: normPhone,
        normalized: docKey,
        createdAt: new Date().toISOString(),
      });

      // 4. Create user profile with canonical normalized phone
      await setDoc(doc(db, "users", cred.user.uid), {
        uid: cred.user.uid,
        name,
        email,
        phone: normPhone,
        role: "customer",
        status: "active",
        isOnline: false,
        rating: 5.0,
        deliveryCount: 0,
        walletBalance: 0,
        loyaltyPoints: 0,
        photoUrl: "",
        createdAt: new Date().toISOString(),
      });
      navigate("/sign-in");
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

  const handleGoogleSignUp = async () => {
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
        navigate("/engdadmin");
      } else {
        navigate("/");
      }
    } catch (err) {
      if (err.code === "auth/popup-closed-by-user") {
        setError("Sign up with Google was cancelled.");
      } else if (err.code === "auth/popup-blocked") {
        setError("Browser pop-up blocked. Please enable pop-ups for this site and try again.");
      } else if (err.code === "auth/account-exists-with-different-credential") {
        setError("An account already exists with this email address using a different sign-in method.");
      } else {
        setError(err.message || "Google sign up failed. Please try again.");
      }
    } finally {
      setGoogleLoading(false);
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
              <label>Phone number</label>
              <input 
                type="tel" 
                placeholder="0803 123 4567" 
                value={phone} 
                onChange={(e) => setPhone(e.target.value)} 
                style={phoneError ? { borderColor: "#dc3545" } : {}}
                required 
              />
              {phoneError && (
                <div style={{ color: "#dc3545", fontSize: "11px", fontWeight: "bold", marginTop: "4px" }}>
                  {phoneError}
                </div>
              )}
            </div>
            <div className="zubuz-account-field">
              <label>Password</label>
              <input type="password" placeholder="Enter Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <div className="zubuz-account-checkbox">
              <input type="checkbox" id="check" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
              <label htmlFor="check">
                I agree to the <Link to="/terms">Terms of Service</Link> and{" "}
                <Link to="/privacy">Privacy Policy</Link>
              </label>
            </div>
            <button id="zubuz-account-btn" type="submit" disabled={loading || googleLoading}>
              <span>{loading ? "Creating account..." : "Create account"}</span>
            </button>
            <div className="zubuz-or">
              <p>or</p>
            </div>
            <button 
              type="button" 
              className="zubuz-connect-login" 
              style={{ width: "100%", border: "none", background: "transparent", cursor: "pointer" }}
              onClick={handleGoogleSignUp}
              disabled={loading || googleLoading}
            >
              <img src="/images/icon/google.svg" alt="" />
              <span>{googleLoading ? "Connecting to Google..." : "Sign up with Google"}</span>
            </button>
            <div className="zubuz-account-bottom">
              <p>
                Already have an account? <Link to="/sign-in">Log in here</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUpForm;
