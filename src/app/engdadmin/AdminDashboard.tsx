"use client";
import React, { useState, useEffect, useCallback, useRef, useId, useMemo } from "react";
import { createPortal } from "react-dom";

async function firestoreRetry<T>(fn: () => Promise<T>, maxRetries = 3, delay = 2000): Promise<T> {
  let lastError: unknown;
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await fn();
    } catch (err) {
      lastError = err;
      if (attempt < maxRetries) await new Promise(r => setTimeout(r, delay));
    }
  }
  throw lastError;
}

function useOnlineStatus() {
  const [online, setOnline] = React.useState(typeof navigator !== "undefined" ? navigator.onLine : true);
  React.useEffect(() => {
    if (typeof window === "undefined") return;
    const go = () => setOnline(true);
    const gl = () => setOnline(false);
    window.addEventListener("online", go);
    window.addEventListener("offline", gl);
    return () => { window.removeEventListener("online", go); window.removeEventListener("offline", gl); };
  }, []);
  return online;
}
import { auth, db, getSecondaryAuth } from "@/lib/firebase";
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, signOut, onAuthStateChanged } from "firebase/auth";
import { collection, query, onSnapshot, doc, updateDoc, setDoc, deleteDoc, where, Timestamp, getDoc, getDocs, writeBatch, addDoc, increment } from "firebase/firestore";
import { Shield, Truck, Package, ShoppingBag, Store, Users, Settings, Activity, Lock, Mail, Key, CheckCircle, CheckCircle2, AlertTriangle, Plus, Trash2, LogOut, Search, Sliders, Award, DollarSign, Zap, Globe, UserPlus, BarChart3, MapPin, ShieldAlert, Image as ImageIcon, Menu, X, ShieldCheck, RefreshCw, UserCheck, UserX, Clock, TrendingUp, Edit3, Copy, Check, Percent, Gift, Star, Layers, Eye, EyeOff, Calendar, ChevronDown, ChevronUp, Phone, AtSign, Hash, Save, Bell, Send, ChevronLeft, ChevronRight, Bookmark, Folder, FileCheck, MessageSquare, Headphones, Settings2, LayoutGrid, FileText, Moon, Sun, Pencil, Repeat, Printer, Power, Wrench, Database, Tag } from "lucide-react";
import CMSTab from "./CMSTab";
import LiveTrackingMap from "./LiveTrackingMap";
import { SoundEngine } from "@/lib/interaction/SoundEngine";
import { StatusBadge } from "@/components/design-system/StatusBadge";
import { RouteDisplay } from "@/components/design-system/RouteDisplay";
import { PriceDisplay } from "@/components/design-system/PriceDisplay";
import { DispatchDecisionDrawer } from "@/components/design-system/DispatchDecisionDrawer";
import { NotificationLifecycleManager } from "@/components/design-system/NotificationLifecycle";
import { ShipmentMicroPage } from "@/components/design-system/ShipmentMicroPage";
type TabId = "dashboard" | "marketplace" | "users" | "shipments" | "banners" | "referrals" | "promotions" | "appcards" | "settings" | "logs" | "cms" | "tracking" | "support";
interface UserProfile { id: string; uid: string; name: string; email: string; phone: string; role: string; status: string; isOnline: boolean; rating: number; deliveryCount: number; walletBalance: number; loyaltyPoints: number; photoUrl: string; bikeNumber?: string; lat?: number; lng?: number; isDeleted?: boolean; updatedAt?: any; }
interface Delivery {
  id: string;
  status: string;
  category?: string;
  receiverName: string;
  deliveryAddress: string;
  senderName: string;
  senderPhone: string;
  receiverPhone: string;
  price: number;
  deliveryFee?: number;
  riderId: string;
  courierName: string;
  courierPhone: string;
  courierLatitude?: number;
  courierLongitude?: number;
  itemName: string;
  pickupAddress: string;
  quantity: number;
  weight: number;
  dateString: string;
  tipAmount: number;
  userId: string;
  otpCode: string;
  riderBikeNumber?: string;
  pickupLat?: number;
  pickupLng?: number;
  deliveryLat?: number;
  deliveryLng?: number;
  driverId?: string;
  driverName?: string;
  createdAt?: any;
  updatedAt?: any;
  reservedRiderId?: string;
  reservedCourierName?: string;
  reservedCourierPhone?: string;
  paymentStatus?: string;
  podUrl?: string;
  exceptionType?: string;
  exceptionReason?: string;
}
interface Banner { id: string; title: string; subtitle: string; imageUrl: string; interval: number; order: number; active: boolean; }
interface Referral { id: string; referrerId: string; referrerName: string; referrerEmail: string; refereeId: string; refereeName: string; refereeEmail: string; rewardAmount: number; status: string; }
interface Promotion { id: string; title: string; description: string; discountType: string; discountValue: number; discountDisplay: string; minOrderAmount: number; maxDiscount: number; code: string; usageLimit: number; usedCount: number; active: boolean; }
interface AuditEntry { id: string; time: string; action: string; details: string; admin: string; timestamp: number; }

interface Product {
  id: string;
  name: string;
  category: string;
  price: number;
  stock: number;
  imageUrl: string;
  status: "In Stock" | "Low Stock" | "Out of Stock";
  description: string;
  vendorStore: string;
  rating: number;
  isDeleted?: boolean;
}

interface VendorStore {
  id: string;
  storeName: string;
  ownerName: string;
  email: string;
  phone: string;
  category: string;
  commissionRate: number;
  status: "PENDING" | "APPROVED" | "REJECTED";
  dateEnlisted: string;
  vendorBalance?: number;
  description?: string;
  address?: string;
  logoUrl?: string;
  coverUrl?: string;
  ownerId?: string;
  storeRating?: number;
  totalSales?: number;
  isVerified?: boolean;
  isFeatured?: boolean;
  featuredRank?: number;
  isDemo?: boolean;
  isDeleted?: boolean;
}

interface MarketplaceOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  storeName: string;
  itemsCount: number;
  totalPrice: number;
  status: "PAID" | "FULFILLED" | "CANCELLED" | "SETTLED";
  date: string;
}

interface VendorPayoutRequest {
  id: string;
  vendorId: string;
  storeName?: string;
  amount: number;
  bankName: string;
  accountNumber: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  requestedAt?: any;
}

