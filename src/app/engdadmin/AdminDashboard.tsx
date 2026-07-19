"use client";
import React, { useState, useEffect, useCallback } from "react";
import { initializeApp, getApps } from "firebase/app";
import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signOut, onAuthStateChanged } from "firebase/auth";
import { getFirestore, collection, query, onSnapshot, doc, updateDoc, setDoc, deleteDoc, where, Timestamp, getDoc, writeBatch, addDoc, increment } from "firebase/firestore";
import { Shield, Truck, Package, Users, Settings, Activity, Lock, Mail, Key, CheckCircle, AlertTriangle, Plus, Trash2, LogOut, Search, Sliders, Award, DollarSign, Zap, Globe, UserPlus, BarChart3, MapPin, ShieldAlert, Image as ImageIcon, Menu, X, ShieldCheck, RefreshCw, UserCheck, UserX, Clock, TrendingUp, Edit3, Copy, Check, Percent, Gift, Star, Layers, Eye, EyeOff, Calendar, ChevronDown, ChevronUp, Phone, AtSign, Hash, Save, Bell, Send, ChevronLeft, ChevronRight, Bookmark, Folder, FileCheck, MessageSquare, Headphones, Settings2, LayoutGrid, FileText, Moon, Sun } from "lucide-react";
import CMSTab from "./CMSTab";
const projectId = process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "engraceddispatch-ffba4";
const firebaseConfig = { apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY || "AIzaSyDa7J-JOfQIW4ZZo59jjEBiLUSRyvdK6uY", authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN || `${projectId}.firebaseapp.com`, projectId, storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET || `${projectId}.appspot.com`, messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_GCM_SENDER_ID || "858437923778", appId: process.env.NEXT_PUBLIC_FIREBASE_APPLICATION_ID || "1:858437923778:android:2d29558caf1a2f15955c5b" };
const app = !getApps().length ? initializeApp(firebaseConfig) : getApps()[0];
const auth = getAuth(app);
const db = getFirestore(app);
type TabId = "dashboard" | "users" | "shipments" | "banners" | "referrals" | "promotions" | "appcards" | "settings" | "logs" | "cms" | "tracking";
interface UserProfile { id: string; uid: string; name: string; email: string; phone: string; role: string; status: string; isOnline: boolean; rating: number; deliveryCount: number; walletBalance: number; loyaltyPoints: number; photoUrl: string; bikeNumber?: string; lat?: number; lng?: number; isDeleted?: boolean; updatedAt?: any; }
interface Delivery { id: string; status: string; category?: string; receiverName: string; deliveryAddress: string; senderName: string; senderPhone: string; receiverPhone: string; price: number; riderId: string; courierName: string; courierPhone: string; itemName: string; pickupAddress: string; quantity: number; weight: number; dateString: string; tipAmount: number; userId: string; otpCode: string; }
interface Banner { id: string; title: string; subtitle: string; imageUrl: string; interval: number; order: number; active: boolean; }
interface Referral { id: string; referrerId: string; referrerName: string; referrerEmail: string; refereeId: string; refereeName: string; refereeEmail: string; rewardAmount: number; status: string; }
interface Promotion { id: string; title: string; description: string; discountType: string; discountValue: number; discountDisplay: string; minOrderAmount: number; maxDiscount: number; code: string; usageLimit: number; usedCount: number; active: boolean; }
interface AuditEntry { id: string; time: string; action: string; details: string; admin: string; timestamp: number; }
interface AppContent { id: string; key: string; title: string; description: string; imageUrl: string; ctaText: string; ctaLink: string; order: number; active: boolean; }
interface Toast { id: number; type: "success" | "error" | "info"; message: string; }
interface BannersTabProps { banners: Banner[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface ReferralsTabProps { referrals: Referral[]; completedReferrals: Referral[]; searchQuery: string; }
interface PromotionsTabProps { promotions: Promotion[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface AppCardsTabProps { appContent: AppContent[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface SettingsTabProps { db: any; addLog: (a: string, d: string) => Promise<void> | void; }
interface LogsTabProps { logs: AuditEntry[]; }

function EdLogoSvg({ size = 36, className = "", dark = false }: { size?: number; className?: string; dark?: boolean }) {
  const s = size;
  const c1 = dark ? "#1a1a1a" : "#FFC542";
  const c2 = dark ? "#1a1a1a" : "#FFFFFF";
  return <svg width={s} height={Math.round(s * 1.625)} viewBox="0 0 15.39 25.2" className={className} aria-label="ED">
    <path fill={c1} d="M2.69 17.18c0.97,-0.14 4.71,-1.44 5.83,-1.66l0 0.83c-0.3,0.15 -0.95,0.28 -1.32,0.39 -0.44,0.13 -0.88,0.26 -1.32,0.39 -0.43,0.13 -0.9,0.25 -1.33,0.38 -0.56,0.17 -1,0.09 -0.81,0.73 0.42,-0.01 1.91,-0.52 2.39,-0.66 0.59,-0.18 1.83,-0.62 2.37,-0.68l-0.02 0.9 -4.7 1.34c0.02,0.16 0.17,0.34 0.24,0.48 0.41,-0.02 1.75,-0.49 2.22,-0.62 0.67,-0.19 1.61,-0.55 2.26,-0.65 -0.02,1.15 0.33,0.83 -1.99,1.48l-1.99 0.56 0.25 0.49c0.47,-0.05 1.9,-0.65 2.22,-0.58l-0 0.57c-0.03,0.01 -0.04,0.01 -0.05,0.02 -0.05,0.15 -1.47,0.32 -1.73,0.59 0.05,0.17 0.09,0.2 0.2,0.29 0.36,-0.05 1.3,-0.41 1.58,-0.36l-0 0.62c-0.32,0.18 -0.86,0.2 -1.17,0.36 0.03,0.18 0.12,0.27 0.2,0.39 0.32,-0.06 0.58,-0.22 0.95,-0.23l0.03 0.69 -0.54 0.15c0.04,0.16 1.12,1.69 1.31,1.82 0.19,-0.11 0.35,-0.45 0.47,-0.64 0.32,-0.48 1.19,-1.65 1.33,-2.08 -0.61,0.03 -1.57,0.48 -2.21,0.6 -0.01,-0.83 -0.15,-0.65 0.76,-0.9l1.68 -0.48c0.4,-0.1 0.44,-0.2 0.57,-0.51 -0.44,-0.02 -2.53,0.81 -2.99,0.75 -0.03,-0.24 -0.01,-1.62 0.06,-1.74 0.09,-0.13 0.03,-0.1 0.26,-0.16 0.11,-0.03 0.2,-0.05 0.32,-0.08l1.87 -0.53c0.28,-0.08 1.66,-0.42 1.8,-0.61 0.07,-0.1 0.23,-0.43 0.27,-0.55l-2.99 0.83 0.01 -0.93c0.61,-0.1 1.41,-0.39 2.01,-0.55 0.33,-0.09 0.64,-0.18 0.98,-0.28 0.44,-0.12 0.64,-0.1 0.69,-0.62 -0.79,0.16 -3.15,0.95 -3.66,0.99l-0.02 -1.79c0.01,-0.08 0.04,-0.45 0.1,-0.51 0.09,-0.1 3.72,-1.11 4.34,-1.28 0.52,-0.14 0.43,0.03 0.83,-1.07 0.31,-0.85 0.64,-2 0.81,-2.95 0.11,-0.57 0.51,-3.06 0.26,-3.48 -0.04,-0.03 -0.68,-0.04 -0.76,0.48 -0.19,1.23 -0.05,1.81 -0.76,3.14 -2.02,3.79 -7.24,4.58 -10.28,1.31 -2.83,-3.04 -2.02,-8.22 2.32,-10.07 2.48,-1.06 4.28,-0.09 4.55,-0.22 0.61,-0.67 -0.05,-0.8 -0.6,-0.95 -3.65,-1.03 -7.78,0.58 -9.23,4.17 -0.89,2.22 -0.57,4.54 -0.01,6.89 0.25,1.06 0.58,2.04 0.94,3.01 0.17,0.47 0.39,0.97 0.58,1.43 0.2,0.46 0.45,0.94 0.63,1.37z" />
    <path fill={c1} d="M11.26 5.05c0.03,0.05 0.02,0.02 0.06,0.05 0.07,0.06 0.12,0.12 0.17,0.2 0.06,0.12 0.08,0.28 0.04,0.42 -0.09,0.38 -0.4,0.42 -0.46,0.49l0.39 1.18c0.12,0.36 0.31,0.86 0.32,1.27 0.03,0.83 -0.56,1.58 -1.33,1.83 -0.29,0.1 -1.15,0.1 -1.48,0.05 -0.48,-0.06 -0.78,-0.32 -0.97,-0.62 -0.4,-0.66 -0.07,-1.53 0.64,-1.79 0.1,-0.04 0.22,-0.05 0.31,-0.08 -0.01,-0.06 -0.14,-0.27 -0.18,-0.34 -0.11,-0.19 -0.09,-0.2 -0.36,-0.19 -0.36,0 -0.72,0.01 -1.07,0.01 -0.56,0 -0.86,0.02 -1.24,-0.36 -0.39,-0.39 0.1,-0.31 -0.98,-0.31 -0.22,0 -0.51,-0.04 -0.64,0.09 -0.14,0.14 -0.12,0.4 0.01,0.54 0.09,0.1 0.29,0.23 0.42,0.32 -0.03,0.02 -0.39,0.16 -0.47,0.19 -0.79,0.39 -1.29,0.98 -1.65,1.76 -0.09,0.2 -0.33,0.82 -0.32,1.06 1.03,-0.01 2.08,-0 3.11,-0 0.52,-0 1.02,0.08 1.32,-0.23 0.79,-0.83 -0.5,-2.42 -2.11,-1.79 -0.53,0.21 -0.97,0.63 -1.21,1.12 -0.1,0.19 -0.15,0.36 -0.21,0.58l-0.31 0c0.03,-0.36 0.37,-0.94 0.53,-1.17 0.12,-0.17 0.27,-0.3 0.42,-0.43 0.49,-0.41 1.27,-0.66 1.95,-0.53 0.9,0.18 1.66,0.99 1.45,1.9 -0.1,0.43 -0.42,0.75 -0.86,0.84 -0.41,0.08 -1.21,0.04 -1.66,0.04 -0.57,0 -1.15,-0.01 -1.72,-0 0,0.51 0.15,0.93 0.45,1.29l0.23 0.22c0.04,0.03 0.05,0.02 0.07,0.07 -0.53,-0.01 0.12,1.42 -0.14,1.52 0.72,0.14 6.76,-1.17 7.79,-1.17 0.44,0 1.09,0.08 1.3,-0.01l-1.54 0.37c0.09,-0.11 1.07,-0.09 1.36,-0.84 0.19,-0.5 -0.59,1.23 -0.87,0.87 -0.58,0.01 2.06,-2.85 1.63,-2.62 -0.28,0.15 -0.46,0.29 -0.67,0.45 -0.08,0.06 -0.53,0.51 -0.55,0.58 -0.02,0.11 0.31,0.59 0.47,0.72l0.23 0.18c0,0 0.01,0.01 0.01,0.01 -1.18,-0.01 -2.96,0.85 -4.15,0.84 -1.05,-0.01 -2.35,0.43 -3.41,0.47 -0.07,0 0.5,-1.39 0.44,-1.39 0.31,-0.27 0.64,-0.54 0.71,-1.04l-0.8 0.01c-0.59,1 -1.73,0.41 -1.73,-0.28 0.72,-0.01 0.37,-0.04 0.62,0.2 0.06,0.06 0.15,0.1 0.26,0.11 0.27,0.01 0.34,-0.16 0.41,-0.31 0.43,-0.01 0.84,-0.02 1.27,-0.05 0.82,-0.06 1.78,-0.04 2.61,-0.04 0.42,0 0.93,0.03 1.34,-0.01 0.39,-0.04 0.68,-0.25 0.89,-0.42 0.32,-0.26 0.71,-1.12 0.96,-1.37 0.19,-0.18 0.32,-0.31 0.56,-0.45 0.16,-0.09 0.32,-0.13 0.47,-0.2 0.01,-0.3 0.01,-0.79 -0.01,-0.98 -0.03,-0.26 -0.03,-0.53 -0.06,-0.79 -0.05,-0.41 -0.11,-0.97 -0.35,-1.27 -0.21,-0.27 -0.67,-0.32 -1.07,-0.31 -0.07,0.56 0.05,1.11 0.12,1.64z" />
    <path fill={c1} d="M6.11 5.95c-0.01,0.19 -0.18,0.48 0.11,0.9 0.23,0.32 0.48,0.33 0.8,0.34 0.37,0 0.75,-0.01 1.12,-0.01 0.35,-0 0.82,-0.04 1.16,-0.01l0.48 2.36c0.08,-0.01 0.62,-0.25 0.67,-0.28 0.02,-0.15 -0.02,-0.52 -0.03,-0.69 -0.01,-0.23 -0.02,-0.46 -0.03,-0.69 -0.02,-0.36 -0,-1.05 -0.15,-1.32 -0.18,-0.32 -0.69,-0.31 -1.12,-0.35 -0.44,-0.04 -0.9,-0.1 -1.34,-0.13l0.27 -0.7c0.03,-0.09 0.04,-0.08 -0.01,-0.15 -0.09,-0.13 -0.21,-0.35 -0.29,-0.45 -0.06,0.01 -1.12,0.61 -1.3,0.72 -0.15,0.09 -0.41,-0.03 -0.59,-0.05l0.75 -1.89 0.31 0.07c-0.01,0.11 -0.25,0.67 -0.31,0.81 -0.1,0.24 -0.25,0.55 -0.33,0.79 0.11,-0.03 1.37,-0.77 1.55,-0.84 0.08,0.08 0.42,0.64 0.51,0.78 0.22,0.35 0.27,0.55 0.8,0.55l1.15 -0.01c0.03,-0.12 0.01,-0.19 0.06,-0.31 0.04,-0.1 0.09,-0.16 0.13,-0.25 -0.4,-0.05 -0.86,-0.12 -1.26,-0.19 -0.11,-0.02 -0.08,-0.03 -0.16,-0.21 -0.04,-0.09 -0.07,-0.17 -0.11,-0.25 -0.08,-0.17 -0.15,-0.35 -0.22,-0.52 -0.2,-0.46 -0.4,-1.23 -1.12,-1.02 -0.1,0.03 -0.18,0.07 -0.25,0.12 -0.08,0.05 -0.13,0.13 -0.21,0.17l-0.36 -0.1c0.05,-0.51 -0.31,-0.52 -0.76,-0.68 -0.19,-0.07 -0.36,-0.13 -0.55,-0.2 -0.37,-0.13 -0.83,-0.39 -1.11,-0.08 -0.1,0.11 -0.6,1.39 -0.66,1.57 -0.64,1.69 -0.96,1.44 1.28,2.23 0.75,0.27 0.54,-0.16 0.83,-0.08 0.1,0.03 0.2,0.05 0.3,0.08z" />
    <path fill={c1} d="M12.62 12.57c0.2,-0.22 0.89,-0.93 1.14,-1.1 0.81,-0.53 -0.43,-1.07 0.64,-1.1 -0.02,-0.53 0.95,-0.88 0.09,-1.19 -0.53,-0.19 -1.04,-0.1 -1.48,0.14 -1.48,0.82 -1.54,2.14 -0.4,3.25z" />
    <path fill={c1} d="M7.73 2.32c0.81,0.02 1.03,0.5 1.2,0.65 0.09,0.08 0.25,0.18 0.43,0.19 0.39,0.04 0.66,-0.22 0.8,-0.44 0.21,-0.35 0.18,-0.63 0.18,-1.08 -0.19,-0.01 -1.06,0.14 -1.18,0.21 -0.03,0.15 0.01,0.39 0.01,0.55 -0.25,-0.08 -0.52,-0.33 -0.37,-0.68 0.12,-0.28 0.44,-0.31 0.77,-0.36 0.33,-0.04 0.66,-0.08 1,-0.13 0.38,-0.05 0.68,-0.05 0.77,-0.35l-1.07 -0.01c-0.2,-0.24 -0.32,-0.45 -0.74,-0.6 -0.73,-0.27 -1.53,0.13 -1.77,0.81 -0.13,0.37 -0.09,0.81 -0.03,1.22z" />
    <path fill={c1} d="M3.67 13.45l0.13 0.03c1.16,0.07 2.85,0.17 3.91,0.17 0.66,-0 1.38,-0.04 2.03,-0.07 0.68,-0.03 1.35,-0.08 2.02,-0.11 -0.01,-0.01 0,-0 -0.04,-0.02l-0.44 -0.02c-0.16,-0 -0.33,-0 -0.5,-0.01l-3.06 -0.02c-1.35,0 -2.71,-0 -4.06,0.04z" />
    <path fill={c1} d="M9.83 9.81l0.11 0.51c0.14,-0.01 0.7,-0.28 0.81,-0.35 0.19,-0.12 0.48,-0.4 0.52,-0.62 -0.1,-0 -0.25,0.08 -0.36,0.12 -0.18,0.06 -0.26,0.03 -0.41,0.06 -0.05,0.01 -0.26,0.12 -0.33,0.15 -0.11,0.05 -0.23,0.09 -0.35,0.13z" />
    <path fill={c2} d="M4.49 2.57c0.14,0.09 1.42,0.53 1.64,0.59l0.11 -0.23c-0.22,-0.06 -1.53,-0.57 -1.64,-0.58l-0.1 0.21z" />
    <path fill={c1} d="M10.85 5.24c-0.43,0.11 -0.29,0.76 0.14,0.66 0.43,-0.1 0.28,-0.76 -0.14,-0.66z" />
    <path fill={c2} d="M12.26 5.84c0.04,-0.05 0.04,-0.13 0.04,-0.19 0.01,-0.08 0.01,-0.16 0.01,-0.24 0,-0.13 -0.01,-0.33 -0.05,-0.44 -0.07,0.08 -0.12,0.29 -0.12,0.42 -0,0.19 0.04,0.3 0.11,0.45z" />
  </svg>;
}

function fmt(n: number): string { return "₦" + n.toLocaleString("en-US"); }
function idShort(id: string): string { return id.length > 8 ? id.slice(-8) : id; }
function rBadge(role: string): string {
  switch (role) {
    case "rider": return "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300";
    case "admin": return "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300";
    case "super_admin": return "bg-[#FFC542]/20 text-[#111] dark:text-white";
    default: return "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300";
  }
}
function sStyle(s: string): string {
  const m: Record<string, string> = {
    PENDING: "bg-amber-50 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
    ASSIGNED: "bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
    TRANSIT: "bg-purple-50 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300",
    OUT_FOR_DELIVERY: "bg-cyan-50 text-cyan-700 dark:bg-cyan-900/30 dark:text-cyan-300",
    DELIVERED: "bg-green-50 text-green-700 dark:bg-green-900/30 dark:text-green-300",
    CANCELLED: "bg-red-50 text-red-700 dark:bg-red-900/30 dark:text-red-300",
  };
  return m[s] || "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300";
}
const statusSteps = ["PENDING", "ASSIGNED", "TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"];
const sIdx: Record<string, number> = { PENDING: 0, ASSIGNED: 1, TRANSIT: 2, OUT_FOR_DELIVERY: 3, DELIVERED: 4, CANCELLED: -1 };

function StatCard({ icon, label, value, sub }: { icon: React.ReactNode; label: string; value: string; sub: string }) {
  return <div className="animate-fade-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm hover:shadow-md hover:scale-[1.02] transition-all duration-300">
    <div className="flex items-center justify-between"><span className="text-xs font-bold text-black/40 dark:text-white/40">{label}</span>{icon}</div>
    <h2 className="text-3xl font-black text-[#111] dark:text-white mt-2">{value}</h2>
    <p className="text-[10px] text-[#FFC542] font-bold mt-1">{sub}</p>
  </div>;
}
function QuickBtn({ label, desc, onClick, loading = false }: { label: string; desc: string; onClick: () => void; loading?: boolean }) {
  return <button onClick={onClick} disabled={loading} className={"p-4 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 hover:border-[#FFC542]/50 hover:bg-[#FFC542]/10 rounded-2xl text-left transition-all " + (loading ? "opacity-50 cursor-not-allowed" : "")}>
    <p className="text-xs font-bold text-[#FFC542]">{loading ? "SEEDING..." : label}</p><p className="text-[10px] text-black/40 dark:text-white/40 mt-1">{desc}</p>
  </button>;
}
function Section({ title, children, className }: { title: string; children: React.ReactNode; className?: string }) {
  return <div className={"bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm " + (className || "")}>
    <h3 className="text-xs font-bold text-[#FFC542] tracking-wider uppercase flex items-center gap-2">{title}</h3>
    <div className="mt-4">{children}</div>
  </div>;
}
function InlineEdit({ value, onSave, type = "text" }: { value: string; onSave: (v: string) => void; type?: string }) {
  const [editing, setEditing] = useState(false);
  const [val, setVal] = useState(value);
  useEffect(() => setVal(value), [value]);
  if (!editing) return <span onClick={() => setEditing(true)} className="cursor-pointer hover:bg-[#FFC542]/10 px-1.5 py-0.5 rounded group inline-flex items-center gap-1.5 -ml-1.5 transition-colors text-[#111] dark:text-white">{value || "—"} <Edit3 className="w-3 h-3 text-[#FFC542]/0 group-hover:text-[#FFC542]" /></span>;
  return <input type={type} value={val} onChange={e => setVal(e.target.value)} onBlur={() => { onSave(val); setEditing(false); }} onKeyDown={e => { if (e.key === "Enter") { onSave(val); setEditing(false); } if (e.key === "Escape") { setVal(value); setEditing(false); }}} className="bg-white dark:bg-[#222] border border-[#FFC542]/50 rounded-xl px-2 py-1 text-sm text-[#111] dark:text-white w-full shadow-sm" autoFocus />;
}
function ConfirmModal({ show, title, message, confirmLabel, onConfirm, onCancel }: { show: boolean; title: string; message: string; confirmLabel?: string; onConfirm: () => void; onCancel: () => void }) {
  if (!show) return null;
  return <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50">
    <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4">
      <h3 className="text-lg font-black text-[#111] dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-red-500" /> {title}</h3>
      <p className="text-sm text-black/60 dark:text-white/60">{message}</p>
      <div className="flex items-center justify-end gap-3 pt-2">
        <button onClick={onCancel} className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">Cancel</button>
        <button onClick={onConfirm} className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-black transition-colors">{confirmLabel || "Confirm"}</button>
      </div>
    </div>
  </div>;
}
function SearchInput({ value, onChange, placeholder = "Search..." }: { value: string; onChange: (v: string) => void; placeholder?: string }) {
  return <div className="relative">
    <Search className="absolute left-3 top-2.5 w-4 h-4 text-black/40 dark:text-white/40" />
    <input type="text" value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}
      className="pl-10 pr-4 py-2 bg-white dark:bg-[#222] border border-black/20 dark:border-white/20 rounded-xl text-sm text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40 focus:border-[#FFC542] w-full transition-all" />
  </div>;
}
function SaveBtn({ onClick, label = "Save", loading = false, size = "sm" }: { onClick: () => void; label?: string; loading?: boolean; size?: "sm" | "md" }) {
  const s = size === "md" ? "px-5 py-2.5 text-sm" : "px-3 py-1.5 text-[10px]";
  return <button onClick={onClick} disabled={loading}
    className={"inline-flex items-center gap-1.5 " + s + " bg-[#FFC542] hover:bg-[#FFC542]/80 disabled:bg-[#FFC542]/40 text-[#111] rounded-xl font-black shadow-sm hover:shadow-md transition-all"}>
    {loading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />} {label}
  </button>;
}

function ToastContainer({ toasts }: { toasts: Toast[] }) {
  if (toasts.length === 0) return null;
  return <div className="fixed top-4 right-4 z-[60] flex flex-col gap-2 max-w-sm">
    {toasts.map(t => (
      <div key={t.id} className={"animate-slide-in px-4 py-3 rounded-3xl shadow-xl border text-xs font-bold flex items-center gap-2 " + (t.type === "success" ? "bg-green-50 dark:bg-green-900/40 border-green-200 dark:border-green-700 text-green-700 dark:text-green-300" : t.type === "error" ? "bg-red-50 dark:bg-red-900/40 border-red-200 dark:border-red-700 text-red-700 dark:text-red-300" : "bg-blue-50 dark:bg-blue-900/40 border-blue-200 dark:border-blue-700 text-blue-700 dark:text-blue-300")}>
        {t.type === "success" ? <CheckCircle className="w-4 h-4 shrink-0" /> : t.type === "error" ? <AlertTriangle className="w-4 h-4 shrink-0" /> : <Bell className="w-4 h-4 shrink-0" />}
        {t.message}
      </div>
    ))}
  </div>;
}

async function seedUsers(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    const users = [
      { uid: "seed_u1", name: "John Doe", email: "john@example.com", phone: "08012345678", role: "rider", status: "online", isOnline: true, rating: 4.8, deliveryCount: 120, walletBalance: 5000, loyaltyPoints: 100, photoUrl: "" },
      { uid: "seed_u2", name: "Jane Smith", email: "jane@example.com", phone: "08087654321", role: "customer", status: "online", isOnline: true, rating: 5.0, deliveryCount: 0, walletBalance: 2000, loyaltyPoints: 0, photoUrl: "" },
      { uid: "seed_u3", name: "Samuel Ade", email: "sam@example.com", phone: "08055544433", role: "rider", status: "offline", isOnline: false, rating: 4.5, deliveryCount: 85, walletBalance: 1200, loyaltyPoints: 50, photoUrl: "" },
    ];
    users.forEach(u => batch.set(doc(collection(db, "users")), u));
    await batch.commit();
    addLog("Seeded", "3 sample users"); addToast("success", "Users seeded"); createNotification("Users Seeded", "3 sample users added to the system");
  } catch (e: any) { addToast("error", "User seed failed: " + e.message); }
}

async function seedDeliveries(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    const couriers = ["John Doe", "Samuel Ade", "Michael Obi", "Esther Frank", "David Okafor", "Grace Eze"];
    const now = new Date();
    const ds = (d: number) => new Date(now.getTime() + d * 86400000).toISOString().slice(0, 10);
    const deliveries = [
      { receiverName: "Alice Johnson", deliveryAddress: "Victoria Island, Lagos", senderName: "Shopify Store", senderPhone: "08011122233", receiverPhone: "08044455566", price: 2500, riderId: "seed_u1", courierName: couriers[0], courierPhone: "08012345678", itemName: "Laptop", pickupAddress: "Ikeja, Lagos", quantity: 1, weight: 1.5, dateString: ds(-1), tipAmount: 500, userId: "seed_u2", otpCode: "1234", status: "DELIVERED", category: "Express" },
      { receiverName: "Bob Brown", deliveryAddress: "Lekki Phase 1, Lagos", senderName: "Amazon Hub", senderPhone: "08099988877", receiverPhone: "08077766655", price: 1500, riderId: "seed_u3", courierName: couriers[1], courierPhone: "08055544433", itemName: "Books", pickupAddress: "Surulere, Lagos", quantity: 2, weight: 0.8, dateString: ds(0), tipAmount: 200, userId: "seed_u2", otpCode: "5678", status: "TRANSIT", category: "Standard" },
      { receiverName: "Charlie Davis", deliveryAddress: "Ajah, Lagos", senderName: "Fresh Foods", senderPhone: "08022233344", receiverPhone: "08055566677", price: 3000, riderId: "seed_u1", courierName: couriers[2], courierPhone: "08066677788", itemName: "Groceries", pickupAddress: "Yaba, Lagos", quantity: 5, weight: 4.2, dateString: ds(0), tipAmount: 0, userId: "seed_u2", otpCode: "9012", status: "PENDING", category: "Cold Chain" },
      { receiverName: "Diana Okonkwo", deliveryAddress: "GRA, Port Harcourt", senderName: "Jumia Foods", senderPhone: "08033344455", receiverPhone: "08088899900", price: 1800, riderId: "seed_u3", courierName: couriers[3], courierPhone: "08011122200", itemName: "Documents", pickupAddress: "Trans Amadi, PH", quantity: 1, weight: 0.3, dateString: ds(1), tipAmount: 100, userId: "seed_u2", otpCode: "3456", status: "DELIVERED", category: "Economy" },
      { receiverName: "Efe Martins", deliveryAddress: "Warri, Delta", senderName: "PharmaCo", senderPhone: "08055566688", receiverPhone: "08099900011", price: 4200, riderId: "", courierName: couriers[4], courierPhone: "08022233300", itemName: "Medical Supplies", pickupAddress: "Benin City", quantity: 3, weight: 6.0, dateString: ds(2), tipAmount: 0, userId: "seed_u2", otpCode: "7890", status: "ASSIGNED", category: "Express" },
      { receiverName: "Fatima Yusuf", deliveryAddress: "Kano City Mall, Kano", senderName: "MegaMart", senderPhone: "08077788899", receiverPhone: "08011122255", price: 3500, riderId: "", courierName: couriers[5], courierPhone: "08044455500", itemName: "Home Appliances", pickupAddress: "Kano Market", quantity: 2, weight: 8.0, dateString: ds(1), tipAmount: 300, userId: "seed_u2", otpCode: "2345", status: "TRANSIT", category: "Batch" },
      { receiverName: "Gloria Adebayo", deliveryAddress: "Bodija, Ibadan", senderName: "PrintHub", senderPhone: "08033322211", receiverPhone: "08066655544", price: 2200, riderId: "seed_u1", courierName: couriers[0], courierPhone: "08012345678", itemName: "Print Materials", pickupAddress: "Dugbe, Ibadan", quantity: 4, weight: 2.5, dateString: ds(3), tipAmount: 150, userId: "seed_u2", otpCode: "6789", status: "PENDING", category: "Multi" },
      { receiverName: "Henry Okafor", deliveryAddress: "Enugu Urban, Enugu", senderName: "TechWorld", senderPhone: "08044455566", receiverPhone: "08077788822", price: 2800, riderId: "seed_u3", courierName: couriers[1], courierPhone: "08055544433", itemName: "Smartphone", pickupAddress: "Awka, Anambra", quantity: 1, weight: 0.6, dateString: ds(4), tipAmount: 250, userId: "seed_u2", otpCode: "1111", status: "PENDING", category: "Standard" },
    ];
    deliveries.forEach(d => batch.set(doc(collection(db, "deliveries")), d));
    await batch.commit();
    addLog("Seeded", "8 sample deliveries across all service types"); addToast("success", "Deliveries seeded"); createNotification("Deliveries Seeded", "8 sample shipments added across Express, Economy, Standard, Batch, Multi, Cold Chain");
  } catch (e: any) { addToast("error", "Delivery seed failed: " + e.message); }
}

async function seedBanners(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    [{ title: "Premium Logistics at Your Doorstep", subtitle: "Fast, secure, and reliable delivery across the city.", imageUrl: "https://images.unsplash.com/photo-1580674285054-bed31e145f59?w=800", interval: 5, order: 0, active: true },
    { title: "Send Packages with Ease", subtitle: "Real-time tracking and professional riders at your service.", imageUrl: "https://images.unsplash.com/photo-1566576912320-8a9549693bb9?w=800", interval: 5, order: 1, active: true },
    { title: "Your Trusted Delivery Partner", subtitle: "Join thousands of happy customers.", imageUrl: "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800", interval: 5, order: 2, active: true }]
      .forEach(s => batch.set(doc(collection(db, "banners")), s)); await batch.commit();
    addLog("Seeded", "3 sample banner slides"); addToast("success", "3 banners created"); createNotification("Banners Seeded", "3 hero slides added to the system");
  } catch (e: any) { addToast("error", "Banner seed failed: " + e.message); }
}

async function seedPromos(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    [{ title: "Festive Save", description: "Enjoy exclusive Eid discounts.", discountType: "percentage", discountValue: 25, discountDisplay: "25% OFF", minOrderAmount: 1000, maxDiscount: 5000, code: "EID2026", usageLimit: 500, usedCount: 0, active: true },
    { title: "First Delivery Free", description: "New users get their first delivery free.", discountType: "percentage", discountValue: 100, discountDisplay: "100% OFF", minOrderAmount: 2000, maxDiscount: 7000, code: "FIRSTFREE", usageLimit: 200, usedCount: 0, active: true },
    { title: "Weekend Rush", description: "Flat discount on every ride.", discountType: "fixed", discountValue: 1500, discountDisplay: "\u20A61,500 OFF", minOrderAmount: 3000, maxDiscount: 1500, code: "WEEKEND30", usageLimit: 300, usedCount: 0, active: true }]
      .forEach(s => batch.set(doc(collection(db, "promotions")), s)); await batch.commit();
    addLog("Seeded", "3 sample promotions"); addToast("success", "3 promotions created"); createNotification("Promotions Seeded", "3 promotions added to the system");
  } catch (e: any) { addToast("error", "Promo seed failed: " + e.message); }
}

async function seedReferrals(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    [{ referrerId: "seed1", referrerName: "Aisha Bello", referrerEmail: "aisha@example.com", refereeId: "seed2", refereeName: "Chidi Okonkwo", refereeEmail: "chidi@example.com", rewardAmount: 500, status: "completed" },
    { referrerId: "seed3", referrerName: "Fatima Musa", referrerEmail: "fatima@example.com", refereeId: "seed4", refereeName: "Emeka Nwosu", refereeEmail: "emeka@example.com", rewardAmount: 500, status: "pending" }]
      .forEach(s => batch.set(doc(collection(db, "referrals")), s)); await batch.commit();
    addLog("Seeded", "2 sample referrals"); addToast("success", "2 referrals created"); createNotification("Referrals Seeded", "2 referral records added");
  } catch (e: any) { addToast("error", "Referral seed failed: " + e.message); }
}

