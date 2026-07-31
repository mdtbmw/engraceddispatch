import { initializeApp, getApps } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY || "AIzaSyCxVWlXrlsXSrJvzlxNiQ4lcBcP05E73Ts",
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN || "engraceddispatch-ffba4.firebaseapp.com",
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "engraceddispatch-ffba4",
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET || "engraceddispatch-ffba4.firebasestorage.app",
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_GCM_SENDER_ID || "858437923778",
  appId: process.env.NEXT_PUBLIC_FIREBASE_APPLICATION_ID || "1:858437923778:web:b4d9dc83a96c1f78955c5b",
  measurementId: "G-SXJZWP1BME",
};

const app = !getApps().length ? initializeApp(firebaseConfig) : getApps()[0];
export const auth = getAuth(app);
export const db = getFirestore(app);
