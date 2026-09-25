import { initializeApp, getApps } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { initializeAppCheck, ReCaptchaV3Provider } from "firebase/app-check";

const getEnv = (key) => {
  if (typeof import.meta !== "undefined" && import.meta.env && import.meta.env[key]) {
    return import.meta.env[key];
  }
  if (typeof process !== "undefined" && process.env && process.env[key]) {
    return process.env[key];
  }
  return "";
};

const firebaseConfig = {
  apiKey: getEnv("VITE_FIREBASE_API_KEY") || getEnv("NEXT_PUBLIC_FIREBASE_API_KEY") || "AIzaSyCxVWlXrlsXSrJvzlxNiQ4lcBcP05E73Ts",
  authDomain: getEnv("VITE_FIREBASE_AUTH_DOMAIN") || getEnv("NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN") || "engraceddispatch-ffba4.firebaseapp.com",
  projectId: getEnv("VITE_FIREBASE_PROJECT_ID") || getEnv("NEXT_PUBLIC_FIREBASE_PROJECT_ID") || "engraceddispatch-ffba4",
  storageBucket: getEnv("VITE_FIREBASE_STORAGE_BUCKET") || getEnv("NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET") || "engraceddispatch-ffba4.firebasestorage.app",
  messagingSenderId: getEnv("VITE_FIREBASE_GCM_SENDER_ID") || getEnv("NEXT_PUBLIC_FIREBASE_GCM_SENDER_ID") || "858437923778",
  appId: getEnv("VITE_FIREBASE_APPLICATION_ID") || getEnv("NEXT_PUBLIC_FIREBASE_APPLICATION_ID") || "1:858437923778:web:b4d9dc83a96c1f78955c5b",
  measurementId: getEnv("NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID") || "G-SXJZWP1BME",
};

const app = !getApps().length ? initializeApp(firebaseConfig) : getApps()[0];
export const auth = getAuth(app);
export const db = getFirestore(app);

// Initialize Firebase App Check for Web (Anti-Scraping / Bot-Blocker)
export let appCheck = null;
if (typeof window !== "undefined") {
  const recaptchaKey = getEnv("NEXT_PUBLIC_RECAPTCHA_SITE_KEY");
  if (recaptchaKey) {
    try {
      if (typeof process !== "undefined" && process.env && process.env.NODE_ENV === "development") {
        // @ts-ignore
        self.FIREBASE_APPCHECK_DEBUG_TOKEN = true;
      }
      appCheck = initializeAppCheck(app, {
        provider: new ReCaptchaV3Provider(recaptchaKey),
        isTokenAutoRefreshEnabled: true
      });
    } catch (e) {
      console.warn("[AppCheck] Web App Check initialization skipped/failed:", e);
    }
  }
}

export function getSecondaryAuth() {
  const secondaryApp = getApps().find(a => a.name === "Secondary") || initializeApp(firebaseConfig, "Secondary");
  return getAuth(secondaryApp);
}

export { firebaseConfig };