async function seedAppContent(db: any, addLog: any, addToast: any, createNotification: any, setSettings: any) {
  try {
    const content = { referral: { benefitText: "Invite your friends and earn \u20A6500 in wallet credit for each successful referral!", referrerCode: "", reward: 500, active: true }, aiAssistant: { title: "Dispatch Assistant", description: "Need help with your delivery? Our AI assistant is here 24/7.", tag: "Powered by HeyTek AI", active: true }, welcomeGift: { title: "Welcome to Engraced!", credit: 2500, coins: 10, active: true }, weatherTraffic: { optimalMessage: "Light traffic conditions — perfect timing.", congestedMessage: "Heavy traffic on major routes — expect 15-20 min delays.", optimalBadge: "Smooth Sailing", congestedBadge: "Heavy Traffic", active: true }, loyalty: { bronzeThreshold: 10, silverThreshold: 25, goldThreshold: 50, platinumThreshold: 100, ordersForBronze: 3, ordersForSilver: 5, ordersForGold: 10, dailyBonus: 25, active: true }, statsConfig: { promoSavingsPerBooking: 3500, statLabels: ["Deliveries", "Saved", "Earned", "Redeemed"], active: true } };
    await setDoc(doc(db, "system_config", "global_settings"), { appContent: content, updatedAt: Timestamp.now() }, { merge: true });
    setSettings((prev: any) => ({ ...prev, appContent: content })); addLog("Seeded", "Default app card content"); addToast("success", "App content seeded"); createNotification("App Content Seeded", "Default dashboard content configured");
  } catch (e: any) { addToast("error", "App content seed failed: " + e.message); }
}