interface AppContent { id: string; key: string; title: string; description: string; imageUrl: string; ctaText: string; ctaLink: string; order: number; active: boolean; }
interface Toast { id: number; type: "success" | "error" | "info"; message: string; }
interface BannersTabProps { banners: Banner[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface ReferralsTabProps { referrals: Referral[]; completedReferrals: Referral[]; searchQuery: string; }
interface PromotionsTabProps { promotions: Promotion[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface AppCardsTabProps { appContent: AppContent[]; db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void; }
interface SettingsTabProps {
  db: any;
  addLog: (a: string, d: string) => Promise<void> | void;
  addToast?: (t: Toast["type"], m: string) => void;
  seedUsers?: () => Promise<void>;
  seedDeliveries?: () => Promise<void>;
  seedBanners?: () => Promise<void>;
  seedPromos?: () => Promise<void>;
  seedReferrals?: () => Promise<void>;
  seedAppContent?: () => Promise<void>;
  seedMarketplace?: () => Promise<void>;
  seeding?: string;
}
interface LogsTabProps { logs: AuditEntry[]; }

function EdLogoSvg({ size = 36, className = "", dark = false }: { size?: number; className?: string; dark?: boolean }) {
  const s = size;
  const c1 = dark ? "#1a1a1a" : "#FFB800";
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
    case "vendor": return "bg-[#FFB800]/25 text-[#111] dark:text-[#FFB800]";
    case "admin": return "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300";
    case "super_admin": return "bg-[#FFB800]/20 text-[#111] dark:text-white";
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

function getLifecycleStageIndex(status: string): number {
  const norm = (status || "").toUpperCase().trim();
  switch (norm) {
    case "PENDING":
    case "RECEIVED":
    case "QUEUED":
      return 0;
    case "RESERVED_NEXT":
    case "ASSIGNED":
      return 1;
    case "PICKED_UP":
    case "TRANSIT":
      return 2;
    case "ARRIVED":
    case "OUT_FOR_DELIVERY":
    case "HANDOVER_VERIFIED":
      return 3;
    case "DELIVERED":
      return 4;
    default:
      return 0;
  }
}

function StatCard({ icon, label, value, sub }: { icon: React.ReactNode; label: string; value: string; sub: string }) {
  return <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200">
    <div className="flex items-center justify-between"><span className="text-xs font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider">{label}</span>{icon}</div>
    <h2 className="text-3xl font-black text-gray-900 dark:text-white mt-2">{value}</h2>
    <p className="text-[11px] text-gray-700 dark:text-[#FFB800] font-semibold mt-1">{sub}</p>
  </div>;
}
function QuickBtn({ label, desc, onClick, loading = false, variant = "default" }: { label: string; desc: string; onClick: () => void; loading?: boolean; variant?: "default" | "warning" | "danger" | "setup" }) {
  const borderClr = variant === "danger"
    ? "hover:border-red-500/50 hover:bg-red-500/10"
    : variant === "warning"
    ? "hover:border-amber-500/50 hover:bg-amber-500/10"
    : "hover:border-[#FFB800]/50 hover:bg-[#FFB800]/10";
  const titleClr = variant === "danger"
    ? "text-red-600 dark:text-red-400"
    : variant === "warning"
    ? "text-amber-600 dark:text-amber-400"
    : "text-gray-900 dark:text-[#FFB800]";
  return <button onClick={onClick} disabled={loading} className={"p-4 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-2xl text-left transition-all " + borderClr + " " + (loading ? "opacity-50 cursor-not-allowed" : "")}>
    <p className={"text-xs font-bold " + titleClr}>{loading ? "PROCESSING..." : label}</p><p className="text-[10px] text-gray-600 dark:text-gray-400 mt-1 font-medium">{desc}</p>
  </button>;
}
function Section({ title, children, className }: { title: string; children: React.ReactNode; className?: string }) {
  return <div className={"bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 shadow-sm " + (className || "")}>
    <h3 className="text-xs font-black text-gray-900 dark:text-[#FFB800] tracking-wider uppercase flex items-center gap-2">{title}</h3>
    <div className="mt-4">{children}</div>
  </div>;
}
function InlineEdit({ value, onSave, type = "text" }: { value: string; onSave: (v: string) => void; type?: string }) {
  const [editing, setEditing] = useState(false);
  const [val, setVal] = useState(value);
  useEffect(() => setVal(value), [value]);
  if (!editing) return <span onClick={() => setEditing(true)} className="cursor-pointer hover:bg-[#FFB800]/10 px-1.5 py-0.5 rounded group inline-flex items-center gap-1.5 -ml-1.5 transition-colors text-gray-900 dark:text-white font-medium">{value || "—"} <Edit3 className="w-3 h-3 text-[#FFB800]/0 group-hover:text-amber-700 dark:group-hover:text-[#FFB800]" /></span>;
  return <input type={type} value={val} onChange={e => setVal(e.target.value)} onBlur={() => { onSave(val); setEditing(false); }} onKeyDown={e => { if (e.key === "Enter") { onSave(val); setEditing(false); } if (e.key === "Escape") { setVal(value); setEditing(false); }}} className="bg-white dark:bg-[#222] border border-[#FFB800]/50 rounded-xl px-2 py-1 text-sm text-gray-900 dark:text-white w-full shadow-sm" autoFocus />;
}
function ConfirmModal({ show, title, message, confirmLabel, onConfirm, onCancel }: { show: boolean; title: string; message: string; confirmLabel?: string; onConfirm: () => void; onCancel: () => void }) {
  if (!show) return null;
  return <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
    <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4">
      <h3 className="text-lg font-black text-gray-900 dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-red-500" /> {title}</h3>
      <p className="text-sm text-gray-700 dark:text-gray-300">{message}</p>
      <div className="flex items-center justify-end gap-3 pt-2">
        <button onClick={onCancel} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold transition-colors">Cancel</button>
        <button onClick={onConfirm} className="px-4 py-2.5 min-h-[38px] bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-black transition-colors">{confirmLabel || "Confirm"}</button>
      </div>
    </div>
  </div>;
}
function SearchInput({ value, onChange, placeholder = "Search..." }: { value: string; onChange: (v: string) => void; placeholder?: string }) {
  return <div className="relative group">
    <Search className="absolute left-3 inset-y-0 my-auto w-4 h-4 text-gray-500 dark:text-gray-400 group-focus-within:text-amber-700 dark:group-focus-within:text-[#FFB800] transition-colors" />
    <input type="text" value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}
      className="pl-9 pr-4 py-2.5 bg-white dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 focus:border-[#FFB800]/60 w-full transition-all" />
  </div>;
}
function SaveBtn({ onClick, label = "Save", loading = false, size = "sm" }: { onClick: () => void; label?: string; loading?: boolean; size?: "sm" | "md" }) {
  const s = size === "md" ? "px-5 py-2.5 text-xs" : "px-4 py-2.5 text-xs";
  return <button onClick={onClick} disabled={loading}
    className={"inline-flex items-center gap-1.5 " + s + " bg-[#FFB800] hover:bg-[#FFB800]/80 disabled:bg-[#FFB800]/40 text-[#111] rounded-xl font-black shadow-sm hover:shadow-md transition-all min-h-[38px]"}>
    {loading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />} {label}
  </button>;
}

function Select({ value, onChange, options, placeholder = "Select...", className = "", compact = false, renderOption }: {
  value: string; onChange: (v: string) => void; options: { value: string; label: string; color?: string; disabled?: boolean }[];
  placeholder?: string; className?: string; compact?: boolean;
  renderOption?: (opt: { value: string; label: string; color?: string; disabled?: boolean }, selected: boolean) => React.ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const containerRef = React.useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [open]);
  const selected = options.find(o => o.value === value);
  return <div ref={containerRef} className={"relative " + className}>
    <button type="button" onClick={() => setOpen(!open)}
      className={"w-full flex items-center justify-between gap-1.5 bg-white dark:bg-[#1c1c1c] border border-gray-300 dark:border-white/15 rounded-xl text-gray-900 dark:text-white hover:border-gray-400 dark:hover:border-white/30 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 focus:border-[#FFB800]/60 transition-all text-left " + (compact ? "px-2 py-1 text-[10px]" : "px-3 py-2.5 text-xs min-h-[38px]")}>
      <span className={"truncate " + (selected ? "font-medium" : "text-gray-400 dark:text-gray-500")}>{selected ? selected.label : placeholder}</span>
      <ChevronDown className={"w-3.5 h-3.5 shrink-0 text-gray-500 dark:text-gray-400 transition-transform " + (open ? "rotate-180" : "")} />
    </button>
    {open && <div className="absolute top-full left-0 mt-1.5 w-full min-w-[160px] z-[100] bg-white dark:bg-[#1a1a1a] rounded-2xl p-1.5 shadow-2xl border border-gray-200 dark:border-white/15 overflow-hidden animate-scale-in">
      <div className="max-h-60 overflow-y-auto space-y-0.5">
        {options.map(o => (
          <button key={o.value} type="button" disabled={o.disabled} onClick={() => { if (!o.disabled) { onChange(o.value); setOpen(false); } }}
            className={"w-full text-left flex items-center gap-2.5 px-3 py-2 rounded-xl transition-colors " + (compact ? "text-[10px]" : "text-xs ") + (o.disabled ? "opacity-40 cursor-not-allowed text-gray-400 dark:text-gray-500" : o.value === value ? "bg-[#FFB800]/20 text-gray-900 dark:text-white font-bold" : "text-gray-800 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-white/10")}>
            {renderOption ? renderOption(o, o.value === value) : <span>{o.label}</span>}
            {o.value === value && <Check className="w-3.5 h-3.5 ml-auto text-amber-800 dark:text-[#FFB800]" />}
          </button>
        ))}
      </div>
    </div>}
  </div>;
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
      { receiverName: "Osasere Igbinedion", deliveryAddress: "14 Ihama Road, GRA, Benin City", senderName: "Shopify Hub", senderPhone: "08011122233", receiverPhone: "08044455566", price: 2500, riderId: "seed_u1", courierName: couriers[0], courierPhone: "08012345678", itemName: "Laptop", pickupAddress: "Ring Road, Kings Square, Benin City", quantity: 1, weight: 1.5, dateString: ds(-1), tipAmount: 500, userId: "seed_u2", otpCode: "1234", status: "DELIVERED", category: "Express" },
      { receiverName: "Nosakhare Edokpayi", deliveryAddress: "Airport Road, GRA, Benin City", senderName: "Amazon Hub", senderPhone: "08099988877", receiverPhone: "08077766655", price: 1500, riderId: "seed_u3", courierName: couriers[1], courierPhone: "08055544433", itemName: "Books", pickupAddress: "Ekehuan Road, UNIBEN Ekehuan, Benin City", quantity: 2, weight: 0.8, dateString: ds(0), tipAmount: 200, userId: "seed_u2", otpCode: "5678", status: "TRANSIT", category: "Standard" },
      { receiverName: "Efosa Osagie", deliveryAddress: "Ugbowo Campus, University of Benin, Benin City", senderName: "Fresh Foods", senderPhone: "08022233344", receiverPhone: "08055566677", price: 3000, riderId: "seed_u1", courierName: couriers[2], courierPhone: "08066677788", itemName: "Groceries", pickupAddress: "Sapele Road, Benin City", quantity: 5, weight: 4.2, dateString: ds(0), tipAmount: 0, userId: "seed_u2", otpCode: "9012", status: "PENDING", category: "Cold Chain" },
      { receiverName: "Ekiuwa Omoruyi", deliveryAddress: "UBTH, Ugbowo, Benin City", senderName: "Jumia Hub", senderPhone: "08033344455", receiverPhone: "08088899900", price: 1800, riderId: "seed_u3", courierName: couriers[3], courierPhone: "08011122200", itemName: "Documents", pickupAddress: "Mission Road, Benin City", quantity: 1, weight: 0.3, dateString: ds(1), tipAmount: 100, userId: "seed_u2", otpCode: "3456", status: "DELIVERED", category: "Economy" },
      { receiverName: "Efe Martins", deliveryAddress: "Ikpoba Hill, Benin City", senderName: "PharmaCo", senderPhone: "08055566688", receiverPhone: "08099900011", price: 4200, riderId: "", courierName: couriers[4], courierPhone: "08022233300", itemName: "Medical Supplies", pickupAddress: "Upper Sakponba Road, Benin City", quantity: 3, weight: 6.0, dateString: ds(2), tipAmount: 0, userId: "seed_u2", otpCode: "7890", status: "ASSIGNED", category: "Express" },
      { receiverName: "Aisosa Obasuyi", deliveryAddress: "Boundary Road, GRA, Benin City", senderName: "MegaMart", senderPhone: "08077788899", receiverPhone: "08011122255", price: 3500, riderId: "", courierName: couriers[5], courierPhone: "08044455500", itemName: "Home Appliances", pickupAddress: "Akpakpava Road, Benin City", quantity: 2, weight: 8.0, dateString: ds(1), tipAmount: 300, userId: "seed_u2", otpCode: "2345", status: "TRANSIT", category: "Batch" },
      { receiverName: "Isoken Agho", deliveryAddress: "New Lagos Road, Benin City", senderName: "PrintHub", senderPhone: "08033322211", receiverPhone: "08066655544", price: 2200, riderId: "seed_u1", courierName: couriers[0], courierPhone: "08012345678", itemName: "Print Materials", pickupAddress: "Siluko Road, Benin City", quantity: 4, weight: 2.5, dateString: ds(3), tipAmount: 150, userId: "seed_u2", otpCode: "6789", status: "PENDING", category: "Multi" },
      { receiverName: "Amenze Osunde", deliveryAddress: "Country Home Motel Road, Benin City", senderName: "TechWorld", senderPhone: "08044455566", receiverPhone: "08077788822", price: 2800, riderId: "seed_u3", courierName: couriers[1], courierPhone: "08055544433", itemName: "Smartphone", pickupAddress: "Adesuwa Road, GRA, Benin City", quantity: 1, weight: 0.6, dateString: ds(4), tipAmount: 250, userId: "seed_u2", otpCode: "1111", status: "PENDING", category: "Standard" },
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
    const content = { referral: { benefitText: "Invite your friends and earn \u20A6500 in wallet credit for each successful referral!", referrerCode: "", reward: 500, active: true }, aiAssistant: { title: "Dispatch Assistant", description: "Need help with your delivery? Our AI assistant is here 24/7.", tag: "Powered by HeyTek AI", active: true }, welcomeGift: { title: "Welcome to ESDispatch!", credit: 2500, coins: 10, active: true }, weatherTraffic: { optimalMessage: "Light traffic conditions — perfect timing.", congestedMessage: "Heavy traffic on major routes — expect 15-20 min delays.", optimalBadge: "Smooth Sailing", congestedBadge: "Heavy Traffic", active: true }, loyalty: { bronzeThreshold: 10, silverThreshold: 25, goldThreshold: 50, platinumThreshold: 100, ordersForBronze: 3, ordersForSilver: 5, ordersForGold: 10, dailyBonus: 25, active: true }, statsConfig: { promoSavingsPerBooking: 3500, statLabels: ["Deliveries", "Saved", "Earned", "Redeemed"], active: true } };
    await setDoc(doc(db, "system_config", "global_settings"), { appContent: content, updatedAt: Timestamp.now() }, { merge: true });
    setSettings((prev: any) => ({ ...prev, appContent: content })); addLog("Seeded", "Default app card content"); addToast("success", "App content seeded"); createNotification("App Content Seeded", "Default dashboard content configured");
  } catch (e: any) { addToast("error", "App content seed failed: " + e.message); }
}


async function seedMarketplace(db: any, addLog: any, addToast: any, createNotification: any) {
  try {
    const batch = writeBatch(db);
    const today = new Date().toISOString().slice(0, 10);
    // Deterministic owner ids so products link to storefronts in the mobile app
    const storeOwners = [
      { id: "seed_store_esdispatch", storeName: "ESDispatch Fleet Supplies", ownerName: "Official Store", email: "supplies@engraced.com", phone: "08012345678", category: "Fleet Equipment", commissionRate: 5, description: "Official ESDispatch fleet equipment, rider kits and dispatch consumables for the whole team.", address: "17 Upper Adesuwa Road, GRA, Benin City" },
      { id: "seed_store_prorider", storeName: "ProRider Wear", ownerName: "Chidi Okonkwo", email: "chidi@prorider.com", phone: "08087654321", category: "Apparel", commissionRate: 10, description: "Premium reflective courier jackets, riding apparel and protective wear engineered for Nigerian roads.", address: "14 Ihama Road, GRA, Benin City" },
      { id: "seed_store_autocare", storeName: "Benin Auto Care", ownerName: "Osagie Adebayo", email: "osagie@autocare.ng", phone: "08055544433", category: "Lubricants", commissionRate: 8, description: "High-performance engine oils, brake pads and workshop supplies for fleet maintenance.", address: "45 Airport Road, GRA, Benin City" },
      { id: "seed_store_safetyfirst", storeName: "SafetyFirst Nigeria", ownerName: "Amina Yusuf", email: "amina@safetyfirst.ng", phone: "08099900011", category: "Safety", commissionRate: 11, description: "Certified helmets, high-visibility vests and DOT-approved rider safety equipment.", address: "88 Sapele Road, Benin City" },
      { id: "seed_store_techrider", storeName: "TechRider Solutions", ownerName: "Efosa Adewale", email: "efosa@techrider.ng", phone: "08044455667", category: "Electronics", commissionRate: 12, description: "Smartphone mounts, power banks, dashcams and rider tech accessories.", address: "12 Mission Road, Kings Square, Benin City" },
    ];
    const productSeeds = [
      { name: "Heavy Duty Bike Delivery Box", category: "Delivery Gear", price: 35000, stock: 45, imageUrl: "https://images.unsplash.com/photo-1580674285054-bed31e145f59?w=500", status: "In Stock", description: "Waterproof insulated delivery cargo box with lock", vendorStore: "ESDispatch Fleet Supplies", vendorId: "seed_store_esdispatch", rating: 4.9 },
      { name: "Executive Courier Rider Jacket", category: "Apparel", price: 18500, stock: 120, imageUrl: "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500", status: "In Stock", description: "High-visibility reflective windproof jacket", vendorStore: "ProRider Wear", vendorId: "seed_store_prorider", rating: 4.8 },
      { name: "Engine Synthetic Oil 1L (4T)", category: "Lubricants", price: 4200, stock: 8, imageUrl: "https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=500", status: "Low Stock", description: "High performance synthetic motor oil", vendorStore: "Benin Auto Care", vendorId: "seed_store_autocare", rating: 4.7 },
      { name: "Heavy Duty Phone Mount & Charger", category: "Electronics", price: 9500, stock: 65, imageUrl: "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=500", status: "In Stock", description: "Anti-vibration aluminum handlebar phone holder", vendorStore: "TechRider Solutions", vendorId: "seed_store_techrider", rating: 4.9 },
      { name: "Full Face Protective Helmet", category: "Safety", price: 28000, stock: 0, imageUrl: "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=500", status: "Out of Stock", description: "DOT certified aerodynamic protective helmet", vendorStore: "SafetyFirst Nigeria", vendorId: "seed_store_safetyfirst", rating: 4.9 },
      { name: "Reflective Safety Vest", category: "Safety", price: 6500, stock: 30, imageUrl: "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=500", status: "In Stock", description: "High-visibility neon vest for night-time dispatch", vendorStore: "SafetyFirst Nigeria", vendorId: "seed_store_safetyfirst", rating: 4.8 },
      { name: "Chrome Helmet Visor", category: "Safety", price: 3200, stock: 22, imageUrl: "https://images.unsplash.com/photo-1573871669400-9a27b80c3c9c?w=500", status: "In Stock", description: "Anti-fog scratch resistant visor for full-face helmets", vendorStore: "SafetyFirst Nigeria", vendorId: "seed_store_safetyfirst", rating: 4.7 },
    ];

    storeOwners.forEach(s => batch.set(doc(db, "marketplace_stores", s.id), {
      id: s.id, ownerId: s.id, storeName: s.storeName, ownerName: s.ownerName, email: s.email,
      phone: s.phone, category: s.category, description: s.description, address: s.address, logoUrl: "",
      coverUrl: "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&fit=crop",
      commissionRate: s.commissionRate, vendorBalance: 0, totalSales: 0, storeRating: 5.0,
      status: "APPROVED", isVerified: true, isPendingReview: false, kycStatus: "approved",
      isFeatured: false, featuredRank: 0, isDemo: true, isDeleted: false,
      verificationNote: "Seeded by ESDispatch admin (demo)", dateEnlisted: today,
      createdAt: Timestamp.now(), verifiedAt: Timestamp.now(), updatedAt: Timestamp.now()
    }));
    productSeeds.forEach(p => batch.set(doc(collection(db, "marketplace_products")), { ...p, createdAt: Timestamp.now(), updatedAt: Timestamp.now() }));

    await batch.commit();
    addLog("Seeded", "5 sample stores and 7 products for marketplace");
    addToast("success", "Marketplace seeded with stores & catalog");
    createNotification("Marketplace Seeded", "Sample storefronts and products added");
  } catch (e: any) {
    addToast("error", "Marketplace seed failed: " + e.message);
  }
}

function DashboardTab({ deliveries, activeUsers, customers, drivers, pendingDeliveries, delivered, totalRevenue, totalTips, referrals, activeDeliveriesData, fmt, setTab, setShipmentsFilterPrefill, marketplaceEnabled, toggleMarketplace, onOpenShipmentFullView }: any) {
  const todayStr = new Date().toISOString().slice(0, 10);
  const isToday = (d: any) => {
    if (!d.dateString) return true;
    return d.dateString.startsWith("Today") || d.dateString.includes(todayStr) || d.dateString === "";
  };
  const recentDeliveries = deliveries
    .filter((d: any) => d.status !== "DELIVERED" && d.status !== "CANCELLED" && isToday(d))
    .sort((a: any, b: any) => {
      const tA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.createdAt ? new Date(a.createdAt).getTime() : 0);
      const tB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.createdAt ? new Date(b.createdAt).getTime() : 0);
      return tA - tB;
    });
  const [filterCat, setFilterCat] = useState("all");
  const [inspectingCategory, setInspectingCategory] = useState<any | null>(null);
  const [modalStatusFilter, setModalStatusFilter] = useState<"PENDING" | "ASSIGNED" | "TRANSIT" | "ARRIVED" | "ALL">("PENDING");
  const [inlineAssigningId, setInlineAssigningId] = useState<string | null>(null);

  const handleInlineAssignRider = async (deliveryId: string, rider: any) => {
    try {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        riderId: rider.id,
        driverId: rider.id,
        driverName: rider.name,
        courierName: rider.name,
        courierPhone: rider.phone || "",
        riderBikeNumber: rider.bikeNumber || "",
        status: "ASSIGNED",
        updatedAt: Timestamp.now()
      });
      const del = deliveries.find((x: any) => x.id === deliveryId);
      if (del?.userId) {
        try {
          const notifRef = doc(collection(db, "users", del.userId, "notifications"));
          await setDoc(notifRef, {
            id: notifRef.id,
            title: "Rider Assigned!",
            message: `${rider.name} (${rider.phone || "Courier"}) has been assigned to your shipment #${idShort(deliveryId)}.`,
            time: "Just now",
            isRead: false,
            parcelId: deliveryId,
            createdAt: Timestamp.now()
          });
        } catch (_) {}
      }
    } catch (err: any) {
      console.error("Failed to assign rider:", err);
      alert("Could not assign rider: " + err.message);
    }
  };

  const handleInlineStatusChange = async (deliveryId: string, newStatus: string) => {
    const del = deliveries.find((x: any) => x.id === deliveryId);
    const hasRider = !!(del?.riderId || del?.driverId || (del?.courierName && del.courierName !== "Unassigned"));
    if (!hasRider && (newStatus === "TRANSIT" || newStatus === "OUT_FOR_DELIVERY" || newStatus === "DELIVERED" || newStatus === "ARRIVED")) {
      alert("Operational Guard: Assign a rider before updating status to " + newStatus.replace(/_/g, " ") + ".");
      return;
    }
    try {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        status: newStatus,
        updatedAt: Timestamp.now()
      });
      if (del?.userId) {
        try {
          const statusMessages: Record<string, string> = {
            ASSIGNED: "A courier has been assigned and will be picking up your package shortly.",
            PICKED_UP: "Your package has been picked up by our courier.",
            TRANSIT: "Your package is on the way to the delivery address.",
            ARRIVED: "The courier has arrived at the delivery address.",
            OUT_FOR_DELIVERY: "The courier is approaching the final delivery location.",
            HANDOVER_VERIFIED: "Delivery handover has been verified with security code.",
            DELIVERED: "Your package has been safely delivered. Thank you for choosing ESDispatch!",
            CANCELLED: "This delivery booking has been cancelled.",
          };
          if (statusMessages[newStatus]) {
            const notifRef = doc(collection(db, "users", del.userId, "notifications"));
            await setDoc(notifRef, {
              id: notifRef.id,
              title: newStatus === "DELIVERED" ? "Package Delivered" : `Shipment ${newStatus.replace(/_/g, " ")}`,
              message: statusMessages[newStatus],
              time: "Just now",
              isRead: false,
              parcelId: deliveryId,
              createdAt: Timestamp.now()
            });
          }
        } catch (_) {}
      }
    } catch (err: any) {
      console.error("Failed to update status:", err);
      alert("Could not update status: " + err.message);
    }
  };

  const serviceIcon = (tag: string, cls: string, size = 18) => {
    switch(tag) {
      case "Express": return <Zap size={size} className={cls} strokeWidth={2.2} />;
      case "Economy": return <DollarSign size={size} className={cls} strokeWidth={2.2} />;
      case "Standard": return <Package size={size} className={cls} strokeWidth={2.2} />;
      case "Batch": return <Layers size={size} className={cls} strokeWidth={2.2} />;
      case "Multi": return <MapPin size={size} className={cls} strokeWidth={2.2} />;
      case "Cold Chain": return <Shield size={size} className={cls} strokeWidth={2.2} />;
      default: return <Package size={size} className={cls} strokeWidth={2.2} />;
    }
  };

  const displayDeliveriesData = filterCat === "all" ? activeDeliveriesData : activeDeliveriesData.filter((a: any) => a.tag === filterCat);

  return <div className="tab-content space-y-8">
      <div className={`p-4 rounded-3xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all ${
        marketplaceEnabled 
          ? "bg-emerald-500/10 border-emerald-500/25 text-emerald-950 dark:text-emerald-300" 
          : "bg-red-500/10 border-red-500/25 text-red-950 dark:text-red-300"
      }`}>
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
            <Store className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-black uppercase tracking-wider">Marketplace & Storefront App Visibility:</span>
              <span className={`text-[10px] font-black px-2.5 py-0.5 rounded-full ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
                {marketplaceEnabled ? "LIVE ON APP" : "HIDDEN ON APP"}
              </span>
            </div>
            <p className="text-[11px] opacity-80 mt-0.5">
              {marketplaceEnabled 
                ? "Customer mobile app displays vendor store catalogs, verified shops carousel, and merchant enrollment. Click button to immediately turn off." 
                : "Marketplace is completely hidden on mobile. Mobile dashboard hero button dynamically converts to 'Live Tracking' radar."}
            </p>
          </div>
        </div>
        <button
          onClick={toggleMarketplace}
          className={`px-4 py-2 rounded-2xl text-xs font-black shrink-0 transition-all shadow-sm flex items-center gap-2 ${
            marketplaceEnabled ? "bg-red-500 hover:bg-red-600 text-white" : "bg-emerald-500 hover:bg-emerald-600 text-white"
          }`}
        >
          <Power className="w-3.5 h-3.5" />
          {marketplaceEnabled ? "Turn OFF Marketplace" : "Turn ON Marketplace"}
        </button>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="stagger-1"><StatCard icon={<Users className="w-4 h-4 text-[#FFB800]" />} label="TOTAL USERS" value={activeUsers.length.toString()} sub={customers.length + " customers · " + drivers.length + " drivers"} /></div>
        <div className="stagger-2"><StatCard icon={<Package className="w-4 h-4 text-[#FFB800]" />} label="SHIPMENTS" value={deliveries.length.toString()} sub={pendingDeliveries.length + " pending · " + delivered.length + " delivered"} /></div>
        <div className="stagger-3"><StatCard icon={<DollarSign className="w-4 h-4 text-[#FFB800]" />} label="REVENUE" value={fmt(totalRevenue)} sub={fmt(totalTips) + " in tips · " + referrals.length + " referrals"} /></div>
        <div className="stagger-4"><StatCard icon={<Activity className="w-4 h-4 text-[#FFB800]" />} label="ONLINE" value={(activeUsers.filter((u: any) => u.isOnline).length).toString()} sub={drivers.filter((d: any) => d.isOnline).length + " drivers · " + (activeUsers.filter((u: any) => u.role === "customer" && u.isOnline).length) + " customers"} /></div>
      </div>

      <section>
        <div className="flex justify-between items-end mb-4 flex-wrap gap-4">
          <div>
            <h1 className="text-xl font-extrabold tracking-tight text-[#111] dark:text-white">Active Deliveries for Today</h1>
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-0.5">Real-time scheduled drop-offs across active service categories</p>
          </div>
        </div>

        {activeDeliveriesData.length === 0 ? (
          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-8 text-center shadow-sm">
            <div className="w-12 h-12 rounded-2xl bg-gray-100 dark:bg-white/10 mx-auto mb-3 flex items-center justify-center text-gray-500 dark:text-gray-400">
              <Package className="w-6 h-6" />
            </div>
            <p className="text-sm font-black text-[#111] dark:text-white">No Active Deliveries Today</p>
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1 max-w-sm mx-auto">Active dispatches booked by customers on mobile or Web will appear here in real-time categorized by service tier.</p>
            <div className="mt-4 flex items-center justify-center gap-2.5">
              <button onClick={() => setTab("shipments")} className="px-4 py-2 bg-[#FFB800] text-[#111] font-black text-xs rounded-xl shadow-sm hover:bg-[#FFB800]/80 transition-all cursor-pointer">
                View All Shipments
              </button>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-4 gap-5">
            {displayDeliveriesData.map((a: any, i: number) => {
              const isGold = a.theme === "gold";
              const isBlack = a.theme === "black";
              
              const cardBg = isGold 
                ? "bg-[#FFB800] border border-[#FFB800]/60 shadow-sm hover:shadow-xl text-[#111]" 
                : isBlack 
                ? "bg-[#111] border border-white/15 text-white shadow-sm hover:shadow-xl" 
                : "bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-gray-900 dark:text-white shadow-sm hover:shadow-xl";
                
              const tagPill = isGold 
                ? "bg-[#111] text-[#FFB800]" 
                : isBlack 
                ? "bg-white text-[#111]" 
                : "bg-gray-100 dark:bg-white/10 text-gray-900 dark:text-white border border-gray-200 dark:border-white/10";
                
              const titleColor = isGold 
                ? "text-[#111]" 
                : isBlack 
                ? "text-white" 
                : "text-gray-900 dark:text-white";
                
              const subColor = isGold 
                ? "text-black/85 font-bold" 
                : isBlack 
                ? "text-white/80 font-medium" 
                : "text-gray-600 dark:text-gray-300 font-medium";
                
              const trackBg = isGold ? "bg-black/20" : isBlack ? "bg-white/20" : "bg-gray-200 dark:bg-white/10";
              const fillBg = isGold ? "bg-[#111]" : isBlack ? "bg-[#FFB800]" : "bg-[#111] dark:bg-[#FFB800]";
              
              const btnStyle = isGold 
                ? "bg-[#111] text-white hover:bg-black/90" 
                : isBlack 
                ? "bg-[#FFB800] text-[#111] hover:bg-[#FFB800]/90" 
                : "bg-gray-900 text-white dark:bg-white dark:text-[#111] hover:opacity-90";
                
              const ringColor = isGold ? "ring-[#FFB800]" : isBlack ? "ring-[#111]" : "ring-white dark:ring-[#1a1a1a]";
              const pct = a.total > 0 ? Math.round((a.progress / a.total) * 100) : 0;
              const cardCouriers = (a.deliveries || []).filter((d: any) => d.courierName && d.courierName !== "Unassigned").slice(0, 3);
              const extraCount = Math.max(0, (a.deliveries || []).filter((d: any) => d.courierName && d.courierName !== "Unassigned").length - 3);

              return <div key={i} className={"animate-fade-in transition-all duration-200 " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i % 6])}>
                <div 
                  onClick={() => {
                    setInspectingCategory(a);
                    setModalStatusFilter(a.pending > 0 ? "PENDING" : "ALL");
                  }}
                  className={cardBg + " rounded-3xl p-5 sm:p-6 flex flex-col justify-between relative group cursor-pointer transition-all duration-200 hover:-translate-y-1 min-h-[305px] h-full"}
                >
                  <div>
                    <div className="flex justify-between items-start mb-3">
                      <span className={tagPill + " text-[10px] font-black px-3 py-1 rounded-full z-10 shadow-xs uppercase tracking-wider"}>
                        {a.tag}
                      </span>
                      <div className={(isGold ? "bg-black/10 text-[#111]" : isBlack ? "bg-white/10 text-white" : "bg-gray-100 dark:bg-white/10 text-gray-900 dark:text-white") + " p-2.5 rounded-2xl z-10"}>
                        {serviceIcon(a.tag, isGold ? "text-[#111]" : isBlack ? "text-white" : "text-gray-900 dark:text-white", 18)}
                      </div>
                    </div>
                    <h3 className={"text-lg font-black leading-tight mb-1 z-10 " + titleColor}>
                      {a.title}
                    </h3>
                    <p className={"text-xs z-10 line-clamp-1 " + subColor}>
                      {a.total} total scheduled • {a.inTransit || 0} active on road
                    </p>
                  </div>

                  <div className="z-10 py-3 my-auto">
                    <div className={"flex justify-between items-end font-black text-xs mb-2 " + titleColor}>
                      <span>In Transit</span>
                      <div className="flex items-center gap-2">
                        <span className="font-extrabold">{a.progress}/{a.total}</span>
                        {a.pending > 0 && (
                          <span className={"px-2 py-0.5 rounded-full text-[10px] font-black shadow-xs " + (isGold ? "bg-black text-[#FFB800]" : "bg-[#FFB800] text-[#111]")}>
                            {a.pending} pending
                          </span>
                        )}
                      </div>
                    </div>
                    <div className={"w-full h-2.5 rounded-full overflow-hidden " + trackBg}>
                      <div className={"h-full rounded-full transition-all duration-500 " + fillBg} style={{ width: `${pct}%` }} />
                    </div>
                  </div>

                  <div className={"z-10 pt-3 border-t " + (isGold ? "border-black/15" : isBlack ? "border-white/15" : "border-gray-200 dark:border-white/10")}>
                    <div className="flex justify-between items-center mb-3">
                      <div className="flex items-center gap-2">
                        <span className={"text-[10px] font-extrabold uppercase tracking-wider " + subColor}>Couriers:</span>
                        <div className="flex -space-x-1.5 relative">
                          {cardCouriers.length > 0 ? (
                            cardCouriers.map((c: any, j: number) => (
                              <div 
                                key={j} 
                                title={c.courierName} 
                                className={"w-7 h-7 rounded-full ring-2 " + ringColor + " " + (isGold ? "bg-[#111] text-white" : isBlack ? "bg-[#FFB800] text-[#111]" : "bg-[#FFB800] text-[#111]") + " text-[10px] font-black flex items-center justify-center shadow-xs"}
                              >
                                {(c.courierName || "R").charAt(0).toUpperCase()}
                              </div>
                            ))
                          ) : (
                            <div className={"w-7 h-7 rounded-full ring-2 " + ringColor + " " + (isGold ? "bg-black/15 text-[#111]" : "bg-gray-100 dark:bg-white/10 text-gray-500 dark:text-gray-400 border border-gray-200 dark:border-white/5") + " text-[10px] font-bold flex items-center justify-center"}>
                              —
                            </div>
                          )}
                          {extraCount > 0 && (
                            <div 
                              title={`+${extraCount} more couriers`} 
                              className={"w-7 h-7 rounded-full ring-2 " + ringColor + " " + (isGold ? "bg-[#111] text-white" : isBlack ? "bg-[#FFB800] text-[#111]" : "bg-[#FFB800] text-[#111]") + " text-[10px] font-black flex items-center justify-center shadow-xs"}
                            >
                              +{extraCount}
                            </div>
                          )}
                        </div>
                      </div>
                      <span className={"text-xs font-black " + titleColor}>{pct}%</span>
                    </div>

                    <button 
                      onClick={(e) => { 
                        e.stopPropagation(); 
                        setInspectingCategory(a); 
                        setModalStatusFilter(a.pending > 0 ? "PENDING" : "ALL");
                      }} 
                      className={btnStyle + " w-full py-2.5 px-4 rounded-xl text-xs font-black shadow-sm active:scale-[0.98] transition-all flex items-center justify-center gap-1.5 cursor-pointer"}
                    >
                      <Eye size={14} /> View Queue ({a.total})
                    </button>
                  </div>
                </div>
              </div>;
            })}
          </div>
        )}

      {inspectingCategory && (() => {
        const categoryDeliveries = deliveries
          .filter((d: any) => (d.category || "General") === inspectingCategory.tag && d.status !== "DELIVERED" && d.status !== "CANCELLED")
          .sort((a: any, b: any) => {
            const tA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.createdAt ? new Date(a.createdAt).getTime() : 0);
            const tB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.createdAt ? new Date(b.createdAt).getTime() : 0);
            return tA - tB;
          });

        const pendingList = categoryDeliveries.filter((d: any) => 
          d.status === "PENDING" || !d.status || (!d.riderId && !d.driverId)
        );
        const assignedList = categoryDeliveries.filter((d: any) => 
          (d.status === "ASSIGNED" || d.status === "PICKED_UP" || d.status === "RESERVED_NEXT" || d.status === "QUEUED") && (d.riderId || d.driverId)
        );
        const transitList = categoryDeliveries.filter((d: any) => 
          d.status === "TRANSIT"
        );
        const arrivedList = categoryDeliveries.filter((d: any) => 
          d.status === "ARRIVED" || d.status === "OUT_FOR_DELIVERY" || d.status === "HANDOVER_VERIFIED"
        );

        const displayedQueue = modalStatusFilter === "PENDING" 
          ? pendingList 
          : modalStatusFilter === "ASSIGNED" 
          ? assignedList 
          : modalStatusFilter === "TRANSIT" 
          ? transitList 
          : modalStatusFilter === "ARRIVED" 
          ? arrivedList 
          : categoryDeliveries;

        return (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setInspectingCategory(null)}>
            <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-3xl shadow-2xl border border-gray-200 dark:border-white/10 space-y-4 animate-scale-in max-h-[90vh] flex flex-col" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between pb-3 border-b border-gray-200 dark:border-white/10 shrink-0">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-black px-2.5 py-0.5 rounded-full bg-[#FFB800] text-[#111]">{inspectingCategory.tag}</span>
                    <h3 className="text-base font-black text-gray-900 dark:text-white">Active Dispatch Queue ({categoryDeliveries.length})</h3>
                  </div>
                  <p className="text-[11px] text-gray-600 dark:text-gray-300 font-medium mt-0.5">Real-time live queue for today in Benin City • Ordered FIFO (Oldest requests first)</p>
                </div>
                <button onClick={() => setInspectingCategory(null)} className="p-2 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white rounded-xl hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer">
                  <X size={18} />
                </button>
              </div>

              <div className="flex items-center gap-1.5 p-1.5 bg-gray-100 dark:bg-white/5 rounded-2xl overflow-x-auto shrink-0">
                <button 
                  onClick={() => setModalStatusFilter("PENDING")}
                  className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer whitespace-nowrap ${
                    modalStatusFilter === "PENDING" 
                      ? "bg-[#FFB800] text-[#111] shadow-xs" 
                      : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-bold"
                  }`}
                >
                  <span>Pending Assignment</span>
                  <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    modalStatusFilter === "PENDING" ? "bg-black/20 text-[#111]" : "bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200"
                  }`}>
                    {pendingList.length}
                  </span>
                </button>

                <button 
                  onClick={() => setModalStatusFilter("ASSIGNED")}
                  className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer whitespace-nowrap ${
                    modalStatusFilter === "ASSIGNED" 
                      ? "bg-[#FFB800] text-[#111] shadow-xs" 
                      : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-bold"
                  }`}
                >
                  <span>Assigned</span>
                  <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    modalStatusFilter === "ASSIGNED" ? "bg-black/20 text-[#111]" : "bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200"
                  }`}>
                    {assignedList.length}
                  </span>
                </button>

                <button 
                  onClick={() => setModalStatusFilter("TRANSIT")}
                  className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer whitespace-nowrap ${
                    modalStatusFilter === "TRANSIT" 
                      ? "bg-[#FFB800] text-[#111] shadow-xs" 
                      : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-bold"
                  }`}
                >
                  <span>In Transit</span>
                  <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    modalStatusFilter === "TRANSIT" ? "bg-black/20 text-[#111]" : "bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200"
                  }`}>
                    {transitList.length}
                  </span>
                </button>

                <button 
                  onClick={() => setModalStatusFilter("ARRIVED")}
                  className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer whitespace-nowrap ${
                    modalStatusFilter === "ARRIVED" 
                      ? "bg-[#FFB800] text-[#111] shadow-xs" 
                      : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-bold"
                  }`}
                >
                  <span>Out / Arrived</span>
                  <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    modalStatusFilter === "ARRIVED" ? "bg-black/20 text-[#111]" : "bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200"
                  }`}>
                    {arrivedList.length}
                  </span>
                </button>

                <button 
                  onClick={() => setModalStatusFilter("ALL")}
                  className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer whitespace-nowrap ${
                    modalStatusFilter === "ALL" 
                      ? "bg-[#FFB800] text-[#111] shadow-xs" 
                      : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-bold"
                  }`}
                >
                  <span>All Active</span>
                  <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    modalStatusFilter === "ALL" ? "bg-black/20 text-[#111]" : "bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200"
                  }`}>
                    {categoryDeliveries.length}
                  </span>
                </button>
              </div>

              <div className="flex-1 overflow-y-auto space-y-3 pr-1">
                {displayedQueue.length === 0 ? (
                  <div className="p-8 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 text-center">
                    <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                    <p className="text-xs font-black text-gray-900 dark:text-white">
                      {modalStatusFilter === "PENDING" ? "All Pending Deliveries Assigned!" : `No consignments currently in ${modalStatusFilter.replace(/_/g, " ").toLowerCase()} stage`}
                    </p>
                    <p className="text-[11px] text-gray-600 dark:text-gray-300 font-medium mt-1 max-w-sm mx-auto">
                      {modalStatusFilter === "PENDING" 
                        ? "Great job! All customer orders in this category have assigned couriers. Switch to Assigned or In Transit tab to track live movements." 
                        : "Check the other progression tabs above to monitor orders across each dispatch phase."}
                    </p>
                    {modalStatusFilter === "PENDING" && assignedList.length > 0 && (
                      <button 
                        onClick={() => setModalStatusFilter("ASSIGNED")}
                        className="mt-3 px-4 py-1.5 rounded-xl bg-[#FFB800] text-[#111] text-xs font-black hover:bg-[#FFB800]/90 transition-all cursor-pointer shadow-xs"
                      >
                        View Assigned Fleet ({assignedList.length}) →
                      </button>
                    )}
                  </div>
                ) : (
                  displayedQueue.map((d: any, qIdx: number) => {
                    const createdAgo = d.createdAt?.toMillis
                      ? Math.max(1, Math.round((Date.now() - d.createdAt.toMillis()) / 60000)) + "m ago"
                      : (d.dateString || "Today");
                    return (
                      <div key={d.id} className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex flex-col gap-3 hover:border-[#FFB800]/60 transition-all">
                        <div className="flex items-start justify-between gap-3">
                          <div className="flex items-center gap-3 min-w-0">
                            <div className="w-10 h-10 rounded-xl bg-[#FFB800]/20 flex items-center justify-center text-amber-800 dark:text-[#FFB800] shrink-0 font-bold text-xs">
                              <Package size={18} />
                            </div>
                            <div className="min-w-0">
                              <div className="flex items-center gap-2 flex-wrap">
                                <span className="font-extrabold text-xs text-gray-900 dark:text-white">{d.itemName || "Consignment"}</span>
                                <span className="text-[10px] text-gray-600 dark:text-gray-400 font-mono font-semibold">#{idShort(d.id)}</span>
                                <span className={"text-[9px] font-bold px-2 py-0.5 rounded-full " + sStyle(d.status)}>{d.status}</span>
                                <span className="text-[9px] font-bold px-2 py-0.5 rounded bg-amber-500/15 text-amber-900 dark:text-amber-300">
                                  FIFO #{qIdx + 1} • {createdAgo}
                                </span>
                              </div>
                              <p className="text-[11px] font-bold text-gray-800 dark:text-gray-200 mt-1">
                                ₦{(d.price || 0).toLocaleString()} • {d.paymentStatus || "PAID"}
                              </p>
                            </div>
                          </div>
                          <div className="text-right shrink-0">
                            <button 
                              onClick={() => { 
                                setInspectingCategory(null); 
                                if (onOpenShipmentFullView) {
                                  onOpenShipmentFullView(d.id);
                                } else {
                                  if (setShipmentsFilterPrefill) setShipmentsFilterPrefill({ search: d.id, selectedId: d.id, status: "ALL", category: "ALL" });
                                  setTab("shipments"); 
                                }
                              }} 
                              className="px-3.5 py-1.5 bg-[#FFB800] text-[#111] text-xs font-black rounded-xl hover:bg-[#FFB800]/90 transition-all cursor-pointer shadow-xs flex items-center gap-1"
                              title="Open full shipment workspace"
                            >
                              Open Full View →
                            </button>
                          </div>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-[11px] bg-white dark:bg-[#1a1a1a] p-2.5 rounded-xl border border-gray-200 dark:border-white/10">
                          <div>
                            <p className="text-[9px] font-black uppercase text-gray-500 dark:text-gray-400">Pickup (Sender)</p>
                            <p className="font-bold text-gray-900 dark:text-white truncate">{d.senderName || "Sender"}</p>
                            <p className="text-gray-700 dark:text-gray-300 font-medium text-[10px] truncate">{d.pickupAddress || "Benin City"}</p>
                            {d.senderPhone && (
                              <div className="flex items-center gap-2 mt-1">
                                <a href={`tel:${d.senderPhone}`} className="text-[10px] font-bold text-emerald-700 dark:text-emerald-400 hover:underline flex items-center gap-0.5">
                                  <Phone size={10} /> Call {d.senderPhone}
                                </a>
                                <a href={`https://wa.me/${d.senderPhone.replace(/[^0-9]/g, "")}`} target="_blank" rel="noreferrer" className="text-[10px] font-bold text-emerald-700 dark:text-emerald-400 hover:underline">
                                  WhatsApp
                                </a>
                              </div>
                            )}
                          </div>
                          <div>
                            <p className="text-[9px] font-black uppercase text-gray-500 dark:text-gray-400">Dropoff (Receiver)</p>
                            <p className="font-bold text-gray-900 dark:text-white truncate">{d.receiverName || "Receiver"}</p>
                            <p className="text-gray-700 dark:text-gray-300 font-medium text-[10px] truncate">{d.deliveryAddress || "Benin City"}</p>
                            {d.receiverPhone && (
                              <div className="flex items-center gap-2 mt-1">
                                <a href={`tel:${d.receiverPhone}`} className="text-[10px] font-bold text-emerald-700 dark:text-emerald-400 hover:underline flex items-center gap-0.5">
                                  <Phone size={10} /> Call {d.receiverPhone}
                                </a>
                                <a href={`https://wa.me/${d.receiverPhone.replace(/[^0-9]/g, "")}`} target="_blank" rel="noreferrer" className="text-[10px] font-bold text-emerald-700 dark:text-emerald-400 hover:underline">
                                  WhatsApp
                                </a>
                              </div>
                            )}
                          </div>
                        </div>

                        <div className="flex items-center justify-between text-[11px] pt-1 border-t border-gray-200 dark:border-white/10 flex-wrap gap-2">
                          <div className="text-gray-700 dark:text-gray-300 font-medium flex items-center gap-1.5 flex-wrap">
                            <Users size={12} className="text-amber-800 dark:text-[#FFB800]" />
                            <span>Rider: <b className="text-gray-900 dark:text-white">{d.courierName || d.driverName || "Unassigned"}</b> {d.courierPhone ? `(${d.courierPhone})` : ""}</span>
                            {d.reservedCourierName && (
                              <span className="text-[9px] bg-purple-500/15 text-purple-700 dark:text-purple-300 px-2 py-0.5 rounded-full font-bold">
                                Reserved Next: {d.reservedCourierName}
                              </span>
                            )}
                          </div>
                          {inlineAssigningId === d.id ? (
                            <div className="flex items-center gap-1.5 flex-wrap">
                              <div className="relative inline-block text-left">
                                <select
                                  onChange={async (e) => {
                                    const rider = drivers.find((x: any) => x.id === e.target.value);
                                    if (rider) {
                                      await handleInlineAssignRider(d.id, rider);
                                    }
                                    setInlineAssigningId(null);
                                  }}
                                  defaultValue=""
                                  className="appearance-none text-xs font-bold bg-white dark:bg-[#1a1a1a] border-2 border-amber-500 dark:border-[#FFB800] rounded-xl pl-3 pr-8 py-1.5 text-gray-900 dark:text-white cursor-pointer shadow-xs hover:border-amber-600 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 transition-all"
                                >
                                  <option value="" disabled className="text-gray-500">Select rider to assign...</option>
                                  {drivers.map((drv: any) => (
                                    <option key={drv.id} value={drv.id} className="text-gray-900 dark:text-white bg-white dark:bg-[#1a1a1a]">
                                      {drv.name} ({drv.phone || "Active"}) • {drv.isOnline ? "🟢 Online" : "⚪ Offline"}
                                    </option>
                                  ))}
                                </select>
                                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2.5 text-amber-800 dark:text-[#FFB800]">
                                  <ChevronDown size={14} strokeWidth={2.5} />
                                </div>
                              </div>
                              <button
                                onClick={() => setInlineAssigningId(null)}
                                className="text-[10px] text-gray-600 dark:text-gray-400 font-bold hover:underline cursor-pointer px-1"
                              >
                                Cancel
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center gap-2">
                              {(!d.riderId && !d.driverId) ? (
                                <button
                                  onClick={() => setInlineAssigningId(d.id)}
                                  className="text-xs font-black px-3 py-1.5 bg-[#FFB800] text-[#111] rounded-xl hover:bg-[#FFB800]/90 transition-all cursor-pointer shadow-xs flex items-center gap-1"
                                >
                                  <UserPlus size={13} /> Quick Assign Rider
                                </button>
                              ) : (
                                <div className="flex items-center gap-2">
                                  <div className="relative inline-block text-left">
                                    <select
                                       value={d.status}
                                       onChange={(e) => handleInlineStatusChange(d.id, e.target.value)}
                                       className="appearance-none text-xs font-black bg-white dark:bg-[#1a1a1a] border border-gray-300 dark:border-white/20 rounded-xl pl-3 pr-8 py-1.5 text-gray-900 dark:text-white cursor-pointer shadow-xs hover:border-[#FFB800] focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 transition-all"
                                     >
                                       {(() => {
                                         const normCurrent = (d.status || "PENDING").toUpperCase();
                                         const currentIdx = getLifecycleStageIndex(normCurrent);
                                         const isDelivered = normCurrent === "DELIVERED";
                                         const isCancelled = normCurrent === "CANCELLED";

                                         const baseStages = [
                                           { value: "PENDING", label: "PENDING", stage: 0 },
                                           { value: "ASSIGNED", label: "ASSIGNED", stage: 1 },
                                           { value: "TRANSIT", label: "TRANSIT", stage: 2 },
                                           { value: "OUT_FOR_DELIVERY", label: "OUT FOR DELIVERY", stage: 3 },
                                           { value: "DELIVERED", label: "DELIVERED", stage: 4 },
                                         ];

                                         const list = [...baseStages];
                                         if (!list.some(s => s.value === normCurrent) && !isCancelled) {
                                           list.push({
                                             value: normCurrent,
                                             label: normCurrent.replace(/_/g, " "),
                                             stage: currentIdx,
                                           });
                                           list.sort((a, b) => a.stage - b.stage);
                                         }

                                         return (
                                           <>
                                             {list.map((step) => {
                                               const isPassed = !isCancelled && step.stage < currentIdx;
                                               const isThisCurrent = !isCancelled && (step.value === normCurrent || (step.stage === currentIdx && isDelivered));

                                               if (isPassed) {
                                                 return (
                                                   <option
                                                     key={step.value}
                                                     value={step.value}
                                                     disabled
                                                     className="text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-[#222]"
                                                   >
                                                     ✓ {step.label} (Passed)
                                                   </option>
                                                 );
                                               }

                                               if (isThisCurrent) {
                                                 return (
                                                   <option
                                                     key={step.value}
                                                     value={step.value}
                                                     className="font-black text-amber-900 dark:text-[#FFB800] bg-amber-50 dark:bg-[#1a1a1a]"
                                                   >
                                                     {isDelivered ? `✓ ${step.label} (Completed)` : `● ${step.label} (Current)`}
                                                   </option>
                                                 );
                                               }

                                               return (
                                                 <option
                                                   key={step.value}
                                                   value={step.value}
                                                   disabled={isDelivered}
                                                   className="text-gray-900 dark:text-white bg-white dark:bg-[#1a1a1a]"
                                                 >
                                                   {step.label}
                                                 </option>
                                               );
                                             })}
                                             <option
                                               value="CANCELLED"
                                               disabled={isDelivered}
                                               className={isCancelled ? "font-bold text-red-600 dark:text-red-400" : "text-red-600 dark:text-red-400"}
                                             >
                                               {isCancelled ? "● CANCELLED (Current)" : "CANCELLED"}
                                             </option>
                                           </>
                                         );
                                       })()}
                                     </select>
                                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-600 dark:text-gray-300">
                                      <ChevronDown size={14} strokeWidth={2.5} />
                                    </div>
                                  </div>
                                  <button
                                    onClick={() => setInlineAssigningId(d.id)}
                                    className="text-xs font-bold px-2.5 py-1.5 bg-gray-100 dark:bg-white/10 text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 rounded-xl hover:bg-gray-200 dark:hover:bg-white/15 transition-all cursor-pointer"
                                    title="Reassign to another rider"
                                  >
                                    Reassign
                                  </button>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              <div className="pt-2 border-t border-gray-200 dark:border-white/10 flex justify-between items-center text-xs shrink-0 flex-wrap gap-2">
                <span className="text-gray-600 dark:text-gray-400 text-[11px] font-medium">Click 'Open Full View' on any consignment above for comprehensive dispatch controls and waybill management.</span>
                <button 
                  onClick={() => { 
                    setInspectingCategory(null); 
                    if (setShipmentsFilterPrefill) setShipmentsFilterPrefill({ category: inspectingCategory.tag });
                    setTab("shipments"); 
                  }} 
                  className="px-4 py-2 bg-[#FFB800] text-[#111] font-black text-xs rounded-xl hover:bg-[#FFB800]/90 transition-all cursor-pointer shadow-xs"
                >
                  Open All in Shipments Tab →
                </button>
              </div>
            </div>
          </div>
        );
      })()}
      </section>

      <section className="grid grid-cols-1 lg:grid-cols-4 gap-6 min-h-0">
        <div className="lg:col-span-3 border border-gray-200 dark:border-white/10 rounded-3xl p-6 sm:p-7 flex flex-col bg-white dark:bg-[#1a1a1a] shadow-xs">
          <div className="flex justify-between items-center mb-5 flex-wrap gap-2">
            <div>
              <h2 className="text-[20px] sm:text-[22px] font-extrabold tracking-tight text-[#111] dark:text-white">Active Bookings Today</h2>
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-0.5">Real-time scheduled drop-offs awaiting fulfillment</p>
            </div>
            <button 
              onClick={() => setTab("shipments")} 
              className="px-3.5 py-1.5 rounded-xl bg-gray-100 hover:bg-gray-200 dark:bg-[#222] dark:hover:bg-[#333] text-xs font-black text-[#111] dark:text-white transition-all cursor-pointer flex items-center gap-1.5"
            >
              View all ({deliveries.length}) <ChevronRight size={14} />
            </button>
          </div>
          {recentDeliveries.length === 0 && (
            <div className="text-center py-12 text-gray-600 dark:text-gray-400 text-xs font-semibold bg-gray-50 dark:bg-[#222] rounded-2xl border border-dashed border-gray-300 dark:border-white/10">
              No active bookings for today. All scheduled drops completed or waiting for new requests.
            </div>
          )}
          {recentDeliveries.length > 0 && (
            <div className="space-y-3 flex-1">
              {recentDeliveries.slice(0, 5).map((d: any) => (
                <div key={d.id} className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex items-center justify-between gap-4 hover:border-gray-300 dark:hover:border-white/20 transition-all">
                  <div className="flex items-center gap-3.5 min-w-0">
                    <div className="w-11 h-11 rounded-2xl bg-gray-200 dark:bg-white/10 text-[#111] dark:text-white flex items-center justify-center shrink-0">
                      <Package size={20} strokeWidth={2.2} />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-extrabold text-sm text-[#111] dark:text-white truncate">{d.itemName || "Consignment"}</p>
                        <span className="text-[10px] font-mono font-bold text-gray-700 dark:text-gray-300">#{idShort(d.id)}</span>
                      </div>
                      <div className="flex items-center gap-1.5 text-xs text-gray-700 dark:text-gray-300 truncate mt-0.5">
                        <span className="font-medium text-[#111] dark:text-white truncate max-w-[140px] sm:max-w-[180px]">{d.pickupAddress || "Pickup"}</span>
                        <span className="text-gray-500 dark:text-gray-400 shrink-0 font-bold">→</span>
                        <span className="truncate max-w-[160px] sm:max-w-[240px]">{d.deliveryAddress || "Destination"}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span className={"text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-wider " + sStyle(d.status)}>{d.status}</span>
                    <button 
                      onClick={() => {
                        if (onOpenShipmentFullView) onOpenShipmentFullView(d.id);
                        else {
                          if (setShipmentsFilterPrefill) setShipmentsFilterPrefill({ search: d.id, selectedId: d.id, status: "ALL", category: "ALL" });
                          setTab("shipments");
                        }
                      }}
                      className="px-3 py-1.5 bg-[#FFB800] text-[#111] text-xs font-black rounded-xl hover:bg-[#FFB800]/90 transition-all shadow-xs cursor-pointer whitespace-nowrap flex items-center gap-1"
                    >
                      Manage <ChevronRight size={13} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="border border-gray-200 dark:border-white/10 rounded-3xl p-6 sm:p-7 flex flex-col justify-between bg-white dark:bg-[#1a1a1a] shadow-xs">
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="font-extrabold text-base text-[#111] dark:text-white">Fleet Readiness</h3>
              <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-800 dark:text-emerald-400 text-[10px] font-black uppercase tracking-wider border border-emerald-500/30">
                {drivers.filter((d: any) => d.isOnline !== false).length} Online
              </span>
            </div>
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mb-5">Active courier capacity & fleet status</p>
            <div className="flex items-center gap-3 mb-6 p-3.5 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10">
              <div className="flex -space-x-2 shrink-0">
                {drivers.slice(0, 4).map((d: any) => (
                  <div 
                    key={d.id} 
                    title={`${d.name || "Courier"} (${d.isOnline !== false ? "Online" : "Offline"})`} 
                    className="w-10 h-10 rounded-full ring-2 ring-white dark:ring-[#222] bg-[#111] dark:bg-white text-white dark:text-[#111] flex items-center justify-center text-xs font-black shadow-xs"
                  >
                    {d.name?.charAt(0)?.toUpperCase() || "R"}
                  </div>
                ))}
                {drivers.length === 0 && (
                  <div className="w-10 h-10 rounded-full ring-2 ring-white dark:ring-[#222] bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400 flex items-center justify-center text-xs font-bold">
                    0
                  </div>
                )}
              </div>
              <div className="text-xs min-w-0">
                <p className="font-extrabold text-[#111] dark:text-white truncate">{drivers.length} Registered Riders</p>
                <p className="text-gray-600 dark:text-gray-400 text-[11px] font-medium truncate">{drivers.filter((d: any) => d.isOnline !== false).length} active for dispatch</p>
              </div>
            </div>
          </div>
          <div className="space-y-3 py-3 border-t border-b border-gray-200 dark:border-white/10 mb-4">
            <div className="flex justify-between items-center text-xs">
              <span className="text-gray-700 dark:text-gray-300 font-semibold">Deliveries today</span>
              <span className="font-extrabold text-[#111] dark:text-white text-sm">{deliveries.length}</span>
            </div>
            <div className="flex justify-between items-center text-xs">
              <span className="text-gray-700 dark:text-gray-300 font-semibold">Today's Revenue</span>
              <span className="font-black text-emerald-600 dark:text-emerald-400 text-sm">{fmt(totalRevenue)}</span>
            </div>
            <div className="flex justify-between items-center text-xs">
              <span className="text-gray-700 dark:text-gray-300 font-semibold">Awaiting Dispatch</span>
              <span className="font-bold text-amber-800 dark:text-amber-300 text-xs px-2 py-0.5 rounded-md bg-amber-500/15">{pendingDeliveries.length} unassigned</span>
            </div>
          </div>
          <button 
            onClick={() => setTab("shipments")} 
            className="w-full bg-[#FFB800] text-[#111] py-3.5 rounded-2xl text-xs font-black shadow-md hover:bg-[#FFB800]/90 active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            <Truck size={16} /> Assign Riders ({pendingDeliveries.length})
          </button>
        </div>
      </section>
    </div>;
}


interface SidebarProps {
  sidebar: boolean;
  setSidebar: (v: boolean) => void;
  tab: TabId;
  setTab: (v: TabId) => void;
  mobileSidebar: boolean;
  setMobileSidebar: (v: boolean) => void;
  navItems: { id: TabId; label: string; icon: React.ReactNode; badge?: number }[];
}

function Sidebar({ sidebar, setSidebar, tab, setTab, mobileSidebar, setMobileSidebar, navItems }: SidebarProps) {
  return (
    <aside className={"h-full bg-[#111] flex flex-col shrink-0 z-10 transition-all duration-300 " + (sidebar ? "w-[220px]" : "w-[70px]") + (mobileSidebar ? " translate-x-0" : " -translate-x-full lg:translate-x-0")}>
      <div className={"shrink-0 mx-3 mt-5 mb-8 flex items-center cursor-pointer transition-all " + (sidebar ? "justify-start px-4 py-3" : "justify-center p-3")}
        onClick={() => setSidebar(!sidebar)}>
        <EdLogoSvg size={28} />
        {sidebar && (
          <div className="flex flex-col ml-3">
            <span className="text-[#FFB800] text-sm font-black leading-tight">ES</span>
            <span className="text-[#FFB800] text-sm font-black leading-tight">DISPATCH</span>
          </div>
        )}
      </div>
      <nav className="flex flex-col gap-1 w-full px-3 flex-1 overflow-y-auto pb-4">
        {navItems.map(n => (
          <button key={n.id} onClick={() => { setTab(n.id); setMobileSidebar(false); }}
            className={"flex items-center gap-3 p-3 rounded-3xl transition-all relative " + (tab === n.id ? "bg-[#FFB800] text-[#111] shadow-lg font-black" : "text-gray-300 hover:text-white hover:bg-white/10 font-bold")}>
            <span className="shrink-0 relative">
              {n.icon}
              {!sidebar && n.badge !== undefined && n.badge > 0 && (
                <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-[#FFB800] text-[#111] text-[9px] font-black flex items-center justify-center">
                  {n.badge > 99 ? "99+" : n.badge}
                </span>
              )}
            </span>
            {sidebar && (
              <div className="flex items-center justify-between flex-1 min-w-0">
                <span className="text-xs font-bold whitespace-nowrap">{n.label}</span>
                {n.badge !== undefined && n.badge > 0 && (
                  <span className={"px-2 py-0.5 rounded-full text-[10px] font-black " + (tab === n.id ? "bg-[#111] text-[#FFB800]" : "bg-[#FFB800] text-[#111]")}>
                    {n.badge > 99 ? "99+" : n.badge}
                  </span>
                )}
              </div>
            )}
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
        <button className="lg:hidden p-2 text-gray-900 dark:text-white hover:bg-black/5 dark:hover:bg-white/10 rounded-xl transition-colors" onClick={() => setMobileSidebar(true)}>
          <Menu size={22} />
        </button>
        <div className="text-sm sm:text-base text-gray-600 dark:text-gray-400">
          Welcome to<br /><span className="font-extrabold text-gray-900 dark:text-white text-lg sm:text-xl tracking-tight">ES<span className="text-amber-600 dark:text-[#FFB800]">DISPATCH</span></span>
        </div>
      </div>
      <div className="flex items-center gap-2 sm:gap-5">
        <div className="hidden sm:flex items-center border border-gray-300 dark:border-white/20 rounded-full pl-5 pr-1.5 py-1.5 w-[200px] lg:w-[280px] shadow-sm bg-gray-50/50 dark:bg-[#1a1a1a]/50">
          <input type="text" placeholder="Search" value={searchQuery} onChange={e => setSearchQuery(e.target.value)} className="outline-none flex-1 text-sm bg-transparent font-medium text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500" />
          <button className="bg-[#FFB800] text-[#111] p-2 rounded-xl hover:bg-[#FFB800]/90 transition-colors cursor-pointer"><Search size={18} strokeWidth={2.5} /></button>
        </div>
        <div className="relative" id="notif-area">
          <button onClick={(e) => { e.stopPropagation(); setShowNotifs(!showNotifs); setShowUserMenu(false); }} className="relative p-2.5 border border-gray-300 dark:border-white/20 rounded-full flex items-center justify-center cursor-pointer shadow-sm hover:bg-black/5 dark:hover:bg-white/10 transition-colors">
            <Bell size={20} className="text-gray-900 dark:text-white" />
            {unreadCount > 0 && <div className="absolute top-2 right-2.5 w-2.5 h-2.5 bg-[#FFB800] rounded-full border-2 border-white dark:border-[#1a1a1a] animate-pulse-ring"></div>}
          </button>
          {showNotifs && <div className="absolute right-0 top-full mt-2 w-80 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl shadow-2xl overflow-hidden z-50 animate-scale-in">
            <div className="p-4 border-b border-gray-200 dark:border-white/10"><p className="text-xs font-black text-amber-800 dark:text-[#FFB800] tracking-wider uppercase">NOTIFICATIONS</p></div>
            <div className="max-h-72 overflow-y-auto">
              {notifications.length === 0 && <div className="p-6 text-center text-xs text-gray-600 dark:text-gray-400 font-semibold">No notifications yet.</div>}
              {notifications.map((n: any) => <div key={n.id} onClick={() => markNotifRead(n.id)} className={"p-4 border-b border-gray-100 dark:border-white/5 hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer " + (n.read ? "" : "bg-[#FFB800]/10")}>
                <div className="flex items-start gap-3">
                  <div className={"w-2 h-2 mt-1.5 rounded-full shrink-0 " + (n.read ? "bg-transparent" : "bg-[#FFB800]")}></div>
                  <div><p className="text-xs font-bold text-gray-900 dark:text-white">{n.title}</p><p className="text-[11px] text-gray-600 dark:text-gray-400 mt-0.5">{n.description}</p><p className="text-[10px] text-gray-500 dark:text-gray-400 mt-1 font-medium">{n.time || "Just now"}</p></div>
                </div>
              </div>)}
            </div>
            <div className="p-3 text-center border-t border-gray-200 dark:border-white/10"><button className="text-xs text-amber-800 dark:text-[#FFB800] font-black hover:underline cursor-pointer">View all notifications</button></div>
          </div>}
        </div>
        <div className="relative" id="user-menu-area">
          <div className="flex items-center gap-3 ml-1 cursor-pointer" onClick={(e) => { e.stopPropagation(); setShowUserMenu(!showUserMenu); setShowNotifs(false); }}>
            <div className="w-11 h-11 rounded-full bg-[#FFB800]/20 border border-black/10 dark:border-white/10 flex items-center justify-center text-amber-900 dark:text-white font-black text-sm"><EdLogoSvg size={20} /></div>
            <div className="text-sm hidden sm:block">
              <div className="font-extrabold text-gray-900 dark:text-white">{currentUser?.email?.split("@")[0] || "Admin"}</div>
              <div className="text-gray-600 dark:text-gray-400 font-bold text-xs mt-0.5">{(userRole || "admin").replace("_", " ").toUpperCase()}</div>
            </div>
          </div>
          {showUserMenu && <div className="absolute right-0 top-full mt-2 w-56 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl shadow-2xl overflow-hidden z-50 animate-scale-in">
            <div className="p-4 border-b border-gray-200 dark:border-white/10">
              <p className="text-xs font-bold text-gray-900 dark:text-white truncate">{currentUser?.email}</p>
              <p className="text-[10px] text-gray-600 dark:text-gray-400 font-bold mt-0.5 uppercase tracking-wider">{(userRole || "Admin").replace("_", " ")}</p>
            </div>
            <div className="p-2">
              <button onClick={toggleDark} className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-gray-100 dark:hover:bg-white/10 transition-colors text-xs font-bold text-gray-900 dark:text-white cursor-pointer">
                {dark ? <Sun className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" /> : <Moon className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" />}
                {dark ? "Light Mode" : "Dark Mode"}
              </button>
              <button onClick={() => { document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); }} className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors text-xs font-bold text-red-600 dark:text-red-400 mt-1 cursor-pointer">
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
  const isFirstDeliveriesLoad = useRef(true);
  const [newOrderAlert, setNewOrderAlert] = useState<{
    id: string;
    itemName: string;
    receiverName: string;
    deliveryAddress: string;
    category: string;
    price: number;
  } | null>(null);
  const [shipmentsFilterPrefill, setShipmentsFilterPrefill] = useState<{ status?: string; category?: string; search?: string; selectedId?: string } | null>(null);

  const addToast = useCallback((type: Toast["type"], message: string, dedupeKey?: string) => {
    if (dedupeKey && NotificationLifecycleManager.isDismissed(dedupeKey)) {
      return;
    }
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
    const handler = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      const notifArea = document.getElementById("notif-area");
      const userArea = document.getElementById("user-menu-area");
      if (notifArea && notifArea.contains(target)) return;
      if (userArea && userArea.contains(target)) return;
      setShowNotifs(false);
      setShowUserMenu(false);
    };
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
    appName: "ESDISPATCH", appSlogan: "PREMIUM LOGISTICS & DISPATCH", fcmServerKey: "",
  });
  const [appContent, setAppContent] = useState<AppContent[]>([]);
  const [logs, setLogs] = useState<AuditEntry[]>([]);

  const [products, setProducts] = useState<Product[]>([]);
  const [stores, setStores] = useState<VendorStore[]>([]);
  const [marketplaceOrders, setMarketplaceOrders] = useState<MarketplaceOrder[]>([]);
  const [payoutRequests, setPayoutRequests] = useState<VendorPayoutRequest[]>([]);

  const [connected, setConnected] = useState(false);
  const [refreshT, setRefreshT] = useState("");
  const [notifications, setNotifications] = useState<any[]>([]);

  const unreadCount = notifications.filter((n: any) => !n.read).length;

  const createNotification = useCallback(async (title: string, description: string) => {
    try {
      const notifRef = await addDoc(collection(db, "notifications"), {
        title, description, time: "Just now", read: false,
        createdAt: Timestamp.now(), timestamp: Date.now(),
      });
      // Fan-out to every active user's subcollection
      const active = users.filter(u => !u.isDeleted);
      for (let i = 0; i < active.length; i += 500) {
        const batch = writeBatch(db);
        const chunk = active.slice(i, i + 500);
        chunk.forEach(u => {
          const ref = doc(collection(db, "users", u.uid, "notifications"));
          batch.set(ref, { title, description, time: "Just now", read: false, adminNotifId: notifRef.id, createdAt: Timestamp.now(), timestamp: Date.now(), });
        });
        await batch.commit();
      }
    } catch (e: any) {
      console.error("createNotification failed:", e);
    }
  }, [users]);

  const markNotifRead = useCallback(async (id: string) => {
    try { await updateDoc(doc(db, "notifications", id), { read: true }); } catch (e: any) {
      console.error("markNotifRead failed:", e);
    }
  }, []);

  const addLog = useCallback(async (action: string, details: string) => {
    const entry = { action, details, admin: currentUser?.email || "Admin", timestamp: Timestamp.now() };
    try { await addDoc(collection(db, "audit_logs"), entry); } catch (e: any) {
      console.error("addLog failed:", e);
    }
    setLogs((prev: AuditEntry[]) => [{ id: Date.now().toString(), time: "Just now", ...entry, timestamp: Date.now() }, ...prev]);
  }, [currentUser]);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (u) => {
      if (u) {
          try {
            await new Promise(r => setTimeout(r, 1500));
            const token = await u.getIdToken();
            const snap = await getDoc(doc(db, "users", u.uid));
            if (!snap.exists()) { setAuthErr("Account not found. Contact an administrator."); document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); setCurrentUser(null); setLoading(false); return; }
            const d = snap.data();
            if (d?.role === "admin" || d?.role === "super_admin") { setCurrentUser(u); setUserRole(d.role); document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`; setLoading(false); return; }
            if (d?.role === "dispatcher") { setCurrentUser(u); setUserRole("dispatcher"); document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`; setLoading(false); return; }
            setAuthErr("Unauthorized"); document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); setCurrentUser(null);
          } catch { setAuthErr("Authentication error. Please try again."); document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); setCurrentUser(null); }
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
      if (!isFirstDeliveriesLoad.current) {
        snap.docChanges().forEach(change => {
          if (change.type === "added") {
            const data = change.doc.data();
            const status = data.status || "PENDING";
            if (status === "PENDING") {
              try {
                SoundEngine.playCue("dispatch_broadcast");
              } catch (e) {
                console.error("Audio chime error:", e);
              }
              const newOrder = {
                id: change.doc.id,
                itemName: data.itemName || "Consignment",
                receiverName: data.receiverName || "Customer",
                deliveryAddress: data.deliveryAddress || "Benin City",
                category: data.category || "General",
                price: data.price || 0,
              };
              setNewOrderAlert(newOrder);
              addToast("info", `🔔 New order incoming: ${newOrder.itemName} for ${newOrder.receiverName}!`);
            }
          }
        });
      } else {
        isFirstDeliveriesLoad.current = false;
      }
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
        riderBikeNumber: x.riderBikeNumber || "",
        driverId: x.driverId || "",
        driverName: x.driverName || "",
        courierLatitude: x.courierLatitude || null,
        courierLongitude: x.courierLongitude || null,
        pickupLat: x.pickupLat || x.pickupLatitude || null,
        pickupLng: x.pickupLng || x.pickupLongitude || null,
        deliveryLat: x.deliveryLat || x.deliveryLatitude || null,
        deliveryLng: x.deliveryLng || x.deliveryLongitude || null,
        createdAt: x.createdAt || null,
        updatedAt: x.updatedAt || null,
        deliveryFee: x.deliveryFee || 0,
        paymentStatus: x.paymentStatus || "PAID",
        reservedRiderId: x.reservedRiderId || "",
        reservedCourierName: x.reservedCourierName || "",
        reservedCourierPhone: x.reservedCourierPhone || "",
        podUrl: x.podUrl || "",
        exceptionType: x.exceptionType || "",
        exceptionReason: x.exceptionReason || "",
      }); });
      setDeliveries(list);
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "banners"), snap => {
      const list: Banner[] = [];
      snap.forEach(d => { const x = d.data(); list.push({ id: d.id, title: x.title || "", subtitle: x.subtitle || "", imageUrl: x.imageUrl || "", interval: x.interval || 5, order: x.order || 0, active: x.active !== false }); });
      setBanners(list.sort((a, b) => a.order - b.order));
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "referrals"), snap => {
      const list: Referral[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, referrerId: x.referrerId || "", referrerName: x.referrerName || "",
        referrerEmail: x.referrerEmail || "", refereeId: x.refereeId || "",
        refereeName: x.refereeName || "", refereeEmail: x.refereeEmail || "",
        rewardAmount: x.rewardAmount || 0, status: x.status || "pending",
      }); });
      setReferrals(list);
    }, console.error));
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
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "appContent"), snap => {
      const list: AppContent[] = [];
      snap.forEach(d => { const x = d.data(); list.push({
        id: d.id, key: x.key || "", title: x.title || "", description: x.description || "",
        imageUrl: x.imageUrl || "", ctaText: x.ctaText || "", ctaLink: x.ctaLink || "",
        order: x.order || 0, active: x.active !== false,
      }); });
      setAppContent(list);
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "notifications"), snap => {
      const list: any[] = [];
      snap.forEach(d => { const x = d.data(); list.push({ id: d.id, ...x }); });
      setNotifications(list.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)));
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "marketplace_products"), snap => {
      const list: Product[] = [];
      snap.forEach(d => {
        const x = d.data();
        list.push({
          id: d.id,
          name: x.name || x.title || "",
          category: x.category || "General",
          price: x.price || 0,
          stock: x.stock || 0,
          imageUrl: x.imageUrl || "",
          status: x.status || (x.stock > 10 ? "In Stock" : x.stock > 0 ? "Low Stock" : "Out of Stock"),
          description: x.description || "",
          vendorStore: x.vendorStore || "Official Store",
          rating: x.rating || 5.0,
          isDeleted: x.isDeleted || false,
        });
      });
      setProducts(list);
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "marketplace_stores"), snap => {
      const list: VendorStore[] = [];
      snap.forEach(d => {
        const x = d.data();
        const ownerId = x.ownerId || x.id || d.id;
        list.push({
          id: d.id,
          storeName: x.storeName || "",
          ownerName: x.ownerName || "",
          email: x.email || x.ownerEmail || "",
          phone: x.phone || "",
          category: x.category || "General",
          commissionRate: x.commissionRate || 10,
          status: x.status || "PENDING",
          dateEnlisted: x.dateEnlisted || "",
          description: x.description || "",
          address: x.address || "",
          logoUrl: x.logoUrl || "",
          coverUrl: x.coverUrl || "",
          ownerId,
          storeRating: x.storeRating || 5.0,
          totalSales: x.totalSales || 0,
          isVerified: x.isVerified || false,
          isFeatured: x.isFeatured || false,
          featuredRank: x.featuredRank || 0,
          isDemo: x.isDemo || false,
          isDeleted: x.isDeleted || false,
        });
      });
      setStores(list);
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "marketplace_orders"), snap => {
      const list: MarketplaceOrder[] = [];
      snap.forEach(d => {
        const x = d.data();
        list.push({
          id: d.id,
          orderNumber: x.orderNumber || d.id.slice(0, 8),
          customerName: x.customerName || "",
          storeName: x.storeName || "",
          itemsCount: x.itemsCount || 1,
          totalPrice: x.totalPrice || 0,
          status: x.status || "PAID",
          date: x.date || "",
        });
      });
      setMarketplaceOrders(list);
    }, console.error));
    unsubs.push(onSnapshot(collection(db, "vendor_payout_requests"), snap => {
      const list: VendorPayoutRequest[] = [];
      snap.forEach(d => {
        const x = d.data();
        list.push({
          id: d.id,
          vendorId: x.vendorId || "",
          storeName: x.storeName || "Vendor Store",
          amount: x.amount || 0,
          bankName: x.bankName || "",
          accountNumber: x.accountNumber || "",
          status: x.status || "PENDING",
          requestedAt: x.requestedAt,
        });
      });
      setPayoutRequests(list);
    }, console.error));
    unsubs.push(onSnapshot(doc(db, "system_config", "global_settings"), s => {
      if (s.exists()) setSettings((prev: any) => ({ ...prev, ...s.data() }));
    }, () => {}));
    getDoc(doc(db, "system_config", "pricing")).then(s => { if (s.exists()) setSettings((prev: any) => ({ ...prev, ...s.data() })); }).catch(() => {});
    return () => unsubs.forEach(f => f());
  }, [currentUser]);

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault(); setAuthErr(""); setAuthOk("");
    try {
      if (signingUp) {
        const cred = await createUserWithEmailAndPassword(auth, email, password);
        await setDoc(doc(db, "users", cred.user.uid), {
          uid: cred.user.uid,
          id: cred.user.uid,
          email,
          name: email.split("@")[0],
          role: signupRole,
          createdAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        });
        const token = await cred.user.getIdToken();
        document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`;
        setAuthOk((signupRole === "dispatcher" ? "Dispatcher" : "Admin") + " account created.");
      } else {
        const cred = await signInWithEmailAndPassword(auth, email, password);
        const snap = await getDoc(doc(db, "users", cred.user.uid));
        if (!snap.exists()) {
          // Self-heal: If authenticated user was missing a Firestore doc, create it
          await setDoc(doc(db, "users", cred.user.uid), {
            uid: cred.user.uid,
            id: cred.user.uid,
            email,
            name: email.split("@")[0],
            role: "super_admin",
            createdAt: Timestamp.now(),
            updatedAt: Timestamp.now()
          });
        }
        const token = await cred.user.getIdToken();
        document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`;
        setAuthOk("Signed in. Loading dashboard...");
      }
    } catch (err: any) { setAuthErr(err?.message || "Operation failed. Please try again."); }
  };

  const updateSetting = async (key: string, val: any) => {
    setSettings((prev: any) => ({ ...prev, [key]: val }));
    await setDoc(doc(db, "system_config", "global_settings"), { [key]: val, updatedAt: Timestamp.now() }, { merge: true });
    addLog("Setting", key + " = " + JSON.stringify(val));
  };

  const marketplaceEnabled = settings.marketplaceEnabled !== false;
  const toggleMarketplace = async () => {
    const nextVal = !marketplaceEnabled;
    await updateSetting("marketplaceEnabled", nextVal);
    addToast("success", `Marketplace is now ${nextVal ? "ENABLED (Live on App)" : "DISABLED (Hidden on App)"}`);
  };

  const [seeding, setSeeding] = useState("");
  const seedUsersWrapper = async () => { setSeeding("users"); await seedUsers(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedDeliveriesWrapper = async () => { setSeeding("deliveries"); await seedDeliveries(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedBannersWrapper = async () => { setSeeding("banners"); await seedBanners(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedPromosWrapper = async () => { setSeeding("promos"); await seedPromos(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedReferralsWrapper = async () => { setSeeding("referrals"); await seedReferrals(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedMarketplaceWrapper = async () => { setSeeding("marketplace"); await seedMarketplace(db, addLog, addToast, createNotification); setSeeding(""); };
  const seedAppContentWrapper = async () => { setSeeding("appcontent"); await seedAppContent(db, addLog, addToast, createNotification, setSettings); setSeeding(""); };


  const activeUsers = users.filter(u => !u.isDeleted);
  const customers = activeUsers.filter(u => u.role === "customer" || u.role === "");
  const drivers = activeUsers.filter(u => u.role === "rider");
  const adminUsers = activeUsers.filter(u => u.role === "admin" || u.role === "super_admin");
  const pendingDeliveries = deliveries.filter(d => ["PENDING", "QUEUED", "RESERVED_NEXT"].includes(d.status));
  const inTransit = deliveries.filter(d => ["TRANSIT", "ASSIGNED", "PICKED_UP", "ARRIVED", "OUT_FOR_DELIVERY", "HANDOVER_VERIFIED"].includes(d.status));
  const delivered = deliveries.filter(d => d.status === "DELIVERED");
  const totalRevenue = delivered.reduce((s, d) => s + (d.price || 0), 0);
  const totalBookedGmv = deliveries.reduce((s, d) => s + (d.price || 0), 0);
  const totalTips = delivered.reduce((s, d) => s + (d.tipAmount || 0), 0);
  const completedReferrals = referrals.filter(r => r.status === "completed");

  if (loading) return (
    <div className="min-h-screen bg-[#111] flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <EdLogoSvg size={48} />
        <div className="text-[#FFB800] font-black flex items-center gap-3"><RefreshCw className="w-5 h-5 animate-spin" /> LOADING...</div>
      </div>
    </div>
  );

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-[#0A0A0A] flex flex-col items-center justify-center p-6 relative">
        <div className="absolute top-6 right-6">
          <button 
            type="button"
            onClick={toggleDark} 
            className="px-3.5 py-2 rounded-2xl bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-gray-900 dark:text-white shadow-sm hover:scale-105 transition-all cursor-pointer flex items-center gap-2 text-xs font-bold"
          >
            {dark ? <Sun className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" /> : <Moon className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" />}
            {dark ? "Light Mode" : "Dark Mode"}
          </button>
        </div>
        <div className="w-full max-w-md bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-8 shadow-2xl animate-scale-in">
          <div className="flex flex-col items-center mb-6">
            <div className="w-16 h-16 bg-[#FFB800] rounded-3xl flex items-center justify-center shadow-lg mb-4"><EdLogoSvg size={36} dark /></div>
            <h1 className="text-xl font-black text-gray-900 dark:text-white tracking-wide text-center">{settings.appName || "ESDISPATCH"}</h1>
            <p className="text-xs text-amber-800 dark:text-[#FFB800] font-black tracking-widest mt-1">ADMIN CONTROL CENTER</p>
          </div>
          {authErr && <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-400 text-xs flex items-center gap-2"><AlertTriangle className="w-4 h-4 shrink-0" /> {authErr}</div>}
          {authOk && <div className="mb-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl text-green-700 dark:text-green-400 text-xs flex items-center gap-2"><ShieldCheck className="w-4 h-4 shrink-0" /> {authOk}</div>}
          <form onSubmit={handleAuth} className="space-y-4">
            <div><label className="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-1">Admin Email</label>
              <div className="relative"><Mail className="absolute left-3.5 top-3 w-4 h-4 text-gray-500 dark:text-gray-400" />
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="admin@engraced.com" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl pl-10 pr-4 py-2.5 text-sm text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 focus:border-[#FFB800]" required /></div></div>
            <div><label className="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-1">Password</label>
              <div className="relative"><Key className="absolute left-3.5 top-3 w-4 h-4 text-gray-500 dark:text-gray-400" />
                <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl pl-10 pr-4 py-2.5 text-sm text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 focus:border-[#FFB800]" required /></div></div>
            {signingUp && <div><label className="block text-[10px] font-black text-gray-800 dark:text-gray-200 mb-1 uppercase tracking-wider">ROLE</label>
                  <Select value={signupRole} onChange={setSignupRole} options={[{value:"super_admin",label:"Super Admin (full access)"},{value:"admin",label:"Admin (restricted)"},{value:"dispatcher",label:"Dispatcher (orders only)"}]} className="w-full" /></div>}
            <button type="submit" className="w-full bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black py-3 rounded-xl transition-all shadow-lg flex items-center justify-center gap-2 text-sm tracking-wider cursor-pointer">
              <Lock className="w-4 h-4" /> {signingUp ? "CREATE ADMIN" : "SIGN IN"}</button>
          </form>
          <div className="mt-6 text-center">
            <button type="button" onClick={() => setSigningUp(!signingUp)} className="text-xs text-amber-800 dark:text-[#FFB800] hover:underline font-bold cursor-pointer">
              {signingUp ? "Already have an account? Sign In" : "Create an admin account"}</button>
          </div>
        </div>
      </div>
    );
  }

  // Purely real active deliveries for today — no simulated default tiers
  const todayStr = new Date().toISOString().slice(0, 10);
  const isDeliveryToday = (d: any) => {
    if (!d.dateString) return true;
    return d.dateString.startsWith("Today") || d.dateString.includes(todayStr) || d.dateString === "";
  };
  const activeDeliveriesToday = deliveries.filter(d => d.status !== "DELIVERED" && d.status !== "CANCELLED" && isDeliveryToday(d));
  const categoriesWithActive = Array.from(new Set(activeDeliveriesToday.map(d => d.category || "General").filter(Boolean)));
  const themes = ["gold", "white", "black"] as const;

  const activeDeliveriesData = categoriesWithActive.map((cat, idx) => {
    const catActive = activeDeliveriesToday.filter(d => (d.category || "General") === cat);
    const catInTransit = catActive.filter(d => ["TRANSIT", "OUT_FOR_DELIVERY", "PICKED_UP", "ARRIVED", "HANDOVER_VERIFIED"].includes(d.status));
    const catPending = catActive.filter(d => ["PENDING", "QUEUED", "RESERVED_NEXT"].includes(d.status));
    return {
      tag: cat,
      title: cat + " Deliveries",
      progress: catInTransit.length,
      total: catActive.length,
      unit: "drops",
      active: catActive.length,
      inTransit: catInTransit.length,
      pending: catPending.length,
      theme: themes[idx % themes.length],
      deliveries: catActive,
    };
  });

  const allNavItems: { id: TabId; label: string; icon: React.ReactNode; roles: string[]; badge?: number }[] = [
    { id: "dashboard", label: "Dashboard", icon: <Folder size={22} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "marketplace", label: "Marketplace & Stores", icon: <ShoppingBag size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "shipments", label: "Shipments", icon: <Package size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"], badge: pendingDeliveries.length > 0 ? pendingDeliveries.length : undefined },
    { id: "tracking", label: "Live Tracking", icon: <MapPin size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "users", label: "Users", icon: <Users size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "banners", label: "Hero Slides", icon: <ImageIcon size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "referrals", label: "Referrals", icon: <Gift size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "promotions", label: "Promotions", icon: <Percent size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "appcards", label: "App Cards", icon: <Layers size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "settings", label: "Settings", icon: <Settings size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "cms", label: "Site Content", icon: <FileText size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
    { id: "support", label: "Live Support", icon: <MessageSquare size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
    { id: "logs", label: "Audit Log", icon: <Headphones size={24} strokeWidth={2} />, roles: ["super_admin", "admin"] },
  ];
  const navItems = allNavItems.filter(n => n.roles.includes(userRole || "super_admin"));












function getDynamicPassword(email: string, pin: string): string {
  const cleanPrefix = (email.split("@")[0] || "").toLowerCase().trim();
  let hash = 0;
  for (let i = 0; i < cleanPrefix.length; i++) {
    hash = ((hash << 5) - hash) + cleanPrefix.charCodeAt(i);
    hash |= 0;
  }
  const hashStr = Math.abs(hash).toString().slice(0, 6).padEnd(6, 's');
  return `${pin}${pin}_${hashStr}`;
}

function UsersTab({ activeUsers, searchQuery, db, addLog, addToast, createNotification }: { 
  activeUsers: UserProfile[]; 
  searchQuery: string; 
  db: any; 
  addLog: any; 
  addToast?: (type: Toast["type"], message: string) => void;
  createNotification?: (title: string, desc: string) => Promise<void>;
}) {
    const [search, setSearch] = useState("");
    const [editUser, setEditUser] = useState<UserProfile | null>(null);
    const [previewUser, setPreviewUser] = useState<UserProfile | null>(null);
    const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
    const [showNewUser, setShowNewUser] = useState(false);
    const [newUserStep, setNewUserStep] = useState(1);
    const [newUserForm, setNewUserForm] = useState({ name: "", email: "", phone: "", role: "customer", pin: "", confirmPin: "", bikeNumber: "" });
    const [creatingUser, setCreatingUser] = useState(false);
    const [fundUser, setFundUser] = useState<UserProfile | null>(null);
    const [fundAction, setFundAction] = useState<"credit" | "debit">("credit");
    const [fundAmount, setFundAmount] = useState("");
    const [fundReason, setFundReason] = useState("");
    const [fundingWallet, setFundingWallet] = useState(false);
    const [form, setForm] = useState({ name: "", role: "", phone: "", bikeNumber: "", status: "" });
    const [uPage, setUPage] = useState(0);
    const uPerPage = 20;
    const filtered = activeUsers.filter(u => { const q = (searchQuery || search).toLowerCase(); return u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q) || (u.phone && u.phone.includes(q)); });
    const uTotalPages = Math.max(1, Math.ceil(filtered.length / uPerPage));
    const pagedUsers = filtered.slice(uPage * uPerPage, (uPage + 1) * uPerPage);
    useEffect(() => { setUPage(0); }, [search, searchQuery]);
    const saveUser = async () => {
      if (!editUser) return;
      await updateDoc(doc(db, "users", editUser.id), { name: form.name || editUser.name, role: form.role || editUser.role, phone: form.phone || editUser.phone, bikeNumber: form.bikeNumber || editUser.bikeNumber, status: form.status || editUser.status, updatedAt: Timestamp.now() });
      addLog("Update User", editUser.name + " -> " + (form.name || editUser.name));
      if (addToast) addToast("success", `Updated details for ${form.name || editUser.name}`);
      setEditUser(null);
    };
    const deleteUser = async (id: string) => { await updateDoc(doc(db, "users", id), { isDeleted: true, updatedAt: Timestamp.now() }); addLog("Delete User", "Soft-deleted " + id); if (addToast) addToast("info", "User deactivated"); setConfirmDelete(null); };
    const promoteToVendor = async (u: UserProfile) => {
      try {
        const today = new Date().toISOString().slice(0, 10);
        await setDoc(doc(db, "marketplace_stores", u.id), {
          id: u.id, ownerId: u.id, storeName: u.name + "'s Store", category: "General",
          ownerName: u.name, email: u.email, phone: u.phone, description: "", address: "",
          commissionRate: 8.5, vendorBalance: 0, totalSales: 0, storeRating: 5.0,
          isVerified: true, isPendingReview: false, kycStatus: "approved", status: "APPROVED",
          dateEnlisted: today, createdAt: Timestamp.now(), verifiedAt: Timestamp.now(), updatedAt: Timestamp.now()
        }, { merge: true });
        await updateDoc(doc(db, "users", u.id), { role: "vendor", userRole: "Vendor", isVendorVerified: true, updatedAt: Timestamp.now() });
        addLog("Upgrade Vendor", `Upgraded '${u.name}' (${u.email}) to vendor with approved storefront`);
        if (addToast) addToast("success", `Upgraded ${u.name} to Vendor!`);
      } catch (err: any) { addLog("Error", "Upgrade to vendor failed: " + (err.message || "unknown")); if (addToast) addToast("error", "Failed to upgrade vendor"); }
    };
    const handleFundWallet = async () => {
      const amt = parseFloat(fundAmount);
      if (isNaN(amt) || amt <= 0 || !fundUser) return;
      setFundingWallet(true);
      try {
        const currentBal = fundUser.walletBalance || 0;
        const requestedDelta = fundAction === "credit" ? amt : -amt;
        const newBal = Math.max(0, currentBal + requestedDelta);
        const actualDelta = newBal - currentBal;
        
        const primaryDocId = fundUser.id || fundUser.uid;
        await setDoc(doc(db, "users", primaryDocId), {
          walletBalance: newBal,
          balance: newBal,
          wallet_balance: newBal,
          updatedAt: Timestamp.now()
        }, { merge: true });

        // Also update by uid if distinct, ensuring older/migrated accounts are synchronized
        if (fundUser.uid && fundUser.uid !== primaryDocId) {
          try {
            await setDoc(doc(db, "users", fundUser.uid), {
              walletBalance: newBal,
              balance: newBal,
              wallet_balance: newBal,
              updatedAt: Timestamp.now()
            }, { merge: true });
          } catch (_) {}
        }

        const txId = "TXN-" + Date.now();
        const txDoc = {
          id: txId,
          userId: primaryDocId,
          userName: fundUser.name,
          title: fundReason || (fundAction === "credit" ? "Wallet Top-up" : "Wallet Debit"),
          amount: Math.abs(actualDelta),
          type: fundAction === "credit" ? "CREDIT" : "DEBIT",
          isTopUp: fundAction === "credit",
          date: new Date().toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }),
          status: "SUCCESS",
          timestamp: Timestamp.now(),
          createdAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        };

        // Write to user's personal ledger subcollection (mobile app listens to this)
        try {
          await setDoc(doc(db, "users", primaryDocId, "transactions", txId), txDoc);
        } catch (subTxErr) {
          console.warn("Personal transactions subcollection write note:", subTxErr);
        }

        if (fundUser.uid && fundUser.uid !== primaryDocId) {
          try {
            await setDoc(doc(db, "users", fundUser.uid, "transactions", txId), txDoc);
          } catch (_) {}
        }

        // Sync legacy email-keyed records if any exist
        if (fundUser.email) {
          try {
            const emailSnap = await getDocs(query(collection(db, "users"), where("email", "==", fundUser.email)));
            emailSnap.forEach(async (dSnap) => {
              if (dSnap.id !== primaryDocId && dSnap.id !== fundUser.uid) {
                try {
                  await setDoc(doc(db, "users", dSnap.id), {
                    walletBalance: newBal,
                    balance: newBal,
                    wallet_balance: newBal,
                    updatedAt: Timestamp.now()
                  }, { merge: true });
                  await setDoc(doc(db, "users", dSnap.id, "transactions", txId), txDoc);
                } catch (_) {}
              }
            });
          } catch (_) {}
        }

        // Also sync by phone if present
        if (fundUser.phone) {
          try {
            const phoneSnap = await getDocs(query(collection(db, "users"), where("phone", "==", fundUser.phone)));
            phoneSnap.forEach(async (dSnap) => {
              if (dSnap.id !== primaryDocId && dSnap.id !== fundUser.uid) {
                try {
                  await setDoc(doc(db, "users", dSnap.id), {
                    walletBalance: newBal,
                    balance: newBal,
                    wallet_balance: newBal,
                    updatedAt: Timestamp.now()
                  }, { merge: true });
                  await setDoc(doc(db, "users", dSnap.id, "transactions", txId), txDoc);
                } catch (_) {}
              }
            });
          } catch (_) {}
        }

        // Top-level ledger mirror in try/catch so subcollection funding never fails if root has constraint
        try {
          await setDoc(doc(db, "transactions", txId), txDoc);
        } catch (rootTxErr) {
          console.warn("Top-level transaction mirror note:", rootTxErr);
        }

        // Send in-app notification directly to user's notifications subcollection
        try {
          await addDoc(collection(db, "users", primaryDocId, "notifications"), {
            title: fundAction === "credit" ? "Wallet Credited!" : "Wallet Debited",
            message: fundReason 
              ? `${fundReason} (₦${amt.toLocaleString()})`
              : `Your account was ${fundAction === "credit" ? "credited with" : "debited by"} ₦${amt.toLocaleString()} by Admin.`,
            amount: actualDelta,
            read: false,
            createdAt: Timestamp.now()
          });
        } catch (nErr) {
          console.warn("Could not dispatch user in-app notification:", nErr);
        }

        addLog("Wallet Adjustment", `${fundAction.toUpperCase()} ₦${amt.toLocaleString()} for ${fundUser.name} (${fundUser.email}). New balance: ₦${newBal.toLocaleString()}`);
        if (addToast) {
          addToast("success", `Successfully ${fundAction === "credit" ? "credited" : "debited"} ₦${amt.toLocaleString()} for ${fundUser.name}`);
        }
        if (createNotification) {
          createNotification("Wallet Funded", `${fundAction.toUpperCase()} ₦${amt.toLocaleString()} for ${fundUser.name}`);
        }
        setFundUser(null);
        setFundAmount("");
        setFundReason("");
      } catch (err: any) {
        addLog("Error", "Fund wallet failed: " + (err.message || "unknown"));
        if (addToast) addToast("error", "Fund wallet failed: " + (err.message || "unknown"));
      }
      setFundingWallet(false);
    };
    const createUser = async () => {
      if (newUserForm.pin !== newUserForm.confirmPin) { addLog("Error", "PIN mismatch"); if (addToast) addToast("error", "PIN mismatch"); return; }
      if (newUserForm.pin.length < 4) { addLog("Error", "PIN must be at least 4 digits"); if (addToast) addToast("error", "PIN must be at least 4 digits"); return; }
      setCreatingUser(true);
      try {
        const derivedPwd = getDynamicPassword(newUserForm.email, newUserForm.pin);
        const secondaryAuth = getSecondaryAuth();
        const cred = await createUserWithEmailAndPassword(secondaryAuth, newUserForm.email, derivedPwd);
        
        const isRider = newUserForm.role === "rider";
        const isAdmin = newUserForm.role === "admin";
        const isVendor = newUserForm.role === "vendor";
        const bikeNum = newUserForm.bikeNumber || (isRider ? `ES-BIKE-${Math.floor(100 + Math.random() * 900)}` : "");

        await setDoc(doc(db, "users", cred.user.uid), {
          uid: cred.user.uid,
          id: cred.user.uid,
          name: newUserForm.name,
          email: newUserForm.email,
          phone: newUserForm.phone,
          role: newUserForm.role,
          userRole: isRider ? "Rider" : (isVendor ? "Vendor" : (isAdmin ? "Admin" : "Customer")),
          bikeNumber: bikeNum,
          pin: newUserForm.pin,
          status: isRider ? "active" : "offline",
          riderStatus: isRider ? "active" : "offline",
          isOnline: isRider ? true : false,
          walletBalance: 0,
          loyaltyPoints: 0,
          deliveryCount: 0,
          rating: 5.0,
          photoUrl: "",
          isDeleted: false,
          createdAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        });

        if (isVendor) {
          const today = new Date().toISOString().slice(0, 10);
          await setDoc(doc(db, "marketplace_stores", cred.user.uid), {
            id: cred.user.uid, ownerId: cred.user.uid, storeName: newUserForm.name + "'s Store", category: "General",
            ownerName: newUserForm.name, email: newUserForm.email, phone: newUserForm.phone, description: "", address: "",
            commissionRate: 8.5, vendorBalance: 0, totalSales: 0, storeRating: 5.0,
            isVerified: true, isPendingReview: false, kycStatus: "approved", status: "APPROVED",
            dateEnlisted: today, createdAt: Timestamp.now(), verifiedAt: Timestamp.now(), updatedAt: Timestamp.now()
          });
          await updateDoc(doc(db, "users", cred.user.uid), { userRole: "Vendor", isVendorVerified: true });
        }

        addLog("Create User", `Created ${newUserForm.role} '${newUserForm.name}' (${newUserForm.email})`);
        if (addToast) addToast("success", `Created ${newUserForm.role} ${newUserForm.name}`);
        setShowNewUser(false);
        setNewUserForm({ name: "", email: "", phone: "", role: "customer", pin: "", confirmPin: "", bikeNumber: "" });
        setNewUserStep(1);
      } catch (err: any) {
        let msg = err.message || "Failed to create user";
        if (err.code === "auth/email-already-in-use") {
          msg = "This email is already in use. Please use a different email.";
        } else if (err.code === "auth/invalid-email") {
          msg = "The provided email address is invalid.";
        } else if (err.code === "auth/weak-password") {
          msg = "The generated password from this PIN is too weak.";
        }
        addLog("Error", "Create user failed: " + msg);
        if (addToast) addToast("error", "Create user failed: " + msg);
      } finally {
        try {
          const secondaryAuth = getSecondaryAuth();
          await signOut(secondaryAuth);
        } catch (_) {}
        setCreatingUser(false);
      }
    };

    return <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div><h1 className="text-xl font-black text-gray-900 dark:text-white flex items-center gap-2"><Users className="w-5 h-5 text-amber-800 dark:text-[#FFB800]" /> Users</h1>
          <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{filtered.length} active registered users</p></div>
        <div className="flex items-center gap-3">
          <button onClick={() => setShowNewUser(true)} className="px-4 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm hover:shadow-md transition-all cursor-pointer"><UserPlus className="w-4 h-4" /> Add User</button>
          <div className="flex-1 sm:flex-none"><SearchInput value={search} onChange={setSearch} placeholder="Search users..." /></div>
        </div>
      </div>
      {showNewUser && <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 shadow-sm space-y-4 animate-scale-in">
        <div className="flex items-center justify-between">
          <h3 className="text-base font-black text-gray-900 dark:text-white flex items-center gap-2"><UserPlus className="w-4 h-4 text-amber-800 dark:text-[#FFB800]" /> New User — Step {newUserStep}/2</h3>
          <button onClick={() => { setShowNewUser(false); setNewUserStep(1); }} className="text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white cursor-pointer"><X className="w-4 h-4" /></button>
        </div>
        <div className="flex gap-2">
          <div className={"h-1.5 flex-1 rounded-full " + (newUserStep >= 1 ? "bg-[#FFB800]" : "bg-gray-200 dark:bg-gray-700")} />
          <div className={"h-1.5 flex-1 rounded-full " + (newUserStep >= 2 ? "bg-[#FFB800]" : "bg-gray-200 dark:bg-gray-700")} />
        </div>
        {newUserStep === 1 ? (
          <div className="space-y-3">
            <div className="grid sm:grid-cols-2 gap-3">
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Full Name *</label>
                <input value={newUserForm.name} onChange={e => setNewUserForm(f => ({ ...f, name: e.target.value }))} placeholder="e.g. Osas Ighodaro" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Email Address *</label>
                <input type="email" value={newUserForm.email} onChange={e => setNewUserForm(f => ({ ...f, email: e.target.value }))} placeholder="e.g. user@esdispatch.com" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
            </div>
            <div className="grid sm:grid-cols-2 gap-3">
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Phone Number</label>
                <input value={newUserForm.phone} onChange={e => setNewUserForm(f => ({ ...f, phone: e.target.value }))} placeholder="e.g. 08012345678" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Account Role</label>
                <Select value={newUserForm.role} onChange={v => setNewUserForm(f => ({ ...f, role: v }))} options={[{ value: "customer", label: "Customer" }, { value: "rider", label: "Rider / Courier" }, { value: "vendor", label: "Vendor" }, { value: "admin", label: "Admin" }]} /></div>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button onClick={() => { if (newUserForm.name && newUserForm.email) setNewUserStep(2); else { addLog("Error", "Fill in name and email first"); if (addToast) addToast("error", "Fill in name and email first"); } }} className="px-6 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black transition-all cursor-pointer">Next →</button>
            </div>
          </div>
        ) : (
          <div className="space-y-3">
            {newUserForm.role === "rider" && <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Bike / Plate Number</label>
              <input value={newUserForm.bikeNumber} onChange={e => setNewUserForm(f => ({ ...f, bikeNumber: e.target.value }))} placeholder="e.g. ES-BIKE-204" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>}
            <div className="grid sm:grid-cols-2 gap-3">
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Set 4-Digit PIN *</label>
                <input type="password" maxLength={6} value={newUserForm.pin} onChange={e => setNewUserForm(f => ({ ...f, pin: e.target.value }))} placeholder="••••" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white font-mono tracking-widest focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
              <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Confirm PIN *</label>
                <input type="password" maxLength={6} value={newUserForm.confirmPin} onChange={e => setNewUserForm(f => ({ ...f, confirmPin: e.target.value }))} placeholder="••••" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white font-mono tracking-widest focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
            </div>
            <div className="flex items-center justify-between gap-2 pt-2">
              <button onClick={() => setNewUserStep(1)} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">← Back</button>
              <button onClick={createUser} disabled={creatingUser || !newUserForm.pin || !newUserForm.confirmPin} className="px-6 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 disabled:opacity-50 text-[#111] rounded-xl text-xs font-black shadow-sm transition-all flex items-center gap-1.5 cursor-pointer">
                {creatingUser ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4" />} Create User
              </button>
            </div>
          </div>
        )}
      </div>}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead className="bg-gray-100 dark:bg-[#222] border-b border-gray-200 dark:border-white/10">
              <tr>
                <th className="text-left font-extrabold text-gray-700 dark:text-gray-300 p-3.5 uppercase tracking-wider text-[11px]">User & Email</th>
                <th className="text-left font-extrabold text-gray-700 dark:text-gray-300 p-3.5 uppercase tracking-wider text-[11px] hidden md:table-cell">Role & Presence</th>
                <th className="text-right font-extrabold text-gray-700 dark:text-gray-300 p-3.5 uppercase tracking-wider text-[11px]">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-black/5 dark:divide-white/10">
              {pagedUsers.length === 0 && <tr><td colSpan={3} className="p-8 text-center text-gray-500 dark:text-gray-400 font-medium">No users found.</td></tr>}
              {pagedUsers.map((u, i) => (
                <tr key={u.id} className={"hover:bg-gray-50 dark:hover:bg-white/5 transition-colors animate-fade-in " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6","stagger-7","stagger-8"][i] || "")}>
                  <td className="p-3.5">
                    <div className="flex items-center gap-2.5">
                      <div className="relative shrink-0">
                        <div className="w-9 h-9 rounded-xl bg-amber-100 dark:bg-amber-950/40 flex items-center justify-center text-xs font-black text-amber-900 dark:text-[#FFB800] border border-amber-300 dark:border-amber-700/40 shadow-xs">
                          {u.name.charAt(0).toUpperCase()}
                        </div>
                        {u.isOnline === true && (
                          <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-[#1a1a1a]" title="Online" />
                        )}
                      </div>
                      <div className="min-w-0">
                        <p className="font-bold text-xs text-gray-900 dark:text-white flex items-center gap-1.5 leading-tight truncate">
                          <span>{u.name}</span>
                          {u.role === "rider" && u.bikeNumber && (
                            <span className="text-[9px] font-mono font-bold px-1.5 py-0.5 bg-gray-100 dark:bg-white/10 rounded-md text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10">
                              {u.bikeNumber}
                            </span>
                          )}
                        </p>
                        <p className="text-[11px] text-gray-600 dark:text-gray-400 font-medium leading-tight truncate mt-0.5">
                          {u.email}
                        </p>
                        {u.phone && (
                          <p className="text-[10px] text-gray-500 dark:text-gray-400 font-mono leading-tight mt-0.5">
                            {u.phone}
                          </p>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="p-3.5 hidden md:table-cell">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className={"text-[10px] font-extrabold px-2.5 py-1 rounded-full shadow-2xs " + rBadge(u.role)}>
                        {(u.role || "customer").toUpperCase()}
                      </span>
                      {u.isOnline === true ? (
                        <span className="inline-flex items-center gap-1.5 text-[10px] font-black px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-300 border border-emerald-500/30 shadow-2xs">
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                          Online
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 text-[10px] font-bold px-2.5 py-1 rounded-full bg-gray-100 dark:bg-white/10 text-gray-600 dark:text-gray-400 border border-gray-200 dark:border-white/10">
                          <span className="w-1.5 h-1.5 rounded-full bg-gray-400 dark:bg-gray-500" />
                          Offline
                        </span>
                      )}
                      {u.status && u.status !== "active" && u.status !== "online" && u.status !== "offline" && (
                        <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-rose-50 text-rose-800 dark:bg-rose-950/40 dark:text-rose-300 border border-rose-200 dark:border-rose-900/30">
                          {u.status.toUpperCase()}
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="p-3.5 text-right whitespace-nowrap">
                    <button title="View Full Details" onClick={() => setPreviewUser(u)} className="p-2 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/30 rounded-lg transition-colors cursor-pointer"><Eye className="w-3.5 h-3.5" /></button>
                    {(u.role !== "vendor" && u.role !== "admin" && u.role !== "super_admin") && <button title="Upgrade to Vendor" onClick={() => promoteToVendor(u)} className="p-2 text-amber-800 dark:text-[#FFB800] hover:bg-amber-50 dark:hover:bg-white/5 rounded-lg transition-colors cursor-pointer"><Store className="w-3.5 h-3.5" /></button>}
                    <button title="Fund / Adjust Wallet" onClick={() => setFundUser(u)} className="p-2 text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 rounded-lg transition-colors cursor-pointer"><DollarSign className="w-3.5 h-3.5" /></button>
                    <button title="Edit User" onClick={() => { setEditUser(u); setForm({ name: u.name, role: u.role || "customer", phone: u.phone || "", bikeNumber: u.bikeNumber || "", status: u.status || "active" }); }} className="p-2 text-amber-800 dark:text-[#FFB800] hover:bg-amber-50 dark:hover:bg-white/5 rounded-lg transition-colors cursor-pointer"><Edit3 className="w-3.5 h-3.5" /></button>
                    <button title="Deactivate User" onClick={() => setConfirmDelete(u.id)} className="p-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-lg transition-colors cursor-pointer"><Trash2 className="w-3.5 h-3.5" /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {uTotalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
        <button onClick={() => setUPage(p => Math.max(0, p - 1))} disabled={uPage === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] cursor-pointer"><ChevronLeft size={14} /></button>
        {Array.from({ length: uTotalPages }, (_, i) => <button key={i} onClick={() => setUPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold transition-all cursor-pointer " + (i === uPage ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
        <button onClick={() => setUPage(p => Math.min(uTotalPages - 1, p + 1))} disabled={uPage >= uTotalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] cursor-pointer"><ChevronRight size={14} /></button>
      </div>}
      <ConfirmModal show={confirmDelete !== null} title="Delete User" message="Soft-delete this user? They will no longer be able to log in or transact." confirmLabel="Deactivate" onConfirm={() => deleteUser(confirmDelete!)} onCancel={() => setConfirmDelete(null)} />
      
      {/* User Details Preview Modal */}
      {previewUser && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setPreviewUser(null)}>
        <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
          <div className="flex items-center justify-between pb-3 border-b border-gray-200 dark:border-white/10">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-[#FFB800] text-[#111] flex items-center justify-center text-lg font-black shadow-md">
                {previewUser.name.charAt(0).toUpperCase()}
              </div>
              <div>
                <h3 className="text-base font-black text-gray-900 dark:text-white flex items-center gap-2">
                  {previewUser.name}
                  {previewUser.isOnline === true && (
                    <span className="inline-flex items-center gap-1 text-[10px] font-black px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-500/20">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                      Online
                    </span>
                  )}
                </h3>
                <p className="text-xs text-gray-600 dark:text-gray-300 font-medium">{previewUser.email}</p>
              </div>
            </div>
            <button onClick={() => setPreviewUser(null)} className="p-2 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white rounded-full hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer"><X size={18} /></button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-500/20 rounded-2xl p-3 text-center">
              <p className="text-[10px] font-extrabold uppercase text-emerald-800 dark:text-emerald-400 tracking-wider">Wallet</p>
              <p className="text-sm font-black text-emerald-700 dark:text-emerald-300 font-mono mt-0.5">₦{(previewUser.walletBalance || 0).toLocaleString()}</p>
            </div>
            <div className="bg-amber-50 dark:bg-amber-950/20 border border-amber-500/20 rounded-2xl p-3 text-center">
              <p className="text-[10px] font-extrabold uppercase text-amber-900 dark:text-[#FFB800] tracking-wider">Points</p>
              <p className="text-sm font-black text-gray-900 dark:text-white font-mono mt-0.5">{previewUser.loyaltyPoints || 0}</p>
            </div>
            <div className="bg-blue-50 dark:bg-blue-950/20 border border-blue-500/20 rounded-2xl p-3 text-center">
              <p className="text-[10px] font-extrabold uppercase text-blue-800 dark:text-blue-400 tracking-wider">Deliveries</p>
              <p className="text-sm font-black text-blue-700 dark:text-blue-300 font-mono mt-0.5">{previewUser.deliveryCount || 0}</p>
            </div>
            <div className="bg-purple-50 dark:bg-purple-950/20 border border-purple-500/20 rounded-2xl p-3 text-center">
              <p className="text-[10px] font-extrabold uppercase text-purple-800 dark:text-purple-400 tracking-wider">Rating</p>
              <p className="text-sm font-black text-purple-700 dark:text-purple-300 font-mono mt-0.5">{previewUser.rating ? `${previewUser.rating} ★` : "5.0 ★"}</p>
            </div>
          </div>

          <div className="bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-2xl p-4 space-y-2 text-xs">
            <div className="flex justify-between py-1 border-b border-gray-200 dark:border-white/10">
              <span className="text-gray-600 dark:text-gray-400 font-medium">Phone Number</span>
              <span className="font-bold text-gray-900 dark:text-white">{previewUser.phone || "None registered"}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-200 dark:border-white/10">
              <span className="text-gray-600 dark:text-gray-400 font-medium">Role</span>
              <span className={"font-bold px-2 py-0.5 rounded-full text-[10px] " + rBadge(previewUser.role)}>{(previewUser.role || "customer").toUpperCase()}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-200 dark:border-white/10">
              <span className="text-gray-600 dark:text-gray-400 font-medium">Presence</span>
              {previewUser.isOnline === true ? (
                <span className="font-black text-[10px] px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
                  ONLINE
                </span>
              ) : (
                <span className="font-bold text-[10px] px-2 py-0.5 rounded-full bg-gray-100 dark:bg-white/10 text-gray-600 dark:text-gray-400">
                  OFFLINE
                </span>
              )}
            </div>
            {previewUser.bikeNumber && <div className="flex justify-between py-1 border-b border-gray-200 dark:border-white/10">
              <span className="text-gray-600 dark:text-gray-400 font-medium">Assigned Bike / Vehicle</span>
              <span className="font-mono font-bold text-gray-900 dark:text-white">{previewUser.bikeNumber}</span>
            </div>}
          </div>

          <div className="flex items-center justify-between gap-3 pt-2">
            <div className="flex gap-2">
              <button onClick={() => { setFundUser(previewUser); setPreviewUser(null); }} className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-black flex items-center gap-1.5 transition-all cursor-pointer">
                <DollarSign size={14} /> Fund Wallet
              </button>
              <button onClick={() => { setEditUser(previewUser); setForm({ name: previewUser.name, role: previewUser.role || "customer", phone: previewUser.phone || "", bikeNumber: previewUser.bikeNumber || "", status: previewUser.status || "active" }); setPreviewUser(null); }} className="px-3.5 py-2 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 transition-all cursor-pointer">
                <Edit3 size={14} /> Edit
              </button>
            </div>
            <button onClick={() => setPreviewUser(null)} className="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">Close</button>
          </div>
        </div>
      </div>}

      {/* Fund User Wallet Modal */}
      {fundUser && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setFundUser(null)}>
        <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
          <h3 className="text-base font-black text-gray-900 dark:text-white flex items-center gap-2"><DollarSign className="w-5 h-5 text-emerald-600 dark:text-emerald-400" /> Fund & Manage Wallet</h3>
          <div className="bg-amber-50 dark:bg-amber-950/30 rounded-2xl p-3 border border-amber-300 dark:border-amber-700/40">
            <p className="text-xs font-bold text-gray-900 dark:text-white">{fundUser.name}</p>
            <p className="text-[10px] text-gray-700 dark:text-gray-300 font-medium">{fundUser.email} · Current Balance: <span className="font-bold text-emerald-700 dark:text-emerald-400">₦{(fundUser.walletBalance || 0).toLocaleString()}</span></p>
          </div>
          <div className="space-y-3">
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Operation</label>
              <div className="flex gap-2">
                <button type="button" onClick={() => setFundAction("credit")} className={`flex-1 py-2 rounded-xl text-xs font-black transition-all cursor-pointer ${fundAction === "credit" ? "bg-emerald-600 text-white shadow-sm" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-white/10"}`}>Credit (+)</button>
                <button type="button" onClick={() => setFundAction("debit")} className={`flex-1 py-2 rounded-xl text-xs font-black transition-all cursor-pointer ${fundAction === "debit" ? "bg-red-600 text-white shadow-sm" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-white/10"}`}>Debit (-)</button>
              </div>
            </div>
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Amount (₦) *</label>
              <input type="number" min="1" value={fundAmount} onChange={e => setFundAmount(e.target.value)} placeholder="e.g. 5000" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 font-bold focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Reason / Narration</label>
              <input type="text" value={fundReason} onChange={e => setFundReason(e.target.value)} placeholder="e.g. Customer promo credit / order refund" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
          </div>
          <div className="flex items-center justify-end gap-3 pt-2 border-t border-gray-200 dark:border-white/10">
            <button onClick={() => setFundUser(null)} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">Cancel</button>
            <button onClick={handleFundWallet} disabled={fundingWallet || !fundAmount} className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-black transition-all disabled:opacity-50 cursor-pointer">
              {fundingWallet ? "Processing..." : `Confirm ${fundAction === "credit" ? "Credit" : "Debit"}`}
            </button>
          </div>
        </div>
      </div>}

      {/* Edit User Modal with proper Dropdowns */}
      {editUser && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setEditUser(null)}>
        <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
          <div className="flex items-center justify-between">
            <h3 className="text-base font-black text-gray-900 dark:text-white flex items-center gap-2"><Edit3 className="w-4 h-4 text-amber-800 dark:text-[#FFB800]" /> Edit User</h3>
            <span className="text-[10px] text-gray-500 dark:text-gray-400 font-mono">{editUser.email}</span>
          </div>
          <div className="space-y-3">
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Full Name</label>
              <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Phone Number</label>
              <input value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Role</label>
                <Select value={form.role || editUser.role} onChange={v => setForm(f => ({ ...f, role: v }))} 
                  options={[
                    { value: "customer", label: "Customer" },
                    { value: "rider", label: "Rider / Courier" },
                    { value: "vendor", label: "Vendor" },
                    { value: "admin", label: "Admin" },
                    { value: "dispatcher", label: "Dispatcher" }
                  ]} />
              </div>
              <div>
                <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Status</label>
                <Select value={form.status || editUser.status} onChange={v => setForm(f => ({ ...f, status: v }))}
                  options={[
                    { value: "active", label: "Active" },
                    { value: "offline", label: "Offline" },
                    { value: "suspended", label: "Suspended" },
                    { value: "pending", label: "Pending Review" }
                  ]} />
              </div>
            </div>
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Bike / Vehicle Number</label>
              <input value={form.bikeNumber} onChange={e => setForm(f => ({ ...f, bikeNumber: e.target.value }))} placeholder="e.g. ES-BIKE-204"
                className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
          </div>
          <div className="flex items-center justify-end gap-3 pt-2">
            <button onClick={() => setEditUser(null)} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">Cancel</button>
            <SaveBtn onClick={saveUser} label="Update User" />
          </div>
        </div>
      </div>}
    </div>;
  }

function ShipmentsTab({ deliveries, drivers, searchQuery, db, addLog, addToast, filterPrefill, setFilterPrefill }: { deliveries: Delivery[]; drivers: UserProfile[]; searchQuery: string; db: any; addLog: any; addToast?: (type: Toast["type"], message: string) => void; filterPrefill?: { status?: string; category?: string; search?: string; selectedId?: string } | null; setFilterPrefill?: (v: any) => void }) {
    const [search, setSearch] = useState(() => filterPrefill?.search || "");
    const [statusFilter, setStatusFilter] = useState(() => filterPrefill?.status || (filterPrefill?.selectedId ? "ALL" : "ACTION_NEEDED"));
    const [categoryFilter, setCategoryFilter] = useState(() => filterPrefill?.category || "ALL");
    const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(() => filterPrefill?.selectedId || null);
    const [selected, setSelected] = useState<Set<string>>(new Set());
    const [bulkStatus, setBulkStatus] = useState("");
    const [page, setPage] = useState(0);
    const [showNew, setShowNew] = useState(false);
    const [assignModal, setAssignModal] = useState<{ delivery: Delivery; show: boolean }>({ delivery: null as any, show: false });
    const [detailsModal, setDetailsModal] = useState<{ delivery: Delivery; show: boolean }>({ delivery: null as any, show: false });
    const [waybillModal, setWaybillModal] = useState<{ delivery: Delivery; show: boolean }>({ delivery: null as any, show: false });
    const [reassignModal, setReassignModal] = useState<{ delivery: Delivery; show: boolean }>({ delivery: null as any, show: false });
    const [confirmStatusModal, setConfirmStatusModal] = useState<{ delivery: Delivery; newStatus: string; show: boolean }>({ delivery: null as any, newStatus: "", show: false });
    const [confirmBulkModal, setConfirmBulkModal] = useState<{ show: boolean }>({ show: false });
    const [bulkAssignModal, setBulkAssignModal] = useState(false);
    const [riderSearch, setRiderSearch] = useState("");
    const [riderOnlineOnly, setRiderOnlineOnly] = useState(false);
    const [newForm, setNewForm] = useState({ receiverName: "", receiverPhone: "", deliveryAddress: "", senderName: "", senderPhone: "", itemName: "", pickupAddress: "", quantity: 1, weight: 1, price: 1500, category: "Standard", status: "PENDING", riderId: "", driverId: "", driverName: "" });
    const [creating, setCreating] = useState(false);
    const [decisionDelivery, setDecisionDelivery] = useState<Delivery | null>(null);
    const perPage = 15;

    useEffect(() => {
      if (filterPrefill) {
        if (filterPrefill.selectedId) {
          setSelectedShipmentId(filterPrefill.selectedId);
          setStatusFilter(filterPrefill.status || "ALL");
          setCategoryFilter(filterPrefill.category || "ALL");
          if (filterPrefill.search) setSearch(filterPrefill.search);
        } else {
          if (filterPrefill.status) setStatusFilter(filterPrefill.status);
          if (filterPrefill.category) setCategoryFilter(filterPrefill.category);
          if (filterPrefill.search) setSearch(filterPrefill.search);
        }
        if (setFilterPrefill) setFilterPrefill(null);
      }
    }, [filterPrefill, setFilterPrefill]);

    const getRiderActiveLoad = (riderId: string) => {
      return deliveries.filter(d => (d.riderId === riderId || d.driverId === riderId) && ["ASSIGNED", "TRANSIT", "OUT_FOR_DELIVERY"].includes(d.status)).length;
    };

    const decisionRiders = useMemo(() => {
      return drivers.map(d => ({
        uid: d.id || d.uid,
        name: d.name,
        phone: d.phone,
        isOnline: d.isOnline,
        status: d.status,
        rating: d.rating,
        deliveryCount: d.deliveryCount,
        currentLoad: getRiderActiveLoad(d.id),
        lat: d.lat,
        lng: d.lng,
        bikeNumber: d.bikeNumber,
      }));
    }, [drivers, deliveries]);

    const scoreRiderForDelivery = (rider: UserProfile, targetDelivery: Delivery | null, load: number) => {
      if (rider.isOnline === false) {
        return { score: 15, badge: "OFFLINE" as const, explanation: "Offline • Unavailable for dispatch" };
      }
      if (rider.status === "SUSPENDED" || rider.status === "DEACTIVATED") {
        return { score: 0, badge: "OFFLINE" as const, explanation: "Blocked • Account under review" };
      }

      let proximityScore = 24;
      let etaText = "5–10 min to pickup";

      if (rider.lat && rider.lng && targetDelivery?.pickupLat && targetDelivery?.pickupLng) {
        const lat1 = rider.lat;
        const lon1 = rider.lng;
        const lat2 = targetDelivery.pickupLat;
        const lon2 = targetDelivery.pickupLng;
        const R = 6371;
        const dLat = (lat2 - lat1) * (Math.PI / 180);
        const dLon = (lon2 - lon1) * (Math.PI / 180);
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        const distKm = R * c;

        if (distKm <= 2.5) {
          proximityScore = 35;
          etaText = `~${Math.max(3, Math.round(distKm * 2.5 + 2))}m away (${distKm.toFixed(1)}km)`;
        } else if (distKm <= 6) {
          proximityScore = 28;
          etaText = `~${Math.round(distKm * 2.2 + 3)}m away (${distKm.toFixed(1)}km)`;
        } else if (distKm <= 12) {
          proximityScore = 20;
          etaText = `~${Math.round(distKm * 2.0 + 5)}m away (${distKm.toFixed(1)}km)`;
        } else {
          proximityScore = 10;
          etaText = `${distKm.toFixed(1)}km from pickup`;
        }
      } else if (targetDelivery?.pickupAddress) {
        const beninZones = ["GRA", "Ring Road", "Ugbowo", "Airport Road", "Sapele Road", "Upper Sakponba", "Ikpoba Hill", "New Benin", "Uselu", "Ekenwan", "Siluko", "Aduwawa"];
        const pAddr = targetDelivery.pickupAddress.toLowerCase();
        const matched = beninZones.find(z => pAddr.includes(z.toLowerCase()));
        if (matched) {
          proximityScore = 30;
          etaText = `In/near ${matched}`;
        }
      }

      let workloadScore = 25;
      if (load === 1) workloadScore = 18;
      else if (load === 2) workloadScore = 10;
      else if (load >= 3) workloadScore = 2;

      let capacityScore = 15;
      if ((targetDelivery?.weight || 0) > 12) capacityScore = 8;

      let gpsScore = 12;
      let gpsAge = "GPS fresh";
      if (rider.updatedAt) {
        const ms = rider.updatedAt.toMillis ? rider.updatedAt.toMillis() : new Date(rider.updatedAt).getTime();
        const ageMin = Math.round((Date.now() - ms) / 60000);
        if (ageMin <= 3) {
          gpsScore = 15;
          gpsAge = "GPS fresh (<3m)";
        } else if (ageMin <= 10) {
          gpsScore = 11;
          gpsAge = `GPS seen ${ageMin}m ago`;
        } else {
          gpsScore = 5;
          gpsAge = `GPS stale (${ageMin}m)`;
        }
      }

      const rating = rider.rating || 5.0;
      const reliabilityScore = Math.min(10, Math.round(rating * 2));

      const totalScore = Math.min(100, Math.max(0, proximityScore + workloadScore + capacityScore + gpsScore + reliabilityScore));

      let badge: "BEST_FIT" | "RESERVE_NEXT" | "ELIGIBLE" | "BUSY" | "OFFLINE" = "ELIGIBLE";
      let explanation = "";

      if (load === 0 && totalScore >= 70) {
        badge = "BEST_FIT";
        explanation = `Best Fit: Free now • ${etaText} • ${gpsAge}`;
      } else if (load === 1 && totalScore >= 55) {
        badge = "RESERVE_NEXT";
        explanation = `Reserve Next: Finishing drop • ${etaText} • ${gpsAge}`;
      } else if (load >= 2) {
        badge = "BUSY";
        explanation = `Busy (${load} active drops) • ${etaText}`;
      } else {
        badge = "ELIGIBLE";
        explanation = `Eligible • ${etaText} • ${gpsAge}`;
      }

      return { score: totalScore, badge, explanation };
    };

    const filteredDrivers = drivers.filter(r => {
      if (riderOnlineOnly && !r.isOnline) return false;
      if (!riderSearch.trim()) return true;
      const q = riderSearch.toLowerCase();
      return (
        (r.name || "").toLowerCase().includes(q) ||
        (r.phone || "").toLowerCase().includes(q) ||
        (r.bikeNumber || "").toLowerCase().includes(q)
      );
    });

    const categories = ["ALL", "Express", "Standard", "Economy", "Batch", "Multi-Stop"];

    const filtered = deliveries.filter(d => {
      const q = (searchQuery || search).toLowerCase();
      const matchSearch = 
        d.receiverName.toLowerCase().includes(q) || 
        d.senderName.toLowerCase().includes(q) || 
        d.id.toLowerCase().includes(q) || 
        (d.itemName && d.itemName.toLowerCase().includes(q)) || 
        (d.deliveryAddress && d.deliveryAddress.toLowerCase().includes(q)) ||
        (d.courierName && d.courierName.toLowerCase().includes(q));

      let matchStatus = true;
      if (statusFilter === "ACTION_NEEDED") {
        matchStatus = ["PENDING", "QUEUED", "RESERVED_NEXT"].includes(d.status) || (!d.riderId && !d.driverId && d.status !== "DELIVERED" && d.status !== "CANCELLED");
      } else if (statusFilter === "IN_MOTION") {
        matchStatus = ["ASSIGNED", "PICKED_UP", "TRANSIT", "ARRIVED", "OUT_FOR_DELIVERY"].includes(d.status);
      } else if (statusFilter === "DELIVERED") {
        matchStatus = d.status === "DELIVERED";
      } else if (statusFilter === "ALL") {
        matchStatus = true;
      } else {
        matchStatus = d.status === statusFilter;
      }

      const matchCat = categoryFilter === "ALL" || d.category === categoryFilter;
      return matchSearch && matchStatus && matchCat;
    }).sort((a, b) => {
      const urgencyRank = (status: string, riderId?: string) => {
        if (["PENDING", "QUEUED", "RESERVED_NEXT"].includes(status) || !riderId) return 0;
        if (["ASSIGNED", "PICKED_UP", "TRANSIT", "ARRIVED", "OUT_FOR_DELIVERY"].includes(status)) return 1;
        if (status === "DELIVERED") return 2;
        return 3;
      };
      const rankA = urgencyRank(a.status, a.riderId || a.driverId);
      const rankB = urgencyRank(b.status, b.riderId || b.driverId);
      if (rankA !== rankB) return rankA - rankB;

      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.createdAt ? new Date(a.createdAt).getTime() : 0);
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.createdAt ? new Date(b.createdAt).getTime() : 0);
      return timeB - timeA;
    });

    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const paged = filtered.slice(page * perPage, (page + 1) * perPage);
    useEffect(() => { setPage(0); }, [search, searchQuery, statusFilter, categoryFilter]);

    const isValidStatusTransition = (current: string, next: string): boolean => {
      if (next === "CANCELLED") return true;
      const allowed: Record<string, string[]> = {
        PENDING: ["QUEUED", "RESERVED_NEXT", "ASSIGNED", "CANCELLED"],
        QUEUED: ["RESERVED_NEXT", "ASSIGNED", "CANCELLED"],
        RESERVED_NEXT: ["ASSIGNED", "CANCELLED"],
        ASSIGNED: ["PICKED_UP", "TRANSIT", "CANCELLED"],
        PICKED_UP: ["TRANSIT", "ARRIVED", "CANCELLED"],
        TRANSIT: ["ARRIVED", "OUT_FOR_DELIVERY", "CANCELLED"],
        ARRIVED: ["HANDOVER_VERIFIED", "OUT_FOR_DELIVERY", "CANCELLED"],
        OUT_FOR_DELIVERY: ["ARRIVED", "HANDOVER_VERIFIED", "DELIVERED", "CANCELLED"],
        HANDOVER_VERIFIED: ["DELIVERED", "CANCELLED"],
        DELIVERED: [],
        CANCELLED: []
      };
      return allowed[current]?.includes(next) || false;
    };

    const requiresRider = (status: string): boolean => {
      return ["ASSIGNED", "PICKED_UP", "TRANSIT", "ARRIVED", "OUT_FOR_DELIVERY", "HANDOVER_VERIFIED", "DELIVERED"].includes(status);
    };

    const updateStatus = async (id: string, s: string) => {
      const del = deliveries.find(d => d.id === id);
      if (!del) return;

      if (!isValidStatusTransition(del.status, s)) {
        alert(`Cannot change status from ${del.status} to ${s}. Invalid transition.`);
        return;
      }

      if (requiresRider(s) && !del.riderId && !del.driverId) {
        alert(`Cannot set status to ${s} without assigning a rider first.`);
        return;
      }

      setConfirmStatusModal({ delivery: del, newStatus: s, show: true });
    };

    const confirmUpdateStatus = async () => {
      const { delivery, newStatus } = confirmStatusModal;
      if (!delivery || !newStatus) return;

      try {
        await updateDoc(doc(db, "deliveries", delivery.id), { status: newStatus, updatedAt: Timestamp.now() });
        addLog("Status", idShort(delivery.id) + " -> " + newStatus);
        const del = deliveries.find(d => d.id === delivery.id);
        if (del && del.userId) {
          try {
            const statusMessages: Record<string, string> = {
              ASSIGNED: `${del.courierName || "A rider"} has been assigned to your shipment.`,
              TRANSIT: "Your shipment is in transit and on the way to the delivery address.",
              OUT_FOR_DELIVERY: `Your rider ${del.courierName || ""} is out for final delivery handover.`,
              DELIVERED: "Your package has been safely delivered. Thank you for choosing ESDispatch!",
              CANCELLED: "Your shipment order has been cancelled."
            };
            if (statusMessages[newStatus]) {
              const notifRef = doc(collection(db, "users", del.userId, "notifications"));
              await setDoc(notifRef, {
                id: notifRef.id,
                title: newStatus === "DELIVERED" ? "Package Delivered" : `Shipment ${newStatus.replace(/_/g, " ")}`,
                message: statusMessages[newStatus],
                time: "Just now",
                isRead: false,
                parcelId: delivery.id,
                createdAt: Timestamp.now()
              });
            }
          } catch (e) {
            console.error("Failed to post status notification:", e);
          }
        }
      } catch (e) {
        console.error("Failed to update status:", e);
        alert("Failed to update status. Please try again.");
      }
      setConfirmStatusModal({ delivery: null as any, newStatus: "", show: false });
    };

    const assignRider = async (deliveryId: string, rider: UserProfile) => {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        riderId: rider.id, driverId: rider.id, driverName: rider.name,
        courierName: rider.name, courierPhone: rider.phone, riderBikeNumber: rider.bikeNumber || "",
        status: "ASSIGNED", updatedAt: Timestamp.now()
      });
      addLog("Assign Rider", `${rider.name} → ${idShort(deliveryId)}`);

      const targetUserId = assignModal.delivery?.userId || deliveries.find(d => d.id === deliveryId)?.userId;
      if (targetUserId) {
        try {
          const notifRef = doc(collection(db, "users", targetUserId, "notifications"));
          await setDoc(notifRef, {
            id: notifRef.id,
            title: "Rider Assigned!",
            message: `${rider.name} (${rider.phone || "Active Courier"}) has been assigned to your shipment #${idShort(deliveryId)}.`,
            time: "Just now",
            isRead: false,
            parcelId: deliveryId,
            createdAt: Timestamp.now()
          });
        } catch (err) {
          console.error("Failed to notify customer of assigned rider:", err);
        }
      }

      setAssignModal({ delivery: null as any, show: false });
    };

    const reserveRider = async (deliveryId: string, rider: UserProfile) => {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        reservedRiderId: rider.id,
        reservedCourierName: rider.name,
        reservedCourierPhone: rider.phone,
        status: "RESERVED_NEXT",
        updatedAt: Timestamp.now()
      });
      addLog("Reserve Rider", `${rider.name} (Next) → ${idShort(deliveryId)}`);

      const targetUserId = assignModal.delivery?.userId || deliveries.find(d => d.id === deliveryId)?.userId;
      if (targetUserId) {
        try {
          const notifRef = doc(collection(db, "users", targetUserId, "notifications"));
          await setDoc(notifRef, {
            id: notifRef.id,
            title: "Rider Reserved",
            message: `${rider.name} has been reserved for your shipment #${idShort(deliveryId)} and will be dispatched once their current drop is finished.`,
            time: "Just now",
            isRead: false,
            parcelId: deliveryId,
            createdAt: Timestamp.now()
          });
        } catch (err) {
          console.error("Failed to notify customer of reserved rider:", err);
        }
      }

      setAssignModal({ delivery: null as any, show: false });
    };

    const reassignRider = async (deliveryId: string, rider: UserProfile) => {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        riderId: rider.id, driverId: rider.id, driverName: rider.name,
        courierName: rider.name, courierPhone: rider.phone, riderBikeNumber: rider.bikeNumber || "",
        updatedAt: Timestamp.now()
      });
      addLog("Reassign Rider", `${rider.name} → ${idShort(deliveryId)}`);

      const targetUserId = reassignModal.delivery?.userId || deliveries.find(d => d.id === deliveryId)?.userId;
      if (targetUserId) {
        try {
          const notifRef = doc(collection(db, "users", targetUserId, "notifications"));
          await setDoc(notifRef, {
            id: notifRef.id,
            title: "Rider Reassigned",
            message: `${rider.name} (${rider.phone || "Active Courier"}) has been reassigned to your shipment #${idShort(deliveryId)}.`,
            time: "Just now",
            isRead: false,
            parcelId: deliveryId,
            createdAt: Timestamp.now()
          });
        } catch (err) {
          console.error("Failed to notify customer of reassigned rider:", err);
        }
      }

      setReassignModal({ delivery: null as any, show: false });
    };

    const bulkUpdate = async () => {
      if (!bulkStatus || selected.size === 0) return;

      const invalidTransitions: string[] = [];
      const missingRiders: string[] = [];

      selected.forEach(id => {
        const del = deliveries.find(d => d.id === id);
        if (del) {
          if (!isValidStatusTransition(del.status, bulkStatus)) {
            invalidTransitions.push(`${idShort(id)} (${del.status} → ${bulkStatus})`);
          }
          if (requiresRider(bulkStatus) && !del.riderId && !del.driverId) {
            missingRiders.push(idShort(id));
          }
        }
      });

      if (invalidTransitions.length > 0) {
        alert(`Invalid transitions for ${invalidTransitions.length} shipment(s):\n${invalidTransitions.slice(0, 5).join("\n")}${invalidTransitions.length > 5 ? "\n..." : ""}`);
        return;
      }

      if (missingRiders.length > 0) {
        alert(`${missingRiders.length} shipment(s) have no rider assigned. Cannot set to ${bulkStatus}.\n${missingRiders.slice(0, 5).join(", ")}${missingRiders.length > 5 ? "..." : ""}`);
        return;
      }

      setConfirmBulkModal({ show: true });
    };

    const confirmBulkUpdate = async () => {
      if (!bulkStatus || selected.size === 0) return;
      const batch = writeBatch(db);
      selected.forEach(id => {
        batch.update(doc(db, "deliveries", id), { status: bulkStatus, updatedAt: Timestamp.now() });
        const del = deliveries.find(d => d.id === id);
        if (del && del.userId) {
          const notifRef = doc(collection(db, "users", del.userId, "notifications"));
          batch.set(notifRef, {
            id: notifRef.id,
            title: bulkStatus === "DELIVERED" ? "Package Delivered" : `Shipment ${bulkStatus.replace(/_/g, " ")}`,
            message: `Shipment #${idShort(id)} status updated to ${bulkStatus.replace(/_/g, " ")}.`,
            time: "Just now",
            isRead: false,
            parcelId: id,
            createdAt: Timestamp.now()
          });
        }
      });
      await batch.commit();
      addLog("Bulk", selected.size + " deliveries -> " + bulkStatus);
      setBulkStatus("");
      setSelected(new Set());
      setConfirmBulkModal({ show: false });
    };

    const bulkAssignRider = async (rider: UserProfile) => {
      if (selected.size === 0) return;
      const batch = writeBatch(db);
      selected.forEach(id => {
        batch.update(doc(db, "deliveries", id), {
          riderId: rider.id,
          driverId: rider.id,
          driverName: rider.name,
          courierName: rider.name,
          courierPhone: rider.phone || "",
          riderBikeNumber: rider.bikeNumber || "",
          status: "ASSIGNED",
          updatedAt: Timestamp.now()
        });
        const del = deliveries.find(d => d.id === id);
        if (del && del.userId) {
          const notifRef = doc(collection(db, "users", del.userId, "notifications"));
          batch.set(notifRef, {
            id: notifRef.id,
            title: "Rider Assigned!",
            message: `${rider.name} (${rider.phone || "Active Courier"}) has been assigned to your shipment #${idShort(id)}.`,
            time: "Just now",
            isRead: false,
            parcelId: id,
            createdAt: Timestamp.now()
          });
        }
      });
      await batch.commit();
      addLog("Bulk Assign", `${rider.name} assigned to ${selected.size} shipments`);
      if (addToast) addToast("success", `Assigned ${selected.size} shipments to ${rider.name}`);
      setSelected(new Set());
      setBulkAssignModal(false);
    };

    const createDelivery = async () => {
      if (!newForm.receiverName.trim() || !newForm.deliveryAddress.trim() || !newForm.senderName.trim() || !newForm.pickupAddress.trim()) {
        alert("Please provide Sender Name, Pickup Address, Receiver Name, and Delivery Address.");
        return;
      }
      setCreating(true);
      try {
        const otp = Math.floor(1000 + Math.random() * 9000).toString();
        const selectedDriver = drivers.find(d => d.id === newForm.riderId);
        const ref = await addDoc(collection(db, "deliveries"), {
          ...newForm,
          id: "",
          quantity: Number(newForm.quantity) || 1,
          weight: Number(newForm.weight) || 1,
          price: Number(newForm.price) || 1500,
          tipAmount: 0,
          userId: "",
          courierName: selectedDriver ? selectedDriver.name : (newForm.driverName || "Unassigned"),
          courierPhone: selectedDriver ? (selectedDriver.phone || "") : "",
          riderBikeNumber: selectedDriver ? (selectedDriver.bikeNumber || "") : "",
          driverId: selectedDriver ? selectedDriver.id : "",
          riderId: selectedDriver ? selectedDriver.id : "",
          status: selectedDriver ? "ASSIGNED" : "PENDING",
          otpCode: otp,
          dateString: new Date().toISOString().slice(0, 10),
          createdAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        });
        await updateDoc(ref, { id: ref.id });
        addLog("Create Delivery", ref.id + " — " + newForm.itemName);
        if (addToast) addToast("success", `Created new shipment #${idShort(ref.id)} (${newForm.itemName || "Parcel"})`);
        setShowNew(false);
        setNewForm({ receiverName: "", receiverPhone: "", deliveryAddress: "", senderName: "", senderPhone: "", itemName: "", pickupAddress: "", quantity: 1, weight: 1, price: 1500, category: "Standard", status: "PENDING", riderId: "", driverId: "", driverName: "" });
      } catch (e: any) { 
        addLog("Error", "Create delivery failed: " + (e?.message || "Unknown error"));
        if (addToast) addToast("error", "Failed to create delivery: " + (e?.message || "Unknown error"));
      }
      setCreating(false);
    };

    // Operational Stat Metrics
    const totalDeliveries = deliveries.length;
    const actionNeededCount = deliveries.filter(d => ["PENDING", "QUEUED", "RESERVED_NEXT"].includes(d.status) || (!d.riderId && !d.driverId && d.status !== "DELIVERED" && d.status !== "CANCELLED")).length;
    const inMotionCount = deliveries.filter(d => ["ASSIGNED", "PICKED_UP", "TRANSIT", "ARRIVED", "OUT_FOR_DELIVERY"].includes(d.status)).length;
    const deliveredCount = deliveries.filter(d => d.status === "DELIVERED").length;
    const totalRevenue = deliveries.reduce((acc, d) => acc + (d.price || 0) + (d.tipAmount || 0), 0);

    // If an individual shipment is selected, render the dedicated in-place Micro-Page (no cramped modals)
    const activeDeliveryForMicroPage = deliveries.find(d => 
      d.id === selectedShipmentId || 
      (d.id && selectedShipmentId && d.id.trim().toLowerCase() === selectedShipmentId.trim().toLowerCase())
    );

    if (selectedShipmentId) {
      if (activeDeliveryForMicroPage) {
        return (
          <ShipmentMicroPage
            delivery={activeDeliveryForMicroPage}
            drivers={drivers}
            deliveries={deliveries}
            onBack={() => {
              setSelectedShipmentId(null);
              setSearch("");
              setStatusFilter("ALL");
            }}
            onAssignRider={assignRider}
            onReserveRider={reserveRider}
            onUpdateStatus={async (delId: string, newStatus: string) => {
              await updateDoc(doc(db, "deliveries", delId), { status: newStatus, updatedAt: Timestamp.now() });
              addLog("Status Update", `${idShort(delId)} → ${newStatus}`);
              if (addToast) addToast("success", `Status updated to ${newStatus.replace(/_/g, " ")}`);
            }}
            onPrintWaybill={(del: any) => setWaybillModal({ delivery: del, show: true })}
            addToast={addToast}
          />
        );
      }

      // If active delivery is not found or still loading from Firestore:
      return (
        <div className="tab-content p-8 sm:p-12 bg-white dark:bg-[#1a1a1a] rounded-3xl border border-gray-200 dark:border-white/10 space-y-4 text-center">
          <div className="w-12 h-12 rounded-2xl bg-amber-500/15 text-amber-800 dark:text-[#FFB800] flex items-center justify-center mx-auto animate-pulse">
            <Package size={24} />
          </div>
          <h3 className="text-base font-black text-[#111] dark:text-white">
            {deliveries.length === 0 ? "Loading Consignment Details..." : "Shipment Not Found"}
          </h3>
          <p className="text-xs text-gray-600 dark:text-gray-400 font-medium max-w-sm mx-auto">
            {deliveries.length === 0
              ? "Syncing live delivery records from Firestore. Please hold on..."
              : `Shipment #${idShort(selectedShipmentId)} could not be located in active records.`}
          </p>
          <button
            onClick={() => {
              setSelectedShipmentId(null);
              setStatusFilter("ALL");
              setSearch("");
            }}
            className="px-4 py-2 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black hover:bg-[#FFB800]/80 transition-all cursor-pointer shadow-xs"
          >
            View All Shipments
          </button>
        </div>
      );
    }

    return (
      <div className="tab-content space-y-6 animate-fade-in">
        {/* Analytics Metric Header Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Total Shipments</span>
              <div className="w-8 h-8 rounded-xl bg-blue-500/15 text-blue-600 dark:text-blue-400 flex items-center justify-center"><Package size={16} /></div>
            </div>
            <p className="text-xl font-black text-gray-900 dark:text-white mt-2">{totalDeliveries}</p>
          </div>

          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs flex flex-col justify-between cursor-pointer hover:border-amber-500/50 transition-colors" onClick={() => setStatusFilter("ACTION_NEEDED")}>
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-extrabold text-amber-700 dark:text-amber-400 uppercase tracking-wider">Action Needed</span>
              <div className="w-8 h-8 rounded-xl bg-amber-500/15 text-amber-700 dark:text-amber-400 flex items-center justify-center"><Clock size={16} /></div>
            </div>
            <p className="text-xl font-black text-gray-900 dark:text-white mt-2">{actionNeededCount}</p>
          </div>

          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs flex flex-col justify-between cursor-pointer hover:border-purple-500/50 transition-colors" onClick={() => setStatusFilter("IN_MOTION")}>
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-extrabold text-purple-700 dark:text-purple-300 uppercase tracking-wider">In Transit</span>
              <div className="w-8 h-8 rounded-xl bg-purple-500/15 text-purple-700 dark:text-purple-400 flex items-center justify-center"><Truck size={16} /></div>
            </div>
            <p className="text-xl font-black text-gray-900 dark:text-white mt-2">{inMotionCount}</p>
          </div>

          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs flex flex-col justify-between cursor-pointer hover:border-emerald-500/50 transition-colors" onClick={() => setStatusFilter("DELIVERED")}>
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-extrabold text-emerald-700 dark:text-emerald-400 uppercase tracking-wider">Delivered</span>
              <div className="w-8 h-8 rounded-xl bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 flex items-center justify-center"><CheckCircle size={16} /></div>
            </div>
            <p className="text-xl font-black text-gray-900 dark:text-white mt-2">{deliveredCount}</p>
          </div>

          <div className="col-span-2 sm:col-span-1 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-extrabold text-amber-900 dark:text-[#FFB800] uppercase tracking-wider">Gross Revenue</span>
              <div className="w-8 h-8 rounded-xl bg-amber-500/15 text-amber-900 dark:text-[#FFB800] flex items-center justify-center"><DollarSign size={16} /></div>
            </div>
            <p className="text-xl font-black text-gray-900 dark:text-white mt-2">{fmt(totalRevenue)}</p>
          </div>
        </div>

        {/* Controls & Search Header */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-4 sm:p-5 shadow-xs space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-lg font-black text-gray-900 dark:text-white flex items-center gap-2">
                <Package className="w-5 h-5 text-amber-800 dark:text-[#FFB800]" /> Dispatch Management Hub
              </h1>
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-0.5">Showing {filtered.length} shipments (page {page + 1}/{totalPages})</p>
            </div>

            <div className="flex items-center gap-2.5 flex-wrap">
              <div className="w-full sm:w-64">
                <SearchInput value={search} onChange={setSearch} placeholder="Search sender, receiver, item, tracking #..." />
              </div>
              <button
                onClick={() => setShowNew(true)}
                className="px-4 py-2.5 min-h-[38px] bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-sm transition-all flex items-center justify-center gap-1.5 shrink-0 cursor-pointer"
              >
                <Plus className="w-4 h-4" /> New Delivery
              </button>
            </div>
          </div>

          {/* Operational Urgency Filter Tabs */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 border-b border-gray-200 dark:border-white/10 mb-3">
            <button
              onClick={() => setStatusFilter("ACTION_NEEDED")}
              className={"px-3.5 py-2 rounded-xl text-xs font-black transition-all whitespace-nowrap flex items-center gap-1.5 cursor-pointer " + (statusFilter === "ACTION_NEEDED" ? "bg-[#FFB800] text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#333] font-bold border border-gray-200 dark:border-white/10")}
            >
              <AlertTriangle size={13} />
              Action Needed
              <span className={"px-1.5 py-0.2 rounded-full text-[10px] font-black " + (statusFilter === "ACTION_NEEDED" ? "bg-black/20 text-[#111]" : "bg-amber-500/20 text-amber-800 dark:text-amber-300")}>
                {actionNeededCount}
              </span>
            </button>
            <button
              onClick={() => setStatusFilter("IN_MOTION")}
              className={"px-3.5 py-2 rounded-xl text-xs font-bold transition-all whitespace-nowrap flex items-center gap-1.5 cursor-pointer " + (statusFilter === "IN_MOTION" ? "bg-gray-900 text-white dark:bg-white dark:text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#333] border border-gray-200 dark:border-white/10")}
            >
              <Truck size={13} />
              In Motion
              <span className="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200">
                {inMotionCount}
              </span>
            </button>
            <button
              onClick={() => setStatusFilter("DELIVERED")}
              className={"px-3.5 py-2 rounded-xl text-xs font-bold transition-all whitespace-nowrap flex items-center gap-1.5 cursor-pointer " + (statusFilter === "DELIVERED" ? "bg-emerald-600 text-white shadow-xs font-black" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#333] border border-gray-200 dark:border-white/10")}
            >
              <CheckCircle size={13} />
              Delivered
              <span className="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200">
                {deliveredCount}
              </span>
            </button>
            <button
              onClick={() => setStatusFilter("ALL")}
              className={"px-3.5 py-2 rounded-xl text-xs font-bold transition-all whitespace-nowrap flex items-center gap-1.5 cursor-pointer " + (statusFilter === "ALL" ? "bg-gray-800 text-white dark:bg-gray-200 dark:text-gray-900 shadow-xs font-black" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#333] border border-gray-200 dark:border-white/10")}
            >
              All Shipments
              <span className="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-gray-200 dark:bg-white/15 text-gray-800 dark:text-gray-200">
                {totalDeliveries}
              </span>
            </button>
          </div>

          {/* Category Filter Bar */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1">
            {categories.map(c => (
              <button
                key={c}
                onClick={() => setCategoryFilter(c)}
                className={"px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer " + (categoryFilter === c ? "bg-[#FFB800] text-[#111] shadow-xs font-black" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#333] border border-gray-200 dark:border-white/10")}
              >
                {c === "ALL" ? "All Categories" : c}
              </button>
            ))}
          </div>

          {/* Sticky Bulk Action Bar */}
          {selected.size > 0 && (
            <div className="bg-amber-50 dark:bg-amber-950/30 border border-amber-300 dark:border-amber-700/40 rounded-2xl p-3 flex items-center justify-between gap-3 animate-fade-in">
              <span className="text-xs font-bold text-gray-900 dark:text-white flex items-center gap-1.5">
                <CheckCircle className="w-4 h-4 text-amber-800 dark:text-[#FFB800]" /> {selected.size} shipments selected
              </span>
              <div className="flex items-center gap-2">
                <Select
                  value={bulkStatus}
                  onChange={setBulkStatus}
                  placeholder="Bulk Status..."
                  options={[
                    { value: "ASSIGNED", label: "ASSIGNED" },
                    { value: "TRANSIT", label: "TRANSIT" },
                    { value: "OUT_FOR_DELIVERY", label: "OUT FOR DELIVERY" },
                    { value: "DELIVERED", label: "DELIVERED" },
                    { value: "CANCELLED", label: "CANCELLED" }
                  ]}
                  compact
                  className="w-36"
                />
                <SaveBtn onClick={bulkUpdate} label="Apply" />
                <button
                  onClick={() => setBulkAssignModal(true)}
                  className="px-3 py-1.5 min-h-[34px] bg-gray-900 dark:bg-white text-white dark:text-[#111] rounded-xl text-xs font-black hover:opacity-90 transition-all flex items-center gap-1.5 cursor-pointer shadow-xs"
                >
                  <UserPlus size={13} /> Assign Rider
                </button>
                <button onClick={() => setSelected(new Set())} className="text-xs font-bold text-gray-600 dark:text-gray-400 hover:text-red-500 px-2 py-1 cursor-pointer">Cancel</button>
              </div>
            </div>
          )}
        </div>

        {/* High-Fidelity Shipments Table */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-100 dark:bg-[#222] border-b border-gray-200 dark:border-white/10">
                <tr>
                  <th className="p-3.5 text-left w-10">
                    <input
                      type="checkbox"
                      checked={selected.size > 0 && selected.size === paged.length}
                      onChange={e => setSelected(e.target.checked ? new Set(paged.map(d => d.id)) : new Set())}
                      className="rounded border-gray-300 text-[#FFB800] focus:ring-[#FFB800]"
                    />
                  </th>
                  <th className="p-3.5 text-left font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px]">Item & Order ID</th>
                  <th className="p-3.5 text-left font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] hidden md:table-cell">Sender & Recipient</th>
                  <th className="p-3.5 text-left font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] hidden lg:table-cell">Assigned Rider</th>
                  <th className="p-3.5 text-left font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px]">Status</th>
                  <th className="p-3.5 text-right font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px]">Price</th>
                  <th className="p-3.5 text-right font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px]">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-white/10">
                {paged.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="p-12 text-center text-gray-500 dark:text-gray-400 font-medium">
                      <Package className="w-8 h-8 text-gray-400 dark:text-gray-600 mx-auto mb-2" />
                      No shipments found matching filters.
                    </td>
                  </tr>
                ) : (
                  paged.map((d, i) => (
                    <tr key={d.id} className={"hover:bg-gray-50 dark:hover:bg-white/5 transition-colors " + (selected.has(d.id) ? "bg-amber-500/10" : "")}>
                      <td className="p-3.5">
                        <input
                          type="checkbox"
                          checked={selected.has(d.id)}
                          onChange={() => {
                            const s = new Set(selected);
                            s.has(d.id) ? s.delete(d.id) : s.add(d.id);
                            setSelected(s);
                          }}
                          className="rounded border-gray-300 text-[#FFB800] focus:ring-[#FFB800]"
                        />
                      </td>
                      <td className="p-3.5 cursor-pointer" onClick={() => setSelectedShipmentId(d.id)}>
                        <p className="font-bold text-gray-900 dark:text-white flex items-center gap-1.5">
                          {d.itemName || "Parcel"}
                          {d.category && <span className="px-2 py-0.5 rounded-full text-[9px] font-extrabold bg-gray-100 dark:bg-[#333] text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10">{d.category}</span>}
                        </p>
                        <p className="text-[10px] font-mono text-gray-600 dark:text-gray-400 font-semibold mt-0.5 flex items-center gap-1">
                          #{idShort(d.id)}
                        </p>
                      </td>
                      <td className="p-3.5 hidden md:table-cell cursor-pointer" onClick={() => setSelectedShipmentId(d.id)}>
                        <div className="space-y-0.5">
                          <p className="font-bold text-xs text-gray-900 dark:text-white flex items-center gap-1 truncate">
                            <span className="text-[9px] font-black uppercase tracking-wider px-1.5 py-0.2 rounded bg-amber-500/15 text-amber-800 dark:text-amber-300">From:</span>
                            {d.senderName || "Sender"}
                          </p>
                          <p className="font-bold text-xs text-gray-900 dark:text-white flex items-center gap-1 truncate">
                            <span className="text-[9px] font-black uppercase tracking-wider px-1.5 py-0.2 rounded bg-emerald-500/15 text-emerald-800 dark:text-emerald-300">To:</span>
                            {d.receiverName || "Receiver"}
                          </p>
                          <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium truncate max-w-xs">{d.deliveryAddress || "Benin City"}</p>
                        </div>
                      </td>
                      <td className="p-3.5 hidden lg:table-cell">
                        {(!d.riderId && !d.driverId) ? (
                          <button
                            onClick={() => setSelectedShipmentId(d.id)}
                            className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-lg text-[10px] font-black transition-all flex items-center gap-1 shadow-xs cursor-pointer"
                          >
                            <UserPlus size={12} /> Assign Rider
                          </button>
                        ) : (
                          <div className="flex items-center gap-2">
                            <div className="w-7 h-7 rounded-full bg-amber-100 dark:bg-amber-950/40 border border-amber-300 dark:border-amber-700/40 flex items-center justify-center text-[10px] font-black text-amber-900 dark:text-[#FFB800] shrink-0">
                              {(d.courierName || d.driverName || "?").charAt(0)}
                            </div>
                            <div>
                              <p className="text-[11px] font-bold text-gray-900 dark:text-white truncate max-w-[110px]">{d.courierName || d.driverName || "Assigned"}</p>
                              {d.courierPhone && <p className="text-[9px] text-gray-500 dark:text-gray-400 font-mono">{d.courierPhone}</p>}
                              {d.status !== "DELIVERED" && d.status !== "CANCELLED" && (
                                <button
                                  onClick={() => setSelectedShipmentId(d.id)}
                                  className="text-[9px] font-bold text-amber-800 dark:text-[#FFB800] hover:underline cursor-pointer"
                                >
                                  Manage / Reassign
                                </button>
                              )}
                            </div>
                          </div>
                        )}
                      </td>
                      <td className="p-3.5">
                        <div className="flex flex-col gap-1.5 items-start">
                          <StatusBadge status={d.status} size="default" useAdminLabel={true} />
                          {(() => {
                            const normCurrent = (d.status || "PENDING").toUpperCase();
                            const currentIdx = getLifecycleStageIndex(normCurrent);
                            const isDelivered = normCurrent === "DELIVERED";
                            const isCancelled = normCurrent === "CANCELLED";

                            const baseStages = [
                              { value: "PENDING", label: "PENDING", stage: 0 },
                              { value: "ASSIGNED", label: "ASSIGNED", stage: 1 },
                              { value: "TRANSIT", label: "TRANSIT", stage: 2 },
                              { value: "OUT_FOR_DELIVERY", label: "OUT FOR DELIVERY", stage: 3 },
                              { value: "DELIVERED", label: "DELIVERED", stage: 4 },
                            ];

                            const list = [...baseStages];
                            if (!list.some(s => s.value === normCurrent) && !isCancelled) {
                              list.push({ value: normCurrent, label: normCurrent.replace(/_/g, " "), stage: currentIdx });
                              list.sort((a, b) => a.stage - b.stage);
                            }

                            const tableOptions = [
                              ...list.map(step => {
                                const isPassed = !isCancelled && step.stage < currentIdx;
                                const isThisCurrent = !isCancelled && (step.value === normCurrent || (step.stage === currentIdx && isDelivered));
                                return {
                                  value: step.value,
                                  label: isPassed ? `✓ ${step.label} (Passed)` : isThisCurrent ? (isDelivered ? `✓ ${step.label} (Completed)` : `● ${step.label} (Current)`) : step.label,
                                  disabled: isPassed || (isDelivered && step.value !== "DELIVERED"),
                                };
                              }),
                              {
                                value: "CANCELLED",
                                label: isCancelled ? "● CANCELLED (Current)" : "CANCELLED",
                                disabled: isDelivered,
                              }
                            ];

                            return (
                              <Select
                                value={d.status}
                                onChange={v => updateStatus(d.id, v)}
                                compact
                                options={tableOptions}
                                renderOption={(o) => (
                                  <span className={`px-2.5 py-1 rounded-xl text-[10px] font-extrabold shadow-2xs ${o.disabled ? "opacity-60 text-gray-500 dark:text-gray-400" : sStyle(o.value)}`}>
                                    {o.label}
                                  </span>
                                )}
                              />
                            );
                          })()}
                        </div>
                      </td>
                      <td className="p-3.5 text-right">
                        <PriceDisplay amount={d.price || 0} variant="admin" />
                        {d.tipAmount > 0 && <p className="text-[10px] font-bold text-emerald-700 dark:text-emerald-400 font-mono mt-0.5">+{fmt(d.tipAmount)} tip</p>}
                      </td>
                      <td className="p-3.5 text-right whitespace-nowrap">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => setSelectedShipmentId(d.id)}
                            className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black text-xs rounded-xl shadow-xs flex items-center gap-1 cursor-pointer transition-all"
                            title="Open Dedicated Shipment Workspace"
                          >
                            Manage →
                          </button>
                          <button
                            onClick={() => setWaybillModal({ delivery: d, show: true })}
                            className="p-1.5 rounded-lg text-gray-500 dark:text-gray-400 hover:text-amber-800 dark:hover:text-[#FFB800] hover:bg-gray-100 dark:hover:bg-white/10 transition-all cursor-pointer"
                            title="Print Waybill / Thermal Receipt"
                          >
                            <Printer size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between pt-2">
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium">Showing page {page + 1} of {totalPages}</p>
            <div className="flex items-center gap-1.5">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-1.5 rounded-xl bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-900 dark:text-white disabled:opacity-30 hover:bg-gray-100 dark:hover:bg-[#222] cursor-pointer">Previous</button>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="px-3 py-1.5 rounded-xl bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-900 dark:text-white disabled:opacity-30 hover:bg-gray-100 dark:hover:bg-[#222] cursor-pointer">Next</button>
            </div>
          </div>
        )}

        {/* Shipment Details Modal */}
        {detailsModal.show && detailsModal.delivery && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setDetailsModal({ delivery: null as any, show: false })}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                  <Package className="w-5 h-5 text-[#FFB800]" /> Shipment Details
                </h3>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      const d = detailsModal.delivery;
                      setDetailsModal({ delivery: null as any, show: false });
                      setWaybillModal({ delivery: d, show: true });
                    }}
                    className="px-3 py-1 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] font-black text-xs rounded-xl flex items-center gap-1 transition-all cursor-pointer"
                  >
                    <Printer size={12} /> Waybill
                  </button>
                  <button onClick={() => setDetailsModal({ delivery: null as any, show: false })} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 ml-1 cursor-pointer">Close</button>
                </div>
              </div>

              <div className="space-y-3 text-xs">
                <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5">
                  <div>
                    <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Tracking ID</p>
                    <p className="font-mono font-bold text-[#111] dark:text-white">{detailsModal.delivery.id}</p>
                  </div>
                  <span className={"px-3 py-1 rounded-xl text-xs font-bold " + sStyle(detailsModal.delivery.status)}>{detailsModal.delivery.status}</span>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5">
                    <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Sender</p>
                    <p className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.senderName || "Sender"}</p>
                    <p className="text-gray-700 dark:text-gray-300 font-medium">{detailsModal.delivery.senderPhone || "—"}</p>
                    <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium mt-1">{detailsModal.delivery.pickupAddress}</p>
                  </div>

                  <div className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5">
                    <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Receiver</p>
                    <p className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.receiverName || "Receiver"}</p>
                    <p className="text-gray-700 dark:text-gray-300 font-medium">{detailsModal.delivery.receiverPhone || "—"}</p>
                    <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium mt-1">{detailsModal.delivery.deliveryAddress}</p>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5">
                  <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Parcel Details</p>
                  <div className="grid grid-cols-2 gap-2 text-[11px]">
                    <div><span className="text-gray-600 dark:text-gray-400 font-medium">Item:</span> <span className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.itemName || "—"}</span></div>
                    <div><span className="text-gray-600 dark:text-gray-400 font-medium">Category:</span> <span className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.category || "Standard"}</span></div>
                    <div><span className="text-gray-600 dark:text-gray-400 font-medium">Weight:</span> <span className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.weight || 1} kg</span></div>
                    <div><span className="text-gray-600 dark:text-gray-400 font-medium">Quantity:</span> <span className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.quantity || 1}</span></div>
                    <div><span className="text-gray-600 dark:text-gray-400 font-medium">Price:</span> <span className="font-bold text-[#111] dark:text-white">{fmt(detailsModal.delivery.price || 0)}</span></div>
                    {detailsModal.delivery.tipAmount > 0 && <div><span className="text-gray-600 dark:text-gray-400 font-medium">Tip:</span> <span className="font-bold text-emerald-600 dark:text-emerald-400">{fmt(detailsModal.delivery.tipAmount)}</span></div>}
                  </div>
                </div>

                <div className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5">
                  <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">OTP Code</p>
                  <p className="font-mono font-black text-lg text-[#111] dark:text-white tracking-widest">{detailsModal.delivery.otpCode || "—"}</p>
                </div>

                <div className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl border border-gray-100 dark:border-white/5 flex items-center justify-between">
                  <div>
                    <p className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Assigned Rider</p>
                    <p className="font-bold text-[#111] dark:text-white">{detailsModal.delivery.courierName || detailsModal.delivery.driverName || "Unassigned"}</p>
                    {detailsModal.delivery.courierPhone && <p className="text-gray-700 dark:text-gray-300 font-medium">{detailsModal.delivery.courierPhone}</p>}
                    {detailsModal.delivery.riderBikeNumber && <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">Bike: {detailsModal.delivery.riderBikeNumber}</p>}
                  </div>
                  {detailsModal.delivery.status === "PENDING" ? (
                    <button
                      onClick={() => { setDetailsModal({ delivery: null as any, show: false }); setAssignModal({ delivery: detailsModal.delivery, show: true }); }}
                      className="px-3 py-1.5 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black cursor-pointer hover:bg-[#FFB800]/90"
                    >
                      Assign
                    </button>
                  ) : (detailsModal.delivery.status !== "DELIVERED" && detailsModal.delivery.status !== "CANCELLED") ? (
                    <button
                      onClick={() => { setDetailsModal({ delivery: null as any, show: false }); setReassignModal({ delivery: detailsModal.delivery, show: true }); }}
                      className="px-3 py-1.5 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black cursor-pointer hover:bg-[#FFB800]/90"
                    >
                      Reassign
                    </button>
                  ) : null}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Printable Waybill & Thermal Receipt Modal */}
        {waybillModal.show && waybillModal.delivery && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50 overflow-y-auto" onClick={() => setWaybillModal({ delivery: null as any, show: false })}>
            <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-4 my-8" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <div className="flex items-center gap-2">
                  <Printer className="w-5 h-5 text-[#FFB800]" />
                  <h3 className="text-base font-black text-[#111] dark:text-white">Waybill & Thermal Receipt</h3>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => window.print()}
                    className="px-3.5 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] font-black text-xs rounded-xl flex items-center gap-1.5 transition-all shadow-xs cursor-pointer"
                  >
                    <Printer size={13} /> Print
                  </button>
                  <button onClick={() => setWaybillModal({ delivery: null as any, show: false })} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 ml-2 cursor-pointer">Close</button>
                </div>
              </div>

              {/* Waybill Document Container */}
              <div className="bg-white text-black p-6 rounded-2xl border-2 border-black/80 font-sans shadow-inner text-xs space-y-4">
                {/* Header */}
                <div className="text-center border-b-2 border-black pb-3">
                  <h2 className="text-xl font-black tracking-widest text-[#111]">ESDISPATCH</h2>
                  <p className="text-[10px] font-black uppercase tracking-widest text-black/60">PREMIUM LOGISTICS & DISPATCH</p>
                  <div className="mt-2 inline-block px-3 py-1 bg-black text-white font-mono text-[11px] font-bold rounded">
                    WAYBILL #{waybillModal.delivery.id}
                  </div>
                </div>

                {/* Routing / Addresses */}
                <div className="grid grid-cols-2 gap-3 border-b border-black/20 pb-3">
                  <div className="bg-gray-50 p-2.5 rounded-lg border border-gray-200">
                    <p className="text-[9px] font-black uppercase text-gray-500">Shipper / Sender</p>
                    <p className="font-bold text-sm text-[#111]">{waybillModal.delivery.senderName || "Sender"}</p>
                    <p className="text-[11px] font-semibold text-gray-700">{waybillModal.delivery.senderPhone || "—"}</p>
                    <p className="text-[10px] text-gray-600 mt-1 leading-tight">{waybillModal.delivery.pickupAddress}</p>
                  </div>
                  <div className="bg-gray-50 p-2.5 rounded-lg border border-gray-200">
                    <p className="text-[9px] font-black uppercase text-gray-500">Consignee / Receiver</p>
                    <p className="font-bold text-sm text-[#111]">{waybillModal.delivery.receiverName || "Receiver"}</p>
                    <p className="text-[11px] font-semibold text-gray-700">{waybillModal.delivery.receiverPhone || "—"}</p>
                    <p className="text-[10px] text-gray-600 mt-1 leading-tight">{waybillModal.delivery.deliveryAddress}</p>
                  </div>
                </div>

                {/* Parcel Particulars */}
                <div className="space-y-1.5 border-b border-black/20 pb-3 text-[11px]">
                  <div className="flex justify-between">
                    <span className="text-gray-600 font-bold">Consignment Item:</span>
                    <span className="font-bold text-black">{waybillModal.delivery.itemName || "Standard Parcel"}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 font-bold">Weight & Quantity:</span>
                    <span>{waybillModal.delivery.weight || 1} kg &bull; {waybillModal.delivery.quantity || 1} unit(s)</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 font-bold">Category & Tier:</span>
                    <span className="uppercase font-semibold">{waybillModal.delivery.category || "Standard"}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 font-bold">Assigned Courier:</span>
                    <span className="font-bold">{waybillModal.delivery.courierName || waybillModal.delivery.driverName || "Assigned Unit"} ({waybillModal.delivery.riderBikeNumber || "Fleet 01"})</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 font-bold">Delivery Handover OTP:</span>
                    <span className="font-mono font-black text-black tracking-widest bg-gray-100 px-2 py-0.5 rounded">{waybillModal.delivery.otpCode || "----"}</span>
                  </div>
                </div>

                {/* Payment Breakdown */}
                <div className="bg-gray-100 p-3 rounded-xl space-y-1 text-xs">
                  <div className="flex justify-between text-gray-600">
                    <span>Base Delivery Charge:</span>
                    <span>{fmt(waybillModal.delivery.price || 0)}</span>
                  </div>
                  {waybillModal.delivery.tipAmount > 0 && (
                    <div className="flex justify-between text-gray-600">
                      <span>Driver Tip:</span>
                      <span>{fmt(waybillModal.delivery.tipAmount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between font-black text-sm pt-1 border-t border-gray-300 text-black">
                    <span>Total Paid:</span>
                    <span>{fmt((waybillModal.delivery.price || 0) + (waybillModal.delivery.tipAmount || 0))}</span>
                  </div>
                </div>

                {/* Sign-off Box */}
                <div className="pt-2 grid grid-cols-2 gap-4 text-[10px] text-gray-500">
                  <div className="border-t border-dashed border-gray-400 pt-1 text-center">Courier Signature / Stamp</div>
                  <div className="border-t border-dashed border-gray-400 pt-1 text-center">Consignee Handover Signature</div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* New Delivery Creation Modal */}
        {showNew && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setShowNew(false)}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-xl shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                  <Plus className="w-5 h-5 text-[#FFB800]" /> Create New Shipment
                </h3>
                <button onClick={() => setShowNew(false)} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 p-1 rounded-lg cursor-pointer">
                  <X size={18} />
                </button>
              </div>

              <div className="space-y-4 text-xs">
                {/* Consignment Item & Category */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Item Description / Name *</label>
                    <input
                      type="text"
                      placeholder="e.g. Legal Documents, Cake, Spare Part"
                      value={newForm.itemName}
                      onChange={e => setNewForm({ ...newForm, itemName: e.target.value })}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-[#FFB800]/40 outline-none"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Service Category *</label>
                    <Select
                      value={newForm.category}
                      onChange={v => setNewForm({ ...newForm, category: v })}
                      options={[
                        { value: "Express", label: "Express Dispatch" },
                        { value: "Standard", label: "Standard Delivery" },
                        { value: "Economy", label: "Economy Parcel" },
                        { value: "Batch", label: "Batch Logistics" },
                        { value: "Multi-Stop", label: "Multi-Stop Routing" },
                        { value: "Cold Chain", label: "Cold Chain Secure" }
                      ]}
                      className="w-full"
                    />
                  </div>
                </div>

                {/* Shipper / Sender Details */}
                <div className="bg-gray-50 dark:bg-[#222] p-3.5 rounded-2xl border border-gray-100 dark:border-white/5 space-y-2.5">
                  <p className="text-[11px] font-black text-[#111] dark:text-white uppercase tracking-wider flex items-center gap-1.5">
                    <MapPin size={13} className="text-[#FFB800]" /> Sender Information (Pickup)
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Sender Name *</label>
                      <input
                        type="text"
                        placeholder="Sender Full Name"
                        value={newForm.senderName}
                        onChange={e => setNewForm({ ...newForm, senderName: e.target.value })}
                        className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Sender Phone *</label>
                      <input
                        type="tel"
                        placeholder="080XXXXXXXX"
                        value={newForm.senderPhone}
                        onChange={e => setNewForm({ ...newForm, senderPhone: e.target.value })}
                        className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Pickup Address in Benin City *</label>
                    <input
                      type="text"
                      placeholder="e.g. 15 Airport Road, GRA, Benin City"
                      value={newForm.pickupAddress}
                      onChange={e => setNewForm({ ...newForm, pickupAddress: e.target.value })}
                      className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                    />
                  </div>
                </div>

                {/* Consignee / Receiver Details */}
                <div className="bg-gray-50 dark:bg-[#222] p-3.5 rounded-2xl border border-gray-100 dark:border-white/5 space-y-2.5">
                  <p className="text-[11px] font-black text-[#111] dark:text-white uppercase tracking-wider flex items-center gap-1.5">
                    <MapPin size={13} className="text-emerald-500" /> Receiver Information (Destination)
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Receiver Name *</label>
                      <input
                        type="text"
                        placeholder="Receiver Full Name"
                        value={newForm.receiverName}
                        onChange={e => setNewForm({ ...newForm, receiverName: e.target.value })}
                        className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Receiver Phone *</label>
                      <input
                        type="tel"
                        placeholder="080XXXXXXXX"
                        value={newForm.receiverPhone}
                        onChange={e => setNewForm({ ...newForm, receiverPhone: e.target.value })}
                        className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Delivery Address in Benin City *</label>
                    <input
                      type="text"
                      placeholder="e.g. 84 Uselu Lagos Road, Benin City"
                      value={newForm.deliveryAddress}
                      onChange={e => setNewForm({ ...newForm, deliveryAddress: e.target.value })}
                      className="w-full bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none"
                    />
                  </div>
                </div>

                {/* Specs: Weight, Quantity, Price, Driver */}
                <div className="grid grid-cols-3 gap-2.5">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Weight (kg)</label>
                    <input
                      type="number"
                      min="0.1"
                      step="0.1"
                      value={newForm.weight}
                      onChange={e => setNewForm({ ...newForm, weight: Number(e.target.value) })}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Quantity</label>
                    <input
                      type="number"
                      min="1"
                      value={newForm.quantity}
                      onChange={e => setNewForm({ ...newForm, quantity: Number(e.target.value) })}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Fare Price (₦) *</label>
                    <input
                      type="number"
                      min="500"
                      step="100"
                      value={newForm.price}
                      onChange={e => setNewForm({ ...newForm, price: Number(e.target.value) })}
                      className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white outline-none font-bold"
                    />
                  </div>
                </div>

                {/* Optional Immediate Rider Assignment */}
                <div>
                  <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Assign Fleet Rider (Optional)</label>
                  <Select
                    value={newForm.riderId}
                    onChange={v => {
                      const d = drivers.find(drv => drv.id === v);
                      setNewForm({ ...newForm, riderId: v, driverId: v, driverName: d?.name || "" });
                    }}
                    options={[
                      { value: "", label: "Leave Unassigned (Pending Broadcast)" },
                      ...drivers.map(d => ({
                        value: d.id,
                        label: `${d.name} (${d.phone || "Active"} · ${getRiderActiveLoad(d.id)} active drops)`
                      }))
                    ]}
                    className="w-full"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-black/10 dark:border-white/10">
                <button
                  onClick={() => setShowNew(false)}
                  className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={createDelivery}
                  disabled={creating}
                  className="px-5 py-2.5 min-h-[38px] bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-all shadow-md flex items-center gap-1.5 disabled:opacity-50 cursor-pointer"
                >
                  {creating ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                  Create & Dispatch Booking
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Assign Rider Modal */}
        {assignModal.show && assignModal.delivery && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => { setAssignModal({ delivery: null as any, show: false }); setRiderSearch(""); }}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><UserPlus className="w-5 h-5 text-[#FFB800]" /> Assign Rider</h3>
                <button onClick={() => { setAssignModal({ delivery: null as any, show: false }); setRiderSearch(""); }} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 cursor-pointer p-1 rounded-lg"><X size={18} /></button>
              </div>

              <div className="bg-[#FFB800]/10 rounded-2xl p-3.5 border border-[#FFB800]/30 space-y-1">
                <p className="text-xs font-bold text-[#111] dark:text-white">{assignModal.delivery.itemName || "Parcel"} <span className="text-[10px] text-gray-600 dark:text-gray-400 font-mono font-bold">#{idShort(assignModal.delivery.id)}</span></p>
                <p className="text-[11px] text-gray-700 dark:text-gray-300 font-medium">{assignModal.delivery.pickupAddress} → {assignModal.delivery.deliveryAddress}</p>
                <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">Recipient: <b className="text-[#111] dark:text-white font-bold">{assignModal.delivery.receiverName}</b> ({assignModal.delivery.receiverPhone || "No phone"})</p>
              </div>

              {/* Rider Search & Online Filter */}
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search size={14} className="absolute left-3 top-2.5 text-gray-500 dark:text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-9 pr-3 py-2 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setRiderOnlineOnly(!riderOnlineOnly)}
                  className={"px-3 py-2 rounded-xl text-[11px] font-bold border transition-colors shrink-0 cursor-pointer " + (riderOnlineOnly ? "bg-emerald-600 text-white border-emerald-600" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#2a2a2a]")}
                >
                  Online Only
                </button>
              </div>

              <div className="max-h-72 overflow-y-auto space-y-2 pr-1">
                {filteredDrivers.length === 0 ? (
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-medium text-center py-6">No matching riders found.</p>
                ) : (
                  [...filteredDrivers]
                    .map(r => {
                      const load = getRiderActiveLoad(r.id);
                      const rec = scoreRiderForDelivery(r, assignModal.delivery, load);
                      return { r, load, rec };
                    })
                    .sort((a, b) => b.rec.score - a.rec.score)
                    .map(({ r, load, rec }) => {
                      const loadClass = load === 0 ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/30" : load <= 2 ? "bg-amber-500/10 text-amber-800 dark:text-amber-400 border-amber-500/30" : "bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-500/30";
                      const loadText = load === 0 ? "0 active (Free)" : load <= 2 ? `${load} active` : `${load} active (Busy)`;
                      return (
                        <div key={r.id} className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl flex items-center justify-between border border-gray-200 dark:border-white/10 hover:border-[#FFB800]/50 transition-all gap-2">
                          <div className="flex items-center gap-2.5 min-w-0">
                            <div className="w-9 h-9 rounded-full bg-[#FFB800] text-[#111] font-black flex items-center justify-center text-xs shrink-0">
                              {(r.name || "?").charAt(0)}
                            </div>
                            <div className="min-w-0">
                              <div className="flex items-center gap-1.5 flex-wrap">
                                <p className="text-xs font-bold text-[#111] dark:text-white truncate">{r.name}</p>
                                <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline !== false ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline !== false ? "Online" : "Offline"} />
                                {rec.badge === "BEST_FIT" && (
                                  <span className="text-[9px] font-black px-1.5 py-0.5 rounded-md bg-[#FFB800] text-[#111]">★ BEST FIT ({rec.score})</span>
                                )}
                                {rec.badge === "RESERVE_NEXT" && (
                                  <span className="text-[9px] font-black px-1.5 py-0.5 rounded-md bg-purple-600 text-white">⚡ RESERVE NEXT ({rec.score})</span>
                                )}
                              </div>
                              <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium truncate">
                                {r.phone || r.email} {r.bikeNumber ? `• ${r.bikeNumber}` : ""}
                              </p>
                              <div className="flex items-center gap-1.5 mt-1 flex-wrap">
                                <span className={"inline-block text-[9px] font-black px-2 py-0.5 rounded-full border " + loadClass}>
                                  {loadText}
                                </span>
                                <span className="text-[9px] font-medium text-gray-700 dark:text-gray-300 bg-gray-200/60 dark:bg-white/10 px-1.5 py-0.5 rounded">
                                  {rec.explanation}
                                </span>
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center gap-1.5 shrink-0">
                            {load > 0 && (
                              <button
                                onClick={() => { reserveRider(assignModal.delivery.id, r); setRiderSearch(""); }}
                                className="px-2.5 py-1.5 bg-purple-600 hover:bg-purple-700 text-white rounded-xl text-[11px] font-bold transition-colors shadow-xs cursor-pointer"
                                title="Reserve this rider to automatically dispatch once their current drop is completed"
                              >
                                Reserve Next
                              </button>
                            )}
                            <button
                              onClick={() => { assignRider(assignModal.delivery.id, r); setRiderSearch(""); }}
                              className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors shadow-xs cursor-pointer"
                            >
                              {load === 0 ? "Assign Now" : "Assign"}
                            </button>
                          </div>
                        </div>
                      );
                    })
                )}
              </div>
            </div>
          </div>
        )}

        {/* Reassign Rider Modal */}
        {reassignModal.show && reassignModal.delivery && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => { setReassignModal({ delivery: null as any, show: false }); setRiderSearch(""); }}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><UserPlus className="w-4 h-4 text-[#FFB800]" /> Reassign Rider</h3>
                <button onClick={() => { setReassignModal({ delivery: null as any, show: false }); setRiderSearch(""); }} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 cursor-pointer p-1 rounded-lg"><X size={18} /></button>
              </div>

              <div className="bg-[#FFB800]/10 rounded-2xl p-3.5 border border-[#FFB800]/30 space-y-1">
                <p className="text-xs font-bold text-[#111] dark:text-white">{reassignModal.delivery.itemName || "Parcel"} <span className="text-[10px] text-gray-600 dark:text-gray-400 font-mono font-bold">#{idShort(reassignModal.delivery.id)}</span></p>
                <p className="text-[10px] text-gray-600 dark:text-gray-400">Currently: <b className="text-[#111] dark:text-white font-bold">{reassignModal.delivery.courierName || "Unassigned"}</b></p>
                <p className="text-[11px] text-gray-700 dark:text-gray-300 font-medium">{reassignModal.delivery.pickupAddress} → {reassignModal.delivery.deliveryAddress}</p>
              </div>

              {/* Rider Search & Online Filter */}
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search size={14} className="absolute left-3 top-2.5 text-gray-500 dark:text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-9 pr-3 py-2 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setRiderOnlineOnly(!riderOnlineOnly)}
                  className={"px-3 py-2 rounded-xl text-[11px] font-bold border transition-colors shrink-0 cursor-pointer " + (riderOnlineOnly ? "bg-emerald-600 text-white border-emerald-600" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#2a2a2a]")}
                >
                  Online Only
                </button>
              </div>

              <div className="max-h-72 overflow-y-auto space-y-2 pr-1">
                {filteredDrivers.length === 0 ? (
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-medium text-center py-6">No matching riders found.</p>
                ) : (
                  [...filteredDrivers]
                    .map(r => {
                      const load = getRiderActiveLoad(r.id);
                      const rec = scoreRiderForDelivery(r, reassignModal.delivery, load);
                      return { r, load, rec };
                    })
                    .sort((a, b) => b.rec.score - a.rec.score)
                    .map(({ r, load, rec }) => {
                      const loadClass = load === 0 ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/30" : load <= 2 ? "bg-amber-500/10 text-amber-800 dark:text-amber-400 border-amber-500/30" : "bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-500/30";
                      const loadText = load === 0 ? "0 active (Free)" : load <= 2 ? `${load} active` : `${load} active (Busy)`;
                      return (
                        <div key={r.id} className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl flex items-center justify-between border border-gray-200 dark:border-white/10 hover:border-[#FFB800]/50 transition-all gap-2">
                          <div className="flex items-center gap-2.5 min-w-0">
                            <div className="w-9 h-9 rounded-full bg-[#FFB800] text-[#111] font-black flex items-center justify-center text-xs shrink-0">
                              {(r.name || "?").charAt(0)}
                            </div>
                            <div className="min-w-0">
                              <div className="flex items-center gap-1.5 flex-wrap">
                                <p className="text-xs font-bold text-[#111] dark:text-white truncate">{r.name}</p>
                                <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline !== false ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline !== false ? "Online" : "Offline"} />
                                {rec.badge === "BEST_FIT" && (
                                  <span className="text-[9px] font-black px-1.5 py-0.5 rounded-md bg-[#FFB800] text-[#111]">★ BEST FIT ({rec.score})</span>
                                )}
                                {rec.badge === "RESERVE_NEXT" && (
                                  <span className="text-[9px] font-black px-1.5 py-0.5 rounded-md bg-purple-600 text-white">⚡ RESERVE NEXT ({rec.score})</span>
                                )}
                              </div>
                              <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium truncate">
                                {r.phone || r.email} {r.bikeNumber ? `• ${r.bikeNumber}` : ""}
                              </p>
                              <div className="flex items-center gap-1.5 mt-1 flex-wrap">
                                <span className={"inline-block text-[9px] font-black px-2 py-0.5 rounded-full border " + loadClass}>
                                  {loadText}
                                </span>
                                <span className="text-[9px] font-medium text-gray-700 dark:text-gray-300 bg-gray-200/60 dark:bg-white/10 px-1.5 py-0.5 rounded">
                                  {rec.explanation}
                                </span>
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center gap-1.5 shrink-0">
                            {load > 0 && (
                              <button
                                onClick={() => { reserveRider(reassignModal.delivery.id, r); setRiderSearch(""); }}
                                className="px-2.5 py-1.5 bg-purple-600 hover:bg-purple-700 text-white rounded-xl text-[11px] font-bold transition-colors shadow-xs cursor-pointer"
                                title="Reserve this rider as next in sequence"
                              >
                                Reserve Next
                              </button>
                            )}
                            <button
                              onClick={() => { reassignRider(reassignModal.delivery.id, r); setRiderSearch(""); }}
                              className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors shrink-0 shadow-xs cursor-pointer"
                            >
                              Reassign
                            </button>
                          </div>
                        </div>
                      );
                    })
                )}
              </div>
            </div>
          </div>
        )}

        {/* Bulk Assign Rider Modal */}
        {bulkAssignModal && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => { setBulkAssignModal(false); setRiderSearch(""); }}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
                <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
                  <UserPlus className="w-5 h-5 text-[#FFB800]" /> Bulk Assign {selected.size} Shipments
                </h3>
                <button onClick={() => { setBulkAssignModal(false); setRiderSearch(""); }} className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 cursor-pointer p-1 rounded-lg"><X size={18} /></button>
              </div>

              <p className="text-xs text-gray-700 dark:text-gray-300 font-medium">
                Select a fleet courier to assign all <b className="text-[#111] dark:text-white font-bold">{selected.size} selected shipments</b> to simultaneously:
              </p>

              {/* Rider Search & Online Filter */}
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search size={14} className="absolute left-3 top-2.5 text-gray-500 dark:text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-9 pr-3 py-2 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setRiderOnlineOnly(!riderOnlineOnly)}
                  className={"px-3 py-2 rounded-xl text-[11px] font-bold border transition-colors shrink-0 cursor-pointer " + (riderOnlineOnly ? "bg-emerald-600 text-white border-emerald-600" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#2a2a2a]")}
                >
                  Online Only
                </button>
              </div>

              <div className="max-h-72 overflow-y-auto space-y-2 pr-1">
                {filteredDrivers.length === 0 ? (
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-medium text-center py-6">No matching riders found.</p>
                ) : (
                  filteredDrivers.map(r => {
                    const load = getRiderActiveLoad(r.id);
                    const loadClass = load === 0 ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/30" : load <= 2 ? "bg-amber-500/10 text-amber-800 dark:text-amber-400 border-amber-500/30" : "bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-500/30";
                    const loadText = load === 0 ? "0 active (Free)" : load <= 2 ? `${load} active` : `${load} active (Busy)`;
                    return (
                      <div key={r.id} className="p-3 bg-gray-50 dark:bg-[#222] rounded-2xl flex items-center justify-between border border-gray-200 dark:border-white/10 hover:border-[#FFB800]/50 transition-all gap-2">
                        <div className="flex items-center gap-2.5 min-w-0">
                          <div className="w-9 h-9 rounded-full bg-[#FFB800] text-[#111] font-black flex items-center justify-center text-xs shrink-0">
                            {(r.name || "?").charAt(0)}
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-2">
                              <p className="text-xs font-bold text-[#111] dark:text-white truncate">{r.name}</p>
                              <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline !== false ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline !== false ? "Online" : "Offline"} />
                            </div>
                            <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium truncate">
                              {r.phone || r.email} {r.bikeNumber ? `• ${r.bikeNumber}` : ""}
                            </p>
                            <span className={"inline-block text-[9px] font-black px-2 py-0.5 rounded-full border mt-1 " + loadClass}>
                              {loadText}
                            </span>
                          </div>
                        </div>
                        <button
                          onClick={() => { bulkAssignRider(r); setRiderSearch(""); }}
                          className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors shrink-0 shadow-xs cursor-pointer"
                        >
                          Assign All
                        </button>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        )}

        {/* Confirm Status Change Modal */}
        {confirmStatusModal.show && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setConfirmStatusModal({ delivery: null as any, newStatus: "", show: false })}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-[#FFB800]" /> Confirm Status Change</h3>
              <div className="space-y-2 text-xs">
                <p className="text-gray-700 dark:text-gray-300">Are you sure you want to change the status of shipment <span className="font-bold text-[#111] dark:text-white">#{idShort(confirmStatusModal.delivery.id)}</span>?</p>
                <div className="flex items-center gap-2">
                  <span className={"px-2 py-1 rounded-lg text-[10px] font-bold " + sStyle(confirmStatusModal.delivery.status)}>{confirmStatusModal.delivery.status}</span>
                  <span className="text-gray-500 dark:text-gray-400 font-bold">→</span>
                  <span className={"px-2 py-1 rounded-lg text-[10px] font-bold " + sStyle(confirmStatusModal.newStatus)}>{confirmStatusModal.newStatus}</span>
                </div>
              </div>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button onClick={() => setConfirmStatusModal({ delivery: null as any, newStatus: "", show: false })} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer">Cancel</button>
                <button onClick={confirmUpdateStatus} className="px-4 py-2.5 min-h-[38px] bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors cursor-pointer">Confirm Change</button>
              </div>
            </div>
          </div>
        )}

        {/* Confirm Bulk Update Modal */}
        {confirmBulkModal.show && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setConfirmBulkModal({ show: false })}>
            <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
              <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-[#FFB800]" /> Confirm Bulk Update</h3>
              <p className="text-xs text-gray-700 dark:text-gray-300">Are you sure you want to change <span className="font-bold text-[#111] dark:text-white">{selected.size} shipment(s)</span> to <span className={"px-2 py-1 rounded-lg text-[10px] font-bold " + sStyle(bulkStatus)}>{bulkStatus}</span>?</p>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button onClick={() => setConfirmBulkModal({ show: false })} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer">Cancel</button>
                <button onClick={confirmBulkUpdate} className="px-4 py-2.5 min-h-[38px] bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors cursor-pointer">Apply to All</button>
              </div>
            </div>
          </div>
        )}

        {/* Dispatch Decision Drawer (Design System) */}
        <DispatchDecisionDrawer
          isOpen={!!decisionDelivery}
          onClose={() => setDecisionDelivery(null)}
          delivery={decisionDelivery}
          riders={decisionRiders}
          onAssignRider={async (deliveryId, riderRecord) => {
            const matchedDriver = drivers.find(drv => drv.id === riderRecord.uid || drv.uid === riderRecord.uid);
            if (matchedDriver) {
              await assignRider(deliveryId, matchedDriver);
            }
          }}
          onReserveRider={async (deliveryId, riderRecord) => {
            const matchedDriver = drivers.find(drv => drv.id === riderRecord.uid || drv.uid === riderRecord.uid);
            if (matchedDriver) {
              await reserveRider(deliveryId, matchedDriver);
            }
          }}
          onUpdateStatus={async (deliveryId, newStatus) => {
            await updateStatus(deliveryId, newStatus);
          }}
          onAddLog={addLog}
        />
      </div>
    );
}

function ReferralsTab({ referrals, completedReferrals, searchQuery }: ReferralsTabProps) {
  const [search, setSearch] = useState("");
  const filtered = referrals.filter(r => { const q = (searchQuery || search).toLowerCase(); return r.referrerName.toLowerCase().includes(q) || r.refereeName.toLowerCase().includes(q); });
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Gift className="w-5 h-5 text-[#FFB800]" /> Referrals</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{referrals.length} total | {completedReferrals.length} completed | {fmt(referrals.reduce((s, r) => s + r.rewardAmount, 0))} total rewards</p></div>
      <SearchInput value={search} onChange={setSearch} placeholder="Search referrals..." />
    </div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl shadow-xs overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-xs"><thead className="bg-gray-50 dark:bg-[#222]">
          <tr><th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-3 border-b border-gray-200 dark:border-white/10">Referrer</th>
            <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-3 border-b border-gray-200 dark:border-white/10 hidden md:table-cell">Referee</th>
            <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-3 border-b border-gray-200 dark:border-white/10 hidden lg:table-cell">Reward</th>
            <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-3 border-b border-gray-200 dark:border-white/10">Status</th></tr>
        </thead><tbody className="divide-y divide-gray-100 dark:divide-white/10">
          {filtered.map(r => <tr key={r.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
            <td className="p-3"><p className="font-bold text-[#111] dark:text-white">{r.referrerName}</p><span className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{r.referrerEmail}</span></td>
            <td className="p-3 hidden md:table-cell"><p className="font-bold text-[#111] dark:text-white">{r.refereeName}</p><span className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{r.refereeEmail}</span></td>
            <td className="p-3 hidden lg:table-cell"><span className="font-bold text-[#111] dark:text-white">{fmt(r.rewardAmount)}</span></td>
            <td className="p-3"><span className={"text-[10px] font-bold px-2 py-0.5 rounded-full border " + (r.status === "completed" ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20" : "bg-amber-500/10 text-amber-800 dark:text-amber-400 border-amber-500/20")}>{r.status.toUpperCase()}</span></td>
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
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Percent className="w-5 h-5 text-[#FFB800]" /> Promotions</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{promotions.length} active</p></div>
      <button onClick={() => { setEditPromo({ id: "", title: "", description: "", discountType: "percentage", discountValue: 0, discountDisplay: "", code: "", usageLimit: 0, usedCount: 0, minOrderAmount: 0, maxDiscount: 0, active: true }); setPForm({ title: "", description: "", discountType: "percentage", discountValue: 0, discountDisplay: "", code: "", usageLimit: 0, minOrderAmount: 0, maxDiscount: 0, active: true }); }}
        className="px-4 py-2 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black shadow-xs transition-all flex items-center gap-2 cursor-pointer"><Plus className="w-3.5 h-3.5" /> New Promo</button>
    </div>
    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {promotions.length === 0 && <div className="sm:col-span-3 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-gray-500 dark:text-gray-400 font-medium">No promotions yet.</p></div>}
      {promotions.map((p, i) => {
        const dd = p.discountDisplay || (p.discountType === "percentage" ? p.discountValue + "% OFF" : "\u20A6" + p.discountValue.toLocaleString() + " OFF");
        return <div key={p.id} className={"animate-fade-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.02] transition-all duration-300 group " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i] || "")}>
          <div className="relative h-32 bg-gradient-to-br from-[#FFB800]/20 to-white dark:to-[#222] p-4 flex items-end">
            <div className="absolute top-3 right-3"><div className="relative"><div className="w-12 h-10 bg-[#FFB800]/80 rounded-md transform rotate-12" />
              <div className="w-10 h-8 bg-[#FFB800] rounded-md absolute -top-1 -left-1 transform -rotate-6 border border-[#FFB800]/50" />
              <div className="absolute -top-0.5 -left-0.5 text-[6px] font-black text-white px-1 bg-[#111] rounded-xs shadow-xs">ED</div></div></div>
            <div className="relative z-10">
              <div className="inline-block bg-[#111] dark:bg-white text-white dark:text-[#111] text-[10px] font-black px-2 py-0.5 rounded-sm mb-1.5 shadow-xs">{dd}</div>
              <h3 className="text-xs font-black text-[#111] dark:text-white">{p.title}</h3>
              <span className="inline-block mt-1 bg-[#FFB800]/20 text-gray-900 dark:text-white text-[8px] font-extrabold px-1.5 py-0.5 rounded-sm border border-[#FFB800]/40">CODE: {p.code}</span>
            </div>
          </div>
          <div className="p-3 space-y-1.5">
            <p className="text-[10px] text-gray-700 dark:text-gray-300 font-medium line-clamp-2">{p.description}</p>
            <div className="flex items-center justify-between pt-1">
              <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full border " + (p.active ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20" : "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400 border-gray-200 dark:border-white/10")}>{p.active ? "Active" : "Inactive"}</span>
              <span className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{p.usedCount}/{p.usageLimit} used</span>
            </div>
            <div className="flex items-center justify-end gap-1 pt-1 border-t border-gray-100 dark:border-white/10">
              <button onClick={() => { setEditPromo(p); setPForm({ title: p.title, description: p.description, discountType: p.discountType, discountValue: p.discountValue, discountDisplay: p.discountDisplay, code: p.code, usageLimit: p.usageLimit, minOrderAmount: p.minOrderAmount, maxDiscount: p.maxDiscount, active: p.active }); }} className="p-1.5 text-amber-800 dark:text-[#FFB800] hover:bg-[#FFB800]/10 rounded-lg cursor-pointer" title="Edit promotion"><Edit3 className="w-3.5 h-3.5" /></button>
              <button onClick={() => deletePromo(p.id)} className="p-1.5 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg cursor-pointer" title="Delete promotion"><Trash2 className="w-3.5 h-3.5" /></button>
            </div>
          </div>
        </div>;
      })}
    </div>
    {editPromo && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setEditPromo(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5 max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><Percent className="w-4 h-4 text-[#FFB800]" /> {editPromo.id ? "Edit" : "New"} Promo</h3>
        <div className="relative h-24 bg-gradient-to-br from-[#FFB800]/20 to-white dark:to-[#222] rounded-2xl overflow-hidden border border-gray-200 dark:border-white/10 p-3 flex items-end">
          <div className="absolute top-2 right-2"><div className="relative"><div className="w-10 h-8 bg-[#FFB800]/80 rounded-md transform rotate-12" />
            <div className="w-8 h-6 bg-[#FFB800] rounded-md absolute -top-1 -left-1 transform -rotate-6 border border-[#FFB800]/50" /></div></div>
          <div><div className="inline-block bg-[#111] dark:bg-white text-white dark:text-[#111] text-[8px] font-black px-1.5 py-0.5 rounded-sm mb-1">{pForm.discountDisplay || (pForm.discountType === "percentage" ? pForm.discountValue + "% OFF" : "\u20A6" + pForm.discountValue.toLocaleString() + " OFF")}</div>
            <p className="text-[10px] font-black text-[#111] dark:text-white">{pForm.title || "Promo Title"}</p>
            {pForm.code && <span className="inline-block mt-0.5 bg-[#FFB800]/20 text-gray-900 dark:text-white text-[7px] font-bold px-1 py-0.5 rounded-sm border border-[#FFB800]/30">CODE: {pForm.code}</span>}
          </div>
        </div>
        <div className="grid sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">TITLE (max 25)</label>
            <input value={pForm.title} maxLength={25} onChange={e => setPForm(f => ({ ...f, title: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">DESCRIPTION (max 60)</label>
            <input value={pForm.description} maxLength={60} onChange={e => setPForm(f => ({ ...f, description: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">DISCOUNT DISPLAY</label>
            <input value={pForm.discountDisplay} maxLength={15} onChange={e => setPForm(f => ({ ...f, discountDisplay: e.target.value }))} placeholder="Auto-generated if empty" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">TYPE</label>
            <Select value={pForm.discountType} onChange={v => setPForm(f => ({ ...f, discountType: v }))} options={[{value:"percentage",label:"Percentage"},{value:"fixed",label:"Fixed"}]} className="w-full" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">VALUE</label>
            <input type="number" value={pForm.discountValue || ""} onChange={e => setPForm(f => ({ ...f, discountValue: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">CODE (max 12)</label>
            <input value={pForm.code} maxLength={12} onChange={e => setPForm(f => ({ ...f, code: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">USAGE LIMIT</label>
            <input type="number" value={pForm.usageLimit || ""} onChange={e => setPForm(f => ({ ...f, usageLimit: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">MIN ORDER (\u20A6)</label>
            <input type="number" value={pForm.minOrderAmount || ""} onChange={e => setPForm(f => ({ ...f, minOrderAmount: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">MAX DISCOUNT (\u20A6)</label>
            <input type="number" value={pForm.maxDiscount || ""} onChange={e => setPForm(f => ({ ...f, maxDiscount: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="inline-flex items-center gap-2 cursor-pointer"><input type="checkbox" checked={pForm.active} onChange={e => setPForm(f => ({ ...f, active: e.target.checked }))} className="rounded border-gray-300 text-[#FFB800] focus:ring-[#FFB800]" /><span className="text-xs text-gray-700 dark:text-gray-300 font-semibold">Active</span></label></div>
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setEditPromo(null)} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer">Cancel</button>
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
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><LayoutGrid className="w-5 h-5 text-[#FFB800]" /> App Cards</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{appContent.length} cards</p></div>
      <button onClick={() => { setEditCard({ id: "", key: "", title: "", description: "", imageUrl: "", ctaText: "", ctaLink: "", order: appContent.length, active: true }); setCForm({ key: "", title: "", description: "", imageUrl: "", ctaText: "", ctaLink: "", order: appContent.length, active: true }); }}
        className="px-4 py-2 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black shadow-xs transition-all flex items-center gap-2 cursor-pointer"><Plus className="w-3.5 h-3.5" /> New Card</button>
    </div>
    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {appContent.length === 0 && <div className="sm:col-span-3 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-gray-500 dark:text-gray-400 font-medium">No cards yet.</p></div>}
      {appContent.map((c: AppContent, i: number) => <div key={c.id} className={"animate-fade-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.02] transition-all duration-300 group " + (["stagger-1","stagger-2","stagger-3","stagger-4","stagger-5","stagger-6"][i] || "")}>
        <div className="relative h-36 overflow-hidden"><div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(" + c.imageUrl + ")" }} />
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
          <div className="absolute bottom-2 left-3 right-3"><span className="inline-block bg-[#FFB800] text-[#111] text-[8px] font-black px-1.5 py-0.5 rounded-sm mb-1">{c.key}</span>
            <h3 className="text-white font-black text-xs drop-shadow-lg">{c.title}</h3></div>
        </div>
        <div className="p-3 space-y-1.5">
          <p className="text-[10px] text-gray-700 dark:text-gray-300 font-medium line-clamp-2">{c.description}</p>
          <div className="flex items-center justify-between pt-1 border-t border-gray-100 dark:border-white/10">
            <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full border " + (c.active ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20" : "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400 border-gray-200 dark:border-white/10")}>{c.active ? "Active" : "Inactive"}</span>
            <span className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">Order {c.order}</span>
          </div>
          <div className="flex items-center justify-end gap-1">
            <button onClick={() => { setEditCard(c); setCForm({ key: c.key, title: c.title, description: c.description, imageUrl: c.imageUrl, ctaText: c.ctaText, ctaLink: c.ctaLink, order: c.order, active: c.active }); }} className="p-1.5 text-amber-800 dark:text-[#FFB800] hover:bg-[#FFB800]/10 rounded-lg cursor-pointer" title="Edit Card"><Edit3 className="w-3.5 h-3.5" /></button>
            <button onClick={() => deleteCard(c.id)} className="p-1.5 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg cursor-pointer" title="Delete Card"><Trash2 className="w-3.5 h-3.5" /></button>
          </div>
        </div>
      </div>)}
    </div>
    {editCard && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setEditCard(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><LayoutGrid className="w-4 h-4 text-[#FFB800]" /> {editCard.id ? "Edit" : "New"} Card</h3>
        <div className="relative h-24 rounded-2xl overflow-hidden bg-gray-200 dark:bg-gray-800 border border-gray-200 dark:border-white/10">
          {cForm.imageUrl && <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(" + cForm.imageUrl + ")" }} />}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent" />
          <div className="absolute bottom-2 left-3"><span className="inline-block bg-[#FFB800] text-[#111] text-[7px] font-black px-1 py-0.5 rounded-sm">{cForm.key || "key"}</span>
            <p className="text-white font-black text-[10px] drop-shadow-lg">{cForm.title || "Card Title"}</p></div>
        </div>
        <div className="grid sm:grid-cols-2 gap-3">
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">KEY (max 20)</label>
            <input value={cForm.key} maxLength={20} onChange={e => setCForm(f => ({ ...f, key: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">ORDER</label>
            <input type="number" value={cForm.order} onChange={e => setCForm(f => ({ ...f, order: +e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">TITLE (max 30)</label>
            <input value={cForm.title} maxLength={30} onChange={e => setCForm(f => ({ ...f, title: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">DESCRIPTION (max 80)</label>
            <input value={cForm.description} maxLength={80} onChange={e => setCForm(f => ({ ...f, description: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div className="sm:col-span-2"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">IMAGE URL</label>
            <input value={cForm.imageUrl} onChange={e => setCForm(f => ({ ...f, imageUrl: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">CTA TEXT</label>
            <input value={cForm.ctaText} onChange={e => setCForm(f => ({ ...f, ctaText: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">CTA LINK</label>
            <input value={cForm.ctaLink} onChange={e => setCForm(f => ({ ...f, ctaLink: e.target.value }))} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
          <div><label className="inline-flex items-center gap-2 cursor-pointer"><input type="checkbox" checked={cForm.active} onChange={e => setCForm(f => ({ ...f, active: e.target.checked }))} className="rounded border-gray-300 text-[#FFB800] focus:ring-[#FFB800]" /><span className="text-xs text-gray-700 dark:text-gray-300 font-semibold">Active</span></label></div>
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setEditCard(null)} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer">Cancel</button>
          <SaveBtn onClick={() => saveCard(editCard.id ? editCard : undefined)} loading={saving} />
        </div>
      </div>
    </div>}
  </div>;
}

function Toggle({ label, desc, checked, onChange }: { label: string; desc?: string; checked: boolean; onChange: (v: boolean) => void }) {
  return <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-white/5 border border-gray-100 dark:border-white/5 rounded-2xl">
    <div><p className="text-xs font-bold text-[#111] dark:text-white">{label}</p>{desc && <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{desc}</p>}</div>
    <label className="relative inline-flex items-center cursor-pointer"><input type="checkbox" checked={checked} onChange={e => onChange(e.target.checked)} className="sr-only peer" />
      <div className="w-9 h-5 bg-gray-200 dark:bg-gray-600 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[#FFB800]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-[#FFB800]"></div></label></div>;
}

function SettingsTab({ db, addLog }: SettingsTabProps) {
  const [sForm, setSForm] = useState<any>({});
  const [fcmKey, setFcmKey] = useState("");
  const [showFcm, setShowFcm] = useState(false);
  const [saving, setSaving] = useState(false);
  useEffect(() => {
    const unsub = onSnapshot(doc(db, "system_config", "global_settings"), snap => {
      if (snap.exists()) {
        const d = snap.data();
        // Never bind fcmServerKey from the shared (app-readable) doc - secrets live in admin_config only
        const { fcmServerKey: _, ...publicKeys } = d;
        setSForm(publicKeys);
      }
    });
    getDoc(doc(db, "admin_config", "fcm")).then(s => {
      if (s.exists() && (s.data() as any).fcmServerKey) setFcmKey((s.data() as any).fcmServerKey);
    }).catch(() => {});
    return unsub;
  }, []);
  const upd = (k: string, v: any) => setSForm((f: any) => ({ ...f, [k]: v }));
  const saveSettings = async (section?: string) => {
    setSaving(true);
    const payload: any = { ...sForm, updatedAt: Timestamp.now() };
    delete payload.fcmServerKey;
    await setDoc(doc(db, "system_config", "global_settings"), payload, { merge: true });
    if (fcmKey) await setDoc(doc(db, "admin_config", "fcm"), { fcmServerKey: fcmKey, updatedAt: Timestamp.now() }, { merge: true });
    addLog("Update Settings", section || "General");
    setSaving(false);
  };
  return <div className="tab-content space-y-6">
    <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><Settings2 className="w-5 h-5 text-[#FFB800]" /> Settings</h1>
      <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">System preferences and configuration</p></div>

    {/* Section 1 — System Controls */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><Shield className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">System Controls</span></div>
      <div className="grid sm:grid-cols-2 gap-3">
        <Toggle label="Allow Signups" desc="Enable new user registration" checked={!!sForm.allowSignups} onChange={v => upd("allowSignups", v)} />
        <Toggle label="Require Approval" desc="Admin must approve new accounts" checked={!!sForm.requireApproval} onChange={v => upd("requireApproval", v)} />
        <Toggle label="Maintenance Mode" desc="Disable app access for users" checked={!!sForm.maintenanceMode} onChange={v => upd("maintenanceMode", v)} />
        <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-white/5 border border-gray-100 dark:border-white/5 rounded-2xl"><div><p className="text-xs font-bold text-[#111] dark:text-white">Default Role</p></div>
          <Select value={sForm.defaultRole || "customer"} onChange={v => upd("defaultRole", v)} options={[{value:"customer",label:"Customer"},{value:"rider",label:"Rider"},{value:"admin",label:"Admin"}]} className="w-36" /></div>
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("System Controls")} loading={saving} /></div>
    </div>

    {/* Section 2 — Feature Toggles */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><Zap className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Feature Toggles</span></div>
      <div className="grid sm:grid-cols-2 gap-3">
        <Toggle label="Marketplace & Vendor Stores" desc="Master switch: enable or hide all storefronts, shop carousels and vendor features on mobile app" checked={sForm.marketplaceEnabled !== false} onChange={v => upd("marketplaceEnabled", v)} />
        <Toggle label="Points & Loyalty" desc="Bronze/Silver/Gold/Platinum tier system" checked={!!sForm.pointsAndLoyalty} onChange={v => { upd("pointsSystemEnabled", v); upd("pointsAndLoyalty", v); }} />
        <Toggle label="Driver Tips" desc="Customers can add tips on delivery" checked={!!sForm.driverTipsEnabled} onChange={v => { upd("tipSystemEnabled", v); upd("driverTipsEnabled", v); }} />
        <Toggle label="Auto-Verify Vendors" desc="Instantly approve vendor stores once KYC + delivery milestone are met" checked={!!sForm.autoVerifyVendors} onChange={v => upd("autoVerifyVendors", v)} />
        <Toggle label="Referral System" desc="Referral rewards and invite codes" checked={!!sForm.referralEnabled} onChange={v => upd("referralEnabled", v)} />
        <Toggle label="Dynamic Pricing" desc="Surge pricing based on demand" checked={!!sForm.dynamicPricing} onChange={v => upd("dynamicPricing", v)} />
        <Toggle label="Phone Verification (SMS/WhatsApp/Call)" desc="Require phone number OTP verification for order booking and account security" checked={!!sForm.phoneVerificationEnabled} onChange={v => upd("phoneVerificationEnabled", v)} />
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Feature Toggles")} loading={saving} /></div>
    </div>

    {/* Section 3 — Delivery Types */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><Truck className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Delivery Types</span></div>
      <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">Manage delivery categories — rename, enable/disable, set per-type pricing</p>
      <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
        <table className="w-full text-xs">
          <thead className="bg-gray-50 dark:bg-[#222]">
            <tr>
              <th className="p-2 text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] w-10">On</th>
              <th className="p-2 text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px]">Name</th>
              <th className="p-2 text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] hidden md:table-cell">Desc</th>
              <th className="p-2 text-right font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] w-20">Base (₦)</th>
              <th className="p-2 text-right font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] w-16">/km</th>
              <th className="p-2 text-right font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] w-16">/kg</th>
              <th className="p-2 w-8"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 dark:divide-white/10">
            {(() => {
              const defaults = [
                { id: "standard", name: "Standard", enabled: true, baseFare: 1000, perKm: 100, perKg: 50, description: "Regular delivery within 24hrs" },
                { id: "express", name: "Express", enabled: true, baseFare: 2500, perKm: 200, perKg: 80, description: "Same-day priority delivery" },
                { id: "economy", name: "Economy", enabled: true, baseFare: 700, perKm: 60, perKg: 30, description: "Budget-friendly 48hr delivery" },
                { id: "coldchain", name: "Cold Chain", enabled: true, baseFare: 3000, perKm: 250, perKg: 100, description: "Temperature-controlled transport" },
                { id: "batch", name: "Batch", enabled: true, baseFare: 5000, perKm: 150, perKg: 40, description: "Multiple packages same route" },
                { id: "multi", name: "Multi-Stop", enabled: true, baseFare: 4000, perKm: 180, perKg: 60, description: "Multiple drop-off points" },
              ];
              const activeTypes = sForm.deliveryTypes || defaults;
              if (!sForm.deliveryTypes) setTimeout(() => upd("deliveryTypes", defaults), 0);
              return activeTypes.map((t: any, i: number) => (
                <tr key={t.id} className={"hover:bg-black/5 dark:hover:bg-white/5 transition-colors " + (t.enabled ? "" : "opacity-50")}>
                  <td className="p-2">
                    <button onClick={() => { const dts = [...activeTypes]; dts[i] = { ...dts[i], enabled: !dts[i].enabled }; upd("deliveryTypes", dts); }}
                      className={"w-8 h-4 rounded-full transition-colors relative cursor-pointer " + (t.enabled ? "bg-green-500" : "bg-gray-300 dark:bg-gray-600")}>
                      <span className={"absolute top-0.5 w-3 h-3 bg-white rounded-full shadow-xs transition-transform " + (t.enabled ? "translate-x-[18px]" : "translate-x-0.5")} />
                    </button>
                  </td>
                  <td className="p-2">
                    <input value={t.name} onChange={e => { const dts = [...activeTypes]; dts[i] = { ...dts[i], name: e.target.value }; upd("deliveryTypes", dts); }}
                      className="w-full bg-transparent text-xs font-bold text-[#111] dark:text-white border-b border-dashed border-transparent focus:border-[#FFB800] focus:outline-none" />
                  </td>
                  <td className="p-2 hidden md:table-cell">
                    <input value={t.description || ""} onChange={e => { const dts = [...activeTypes]; dts[i] = { ...dts[i], description: e.target.value }; upd("deliveryTypes", dts); }}
                      className="w-full bg-transparent text-[10px] text-gray-600 dark:text-gray-400 border-b border-dashed border-transparent focus:border-black/20 dark:focus:border-white/20 focus:outline-none placeholder-gray-400 dark:placeholder-gray-500" placeholder="Description" />
                  </td>
                  <td className="p-2">
                    <input type="number" value={t.baseFare ?? ""} onChange={e => { const dts = [...activeTypes]; dts[i] = { ...dts[i], baseFare: parseFloat(e.target.value) || 0 }; upd("deliveryTypes", dts); }}
                      className="w-full text-right bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-lg px-1.5 py-1 text-[10px] text-[#111] dark:text-white font-medium" />
                  </td>
                  <td className="p-2">
                    <input type="number" value={t.perKm ?? ""} onChange={e => { const dts = [...activeTypes]; dts[i] = { ...dts[i], perKm: parseFloat(e.target.value) || 0 }; upd("deliveryTypes", dts); }}
                      className="w-full text-right bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-lg px-1.5 py-1 text-[10px] text-[#111] dark:text-white font-medium" />
                  </td>
                  <td className="p-2">
                    <input type="number" value={t.perKg ?? ""} onChange={e => { const dts = [...activeTypes]; dts[i] = { ...dts[i], perKg: parseFloat(e.target.value) || 0 }; upd("deliveryTypes", dts); }}
                      className="w-full text-right bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-lg px-1.5 py-1 text-[10px] text-[#111] dark:text-white font-medium" />
                  </td>
                  <td className="p-2">
                    <button onClick={() => { const dts = [...activeTypes]; dts.splice(i, 1); upd("deliveryTypes", dts); }}
                      className="text-red-500 hover:text-red-700 transition-colors p-1 cursor-pointer"><X className="w-3.5 h-3.5" /></button>
                  </td>
                </tr>
              ));
            })()}
          </tbody>
        </table>
      </div>
      <button onClick={() => {
        const dts = [...(sForm.deliveryTypes || [])];
        const newId = "custom" + (dts.length + 1);
        dts.push({ id: newId, name: "New Type", enabled: true, baseFare: 1000, perKm: 100, perKg: 50, description: "" });
        upd("deliveryTypes", dts);
      }} className="text-[10px] font-extrabold text-amber-800 dark:text-[#FFB800] hover:text-amber-900 dark:hover:text-[#e6b13b] flex items-center gap-1 cursor-pointer"><Plus className="w-3 h-3" /> Add Type</button>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Delivery Types")} loading={saving} /></div>
    </div>

    {/* Section 4 — Pricing */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><DollarSign className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Pricing</span></div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">BASE FARE (₦)</label>
          <input type="number" value={sForm.baseFare ?? ""} onChange={e => upd("baseFare", parseFloat(e.target.value) || 0)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">PER KG RATE (₦)</label>
          <input type="number" step="0.1" value={sForm.perKgRate ?? ""} onChange={e => upd("perKgRate", parseFloat(e.target.value) || 0)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">EXPRESS SURCHARGE (₦)</label>
          <input type="number" value={sForm.expressSurcharge ?? ""} onChange={e => upd("expressSurcharge", parseFloat(e.target.value) || 0)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">SURGE MULTIPLIER (×)</label>
          <input type="number" step="0.1" min="1" value={sForm.surgeMultiplier ?? ""} onChange={e => upd("surgeMultiplier", parseFloat(e.target.value) || 1)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Pricing")} loading={saving} /></div>
    </div>

    {/* Section 4 — Points & Rewards Configuration */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><Award className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Points & Rewards</span></div>
      <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">Configure loyalty tiers, welcome gift, and referral rewards</p>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Toggle label="Points System Active" desc="" checked={!!(sForm.appContent?.loyalty?.active ?? sForm.pointsAndLoyalty)} onChange={v => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), active: v } })} />
        <Toggle label="Daily Bonus" desc="" checked={!!(sForm.appContent?.loyalty?.dailyBonusEnabled ?? true)} onChange={v => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), dailyBonusEnabled: v } })} />
      </div>

      <div className="text-xs font-bold text-[#111] dark:text-white pt-2">Loyalty Tier Thresholds (orders)</div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Bronze</label>
          <input type="number" value={sForm.appContent?.loyalty?.bronzeThreshold ?? 3} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), bronzeThreshold: parseInt(e.target.value) || 3 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Silver</label>
          <input type="number" value={sForm.appContent?.loyalty?.silverThreshold ?? 10} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), silverThreshold: parseInt(e.target.value) || 10 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Gold</label>
          <input type="number" value={sForm.appContent?.loyalty?.goldThreshold ?? 25} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), goldThreshold: parseInt(e.target.value) || 25 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Platinum</label>
          <input type="number" value={sForm.appContent?.loyalty?.platinumThreshold ?? 50} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), platinumThreshold: parseInt(e.target.value) || 50 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
      </div>

      <div className="text-xs font-bold text-[#111] dark:text-white pt-2">Orders Required Per Tier</div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Bronze Req.</label>
          <input type="number" value={sForm.appContent?.loyalty?.ordersForBronze ?? 1} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), ordersForBronze: parseInt(e.target.value) || 1 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Silver Req.</label>
          <input type="number" value={sForm.appContent?.loyalty?.ordersForSilver ?? 3} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), ordersForSilver: parseInt(e.target.value) || 3 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Gold Req.</label>
          <input type="number" value={sForm.appContent?.loyalty?.ordersForGold ?? 5} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), ordersForGold: parseInt(e.target.value) || 5 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Platinum Req.</label>
          <input type="number" value={sForm.appContent?.loyalty?.ordersForPlatinum ?? 10} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), ordersForPlatinum: parseInt(e.target.value) || 10 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
      </div>

      <div className="grid sm:grid-cols-3 gap-4 pt-2">
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Daily Bonus Points</label>
          <input type="number" value={sForm.appContent?.loyalty?.dailyBonus ?? 10} onChange={e => upd("appContent", { ...(sForm.appContent || {}), loyalty: { ...(sForm.appContent?.loyalty || {}), dailyBonus: parseInt(e.target.value) || 10 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Welcome Gift (₦)</label>
          <input type="number" value={sForm.appContent?.welcomeGift?.credit ?? 2500} onChange={e => upd("appContent", { ...(sForm.appContent || {}), welcomeGift: { ...(sForm.appContent?.welcomeGift || {}), credit: parseFloat(e.target.value) || 0 } })}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="text-[10px] font-bold text-gray-600 dark:text-gray-400 block">Referral Reward (₦)</label>
          <input type="number" value={sForm.referralReward ?? 500} onChange={e => upd("referralReward", parseFloat(e.target.value) || 0)}
            className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-2 py-1.5 text-xs text-[#111] dark:text-white" /></div>
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Points & Rewards")} loading={saving} /></div>
    </div>

    {/* Section 5 — Branding & Communication */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><Globe className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Branding & Communication</span></div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">APP NAME</label>
          <input value={sForm.appName ?? "ESDispatch"} onChange={e => upd("appName", e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">APP SLOGAN</label>
          <input value={sForm.appSlogan ?? ""} onChange={e => upd("appSlogan", e.target.value)} placeholder="Premium Logistics & Dispatch" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">APP VERSION</label>
          <input value={sForm.appVersion ?? "1.0.0"} onChange={e => upd("appVersion", e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">CONTACT EMAIL</label>
          <input value={sForm.contactEmail ?? ""} onChange={e => upd("contactEmail", e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">SUPPORT PHONE</label>
          <input value={sForm.supportPhone ?? ""} onChange={e => upd("supportPhone", e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">SUPPORT WHATSAPP</label>
          <input value={sForm.supportWhatsapp ?? ""} onChange={e => upd("supportWhatsapp", e.target.value)} placeholder="e.g. +2348012345678" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
        <div className="relative"><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">FCM SERVER KEY</label>
          <div className="relative"><input type={showFcm ? "text" : "password"} value={fcmKey} onChange={e => setFcmKey(e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2 pr-8 text-xs text-[#111] dark:text-white" placeholder="Stored in admin-only config"/>
            <button onClick={() => setShowFcm(!showFcm)} className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 cursor-pointer"><Eye size={14} /></button></div></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">PLAY STORE URL</label>
          <input value={sForm.playStoreUrl ?? ""} onChange={e => upd("playStoreUrl", e.target.value)} placeholder="https://play.google.com/store/apps/details?id=..." className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">APP STORE URL</label>
          <input value={sForm.appStoreUrl ?? ""} onChange={e => upd("appStoreUrl", e.target.value)} placeholder="https://apps.apple.com/app/id..." className="w-full bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 py-2.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500" /></div>
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Branding")} loading={saving} /></div>
    </div>

    {/* Section 6 — Master System Overrides */}
    <div className="bg-white dark:bg-[#1a1a1a] border-2 border-[#FFB800]/40 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center gap-2 pb-1 border-b border-gray-100 dark:border-white/10"><AlertTriangle className="w-4 h-4 text-[#FFB800]" /><span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Master System Overrides</span></div>
      <div className="grid sm:grid-cols-2 gap-3">
        <Toggle label="Broadcast Surge Pricing" desc="Push surge multiplier to all active pricing" checked={!!sForm.broadcastSurge} onChange={v => upd("broadcastSurge", v)} />
        <Toggle label="Fleet Sync" desc="Force synchronization across all drivers" checked={!!sForm.fleetSync} onChange={v => upd("fleetSync", v)} />
      </div>
      <div className="flex justify-end pt-1"><SaveBtn onClick={() => saveSettings("Master Overrides")} loading={saving} /></div>
    </div>
  </div>;
}


function MarketplaceTab({ products, stores, orders, payoutRequests, db, addLog, addToast, seedMarketplace, marketplaceEnabled, toggleMarketplace }: {
  products: Product[]; stores: VendorStore[]; orders: MarketplaceOrder[]; payoutRequests: VendorPayoutRequest[];
  db: any; addLog: (a: string, d: string) => Promise<void> | void; addToast: (t: Toast["type"], m: string) => void;
  seedMarketplace: () => Promise<void>;
  marketplaceEnabled: boolean;
  toggleMarketplace: () => Promise<void> | void;
}) {
  const [activeSubTab, setActiveSubTab] = useState<"products" | "stores" | "orders" | "payouts">("products");
  const [search, setSearch] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [showAddStoreModal, setShowAddStoreModal] = useState(false);
  const [deleteProductTarget, setDeleteProductTarget] = useState<Product | null>(null);

  // New product form
  const [pName, setPName] = useState("");
  const [pCategory, setPCategory] = useState("Delivery Gear");
  const [pPrice, setPPrice] = useState("");
  const [pStock, setPStock] = useState("");
  const [pVendor, setPVendor] = useState("ESDispatch Fleet Supplies");
  const [pDesc, setPDesc] = useState("");
  const [pImg, setPImg] = useState("");
  const [savingProduct, setSavingProduct] = useState(false);

  // New Store form
  const [sName, setSName] = useState("");
  const [sOwner, setSOwner] = useState("");
  const [sEmail, setSEmail] = useState("");
  const [sPhone, setSPhone] = useState("");
  const [sCat, setSCat] = useState("Spare Parts & Accessories");
  const [sComm, setSComm] = useState("10");
  const [sDesc, setSDesc] = useState("");
  const [sAddr, setSAddr] = useState("");
  const [sLogo, setSLogo] = useState("");
  const [sCover, setSCover] = useState("");
  const [savingStore, setSavingStore] = useState(false);

  // Edit / transfer / delete store targets
  const [editStoreTarget, setEditStoreTarget] = useState<VendorStore | null>(null);
  const [transferStoreTarget, setTransferStoreTarget] = useState<VendorStore | null>(null);
  const [transferEmail, setTransferEmail] = useState("");
  const [savingStoreOp, setSavingStoreOp] = useState(false);

  const totalProducts = products.filter(p => !p.isDeleted).length;
  const approvedStores = stores.filter(s => s.status === "APPROVED" && !s.isDeleted);
  const pendingStores = stores.filter(s => s.status === "PENDING" && !s.isDeleted);
  const totalMarketplaceSales = orders.filter(o => o.status === "PAID" || o.status === "FULFILLED").reduce((s, o) => s + (o.totalPrice || 0), 0);

  const filteredProducts = products.filter(p => !p.isDeleted && (p.name.toLowerCase().includes(search.toLowerCase()) || p.category.toLowerCase().includes(search.toLowerCase()) || p.vendorStore.toLowerCase().includes(search.toLowerCase())));
  const filteredStores = stores.filter(s => !s.isDeleted && (s.storeName.toLowerCase().includes(search.toLowerCase()) || s.ownerName.toLowerCase().includes(search.toLowerCase()) || s.category.toLowerCase().includes(search.toLowerCase())));

  const handleAddProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pName || !pPrice) { addToast("error", "Product name and price are required"); return; }
    setSavingProduct(true);
    try {
      const priceNum = parseFloat(pPrice) || 0;
      const stockNum = parseInt(pStock) || 0;
      const statusStr = stockNum === 0 ? "Out of Stock" : stockNum < 10 ? "Low Stock" : "In Stock";
      await addDoc(collection(db, "marketplace_products"), {
        name: pName, category: pCategory, price: priceNum, stock: stockNum,
        vendorStore: pVendor, description: pDesc, imageUrl: pImg || "https://images.unsplash.com/photo-1580674285054-bed31e145f59?w=500",
        status: statusStr, rating: 5.0, createdAt: Timestamp.now()
      });
      addLog("Create Product", `Added product '${pName}' (₦${priceNum.toLocaleString()})`);
      addToast("success", `Product '${pName}' created successfully`);
      setShowAddModal(false);
      setPName(""); setPPrice(""); setPStock(""); setPDesc(""); setPImg("");
    } catch (err: any) {
      addToast("error", "Failed to add product: " + err.message);
    } finally { setSavingProduct(false); }
  };

  const handleAddStore = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sName || !sOwner || !sEmail) { addToast("error", "Store name, owner, and email are required"); return; }
    setSavingStore(true);
    try {
      // Resolve owner user — link to an existing account (by email) or create a vendor user doc
      let ownerId = "";
      let ownerEmail = sEmail.trim().toLowerCase();
      const matched = await getDocs(query(collection(db, "users"), where("email", "==", ownerEmail)));
      let existingUser: any = null;
      matched.forEach(d => { const x = d.data(); if (x.isDeleted) return; existingUser = { id: d.id, ...x }; });
      if (existingUser) {
        ownerId = existingUser.id;
        await updateDoc(doc(db, "users", ownerId), { role: "vendor", userRole: "Vendor", isVendorVerified: true, updatedAt: Timestamp.now() });
      } else {
        ownerId = "vendor_" + Date.now();
        await setDoc(doc(db, "users", ownerId), {
          uid: ownerId, name: sOwner, email: ownerEmail, phone: sPhone || "", role: "vendor",
          userRole: "Vendor", isVendorVerified: true, pin: "", status: "offline", isOnline: false,
          rating: 0, deliveryCount: 0, walletBalance: 0, loyaltyPoints: 0, photoUrl: "",
          isDeleted: false, pendingAuth: true, createdAt: Timestamp.now(), updatedAt: Timestamp.now()
        });
        addLog("Create Vendor User", `Created vendor user record '${sOwner}' (${ownerEmail})`);
      }
      const today = new Date().toISOString().slice(0, 10);
      await setDoc(doc(db, "marketplace_stores", ownerId), {
        id: ownerId, ownerId, storeName: sName, category: sCat, ownerName: sOwner, email: ownerEmail,
        phone: sPhone, description: sDesc, address: sAddr, logoUrl: sLogo, coverUrl: sCover,
        commissionRate: parseFloat(sComm) || 10, vendorBalance: 0, totalSales: 0, storeRating: 5.0,
        status: "APPROVED", isVerified: true, isPendingReview: false, kycStatus: "approved",
        isFeatured: false, featuredRank: 0, isDemo: false, isDeleted: false,
        verificationNote: "Enlisted by ESDispatch admin", dateEnlisted: today,
        createdAt: Timestamp.now(), verifiedAt: Timestamp.now(), updatedAt: Timestamp.now()
      });
      addLog("Create Store", `Enlisted vendor store '${sName}' owned by '${sOwner}'`);
      addToast("success", `Vendor store '${sName}' enlisted and approved`);
      setShowAddStoreModal(false);
      setSName(""); setSOwner(""); setSEmail(""); setSPhone(""); setSDesc(""); setSAddr(""); setSLogo(""); setSCover("");
    } catch (err: any) {
      addToast("error", "Failed to enlist store: " + err.message);
    } finally { setSavingStore(false); }
  };

  const handleUpdateStoreStatus = async (storeId: string, storeName: string, status: "APPROVED" | "REJECTED") => {
    try {
      const approved = status === "APPROVED";
      // Mirrors mobile VendorPortal/admin schema so approval reflects instantly in-app
      await updateDoc(doc(db, "marketplace_stores", storeId), {
        status,
        isVerified: approved,
        isPendingReview: !approved,
        kycStatus: approved ? "approved" : "rejected",
        verificationNote: approved ? "Approved by ESDispatch admin" : "Rejected by ESDispatch admin",
        reviewedAt: Timestamp.now(),
        updatedAt: Timestamp.now()
      });
      addLog("Update Store", `Set store '${storeName}' status to ${status}`);
      addToast("success", `Store '${storeName}' is now ${status}`);
    } catch (err: any) {
      addToast("error", "Failed to update store status: " + err.message);
    }
  };

  // Save editable store profile fields (name/owner/contact/category/commission/description/address/images/featured rank)
  const handleEditStore = async (store: VendorStore) => {
    setSavingStoreOp(true);
    try {
      await updateDoc(doc(db, "marketplace_stores", store.id), {
        storeName: store.storeName,
        ownerName: store.ownerName,
        email: store.email,
        phone: store.phone,
        category: store.category,
        commissionRate: store.commissionRate || 10,
        description: store.description || "",
        address: store.address || "",
        logoUrl: store.logoUrl || "",
        coverUrl: store.coverUrl || "",
        isFeatured: !!store.isFeatured,
        featuredRank: store.isFeatured ? (store.featuredRank ?? 1) : 0,
        updatedAt: Timestamp.now()
      });
      addLog("Update Store", `Edited store '${store.storeName}'`);
      addToast("success", `Store '${store.storeName}' updated`);
      setEditStoreTarget(null);
    } catch (err: any) {
      addToast("error", "Failed to update store: " + err.message);
    } finally { setSavingStoreOp(false); }
  };

  /** Quick toggle for the featured carousel slot without opening the full editor */
  const handleToggleFeatured = async (store: VendorStore) => {
    try {
      const nextFeatured = !store.isFeatured;
      await updateDoc(doc(db, "marketplace_stores", store.id), {
        isFeatured: nextFeatured,
        featuredRank: nextFeatured ? ((store.featuredRank ?? 0) > 0 ? (store.featuredRank ?? 1) : 1) : 0,
        updatedAt: Timestamp.now()
      });
      addLog("Update Store", `${nextFeatured ? "Featured" : "Unfeatured"} store '${store.storeName}'`);
      addToast("success", nextFeatured ? `'${store.storeName}' is now featured on the Marketplace` : `'${store.storeName}' removed from featured`);
    } catch (err: any) {
      addToast("error", "Featured toggle failed: " + err.message);
    }
  };

  /** Transfer store ownership to another registered user: store doc + all linked products reassigned */
  const handleTransferStore = async (store: VendorStore, targetEmail: string) => {
    const email = targetEmail.trim().toLowerCase();
    if (!email) { addToast("error", "Enter the new owner's registered email"); return; }
    setSavingStoreOp(true);
    try {
      const matched = await getDocs(query(collection(db, "users"), where("email", "==", email)));
      let newOwner: any = null;
      matched.forEach(d => { const x = d.data(); if (!x.isDeleted) newOwner = { id: d.id, ...x }; });
      if (!newOwner) {
        addToast("error", `No user account found for '${email}'. The target owner must have a registered account.`);
        return;
      }
      const oldOwnerId = store.ownerId || store.id;
      const newOwnerId = newOwner.id;
      const batch = writeBatch(db);
      batch.set(doc(db, "marketplace_stores", newOwnerId), {
        id: newOwnerId, ownerId: newOwnerId, ownerName: newOwner.name || newOwner.displayName || newOwnerId,
        email, ownerEmail: email, storeName: store.storeName, category: store.category,
        phone: newOwner.phone || store.phone || "",
        description: store.description || "",
        address: store.address || "",
        logoUrl: store.logoUrl || "",
        coverUrl: store.coverUrl || "",
        commissionRate: store.commissionRate || 10,
        vendorBalance: store.vendorBalance || 0, totalSales: store.totalSales || 0, storeRating: store.storeRating || 5.0,
        status: store.status || "APPROVED", isVerified: !!store.isVerified, isPendingReview: !store.isVerified,
        kycStatus: store.isVerified ? "approved" : "submitted",
        isFeatured: !!store.isFeatured, featuredRank: store.featuredRank || 0,
        isDemo: false, isDeleted: false,
        dateEnlisted: store.dateEnlisted || new Date().toISOString().slice(0, 10),
        transferredAt: Timestamp.now(), updatedAt: Timestamp.now()
      }, { merge: true });
      batch.update(doc(db, "marketplace_stores", oldOwnerId), {
        isDeleted: true, status: "REJECTED", isVerified: false, isPendingReview: false,
        updatedAt: Timestamp.now()
      });
      // Reassign all products of the old owner to the new owner
      const prodSnap = await getDocs(query(collection(db, "marketplace_products"), where("vendorId", "==", oldOwnerId)));
      prodSnap.forEach(pd => {
        const pdData = pd.data();
        batch.update(pd.ref, {
          vendorId: newOwnerId,
          vendorStore: store.storeName || pdData.vendorStore || "Store",
          updatedAt: Timestamp.now()
        });
      });
      await batch.commit();
      await updateDoc(doc(db, "users", newOwnerId), { role: "vendor", userRole: "Vendor", isVendorVerified: true, updatedAt: Timestamp.now() });
      addLog("Transfer Store", `Transferred '${store.storeName}' from '${store.ownerName}' to '${newOwner.name || email}' (${prodSnap.size} products reassigned)`);
      addToast("success", `Store transferred to '${email}' with ${prodSnap.size} products`);
      setTransferStoreTarget(null);
      setTransferEmail("");
    } catch (err: any) {
      addToast("error", "Transfer failed: " + err.message);
    } finally { setSavingStoreOp(false); }
  };

  /** Soft-delete a store: hidden from the app immediately, products stay for audit */
  const handleDeleteStore = async (store: VendorStore) => {
    await updateDoc(doc(db, "marketplace_stores", store.id), {
      isDeleted: true, isPendingReview: false, updatedAt: Timestamp.now()
    });
    addLog("Delete Store", `Soft-deleted store '${store.storeName}'`);
    addToast("success", `Store '${store.storeName}' removed from the marketplace`);
  };

  const handleDeleteProduct = async (product: Product) => {
    try {
      await updateDoc(doc(db, "marketplace_products", product.id), { isDeleted: true, updatedAt: Timestamp.now() });
      addLog("Delete Product", `Deleted product '${product.name}'`);
      addToast("success", `Product '${product.name}' deleted`);
      setDeleteProductTarget(null);
    } catch (err: any) {
      addToast("error", "Failed to delete product: " + err.message);
    }
  };

  const handleUpdateProductStock = async (productId: string, name: string, stock: number) => {
    try {
      const statusStr = stock === 0 ? "Out of Stock" : stock < 10 ? "Low Stock" : "In Stock";
      await updateDoc(doc(db, "marketplace_products", productId), { stock, status: statusStr, updatedAt: Timestamp.now() });
      addLog("Update Stock", `Updated '${name}' stock to ${stock}`);
      addToast("success", `Stock updated for '${name}'`);
    } catch (err: any) {
      addToast("error", "Stock update failed: " + err.message);
    }
  };

  const handleUpdateProductPrice = async (productId: string, name: string, price: number) => {
    try {
      await updateDoc(doc(db, "marketplace_products", productId), { price, updatedAt: Timestamp.now() });
      addLog("Update Price", `Updated '${name}' price to ₦${price.toLocaleString()}`);
      addToast("success", `Price updated for '${name}'`);
    } catch (err: any) {
      addToast("error", "Price update failed: " + err.message);
    }
  };

  return <div className="tab-content space-y-8">
    {/* Header & Metrics */}
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 className="text-[28px] font-extrabold tracking-tight text-[#111] dark:text-white flex items-center gap-3">
          <ShoppingBag className="w-7 h-7 text-[#FFB800]" /> Marketplace & Store Operations
        </h1>
        <p className="text-xs font-medium text-gray-600 dark:text-gray-400 mt-1">Manage vendor product catalogs, store enlistments, prices, inventory, and sales commissions.</p>
      </div>
      <div className="flex items-center gap-3">
        {products.length === 0 && (
          <button onClick={seedMarketplace} className="px-4 py-2.5 bg-[#FFB800]/20 border border-[#FFB800]/40 text-[#111] dark:text-white text-xs font-bold rounded-2xl hover:bg-[#FFB800]/30 transition-all flex items-center gap-2">
            <RefreshCw className="w-4 h-4 text-[#FFB800]" /> Seed Marketplace Data
          </button>
        )}
        <button onClick={() => setShowAddModal(true)} className="px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black text-xs rounded-2xl shadow-md transition-all flex items-center gap-2">
          <Plus className="w-4 h-4" /> Add Product
        </button>
        <button onClick={() => setShowAddStoreModal(true)} className="px-5 py-2.5 bg-[#111] dark:bg-white text-white dark:text-[#111] font-black text-xs rounded-2xl shadow-md transition-all flex items-center gap-2">
          <Store className="w-4 h-4" /> Enlist Store
        </button>
      </div>
    </div>

    {/* Master Killswitch Banner */}
    <div className={`p-5 rounded-3xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all shadow-sm ${
      marketplaceEnabled 
        ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-950 dark:text-emerald-200" 
        : "bg-red-500/10 border-red-500/30 text-red-950 dark:text-red-200"
    }`}>
      <div className="flex items-center gap-3.5">
        <div className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-sm ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
          <Store className="w-6 h-6" />
        </div>
        <div>
          <div className="flex items-center gap-2.5 flex-wrap">
            <span className="text-sm font-black uppercase tracking-wider">
              App Marketplace & Stores Master Switch:
            </span>
            <span className={`text-xs font-black px-3 py-0.5 rounded-full shadow-sm ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
              {marketplaceEnabled ? "ENABLED (LIVE ON MOBILE APP)" : "DISABLED (HIDDEN ON MOBILE APP)"}
            </span>
          </div>
          <p className="text-xs opacity-90 font-medium mt-1 max-w-2xl">
            {marketplaceEnabled 
              ? "All store catalogs, verified shops carousel, and vendor recruitment cards are currently visible to customers on mobile. Click the switch button anytime to turn off and hide everything marketplace." 
              : "Marketplace is completely hidden on mobile app (no store catalogs, no merchant enrollment, and the mobile dashboard hero button dynamically converts to 'Live Tracking' radar). Click to turn on."}
          </p>
        </div>
      </div>
      <button
        onClick={toggleMarketplace}
        className={`px-6 py-3 rounded-2xl text-xs font-black shrink-0 transition-all shadow-md flex items-center gap-2 hover:scale-[1.02] ${
          marketplaceEnabled 
            ? "bg-red-500 hover:bg-red-600 text-white" 
            : "bg-emerald-500 hover:bg-emerald-600 text-white"
        }`}
      >
        <Power className="w-4 h-4" />
        {marketplaceEnabled ? "Turn OFF Marketplace on App" : "Turn ON Marketplace on App"}
      </button>
    </div>

    {/* Metric Stat Cards */}
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard icon={<Package className="w-4 h-4 text-[#FFB800]" />} label="CATALOG PRODUCTS" value={totalProducts.toString()} sub={`${products.filter(p => p.status === "In Stock").length} in stock · ${products.filter(p => p.status === "Low Stock").length} low stock`} />
      <StatCard icon={<Store className="w-4 h-4 text-[#FFB800]" />} label="ACTIVE VENDOR STORES" value={approvedStores.length.toString()} sub={`${pendingStores.length} pending enlistment requests`} />
      <StatCard icon={<DollarSign className="w-4 h-4 text-[#FFB800]" />} label="MARKETPLACE SALES" value={`₦${totalMarketplaceSales.toLocaleString()}`} sub={`${orders.length} completed transactions`} />
      <StatCard icon={<Percent className="w-4 h-4 text-[#FFB800]" />} label="AVG COMMISSION" value="8.5%" sub="Revenue split per store transaction" />
    </div>

    {/* Sub-tab Navigation & Search Bar */}
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-black/10 dark:border-white/10 pb-4">
      <div className="flex gap-2">
        <button onClick={() => setActiveSubTab("products")} className={"px-5 py-2.5 rounded-2xl text-xs font-extrabold transition-all " + (activeSubTab === "products" ? "bg-[#FFB800] text-[#111] shadow-md" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          📦 Products Catalog ({totalProducts})
        </button>
        <button onClick={() => setActiveSubTab("stores")} className={"px-5 py-2.5 rounded-2xl text-xs font-extrabold transition-all relative " + (activeSubTab === "stores" ? "bg-[#FFB800] text-[#111] shadow-md" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          🏪 Vendor Stores ({stores.length})
          {pendingStores.length > 0 && <span className="ml-2 px-2 py-0.5 bg-red-500 text-white rounded-full text-[10px]">{pendingStores.length}</span>}
        </button>
        <button onClick={() => setActiveSubTab("orders")} className={"px-5 py-2.5 rounded-2xl text-xs font-extrabold transition-all " + (activeSubTab === "orders" ? "bg-[#FFB800] text-[#111] shadow-md" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          🛒 Sales & Cart Orders ({orders.length})
        </button>
        <button onClick={() => setActiveSubTab("payouts")} className={"px-5 py-2.5 rounded-2xl text-xs font-extrabold transition-all relative " + (activeSubTab === "payouts" ? "bg-[#FFB800] text-[#111] shadow-md" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          💳 Payout Requests ({payoutRequests.length})
          {payoutRequests.filter(p => p.status === "PENDING").length > 0 && (
            <span className="ml-2 px-2 py-0.5 bg-red-500 text-white rounded-full text-[10px]">
              {payoutRequests.filter(p => p.status === "PENDING").length}
            </span>
          )}
        </button>
      </div>
      <div className="w-full sm:w-64">
        <SearchInput value={search} onChange={setSearch} placeholder="Search product, category, store..." />
      </div>
    </div>

    {/* Products Catalog View */}
    {activeSubTab === "products" && (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm">
        {filteredProducts.length === 0 ? (
          <div className="text-center py-12 text-gray-600 dark:text-gray-400 font-medium">
            <Package className="w-12 h-12 mx-auto mb-3 text-[#FFB800]/50" />
            <p className="font-extrabold text-base text-gray-900 dark:text-white">No marketplace products found</p>
            <p className="text-xs mt-1 text-gray-600 dark:text-gray-400 font-medium">Click "Add Product" or "Seed Marketplace Data" to populate your inventory.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Product</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Category</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Vendor Store</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Price (₦)</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Stock Qty</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Status</th>
                  <th className="text-right font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {filteredProducts.map(p => (
                  <tr key={p.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-4 flex items-center gap-3">
                      <img src={p.imageUrl || "https://images.unsplash.com/photo-1580674285054-bed31e145f59?w=100"} alt="" className="w-12 h-12 rounded-2xl object-cover border border-black/10 dark:border-white/10 bg-gray-100" />
                      <div>
                        <div className="font-extrabold text-sm text-[#111] dark:text-white">{p.name}</div>
                        <div className="text-[10px] text-gray-600 dark:text-gray-400 font-medium mt-0.5 line-clamp-1">{p.description}</div>
                      </div>
                    </td>
                    <td className="p-4"><span className="px-3 py-1 bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white rounded-full font-bold text-[10px]">{p.category}</span></td>
                    <td className="p-4 font-bold text-gray-800 dark:text-gray-200">{p.vendorStore}</td>
                    <td className="p-4 font-black text-sm text-[#111] dark:text-white">
                      <InlineEdit value={p.price.toString()} onSave={v => handleUpdateProductPrice(p.id, p.name, parseFloat(v) || 0)} type="number" />
                    </td>
                    <td className="p-4 font-black text-sm text-[#111] dark:text-white">
                      <InlineEdit value={p.stock.toString()} onSave={v => handleUpdateProductStock(p.id, p.name, parseInt(v) || 0)} type="number" />
                    </td>
                    <td className="p-4">
                      <span className={"px-3 py-1 rounded-full text-[10px] font-bold " + (p.status === "In Stock" ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : p.status === "Low Stock" ? "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300" : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300")}>
                        {p.status}
                      </span>
                    </td>
                    <td className="p-4 text-right">
                      <button onClick={() => setDeleteProductTarget(p)} className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    )}

    {/* Vendor Stores & Enlistments View */}
    {activeSubTab === "stores" && (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm">
        {filteredStores.length === 0 ? (
          <div className="text-center py-12 text-gray-600 dark:text-gray-400 font-medium">
            <Store className="w-12 h-12 mx-auto mb-3 text-[#FFB800]/50" />
            <p className="font-extrabold text-base text-gray-900 dark:text-white">No vendor stores enlisted yet</p>
            <p className="text-xs mt-1 text-gray-600 dark:text-gray-400 font-medium">Click "Enlist Store" to register vendor store partners.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Store Name</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Owner Details</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Category</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Commission Rate</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Enlistment Date</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Status</th>
                  <th className="text-right font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Approval Controls</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {filteredStores.map(s => (
                  <tr key={s.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-4">
                      <div className="font-black text-sm text-[#111] dark:text-white flex items-center gap-2">
                        {s.storeName}
                        {s.isFeatured && (s.featuredRank ?? 0) > 0 && (
                          <span className="px-2 py-0.5 bg-[#FFB800] text-[#111] rounded-lg font-black text-[9px] flex items-center gap-1">
                            <Star className="w-3 h-3" /> FEATURED #{s.featuredRank}
                          </span>
                        )}
                        {s.isVerified && (
                          <span className="px-2 py-0.5 bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300 rounded-lg font-black text-[9px]">VERIFIED</span>
                        )}
                      </div>
                      {s.isDemo && <div className="text-[10px] text-amber-800 dark:text-amber-400 font-black mt-0.5">DEMO · hidden from customers</div>}
                    </td>
                    <td className="p-4">
                      <div className="font-bold text-[#111] dark:text-white">{s.ownerName}</div>
                      <div className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{s.email} · {s.phone}</div>
                    </td>
                    <td className="p-4"><span className="px-3 py-1 bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white rounded-full font-bold text-[10px]">{s.category}</span></td>
                    <td className="p-4 font-bold text-sm text-[#111] dark:text-white">{s.commissionRate}%</td>
                    <td className="p-4 text-gray-700 dark:text-gray-300 font-medium">{s.dateEnlisted}</td>
                    <td className="p-4">
                      <span className={"px-3 py-1 rounded-full text-[10px] font-bold " + (s.status === "APPROVED" ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : s.status === "PENDING" ? "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300" : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300")}>
                        {s.status}
                      </span>
                    </td>
                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1.5 flex-wrap">
                        {s.status !== "APPROVED" && (
                          <button onClick={() => handleUpdateStoreStatus(s.id, s.storeName, "APPROVED")} className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white rounded-xl font-bold text-[10px] transition-all flex items-center gap-1">
                            <CheckCircle className="w-3.5 h-3.5" /> Approve
                          </button>
                        )}
                        <button onClick={() => handleToggleFeatured(s)} title="Feature / unfeature on mobile carousel" className={"px-3 py-1.5 rounded-xl font-bold text-[10px] transition-all flex items-center gap-1 " + (s.isFeatured && (s.featuredRank ?? 0) > 0 ? "bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111]" : "bg-gray-100 dark:bg-[#222] hover:bg-black/5 dark:hover:bg-white/10 text-gray-700 dark:text-gray-300 font-bold")}>
                          <Star className="w-3.5 h-3.5" /> {s.isFeatured && (s.featuredRank ?? 0) > 0 ? "Unfeature" : "Feature"}
                        </button>
                        <button onClick={() => setEditStoreTarget(s)} className="px-3 py-1.5 bg-white dark:bg-[#222] border border-black/15 dark:border-white/15 hover:bg-black/5 dark:hover:bg-white/10 text-gray-800 dark:text-gray-200 rounded-xl font-bold text-[10px] transition-all flex items-center gap-1">
                          <Pencil className="w-3.5 h-3.5" /> Edit
                        </button>
                        <button onClick={() => setTransferStoreTarget(s)} className="px-3 py-1.5 bg-white dark:bg-[#222] border border-black/15 dark:border-white/15 hover:bg-black/5 dark:hover:bg-white/10 text-gray-800 dark:text-gray-200 rounded-xl font-bold text-[10px] transition-all flex items-center gap-1">
                          <Repeat className="w-3.5 h-3.5" /> Transfer
                        </button>
                        {s.status !== "REJECTED" && (
                          <button onClick={() => handleUpdateStoreStatus(s.id, s.storeName, "REJECTED")} className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-xl font-bold text-[10px] transition-all flex items-center gap-1">
                            <X className="w-3.5 h-3.5" /> Reject
                          </button>
                        )}
                        <button
                          onClick={() => { if (window.confirm(`Permanently remove '${s.storeName}' from the customer marketplace? Products and order history are kept for audit.`)) handleDeleteStore(s); }}
                          className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all" title="Remove store"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    )}

    {/* Sales & Orders Feed */}
    {activeSubTab === "orders" && (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm">
        {orders.length === 0 ? (
          <div className="text-center py-12 text-gray-600 dark:text-gray-400 font-medium">
            <ShoppingBag className="w-12 h-12 mx-auto mb-3 text-[#FFB800]/50" />
            <p className="font-extrabold text-base text-gray-900 dark:text-white">No marketplace transactions yet</p>
            <p className="text-xs mt-1 text-gray-600 dark:text-gray-400 font-medium">Customer purchases from vendor stores will stream live here.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Order #</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Customer</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Store</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Total (₦)</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Status</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {orders.map(o => (
                  <tr key={o.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-4 font-black text-sm text-amber-800 dark:text-[#FFB800]">{o.orderNumber}</td>
                    <td className="p-4 font-bold text-[#111] dark:text-white">{o.customerName}</td>
                    <td className="p-4 font-semibold text-gray-800 dark:text-gray-200">{o.storeName}</td>
                    <td className="p-4 font-black text-sm text-[#111] dark:text-white">₦{o.totalPrice.toLocaleString()}</td>
                    <td className="p-4">
                      <span className="px-3 py-1 bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300 rounded-full font-bold text-[10px]">{o.status}</span>
                    </td>
                    <td className="p-4 text-gray-700 dark:text-gray-300 font-medium">{o.date}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    )}

    {/* Vendor Payout Requests View */}
    {activeSubTab === "payouts" && (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm">
        {payoutRequests.length === 0 ? (
          <div className="text-center py-12 text-gray-600 dark:text-gray-400 font-medium">
            <DollarSign className="w-12 h-12 mx-auto mb-3 text-[#FFB800]/50" />
            <p className="font-extrabold text-base text-gray-900 dark:text-white">No vendor payout requests</p>
            <p className="text-xs mt-1 text-gray-600 dark:text-gray-400 font-medium">Vendor balance withdrawal applications will stream here for review.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Vendor Store</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Amount (₦)</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Bank Details</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Status</th>
                  <th className="text-right font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {payoutRequests.map(p => (
                  <tr key={p.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-4 font-extrabold text-[#111] dark:text-white">{p.storeName}</td>
                    <td className="p-4 font-black text-sm text-gray-900 dark:text-white">₦{p.amount.toLocaleString()}</td>
                    <td className="p-4">
                      <div className="font-bold text-[#111] dark:text-white">{p.bankName}</div>
                      <div className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">{p.accountNumber}</div>
                    </td>
                    <td className="p-4">
                      <span className={`px-3 py-1 rounded-full font-bold text-[10px] ${
                        p.status === "APPROVED"
                          ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300"
                          : p.status === "REJECTED"
                          ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300"
                          : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300"
                      }`}>
                        {p.status}
                      </span>
                    </td>
                    <td className="p-4 text-right">
                      {p.status === "PENDING" && (
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={async () => {
                              try {
                                await updateDoc(doc(db, "vendor_payout_requests", p.id), { status: "APPROVED", processedAt: Timestamp.now() });
                                addLog("Approve Payout", `Approved ₦${p.amount.toLocaleString()} for ${p.storeName}`);
                                addToast("success", `Approved ₦${p.amount.toLocaleString()} payout`);
                              } catch (err: any) { addToast("error", err.message); }
                            }}
                            className="px-3 py-1.5 bg-emerald-500 text-white font-black text-[10px] rounded-xl hover:bg-emerald-600 transition-colors"
                          >
                            Approve
                          </button>
                          <button
                            onClick={async () => {
                              try {
                                await updateDoc(doc(db, "vendor_payout_requests", p.id), { status: "REJECTED", processedAt: Timestamp.now() });
                                const storeRef = doc(db, "marketplace_stores", p.vendorId);
                                await updateDoc(storeRef, { vendorBalance: increment(p.amount) });
                                addLog("Reject Payout", `Rejected ₦${p.amount.toLocaleString()} for ${p.storeName}`);
                                addToast("info", "Payout rejected and funds returned to vendor");
                              } catch (err: any) { addToast("error", err.message); }
                            }}
                            className="px-3 py-1.5 bg-red-500/10 text-red-700 dark:text-red-400 border border-red-500/30 font-bold text-[10px] rounded-xl hover:bg-red-500/20 transition-colors"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    )}

    {/* Add Product Modal */}
    {showAddModal && (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
        <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-lg shadow-2xl border border-black/10 dark:border-white/10 space-y-4 animate-scale-in">
          <div className="flex items-center justify-between pb-2 border-b border-black/10 dark:border-white/10">
            <h3 className="text-lg font-black text-[#111] dark:text-white flex items-center gap-2">
              <Plus className="w-5 h-5 text-[#FFB800]" /> Add New Marketplace Product
            </h3>
            <button onClick={() => setShowAddModal(false)} className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-colors">
              <X className="w-5 h-5" />
            </button>
          </div>
          <form onSubmit={handleAddProduct} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Product Title</label>
              <input type="text" value={pName} onChange={e => setPName(e.target.value)} placeholder="e.g. Heavy Duty Bike Delivery Box" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Category</label>
                <Select value={pCategory} onChange={setPCategory} options={[
                  { value: "Delivery Gear", label: "Delivery Gear" },
                  { value: "Apparel", label: "Apparel & Uniforms" },
                  { value: "Lubricants", label: "Oil & Lubricants" },
                  { value: "Accessories", label: "Electronics & Accessories" },
                  { value: "Safety", label: "Helmets & Safety" },
                ]} className="w-full" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Price (₦)</label>
                <input type="number" value={pPrice} onChange={e => setPPrice(e.target.value)} placeholder="35000" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Stock Quantity</label>
                <input type="number" value={pStock} onChange={e => setPStock(e.target.value)} placeholder="50" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Vendor Store</label>
                <input type="text" value={pVendor} onChange={e => setPVendor(e.target.value)} placeholder="ESDispatch Fleet Supplies" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Image URL</label>
              <input type="url" value={pImg} onChange={e => setPImg(e.target.value)} placeholder="https://..." className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Description</label>
              <textarea value={pDesc} onChange={e => setPDesc(e.target.value)} placeholder="Product specification..." className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40 h-20" />
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowAddModal(false)} className="px-4 py-2.5 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors rounded-xl text-xs font-bold">Cancel</button>
              <button type="submit" disabled={savingProduct} className="px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black rounded-xl text-xs shadow-md">
                {savingProduct ? "Saving..." : "Create Product"}
              </button>
            </div>
          </form>
        </div>
      </div>
    )}

    {/* Enlist Store Modal */}
    {showAddStoreModal && (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
        <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-lg shadow-2xl border border-black/10 dark:border-white/10 space-y-4 animate-scale-in">
          <div className="flex items-center justify-between pb-2 border-b border-black/10 dark:border-white/10">
            <h3 className="text-lg font-black text-[#111] dark:text-white flex items-center gap-2">
              <Store className="w-5 h-5 text-[#FFB800]" /> Enlist Vendor Store Partner
            </h3>
            <button onClick={() => setShowAddStoreModal(false)} className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-colors">
              <X className="w-5 h-5" />
            </button>
          </div>
          <form onSubmit={handleAddStore} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Store Name</label>
              <input type="text" value={sName} onChange={e => setSName(e.target.value)} placeholder="e.g. Benin Auto Care" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Owner Name</label>
                <input type="text" value={sOwner} onChange={e => setSOwner(e.target.value)} placeholder="Bisi Adebayo" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input type="email" value={sEmail} onChange={e => setSEmail(e.target.value)} placeholder="bisi@autocare.ng" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Phone Number</label>
                <input type="text" value={sPhone} onChange={e => setSPhone(e.target.value)} placeholder="08055544433" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Commission Rate (%)</label>
                <input type="number" value={sComm} onChange={e => setSComm(e.target.value)} placeholder="10" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Store Description (shown on storefront)</label>
              <textarea value={sDesc} onChange={e => setSDesc(e.target.value)} rows={2} placeholder="Premium spare parts and delivery accessories in Benin City…" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Store Address (full detail, shown on storefront)</label>
              <input type="text" value={sAddr} onChange={e => setSAddr(e.target.value)} placeholder="17 Upper Adesuwa Road, GRA, Benin City" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Logo URL (optional)</label>
              <input type="text" value={sLogo} onChange={e => setSLogo(e.target.value)} placeholder="https://.../logo.png" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Cover Photo URL (optional)</label>
              <input type="text" value={sCover} onChange={e => setSCover(e.target.value)} placeholder="https://.../cover.jpg" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
            </div>
            <div className="bg-[#FFB800]/10 rounded-2xl p-3 border border-[#FFB800]/30">
              <p className="text-[10px] font-black text-amber-800 dark:text-[#FFB800] uppercase tracking-wider">Owner Account</p>
              <p className="text-xs text-gray-800 dark:text-gray-200 mt-1 font-medium">If the email matches an existing user, they are upgraded to Vendor instantly. Otherwise a vendor user record is created automatically.</p>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowAddStoreModal(false)} className="px-4 py-2.5 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors rounded-xl text-xs font-bold">Cancel</button>
              <button type="submit" disabled={savingStore} className="px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black rounded-xl text-xs shadow-md">
                {savingStore ? "Enlisting..." : "Enlist & Approve Store"}
              </button>
            </div>
          </form>
        </div>
      </div>
    )}

    {/* Edit Store Modal */}
    {editStoreTarget && (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
        <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-lg shadow-2xl border border-black/10 dark:border-white/10 space-y-4 animate-scale-in">
          <div className="flex items-center justify-between pb-2 border-b border-black/10 dark:border-white/10">
            <h3 className="text-lg font-black text-[#111] dark:text-white flex items-center gap-2">
              <Pencil className="w-5 h-5 text-[#FFB800]" /> Edit Store · {editStoreTarget.storeName}
            </h3>
            <button onClick={() => setEditStoreTarget(null)} className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-colors">
              <X className="w-5 h-5" />
            </button>
          </div>
          <form
            onSubmit={e => { e.preventDefault(); handleEditStore(editStoreTarget); }}
            className="space-y-3"
          >
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Store Name</label>
              <input type="text" value={editStoreTarget.storeName} onChange={e => setEditStoreTarget({ ...editStoreTarget, storeName: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" required />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Owner Name</label>
                <input type="text" value={editStoreTarget.ownerName} onChange={e => setEditStoreTarget({ ...editStoreTarget, ownerName: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Category</label>
                <input type="text" value={editStoreTarget.category} onChange={e => setEditStoreTarget({ ...editStoreTarget, category: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input type="email" value={editStoreTarget.email} onChange={e => setEditStoreTarget({ ...editStoreTarget, email: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Phone</label>
                <input type="text" value={editStoreTarget.phone || ""} onChange={e => setEditStoreTarget({ ...editStoreTarget, phone: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Commission Rate (%)</label>
                <input type="number" value={editStoreTarget.commissionRate} onChange={e => setEditStoreTarget({ ...editStoreTarget, commissionRate: parseFloat(e.target.value) || 0 })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Featured Rank (1–10, 0 = off)</label>
                <input type="number" min={0} max={10} value={editStoreTarget.isFeatured ? (editStoreTarget.featuredRank || 1) : 0} onChange={e => { const r = parseInt(e.target.value) || 0; setEditStoreTarget({ ...editStoreTarget, featuredRank: r, isFeatured: r > 0 }); }} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Description</label>
              <textarea value={editStoreTarget.description || ""} onChange={e => setEditStoreTarget({ ...editStoreTarget, description: e.target.value })} rows={2} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Address</label>
              <input type="text" value={editStoreTarget.address || ""} onChange={e => setEditStoreTarget({ ...editStoreTarget, address: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Logo URL</label>
                <input type="text" value={editStoreTarget.logoUrl || ""} onChange={e => setEditStoreTarget({ ...editStoreTarget, logoUrl: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Cover URL</label>
                <input type="text" value={editStoreTarget.coverUrl || ""} onChange={e => setEditStoreTarget({ ...editStoreTarget, coverUrl: e.target.value })} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white" />
              </div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setEditStoreTarget(null)} className="px-4 py-2.5 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors rounded-xl text-xs font-bold">Cancel</button>
              <button type="submit" disabled={savingStoreOp} className="px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black rounded-xl text-xs shadow-md">
                {savingStoreOp ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </form>
        </div>
      </div>
    )}

    {/* Transfer Store Ownership Modal */}
    {transferStoreTarget && (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
        <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-md shadow-2xl border border-black/10 dark:border-white/10 space-y-4 animate-scale-in">
          <div className="flex items-center justify-between pb-2 border-b border-black/10 dark:border-white/10">
            <h3 className="text-lg font-black text-[#111] dark:text-white flex items-center gap-2">
              <Repeat className="w-5 h-5 text-[#FFB800]" /> Transfer Ownership
            </h3>
            <button onClick={() => setTransferStoreTarget(null)} className="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-colors">
              <X className="w-5 h-5" />
            </button>
          </div>
          <p className="text-xs text-gray-700 dark:text-gray-300 font-medium">
            Transfer <b className="text-[#111] dark:text-white">{transferStoreTarget.storeName}</b> from <b className="text-[#111] dark:text-white">{transferStoreTarget.ownerName}</b> to another registered user. All products listed under this store are reassigned to the new owner.
          </p>
          <div>
            <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">New Owner's Registered Email</label>
            <input type="email" value={transferEmail} onChange={e => setTransferEmail(e.target.value)} placeholder="owner@company.com" className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-4 py-2.5 text-xs text-[#111] dark:text-white focus:ring-2 focus:ring-[#FFB800]/40" />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setTransferStoreTarget(null)} className="px-4 py-2.5 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors rounded-xl text-xs font-bold">Cancel</button>
            <button onClick={() => handleTransferStore(transferStoreTarget, transferEmail)} disabled={savingStoreOp} className="px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black rounded-xl text-xs shadow-md">
              {savingStoreOp ? "Transferring..." : "Transfer Store"}
            </button>
          </div>
        </div>
      </div>
    )}

    {/* Delete Confirmation Modal */}
    <ConfirmModal show={!!deleteProductTarget} title="Delete Marketplace Product" message={`Are you sure you want to delete '${deleteProductTarget?.name}'?`} confirmLabel="Delete Product" onConfirm={() => deleteProductTarget && handleDeleteProduct(deleteProductTarget)} onCancel={() => setDeleteProductTarget(null)} />
  </div>;
}

interface SupportTicket {
  id: string;
  ticketId: string;
  userId: string;
  userName: string;
  lastMessage: string;
  lastUpdated: number;
  status: string;
}

interface SupportChatMessage {
  id: string;
  senderId: string;
  senderName: string;
  senderRole: string;
  messageText: string;
  timestamp: number;
}

function SupportTab({ db, addLog, addToast }: { db: any; addLog: any; addToast: any }) {
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [messages, setMessages] = useState<SupportChatMessage[]>([]);
  const [replyText, setReplyText] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!db) return;
    const unsub = onSnapshot(collection(db, "support_chats"), (snapshot) => {
      const list: SupportTicket[] = [];
      snapshot.forEach((doc) => {
        const d = doc.data();
        list.push({
          id: doc.id,
          ticketId: d.ticketId || doc.id,
          userId: d.userId || "",
          userName: d.userName || "Customer",
          lastMessage: d.lastMessage || "",
          lastUpdated: d.lastUpdated || 0,
          status: d.status || "OPEN"
        });
      });
      list.sort((a, b) => b.lastUpdated - a.lastUpdated);
      setTickets(list);
      setLoading(false);
      if (!selectedTicket && list.length > 0) {
        setSelectedTicket(list[0]);
      }
    });
    return () => unsub();
  }, [db]);

  useEffect(() => {
    if (!db || !selectedTicket) return;
    const unsub = onSnapshot(
      collection(db, "support_chats", selectedTicket.id, "messages"),
      (snapshot) => {
        const msgs: SupportChatMessage[] = [];
        snapshot.forEach((doc) => {
          const d = doc.data();
          msgs.push({
            id: doc.id,
            senderId: d.senderId || "",
            senderName: d.senderName || "",
            senderRole: d.senderRole || "customer",
            messageText: d.messageText || "",
            timestamp: d.timestamp || 0
          });
        });
        msgs.sort((a, b) => a.timestamp - b.timestamp);
        setMessages(msgs);
      }
    );
    return () => unsub();
  }, [db, selectedTicket]);

  const handleSendReply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyText.trim() || !selectedTicket || !db) return;
    const text = replyText.trim();
    setReplyText("");
    try {
      const msgId = "MSG-" + Date.now();
      const now = Date.now();
      const msgData = {
        id: msgId,
        senderId: "ADMIN_HQ",
        senderName: "HQ Dispatcher",
        senderRole: "dispatcher",
        messageText: text,
        timestamp: now
      };
      await setDoc(doc(db, "support_chats", selectedTicket.id, "messages", msgId), msgData);
      await updateDoc(doc(db, "support_chats", selectedTicket.id), {
        lastMessage: text,
        lastUpdated: now
      });
      addLog("Support Reply", `Replied to ticket ${selectedTicket.ticketId}`);
    } catch (err: any) {
      addToast("error", err.message || "Failed to send reply");
    }
  };

  return (
    <div className="tab-content space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-black text-[#111] dark:text-white flex items-center gap-3">
            <Headphones className="text-[#FFB800]" size={28} />
            Live Customer Support & Dispatch Chat
          </h2>
          <p className="text-xs font-medium text-gray-600 dark:text-gray-400 mt-1">
            Real-time two-way communication channel between customers and central headquarters.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[650px]">
        <div className="bg-[#f9f9f9] dark:bg-[#141414] rounded-3xl p-4 border border-black/10 dark:border-white/10 flex flex-col overflow-hidden">
          <h3 className="font-bold text-sm text-[#111] dark:text-white mb-3 px-2">Support Conversations ({tickets.length})</h3>
          <div className="flex-1 overflow-y-auto space-y-2 pr-1">
            {loading ? (
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium p-3">Loading tickets...</p>
            ) : tickets.length === 0 ? (
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium p-3">No support conversations yet.</p>
            ) : (
              tickets.map((t) => (
                <button
                  key={t.id}
                  onClick={() => setSelectedTicket(t)}
                  className={`w-full text-left p-3.5 rounded-2xl transition-all border ${
                    selectedTicket?.id === t.id
                      ? "bg-[#FFB800] text-[#111] border-[#FFB800] shadow-md"
                      : "bg-white dark:bg-[#1c1c1c] text-[#111] dark:text-white border-black/10 dark:border-white/10 hover:border-[#FFB800]/50"
                  }`}
                >
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-bold text-xs">{t.userName}</span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      selectedTicket?.id === t.id 
                        ? "bg-black text-[#FFB800]" 
                        : "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300"
                    }`}>
                      {t.status}
                    </span>
                  </div>
                  <p className={`text-xs truncate ${selectedTicket?.id === t.id ? "text-gray-900 font-medium" : "text-gray-600 dark:text-gray-400 font-medium"}`}>
                    {t.lastMessage || "No messages yet"}
                  </p>
                </button>
              ))
            )}
          </div>
        </div>

        <div className="lg:col-span-2 bg-[#f9f9f9] dark:bg-[#141414] rounded-3xl p-5 border border-black/10 dark:border-white/10 flex flex-col overflow-hidden">
          {selectedTicket ? (
            <>
              <div className="pb-3 border-b border-black/10 dark:border-white/10 flex items-center justify-between">
                <div>
                  <h4 className="font-black text-sm text-[#111] dark:text-white">{selectedTicket.userName}</h4>
                  <p className="text-[11px] text-gray-600 dark:text-gray-400 font-medium">Ticket: {selectedTicket.ticketId} • User ID: {selectedTicket.userId}</p>
                </div>
                <span className="text-[11px] font-bold text-emerald-800 dark:text-emerald-300 bg-emerald-500/15 px-2.5 py-1 rounded-full flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  Live Connected
                </span>
              </div>

              <div className="flex-1 overflow-y-auto py-4 space-y-3 pr-2">
                {messages.length === 0 ? (
                  <div className="text-center py-12 text-gray-600 dark:text-gray-400 font-medium text-xs">
                    No message history in this ticket.
                  </div>
                ) : (
                  messages.map((m) => {
                    const isStaff = m.senderRole === "dispatcher" || m.senderRole === "admin";
                    return (
                      <div key={m.id} className={`flex ${isStaff ? "justify-end" : "justify-start"}`}>
                        <div className={`max-w-[75%] rounded-2xl p-3.5 text-xs ${
                          isStaff
                            ? "bg-[#FFB800] text-[#111] font-semibold"
                            : "bg-white dark:bg-[#242424] text-[#111] dark:text-white border border-black/10 dark:border-white/10 font-medium"
                        }`}>
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-bold text-[10px] text-gray-800 dark:text-gray-200">
                              {isStaff ? "HQ Dispatcher" : selectedTicket.userName}
                            </span>
                            <span className="text-[9px] text-gray-600 dark:text-gray-400 font-medium">
                              {m.timestamp ? new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ""}
                            </span>
                          </div>
                          <p>{m.messageText}</p>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              <form onSubmit={handleSendReply} className="pt-3 border-t border-black/10 dark:border-white/10 flex gap-2">
                <input
                  type="text"
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  placeholder={`Reply to ${selectedTicket.userName}...`}
                  className="flex-1 bg-white dark:bg-[#202020] text-[#111] dark:text-white px-4 py-3 rounded-2xl text-xs outline-none border border-black/10 dark:border-white/10 focus:border-[#FFB800]"
                />
                <button
                  type="submit"
                  disabled={!replyText.trim()}
                  className="bg-[#FFB800] text-[#111] px-5 py-3 rounded-2xl font-black text-xs flex items-center gap-2 hover:bg-[#FFB800]/90 disabled:opacity-50 transition-all shadow-md"
                >
                  <Send size={14} />
                  Send
                </button>
              </form>
            </>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-6">
              <Headphones size={48} className="text-gray-400 dark:text-gray-600 mb-3" />
              <p className="font-bold text-sm text-[#111] dark:text-white">No Support Conversation Selected</p>
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">Select a ticket from the left panel to begin live chat.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
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
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><FileText className="w-5 h-5 text-[#FFB800]" /> Audit Log</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{filtered.length} entries (page {lPage + 1}/{lTotalPages})</p></div>
      <Select value={typeFilter} onChange={setTypeFilter} options={[{value:"all",label:"All Actions"},{value:"Create",label:"Create"},{value:"Update",label:"Update"},{value:"Delete",label:"Delete"},{value:"Toggle",label:"Toggle"},{value:"Login",label:"Login"}]} className="w-36" />
    </div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-xs"><thead className="bg-gray-50 dark:bg-[#222]">
          <tr><th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-3 border-b border-black/10 dark:border-white/10">Action</th>
            <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-3 border-b border-black/10 dark:border-white/10 hidden md:table-cell">Detail</th>
            <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-3 border-b border-black/10 dark:border-white/10 hidden lg:table-cell">Admin</th>
            <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-3 border-b border-black/10 dark:border-white/10">Date</th></tr>
        </thead><tbody className="divide-y divide-black/5 dark:divide-white/10">
          {pagedLogs.length === 0 && <tr><td colSpan={4} className="p-8 text-center text-gray-600 dark:text-gray-400 font-medium">No entries.</td></tr>}
          {pagedLogs.map(l => <tr key={l.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
            <td className="p-3"><span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + (l.action.startsWith("Create") ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300" : l.action.startsWith("Delete") ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300" : l.action.startsWith("Update") ? "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300" : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300")}>{l.action}</span></td>
            <td className="p-3 hidden md:table-cell"><span className="text-[#111] dark:text-white font-semibold">{l.details}</span></td>
            <td className="p-3 hidden lg:table-cell"><span className="text-gray-700 dark:text-gray-300 font-medium">{l.admin}</span></td>
            <td className="p-3"><span className="text-gray-600 dark:text-gray-400 font-medium text-[10px]">{new Date(l.timestamp).toLocaleDateString()}</span></td>
          </tr>)}
        </tbody></table>
      </div>
    </div>
    {lTotalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
      <button onClick={() => setLPage(p => Math.max(0, p - 1))} disabled={lPage === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronLeft size={14} /></button>
      {Array.from({ length: lTotalPages }, (_, i) => <button key={i} onClick={() => setLPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold " + (i === lPage ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
      <button onClick={() => setLPage(p => Math.min(lTotalPages - 1, p + 1))} disabled={lPage >= lTotalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronRight size={14} /></button>
    </div>}
  </div>;
}



function BannersTab({ banners, db, addLog, addToast }: { banners: Banner[]; db: any; addLog: any; addToast: any }) {
  const [showAdd, setShowAdd] = useState(false);
  const [title, setTitle] = useState("");
  const [subtitle, setSubtitle] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [saving, setSaving] = useState(false);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !imageUrl) { addToast("error", "Title and image URL are required"); return; }
    setSaving(true);
    try {
      await addDoc(collection(db, "banners"), { title, subtitle, imageUrl, active: true, order: banners.length + 1, createdAt: Timestamp.now() });
      addLog("Add Banner", `Added hero slide '${title}'`);
      addToast("success", "Hero slide added successfully");
      setShowAdd(false); setTitle(""); setSubtitle(""); setImageUrl("");
    } catch (err: any) { addToast("error", "Failed to add hero slide: " + err.message); }
    setSaving(false);
  };

  const toggleActive = async (id: string, current: boolean) => {
    await updateDoc(doc(db, "banners", id), { active: !current, updatedAt: Timestamp.now() });
    addToast("success", "Banner status updated");
  };

  const deleteBanner = async (id: string) => {
    await deleteDoc(doc(db, "banners", id));
    addToast("success", "Banner removed");
  };

  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div>
        <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><ImageIcon className="w-5 h-5 text-[#FFB800]" /> Hero Banners & Slides</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">Manage promotional banners displayed on mobile app hero carousel</p>
      </div>
      <button onClick={() => setShowAdd(true)} className="px-4 py-2.5 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm hover:bg-[#FFB800]/80"><Plus size={16} /> Add Slide</button>
    </div>

    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {banners.length === 0 ? (
        <div className="col-span-full bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 text-center text-gray-600 dark:text-gray-400 font-medium">No hero banners created yet.</div>
      ) : (
        banners.map(b => (
          <div key={b.id} className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl overflow-hidden shadow-sm flex flex-col justify-between">
            <div className="h-36 bg-gray-100 dark:bg-[#222] relative">
              <img src={b.imageUrl} alt={b.title} className="w-full h-full object-cover" />
              <span className={"absolute top-3 right-3 px-2 py-0.5 rounded-full text-[10px] font-black " + (b.active ? "bg-emerald-500 text-white" : "bg-gray-500 text-white")}>{b.active ? "ACTIVE" : "INACTIVE"}</span>
            </div>
            <div className="p-4 space-y-2">
              <p className="font-bold text-xs text-[#111] dark:text-white">{b.title}</p>
              <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium leading-relaxed">{b.subtitle || "No subtitle"}</p>
              <div className="flex items-center justify-between pt-2 border-t border-black/5 dark:border-white/5">
                <button onClick={() => toggleActive(b.id, b.active)} className="text-[10px] font-black text-amber-800 dark:text-[#FFB800] hover:underline">{b.active ? "Disable" : "Enable"}</button>
                <button onClick={() => deleteBanner(b.id)} className="text-[10px] font-bold text-red-600 dark:text-red-400 hover:underline">Delete</button>
              </div>
            </div>
          </div>
        ))
      )}
    </div>

    {showAdd && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setShowAdd(false)}>
        <form onSubmit={handleAdd} className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
          <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><ImageIcon className="w-4 h-4 text-[#FFB800]" /> New Hero Slide</h3>
          <div className="space-y-3 text-xs">
            <div><label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Title</label>
              <input value={title} onChange={e => setTitle(e.target.value)} required className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
            <div><label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Subtitle</label>
              <input value={subtitle} onChange={e => setSubtitle(e.target.value)} className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
            <div><label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Image URL</label>
              <input value={imageUrl} onChange={e => setImageUrl(e.target.value)} required placeholder="https://..." className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-[#111] dark:text-white" /></div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors text-xs font-bold rounded-xl">Cancel</button>
            <button type="submit" disabled={saving} className="px-4 py-2 bg-[#FFB800] text-[#111] text-xs font-black rounded-xl hover:bg-[#FFB800]/80">{saving ? "Saving..." : "Add Slide"}</button>
          </div>
        </form>
      </div>
    )}
  </div>;
}

function TrackingTab({ deliveries, drivers }: { deliveries: Delivery[]; drivers: UserProfile[] }) {
  const [trackSearch, setTrackSearch] = useState("");
  const [tSelectedId, setTSelectedId] = useState<string | null>(null);
  const activeD = deliveries.filter(d => d.status !== "DELIVERED" && d.status !== "CANCELLED");
  const filtered = trackSearch ? activeD.filter(d => d.id.includes(trackSearch) || d.receiverName.toLowerCase().includes(trackSearch.toLowerCase()) || d.itemName?.toLowerCase().includes(trackSearch.toLowerCase())) : activeD;
  const [tPage, setTPage] = useState(0);
  const tPerPage = 10;
  const tTotalPages = Math.max(1, Math.ceil(filtered.length / tPerPage));
  const pagedT = filtered.slice(tPage * tPerPage, (tPage + 1) * tPerPage);
  useEffect(() => { setTPage(0); }, [trackSearch]);
  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><MapPin className="w-5 h-5 text-[#FFB800]" /> Live Tracking</h1><p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">{activeD.length} active deliveries, {drivers.filter(d => d.lat && d.lng).length} riders on map</p></div>
      <SearchInput value={trackSearch} onChange={setTrackSearch} placeholder="Search by ID, name, item..." />
    </div>
    <div className="h-[400px] rounded-3xl overflow-hidden border border-gray-200 dark:border-white/10 shadow-sm">
      <LiveTrackingMap deliveries={filtered} drivers={drivers} selectedId={tSelectedId} onSelect={setTSelectedId} />
    </div>
    <div className="grid gap-4">
      {pagedT.length === 0 && <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-8 text-center"><p className="text-sm text-gray-600 dark:text-gray-400 font-medium">No active deliveries.</p></div>}
      {pagedT.map(d => {
        const step = sIdx[d.status] || 0;
        return <div key={d.id} onClick={() => setTSelectedId(tSelectedId === d.id ? null : d.id)} className={"bg-white dark:bg-[#1a1a1a] border rounded-3xl p-5 shadow-sm animate-fade-in cursor-pointer transition-all " + (tSelectedId === d.id ? "border-[#FFB800] ring-2 ring-[#FFB800]/30" : "border-gray-200 dark:border-white/10 hover:border-[#FFB800]/50")}>
          <div className="flex items-start justify-between mb-4 flex-wrap gap-2">
            <div><p className="font-bold text-[#111] dark:text-white">{d.itemName || "Parcel"}</p>
              <p className="text-[10px] text-gray-600 dark:text-gray-400 font-medium">#{idShort(d.id)} • {d.receiverName} → {d.deliveryAddress}</p>
              {d.courierName && <p className="text-[10px] text-amber-800 dark:text-[#FFB800] mt-0.5 font-bold">{d.courierName}</p>}</div>
            <span className={"text-[10px] font-bold px-2 py-0.5 rounded-full " + sStyle(d.status)}>{d.status.replace(/_/g, " ")}</span>
          </div>
          <div className="relative mt-4 mb-2">
            <div className="flex items-center justify-between mb-2">
              {statusSteps.map((label, i) => <div key={label} className="flex flex-col items-center gap-1 relative z-10">
                <div className={"w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold " + (i <= step ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-gray-500 dark:text-gray-400 font-bold")}>
                  {i < step ? <Check size={12} /> : i + 1}
                </div>
                <span className={"text-[8px] font-semibold whitespace-nowrap " + (i <= step ? "text-[#111] dark:text-white" : "text-gray-600 dark:text-gray-400")}>{label}</span>
              </div>)}
            </div>
            <div className="absolute top-3 left-3 right-3 h-0.5 bg-gray-200 dark:bg-[#222] rounded-full">
              <div className="h-full bg-[#FFB800] rounded-full transition-all duration-500" style={{ width: `${(step / Math.max(1, statusSteps.length - 1)) * 100}%` }} />
            </div>
          </div>
        </div>;
      })}
    </div>
    {tTotalPages > 1 && <div className="flex items-center justify-center gap-2 pt-2">
      <button onClick={() => setTPage(p => Math.max(0, p - 1))} disabled={tPage === 0} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-40 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronLeft size={14} /></button>
      {Array.from({ length: tTotalPages }, (_, i) => <button key={i} onClick={() => setTPage(i)} className={"w-8 h-8 rounded-xl text-xs font-bold " + (i === tPage ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-[#222] text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-[#333]")}>{i + 1}</button>)}
      <button onClick={() => setTPage(p => Math.min(tTotalPages - 1, p + 1))} disabled={tPage >= tTotalPages - 1} className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-40 hover:bg-gray-200 dark:hover:bg-[#333]"><ChevronRight size={14} /></button>
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
        {newOrderAlert && (
          <div className="mb-6 bg-gradient-to-r from-amber-500/15 via-[#FFB800]/20 to-amber-500/10 border border-[#FFB800]/50 rounded-3xl p-4 flex items-center justify-between gap-4 animate-fade-in shadow-lg">
            <div className="flex items-center gap-3 min-w-0">
              <div className="w-10 h-10 rounded-2xl bg-[#FFB800] text-[#111] flex items-center justify-center font-black animate-pulse shrink-0">
                <Bell size={20} />
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-[10px] font-black uppercase tracking-wider text-amber-900 dark:text-[#FFB800] bg-amber-200/80 dark:bg-black/60 px-2.5 py-0.5 rounded-full border border-amber-300 dark:border-[#FFB800]/30">New Booking Received</span>
                  <span className="text-xs text-gray-700 dark:text-gray-300 font-mono font-bold">#{idShort(newOrderAlert.id)}</span>
                </div>
                <p className="text-xs sm:text-sm font-bold text-[#111] dark:text-white mt-1 truncate">
                  {newOrderAlert.itemName} • Deliver to <span className="text-amber-900 dark:text-[#FFB800] font-black">{newOrderAlert.receiverName}</span> ({newOrderAlert.deliveryAddress})
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              <button
                onClick={() => {
                  setShipmentsFilterPrefill({ search: newOrderAlert.id, selectedId: newOrderAlert.id, status: "ALL", category: "ALL" });
                  setTab("shipments");
                  setNewOrderAlert(null);
                }}
                className="px-4 py-2 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm hover:bg-[#FFB800]/80 transition-all cursor-pointer whitespace-nowrap"
              >
                <Truck size={15} /> Review & Assign Rider
              </button>
              <button
                onClick={() => setNewOrderAlert(null)}
                className="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white rounded-xl cursor-pointer transition-colors"
                title="Dismiss"
              >
                <X size={16} />
              </button>
            </div>
          </div>
        )}
        {tab === "marketplace" && <MarketplaceTab products={products} stores={stores} orders={marketplaceOrders} payoutRequests={payoutRequests} db={db} addLog={addLog} addToast={addToast} seedMarketplace={seedMarketplaceWrapper} marketplaceEnabled={marketplaceEnabled} toggleMarketplace={toggleMarketplace} />}
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
            marketplaceEnabled={marketplaceEnabled}
            toggleMarketplace={toggleMarketplace}
            setShipmentsFilterPrefill={setShipmentsFilterPrefill}
            onOpenShipmentFullView={(shipmentId: string) => {
              setShipmentsFilterPrefill({ search: shipmentId, selectedId: shipmentId, status: "ALL", category: "ALL" });
              setTab("shipments");
            }}
          />}
        {tab === "users" && <UsersTab activeUsers={activeUsers} searchQuery={searchQuery} db={db} addLog={addLog} addToast={addToast} createNotification={createNotification} />}
        {tab === "shipments" && <ShipmentsTab deliveries={deliveries} drivers={drivers} searchQuery={searchQuery} db={db} addLog={addLog} addToast={addToast} filterPrefill={shipmentsFilterPrefill} setFilterPrefill={setShipmentsFilterPrefill} />}
        {tab === "tracking" && <TrackingTab deliveries={deliveries} drivers={drivers} />}
        {tab === "banners" && <BannersTab banners={banners} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "referrals" && <ReferralsTab referrals={referrals} completedReferrals={completedReferrals} searchQuery={searchQuery} />}
        {tab === "promotions" && <PromotionsTab promotions={promotions} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "appcards" && <AppCardsTab appContent={appContent} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "settings" && <SettingsTab db={db} addLog={addLog} />}
        {tab === "cms" && <CMSTab db={db} addLog={addLog} />}
        {tab === "support" && <SupportTab db={db} addLog={addLog} addToast={addToast} />}
        {tab === "logs" && <LogsTab logs={logs} />}
      </div>
    </main>
    </div>;
  }

export default function AdminDashboardWrapper() {
  return <AdminDashboardPage />;
}