function DashboardTab({ deliveries, activeUsers, customers, drivers, pendingDeliveries, delivered, totalRevenue, totalTips, referrals, activeDeliveriesData, fmt, seedUsers, seedDeliveries, seedBanners, seedPromos, seedReferrals, seedAppContent, seeding, setTab }: any) {
  const recentDeliveries = deliveries.slice(-10).reverse();
  const [filterCat, setFilterCat] = useState("all");
  const serviceIcon = (tag: string, cls: string) => {
    switch(tag) {
      case "Express": return <Zap size={24} className={cls} strokeWidth={2} />;
      case "Economy": return <DollarSign size={24} className={cls} strokeWidth={2} />;
      case "Standard": return <Package size={24} className={cls} strokeWidth={2} />;
      case "Batch": return <Layers size={24} className={cls} strokeWidth={2} />;
      case "Multi": return <MapPin size={24} className={cls} strokeWidth={2} />;
      case "Cold Chain": return <Shield size={24} className={cls} strokeWidth={2} />;
      default: return <Package size={24} className={cls} strokeWidth={2} />;
    }
  };
  const catBtn = (cat: string, label: string) => (
    <button onClick={() => setFilterCat(cat)}
      className={"px-5 py-2.5 rounded-full text-sm font-bold transition-all shadow-sm " + (filterCat === cat ? "bg-[#111] dark:bg-white text-white dark:text-[#111]" : "bg-white dark:bg-[#222] text-[#111] dark:text-white border border-black/20 dark:border-white/20 hover:bg-black/5 dark:hover:bg-white/10")}>{label}</button>
  );
  return <div className="tab-content space-y-8">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="stagger-1"><StatCard icon={<Users className="w-4 h-4 text-[#FFC542]" />} label="TOTAL USERS" value={activeUsers.length.toString()} sub={customers.length + " customers · " + drivers.length + " drivers"} /></div>
        <div className="stagger-2"><StatCard icon={<Package className="w-4 h-4 text-[#FFC542]" />} label="SHIPMENTS" value={deliveries.length.toString()} sub={pendingDeliveries.length + " pending · " + delivered.length + " delivered"} /></div>
        <div className="stagger-3"><StatCard icon={<DollarSign className="w-4 h-4 text-[#FFC542]" />} label="REVENUE" value={fmt(totalRevenue)} sub={fmt(totalTips) + " in tips · " + referrals.length + " referrals"} /></div>
        <div className="stagger-4"><StatCard icon={<Activity className="w-4 h-4 text-[#FFC542]" />} label="ONLINE" value={(activeUsers.filter((u: any) => u.isOnline).length).toString()} sub={drivers.filter((d: any) => d.isOnline).length + " drivers · " + (activeUsers.filter((u: any) => u.role === "customer" && u.isOnline).length) + " customers"} /></div>
      </div>
      <section>
        <div className="flex justify-between items-end mb-6 flex-wrap gap-4">
          <h1 className="text-[28px] font-extrabold tracking-tight text-[#111] dark:text-white">Active Deliveries</h1>
          <div className="flex gap-2.5 flex-wrap">
            {catBtn("all", "All deliveries")}
            {catBtn("Express", "Express")}
            {catBtn("Economy", "Economy")}
            {catBtn("Standard", "Standard")}
            {catBtn("Batch", "Batch")}
            {catBtn("Multi", "Multi")}
            {catBtn("Cold Chain", "Cold Chain")}
          </div>
        </div>
        {deliveries.length === 0 && <div className="text-center py-6 text-black/40 dark:text-white/40 font-bold text-base">No deliveries yet. Seed sample data below.</div>}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {(filterCat === "all" ? activeDeliveriesData : activeDeliveriesData.filter((a: any) => a.tag === filterCat)).map((a: any, i: number) => {
            const g = a.theme === "gold" ? "bg-[#FFC542]" : a.theme === "black" ? "bg-[#111]" : "bg-white border-[2.5px] border-[#111] dark:border-white";
            const t = a.theme === "gold" ? "bg-[#111] text-white" : a.theme === "black" ? "bg-white text-[#111]" : "bg-[#FFC542] text-[#111]";
            const p = a.theme === "gold" ? "bg-black/20" : a.theme === "black" ? "bg-white/20" : "bg-black/10 dark:bg-white/10";
            const pf = a.theme === "black" ? "bg-[#FFC542]" : "bg-[#111]";
            const tc = a.theme === "black" ? "text-white" : "text-[#111] dark:text-white";
            const bt = a.theme === "black" ? "bg-[#FFC542] text-[#111]" : "bg-[#111] text-white";
            const ac = a.theme === "gold" ? "ring-[#FFC542]" : a.theme === "black" ? "ring-[#111]" : "ring-white";
            const ec = a.theme === "gold" ? "bg-[#111] text-white" : a.theme === "black" ? "bg-white text-[#111]" : "bg-[#FFC542] text-[#111]";
            const bk = a.theme === "black" ? "fill-none text-white" : a.theme === "gold" ? "fill-[#111] text-[#111]" : "fill-none text-[#111] dark:text-white";
            const pct = a.total > 0 ? Math.round((a.progress / a.total) * 100) : 0;
            const cardCouriers = deliveries.filter((d: any) => d.category === a.tag && d.courierName && d.courierName !== "Unassigned").slice(0, 3);
            const extraCount = Math.max(0, deliveries.filter((d: any) => d.category === a.tag && d.courierName && d.courierName !== "Unassigned").length - 3);
            return <div key={i} className={"animate-fade-in hover:scale-[1.02] transition-all duration-300 " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i % 6])}>
              <div className={g + " rounded-3xl p-7 flex flex-col min-h-[220px] shadow-sm relative overflow-hidden group"}>
                <div className="flex justify-between items-start mb-6">
                  <span className={t + " text-xs font-bold px-4 py-1.5 rounded-full z-10"}>{a.tag}</span>
                  {serviceIcon(a.tag, bk)}
                </div>
                <h3 className={"text-[22px] font-extrabold leading-snug pr-8 mb-8 z-10 " + tc}>{a.title}</h3>
                <div className="mt-auto z-10">
                  <div className={"flex justify-between items-end font-bold text-[13px] mb-3 " + tc}>
                    <span>Progress</span>
                    <span>{a.progress}/{a.total} {a.unit}</span>
                  </div>
                  <div className={"w-full h-2 rounded-full mb-6 " + p}>
                    <div className={"h-full rounded-full " + pf} style={{ width: pct + "%" }}></div>
                  </div>
                  <div className="flex justify-between items-center">
                    <div className="flex -space-x-3">
                      {cardCouriers.length > 0 ? cardCouriers.map((c: any, j: number) => <img key={j} src={"https://api.dicebear.com/7.x/avataaars/svg?seed=" + c.courierName} alt="" className={"w-9 h-9 rounded-full ring-2 " + ac + " bg-white object-cover"} />) : ["Mia","John","Sarah"].slice(0, Math.max(1, a.total)).map((s, j) => <div key={j} className={"w-9 h-9 rounded-full ring-2 " + ac + " " + ec + " text-[10px] font-bold flex items-center justify-center"}>{s.charAt(0)}</div>)}
                      <div className={"w-9 h-9 rounded-full ring-2 " + ac + " " + ec + " text-[10px] font-bold flex items-center justify-center"}>{cardCouriers.length > 0 ? "+" + extraCount : "+" + Math.max(0, a.total)}</div>
                    </div>
                    <button onClick={() => setTab("shipments")} className={bt + " px-6 py-2.5 rounded-full text-sm font-bold shadow-md hover:opacity-80 transition-all"}>Track</button>
                  </div>
                </div>
              </div>
            </div>;
          })}
        </div>
      </section>
      <section className="grid grid-cols-1 lg:grid-cols-4 gap-6 min-h-0">
        <div className="lg:col-span-3 border border-black/10 dark:border-white/10 rounded-3xl p-7 flex flex-col bg-white dark:bg-[#1a1a1a]">
          <div className="flex justify-between items-center mb-5">
            <h2 className="text-[22px] font-extrabold tracking-tight text-[#111] dark:text-white">Next scheduled drops</h2>
            <button onClick={() => setTab("shipments")} className="text-[#FFC542] font-bold text-sm hover:underline">View all deliveries</button>
          </div>
          {recentDeliveries.length === 0 && <div className="text-center py-10 text-black/40 dark:text-white/40 text-sm">No scheduled drops yet.</div>}
          {recentDeliveries.length > 0 && <>
            <div className="grid grid-cols-12 gap-4 pb-3 border-b-2 border-black/5 dark:border-white/10 text-[13px] text-black/50 dark:text-white/50 font-bold px-3">
              <div className="col-span-6">Delivery Task</div>
              <div className="col-span-4">Assigned Rider</div>
              <div className="col-span-2 text-right">Est. Time</div>
            </div>
            <div className="flex-1 overflow-y-auto pr-2 mt-2 space-y-1">
              {recentDeliveries.filter((d: any) => filterCat === "all" || d.category === filterCat).slice(0, 8).map((d: any, idx: number) => (
                <div key={d.id} className={"grid grid-cols-12 gap-4 py-3.5 border-b border-black/5 dark:border-white/5 items-center px-3 hover:bg-black/5 dark:hover:bg-white/5 transition-colors rounded-xl last:border-b-0 cursor-pointer animate-fade-in " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6","stagger-7","stagger-8"][idx] || "stagger-1")}>
                  <div className="col-span-6 flex flex-col justify-center">
                    <div className="font-extrabold text-[14px] text-[#111] dark:text-white leading-snug">{(idx + 1).toString().padStart(2,"0")}. {d.itemName || "Parcel"} — {d.receiverName}</div>
                    <div className="text-[13px] font-medium text-black/50 dark:text-white/50 mt-0.5">{d.status.replace(/_/g, " ")} · {d.pickupAddress}</div>
                  </div>
                  <div className="col-span-4 flex items-center gap-3.5">
                    <div className="w-9 h-9 rounded-full bg-[#FFC542]/10 border border-black/5 dark:border-white/10 flex items-center justify-center text-xs font-black text-[#111] dark:text-white">{d.courierName?.charAt(0) || "?"}</div>
                    <span className="font-bold text-[14px] text-[#111] dark:text-white">{d.courierName || "Unassigned"}</span>
                  </div>
                  <div className="col-span-2 text-right font-extrabold text-[14px] text-[#111] dark:text-white">{d.dateString ? d.dateString.slice(0, 10) : "—"}</div>
                </div>
              ))}
            </div>
          </>}
        </div>
        <div className="bg-[#111] dark:bg-black text-white rounded-3xl p-8 flex flex-col justify-between shadow-lg">
          <div>
            <p className="text-white/60 text-sm font-semibold mb-6 tracking-wide">Fleet summary</p>
            <span className="bg-[#FFC542] text-[#111] text-xs font-bold px-3.5 py-1.5 rounded-lg inline-block mb-4 shadow-sm">{drivers.length} Active</span>
            <h3 className="text-[26px] font-extrabold leading-snug mb-6 pr-4">Total Fleet: {drivers.length + customers.length}</h3>
            <p className="text-white/60 text-sm font-semibold mb-4 tracking-wide">Active drivers in region</p>
            <div className="flex -space-x-3.5 mb-6">
              {drivers.slice(0, 3).map((d: any) => (
                <div key={d.id} className="w-[42px] h-[42px] rounded-full ring-[2.5px] ring-[#111] bg-[#FFC542]/20 flex items-center justify-center text-xs font-black text-[#FFC542]">{d.name?.charAt(0) || "?"}</div>
              ))}
              {[...Array(Math.min(3, Math.max(0, 3 - (drivers?.length || 0))))].map((_: any, i: number) => (
                <div key={i} className="w-[42px] h-[42px] rounded-full ring-[2.5px] ring-[#111] bg-gray-800 flex items-center justify-center text-xs text-white/40">?</div>
              ))}
              <div className="w-[42px] h-[42px] rounded-full ring-[2.5px] ring-[#111] bg-[#FFC542] text-[#111] text-xs font-extrabold flex items-center justify-center shadow-inner">+{Math.max(0, (drivers?.length || 0) - 3)}</div>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex justify-between text-sm"><span className="text-white/60">Deliveries today</span><span className="font-bold">{deliveries.length}</span></div>
            <div className="flex justify-between text-sm"><span className="text-white/60">Revenue</span><span className="font-bold">{fmt(totalRevenue)}</span></div>
          </div>
          <button onClick={() => setTab("shipments")} className="w-full bg-[#FFC542] text-[#111] py-4 rounded-[18px] text-[15px] font-extrabold shadow-md hover:bg-[#FFC542]/90 transition-all mt-4">Assign riders</button>
        </div>
      </section>
      <div className="flex flex-wrap gap-3">
        <QuickBtn label="Seed Users" desc="Create sample users" onClick={seedUsers} loading={seeding === "users"} />
        <QuickBtn label="Seed Deliveries" desc="Create sample shipments" onClick={seedDeliveries} loading={seeding === "deliveries"} />
        <QuickBtn label="Seed Banners" desc="Create 3 sample hero slides" onClick={seedBanners} loading={seeding === "banners"} />
        <QuickBtn label="Seed Promos" desc="Create 3 sample promotions" onClick={seedPromos} loading={seeding === "promos"} />
        <QuickBtn label="Seed Referrals" desc="Create sample referral records" onClick={seedReferrals} loading={seeding === "referrals"} />
        <QuickBtn label="Seed App Content" desc="Set default dashboard card content" onClick={seedAppContent} loading={seeding === "appcontent"} />
      </div>
    </div>;
  }



interface SidebarProps {
  sidebar: boolean;
  setSidebar: (v: boolean) => void;
  tab: TabId;
  setTab: (v: TabId) => void;
  mobileSidebar: boolean;
  setMobileSidebar: (v: boolean) => void;
  navItems: { id: TabId; label: string; icon: React.ReactNode }[];
}

function Sidebar({ sidebar, setSidebar, tab, setTab, mobileSidebar, setMobileSidebar, navItems }: SidebarProps) {
  return (
    <aside className={"h-full bg-[#111] flex flex-col shrink-0 z-10 transition-all duration-300 overflow-y-auto scrollbar-none " + (sidebar ? "w-[220px]" : "w-[70px]") + (mobileSidebar ? " translate-x-0" : " -translate-x-full lg:translate-x-0")}>
      <div className={"shrink-0 mx-3 mt-5 mb-8 flex items-center cursor-pointer transition-all " + (sidebar ? "justify-start px-4 py-3" : "justify-center p-3")}
        onClick={() => setSidebar(!sidebar)}>
        <EdLogoSvg size={28} />
        {sidebar && (
          <div className="flex flex-col ml-3">
            <span className="text-[#FFC542] text-sm font-black leading-tight">ENGRACE</span>
            <span className="text-[#FFC542] text-sm font-black leading-tight">DISPATCH</span>
          </div>
        )}
      </div>
      <nav className="flex flex-col gap-1 w-full px-3 flex-1 overflow-y-auto scrollbar-none pb-4">
        {navItems.map(n => (
          <button key={n.id} onClick={() => { setTab(n.id); setMobileSidebar(false); }}
            className={"flex items-center gap-3 p-3 rounded-3xl transition-all " + (tab === n.id ? "bg-[#FFC542] text-[#111] shadow-lg" : "text-white/50 hover:text-white hover:bg-white/5")}>
            <span className="shrink-0">{n.icon}</span>
            {sidebar && <span className="text-xs font-bold whitespace-nowrap">{n.label}</span>}
          </button>
        ))}
      </nav>
    </aside>
  );
}

interface HeaderProps {
  searchQuery: string;
  setSearchQuery: (v: string) => void;
  unreadCount: number;
  setShowNotifs: (v: boolean) => void;
  showNotifs: boolean;
  setShowUserMenu: (v: boolean) => void;
  showUserMenu: boolean;
  currentUser: any;
  userRole: string;
  toggleDark: () => void;
  dark: boolean;
  setMobileSidebar: (v: boolean) => void;
  notifications: any[];
  markNotifRead: (id: string) => void;
}

function Header({ searchQuery, setSearchQuery, unreadCount, setShowNotifs, showNotifs, setShowUserMenu, showUserMenu, currentUser, userRole, toggleDark, dark, setMobileSidebar, notifications, markNotifRead }: HeaderProps) {
  return (
    <header className="flex justify-between items-center px-4 sm:px-8 lg:px-12 pt-6 lg:pt-10 pb-0 shrink-0 gap-4">
      <div className="flex items-center gap-3">
        <button className="lg:hidden p-2 text-[#111] dark:text-white hover:bg-black/5 dark:hover:bg-white/10 rounded-xl transition-colors" onClick={() => setMobileSidebar(true)}>
          <Menu size={22} />
        </button>
        <div className="text-sm sm:text-base text-black/60 dark:text-white/60">
          Welcome to<br /><span className="font-extrabold text-[#111] dark:text-white text-lg sm:text-xl tracking-tight">Engraced <span className="text-[#FFC542]">Dispatch</span></span>
        </div>
      </div>
      <div className="flex items-center gap-2 sm:gap-5">
        <div className="hidden sm:flex items-center border border-black/20 dark:border-white/20 rounded-full pl-5 pr-1.5 py-1.5 w-[200px] lg:w-[280px] shadow-sm">
          <input type="text" placeholder="Search" value={searchQuery} onChange={e => setSearchQuery(e.target.value)} className="outline-none flex-1 text-sm bg-transparent font-medium text-[#111] dark:text-white" />
          <button className="bg-[#FFC542] text-[#111] p-2 rounded-xl hover:bg-[#FFC542]/90 transition-colors"><Search size={18} strokeWidth={2.5} /></button>
        </div>
        <div className="relative">
          <button onClick={(e) => { e.stopPropagation(); setShowNotifs(!showNotifs); setShowUserMenu(false); }} className="relative p-2.5 border border-black/10 dark:border-white/10 rounded-full flex items-center justify-center cursor-pointer shadow-sm hover:bg-black/5 dark:hover:bg-white/10 transition-colors">
            <Bell size={20} className="text-[#111] dark:text-white" />
            {unreadCount > 0 && <div className="absolute top-2 right-2.5 w-2.5 h-2.5 bg-[#FFC542] rounded-full border-2 border-white dark:border-[#1a1a1a] animate-pulse-ring"></div>}
          </button>
          {showNotifs && <div className="absolute right-0 top-full mt-2 w-80 bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-2xl overflow-hidden z-50">
            <div className="p-4 border-b border-black/10 dark:border-white/10"><p className="text-xs font-bold text-[#FFC542]">NOTIFICATIONS</p></div>
            <div className="max-h-72 overflow-y-auto">
              {notifications.length === 0 && <div className="p-6 text-center text-[10px] text-black/40 dark:text-white/40 font-bold">No notifications yet.</div>}
              {notifications.map((n: any) => <div key={n.id} onClick={() => markNotifRead(n.id)} className={"p-4 border-b border-black/5 dark:border-white/5 hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer " + (n.read ? "" : "bg-[#FFC542]/5")}>
                <div className="flex items-start gap-3">
                  <div className={"w-2 h-2 mt-1.5 rounded-full shrink-0 " + (n.read ? "bg-transparent" : "bg-[#FFC542]")}></div>
                  <div><p className="text-xs font-bold text-[#111] dark:text-white">{n.title}</p><p className="text-[10px] text-black/50 dark:text-white/50 mt-0.5">{n.description}</p><p className="text-[9px] text-black/30 dark:text-white/30 mt-1">{n.time || "Just now"}</p></div>
                </div>
              </div>)}
            </div>
            <div className="p-3 text-center border-t border-black/10 dark:border-white/10"><button className="text-[10px] text-[#FFC542] font-bold hover:underline">View all notifications</button></div>
          </div>}
        </div>
        <div className="relative">
          <div className="flex items-center gap-3 ml-1 cursor-pointer" onClick={(e) => { e.stopPropagation(); setShowUserMenu(!showUserMenu); setShowNotifs(false); }}>
            <div className="w-11 h-11 rounded-full bg-[#FFC542]/20 border border-black/10 dark:border-white/10 flex items-center justify-center text-[#111] dark:text-white font-black text-sm"><EdLogoSvg size={20} /></div>
            <div className="text-sm hidden sm:block">
              <div className="font-extrabold text-[#111] dark:text-white">{currentUser?.email?.split("@")[0] || "Admin"}</div>
              <div className="text-black/50 dark:text-white/50 font-medium text-xs mt-0.5">{(userRole || "admin").replace("_", " ").toUpperCase()}</div>
            </div>
          </div>
          {showUserMenu && <div className="absolute right-0 top-full mt-2 w-56 bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-2xl overflow-hidden z-50">
            <div className="p-4 border-b border-black/10 dark:border-white/10">
              <p className="text-xs font-bold text-[#111] dark:text-white">{currentUser?.email}</p>
              <p className="text-[10px] text-black/40 dark:text-white/40 mt-0.5">{(userRole || "Admin").replace("_", " ").toUpperCase()}</p>
            </div>
            <div className="p-2">
              <button onClick={toggleDark} className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-black/5 dark:hover:bg-white/10 transition-colors text-xs font-bold text-[#111] dark:text-white">
                {dark ? <Sun className="w-4 h-4 text-[#FFC542]" /> : <Moon className="w-4 h-4 text-[#FFC542]" />}
                {dark ? "Light Mode" : "Dark Mode"}
              </button>
              <button onClick={() => { document.cookie = "admin_auth=; path=/; max-age=0; SameSite=Strict"; signOut(auth); }} className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors text-xs font-bold text-red-500 mt-1">
                <LogOut className="w-4 h-4" /> Sign Out
              </button>
            </div>
          </div>}
        </div>
      </div>
    </header>
  );
}




function AdminDashboardPage() {
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [userRole, setUserRole] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [signingUp, setSigningUp] = useState(false);
  const [signupRole, setSignupRole] = useState("super_admin");
  const [email, setEmail] = useState("admin@engraced.com");
  const [password, setPassword] = useState("");
  const [authErr, setAuthErr] = useState("");
  const [authOk, setAuthOk] = useState("");
  const [tab, setTab] = useState<TabId>("dashboard");
  const [sidebar, setSidebar] = useState(true);
  const [mobileSidebar, setMobileSidebar] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [dark, setDark] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [showNotifs, setShowNotifs] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const addToast = useCallback((type: Toast["type"], message: string) => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4000);
  }, []);

  useEffect(() => {
    const saved = localStorage.getItem("ed_dark");
    if (saved !== null) {
      const isDark = saved === "true";
      setDark(isDark);
      document.documentElement.classList.toggle("dark", isDark);
    } else {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      setDark(prefersDark);
      document.documentElement.classList.toggle("dark", prefersDark);
    }
  }, []);

  useEffect(() => {
    localStorage.setItem("ed_dark", String(dark));
    document.documentElement.classList.toggle("dark", dark);
  }, [dark]);

  useEffect(() => {
    const handler = () => { setShowNotifs(false); setShowUserMenu(false); };
    window.addEventListener("click", handler);
    return () => window.removeEventListener("click", handler);
  }, []);

  const toggleDark = () => setDark(d => !d);

  const [users, setUsers] = useState<UserProfile[]>([]);
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [referrals, setReferrals] = useState<Referral[]>([]);
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [settings, setSettings] = useState<any>({
    pointsSystemEnabled: true, tipSystemEnabled: true, pricingModeDynamic: false,
    referralEnabled: true, referralReward: 500, minDeliveryForPayout: 1,
    baseFare: 4500, perKgRate: 250, expressSurcharge: 1500, surgeMultiplier: 1.25,
    dashboardSections: {} as Record<string, boolean>, appContent: {} as Record<string, any>,
    appName: "ENGRACED DISPATCH", appSlogan: "PREMIUM LOGISTICS & DISPATCH", fcmServerKey: "",
  });
  const [appContent, setAppContent] = useState<AppContent[]>([]);
  const [logs, setLogs] = useState<AuditEntry[]>([]);
  const [connected, setConnected] = useState(false);
  const [refreshT, setRefreshT] = useState("");
  const [notifications, setNotifications] = useState<any[]>([]);

  const unreadCount = notifications.filter((n: any) => !n.read).length;

  const createNotification = useCallback(async (title: string, description: string) => {
    try {
      await addDoc(collection(db, "notifications"), {
        title, description, time: "Just now", read: false,
        createdAt: Timestamp.now(), timestamp: Date.now(),
      });
    } catch {}
  }, []);

  const markNotifRead = useCallback(async (id: string) => {
    try { await updateDoc(doc(db, "notifications", id), { read: true }); } catch {}
  }, []);

  const addLog = useCallback(async (action: string, details: string) => {
    const entry = { action, details, admin: currentUser?.email || "Admin", timestamp: Date.now() };
    try { await addDoc(collection(db, "audit_logs"), entry); } catch {}
    setLogs((prev: AuditEntry[]) => [{ id: Date.now().toString(), time: "Just now", ...entry }, ...prev]);
  }, [currentUser]);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (u) => {
      if (u) {
        const isAdmin = (u.email || "").endsWith("@engraced.com") || (u.email || "").includes("admin");
        if (isAdmin) { setCurrentUser(u); setUserRole("super_admin"); document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax"; }
        else {
          try {
            await new Promise(r => setTimeout(r, 1500));
            const snap = await getDoc(doc(db, "users", u.uid));
            if (!snap.exists()) { setCurrentUser(u); setUserRole("super_admin"); document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax"; setLoading(false); return; }
            const d = snap.data();
            if (d?.role === "admin" || d?.role === "super_admin") { setCurrentUser(u); setUserRole(d.role); document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax"; setLoading(false); return; }
            if (d?.role === "dispatcher") { setCurrentUser(u); setUserRole("dispatcher"); document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax"; setLoading(false); return; }
            setAuthErr("Unauthorized"); document.cookie = "admin_auth=; path=/; max-age=0; SameSite=Lax"; signOut(auth); setCurrentUser(null);
          } catch { setCurrentUser(u); setUserRole("super_admin"); document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax"; }
        }
      } else { setCurrentUser(null); setUserRole(""); setLoading(false); return; }
      setLoading(false);
    });
    return () => unsub();
  }, []);

  useEffect(() => {
    if (!currentUser) return;
    const unsubs: (() => void)[] = [];
    unsubs.push(onSnapshot(collection(db, "users"), snap => {
      const list: UserProfile[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, uid: x.uid || d.id, name: x.name || "", email: x.email || "", phone: x.phone || "",
        role: x.role || "customer", status: x.status || "offline", isOnline: x.isOnline || false,
        rating: x.rating || 0, deliveryCount: x.deliveryCount || 0, walletBalance: x.walletBalance || 0,
        loyaltyPoints: x.loyaltyPoints || 0, photoUrl: x.photoUrl || "",
        bikeNumber: x.bikeNumber || "", lat: x.lat || x.latitude, lng: x.lng || x.longitude,
        isDeleted: x.isDeleted || false, updatedAt: x.updatedAt,
      }); });
      setUsers(list); setConnected(true); setRefreshT(new Date().toLocaleTimeString());
    }, () => setConnected(false)));
    unsubs.push(onSnapshot(collection(db, "deliveries"), snap => {
      const list: Delivery[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, status: x.status || "PENDING", receiverName: x.receiverName || "",
        deliveryAddress: x.deliveryAddress || "", senderName: x.senderName || "",
        senderPhone: x.senderPhone || "", receiverPhone: x.receiverPhone || "",
        price: x.price || 0, riderId: x.riderId || "", courierName: x.courierName || "Unassigned",
        courierPhone: x.courierPhone || "", itemName: x.itemName || "Parcel",
        pickupAddress: x.pickupAddress || "", quantity: x.quantity || 1, weight: x.weight || 0,
        dateString: x.dateString || "", tipAmount: x.tipAmount || 0, userId: x.userId || "", otpCode: x.otpCode || "",
        category: x.category || "",
      }); });
      setDeliveries(list);
    }));
    unsubs.push(onSnapshot(collection(db, "banners"), snap => {
      const list: Banner[] = [];
      snap.forEach(d => { const x = d.data(); list.push({ id: d.id, title: x.title || "", subtitle: x.subtitle || "", imageUrl: x.imageUrl || "", interval: x.interval || 5, order: x.order || 0, active: x.active !== false }); });
      setBanners(list.sort((a, b) => a.order - b.order));
    }));
    unsubs.push(onSnapshot(collection(db, "referrals"), snap => {
      const list: Referral[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, referrerId: x.referrerId || "", referrerName: x.referrerName || "",
        referrerEmail: x.referrerEmail || "", refereeId: x.refereeId || "",
        refereeName: x.refereeName || "", refereeEmail: x.refereeEmail || "",
        rewardAmount: x.rewardAmount || 0, status: x.status || "pending",
      }); });
      setReferrals(list);
    }));
    unsubs.push(onSnapshot(collection(db, "promotions"), snap => {
      const list: Promotion[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, title: x.title || "", description: x.description || "",
        discountType: x.discountType || "percentage", discountValue: x.discountValue || 0,
        discountDisplay: x.discountDisplay || "", minOrderAmount: x.minOrderAmount || 0,
        maxDiscount: x.maxDiscount || 0, code: x.code || "", usageLimit: x.usageLimit || 0,
        usedCount: x.usedCount || 0, active: x.active !== false,
      }); });
      setPromotions(list);
    }));
    unsubs.push(onSnapshot(collection(db, "appContent"), snap => {
      const list: AppContent[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, key: x.key || "", title: x.title || "", description: x.description || "",
        imageUrl: x.imageUrl || "", ctaText: x.ctaText || "", ctaLink: x.ctaLink || "",
        order: x.order || 0, active: x.active !== false,
      }); });
      setAppContent(list);
    }));
    unsubs.push(onSnapshot(collection(db, "notifications"), snap => {
      const list: any[] = [];
      snap.forEach(d => { const x = d.data(); list.push({ id: d.id, ...x }); });
      setNotifications(list.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)));
    }));
    getDoc(doc(db, "system_config", "global_settings")).then(s => { if (s.exists()) setSettings((prev: any) => ({ ...prev, ...s.data() })); }).catch(() => {});
    getDoc(doc(db, "system_config", "pricing")).then(s => { if (s.exists()) setSettings((prev: any) => ({ ...prev, ...s.data() })); }).catch(() => {});
    return () => unsubs.forEach(f => f());
  }, [currentUser]);

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault(); setAuthErr(""); setAuthOk("");
    try {
      if (signingUp) {
        const cred = await createUserWithEmailAndPassword(auth, email, password);
        await setDoc(doc(db, "users", cred.user.uid), { uid: cred.user.uid, email, name: email.split("@")[0], role: signupRole, createdAt: Timestamp.now(), updatedAt: Timestamp.now() });
        document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax";
        setAuthOk((signupRole === "dispatcher" ? "Dispatcher" : "Admin") + " account created.");
      } else {
        const cred = await signInWithEmailAndPassword(auth, email, password);
        await setDoc(doc(db, "users", cred.user.uid), { uid: cred.user.uid, email, name: email.split("@")[0], role: "super_admin", updatedAt: Timestamp.now() }, { merge: true });
        document.cookie = "admin_auth=true; path=/; max-age=86400; SameSite=Lax";
      }
    } catch (err: any) { setAuthErr(err.message); }
  };

  const updateSetting = async (key: string, val: any) => {
    setSettings((prev: any) => ({ ...prev, [key]: val }));
    await setDoc(doc(db, "system_config", "global_settings"), { [key]: val, updatedAt: Timestamp.now() }, { merge: true });
    addLog("Setting", key + " = " + JSON.stringify(val));
  };

  const [seeding, setSeeding] = useState("");
  const seedUsersWrapper = async () => { setSeeding("users"); await seedUsers(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedDeliveriesWrapper = async () => { setSeeding("deliveries"); await seedDeliveries(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedBannersWrapper = async () => { setSeeding("banners"); await seedBanners(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedPromosWrapper = async () => { setSeeding("promos"); await seedPromos(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedReferralsWrapper = async () => { setSeeding("referrals"); await seedReferrals(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedAppContentWrapper = async () => { setSeeding("appcontent"); await seedAppContent(db, addLog, addToast, createNotification, setSettings); setSeeding(""); };


  const activeUsers = users.filter(u => !u.isDeleted);
  const customers = activeUsers.filter(u => u.role === "customer" || u.role === "");
  const drivers = activeUsers.filter(u => u.role === "rider");
  const adminUsers = activeUsers.filter(u => u.role === "admin" || u.role === "super_admin");
  const pendingDeliveries = deliveries.filter(d => d.status === "PENDING");
  const inTransit = deliveries.filter(d => ["TRANSIT", "ASSIGNED", "OUT_FOR_DELIVERY"].includes(d.status));
  const delivered = deliveries.filter(d => d.status === "DELIVERED");
  const totalRevenue = deliveries.reduce((s, d) => s + (d.price || 0), 0);
  const totalTips = deliveries.reduce((s, d) => s + (d.tipAmount || 0), 0);
  const completedReferrals = referrals.filter(r => r.status === "completed");

  if (loading) return (
    <div className="min-h-screen bg-[#111] flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <EdLogoSvg size={48} />
        <div className="text-[#FFC542] font-black flex items-center gap-3"><RefreshCw className="w-5 h-5 animate-spin" /> LOADING...</div>
      </div>
    </div>
  );

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-[#111] flex flex-col items-center justify-center p-6">
        <div className="w-full max-w-md bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 shadow-xl">
          <div className="flex flex-col items-center mb-6">
            <div className="w-16 h-16 bg-[#FFC542] rounded-3xl flex items-center justify-center shadow-lg mb-4"><EdLogoSvg size={36} dark /></div>
            <h1 className="text-xl font-black text-[#111] dark:text-white tracking-wide text-center">{settings.appName || "ENGRACED DISPATCH"}</h1>
            <p className="text-xs text-[#FFC542] font-semibold tracking-widest mt-1">ADMIN CONTROL CENTER</p>
          </div>
          {authErr && <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-400 text-xs flex items-center gap-2"><AlertTriangle className="w-4 h-4 shrink-0" /> {authErr}</div>}
          {authOk && <div className="mb-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl text-green-700 dark:text-green-400 text-xs flex items-center gap-2"><ShieldCheck className="w-4 h-4 shrink-0" /> {authOk}</div>}
          <form onSubmit={handleAuth} className="space-y-4">
            <div><label className="block text-xs font-semibold text-black/40 dark:text-white/40 mb-1">Admin Email</label>
              <div className="relative"><Mail className="absolute left-3 top-3 w-4 h-4 text-black/40 dark:text-white/40" />
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-10 py-2.5 text-sm text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40" required /></div></div>
            <div><label className="block text-xs font-semibold text-black/40 dark:text-white/40 mb-1">Password</label>
              <div className="relative"><Key className="absolute left-3 top-3 w-4 h-4 text-black/40 dark:text-white/40" />
                <input type="password" value={password} onChange={e => setPassword(e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-10 py-2.5 text-sm text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40" required /></div></div>
            {signingUp && <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">ROLE</label>
              <select value={signupRole} onChange={e => setSignupRole(e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white"><option value="super_admin">Super Admin (full access)</option><option value="admin">Admin (restricted)</option><option value="dispatcher">Dispatcher (orders only)</option></select></div>}
            <button type="submit" className="w-full bg-[#FFC542] hover:bg-[#FFC542]/80 text-[#111] font-black py-3 rounded-xl transition-all shadow-lg flex items-center justify-center gap-2 text-sm tracking-wider">
              <Lock className="w-4 h-4" /> {signingUp ? "CREATE ADMIN" : "SIGN IN"}</button>
          </form>
          <div className="mt-6 text-center">
            <button type="button" onClick={() => setSigningUp(!signingUp)} className="text-xs text-[#FFC542] hover:underline font-semibold">
              {signingUp ? "Already have an account? Sign In" : "Create an admin account"}</button>
          </div>
        </div>
    </div>
    );
  }

  

    const categories = Array.from(new Set(deliveries.map(d => d.category).filter(Boolean)));
    const themes = ["gold", "white", "black"] as const;
    const defaultTiers = [
      { tag: "Express", title: "Express Deliveries", progress: 0, total: 0, unit: "drops", theme: "gold" as const },
      { tag: "Economy", title: "Economy Deliveries", progress: 0, total: 0, unit: "drops", theme: "white" as const },
      { tag: "Standard", title: "Standard Deliveries", progress: 0, total: 0, unit: "drops", theme: "black" as const },
      { tag: "Batch", title: "Batch Deliveries", progress: 0, total: 0, unit: "drops", theme: "gold" as const },
      { tag: "Multi", title: "Multi-Stop Deliveries", progress: 0, total: 0, unit: "drops", theme: "white" as const },
      { tag: "Cold Chain", title: "Cold Chain Deliveries", progress: 0, total: 0, unit: "drops", theme: "black" as const },
    ];
    const activeDeliveriesData = categories.length > 0 ? categories.map((cat, idx) => {
      const catDeliveries = deliveries.filter(d => d.category === cat);
      return {
        tag: cat,
        title: cat + " Deliveries",
        progress: catDeliveries.filter(d => d.status === "DELIVERED").length,
        total: catDeliveries.length,
        unit: "drops",
        theme: themes[idx % themes.length],
      };
    }) : defaultTiers;

  const allNavItems: { id: TabId; label: string; icon: React.ReactNode; roles: string[] }[] = [
    { id: "dashboard", label: "Dashboard", icon: <Folder size={22} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "shipments", label: "Shipments", icon: <Package size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "tracking", label: "Live Tracking", icon: <MapPin size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "users", label: "Users", icon: <Users size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "banners", label: "Hero Slides", icon: <ImageIcon size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "referrals", label: "Referrals", icon: <Gift size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "promotions", label: "Promotions", icon: <Percent size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "appcards", label: "App Cards", icon: <Layers size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "settings", label: "Settings", icon: <Settings size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "cms", label: "Site Content", icon: <FileText size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "logs", label: "Audit Log", icon: <Headphones size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
  ];
  const navItems = allNavItems.filter(n => n.roles.includes(userRole || "super_admin"));












function UsersTab({ activeUsers, searchQuery, db, addLog }: { activeUsers: UserProfile[]; searchQuery: string; db: any; addLog: any }) {
    const [search, setSearch] = useState("");
    const [editUser, setEditUser] = useState<UserProfile | null>(null);
    const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
    const [addingUser, setAddingUser] = useState(false);
    const [walletUser, setWalletUser] = useState<UserProfile | null>(null);
    const [fundAmount, setFundAmount] = useState("");
    const [funding, setFunding] = useState(false);
    const [saving, setSaving] = useState(false);

    const [form, setForm] = useState({
      name: "",
      email: "",
      role: "customer",
      phone: "",
      bikeNumber: "",
      status: "active",
      walletBalance: "0"
    });

    const [uPage, setUPage] = useState(0);
    const uPerPage = 20;
    const filtered = activeUsers.filter(u => {
      const q = (searchQuery || search).toLowerCase();
      return u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q) || u.phone.includes(q);
    });
    const uTotalPages = Math.max(1, Math.ceil(filtered.length / uPerPage));
    const pagedUsers = filtered.slice(uPage * uPerPage, (uPage + 1) * uPerPage);

    useEffect(() => { setUPage(0); }, [search, searchQuery]);

    const saveUser = async () => {
      if (!editUser) return;
      setSaving(true);
      try {
        await updateDoc(doc(db, "users", editUser.id), {
          name: form.name || editUser.name,
          role: form.role || editUser.role,
          phone: form.phone || editUser.phone,
          bikeNumber: form.bikeNumber || editUser.bikeNumber,
          status: form.status || editUser.status,
          walletBalance: parseFloat(form.walletBalance) || editUser.walletBalance || 0,
          updatedAt: Timestamp.now()
        });
        addLog("Update User", editUser.name + " -> " + (form.name || editUser.name));
        setEditUser(null);
      } catch (e: any) {
        console.error(e);
      }
      setSaving(false);
    };

    const createUser = async () => {
      setSaving(true);
      try {
        const id = "user_" + Math.random().toString(36).substring(2, 11);
        const data = {
          id: id,
          uid: id,
          name: form.name,
          email: form.email,
          role: form.role,
          phone: form.phone,
          bikeNumber: form.role === "rider" ? form.bikeNumber : "",
          status: form.status,
          walletBalance: parseFloat(form.walletBalance) || 0,
          loyaltyPoints: 0,
          deliveryCount: 0,
          rating: 5.0,
          isOnline: false,
          photoUrl: "",
          isDeleted: false,
          createdAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        };
        await setDoc(doc(db, "users", id), data);
        addLog("Create User", form.name + " (" + form.role + ")");
        setAddingUser(false);
      } catch (e: any) {
        console.error(e);
      }
      setSaving(false);
    };

    const fundWallet = async () => {
      if (!walletUser) return;
      const amt = parseFloat(fundAmount);
      if (isNaN(amt)) return;
      setFunding(true);
      try {
        const newBalance = (walletUser.walletBalance || 0) + amt;
        await updateDoc(doc(db, "users", walletUser.id), {
          walletBalance: newBalance,
          updatedAt: Timestamp.now()
        });
        addLog("Fund Wallet", walletUser.name + ": added ₦" + amt + " (New: ₦" + newBalance + ")");
        setWalletUser(null);
      } catch (e: any) {
        console.error(e);
      }
      setFunding(false);
    };

    const deleteUser = async (id: string) => {
      await updateDoc(doc(db, "users", id), { isDeleted: true, updatedAt: Timestamp.now() });
      addLog("Delete User", "Soft-deleted " + id);
      setConfirmDelete(null);
    };

    return (
      <div className="tab-content space-y-6">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div>
            <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2">
              <Users className="w-5 h-5 text-[#FFC542]" /> Users
            </h1>
            <p className="text-xs text-black/40 dark:text-white/40 mt-1">
              {filtered.length} total (page {uPage + 1}/{uTotalPages})
            </p>
          </div>
          <div className="flex items-center gap-3">
            <SearchInput value={search} onChange={setSearch} placeholder="Search users..." />
            <button
              onClick={() => {
                setAddingUser(true);
                setForm({
                  name: "",
                  email: "",
                  role: "customer",
                  phone: "",
                  bikeNumber: "",
                  status: "active",
                  walletBalance: "0"
                });
              }}
              className="px-4 py-2 bg-[#FFC542] text-[#111] hover:bg-[#FFC542]/80 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4" /> Add User
            </button>
          </div>
        </div>

        <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Name</th>
                  <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden md:table-cell">Email</th>
                  <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Wallet</th>
                  <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden lg:table-cell">Role</th>
                  <th className="text-right font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {pagedUsers.length === 0 && (
                  <tr>
                    <td colSpan={5} className="p-8 text-center text-black/40 dark:text-white/40">
                      No users found.
                    </td>
                  </tr>
                )}
                {pagedUsers.map((u, i) => (
                  <tr key={u.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-3">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 rounded-full bg-[#FFC542]/20 flex items-center justify-center text-xs font-black text-[#111] dark:text-white">
                          {u.name.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-bold text-[#111] dark:text-white">{u.name}</p>
                          <span className="text-[10px] text-black/40 dark:text-white/40">{u.phone || ""}</span>
                        </div>
                      </div>
                    </td>
                    <td className="p-3 hidden md:table-cell">
                      <span className="text-black/60 dark:text-white/60">{u.email}</span>
                    </td>
                    <td className="p-3 font-bold text-black dark:text-white">
                      ₦{(u.walletBalance || 0).toLocaleString()}
                    </td>
                    <td className="p-3 hidden lg:table-cell">
                      <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + rBadge(u.role)}>
                        {u.role.toUpperCase()}
                      </span>
                    </td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => {
                          setWalletUser(u);
                          setFundAmount("");
                        }}
                        title="Fund User Wallet"
                        className="p-2 text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 rounded-lg transition-colors inline-block"
                      >
                        <DollarSign className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => {
                          setEditUser(u);
                          setForm({
                            name: u.name,
                            email: u.email,
                            role: u.role,
                            phone: u.phone,
                            bikeNumber: u.bikeNumber || "",
                            status: u.status,
                            walletBalance: (u.walletBalance || 0).toString()
                          });
                        }}
                        className="p-2 text-[#FFC542] hover:bg-[#FFC542]/10 rounded-lg transition-colors inline-block"
                      >
                        <Edit3 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => setConfirmDelete(u.id)}
                        className="p-2 text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg transition-colors inline-block"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {uTotalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-2">
            <button
              onClick={() => setUPage(p => Math.max(0, p - 1))}
              disabled={uPage === 0}
              className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"
            >
              <ChevronLeft size={14} />
            </button>
            {Array.from({ length: uTotalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setUPage(i)}
                className={
                  "w-8 h-8 rounded-xl text-xs font-bold " +
                  (i === uPage
                    ? "bg-[#FFC542] text-[#111]"
                    : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")
                }
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => setUPage(p => Math.min(uTotalPages - 1, p + 1))}
              disabled={uPage >= uTotalPages - 1}
              className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"
            >
              <ChevronRight size={14} />
            </button>
          </div>
        )}

        <ConfirmModal
          show={confirmDelete !== null}
          title="Delete User"
          message="Soft-delete this user?"
          confirmLabel="Delete"
          onConfirm={() => deleteUser(confirmDelete!)}
          onCancel={() => setConfirmDelete(null)}
        />

        {/* Add User Modal */}
        {addingUser && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setAddingUser(false)}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                <UserPlus className="w-4 h-4 text-[#FFC542]" /> Add New User
              </h3>
              <div className="space-y-3">
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Full Name</label>
                  <input
                    value={form.name}
                    onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    placeholder="John Doe"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Email Address</label>
                  <input
                    type="email"
                    value={form.email}
                    onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    placeholder="john@example.com"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Phone Number</label>
                  <input
                    value={form.phone}
                    onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    placeholder="+234..."
                    required
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Role</label>
                    <select
                      value={form.role}
                      onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    >
                      <option value="customer">Customer</option>
                      <option value="rider">Rider</option>
                      <option value="admin">Admin</option>
                      <option value="super_admin">Super Admin</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Status</label>
                    <select
                      value={form.status}
                      onChange={e => setForm(f => ({ ...f, status: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    >
                      <option value="active">Active</option>
                      <option value="suspended">Suspended</option>
                    </select>
                  </div>
                </div>
                {form.role === "rider" && (
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Bike Number</label>
                    <input
                      value={form.bikeNumber}
                      onChange={e => setForm(f => ({ ...f, bikeNumber: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                      placeholder="ED-991"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Initial Wallet Balance (₦)</label>
                  <input
                    type="number"
                    value={form.walletBalance}
                    onChange={e => setForm(f => ({ ...f, walletBalance: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    placeholder="0"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  onClick={() => setAddingUser(false)}
                  className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600"
                >
                  Cancel
                </button>
                <SaveBtn onClick={createUser} label="Create User" loading={saving} />
              </div>
            </div>
          </div>
        )}

        {/* Edit User Modal */}
        {editUser && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setEditUser(null)}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                <Edit3 className="w-4 h-4 text-[#FFC542]" /> Edit User
              </h3>
              <div className="space-y-3">
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Full Name</label>
                  <input
                    value={form.name}
                    onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Phone Number</label>
                  <input
                    value={form.phone}
                    onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Role</label>
                    <select
                      value={form.role}
                      onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    >
                      <option value="customer">Customer</option>
                      <option value="rider">Rider</option>
                      <option value="admin">Admin</option>
                      <option value="super_admin">Super Admin</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Status</label>
                    <select
                      value={form.status}
                      onChange={e => setForm(f => ({ ...f, status: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    >
                      <option value="active">Active</option>
                      <option value="suspended">Suspended</option>
                    </select>
                  </div>
                </div>
                {form.role === "rider" && (
                  <div>
                    <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Bike Number</label>
                    <input
                      value={form.bikeNumber}
                      onChange={e => setForm(f => ({ ...f, bikeNumber: e.target.value }))}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Direct Wallet Balance (₦)</label>
                  <input
                    type="number"
                    value={form.walletBalance}
                    onChange={e => setForm(f => ({ ...f, walletBalance: e.target.value }))}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  onClick={() => setEditUser(null)}
                  className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600"
                >
                  Cancel
                </button>
                <SaveBtn onClick={saveUser} label="Update User" loading={saving} />
              </div>
            </div>
          </div>
        )}

        {/* Fund Wallet Modal */}
        {walletUser && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setWalletUser(null)}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-sm shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-[#FFC542]" /> Fund Wallet
              </h3>
              <div>
                <p className="text-xs text-black/60 dark:text-white/60">
                  Fund wallet balance for <strong>{walletUser.name}</strong>.
                </p>
                <p className="text-xs font-bold text-black/80 dark:text-white/80 mt-1">
                  Current Balance: ₦{(walletUser.walletBalance || 0).toLocaleString()}
                </p>
              </div>
              <div>
                <label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1 uppercase">Amount to Add (₦)</label>
                <input
                  type="number"
                  value={fundAmount}
                  onChange={e => setFundAmount(e.target.value)}
                  className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40"
                  placeholder="e.g. 5000 or -2000 to deduct"
                  required
                />
              </div>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  onClick={() => setWalletUser(null)}
                  className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600"
                >
                  Cancel
                </button>
                <button
                  onClick={fundWallet}
                  disabled={funding || !fundAmount}
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl text-xs font-bold transition-all disabled:opacity-50"
                >
                  {funding ? "Funding..." : "Confirm Fund"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
}


function ShipmentsTab({ deliveries, searchQuery, db, addLog }: { deliveries: Delivery[]; searchQuery: string; db: any; addLog: any }) {
    const [search, setSearch] = useState("");
    const [selected, setSelected] = useState<Set<string>>(new Set());
    const [bulkStatus, setBulkStatus] = useState("");
    const [page, setPage] = useState(0);
    const perPage = 20;
    const filtered = deliveries.filter(d => { const q = (searchQuery || search).toLowerCase(); return d.receiverName.toLowerCase().includes(q) || d.senderName.toLowerCase().includes(q) || d.id.includes(q) || (d.itemName && d.itemName.toLowerCase().includes(q)); });
    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const paged = filtered.slice(page * perPage, (page + 1) * perPage);
    useEffect(() => { setPage(0); }, [search, searchQuery]);
    const updateStatus = async (id: string, s: string) => { await updateDoc(doc(db, "deliveries", id), { status: s, updatedAt: Timestamp.now() }); addLog("Status", idShort(id) + " -> " + s); };
    const bulkUpdate = async () => {
      if (!bulkStatus || selected.size === 0) return;
      const batch = writeBatch(db); selected.forEach(id => batch.update(doc(db, "deliveries", id), { status: bulkStatus, updatedAt: Timestamp.now() })); await batch.commit();
      addLog("Bulk", selected.size + " deliveries -> " + bulkStatus); setBulkStatus(""); setSelected(new Set());
    };
    return <div className="tab-content space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Package className="w-5 h-5 text-[#FFC542]" /> Shipments</h1>
          <p className="text-xs text-black/40 dark:text-white/40 mt-1">{filtered.length} total (page {page + 1}/{totalPages})</p></div>
        <div className="flex items-center gap-3 flex-wrap"><SearchInput value={search} onChange={setSearch} placeholder="Search..." />
          {selected.size > 0 && <div className="flex items-center gap-2"><span className="text-[10px] font-bold text-black/40 dark:text-white/40">{selected.size} selected</span>
            <select value={bulkStatus} onChange={e => setBulkStatus(e.target.value)} className="bg-white dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white">
              <option value="">Bulk...</option>
              <option value="ASSIGNED">Assign</option><option value="TRANSIT">Transit</option><option value="OUT_FOR_DELIVERY">Out for delivery</option><option value="DELIVERED">Deliver</option><option value="CANCELLED">Cancel</option>
            </select>
            <SaveBtn onClick={bulkUpdate} label="Apply" />
            <button onClick={() => setSelected(new Set())} className="text-[10px] text-black/40 dark:text-white/40 hover:text-red-500 font-semibold">Clear</button>
          </div>}
        </div>
      </div>
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-xs"><thead className="bg-gray-50 dark:bg-[#222]">
            <tr><th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 w-8"><input type="checkbox" onChange={e => setSelected(e.target.checked ? new Set(paged.map(d => d.id)) : new Set())} className="rounded border-gray-300 text-[#FFC542] focus:ring-[#FFC542]" /></th>
              <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Item</th>
              <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden md:table-cell">Receiver</th>
              <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Status</th>
              <th className="text-right font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Price</th></tr>
          </thead><tbody className="divide-y divide-black/5 dark:divide-white/10">
            {paged.length === 0 && <tr><td colSpan={5} className="p-8 text-center text-black/40 dark:text-white/40">No shipments.</td></tr>}
            {paged.map((d, i) => <tr key={d.id} className={"hover:bg-black/5 dark:hover:bg-white/5 transition-colors animate-fade-in " + (selected.has(d.id) ? "bg-[#FFC542]/10" : "") + " " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6","stagger-7","stagger-8"][i] || "")}>
              <td className="p-3"><input type="checkbox" checked={selected.has(d.id)} onChange={() => { const s = new Set(selected); s.has(d.id) ? s.delete(d.id) : s.add(d.id); setSelected(s); }} className="rounded border-gray-300 text-[#FFC542] focus:ring-[#FFC542]" /></td>
              <td className="p-3"><p className="font-bold text-[#111] dark:text-white">{d.itemName || "Parcel"}</p><p className="text-[10px] text-black/40 dark:text-white/40">#{idShort(d.id)}</p></td>
              <td className="p-3 hidden md:table-cell"><span className="text-black/60 dark:text-white/60">{d.receiverName}</span></td>
              <td className="p-3">
                <select value={d.status} onChange={e => updateStatus(d.id, e.target.value)} className={"text-[10px] font-bold px-1.5 py-0.5 rounded-full border-0 " + sStyle(d.status)}>
                  <option value="PENDING">PENDING</option><option value="ASSIGNED">ASSIGNED</option><option value="TRANSIT">TRANSIT</option>
                  <option value="OUT_FOR_DELIVERY">OUT FOR DELIVERY</option><option value="DELIVERED">DELIVERED</option><option value="CANCELLED">CANCELLED</option>
                </select></td>
              <td className="p-3 text-right"><span className="font-bold text-[#111] dark:text-white">{fmt(d.price || 0)}</span>
                {d.tipAmount > 0 && <p className="text-[10px] text-green-600 dark:text-green-400">+{fmt(d.tipAmount)} tip</p>}</td>
            </tr>)}
          </tbody></table>
        </div>
      </div>
      {totalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronLeft size={14} /></button>
        {Array.from({ length: totalPages }, (_, i) => <button key={i} onClick={() => setPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold " + (i === page ? "bg-[#FFC542] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
        <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronRight size={14} /></button>
      </div>}
    </div>;
  }

function BannersTab({ banners, db, addLog, addToast }: BannersTabProps) {
  const [editBanner, setEditBanner] = useState<Banner | null>(null);
  const [bForm, setBForm] = useState({ title: "", subtitle: "", imageUrl: "", interval: 5, order: 0, active: true });
  const [saving, setSaving] = useState(false);
  const [activeIdx, setActiveIdx] = useState(0);
  const saveBanner = async (b?: Banner) => {
    setSaving(true);
    const data: any = { ...bForm, updatedAt: Timestamp.now() };
    try {
      if (b) { await updateDoc(doc(db, "banners", b.id), data); addLog("Update Banner", bForm.title); addToast("success", "Banner updated"); }
      else { await addDoc(collection(db, "banners"), data); addLog("Create Banner", bForm.title); addToast("success", "Banner created"); }
    } catch (e: any) { addToast("error", e.message); }
    setSaving(false); setEditBanner(null);
  };
  const deleteBanner = async (id: string) => { try { await deleteDoc(doc(db, "banners", id)); addLog("Delete Banner", id); addToast("success", "Banner deleted"); } catch (e: any) { addToast("error", e.message); } };
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><ImageIcon className="w-5 h-5 text-[#FFC542]" /> Hero Slides</h1>
        <p className="text-xs text-black/40 dark:text-white/40 mt-1">{banners.length} slides</p></div>
      <button onClick={() => { setEditBanner({ id: "", title: "", subtitle: "", imageUrl: "", interval: 5, order: banners.length, active: true }); setBForm({ title: "", subtitle: "", imageUrl: "", interval: 5, order: banners.length, active: true }); }}
        className="px-4 py-2 bg-[#FFC542] hover:bg-[#FFC542]/80 text-[#111] rounded-xl text-xs font-black shadow-sm transition-all flex items-center gap-2"><Plus className="w-3.5 h-3.5" /> New Slide</button>
    </div>
    <div className="relative w-full h-40 lg:h-56 rounded-3xl overflow-hidden bg-gray-200 dark:bg-gray-800 shadow-sm">
      {banners.length === 0 && <div className="absolute inset-0 flex items-center justify-center text-black/40 dark:text-white/40 text-xs font-bold">No slides yet.</div>}
      {banners.length > 0 && <>
        <div className="absolute inset-0 transition-all duration-500" style={{ backgroundImage: "url(" + banners[activeIdx]?.imageUrl + ")", backgroundSize: "cover", backgroundPosition: "center" }} />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 p-4 lg:p-6">
          <span className="inline-block bg-[#FFC542] text-[#111] text-[10px] font-black px-2 py-0.5 rounded-sm mb-2">ENGRACED</span>
          <h2 className="text-white font-black text-sm lg:text-lg leading-tight drop-shadow-lg">{banners[activeIdx]?.title}</h2>
          <p className="text-white/80 text-[10px] lg:text-xs mt-1 line-clamp-1">{banners[activeIdx]?.subtitle}</p>
        </div>
        <div className="absolute top-3 right-3 flex gap-1">
          {banners.map((_, i) => <button key={i} onClick={() => setActiveIdx(i)} className={"w-2 h-2 rounded-full transition-all " + (i === activeIdx ? "bg-[#FFC542] w-4" : "bg-white/50 hover:bg-white/80")} />)}
        </div>
      </>}
    </div>
    <div className="grid gap-3">
      {banners.map((b, i) => <div key={b.id} className={"animate-fade-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-4 shadow-sm hover:shadow-md hover:scale-[1.01] transition-all duration-300 flex items-center gap-4 " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5"][i] || "")}>
        <div className="w-20 h-14 rounded-xl bg-cover bg-center shrink-0 border border-black/10 dark:border-white/10" style={{ backgroundImage: "url(" + b.imageUrl + ")" }} />
        <div className="flex-1 min-w-0"><p className="text-xs font-bold text-[#111] dark:text-white truncate">{b.title || "Untitled"}</p>
          <p className="text-[10px] text-black/40 dark:text-white/40 truncate">{b.subtitle}</p>
          <span className="text-[10px] text-black/40 dark:text-white/40">Order: {b.order} | Interval: {b.interval}s | {b.active ? "Active" : "Inactive"}</span></div>
        <div className="flex items-center gap-1 shrink-0">
          <button onClick={() => { setEditBanner(b); setBForm({ title: b.title, subtitle: b.subtitle, imageUrl: b.imageUrl, interval: b.interval, order: b.order, active: b.active }); }} className="p-2 text-[#FFC542] hover:bg-[#FFC542]/10 rounded-lg"><Edit3 className="w-3.5 h-3.5" /></button>
          <button onClick={() => deleteBanner(b.id)} className="p-2 text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg"><Trash2 className="w-3.5 h-3.5" /></button>
        </div>
      </div>)}
    </div>
    {editBanner && <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setEditBanner(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><ImageIcon className="w-4 h-4 text-[#FFC542]" /> {editBanner.id ? "Edit" : "New"} Slide</h3>
        <div className="relative w-full h-32 rounded-2xl overflow-hidden bg-gray-200 dark:bg-gray-800">
          {bForm.imageUrl && <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(" + bForm.imageUrl + ")" }} />}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent" />
          <div className="absolute bottom-2 left-3">
            <span className="inline-block bg-[#FFC542] text-[#111] text-[8px] font-black px-1.5 py-0.5 rounded-sm mb-1">ENGRACED</span>
            <p className="text-white font-black text-xs drop-shadow-lg">{bForm.title || "Slide Title"}</p>
          </div>
        </div>
        <div className="grid sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">TITLE (max 30)</label>
            <input value={bForm.title} maxLength={30} onChange={e => setBForm(f => ({ ...f, title: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">SUBTITLE (max 80)</label>
            <input value={bForm.subtitle} maxLength={80} onChange={e => setBForm(f => ({ ...f, subtitle: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">IMAGE URL</label>
            <input value={bForm.imageUrl} onChange={e => setBForm(f => ({ ...f, imageUrl: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">ORDER</label>
            <input type="number" value={bForm.order} onChange={e => setBForm(f => ({ ...f, order: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">INTERVAL (sec)</label>
            <input type="number" value={bForm.interval} onChange={e => setBForm(f => ({ ...f, interval: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="inline-flex items-center gap-2 cursor-pointer"><input type="checkbox" checked={bForm.active} onChange={e => setBForm(f => ({ ...f, active: e.target.checked }))} className="rounded border-gray-300 text-[#FFC542] focus:ring-[#FFC542]" /><span className="text-xs text-gray-700 dark:text-gray-300 font-semibold">Active</span></label></div>
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setEditBanner(null)} className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600">Cancel</button>
          <SaveBtn onClick={() => saveBanner(editBanner.id ? editBanner : undefined)} loading={saving} />
        </div>
      </div>
    </div>}
  </div>;
}

function ReferralsTab({ referrals, completedReferrals, searchQuery }: ReferralsTabProps) {
  const [search, setSearch] = useState("");
  const filtered = referrals.filter(r => { const q = (searchQuery || search).toLowerCase(); return r.referrerName.toLowerCase().includes(q) || r.refereeName.toLowerCase().includes(q); });
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Gift className="w-5 h-5 text-[#FFC542]" /> Referrals</h1>
        <p className="text-xs text-black/40 dark:text-white/40 mt-1">{referrals.length} total | {completedReferrals.length} completed | {fmt(referrals.reduce((s, r) => s + r.rewardAmount, 0))} total rewards</p></div>
      <SearchInput value={search} onChange={setSearch} placeholder="Search..." />
    </div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-xs"><thead className="bg-gray-50 dark:bg-[#222]">
          <tr><th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Referrer</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden md:table-cell">Referee</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden lg:table-cell">Reward</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Status</th></tr>
        </thead><tbody className="divide-y divide-black/5 dark:divide-white/10">
          {filtered.map(r => <tr key={r.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
            <td className="p-3"><p className="font-bold text-[#111] dark:text-white">{r.referrerName}</p><span className="text-[10px] text-black/40 dark:text-white/40">{r.referrerEmail}</span></td>
            <td className="p-3 hidden md:table-cell"><p className="font-bold text-[#111] dark:text-white">{r.refereeName}</p><span className="text-[10px] text-black/40 dark:text-white/40">{r.refereeEmail}</span></td>
            <td className="p-3 hidden lg:table-cell"><span className="font-bold text-[#111] dark:text-white">{fmt(r.rewardAmount)}</span></td>
            <td className="p-3"><span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + (r.status === "completed" ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300")}>{r.status.toUpperCase()}</span></td>
          </tr>)}
        </tbody></table>
      </div>
    </div>
  </div>;
}

function PromotionsTab({ promotions, db, addLog, addToast }: PromotionsTabProps) {
  const [editPromo, setEditPromo] = useState<Promotion | null>(null);
  const [pForm, setPForm] = useState({ title: "", description: "", discountType: "percentage", discountValue: 0, discountDisplay: "", code: "", usageLimit: 0, minOrderAmount: 0, maxDiscount: 0, active: true });
  const [saving, setSaving] = useState(false);
  const savePromo = async (p?: Promotion) => {
    setSaving(true);
    const data: any = { ...pForm, updatedAt: Timestamp.now() };
    try {
      if (p) { await updateDoc(doc(db, "promotions", p.id), data); addLog("Update Promo", pForm.title); addToast("success", "Promotion updated"); }
      else { await addDoc(collection(db, "promotions"), data); addLog("Create Promo", pForm.title); addToast("success", "Promotion created"); }
    } catch (e: any) { addToast("error", e.message); }
    setSaving(false); setEditPromo(null);
  };
  const deletePromo = async (id: string) => { try { await deleteDoc(doc(db, "promotions", id)); addLog("Delete Promo", id); addToast("success", "Promotion deleted"); } catch (e: any) { addToast("error", e.message); } };
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Percent className="w-5 h-5 text-[#FFC542]" /> Promotions</h1>
        <p className="text-xs text-black/40 dark:text-white/40 mt-1">{promotions.length} active</p></div>
      <button onClick={() => { setEditPromo({ id: "", title: "", description: "", discountType: "percentage", discountValue: 0, discountDisplay: "", code: "", usageLimit: 0, usedCount: 0, minOrderAmount: 0, maxDiscount: 0, active: true }); setPForm({ title: "", description: "", discountType: "percentage", discountValue: 0, discountDisplay: "", code: "", usageLimit: 0, minOrderAmount: 0, maxDiscount: 0, active: true }); }}
        className="px-4 py-2 bg-[#FFC542] hover:bg-[#FFC542]/80 text-[#111] rounded-xl text-xs font-black shadow-sm transition-all flex items-center gap-2"><Plus className="w-3.5 h-3.5" /> New Promo</button>
    </div>
    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {promotions.length === 0 && <div className="sm:col-span-3 bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-black/40 dark:text-white/40">No promotions yet.</p></div>}
      {promotions.map((p, i) => {
        const dd = p.discountDisplay || (p.discountType === "percentage" ? p.discountValue + "% OFF" : "\u20A6" + p.discountValue.toLocaleString() + " OFF");
        return <div key={p.id} className={"animate-fade-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl overflow-hidden shadow-sm hover:shadow-md hover:scale-[1.02] transition-all duration-300 group " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i] || "")}>
          <div className="relative h-32 bg-gradient-to-br from-[#FFC542]/20 to-white dark:to-[#222] p-4 flex items-end">
            <div className="absolute top-3 right-3"><div className="relative"><div className="w-12 h-10 bg-[#FFC542]/80 rounded-md transform rotate-12" />
              <div className="w-10 h-8 bg-[#FFC542] rounded-md absolute -top-1 -left-1 transform -rotate-6 border border-[#FFC542]/50" />
              <div className="absolute -top-0.5 -left-0.5 text-[6px] font-black text-white px-1 bg-[#111] rounded-sm shadow-sm">ED</div></div></div>
            <div className="relative z-10">
              <div className="inline-block bg-[#111] dark:bg-white text-white dark:text-[#111] text-[10px] font-black px-2 py-0.5 rounded-sm mb-1.5 shadow-sm">{dd}</div>
              <h3 className="text-xs font-black text-[#111] dark:text-white">{p.title}</h3>
              <span className="inline-block mt-1 bg-[#FFC542]/20 text-[#111] dark:text-white text-[8px] font-bold px-1.5 py-0.5 rounded-sm border border-[#FFC542]/30">CODE: {p.code}</span>
            </div>
          </div>
          <div className="p-3 space-y-1.5">
            <p className="text-[10px] text-black/50 dark:text-white/50 line-clamp-2">{p.description}</p>
            <div className="flex items-center justify-between pt-1">
              <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + (p.active ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : "bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400")}>{p.active ? "Active" : "Inactive"}</span>
              <span className="text-[10px] text-black/40 dark:text-white/40">{p.usedCount}/{p.usageLimit} used</span>
            </div>
            <div className="flex items-center justify-end gap-1 pt-1 border-t border-black/10 dark:border-white/10">
              <button onClick={() => { setEditPromo(p); setPForm({ title: p.title, description: p.description, discountType: p.discountType, discountValue: p.discountValue, discountDisplay: p.discountDisplay, code: p.code, usageLimit: p.usageLimit, minOrderAmount: p.minOrderAmount, maxDiscount: p.maxDiscount, active: p.active }); }} className="p-1.5 text-[#FFC542] hover:bg-[#FFC542]/10 rounded-lg"><Edit3 className="w-3 h-3" /></button>
              <button onClick={() => deletePromo(p.id)} className="p-1.5 text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg"><Trash2 className="w-3 h-3" /></button>
            </div>
          </div>
        </div>;
      })}
    </div>
    {editPromo && <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setEditPromo(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5 max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><Percent className="w-4 h-4 text-[#FFC542]" /> {editPromo.id ? "Edit" : "New"} Promo</h3>
        <div className="relative h-24 bg-gradient-to-br from-[#FFC542]/20 to-white dark:to-[#222] rounded-2xl overflow-hidden border border-black/10 dark:border-white/10 p-3 flex items-end">
          <div className="absolute top-2 right-2"><div className="relative"><div className="w-10 h-8 bg-[#FFC542]/80 rounded-md transform rotate-12" />
            <div className="w-8 h-6 bg-[#FFC542] rounded-md absolute -top-1 -left-1 transform -rotate-6 border border-[#FFC542]/50" /></div></div>
          <div><div className="inline-block bg-[#111] dark:bg-white text-white dark:text-[#111] text-[8px] font-black px-1.5 py-0.5 rounded-sm mb-1">{pForm.discountDisplay || (pForm.discountType === "percentage" ? pForm.discountValue + "% OFF" : "\u20A6" + pForm.discountValue.toLocaleString() + " OFF")}</div>
            <p className="text-[10px] font-black text-[#111] dark:text-white">{pForm.title || "Promo Title"}</p>
            {pForm.code && <span className="inline-block mt-0.5 bg-[#FFC542]/20 text-[#111] dark:text-white text-[7px] font-bold px-1 py-0.5 rounded-sm">CODE: {pForm.code}</span>}
          </div>
        </div>
        <div className="grid sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">TITLE (max 25)</label>
            <input value={pForm.title} maxLength={25} onChange={e => setPForm(f => ({ ...f, title: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">DESCRIPTION (max 60)</label>
            <input value={pForm.description} maxLength={60} onChange={e => setPForm(f => ({ ...f, description: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">DISCOUNT DISPLAY</label>
            <input value={pForm.discountDisplay} maxLength={15} onChange={e => setPForm(f => ({ ...f, discountDisplay: e.target.value }))} placeholder="Auto-generated if empty" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">TYPE</label>
            <select value={pForm.discountType} onChange={e => setPForm(f => ({ ...f, discountType: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white"><option value="percentage">Percentage</option><option value="fixed">Fixed</option></select></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">VALUE</label>
            <input type="number" value={pForm.discountValue || ""} onChange={e => setPForm(f => ({ ...f, discountValue: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">CODE (max 12)</label>
            <input value={pForm.code} maxLength={12} onChange={e => setPForm(f => ({ ...f, code: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">USAGE LIMIT</label>
            <input type="number" value={pForm.usageLimit || ""} onChange={e => setPForm(f => ({ ...f, usageLimit: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">MIN ORDER (\u20A6)</label>
            <input type="number" value={pForm.minOrderAmount || ""} onChange={e => setPForm(f => ({ ...f, minOrderAmount: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">MAX DISCOUNT (\u20A6)</label>
            <input type="number" value={pForm.maxDiscount || ""} onChange={e => setPForm(f => ({ ...f, maxDiscount: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="inline-flex items-center gap-2 cursor-pointer"><input type="checkbox" checked={pForm.active} onChange={e => setPForm(f => ({ ...f, active: e.target.checked }))} className="rounded border-gray-300 text-[#FFC542] focus:ring-[#FFC542]" /><span className="text-xs text-gray-700 dark:text-gray-300 font-semibold">Active</span></label></div>
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setEditPromo(null)} className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600">Cancel</button>
          <SaveBtn onClick={() => savePromo(editPromo.id ? editPromo : undefined)} loading={saving} />
        </div>
      </div>
    </div>}
  </div>;
}

function AppCardsTab({ appContent, db, addLog, addToast }: AppCardsTabProps) {
  const [editCard, setEditCard] = useState<AppContent | null>(null);
  const [cForm, setCForm] = useState({ key: "", title: "", description: "", imageUrl: "", ctaText: "", ctaLink: "", order: 0, active: true });
  const [saving, setSaving] = useState(false);
  const saveCard = async (c?: AppContent) => {
    setSaving(true);
    const data: any = { ...cForm, updatedAt: Timestamp.now() };
    try {
      if (c) { await updateDoc(doc(db, "appContent", c.id), data); addLog("Update Card", cForm.title); addToast("success", "Card updated"); }
      else { await addDoc(collection(db, "appContent"), data); addLog("Create Card", cForm.title); addToast("success", "Card created"); }
    } catch (e: any) { addToast("error", e.message); }
    setSaving(false); setEditCard(null);
  };
  const deleteCard = async (id: string) => { try { await deleteDoc(doc(db, "appContent", id)); addLog("Delete Card", id); addToast("success", "Card deleted"); } catch (e: any) { addToast("error", e.message); } };
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><LayoutGrid className="w-5 h-5 text-[#FFC542]" /> App Cards</h1>
        <p className="text-xs text-black/40 dark:text-white/40 mt-1">{appContent.length} cards</p></div>
      <button onClick={() => { setEditCard({ id: "", key: "", title: "", description: "", imageUrl: "", ctaText: "", ctaLink: "", order: appContent.length, active: true }); setCForm({ key: "", title: "", description: "", imageUrl: "", ctaText: "", ctaLink: "", order: appContent.length, active: true }); }}
        className="px-4 py-2 bg-[#FFC542] hover:bg-[#FFC542]/80 text-[#111] rounded-xl text-xs font-black shadow-sm transition-all flex items-center gap-2"><Plus className="w-3.5 h-3.5" /> New Card</button>
    </div>
    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {appContent.length === 0 && <div className="sm:col-span-3 bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-black/40 dark:text-white/40">No cards yet.</p></div>}
      {appContent.map((c, i) => <div key={c.id} className={"animate-fade-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl overflow-hidden shadow-sm hover:shadow-md hover:scale-[1.02] transition-all duration-300 group " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i] || "")}>
        <div className="relative h-36 overflow-hidden"><div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(" + c.imageUrl + ")" }} />
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
          <div className="absolute bottom-2 left-3 right-3"><span className="inline-block bg-[#FFC542] text-[#111] text-[8px] font-black px-1.5 py-0.5 rounded-sm mb-1">{c.key}</span>
            <h3 className="text-white font-black text-xs drop-shadow-lg">{c.title}</h3></div>
        </div>
        <div className="p-3 space-y-1.5">
          <p className="text-[10px] text-black/50 dark:text-white/50 line-clamp-2">{c.description}</p>
          <div className="flex items-center justify-between pt-1 border-t border-black/10 dark:border-white/10">
            <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + (c.active ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : "bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400")}>{c.active ? "Active" : "Inactive"}</span>
            <span className="text-[10px] text-black/40 dark:text-white/40">Order {c.order}</span>
          </div>
          <div className="flex items-center justify-end gap-1">
            <button onClick={() => { setEditCard(c); setCForm({ key: c.key, title: c.title, description: c.description, imageUrl: c.imageUrl, ctaText: c.ctaText, ctaLink: c.ctaLink, order: c.order, active: c.active }); }} className="p-1.5 text-[#FFC542] hover:bg-[#FFC542]/10 rounded-lg"><Edit3 className="w-3 h-3" /></button>
            <button onClick={() => deleteCard(c.id)} className="p-1.5 text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg"><Trash2 className="w-3 h-3" /></button>
          </div>
        </div>
      </div>)}
    </div>
    {editCard && <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => setEditCard(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><LayoutGrid className="w-4 h-4 text-[#FFC542]" /> {editCard.id ? "Edit" : "New"} Card</h3>
        <div className="relative h-24 rounded-2xl overflow-hidden bg-gray-200 dark:bg-gray-800 border border-black/10 dark:border-white/10">
          {cForm.imageUrl && <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(" + cForm.imageUrl + ")" }} />}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent" />
          <div className="absolute bottom-2 left-3"><span className="inline-block bg-[#FFC542] text-[#111] text-[7px] font-black px-1 py-0.5 rounded-sm">{cForm.key || "key"}</span>
            <p className="text-white font-black text-[10px] drop-shadow-lg">{cForm.title || "Card Title"}</p></div>
        </div>
        <div className="grid sm:grid-cols-2 gap-3">
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">KEY (max 20)</label>
            <input value={cForm.key} maxLength={20} onChange={e => setCForm(f => ({ ...f, key: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFC542]/40" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">ORDER</label>
            <input type="number" value={cForm.order} onChange={e => setCForm(f => ({ ...f, order: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">TITLE (max 30)</label>
            <input value={cForm.title} maxLength={30} onChange={e => setCForm(f => ({ ...f, title: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">DESCRIPTION (max 80)</label>
            <input value={cForm.description} maxLength={80} onChange={e => setCForm(f => ({ ...f, description: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">IMAGE URL</label>
            <input value={cForm.imageUrl} onChange={e => setCForm(f => ({ ...f, imageUrl: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">CTA TEXT</label>
            <input value={cForm.ctaText} onChange={e => setCForm(f => ({ ...f, ctaText: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">CTA LINK</label>
            <input value={cForm.ctaLink} onChange={e => setCForm(f => ({ ...f, ctaLink: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="inline-flex items-center gap-2 cursor-pointer"><input type="checkbox" checked={cForm.active} onChange={e => setCForm(f => ({ ...f, active: e.target.checked }))} className="rounded border-gray-300 text-[#FFC542] focus:ring-[#FFC542]" /><span className="text-xs text-gray-700 dark:text-gray-300 font-semibold">Active</span></label></div>
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setEditCard(null)} className="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600">Cancel</button>
          <SaveBtn onClick={() => saveCard(editCard.id ? editCard : undefined)} loading={saving} />
        </div>
      </div>
    </div>}
  </div>;
}

function SettingsTab({ db, addLog }: SettingsTabProps) {
  const [sForm, setSForm] = useState({ allowSignups: true, requireApproval: true, defaultRole: "customer", maintenanceMode: false, appVersion: "1.0.0", contactEmail: "", supportPhone: "", playStoreUrl: "", appStoreUrl: "" });
  const [saving, setSaving] = useState(false);
  useEffect(() => {
    const unsub = onSnapshot(doc(db, "system_config", "global_settings"), snap => { if (snap.exists()) setSForm(snap.data() as any); });
    return unsub;
  }, []);
  const saveSettings = async () => {
    setSaving(true); await setDoc(doc(db, "system_config", "global_settings"), { ...sForm, updatedAt: Timestamp.now() }, { merge: true }); addLog("Update Settings", "General"); setSaving(false);
  };
  return <div className="tab-content space-y-6">
    <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Settings2 className="w-5 h-5 text-[#FFC542]" /> Settings</h1>
      <p className="text-xs text-black/40 dark:text-white/40 mt-1">System preferences</p></div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-5 shadow-sm space-y-5">
      <div className="grid sm:grid-cols-2 gap-4">
        <div className="flex items-center justify-between p-3 bg-gray-50/50 dark:bg-white/5 rounded-2xl"><div><p className="text-xs font-bold text-[#111] dark:text-white">Allow Signups</p><p className="text-[10px] text-black/40 dark:text-white/40">Enable new user registration</p></div>
          <label className="relative inline-flex items-center cursor-pointer"><input type="checkbox" checked={sForm.allowSignups} onChange={e => setSForm(f => ({ ...f, allowSignups: e.target.checked }))} className="sr-only peer" />
            <div className="w-9 h-5 bg-gray-200 dark:bg-gray-600 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[#FFC542]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-[#FFC542]"></div></label></div>
        <div className="flex items-center justify-between p-3 bg-gray-50/50 dark:bg-white/5 rounded-2xl"><div><p className="text-xs font-bold text-[#111] dark:text-white">Require Approval</p><p className="text-[10px] text-black/40 dark:text-white/40">Admin must approve new accounts</p></div>
          <label className="relative inline-flex items-center cursor-pointer"><input type="checkbox" checked={sForm.requireApproval} onChange={e => setSForm(f => ({ ...f, requireApproval: e.target.checked }))} className="sr-only peer" />
            <div className="w-9 h-5 bg-gray-200 dark:bg-gray-600 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[#FFC542]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-[#FFC542]"></div></label></div>
        <div className="flex items-center justify-between p-3 bg-gray-50/50 dark:bg-white/5 rounded-2xl"><div><p className="text-xs font-bold text-[#111] dark:text-white">Maintenance Mode</p><p className="text-[10px] text-black/40 dark:text-white/40">Disable app access for users</p></div>
          <label className="relative inline-flex items-center cursor-pointer"><input type="checkbox" checked={sForm.maintenanceMode} onChange={e => setSForm(f => ({ ...f, maintenanceMode: e.target.checked }))} className="sr-only peer" />
            <div className="w-9 h-5 bg-gray-200 dark:bg-gray-600 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[#FFC542]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-red-500"></div></label></div>
        <div className="flex items-center justify-between p-3 bg-gray-50/50 dark:bg-white/5 rounded-2xl"><div><p className="text-xs font-bold text-[#111] dark:text-white">Default Role</p></div>
          <select value={sForm.defaultRole} onChange={e => setSForm(f => ({ ...f, defaultRole: e.target.value }))} className="bg-white dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-1.5 text-xs text-[#111] dark:text-white"><option value="customer">Customer</option><option value="rider">Rider</option><option value="admin">Admin</option></select></div>
        <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">APP VERSION</label>
          <input value={sForm.appVersion} onChange={e => setSForm(f => ({ ...f, appVersion: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">CONTACT EMAIL</label>
          <input value={sForm.contactEmail} onChange={e => setSForm(f => ({ ...f, contactEmail: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">SUPPORT PHONE</label>
          <input value={sForm.supportPhone} onChange={e => setSForm(f => ({ ...f, supportPhone: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">PLAY STORE URL</label>
          <input value={sForm.playStoreUrl} onChange={e => setSForm(f => ({ ...f, playStoreUrl: e.target.value }))} placeholder="https://play.google.com/store/apps/details?id=..." className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-bold text-black/40 dark:text-white/40 mb-1">APP STORE URL</label>
          <input value={sForm.appStoreUrl} onChange={e => setSForm(f => ({ ...f, appStoreUrl: e.target.value }))} placeholder="https://apps.apple.com/app/id..." className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
      </div>
      <div className="flex justify-end pt-2 border-t border-black/10 dark:border-white/10"><SaveBtn onClick={saveSettings} loading={saving} /></div>
    </div>
  </div>;
}

function LogsTab({ logs }: LogsTabProps) {
  const [typeFilter, setTypeFilter] = useState("all");
  const [lPage, setLPage] = useState(0);
  const lPerPage = 20;
  const filtered = typeFilter === "all" ? logs : logs.filter(l => l.action.includes(typeFilter));
  const lTotalPages = Math.max(1, Math.ceil(filtered.length / lPerPage));
  const pagedLogs = filtered.slice(lPage * lPerPage, (lPage + 1) * lPerPage);
  useEffect(() => { setLPage(0); }, [typeFilter]);
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><FileText className="w-5 h-5 text-[#FFC542]" /> Audit Log</h1>
        <p className="text-xs text-black/40 dark:text-white/40 mt-1">{filtered.length} entries (page {lPage + 1}/{lTotalPages})</p></div>
      <select value={typeFilter} onChange={e => setTypeFilter(e.target.value)} className="bg-white dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white"><option value="all">All Actions</option><option value="Create">Create</option><option value="Update">Update</option><option value="Delete">Delete</option><option value="Toggle">Toggle</option><option value="Login">Login</option></select>
    </div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-xs"><thead className="bg-gray-50 dark:bg-[#222]">
          <tr><th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Action</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden md:table-cell">Detail</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10 hidden lg:table-cell">Admin</th>
            <th className="text-left font-bold text-black/40 dark:text-white/40 p-3 border-b border-black/10 dark:border-white/10">Date</th></tr>
        </thead><tbody className="divide-y divide-black/5 dark:divide-white/10">
          {pagedLogs.length === 0 && <tr><td colSpan={4} className="p-8 text-center text-black/40 dark:text-white/40">No entries.</td></tr>}
          {pagedLogs.map(l => <tr key={l.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
            <td className="p-3"><span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + (l.action.startsWith("Create") ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : l.action.startsWith("Delete") ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300" : l.action.startsWith("Update") ? "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300" : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300")}>{l.action}</span></td>
            <td className="p-3 hidden md:table-cell"><span className="text-[#111] dark:text-white font-semibold">{l.details}</span></td>
            <td className="p-3 hidden lg:table-cell"><span className="text-black/60 dark:text-white/60">{l.admin}</span></td>
            <td className="p-3"><span className="text-black/40 dark:text-white/40 text-[10px]">{new Date(l.timestamp).toLocaleDateString()}</span></td>
          </tr>)}
        </tbody></table>
      </div>
    </div>
    {lTotalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
      <button onClick={() => setLPage(p => Math.max(0, p - 1))} disabled={lPage === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronLeft size={14} /></button>
      {Array.from({ length: lTotalPages }, (_, i) => <button key={i} onClick={() => setLPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold " + (i === lPage ? "bg-[#FFC542] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
      <button onClick={() => setLPage(p => Math.min(lTotalPages - 1, p + 1))} disabled={lPage >= lTotalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronRight size={14} /></button>
    </div>}
  </div>;
}



function TrackingTab({ deliveries }: { deliveries: Delivery[] }) {
  const [trackSearch, setTrackSearch] = useState("");
  const activeD = deliveries.filter(d => d.status !== "DELIVERED" && d.status !== "CANCELLED");
  const filtered = trackSearch ? activeD.filter(d => d.id.includes(trackSearch) || d.receiverName.toLowerCase().includes(trackSearch.toLowerCase()) || d.itemName?.toLowerCase().includes(trackSearch.toLowerCase())) : activeD;
  const [tPage, setTPage] = useState(0);
  const tPerPage = 10;
  const tTotalPages = Math.max(1, Math.ceil(filtered.length / tPerPage));
  const pagedT = filtered.slice(tPage * tPerPage, (tPage + 1) * tPerPage);
  useEffect(() => { setTPage(0); }, [trackSearch]);
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><MapPin className="w-5 h-5 text-[#FFC542]" /> Live Tracking</h1><p className="text-xs text-black/40 dark:text-white/40 mt-1">{activeD.length} active deliveries</p></div>
      <SearchInput value={trackSearch} onChange={setTrackSearch} placeholder="Search by ID, name, item..." />
    </div>
    <div className="grid gap-4">
      {pagedT.length === 0 && <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-black/40 dark:text-white/40">No active deliveries.</p></div>}
      {pagedT.map(d => {
        const step = sIdx[d.status] || 0;
        return <div key={d.id} className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-5 shadow-sm animate-fade-in">
          <div className="flex items-start justify-between mb-4 flex-wrap gap-2">
            <div><p className="font-bold text-[#111] dark:text-white">{d.itemName || "Parcel"}</p>
              <p className="text-[10px] text-black/40 dark:text-white/40">#{idShort(d.id)} • {d.receiverName} → {d.deliveryAddress}</p></div>
            <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + sStyle(d.status)}>{d.status.replace(/_/g, " ")}</span>
          </div>
          <div className="relative mt-4 mb-2">
            <div className="flex items-center justify-between mb-2">
              {statusSteps.map((label, i) => <div key={label} className="flex flex-col items-center gap-1 relative z-10">
                <div className={"w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold " + (i <= step ? "bg-[#FFC542] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-black/30 dark:text-white/30")}>
                  {i < step ? <Check size={12} /> : i + 1}
                </div>
                <span className={"text-[8px] font-semibold whitespace-nowrap " + (i <= step ? "text-[#111] dark:text-white" : "text-black/30 dark:text-white/30")}>{label}</span>
              </div>)}
            </div>
            <div className="absolute top-3 left-3 right-3 h-0.5 bg-gray-100 dark:bg-[#222] rounded-full">
              <div className="h-full bg-[#FFC542] rounded-full transition-all duration-500" style={{ width: `${(step / Math.max(1, statusSteps.length - 1)) * 100}%` }} />
            </div>
          </div>
        </div>;
      })}
    </div>
    {tTotalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
      <button onClick={() => setTPage(p => Math.max(0, p - 1))} disabled={tPage === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronLeft size={14} /></button>
      {Array.from({ length: tTotalPages }, (_, i) => <button key={i} onClick={() => setTPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold " + (i === tPage ? "bg-[#FFC542] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
      <button onClick={() => setTPage(p => Math.min(tTotalPages - 1, p + 1))} disabled={tPage >= tTotalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronRight size={14} /></button>
    </div>}
  </div>;
}

  return <div className="flex h-screen bg-[#111] overflow-hidden">
    <ToastContainer toasts={toasts} />
    {mobileSidebar && <div className="fixed inset-0 bg-black/60 z-40 lg:hidden" onClick={() => setMobileSidebar(false)} />}
    <Sidebar 
      sidebar={sidebar} setSidebar={setSidebar} tab={tab} setTab={setTab} 
      mobileSidebar={mobileSidebar} setMobileSidebar={setMobileSidebar} 
      navItems={navItems} 
    />
    <main className="flex-1 flex flex-col bg-white dark:bg-[#050505] rounded-3xl m-4 ml-0 overflow-hidden">
      <Header 
        searchQuery={searchQuery} setSearchQuery={setSearchQuery} unreadCount={unreadCount} 
        setShowNotifs={setShowNotifs} showNotifs={showNotifs} setShowUserMenu={setShowUserMenu} 
        showUserMenu={showUserMenu} currentUser={currentUser} userRole={userRole} toggleDark={toggleDark} dark={dark} 
        setMobileSidebar={setMobileSidebar} notifications={notifications} markNotifRead={markNotifRead} 
      />
      <div className="flex-1 overflow-y-auto px-4 sm:px-8 lg:px-12 pb-10 pt-6">
         {tab === "dashboard" && <DashboardTab 
            deliveries={deliveries} 
            activeUsers={activeUsers} 
            customers={customers} 
            drivers={drivers} 
            pendingDeliveries={pendingDeliveries} 
            delivered={delivered} 
            totalRevenue={totalRevenue} 
            totalTips={totalTips} 
            referrals={referrals} 
            activeDeliveriesData={activeDeliveriesData} 
            fmt={fmt} 
            seedUsers={seedUsersWrapper} 
            seedDeliveries={seedDeliveriesWrapper} 
            seedBanners={seedBannersWrapper} 
            seedPromos={seedPromosWrapper} 
            seedReferrals={seedReferralsWrapper} 
            seedAppContent={seedAppContentWrapper} 
            seeding={seeding}
            setTab={setTab}
          />}
        {tab === "users" && <UsersTab activeUsers={activeUsers} searchQuery={searchQuery} db={db} addLog={addLog} />}
        {tab === "shipments" && <ShipmentsTab deliveries={deliveries} searchQuery={searchQuery} db={db} addLog={addLog} />}
        {tab === "tracking" && <TrackingTab deliveries={deliveries} />}
        {tab === "banners" && <BannersTab banners={banners} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "referrals" && <ReferralsTab referrals={referrals} completedReferrals={completedReferrals} searchQuery={searchQuery} />}
        {tab === "promotions" && <PromotionsTab promotions={promotions} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "appcards" && <AppCardsTab appContent={appContent} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "settings" && <SettingsTab db={db} addLog={addLog} />}
        {tab === "cms" && <CMSTab db={db} addLog={addLog} />}
        {tab === "logs" && <LogsTab logs={logs} />}
      </div>
    </main>
    </div>;
  }

export default function AdminDashboardWrapper() {
  return <AdminDashboardPage />;
}

