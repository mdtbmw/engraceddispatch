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
import { collection, query, onSnapshot, doc, updateDoc, setDoc, deleteDoc, where, Timestamp, getDoc, getDocs, writeBatch, addDoc, increment, limit, orderBy } from "firebase/firestore";
import { Download, Shield, Truck, Package, ShoppingBag, Store, Users, User, Settings, Activity, Lock, Mail, Key, CheckCircle, CheckCircle2, AlertTriangle, Plus, Trash2, LogOut, Search, Sliders, Award, DollarSign, Zap, Globe, UserPlus, BarChart3, MapPin, ShieldAlert, Image as ImageIcon, Menu, X, ShieldCheck, RefreshCw, UserCheck, UserX, Clock, TrendingUp, Edit3, Copy, Check, Percent, Gift, Star, Layers, Eye, EyeOff, Calendar, ChevronDown, ChevronUp, Phone, AtSign, Hash, Save, Bell, Send, ChevronLeft, ChevronRight, Bookmark, Folder, FileCheck, MessageSquare, Headphones, Settings2, LayoutGrid, FileText, Moon, Sun, Pencil, Repeat, Printer, Power, Wrench, Database, Tag, Radio } from "lucide-react";
import CMSTab from "./CMSTab";
import LiveTrackingMap from "./LiveTrackingMap";
import BroadcastNewsTab from "./BroadcastNewsTab";
import { SoundEngine } from "@/lib/interaction/SoundEngine";
import { StatusBadge } from "@/components/design-system/StatusBadge";
import { RouteDisplay } from "@/components/design-system/RouteDisplay";
import { PriceDisplay } from "@/components/design-system/PriceDisplay";
import { DispatchDecisionDrawer } from "@/components/design-system/DispatchDecisionDrawer";
import { NotificationLifecycleManager } from "@/components/design-system/NotificationLifecycle";
import { ShipmentMicroPage } from "@/components/design-system/ShipmentMicroPage";
type TabId = "dashboard" | "marketplace" | "users" | "shipments" | "tracking" | "broadcast" | "banners" | "referrals" | "promotions" | "appcards" | "settings" | "logs" | "cms" | "support";
interface UserProfile { id: string; uid: string; name: string; email: string; phone: string; role: string; status: string; isOnline: boolean; rating: number; deliveryCount: number; walletBalance: number; loyaltyPoints: number; photoUrl: string; bikeNumber?: string; staffId?: string; lat?: number; lng?: number; isDeleted?: boolean; updatedAt?: any; lastSeen?: any; }
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
  additionalStops?: string;
  otpVerified?: boolean;
  adminOverrideReason?: string;
  adminOverrideBy?: string;
  adminOverrideAt?: any;
}
interface Banner { id: string; title: string; subtitle: string; imageUrl: string; interval: number; order: number; active: boolean; }
interface Referral { id: string; referrerId: string; referrerName: string; referrerEmail: string; refereeId: string; refereeName: string; refereeEmail: string; rewardAmount: number; status: string; }
interface Promotion { id: string; title: string; description: string; discountType: string; discountValue: number; discountDisplay: string; minOrderAmount: number; maxDiscount: number; code: string; usageLimit: number; usedCount: number; active: boolean; }
interface AuditEntry { id: string; time: string; action: string; details: string; admin: string; timestamp: number; staffId?: string; adminEmail?: string; category?: string; }

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
  addLog: (a: string, d: string, c?: string) => Promise<void> | void;
  addToast?: (t: Toast["type"], m: string) => void;
  activeUsers?: UserProfile[];
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
  return <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-4 sm:p-5 shadow-2xs hover:shadow-md transition-all duration-200">
    <div className="flex items-center justify-between"><span className="text-[11px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider">{label}</span>{icon}</div>
    <h2 className="text-2xl sm:text-3xl font-black text-gray-900 dark:text-white mt-1.5 tracking-tight">{value}</h2>
    <p className="text-[11px] text-gray-600 dark:text-gray-400 font-semibold mt-0.5 truncate">{sub}</p>
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
  return <button onClick={onClick} disabled={loading} className={"p-3.5 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-2xl text-left transition-all " + borderClr + " " + (loading ? "opacity-50 cursor-not-allowed" : "")}>
    <p className={"text-xs font-bold " + titleClr}>{loading ? "PROCESSING..." : label}</p><p className="text-[10px] text-gray-600 dark:text-gray-400 mt-0.5 font-medium">{desc}</p>
  </button>;
}
function Section({ title, children, className }: { title: string; children: React.ReactNode; className?: string }) {
  return <div className={"bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-2xs " + (className || "")}>
    <h3 className="text-xs font-black text-gray-900 dark:text-[#FFB800] tracking-wider uppercase flex items-center gap-2">{title}</h3>
    <div className="mt-3.5">{children}</div>
  </div>;
}
function InlineEdit({ value, onSave, type = "text" }: { value: string; onSave: (v: string) => void; type?: string }) {
  const [editing, setEditing] = useState(false);
  const [val, setVal] = useState(value);
  useEffect(() => setVal(value), [value]);
  if (!editing) return <span onClick={() => setEditing(true)} className="cursor-pointer hover:bg-[#FFB800]/10 px-1.5 py-0.5 rounded group inline-flex items-center gap-1.5 -ml-1.5 transition-colors text-gray-900 dark:text-white font-medium">{value || "—"} <Edit3 className="w-3 h-3 text-[#FFB800]/0 group-hover:text-amber-700 dark:group-hover:text-[#FFB800]" /></span>;
  return <input type={type} value={val} onChange={e => setVal(e.target.value)} onBlur={() => { onSave(val); setEditing(false); }} onKeyDown={e => { if (e.key === "Enter") { onSave(val); setEditing(false); } if (e.key === "Escape") { setVal(value); setEditing(false); }}} className="h-8 bg-white dark:bg-[#222] border border-[#FFB800]/50 rounded-lg px-2 text-xs text-gray-900 dark:text-white w-full shadow-xs focus:outline-none focus:ring-1 focus:ring-[#FFB800]" autoFocus />;
}
function ConfirmModal({ show, title, message, confirmLabel, onConfirm, onCancel }: { show: boolean; title: string; message: string; confirmLabel?: string; onConfirm: () => void; onCancel: () => void }) {
  if (!show) return null;
  return <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50">
    <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4">
      <h3 className="text-lg font-black text-gray-900 dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-red-500" /> {title}</h3>
      <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 font-medium leading-relaxed">{message}</p>
      <div className="flex items-center justify-end gap-3 pt-2">
        <button onClick={onCancel} className="h-10 px-4 bg-gray-100 dark:bg-white/10 hover:bg-gray-200 dark:hover:bg-white/15 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold transition-colors cursor-pointer">Cancel</button>
        <button onClick={onConfirm} className="h-10 px-5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-black transition-colors cursor-pointer shadow-xs">{confirmLabel || "Confirm"}</button>
      </div>
    </div>
  </div>;
}
function SearchInput({ value, onChange, placeholder = "Search...", className = "" }: { value: string; onChange: (v: string) => void; placeholder?: string; className?: string }) {
  return <div className={"relative group flex items-center w-full " + className}>
    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500 group-focus-within:text-[#FFB800] pointer-events-none transition-colors" />
    <input
      type="text"
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
      className="h-10 pl-12 pr-9 bg-white dark:bg-[#1c1c1c] border border-gray-300 dark:border-white/15 rounded-xl text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 focus:border-[#FFB800] w-full transition-all shadow-2xs"
    />
    {value && (
      <button
        type="button"
        onClick={() => onChange("")}
        className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-gray-700 dark:hover:text-white rounded-md transition-colors cursor-pointer"
        title="Clear search"
      >
        <X className="w-3.5 h-3.5" />
      </button>
    )}
  </div>;
}
function SaveBtn({ onClick, label = "Save", loading = false, size = "sm" }: { onClick: () => void; label?: string; loading?: boolean; size?: "sm" | "md" }) {
  const s = size === "md" ? "h-10 px-5 text-xs" : "h-10 px-4 text-xs";
  return <button onClick={onClick} disabled={loading}
    className={"inline-flex items-center justify-center gap-2 " + s + " bg-[#FFB800] hover:bg-[#FFB800]/90 disabled:bg-[#FFB800]/40 text-[#111] rounded-xl font-black shadow-xs hover:shadow-md transition-all shrink-0 cursor-pointer"}>
    {loading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />} {label}
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
      className={"w-full flex items-center justify-between gap-2 bg-white dark:bg-[#1c1c1c] border border-gray-300 dark:border-white/15 rounded-xl text-gray-900 dark:text-white hover:border-gray-400 dark:hover:border-white/30 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 focus:border-[#FFB800] transition-all text-left cursor-pointer shadow-2xs " + (compact ? "h-8 px-2.5 text-[10px]" : "h-10 px-3.5 text-xs")}>
      <span className={"truncate " + (selected ? "font-bold" : "text-gray-400 dark:text-gray-500 font-medium")}>{selected ? selected.label : placeholder}</span>
      <ChevronDown className={"w-3.5 h-3.5 shrink-0 text-gray-500 dark:text-gray-400 transition-transform duration-200 " + (open ? "rotate-180" : "")} />
    </button>
    {open && <div className="absolute top-full left-0 mt-1.5 w-full min-w-[170px] z-[100] bg-white dark:bg-[#1a1a1a] rounded-2xl p-1.5 shadow-2xl border border-gray-200 dark:border-white/15 overflow-hidden animate-scale-in">
      <div className="max-h-60 overflow-y-auto space-y-0.5">
        {options.map(o => (
          <button key={o.value} type="button" disabled={o.disabled} onClick={() => { if (!o.disabled) { onChange(o.value); setOpen(false); } }}
            className={"w-full text-left flex items-center gap-2.5 px-3 py-2 rounded-xl transition-colors cursor-pointer " + (compact ? "text-[10px]" : "text-xs ") + (o.disabled ? "opacity-40 cursor-not-allowed text-gray-400 dark:text-gray-500" : o.value === value ? "bg-[#FFB800]/20 text-gray-900 dark:text-white font-bold" : "text-gray-800 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-white/10")}>
            {renderOption ? renderOption(o, o.value === value) : <span className="truncate">{o.label}</span>}
            {o.value === value && <Check className="w-3.5 h-3.5 ml-auto text-amber-800 dark:text-[#FFB800] shrink-0" />}
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
      { uid: "seed_u1", name: "John Doe", email: "john@example.com", phone: "08012345678", role: "rider", status: "offline", isOnline: false, rating: 4.8, deliveryCount: 120, walletBalance: 5000, loyaltyPoints: 100, photoUrl: "" },
      { uid: "seed_u2", name: "Jane Smith", email: "jane@example.com", phone: "08087654321", role: "customer", status: "offline", isOnline: false, rating: 5.0, deliveryCount: 0, walletBalance: 2000, loyaltyPoints: 0, photoUrl: "" },
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

function DashboardTab({ deliveries, activeUsers, customers, drivers, pendingDeliveries, delivered, totalRevenue, totalTips, referrals, activeDeliveriesData, fmt, setTab, setShipmentsFilterPrefill, marketplaceEnabled, toggleMarketplace, onOpenShipmentFullView, addToast }: any) {
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
      if (addToast) addToast("success", `Assigned ${rider.name} to shipment #${idShort(deliveryId)}`);
    } catch (err: any) {
      console.error("Failed to assign rider:", err);
      if (addToast) addToast("error", "Could not assign rider: " + err.message);
      else alert("Could not assign rider: " + err.message);
    }
  };

  const handleInlineStatusChange = async (deliveryId: string, newStatus: string) => {
    const del = deliveries.find((x: any) => x.id === deliveryId);
    const hasRider = !!(del?.riderId || del?.driverId || (del?.courierName && del.courierName !== "Unassigned"));
    if (!hasRider && (newStatus === "TRANSIT" || newStatus === "OUT_FOR_DELIVERY" || newStatus === "DELIVERED" || newStatus === "ARRIVED")) {
      if (addToast) addToast("error", "Operational Guard: Assign a rider before updating status to " + newStatus.replace(/_/g, " ") + ".");
      else alert("Operational Guard: Assign a rider before updating status.");
      return;
    }
    try {
      await updateDoc(doc(db, "deliveries", deliveryId), {
        status: newStatus,
        updatedAt: Timestamp.now()
      });
      if (addToast) addToast("success", `Updated status to ${newStatus.replace(/_/g, " ")}`);
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
      if (addToast) addToast("error", "Could not update status: " + err.message);
      else alert("Could not update status: " + err.message);
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

  return <div className="tab-content space-y-5">
      <div className={`p-4 rounded-2xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all ${
        marketplaceEnabled 
          ? "bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800/60 text-emerald-900 dark:text-emerald-200" 
          : "bg-red-50 dark:bg-red-950/40 border-red-200 dark:border-red-800/60 text-red-900 dark:text-red-200"
      }`}>
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 ${marketplaceEnabled ? "bg-emerald-600 text-white" : "bg-red-600 text-white"}`}>
            <Store className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-black uppercase tracking-wider">Marketplace & Storefront App Visibility:</span>
              <span className={`text-[10px] font-black px-2.5 py-0.5 rounded-full ${marketplaceEnabled ? "bg-emerald-600 text-white" : "bg-red-600 text-white"}`}>
                {marketplaceEnabled ? "LIVE ON APP" : "HIDDEN ON APP"}
              </span>
            </div>
            <p className="text-[11px] text-gray-700 dark:text-gray-300 font-medium mt-0.5">
              {marketplaceEnabled 
                ? "Customer mobile app displays vendor store catalogs, verified shops carousel, and merchant enrollment. Click button to immediately turn off." 
                : "Marketplace is completely hidden on mobile. Mobile dashboard hero button dynamically converts to 'Live Tracking' radar."}
            </p>
          </div>
        </div>
        <button
          onClick={toggleMarketplace}
          className={`h-10 px-4 rounded-xl text-xs font-black shrink-0 transition-all shadow-xs flex items-center gap-2 cursor-pointer ${
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

      <section className="space-y-5 min-h-0">
        <div className="border border-gray-200 dark:border-white/10 rounded-3xl p-5 sm:p-6 flex flex-col bg-white dark:bg-[#1a1a1a] shadow-xs">
          <div className="flex justify-between items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 className="text-lg sm:text-xl font-extrabold tracking-tight text-[#111] dark:text-white">Active Bookings Today</h2>
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-0.5">Real-time scheduled drop-offs awaiting fulfillment</p>
            </div>
            <button 
              onClick={() => setTab("shipments")} 
              className="h-9 px-3.5 rounded-xl bg-gray-100 hover:bg-gray-200 dark:bg-[#222] dark:hover:bg-[#333] text-xs font-black text-[#111] dark:text-white transition-all cursor-pointer flex items-center gap-1.5"
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
                        <span className="font-medium text-[#111] dark:text-white truncate max-w-[140px] sm:max-w-[260px]">{d.pickupAddress || "Pickup"}</span>
                        <span className="text-gray-500 dark:text-gray-400 shrink-0 font-bold">→</span>
                        <span className="truncate max-w-[160px] sm:max-w-[340px]">{d.deliveryAddress || "Destination"}</span>
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
                      className="h-8 px-3 bg-[#FFB800] text-[#111] text-xs font-black rounded-xl hover:bg-[#FFB800]/90 transition-all shadow-xs cursor-pointer whitespace-nowrap flex items-center gap-1"
                    >
                      Manage <ChevronRight size={13} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Fleet Readiness: Dedicated uncompressed section directly under Active Bookings */}
        <div className="border border-gray-200 dark:border-white/10 rounded-3xl p-5 sm:p-6 bg-white dark:bg-[#1a1a1a] shadow-xs space-y-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-gray-100 dark:border-white/10">
            <div>
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-[#FFB800]/15 flex items-center justify-center text-[#FFB800]">
                  <Truck size={18} />
                </div>
                <h3 className="font-extrabold text-base sm:text-lg text-[#111] dark:text-white">Fleet Readiness & Operations</h3>
              </div>
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">Active courier dispatch capacity, rider telemetry, and fulfillment readiness</p>
            </div>
            <div className="flex items-center gap-3 flex-wrap">
              <span className="px-3.5 py-1.5 rounded-full bg-emerald-500/15 text-emerald-800 dark:text-emerald-300 text-xs font-black uppercase tracking-wider border border-emerald-500/30 flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                {drivers.filter((d: any) => d.isOnline === true).length} Online Couriers
              </span>
              <button 
                onClick={() => setTab("shipments")} 
                className="h-9 px-4 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black shadow-xs hover:bg-[#FFB800]/90 active:scale-[0.99] transition-all flex items-center gap-2 cursor-pointer"
              >
                <Truck size={14} /> Assign Riders ({pendingDeliveries.length})
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Courier Roster Card */}
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex items-center gap-3.5">
              <div className="flex -space-x-2 shrink-0">
                {drivers.slice(0, 3).map((d: any) => (
                  <div 
                    key={d.id} 
                    title={`${d.name || "Courier"} (${d.isOnline === true ? "Online" : "Offline"})`} 
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
              <div className="min-w-0">
                <p className="font-extrabold text-sm text-[#111] dark:text-white truncate">{drivers.length} Registered Riders</p>
                <p className="text-gray-600 dark:text-gray-400 text-xs font-medium truncate mt-0.5">{drivers.filter((d: any) => d.isOnline === true).length} active for dispatch</p>
              </div>
            </div>

            {/* Deliveries Today */}
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex flex-col justify-between">
              <span className="text-xs text-gray-600 dark:text-gray-400 font-bold uppercase tracking-wider">Deliveries Today</span>
              <div className="flex items-baseline justify-between mt-2">
                <span className="text-2xl font-black text-[#111] dark:text-white">{deliveries.length}</span>
                <span className="text-xs font-bold text-gray-500 dark:text-gray-400">{delivered.length} completed</span>
              </div>
            </div>

            {/* Today's Revenue */}
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex flex-col justify-between">
              <span className="text-xs text-gray-600 dark:text-gray-400 font-bold uppercase tracking-wider">Today's Revenue</span>
              <div className="flex items-baseline justify-between mt-2">
                <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400">{fmt(totalRevenue)}</span>
                <span className="text-xs font-bold text-emerald-700 dark:text-emerald-500">Gross volume</span>
              </div>
            </div>

            {/* Awaiting Dispatch (Uncompressed with ample room) */}
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 flex flex-col justify-between">
              <span className="text-xs text-gray-600 dark:text-gray-400 font-bold uppercase tracking-wider">Awaiting Dispatch</span>
              <div className="flex items-center justify-between mt-2 gap-2">
                <span className="text-2xl font-black text-[#111] dark:text-white">{pendingDeliveries.length}</span>
                <span className="font-extrabold text-xs px-3 py-1.5 rounded-xl bg-amber-500/15 text-amber-900 dark:text-amber-300 border border-amber-500/30 whitespace-nowrap">
                  {pendingDeliveries.length === 0 ? "0 Unassigned" : `${pendingDeliveries.length} Unassigned`}
                </span>
              </div>
            </div>
          </div>
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
  adminProfile?: { name: string; staffId: string; phone: string; role: string } | null;
  onOpenAdminProfile?: () => void;
  onMarkAllNotifsRead?: () => void;
  onClearAllNotifs?: () => void;
}

function formatNotifTime(n: any): string {
  const ts = n.timestamp?.toMillis ? n.timestamp.toMillis() : (n.createdAt?.toMillis ? n.createdAt.toMillis() : (typeof n.timestamp === "number" ? n.timestamp : (n.createdAt ? new Date(n.createdAt).getTime() : null)));
  if (!ts) {
    if (n.time && n.time !== "Just now") return n.time;
    return "Just now";
  }
  const now = Date.now();
  const diffMs = now - ts;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return "Just now";
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays === 1) return "Yesterday";
  if (diffDays < 7) return `${diffDays}d ago`;
  return new Date(ts).toLocaleDateString(undefined, { month: "short", day: "numeric" });
}

function Header({ 
  searchQuery, setSearchQuery, unreadCount, setShowNotifs, showNotifs, setShowUserMenu, showUserMenu, 
  currentUser, userRole, toggleDark, dark, setMobileSidebar, notifications, markNotifRead,
  adminProfile, onOpenAdminProfile, onMarkAllNotifsRead, onClearAllNotifs
}: HeaderProps) {
  return (
    <header className="flex justify-between items-center px-4 sm:px-6 lg:px-8 pt-4 sm:pt-5 pb-2 shrink-0 gap-4">
      <div className="flex items-center gap-3">
        <button className="lg:hidden p-2 text-gray-900 dark:text-white hover:bg-black/5 dark:hover:bg-white/10 rounded-xl transition-colors cursor-pointer" onClick={() => setMobileSidebar(true)}>
          <Menu size={22} />
        </button>
        <div className="text-xs sm:text-sm text-gray-600 dark:text-gray-400">
          Welcome to<br /><span className="font-extrabold text-gray-900 dark:text-white text-base sm:text-lg tracking-tight">ES<span className="text-amber-600 dark:text-[#FFB800]">DISPATCH</span></span>
        </div>
      </div>
      <div className="flex items-center gap-2 sm:gap-3">
        <div className="hidden sm:block w-[220px] lg:w-[300px]">
          <SearchInput value={searchQuery} onChange={setSearchQuery} placeholder="Search console..." />
        </div>
        <button 
          onClick={toggleDark} 
          className="w-10 h-10 border border-gray-300 dark:border-white/15 rounded-full flex items-center justify-center cursor-pointer shadow-2xs hover:bg-black/5 dark:hover:bg-white/10 transition-colors shrink-0"
          title={dark ? "Switch to Light Mode" : "Switch to Dark Mode"}
          aria-label="Toggle Theme"
        >
          {dark ? <Sun size={18} className="text-[#FFB800]" /> : <Moon size={18} className="text-gray-900" />}
        </button>

        {/* High-Density Compact Notification Dropdown */}
        <div className="relative" id="notif-area">
          <button 
            onClick={(e) => { e.stopPropagation(); setShowNotifs(!showNotifs); setShowUserMenu(false); }} 
            className="relative w-10 h-10 border border-gray-300 dark:border-white/15 rounded-full flex items-center justify-center cursor-pointer shadow-2xs hover:bg-black/5 dark:hover:bg-white/10 transition-colors shrink-0" 
            title="Notifications"
          >
            <Bell size={18} className="text-gray-900 dark:text-white" />
            {unreadCount > 0 && <div className="absolute top-2 right-2.5 w-2.5 h-2.5 bg-[#FFB800] rounded-full border-2 border-white dark:border-[#1a1a1a] animate-pulse-ring" />}
          </button>

          {showNotifs && (
            <div className="absolute right-0 top-full mt-2 w-[340px] sm:w-[380px] max-w-[calc(100vw-1.5rem)] bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-2xl shadow-2xl overflow-hidden z-50 flex flex-col animate-scale-in">
              {/* Compact Header */}
              <div className="px-4 py-2.5 border-b border-gray-100 dark:border-white/10 flex items-center justify-between bg-gray-50 dark:bg-white/5 shrink-0">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-black text-gray-900 dark:text-white uppercase tracking-wider">Notifications</span>
                  {unreadCount > 0 && (
                    <span className="px-1.5 py-0.5 rounded-full text-[10px] font-black bg-[#FFB800] text-[#111]">
                      {unreadCount} new
                    </span>
                  )}
                </div>
                {unreadCount > 0 && onMarkAllNotifsRead && (
                  <button 
                    onClick={(e) => { e.stopPropagation(); onMarkAllNotifsRead(); }} 
                    className="text-[11px] font-bold text-amber-800 dark:text-[#FFB800] hover:underline cursor-pointer"
                  >
                    Mark all read
                  </button>
                )}
              </div>

              {/* High-Density Clean List — EXACT Marketplace & Stores standard */}
              <div className="max-h-[300px] overflow-y-auto divide-y divide-gray-100 dark:divide-white/5">
                {notifications.length === 0 ? (
                  <div className="py-8 px-4 text-center">
                    <Bell className="w-6 h-6 text-gray-300 dark:text-gray-600 mx-auto mb-2 opacity-60" />
                    <p className="text-xs text-gray-600 dark:text-gray-400 font-bold">No notifications</p>
                    <p className="text-[10px] text-gray-400 dark:text-gray-500 mt-0.5">Alerts appear here in real-time.</p>
                  </div>
                ) : (
                  notifications.map((n: any) => {
                    const isOrder = n.title?.toLowerCase().includes("order") || n.title?.toLowerCase().includes("shipment");
                    const isWallet = n.title?.toLowerCase().includes("wallet") || n.title?.toLowerCase().includes("credit") || n.title?.toLowerCase().includes("payout");
                    const isUser = n.title?.toLowerCase().includes("user") || n.title?.toLowerCase().includes("rider");
                    return (
                      <div 
                        key={n.id} 
                        onClick={() => markNotifRead(n.id)} 
                        className={`px-3.5 py-2.5 flex items-center gap-3 transition-colors cursor-pointer border-l-2 ${
                          !n.read 
                            ? "bg-[#FFB800]/10 border-l-[#FFB800] hover:bg-[#FFB800]/15" 
                            : "bg-transparent border-l-transparent hover:bg-black/5 dark:hover:bg-white/5 opacity-80"
                        }`}
                      >
                        {/* Icon on the Left */}
                        <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                          isOrder 
                            ? "bg-blue-500/15 text-blue-600 dark:text-blue-400"
                            : isWallet 
                              ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400"
                              : isUser 
                                ? "bg-purple-500/15 text-purple-600 dark:text-purple-400"
                                : "bg-[#FFB800]/20 text-amber-900 dark:text-[#FFB800]"
                        }`}>
                          {isOrder ? <Package size={15} /> : isWallet ? <DollarSign size={15} /> : isUser ? <User size={15} /> : <Bell size={15} />}
                        </div>

                        {/* Text Stack: Title on top, Subtitle right below it */}
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center justify-between gap-2">
                            <p className="text-xs font-bold text-gray-900 dark:text-white truncate">
                              {n.title}
                            </p>
                            <span className="text-[10px] text-gray-400 dark:text-gray-500 font-medium shrink-0 whitespace-nowrap">
                              {formatNotifTime(n)}
                            </span>
                          </div>
                          <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium truncate mt-0.5">
                            {n.description}
                          </p>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Compact Footer */}
              <div className="py-2 px-3.5 bg-gray-50 dark:bg-white/5 border-t border-gray-100 dark:border-white/10 flex items-center justify-between text-[11px] shrink-0">
                <span className="text-gray-500 dark:text-gray-400 font-medium">{notifications.length} alerts</span>
                {notifications.length > 0 && onClearAllNotifs && (
                  <button onClick={(e) => { e.stopPropagation(); onClearAllNotifs(); }} className="text-gray-500 hover:text-red-500 dark:text-gray-400 dark:hover:text-red-400 font-bold transition-colors cursor-pointer">
                    Clear all
                  </button>
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Profile Menu with Staff ID */}
        <div className="relative" id="user-menu-area">
          <div className="flex items-center gap-2.5 ml-1 cursor-pointer h-10 select-none" onClick={(e) => { e.stopPropagation(); setShowUserMenu(!showUserMenu); setShowNotifs(false); }}>
            <div className="w-10 h-10 rounded-full bg-[#FFB800]/20 border border-black/10 dark:border-white/10 flex items-center justify-center text-amber-900 dark:text-white font-black text-sm shrink-0">
              <EdLogoSvg size={18} dark={!dark} />
            </div>
            <div className="text-sm hidden sm:block">
              <div className="font-extrabold text-gray-900 dark:text-white leading-tight">
                {adminProfile?.name || currentUser?.email?.split("@")[0] || "Admin"}
              </div>
              <div className="text-gray-600 dark:text-gray-400 font-bold text-[10px] uppercase tracking-wider leading-tight mt-0.5 flex items-center gap-1">
                {adminProfile?.staffId && <span className="text-[#FFB800]">{adminProfile.staffId} •</span>}
                <span>{(userRole || "admin").replace("_", " ")}</span>
              </div>
            </div>
          </div>

          {showUserMenu && (
            <div className="absolute right-0 top-full mt-2 w-64 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-2xl shadow-2xl overflow-hidden z-50 animate-scale-in">
              <div className="p-3.5 border-b border-gray-200 dark:border-white/10 bg-gray-50/70 dark:bg-white/5">
                <p className="text-xs font-black text-gray-900 dark:text-white truncate">{adminProfile?.name || "Administrator"}</p>
                <p className="text-[11px] text-gray-500 dark:text-gray-400 truncate mt-0.5">{currentUser?.email}</p>
                <div className="flex items-center gap-1.5 mt-2 flex-wrap">
                  <span className="px-2 py-0.5 rounded-md text-[9px] font-black bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 uppercase">
                    {(userRole || "Admin").replace("_", " ")}
                  </span>
                  {adminProfile?.staffId && (
                    <span className="px-2 py-0.5 rounded-md text-[9px] font-mono font-bold bg-gray-100 dark:bg-white/10 text-gray-700 dark:text-gray-300">
                      ID: {adminProfile.staffId}
                    </span>
                  )}
                </div>
              </div>
              <div className="p-2 space-y-1">
                <button 
                  onClick={() => { setShowUserMenu(false); onOpenAdminProfile?.(); }} 
                  className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/10 transition-colors text-xs font-bold text-gray-900 dark:text-white cursor-pointer"
                >
                  <User className="w-4 h-4 text-[#FFB800]" />
                  <span>My Profile & Staff ID</span>
                </button>
                <button 
                  onClick={toggleDark} 
                  className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/10 transition-colors text-xs font-bold text-gray-900 dark:text-white cursor-pointer"
                >
                  {dark ? <Sun className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" /> : <Moon className="w-4 h-4 text-amber-700 dark:text-[#FFB800]" />}
                  <span>{dark ? "Light Mode" : "Dark Mode"}</span>
                </button>
                <button 
                  onClick={() => { document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); }} 
                  className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors text-xs font-bold text-red-600 dark:text-red-400 cursor-pointer"
                >
                  <LogOut className="w-4 h-4" /> 
                  <span>Sign Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

function AdminProfileModal({
  show,
  onClose,
  currentUser,
  adminProfile,
  onSave,
  userRole
}: {
  show: boolean;
  onClose: () => void;
  currentUser: any;
  adminProfile: { name: string; staffId: string; phone: string; role: string } | null;
  onSave: (data: { name: string; staffId: string; phone: string }) => Promise<void>;
  userRole: string;
}) {
  const [name, setName] = useState(adminProfile?.name || "");
  const [staffId, setStaffId] = useState(adminProfile?.staffId || "");
  const [phone, setPhone] = useState(adminProfile?.phone || "");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (adminProfile) {
      setName(adminProfile.name || "");
      setStaffId(adminProfile.staffId || "");
      setPhone(adminProfile.phone || "");
    }
  }, [adminProfile, show]);

  if (!show) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    await onSave({ name, staffId, phone });
    setSaving(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs animate-fade-in">
      <div className="w-full max-w-md bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl p-6 shadow-2xl animate-scale-in space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-black text-gray-900 dark:text-white">Admin Profile & Staff ID</h3>
              <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium">Internal audit identity & credentials</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-700 dark:hover:text-white rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer">
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">
              Admin Full Name
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Osasogie Ekhator"
              className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">
                Official Staff ID
              </label>
              <input
                type="text"
                value={staffId}
                onChange={e => setStaffId(e.target.value.toUpperCase())}
                placeholder="e.g. ESD-ADM-001"
                className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 font-mono font-bold"
                required
              />
            </div>
            <div>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">
                System Role
              </label>
              <div className="h-10 bg-gray-100 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl px-3.5 flex items-center text-xs font-black text-amber-800 dark:text-[#FFB800] uppercase">
                {(userRole || "admin").replace("_", " ")}
              </div>
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">
              Phone Number
            </label>
            <input
              type="tel"
              value={phone}
              onChange={e => setPhone(e.target.value)}
              placeholder="e.g. +234 801 234 5678"
              className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40"
            />
          </div>

          <div>
            <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">
              Admin Email (Read-Only)
            </label>
            <input
              type="email"
              value={currentUser?.email || ""}
              disabled
              className="w-full h-10 bg-gray-100 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-500 dark:text-gray-400 cursor-not-allowed"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100 dark:border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="h-10 px-4 rounded-xl border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-300 hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
            >
              Cancel
            </button>
            <SaveBtn onClick={() => {}} label={saving ? "Saving..." : "Save Identity"} loading={saving} />
          </div>
        </form>
      </div>
    </div>
  );
}

function AdminDashboardPage() {
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [adminProfile, setAdminProfile] = useState<{ name: string; staffId: string; phone: string; role: string } | null>(null);
  const [showAdminProfileModal, setShowAdminProfileModal] = useState(false);
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
      document.body.classList.toggle("dark", isDark);
    } else {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      setDark(prefersDark);
      document.documentElement.classList.toggle("dark", prefersDark);
      document.body.classList.toggle("dark", prefersDark);
    }
  }, []);

  useEffect(() => {
    localStorage.setItem("ed_dark", String(dark));
    document.documentElement.classList.toggle("dark", dark);
    document.body.classList.toggle("dark", dark);
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

  const addLog = useCallback(async (action: string, details: string, category?: string) => {
    const adminName = adminProfile?.name || currentUser?.email?.split("@")[0] || "Admin";
    const staffId = adminProfile?.staffId || "ESD-ADM-001";
    const adminIdentifier = `${adminName} [${staffId}]`;
    const entry: any = { 
      action, 
      details, 
      admin: adminIdentifier, 
      staffId,
      adminEmail: currentUser?.email || "",
      category: category || "System",
      timestamp: Timestamp.now() 
    };
    try { 
      await addDoc(collection(db, "audit_logs"), entry); 
    } catch (e: any) {
      console.error("addLog failed:", e);
    }
    setLogs((prev: AuditEntry[]) => [{ id: Date.now().toString(), time: "Just now", ...entry, timestamp: Date.now() }, ...prev]);
  }, [currentUser, adminProfile]);

  const handleMarkAllNotifsRead = async () => {
    try {
      const batch = writeBatch(db);
      notifications.filter(n => !n.read).forEach(n => {
        batch.update(doc(db, "notifications", n.id), { read: true });
      });
      await batch.commit();
      addToast("success", "All notifications marked as read");
    } catch (e: any) {
      console.error("Failed to mark all read:", e);
    }
  };

  const handleClearAllNotifs = async () => {
    try {
      const batch = writeBatch(db);
      notifications.forEach(n => {
        batch.delete(doc(db, "notifications", n.id));
      });
      await batch.commit();
      addToast("success", "Notification feed cleared");
    } catch (e: any) {
      console.error("Failed to clear notifications:", e);
    }
  };

  const handleSaveAdminProfile = async (data: { name: string; staffId: string; phone: string }) => {
    if (!currentUser) return;
    try {
      await setDoc(doc(db, "users", currentUser.uid), {
        name: data.name,
        staffId: data.staffId,
        phone: data.phone,
        updatedAt: Timestamp.now()
      }, { merge: true });
      setAdminProfile(prev => ({
        name: data.name,
        staffId: data.staffId,
        phone: data.phone,
        role: prev?.role || userRole || "admin"
      }));
      addToast("success", "Admin identity & Staff ID saved successfully");
      addLog("Admin Profile Update", `Updated admin identity: ${data.name} [${data.staffId}] (${currentUser.email})`, "Staff");
    } catch (e: any) {
      addToast("error", `Failed to save profile: ${e.message}`);
    }
  };

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (u) => {
      if (u) {
          try {
            await new Promise(r => setTimeout(r, 1500));
            const token = await u.getIdToken();
            const snap = await getDoc(doc(db, "users", u.uid));
            if (!snap.exists()) { setAuthErr("Account not found. Contact an administrator."); document.cookie = "admin_token=; path=/; max-age=0; SameSite=Strict"; signOut(auth); setCurrentUser(null); setLoading(false); return; }
            const d = snap.data();
            if (d?.role === "admin" || d?.role === "super_admin") { setCurrentUser(u); setUserRole(d.role); setAdminProfile({ name: d.name || u.email?.split("@")[0] || "Admin", staffId: d.staffId || "ESD-ADM-001", phone: d.phone || "", role: d.role }); document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`; setLoading(false); return; }
            if (d?.role === "dispatcher") { setCurrentUser(u); setUserRole("dispatcher"); setAdminProfile({ name: d.name || u.email?.split("@")[0] || "Dispatcher", staffId: d.staffId || "ESD-DSP-001", phone: d.phone || "", role: "dispatcher" }); document.cookie = `admin_token=${token}; path=/; max-age=86400; SameSite=Strict; Secure`; setLoading(false); return; }
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
      snap.forEach(d => {
        const x = d.data();
        const rawStatus = (x.status || "active").toLowerCase();
        
        // Strict real presence logic:
        // Account status ('active') means not banned or suspended.
        // It does NOT mean the user is currently online on a device!
        // isOnline is true ONLY if explicitly isOnline === true AND has a verified recent heartbeat (< 15 mins).
        let isOnline = false;
        if (x.isOnline === true && rawStatus !== "offline" && rawStatus !== "suspended") {
          const lastActive = x.lastSeen?.toMillis ? x.lastSeen.toMillis() : (x.updatedAt?.toMillis ? x.updatedAt.toMillis() : (typeof x.lastSeen === "number" ? x.lastSeen : (typeof x.updatedAt === "number" ? x.updatedAt : null)));
          if (lastActive) {
            isOnline = (Date.now() - lastActive) < 15 * 60 * 1000;
          } else {
            isOnline = false;
          }
        }

        list.push({
          id: d.id, uid: x.uid || d.id, name: x.name || "User", email: x.email || "", phone: x.phone || "",
          role: x.role || "customer",
          status: rawStatus === "offline" || rawStatus === "suspended" || rawStatus === "pending" ? rawStatus : "active",
          isOnline,
          rating: x.rating || 5.0, deliveryCount: x.deliveryCount || 0,
          walletBalance: x.walletBalance || x.balance || x.wallet_balance || 0,
          loyaltyPoints: x.loyaltyPoints || 0, photoUrl: x.photoUrl || "",
          bikeNumber: x.bikeNumber || "", staffId: x.staffId || "", lat: x.lat || x.latitude, lng: x.lng || x.longitude,
          isDeleted: x.isDeleted || false, updatedAt: x.updatedAt, lastSeen: x.lastSeen,
        });
      });
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
    unsubs.push(onSnapshot(query(collection(db, "audit_logs"), orderBy("timestamp", "desc"), limit(100)), snap => {
      const list: AuditEntry[] = [];
      snap.forEach(d => {
        const x = d.data();
        const ts = x.timestamp?.toMillis ? x.timestamp.toMillis() : (x.timestamp ? new Date(x.timestamp).getTime() : Date.now());
        list.push({
          id: d.id,
          time: new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          action: x.action || "Action",
          details: x.details || "",
          admin: x.admin || "Admin",
          staffId: x.staffId || "",
          adminEmail: x.adminEmail || "",
          category: x.category || "General",
          timestamp: ts,
        });
      });
      setLogs(list);
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
              <div className="relative"><Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" />
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="admin@engraced.com" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl pl-12 pr-4 py-2.5 text-sm text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 focus:border-[#FFB800]" required /></div></div>
            <div><label className="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-1">Password</label>
              <div className="relative"><Key className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" />
                <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl pl-12 pr-4 py-2.5 text-sm text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 focus:border-[#FFB800]" required /></div></div>
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
    { id: "broadcast", label: "Broadcast News", icon: <Radio size={24} strokeWidth={2} />, roles: ["super_admin", "admin", "dispatcher"] },
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
  const [roleFilter, setRoleFilter] = useState<string>("ALL");
  const [presenceFilter, setPresenceFilter] = useState<string>("ALL");
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [editUser, setEditUser] = useState<UserProfile | null>(null);
  const [previewUser, setPreviewUser] = useState<UserProfile | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState<{ mode: "single" | "bulk"; user?: UserProfile; count?: number } | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [showNewUser, setShowNewUser] = useState(false);
  const [newUserStep, setNewUserStep] = useState(1);
  const [newUserForm, setNewUserForm] = useState({ name: "", email: "", phone: "", role: "customer", pin: "", confirmPin: "", bikeNumber: "", staffId: "" });
  const [creatingUser, setCreatingUser] = useState(false);
  const [sweepingPresence, setSweepingPresence] = useState(false);
  const [fundUser, setFundUser] = useState<UserProfile | null>(null);
  const [fundAction, setFundAction] = useState<"credit" | "debit">("credit");
  const [fundAmount, setFundAmount] = useState("");
  const [fundReason, setFundReason] = useState("");
  const [fundingWallet, setFundingWallet] = useState(false);
  const [form, setForm] = useState({ name: "", role: "", phone: "", bikeNumber: "", staffId: "", status: "" });
  const [uPage, setUPage] = useState(0);
  const uPerPage = 15;

  const totalUsersCount = activeUsers.length;
  const onlineUsersCount = activeUsers.filter(u => u.isOnline === true).length;
  const ridersCount = activeUsers.filter(u => u.role === "rider").length;
  const customersCount = activeUsers.filter(u => u.role === "customer" || !u.role).length;
  const vendorsCount = activeUsers.filter(u => u.role === "vendor").length;
  const adminsCount = activeUsers.filter(u => u.role === "admin" || u.role === "super_admin" || u.role === "dispatcher").length;

  const filtered = useMemo(() => {
    const q = (searchQuery || search).toLowerCase().trim();
    return activeUsers.filter(u => {
      // Role filter
      if (roleFilter !== "ALL") {
        if (roleFilter === "customer" && (u.role !== "customer" && u.role !== "")) return false;
        if (roleFilter === "admin" && (u.role !== "admin" && u.role !== "super_admin" && u.role !== "dispatcher")) return false;
        if (roleFilter !== "customer" && roleFilter !== "admin" && u.role !== roleFilter) return false;
      }
      // Presence filter
      if (presenceFilter === "online" && !u.isOnline) return false;
      if (presenceFilter === "offline" && u.isOnline) return false;

      // Search query
      if (!q) return true;
      return (
        (u.name && u.name.toLowerCase().includes(q)) ||
        (u.email && u.email.toLowerCase().includes(q)) ||
        (u.phone && u.phone.includes(q)) ||
        (u.bikeNumber && u.bikeNumber.toLowerCase().includes(q)) ||
        (u.staffId && u.staffId.toLowerCase().includes(q)) ||
        (u.role && u.role.toLowerCase().includes(q))
      );
    });
  }, [activeUsers, searchQuery, search, roleFilter, presenceFilter]);

  const uTotalPages = Math.max(1, Math.ceil(filtered.length / uPerPage));
  const pagedUsers = filtered.slice(uPage * uPerPage, (uPage + 1) * uPerPage);

  useEffect(() => { setUPage(0); }, [search, searchQuery, roleFilter, presenceFilter]);

  // Selection handlers
  const handleSelectAll = () => {
    if (pagedUsers.length === 0) return;
    const allSelectedOnPage = pagedUsers.every(u => selectedIds.has(u.id));
    const next = new Set(selectedIds);
    if (allSelectedOnPage) {
      pagedUsers.forEach(u => next.delete(u.id));
    } else {
      pagedUsers.forEach(u => next.add(u.id));
    }
    setSelectedIds(next);
  };

  const handleToggleSelect = (id: string) => {
    const next = new Set(selectedIds);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setSelectedIds(next);
  };

  /** Sweep all stale presence in database */
  const handleSweepStalePresence = async () => {
    setSweepingPresence(true);
    try {
      const now = Date.now();
      const threshold = 15 * 60 * 1000;
      let count = 0;
      const batch = writeBatch(db);
      activeUsers.forEach(u => {
        if (u.isOnline) {
          const lastActive = u.lastSeen?.toMillis ? u.lastSeen.toMillis() : (u.updatedAt?.toMillis ? u.updatedAt.toMillis() : (typeof u.lastSeen === "number" ? u.lastSeen : (typeof u.updatedAt === "number" ? u.updatedAt : null)));
          if (!lastActive || (now - lastActive > threshold)) {
            batch.update(doc(db, "users", u.id), {
              isOnline: false,
              status: "offline",
              updatedAt: Timestamp.now()
            });
            count++;
          }
        }
      });
      if (count > 0) {
        await batch.commit();
        addLog("Sweep Presence", `Reset ${count} stale account(s) to offline status`, "Users");
        addToast?.("success", `Reset ${count} inactive account(s) to offline`);
      } else {
        addToast?.("info", "All active presence states are fresh and verified");
      }
    } catch (e: any) {
      addToast?.("error", `Sweep failed: ${e.message}`);
    }
    setSweepingPresence(false);
  };

  /** Execute hard permanent deletion from Firestore */
  const executePermanentDelete = async () => {
    if (!showDeleteModal) return;
    setDeleting(true);
    try {
      if (showDeleteModal.mode === "single" && showDeleteModal.user) {
        const u = showDeleteModal.user;
        await deleteDoc(doc(db, "users", u.id));
        addLog("User Permanent Delete", `Permanently deleted user doc ${u.id} (${u.name} - ${u.email || u.phone}) from Firestore`, "Users");
        addToast?.("success", `User ${u.name} permanently removed from database`);
        setSelectedIds(prev => { const n = new Set(prev); n.delete(u.id); return n; });
      } else if (showDeleteModal.mode === "bulk" && selectedIds.size > 0) {
        const ids = Array.from(selectedIds);
        const batch = writeBatch(db);
        const names: string[] = [];
        ids.forEach(id => {
          batch.delete(doc(db, "users", id));
          const match = activeUsers.find(u => u.id === id);
          if (match) names.push(match.name);
        });
        await batch.commit();
        addLog("Bulk User Permanent Delete", `Permanently deleted ${ids.length} user document(s) from Firestore: ${names.slice(0, 5).join(", ")}${names.length > 5 ? "..." : ""}`, "Users");
        addToast?.("success", `Successfully deleted ${ids.length} user(s) permanently from database`);
        setSelectedIds(new Set());
      }
    } catch (e: any) {
      addToast?.("error", `Delete failed: ${e.message}`);
    }
    setDeleting(false);
    setShowDeleteModal(null);
  };

  const saveUser = async () => {
    if (!editUser) return;
    try {
      const payload: any = {
        name: form.name,
        role: form.role,
        phone: form.phone,
        status: form.status,
        updatedAt: Timestamp.now()
      };
      if (form.bikeNumber) payload.bikeNumber = form.bikeNumber;
      if (form.staffId) payload.staffId = form.staffId;
      await updateDoc(doc(db, "users", editUser.id), payload);
      addLog("Update User", `Updated profile of ${form.name} (${editUser.email || editUser.id})`, "Users");
      addToast?.("success", "User profile updated successfully");
      setEditUser(null);
    } catch (e: any) {
      addToast?.("error", "Failed to update user: " + e.message);
    }
  };

  const promoteToVendor = async (u: UserProfile) => {
    try {
      await updateDoc(doc(db, "users", u.id), { role: "vendor", updatedAt: Timestamp.now() });
      const storeRef = doc(collection(db, "marketplace_stores"));
      await setDoc(storeRef, {
        storeName: `${u.name}'s Store`,
        ownerId: u.id,
        phone: u.phone,
        email: u.email,
        status: "APPROVED",
        isVerified: true,
        dateEnlisted: new Date().toISOString(),
        storeRating: 5.0,
        totalSales: 0,
        vendorBalance: 0,
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now()
      });
      addLog("Upgrade User", `Upgraded ${u.name} to Vendor and created store`, "Marketplace");
      addToast?.("success", `${u.name} promoted to Vendor store owner`);
    } catch (e: any) {
      addToast?.("error", "Failed to promote user: " + e.message);
    }
  };

  const handleFundWallet = async () => {
    if (!fundUser) return;
    const amt = parseFloat(fundAmount);
    if (!amt || isNaN(amt) || amt <= 0) {
      addToast?.("error", "Please enter a valid amount greater than 0");
      return;
    }
    const currentBal = fundUser.walletBalance || 0;
    if (fundAction === "debit" && amt > currentBal) {
      addToast?.("error", `Cannot debit ${fmt(amt)}. User only has ${fmt(currentBal)} available.`);
      return;
    }
    setFundingWallet(true);
    try {
      const delta = fundAction === "credit" ? amt : -amt;
      const userRef = doc(db, "users", fundUser.id);
      await updateDoc(userRef, {
        walletBalance: increment(delta),
        updatedAt: Timestamp.now()
      });

      const txRef = doc(collection(db, "users", fundUser.id, "transactions"));
      await setDoc(txRef, {
        id: txRef.id,
        amount: amt,
        type: fundAction.toUpperCase(),
        title: `Wallet ${fundAction === "credit" ? "Credit" : "Debit"} (Admin)`,
        narration: fundReason.trim() || (fundAction === "credit" ? "Manual credit by Admin" : "Manual debit by Admin"),
        status: "completed",
        timestamp: Timestamp.now(),
        createdAt: Timestamp.now()
      });

      if (createNotification) {
        await createNotification(
          `Wallet ${fundAction === "credit" ? "Credited" : "Debited"}`,
          `Your wallet has been ${fundAction === "credit" ? "credited with" : "debited by"} ${fmt(amt)}. ${fundReason ? `Reason: ${fundReason}` : ""}`
        );
      }

      addLog(
        fundAction === "credit" ? "Credit Wallet" : "Debit Wallet",
        `${fundAction === "credit" ? "Credited" : "Debited"} ${fmt(amt)} for ${fundUser.name} (${fundUser.email || fundUser.id}). New projected balance: ${fmt(currentBal + delta)}`,
        "Wallet"
      );

      addToast?.("success", `Successfully ${fundAction === "credit" ? "credited" : "debited"} ${fmt(amt)} for ${fundUser.name}`);
      setFundUser(null);
      setFundAmount("");
      setFundReason("");
    } catch (e: any) {
      addToast?.("error", "Failed to update wallet: " + e.message);
    }
    setFundingWallet(false);
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    const parts = name.trim().split(" ");
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <div className="tab-content space-y-6">
      {/* Header with Title and Actions */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2">
            <Users className="w-5 h-5 text-[#FFB800]" /> User Accounts & Fleet
          </h1>
          <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">
            Manage customers, active couriers, merchants, and administrative personnel across the platform.
          </p>
        </div>
        <div className="flex items-center gap-2.5 flex-wrap">
          <button
            type="button"
            onClick={handleSweepStalePresence}
            disabled={sweepingPresence}
            title="Clean up stale device sessions"
            className="h-10 px-3.5 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:border-[#FFB800]/50 transition-all flex items-center gap-2 shadow-2xs cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-[#FFB800] ${sweepingPresence ? "animate-spin" : ""}`} />
            <span className="hidden sm:inline">Sweep Stale Presence</span>
          </button>
          <button
            type="button"
            onClick={() => {
              setNewUserStep(1);
              setNewUserForm({ name: "", email: "", phone: "", role: "customer", pin: "", confirmPin: "", bikeNumber: "", staffId: "" });
              setShowNewUser(true);
            }}
            className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-xs hover:shadow-md transition-all flex items-center gap-2 cursor-pointer"
          >
            <UserPlus className="w-4 h-4" /> Add User
          </button>
        </div>
      </div>

      {/* 4 Spacious Executive Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-3 sm:gap-4">
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Total Accounts</span>
          <p className="text-xl sm:text-2xl font-black text-[#111] dark:text-white mt-1">{totalUsersCount}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Registered records</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-emerald-500/20 dark:border-emerald-500/20 rounded-2xl p-4 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-extrabold text-emerald-700 dark:text-emerald-400 uppercase tracking-wider block">Currently Online</span>
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          </div>
          <p className="text-xl sm:text-2xl font-black text-emerald-600 dark:text-emerald-400 mt-1">{onlineUsersCount}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Verified device presence</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Couriers / Fleet</span>
          <p className="text-xl sm:text-2xl font-black text-[#111] dark:text-white mt-1">{ridersCount}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Active riders</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Customers</span>
          <p className="text-xl sm:text-2xl font-black text-[#111] dark:text-white mt-1">{customersCount}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">App buyers & senders</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs col-span-2 sm:col-span-1">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Vendors & Staff</span>
          <p className="text-xl sm:text-2xl font-black text-[#111] dark:text-white mt-1">{vendorsCount + adminsCount}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">{vendorsCount} stores • {adminsCount} staff</span>
        </div>
      </div>

      {/* Filter Chips & Search Bar */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-3 sm:p-4 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3 shadow-xs">
        {/* Role & Presence Chips */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 md:pb-0 scrollbar-none">
          <span className="text-xs font-bold text-gray-500 dark:text-gray-400 mr-1 hidden lg:inline">Filter:</span>
          {[
            { id: "ALL", label: "All Roles", count: totalUsersCount },
            { id: "customer", label: "Customers", count: customersCount },
            { id: "rider", label: "Riders", count: ridersCount },
            { id: "vendor", label: "Vendors", count: vendorsCount },
            { id: "admin", label: "Staff & Admins", count: adminsCount },
          ].map(tab => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setRoleFilter(tab.id)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer flex items-center gap-1.5 ${
                roleFilter === tab.id
                  ? "bg-[#FFB800] text-[#111] shadow-xs"
                  : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#2c2c2c]"
              }`}
            >
              {tab.label}
              <span className={`text-[10px] px-1.5 py-0.2 rounded-full font-extrabold ${roleFilter === tab.id ? "bg-[#111]/20 text-[#111]" : "bg-black/10 dark:bg-white/10"}`}>
                {tab.count}
              </span>
            </button>
          ))}

          <div className="h-4 w-px bg-gray-300 dark:bg-white/10 mx-1 hidden sm:block" />

          {[
            { id: "ALL", label: "All Status" },
            { id: "online", label: "Online Only" },
            { id: "offline", label: "Offline" }
          ].map(p => (
            <button
              key={p.id}
              type="button"
              onClick={() => setPresenceFilter(p.id)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer ${
                presenceFilter === p.id
                  ? "bg-[#111] dark:bg-white text-white dark:text-[#111] shadow-xs"
                  : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#2c2c2c]"
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>

        {/* Generous Search Bar with pl-12 */}
        <div className="w-full md:w-80 shrink-0">
          <SearchInput value={search} onChange={setSearch} placeholder="Search name, email, phone, bike #..." />
        </div>
      </div>

      {/* Bulk Operations Toolbar */}
      {selectedIds.size > 0 && (
        <div className="bg-[#FFB800]/15 border border-[#FFB800]/40 rounded-2xl p-3 px-4 flex items-center justify-between flex-wrap gap-3 animate-fade-in shadow-xs">
          <div className="flex items-center gap-2.5">
            <span className="w-7 h-7 rounded-lg bg-[#FFB800] text-[#111] font-black text-xs flex items-center justify-center">
              {selectedIds.size}
            </span>
            <span className="text-xs font-bold text-gray-900 dark:text-white">
              user account(s) selected
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setSelectedIds(new Set())}
              className="h-8 px-3 rounded-xl bg-white dark:bg-[#222] border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer"
            >
              Deselect All
            </button>
            <button
              type="button"
              onClick={() => setShowDeleteModal({ mode: "bulk", count: selectedIds.size })}
              className="h-8 px-3.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-xs font-black flex items-center gap-1.5 shadow-xs transition-all cursor-pointer"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Delete Selected ({selectedIds.size})</span>
            </button>
          </div>
        </div>
      )}

      {/* Main Table Container */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs overflow-hidden">
        {filtered.length === 0 ? (
          <div className="text-center py-16">
            <div className="w-12 h-12 rounded-2xl bg-[#FFB800]/10 border border-[#FFB800]/20 flex items-center justify-center text-[#FFB800] mx-auto mb-3">
              <Users className="w-6 h-6" />
            </div>
            <p className="text-sm font-bold text-gray-900 dark:text-white">No accounts found</p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Try resetting your role or presence filters.</p>
            <button
              type="button"
              onClick={() => { setSearch(""); setRoleFilter("ALL"); setPresenceFilter("ALL"); }}
              className="mt-4 px-4 py-2 bg-gray-100 dark:bg-white/10 text-xs font-bold rounded-xl hover:bg-gray-200 dark:hover:bg-white/20 transition-all cursor-pointer"
            >
              Clear Filters
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-black/5 dark:border-white/10 text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider bg-gray-50/50 dark:bg-white/5">
                  <th className="p-3.5 w-10 text-center">
                    <input
                      type="checkbox"
                      checked={pagedUsers.length > 0 && pagedUsers.every(u => selectedIds.has(u.id))}
                      onChange={handleSelectAll}
                      className="w-4 h-4 rounded-md accent-[#FFB800] cursor-pointer"
                      title="Select all on this page"
                    />
                  </th>
                  <th className="p-3.5">User Details</th>
                  <th className="p-3.5">Role</th>
                  <th className="p-3.5">Financial & Activity</th>
                  <th className="p-3.5">Status & Presence</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10 text-xs">
                {pagedUsers.map(u => (
                  <tr key={u.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group">
                    {/* Checkbox */}
                    <td className="p-3.5 text-center">
                      <input
                        type="checkbox"
                        checked={selectedIds.has(u.id)}
                        onChange={() => handleToggleSelect(u.id)}
                        className="w-4 h-4 rounded-md accent-[#FFB800] cursor-pointer"
                      />
                    </td>

                    {/* User Identity Column */}
                    <td className="p-3.5">
                      <div className="flex items-center gap-3">
                        <div className="relative shrink-0">
                          {u.photoUrl ? (
                            <img src={u.photoUrl} alt={u.name} className="w-10 h-10 rounded-2xl object-cover border border-black/10 dark:border-white/15" />
                          ) : (
                            <div className="w-10 h-10 rounded-2xl bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black text-xs">
                              {getInitials(u.name)}
                            </div>
                          )}
                          {/* Online indicator dot */}
                          {u.isOnline ? (
                            <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-[#1a1a1a]" title="Online now" />
                          ) : (
                            <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-zinc-400 dark:bg-zinc-600 ring-2 ring-white dark:ring-[#1a1a1a]" title="Offline" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5 flex-wrap">
                            <span className="font-extrabold text-gray-900 dark:text-white truncate">{u.name}</span>
                            {u.staffId && (
                              <span className="text-[9px] font-mono font-bold px-1.5 py-0.2 rounded bg-gray-100 dark:bg-white/10 text-gray-700 dark:text-gray-300">
                                {u.staffId}
                              </span>
                            )}
                            {u.bikeNumber && (
                              <span className="text-[9px] font-mono font-bold px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-800 dark:text-[#FFB800] border border-amber-500/20">
                                {u.bikeNumber}
                              </span>
                            )}
                          </div>
                          <div className="text-[11px] text-gray-500 dark:text-gray-400 truncate mt-0.5">
                            {u.email || u.phone || "No direct contact"}
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* Role Column */}
                    <td className="p-3.5">
                      <span className={`inline-block px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${
                        u.role === "rider"
                          ? "bg-blue-500/10 text-blue-700 dark:text-blue-400 border border-blue-500/20"
                          : u.role === "vendor"
                          ? "bg-purple-500/10 text-purple-700 dark:text-purple-400 border border-purple-500/20"
                          : u.role === "admin" || u.role === "super_admin" || u.role === "dispatcher"
                          ? "bg-amber-500/15 text-amber-800 dark:text-[#FFB800] border border-amber-500/30"
                          : "bg-gray-100 dark:bg-white/10 text-gray-700 dark:text-gray-300"
                      }`}>
                        {u.role || "Customer"}
                      </span>
                    </td>

                    {/* Financials & Deliveries */}
                    <td className="p-3.5">
                      <div className="flex flex-col gap-0.5">
                        <span className="font-black text-gray-900 dark:text-white text-xs">
                          {fmt(u.walletBalance || 0)}
                        </span>
                        <div className="flex items-center gap-2 text-[10px] text-gray-500 dark:text-gray-400">
                          <span>{u.deliveryCount || 0} orders</span>
                          <span>•</span>
                          <span className="flex items-center gap-0.5 text-amber-800 dark:text-[#FFB800] font-bold">
                            <Star className="w-2.5 h-2.5 fill-current" /> {u.rating ? u.rating.toFixed(1) : "5.0"}
                          </span>
                        </div>
                      </div>
                    </td>

                    {/* Status & Real Presence */}
                    <td className="p-3.5">
                      <div className="flex flex-col gap-1 items-start">
                        {/* Device Presence */}
                        {u.isOnline ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[9px] font-black bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 shadow-2xs">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                            ONLINE
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[9px] font-bold bg-gray-100 dark:bg-white/10 text-gray-500 dark:text-gray-400">
                            <span className="w-1.5 h-1.5 rounded-full bg-zinc-400" />
                            OFFLINE
                          </span>
                        )}
                        {/* Account Status */}
                        <span className={`text-[9px] font-extrabold uppercase tracking-wider px-2 py-0.2 rounded ${
                          u.status === "suspended" 
                            ? "bg-red-500/10 text-red-600 border border-red-500/20" 
                            : u.status === "pending"
                            ? "bg-amber-500/10 text-amber-600 border border-amber-500/20"
                            : "text-gray-500 dark:text-gray-400"
                        }`}>
                          {u.status === "suspended" ? "SUSPENDED" : u.status === "pending" ? "PENDING REVIEW" : "ACTIVE"}
                        </span>
                      </div>
                    </td>

                    {/* Right-aligned Actions Suite */}
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          title="View Full Profile"
                          onClick={() => setPreviewUser(u)}
                          className="p-2 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/30 rounded-xl transition-all cursor-pointer"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          title="Fund / Adjust Wallet"
                          onClick={() => {
                            setFundUser(u);
                            setFundAmount("");
                            setFundReason("");
                          }}
                          className="p-2 text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 rounded-xl transition-all cursor-pointer"
                        >
                          <DollarSign className="w-4 h-4" />
                        </button>
                        {u.role !== "vendor" && u.role !== "admin" && u.role !== "super_admin" && (
                          <button
                            title="Upgrade to Vendor Store"
                            onClick={() => promoteToVendor(u)}
                            className="p-2 text-amber-800 dark:text-[#FFB800] hover:bg-amber-50 dark:hover:bg-white/5 rounded-xl transition-all cursor-pointer"
                          >
                            <Store className="w-4 h-4" />
                          </button>
                        )}
                        <button
                          title="Edit User"
                          onClick={() => {
                            setEditUser(u);
                            setForm({
                              name: u.name,
                              role: u.role || "customer",
                              phone: u.phone || "",
                              bikeNumber: u.bikeNumber || "",
                              staffId: u.staffId || "",
                              status: u.status || "active"
                            });
                          }}
                          className="p-2 text-gray-700 dark:text-gray-300 hover:bg-black/5 dark:hover:bg-white/5 rounded-xl transition-all cursor-pointer"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        <button
                          title="Permanently Delete User"
                          onClick={() => setShowDeleteModal({ mode: "single", user: u })}
                          className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-xl transition-all cursor-pointer"
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

        {/* Pagination */}
        {uTotalPages > 1 && (
          <div className="flex items-center justify-between pt-5 border-t border-black/5 dark:border-white/10 mt-4 flex-wrap gap-2">
            <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
              Showing page {uPage + 1} of {uTotalPages} ({filtered.length} matching users)
            </span>
            <div className="flex items-center gap-1.5">
              <button
                onClick={() => setUPage(p => Math.max(0, p - 1))}
                disabled={uPage === 0}
                className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] cursor-pointer flex items-center gap-1"
              >
                <ChevronLeft size={14} /> Prev
              </button>
              {Array.from({ length: Math.min(5, uTotalPages) }, (_, i) => {
                let pNum = i;
                if (uTotalPages > 5 && uPage > 2) {
                  pNum = Math.min(uTotalPages - 5 + i, uPage - 2 + i);
                }
                return (
                  <button
                    key={pNum}
                    onClick={() => setUPage(pNum)}
                    className={`w-8 h-8 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                      pNum === uPage
                        ? "bg-[#FFB800] text-[#111] shadow-xs"
                        : "bg-gray-100 dark:bg-[#222] text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#333]"
                    }`}
                  >
                    {pNum + 1}
                  </button>
                );
              })}
              <button
                onClick={() => setUPage(p => Math.min(uTotalPages - 1, p + 1))}
                disabled={uPage >= uTotalPages - 1}
                className="px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-gray-900 dark:text-white border border-gray-200 dark:border-white/10 disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] cursor-pointer flex items-center gap-1"
              >
                Next <ChevronRight size={14} />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Permanent Deletion Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-[#1a1a1a] border border-red-500/30 rounded-3xl p-6 shadow-2xl animate-scale-in space-y-4">
            <div className="flex items-center gap-3 text-red-600 dark:text-red-400">
              <div className="w-12 h-12 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center shrink-0">
                <AlertTriangle className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-black text-gray-900 dark:text-white">
                  {showDeleteModal.mode === "bulk" ? "Permanent Bulk Deletion" : "Permanently Delete Account"}
                </h3>
                <p className="text-xs text-red-500 font-bold mt-0.5">Wipe record from database</p>
              </div>
            </div>

            <div className="p-3.5 rounded-2xl bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/30 text-xs text-gray-700 dark:text-gray-300 leading-relaxed">
              {showDeleteModal.mode === "bulk" ? (
                <p>
                  You are about to permanently remove <b>{showDeleteModal.count} selected user accounts</b> from the Firestore database. This action completely clears their documents and cannot be undone.
                </p>
              ) : (
                <p>
                  Are you sure you want to permanently delete user <b>{showDeleteModal.user?.name}</b> ({showDeleteModal.user?.email || showDeleteModal.user?.phone || showDeleteModal.user?.id})? This will permanently wipe the record from Firestore.
                </p>
              )}
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={deleting}
                onClick={() => setShowDeleteModal(null)}
                className="h-10 px-4 rounded-xl border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-300 hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={deleting}
                onClick={executePermanentDelete}
                className="h-10 px-5 rounded-xl bg-red-600 hover:bg-red-700 disabled:bg-red-600/50 text-white text-xs font-black flex items-center gap-2 shadow-md cursor-pointer"
              >
                {deleting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                <span>{deleting ? "Deleting from Database..." : "Delete Permanently"}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* User Inspector Modal */}
      {previewUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs animate-fade-in">
          <div className="w-full max-w-lg bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl p-6 shadow-2xl animate-scale-in space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-white/10">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-2xl bg-[#FFB800]/20 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black text-sm">
                  {getInitials(previewUser.name)}
                </div>
                <div>
                  <h3 className="text-base font-black text-gray-900 dark:text-white flex items-center gap-2">
                    {previewUser.name}
                    {previewUser.isOnline === true && (
                      <span className="inline-flex items-center gap-1 text-[9px] px-2 py-0.5 rounded-full font-black bg-emerald-500/10 text-emerald-600 border border-emerald-500/20">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                        Online
                      </span>
                    )}
                  </h3>
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-mono mt-0.5">{previewUser.id}</p>
                </div>
              </div>
              <button onClick={() => setPreviewUser(null)} className="p-2 text-gray-400 hover:text-gray-700 dark:hover:text-white rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div className="p-3 bg-gray-50 dark:bg-white/5 rounded-xl border border-gray-200 dark:border-white/5">
                <span className="text-[10px] uppercase font-extrabold text-gray-500 dark:text-gray-400 block">Wallet Balance</span>
                <span className="text-sm font-black text-gray-900 dark:text-white block mt-0.5">{fmt(previewUser.walletBalance || 0)}</span>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-white/5 rounded-xl border border-gray-200 dark:border-white/5">
                <span className="text-[10px] uppercase font-extrabold text-gray-500 dark:text-gray-400 block">Reward Points</span>
                <span className="text-sm font-black text-[#FFB800] block mt-0.5">{previewUser.loyaltyPoints || 0} pts</span>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-white/5 rounded-xl border border-gray-200 dark:border-white/5">
                <span className="text-[10px] uppercase font-extrabold text-gray-500 dark:text-gray-400 block">Completed Orders</span>
                <span className="text-sm font-black text-gray-900 dark:text-white block mt-0.5">{previewUser.deliveryCount || 0}</span>
              </div>
            </div>

            <div className="space-y-2 text-xs border-t border-gray-100 dark:border-white/10 pt-3">
              <div className="flex justify-between py-1 border-b border-gray-100 dark:border-white/5">
                <span className="text-gray-500 dark:text-gray-400 font-medium">Email Address</span>
                <span className="font-bold text-gray-900 dark:text-white">{previewUser.email || "None"}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-gray-100 dark:border-white/5">
                <span className="text-gray-500 dark:text-gray-400 font-medium">Phone Number</span>
                <span className="font-bold text-gray-900 dark:text-white">{previewUser.phone || "None"}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-gray-100 dark:border-white/5">
                <span className="text-gray-500 dark:text-gray-400 font-medium">Assigned Role</span>
                <span className="font-bold uppercase text-[#FFB800]">{previewUser.role || "Customer"}</span>
              </div>
              {previewUser.staffId && (
                <div className="flex justify-between py-1 border-b border-gray-100 dark:border-white/5">
                  <span className="text-gray-500 dark:text-gray-400 font-medium">Official Staff ID</span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">{previewUser.staffId}</span>
                </div>
              )}
              {previewUser.bikeNumber && (
                <div className="flex justify-between py-1 border-b border-gray-100 dark:border-white/5">
                  <span className="text-gray-500 dark:text-gray-400 font-medium">Vehicle / Bike Number</span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">{previewUser.bikeNumber}</span>
                </div>
              )}
              <div className="flex justify-between py-1">
                <span className="text-gray-500 dark:text-gray-400 font-medium">Live Telemetry</span>
                <span className="font-bold text-gray-900 dark:text-white">
                  {previewUser.isOnline === true ? (
                    <span className="text-emerald-500 font-black flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" /> ONLINE
                    </span>
                  ) : (
                    <span className="text-gray-400">Offline</span>
                  )}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 pt-2 border-t border-gray-100 dark:border-white/10">
              <button
                type="button"
                onClick={() => {
                  const u = previewUser;
                  setPreviewUser(null);
                  setFundUser(u);
                }}
                className="h-9 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 cursor-pointer shadow-xs"
              >
                <DollarSign className="w-3.5 h-3.5" /> Fund Wallet
              </button>
              <button
                type="button"
                onClick={() => {
                  const u = previewUser;
                  setPreviewUser(null);
                  setEditUser(u);
                  setForm({
                    name: u.name,
                    role: u.role || "customer",
                    phone: u.phone || "",
                    bikeNumber: u.bikeNumber || "",
                    staffId: u.staffId || "",
                    status: u.status || "active"
                  });
                }}
                className="h-9 px-4 rounded-xl bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black text-xs flex items-center gap-1.5 cursor-pointer shadow-xs"
              >
                <Edit3 className="w-3.5 h-3.5" /> Edit Account
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Luxury Obsidian/Gold Fund Wallet Modal */}
      {fundUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-xs animate-fade-in">
          <div className="w-full max-w-md bg-[#161616] text-white border border-[#FFB800]/30 rounded-3xl p-6 shadow-2xl animate-scale-in space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#FFB800]/20 text-[#FFB800] border border-[#FFB800]/40 flex items-center justify-center font-black">
                  <DollarSign className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-black text-white">Adjust Wallet Balance</h3>
                  <p className="text-[11px] text-gray-400 font-medium">Direct ledger credit or debit</p>
                </div>
              </div>
              <button onClick={() => setFundUser(null)} className="p-2 text-gray-400 hover:text-white rounded-xl hover:bg-white/5 cursor-pointer">
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Target User Summary Card */}
            <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-[#FFB800]/15 text-[#FFB800] font-black text-xs flex items-center justify-center">
                  {getInitials(fundUser.name)}
                </div>
                <div>
                  <p className="text-xs font-black text-white">{fundUser.name}</p>
                  <p className="text-[10px] text-gray-400 truncate">{fundUser.email || fundUser.phone}</p>
                </div>
              </div>
              <div className="text-right">
                <span className="text-[10px] text-gray-400 font-medium block">Current Balance</span>
                <span className="text-sm font-black text-[#FFB800]">{fmt(fundUser.walletBalance || 0)}</span>
              </div>
            </div>

            {/* Credit / Debit Segmented Toggle */}
            <div className="grid grid-cols-2 p-1 bg-black/40 border border-white/10 rounded-xl">
              <button
                type="button"
                onClick={() => setFundAction("credit")}
                className={`py-2 rounded-lg text-xs font-black transition-all cursor-pointer ${
                  fundAction === "credit"
                    ? "bg-emerald-600 text-white shadow-xs"
                    : "text-gray-400 hover:text-white"
                }`}
              >
                + Credit Account
              </button>
              <button
                type="button"
                onClick={() => setFundAction("debit")}
                className={`py-2 rounded-lg text-xs font-black transition-all cursor-pointer ${
                  fundAction === "debit"
                    ? "bg-red-600 text-white shadow-xs"
                    : "text-gray-400 hover:text-white"
                }`}
              >
                - Debit Account
              </button>
            </div>

            {/* Amount Input with clear left icon and pl-12 */}
            <div>
              <label className="block text-[10px] font-extrabold text-gray-300 mb-1.5 uppercase tracking-wider">
                Transaction Amount (₦)
              </label>
              <div className="relative">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-sm font-black text-[#FFB800] pointer-events-none">
                  ₦
                </div>
                <input
                  type="number"
                  min="1"
                  step="any"
                  placeholder="0.00"
                  value={fundAmount}
                  onChange={e => setFundAmount(e.target.value)}
                  className="w-full h-11 bg-white/5 border border-white/15 rounded-xl pl-12 pr-4 text-sm font-bold text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50"
                  autoFocus
                />
              </div>
            </div>

            {/* Quick Preset Chips */}
            <div className="space-y-1.5">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">Quick Presets</span>
              <div className="grid grid-cols-4 gap-1.5">
                {[1000, 2500, 5000, 10000, 25000, 50000, 100000].map(amt => (
                  <button
                    key={amt}
                    type="button"
                    onClick={() => setFundAmount(String(amt))}
                    className="py-1.5 px-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-[11px] font-bold text-gray-200 hover:text-[#FFB800] transition-colors cursor-pointer"
                  >
                    +{fmt(amt).replace("₦", "")}
                  </button>
                ))}
                <button
                  type="button"
                  onClick={() => setFundAmount("")}
                  className="py-1.5 px-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-[11px] font-bold text-gray-400 hover:text-white transition-colors cursor-pointer"
                >
                  Clear
                </button>
              </div>
            </div>

            {/* Narration Input */}
            <div>
              <label className="block text-[10px] font-extrabold text-gray-300 mb-1 uppercase tracking-wider">
                Audit Narration & Reason
              </label>
              <input
                type="text"
                placeholder={fundAction === "credit" ? "e.g. Operational promo bonus" : "e.g. Overdraft correction"}
                value={fundReason}
                onChange={e => setFundReason(e.target.value)}
                className="w-full h-10 bg-white/5 border border-white/15 rounded-xl px-3.5 text-xs text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50"
              />
            </div>

            {/* Projected Live Balance Card */}
            {parseFloat(fundAmount) > 0 && (
              <div className="p-3 bg-black/40 border border-[#FFB800]/30 rounded-xl flex items-center justify-between text-xs animate-fade-in">
                <span className="text-gray-400 font-medium">Projected Balance:</span>
                <span className="font-black text-white text-sm">
                  {fmt(
                    Math.max(
                      0,
                      (fundUser.walletBalance || 0) + (fundAction === "credit" ? parseFloat(fundAmount) : -parseFloat(fundAmount))
                    )
                  )}
                </span>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex items-center justify-end gap-3 pt-2 border-t border-white/10">
              <button
                type="button"
                onClick={() => setFundUser(null)}
                className="h-10 px-4 rounded-xl border border-white/15 text-xs font-bold text-gray-300 hover:bg-white/5 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={fundingWallet || !parseFloat(fundAmount)}
                onClick={handleFundWallet}
                className={`h-10 px-5 rounded-xl font-black text-xs flex items-center gap-2 shadow-lg cursor-pointer transition-all ${
                  fundAction === "credit"
                    ? "bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111]"
                    : "bg-red-600 hover:bg-red-700 text-white"
                }`}
              >
                {fundingWallet ? <RefreshCw className="w-4 h-4 animate-spin" /> : <DollarSign className="w-4 h-4" />}
                <span>{fundingWallet ? "Executing..." : fundAction === "credit" ? "Credit Balance" : "Debit Balance"}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      {editUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl p-6 shadow-2xl animate-scale-in space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-white/10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black">
                  <Edit3 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-black text-gray-900 dark:text-white">Edit User Profile</h3>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400 font-mono truncate">{editUser.email || editUser.id}</p>
                </div>
              </div>
              <button onClick={() => setEditUser(null)} className="p-2 text-gray-400 hover:text-gray-700 dark:hover:text-white rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Full Name</label>
                <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
              </div>
              <div>
                <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Phone Number</label>
                <input value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                  className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
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
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Staff ID</label>
                  <input value={form.staffId} onChange={e => setForm(f => ({ ...f, staffId: e.target.value.toUpperCase() }))} placeholder="e.g. ESD-ADM-001"
                    className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 font-mono" />
                </div>
                <div>
                  <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">Vehicle / Bike Number</label>
                  <input value={form.bikeNumber} onChange={e => setForm(f => ({ ...f, bikeNumber: e.target.value }))} placeholder="e.g. ES-BIKE-204"
                    className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 font-mono" />
                </div>
              </div>
            </div>
            <div className="flex items-center justify-end gap-3 pt-2">
              <button onClick={() => setEditUser(null)} className="h-10 px-4 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">Cancel</button>
              <SaveBtn onClick={saveUser} label="Update User" />
            </div>
          </div>
        </div>
      )}
    </div>
  );
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
    const [overrideReason, setOverrideReason] = useState("");
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
        if (addToast) addToast("error", `Cannot change status from ${del.status} to ${s}. Invalid transition.`);
        else alert(`Cannot change status from ${del.status} to ${s}. Invalid transition.`);
        return;
      }

      if (requiresRider(s) && !del.riderId && !del.driverId) {
        if (addToast) addToast("error", `Cannot set status to ${s} without assigning a rider first.`);
        else alert(`Cannot set status to ${s} without assigning a rider first.`);
        return;
      }

      setConfirmStatusModal({ delivery: del, newStatus: s, show: true });
    };

    const confirmUpdateStatus = async () => {
      const { delivery, newStatus } = confirmStatusModal;
      if (!delivery || !newStatus) return;

      try {
        const updatePayload: any = { status: newStatus, updatedAt: Timestamp.now() };
        if (newStatus === "DELIVERED" && !delivery.otpVerified) {
          updatePayload.adminOverrideReason = overrideReason.trim() || "Administrative manual verification";
          updatePayload.adminOverrideAt = Timestamp.now();
        }
        await updateDoc(doc(db, "deliveries", delivery.id), updatePayload);
        addLog("Status", `${idShort(delivery.id)} -> ${newStatus}${updatePayload.adminOverrideReason ? ` (Override: ${updatePayload.adminOverrideReason})` : ""}`);
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
        if (addToast) addToast("error", "Failed to update status. Please try again.");
        else alert("Failed to update status. Please try again.");
      }
      setOverrideReason("");
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
        const msg = `Invalid transitions for ${invalidTransitions.length} shipment(s): ${invalidTransitions.slice(0, 3).join(", ")}${invalidTransitions.length > 3 ? "..." : ""}`;
        if (addToast) addToast("error", msg);
        else alert(msg);
        return;
      }

      if (missingRiders.length > 0) {
        const msg = `${missingRiders.length} shipment(s) have no rider assigned. Cannot set to ${bulkStatus}.`;
        if (addToast) addToast("error", msg);
        else alert(msg);
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
        if (addToast) addToast("error", "Please provide Sender Name, Pickup Address, Receiver Name, and Delivery Address.");
        else alert("Please provide Sender Name, Pickup Address, Receiver Name, and Delivery Address.");
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
              addLog("Status Update", `Shipment #${idShort(delId)} status updated to ${newStatus.toUpperCase()}`, "Shipments");
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
                className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-xs transition-all flex items-center justify-center gap-1.5 shrink-0 cursor-pointer"
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
          <div className="overflow-x-auto max-h-[700px]">
            <table className="w-full text-xs">
              <thead className="bg-gray-100 dark:bg-[#222] border-b border-gray-200 dark:border-white/10 sticky top-0 z-10 backdrop-blur-md">
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
                        <p className="font-bold text-gray-900 dark:text-white flex items-center gap-1.5 flex-wrap">
                          {d.itemName || "Parcel"}
                          {d.category && <span className="px-2 py-0.5 rounded-full text-[9px] font-extrabold bg-gray-100 dark:bg-[#333] text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10">{d.category}</span>}
                          {d.additionalStops && d.additionalStops.startsWith("batch:") && (
                            <span className="px-2 py-0.5 rounded-full text-[9px] font-black bg-amber-500/20 text-amber-800 dark:text-[#FFB800] border border-amber-500/30 flex items-center gap-1">
                              <Layers size={10} /> BATCH RUN
                            </span>
                          )}
                          {(d as any).adminOverrideReason && (
                            <span className="px-1.5 py-0.5 rounded text-[8px] font-black bg-red-500/15 text-red-700 dark:text-red-400 border border-red-500/30" title={`Manual Override: ${(d as any).adminOverrideReason}`}>
                              OVERRIDE
                            </span>
                          )}
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
                      className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-[#FFB800]/40 outline-none"
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
                        className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Sender Phone *</label>
                      <input
                        type="tel"
                        placeholder="080XXXXXXXX"
                        value={newForm.senderPhone}
                        onChange={e => setNewForm({ ...newForm, senderPhone: e.target.value })}
                        className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                      className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                        className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Receiver Phone *</label>
                      <input
                        type="tel"
                        placeholder="080XXXXXXXX"
                        value={newForm.receiverPhone}
                        onChange={e => setNewForm({ ...newForm, receiverPhone: e.target.value })}
                        className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                      className="w-full h-9 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                      className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-[#111] dark:text-white outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Quantity</label>
                    <input
                      type="number"
                      min="1"
                      value={newForm.quantity}
                      onChange={e => setNewForm({ ...newForm, quantity: Number(e.target.value) })}
                      className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-[#111] dark:text-white outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                      className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-[#111] dark:text-white outline-none font-bold focus:ring-2 focus:ring-[#FFB800]/40"
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
                  className="h-10 px-4 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={createDelivery}
                  disabled={creating}
                  className="h-10 px-5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black transition-all shadow-xs flex items-center gap-1.5 disabled:opacity-50 cursor-pointer"
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
                  <Search size={14} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400 pointer-events-none" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-12 pr-3.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setRiderOnlineOnly(!riderOnlineOnly)}
                  className={"h-10 px-3.5 rounded-xl text-xs font-bold border transition-colors shrink-0 flex items-center justify-center cursor-pointer " + (riderOnlineOnly ? "bg-emerald-600 text-white border-emerald-600" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#2a2a2a]")}
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
                                <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline === true ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline === true ? "Online" : "Offline"} />
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
                  <Search size={14} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400 pointer-events-none" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-12 pr-3.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setRiderOnlineOnly(!riderOnlineOnly)}
                  className={"h-10 px-3.5 rounded-xl text-xs font-bold border transition-colors shrink-0 flex items-center justify-center cursor-pointer " + (riderOnlineOnly ? "bg-emerald-600 text-white border-emerald-600" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 border-gray-200 dark:border-white/10 hover:bg-gray-200 dark:hover:bg-[#2a2a2a]")}
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
                                <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline === true ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline === true ? "Online" : "Offline"} />
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
                  <Search size={14} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400 pointer-events-none" />
                  <input
                    type="text"
                    placeholder="Search rider name, phone, bike #..."
                    value={riderSearch}
                    onChange={e => setRiderSearch(e.target.value)}
                    className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl pl-12 pr-3.5 text-xs text-[#111] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
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
                              <span className={"w-2 h-2 rounded-full shrink-0 " + (r.isOnline === true ? "bg-emerald-500" : "bg-gray-400")} title={r.isOnline === true ? "Online" : "Offline"} />
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

                {confirmStatusModal.newStatus === "DELIVERED" && !confirmStatusModal.delivery?.otpVerified && (
                  <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-2xl space-y-2 text-left mt-2">
                    <div className="flex items-center gap-2 text-amber-800 dark:text-[#FFB800] font-black text-xs">
                      <ShieldAlert size={15} /> Handover PIN Not Verified
                    </div>
                    <p className="text-[11px] text-gray-700 dark:text-gray-300 font-medium">
                      Recipient 4-digit Handover PIN was not entered by courier. To force status to DELIVERED, enter an administrative justification:
                    </p>
                    <textarea
                      value={overrideReason}
                      onChange={e => setOverrideReason(e.target.value)}
                      placeholder="e.g. Recipient confirmed safe delivery by phone call; courier phone battery drained."
                      rows={2}
                      className="w-full p-2.5 bg-white dark:bg-[#111] border border-black/10 dark:border-white/10 rounded-xl text-xs font-medium text-[#111] dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50"
                      required
                    />
                  </div>
                )}
              </div>
              <div className="flex items-center justify-end gap-3 pt-2">
                <button onClick={() => { setOverrideReason(""); setConfirmStatusModal({ delivery: null as any, newStatus: "", show: false }); }} className="px-4 py-2.5 min-h-[38px] bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors cursor-pointer">Cancel</button>
                <button
                  disabled={confirmStatusModal.newStatus === "DELIVERED" && !confirmStatusModal.delivery?.otpVerified && !overrideReason.trim()}
                  onClick={confirmUpdateStatus}
                  className="px-4 py-2.5 min-h-[38px] bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black transition-colors cursor-pointer disabled:opacity-50"
                >
                  Confirm Change
                </button>
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

function ReferralsTab({ referrals, completedReferrals, searchQuery, addToast }: ReferralsTabProps & { addToast?: (type: Toast["type"], message: string) => void }) {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [rPage, setRPage] = useState(0);
  const rPerPage = 10;

  const totalReferrals = referrals.length;
  const completedCount = completedReferrals.length;
  const pendingCount = referrals.filter(r => r.status?.toLowerCase() !== "completed").length;
  const totalRewardsPool = referrals.reduce((s, r) => s + (r.rewardAmount || 0), 0);
  const totalPaidOut = completedReferrals.reduce((s, r) => s + (r.rewardAmount || 0), 0);

  const filtered = useMemo(() => {
    const q = (searchQuery || search).toLowerCase().trim();
    return referrals.filter(r => {
      // Status filter
      if (statusFilter === "COMPLETED" && r.status?.toLowerCase() !== "completed") return false;
      if (statusFilter === "PENDING" && r.status?.toLowerCase() === "completed") return false;

      // Search query
      if (!q) return true;
      return (
        (r.referrerName && r.referrerName.toLowerCase().includes(q)) ||
        (r.referrerEmail && r.referrerEmail.toLowerCase().includes(q)) ||
        (r.refereeName && r.refereeName.toLowerCase().includes(q)) ||
        (r.refereeEmail && r.refereeEmail.toLowerCase().includes(q)) ||
        (r.id && r.id.toLowerCase().includes(q))
      );
    });
  }, [referrals, searchQuery, search, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / rPerPage));
  const pagedReferrals = filtered.slice(rPage * rPerPage, (rPage + 1) * rPerPage);

  useEffect(() => {
    setRPage(0);
  }, [search, searchQuery, statusFilter]);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    if (addToast) addToast("success", "Referral ID copied to clipboard");
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    const parts = name.trim().split(" ");
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <div className="tab-content space-y-6">
      {/* Top Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-[#FFB800]/15 flex items-center justify-center border border-[#FFB800]/30 text-[#FFB800]">
              <Gift className="w-4 h-4" />
            </div>
            Referrals & Growth Rewards
          </h1>
          <p className="text-xs text-gray-500 dark:text-gray-400 font-medium mt-1">
            Track user invitations, referee conversion funnels, and automated wallet incentives.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <SearchInput value={search} onChange={setSearch} placeholder="Search referrer or referee..." />
        </div>
      </div>

      {/* 4 Spacious Executive Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Total Referrals */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs relative overflow-hidden group hover:border-[#FFB800]/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-gray-500 dark:text-gray-400">Total Referrals</span>
            <div className="w-9 h-9 rounded-xl bg-[#FFB800]/10 border border-[#FFB800]/20 flex items-center justify-center text-[#FFB800]">
              <Gift className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-black text-gray-900 dark:text-white tracking-tight">{totalReferrals}</p>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium mt-1">
            Total tracked invite events
          </p>
        </div>

        {/* Card 2: Completed & Rewarded */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs relative overflow-hidden group hover:border-emerald-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-gray-500 dark:text-gray-400">Completed & Paid</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-500">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-black text-emerald-600 dark:text-emerald-400 tracking-tight">{completedCount}</p>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium mt-1">
            First order completed & verified
          </p>
        </div>

        {/* Card 3: Pending Conversions */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs relative overflow-hidden group hover:border-amber-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-gray-500 dark:text-gray-400">Pending Conversions</span>
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-500">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-black text-amber-600 dark:text-amber-400 tracking-tight">{pendingCount}</p>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium mt-1">
            Invited, awaiting first booking
          </p>
        </div>

        {/* Card 4: Total Rewards Disbursed */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs relative overflow-hidden group hover:border-[#FFB800]/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-gray-500 dark:text-gray-400">Rewards Disbursed</span>
            <div className="w-9 h-9 rounded-xl bg-[#FFB800]/10 border border-[#FFB800]/20 flex items-center justify-center text-[#FFB800]">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-black text-gray-900 dark:text-white tracking-tight">{fmt(totalPaidOut)}</p>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium mt-1">
            Pool: {fmt(totalRewardsPool)} committed
          </p>
        </div>
      </div>

      {/* Filter Tabs & Count Toolbar */}
      <div className="flex items-center justify-between flex-wrap gap-3 pt-2">
        <div className="flex items-center gap-2 overflow-x-auto pb-1 max-w-full">
          {[
            { id: "ALL", label: "All Referrals", count: totalReferrals },
            { id: "COMPLETED", label: "Completed & Paid", count: completedCount },
            { id: "PENDING", label: "Pending", count: pendingCount }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setStatusFilter(tab.id)}
              className={`h-9 px-3.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer whitespace-nowrap ${
                statusFilter === tab.id
                  ? "bg-[#FFB800] text-[#111] shadow-xs font-black"
                  : "bg-white dark:bg-[#1a1a1a] text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-white/10 hover:border-gray-300 dark:hover:border-white/20"
              }`}
            >
              <span>{tab.label}</span>
              <span
                className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                  statusFilter === tab.id
                    ? "bg-black/20 text-[#111] font-black"
                    : "bg-gray-100 dark:bg-white/10 text-gray-600 dark:text-gray-400"
                }`}
              >
                {tab.count}
              </span>
            </button>
          ))}
        </div>

        <div className="text-xs text-gray-500 dark:text-gray-400 font-medium">
          Showing <span className="font-bold text-gray-900 dark:text-white">{filtered.length}</span> results
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl shadow-xs overflow-hidden">
        {filtered.length === 0 ? (
          <div className="p-12 text-center">
            <div className="w-14 h-14 rounded-2xl bg-[#FFB800]/10 border border-[#FFB800]/20 flex items-center justify-center text-[#FFB800] mx-auto mb-3">
              <Gift className="w-7 h-7" />
            </div>
            <h3 className="text-sm font-bold text-gray-900 dark:text-white mb-1">No referrals match your filter</h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto mb-4">
              Try adjusting your search terms or status filter to view other tracked invitations.
            </p>
            <button
              onClick={() => { setSearch(""); setStatusFilter("ALL"); }}
              className="px-4 py-2 bg-gray-100 dark:bg-white/10 hover:bg-gray-200 dark:hover:bg-white/15 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold transition-all cursor-pointer"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222]">
                <tr>
                  <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10">
                    Referrer (Inviter)
                  </th>
                  <th className="text-center font-extrabold text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10 hidden sm:table-cell w-12">
                    Flow
                  </th>
                  <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10">
                    Referee (Invited User)
                  </th>
                  <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10 hidden md:table-cell">
                    Reward Incentive
                  </th>
                  <th className="text-left font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10">
                    Status
                  </th>
                  <th className="text-right font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider text-[10px] p-4 border-b border-gray-200 dark:border-white/10 w-24">
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-white/10">
                {pagedReferrals.map((r) => {
                  const isDone = r.status?.toLowerCase() === "completed";
                  return (
                    <tr key={r.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                      {/* Referrer Column */}
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black text-xs shrink-0 shadow-xs">
                            {getInitials(r.referrerName)}
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-1.5">
                              <p className="font-black text-xs text-[#111] dark:text-white truncate">{r.referrerName || "Unnamed User"}</p>
                              <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-gray-100 dark:bg-white/10 text-gray-600 dark:text-gray-400">
                                Referrer
                              </span>
                            </div>
                            <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium truncate mt-0.5">{r.referrerEmail || "No email"}</p>
                          </div>
                        </div>
                      </td>

                      {/* Direction Flow */}
                      <td className="p-4 text-center hidden sm:table-cell">
                        <div className="w-7 h-7 rounded-full bg-gray-100 dark:bg-white/5 flex items-center justify-center mx-auto text-gray-400">
                          <ChevronRight className="w-3.5 h-3.5" />
                        </div>
                      </td>

                      {/* Referee Column */}
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-gray-100 dark:bg-[#252525] text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-white/10 flex items-center justify-center font-bold text-xs shrink-0">
                            {getInitials(r.refereeName)}
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-1.5">
                              <p className="font-black text-xs text-[#111] dark:text-white truncate">{r.refereeName || "New Customer"}</p>
                              <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-[#FFB800]/10 text-[#FFB800]">
                                New Joiner
                              </span>
                            </div>
                            <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium truncate mt-0.5">{r.refereeEmail || "No email"}</p>
                          </div>
                        </div>
                      </td>

                      {/* Reward Amount */}
                      <td className="p-4 hidden md:table-cell">
                        <div className="flex flex-col">
                          <span className="font-black text-xs text-gray-900 dark:text-white flex items-center gap-1.5">
                            <span className="text-[#FFB800]">₦</span>
                            {Number(r.rewardAmount || 0).toLocaleString("en-US")}
                          </span>
                          <span className="text-[10px] text-gray-500 dark:text-gray-400 font-medium">
                            {isDone ? "Settled to wallet" : "Pending qualification"}
                          </span>
                        </div>
                      </td>

                      {/* Status Pill */}
                      <td className="p-4">
                        <span
                          className={`inline-flex items-center gap-1 text-[10px] font-extrabold px-2.5 py-1 rounded-full border ${
                            isDone
                              ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20"
                              : "bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-500/20"
                          }`}
                        >
                          {isDone ? <CheckCircle2 className="w-3 h-3" /> : <Clock className="w-3 h-3" />}
                          {isDone ? "COMPLETED" : "PENDING ORDER"}
                        </span>
                      </td>

                      {/* Copy Action */}
                      <td className="p-4 text-right">
                        <button
                          onClick={() => copyToClipboard(r.id, r.id)}
                          title="Copy Referral ID"
                          className="w-8 h-8 rounded-xl bg-gray-100 dark:bg-white/10 hover:bg-gray-200 dark:hover:bg-white/15 text-gray-700 dark:text-gray-300 flex items-center justify-center transition-colors cursor-pointer ml-auto"
                        >
                          {copiedId === r.id ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-gray-100 dark:border-white/10 flex items-center justify-between flex-wrap gap-3">
            <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
              Page <span className="font-bold text-gray-900 dark:text-white">{rPage + 1}</span> of{" "}
              <span className="font-bold text-gray-900 dark:text-white">{totalPages}</span> ({filtered.length} referrals)
            </span>
            <div className="flex items-center gap-1.5">
              <button
                onClick={() => setRPage((p) => Math.max(0, p - 1))}
                disabled={rPage === 0}
                className="px-3 py-1.5 rounded-xl border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-300 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
              >
                Previous
              </button>
              <button
                onClick={() => setRPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={rPage >= totalPages - 1}
                className="px-3 py-1.5 rounded-xl border border-gray-200 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-300 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
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

function SettingsTab({ db, addLog, addToast, activeUsers }: SettingsTabProps) {
  const [sForm, setSForm] = useState<any>({});
  const [fcmKey, setFcmKey] = useState("");
  const [showFcm, setShowFcm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editingStaff, setEditingStaff] = useState<UserProfile | null>(null);
  const [staffForm, setStaffForm] = useState({ name: "", staffId: "", role: "admin", phone: "", status: "active" });
  const [savingStaff, setSavingStaff] = useState(false);

  const staffMembers = useMemo(() => {
    return (activeUsers || []).filter(u => u.role === "admin" || u.role === "super_admin" || u.role === "dispatcher");
  }, [activeUsers]);

  const handleSaveStaff = async () => {
    if (!editingStaff) return;
    setSavingStaff(true);
    try {
      await updateDoc(doc(db, "users", editingStaff.id), {
        name: staffForm.name,
        staffId: staffForm.staffId.toUpperCase().trim(),
        role: staffForm.role,
        phone: staffForm.phone,
        status: staffForm.status,
        updatedAt: Timestamp.now()
      });
      addLog("Staff Profile Update", `Updated credentials for ${staffForm.name} [${staffForm.staffId}]`, "Staff");
      addToast?.("success", "Staff profile updated successfully");
      setEditingStaff(null);
    } catch (e: any) {
      addToast?.("error", "Failed to update staff: " + e.message);
    }
    setSavingStaff(false);
  };
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
        <Toggle label="QR Code Delivery Handover" desc="Display QR code alongside 4-digit PIN for parcel handover verification (disabled by default for direct PIN entry)" checked={!!sForm.enableQrCodeHandover} onChange={v => upd("enableQrCodeHandover", v)} />
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
          <input type="number" value={sForm.baseFare ?? ""} onChange={e => upd("baseFare", parseFloat(e.target.value) || 0)} className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">PER KG RATE (₦)</label>
          <input type="number" step="0.1" value={sForm.perKgRate ?? ""} onChange={e => upd("perKgRate", parseFloat(e.target.value) || 0)} className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">EXPRESS SURCHARGE (₦)</label>
          <input type="number" value={sForm.expressSurcharge ?? ""} onChange={e => upd("expressSurcharge", parseFloat(e.target.value) || 0)} className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
        <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">SURGE MULTIPLIER (×)</label>
          <input type="number" step="0.1" min="1" value={sForm.surgeMultiplier ?? ""} onChange={e => upd("surgeMultiplier", parseFloat(e.target.value) || 1)} className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
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

    {/* Section 7 — Administrative Staff & Sub-Admins */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs space-y-4">
      <div className="flex items-center justify-between flex-wrap gap-3 pb-2 border-b border-gray-100 dark:border-white/10">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-5 h-5 text-[#FFB800]" />
          <div>
            <span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Administrative Staff & Sub-Admins</span>
            <p className="text-[10px] text-gray-500 dark:text-gray-400 font-medium">Manage corporate identities, Staff IDs, access roles, and permissions</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-white/10 px-2.5 py-1 rounded-xl">
            {staffMembers.length} Officer(s)
          </span>
        </div>
      </div>

      <div className="overflow-x-auto rounded-2xl border border-gray-100 dark:border-white/10">
        <table className="w-full text-xs">
          <thead className="bg-gray-50 dark:bg-[#222]">
            <tr className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              <th className="p-3 text-left">Staff Member</th>
              <th className="p-3 text-left">Staff ID</th>
              <th className="p-3 text-left">Role / Permission</th>
              <th className="p-3 text-left">Phone</th>
              <th className="p-3 text-left">Status</th>
              <th className="p-3 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 dark:divide-white/10">
            {staffMembers.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-6 text-center text-gray-500 font-medium">
                  No administrative personnel records found.
                </td>
              </tr>
            ) : (
              staffMembers.map(staff => (
                <tr key={staff.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                  <td className="p-3">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-xl bg-[#FFB800]/15 text-[#FFB800] border border-[#FFB800]/30 flex items-center justify-center font-black text-[11px]">
                        {(staff.name || "A").slice(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <p className="font-bold text-[#111] dark:text-white text-xs">{staff.name || "Unnamed Staff"}</p>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">{staff.email || "No email"}</p>
                      </div>
                    </div>
                  </td>
                  <td className="p-3">
                    <span className="font-mono text-[11px] font-bold px-2 py-0.5 rounded-lg bg-black/5 dark:bg-white/10 text-gray-800 dark:text-gray-200 border border-black/10 dark:border-white/10">
                      {staff.staffId || "UNASSIGNED"}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className={`text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider ${
                      staff.role === "super_admin"
                        ? "bg-[#FFB800] text-[#111]"
                        : staff.role === "dispatcher"
                        ? "bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-500/20"
                        : "bg-blue-100 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 border border-blue-500/20"
                    }`}>
                      {staff.role.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="p-3 font-mono text-gray-600 dark:text-gray-300 text-[11px]">
                    {staff.phone || "—"}
                  </td>
                  <td className="p-3">
                    <span className={`inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      staff.status === "active" || staff.status === "approved" || !staff.status
                        ? "bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300"
                        : "bg-red-100 dark:bg-red-950/40 text-red-700 dark:text-red-300"
                    }`}>
                      <span className={`w-1.5 h-1.5 rounded-full ${staff.status === "suspended" ? "bg-red-500" : "bg-emerald-500"}`} />
                      {staff.status || "active"}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      type="button"
                      onClick={() => {
                        setEditingStaff(staff);
                        setStaffForm({
                          name: staff.name || "",
                          staffId: staff.staffId || "",
                          role: staff.role || "admin",
                          phone: staff.phone || "",
                          status: staff.status || "active"
                        });
                      }}
                      className="h-8 px-3 rounded-xl bg-gray-100 dark:bg-[#222] hover:bg-[#FFB800] hover:text-[#111] text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-white/10 text-xs font-bold transition-all cursor-pointer inline-flex items-center gap-1.5"
                    >
                      <Pencil className="w-3 h-3" />
                      <span>Edit Staff ID</span>
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Edit Staff Modal */}
      {editingStaff && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-fade-in">
          <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/15 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-gray-100 dark:border-white/10">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-[#FFB800]" />
                <h3 className="text-sm font-black text-[#111] dark:text-white">Edit Staff Credentials</h3>
              </div>
              <button
                type="button"
                onClick={() => setEditingStaff(null)}
                className="w-8 h-8 rounded-xl bg-gray-100 dark:bg-[#222] flex items-center justify-center text-gray-500 hover:text-gray-900 dark:hover:text-white cursor-pointer"
              >
                <X size={16} />
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="block text-[10px] font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  value={staffForm.name}
                  onChange={e => setStaffForm(prev => ({ ...prev, name: e.target.value }))}
                  className="w-full h-10 rounded-xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 px-3.5 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                />
              </div>

              <div>
                <label className="block text-[10px] font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-1">
                  Corporate Staff ID
                </label>
                <input
                  type="text"
                  value={staffForm.staffId}
                  onChange={e => setStaffForm(prev => ({ ...prev, staffId: e.target.value.toUpperCase() }))}
                  placeholder="e.g. ESD-ADM-001"
                  className="w-full h-10 rounded-xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 px-3.5 text-xs text-gray-900 dark:text-white font-mono font-bold focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-1">
                    System Role
                  </label>
                  <Select
                    value={staffForm.role}
                    onChange={v => setStaffForm(prev => ({ ...prev, role: v }))}
                    options={[
                      { value: "super_admin", label: "Super Admin" },
                      { value: "admin", label: "Admin" },
                      { value: "dispatcher", label: "Dispatcher" }
                    ]}
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-1">
                    Account Status
                  </label>
                  <Select
                    value={staffForm.status}
                    onChange={v => setStaffForm(prev => ({ ...prev, status: v }))}
                    options={[
                      { value: "active", label: "Active" },
                      { value: "suspended", label: "Suspended" }
                    ]}
                  />
                </div>
              </div>

              <div>
                <label className="block text-[10px] font-extrabold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-1">
                  Phone Number
                </label>
                <input
                  type="text"
                  value={staffForm.phone}
                  onChange={e => setStaffForm(prev => ({ ...prev, phone: e.target.value }))}
                  className="w-full h-10 rounded-xl bg-gray-50 dark:bg-[#222] border border-gray-200 dark:border-white/10 px-3.5 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-gray-100 dark:border-white/10">
              <button
                type="button"
                onClick={() => setEditingStaff(null)}
                className="h-9 px-4 rounded-xl bg-gray-100 dark:bg-white/5 text-gray-700 dark:text-gray-300 text-xs font-bold hover:bg-gray-200 dark:hover:bg-white/10 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSaveStaff}
                disabled={savingStaff}
                className="h-9 px-4 rounded-xl bg-[#FFB800] text-[#111] text-xs font-black hover:bg-[#FFB800]/90 shadow-xs cursor-pointer disabled:opacity-50"
              >
                {savingStaff ? "Saving..." : "Save Credentials"}
              </button>
            </div>
          </div>
        </div>
      )}
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

  // Payout Settlement Modal
  const [settlePayoutTarget, setSettlePayoutTarget] = useState<VendorPayoutRequest | null>(null);
  const [settleBankRef, setSettleBankRef] = useState("");
  const [settleNotes, setSettleNotes] = useState("");
  const [settlingPayout, setSettlingPayout] = useState(false);

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

  return <div className="tab-content space-y-5">
    {/* Header & Metrics */}
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 className="text-xl sm:text-2xl font-extrabold tracking-tight text-[#111] dark:text-white flex items-center gap-3">
          <ShoppingBag className="w-6 h-6 text-[#FFB800]" /> Marketplace & Store Operations
        </h1>
        <p className="text-xs font-medium text-gray-600 dark:text-gray-400 mt-0.5">Manage vendor product catalogs, store enlistments, prices, inventory, and sales commissions.</p>
      </div>
      <div className="flex items-center gap-2.5 flex-wrap">
        {products.length === 0 && (
          <button onClick={seedMarketplace} className="h-10 px-4 bg-[#FFB800]/20 border border-[#FFB800]/40 text-[#111] dark:text-white text-xs font-bold rounded-xl hover:bg-[#FFB800]/30 transition-all flex items-center gap-2 cursor-pointer">
            <RefreshCw className="w-4 h-4 text-[#FFB800]" /> Seed Marketplace Data
          </button>
        )}
        <button onClick={() => setShowAddModal(true)} className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black text-xs rounded-xl shadow-xs transition-all flex items-center gap-2 cursor-pointer">
          <Plus className="w-4 h-4" /> Add Product
        </button>
        <button onClick={() => setShowAddStoreModal(true)} className="h-10 px-4 bg-[#111] dark:bg-white text-white dark:text-[#111] font-black text-xs rounded-xl shadow-xs transition-all flex items-center gap-2 cursor-pointer">
          <Store className="w-4 h-4" /> Enlist Store
        </button>
      </div>
    </div>

    {/* Master Killswitch Banner */}
    <div className={`p-4 rounded-2xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all shadow-xs ${
      marketplaceEnabled 
        ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-950 dark:text-emerald-200" 
        : "bg-red-500/10 border-red-500/30 text-red-950 dark:text-red-200"
    }`}>
      <div className="flex items-center gap-3.5">
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 shadow-xs ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
          <Store className="w-5 h-5" />
        </div>
        <div>
          <div className="flex items-center gap-2.5 flex-wrap">
            <span className="text-xs font-black uppercase tracking-wider">
              App Marketplace & Stores Master Switch:
            </span>
            <span className={`text-[10px] font-black px-2.5 py-0.5 rounded-full shadow-xs ${marketplaceEnabled ? "bg-emerald-500 text-white" : "bg-red-500 text-white"}`}>
              {marketplaceEnabled ? "ENABLED (LIVE ON MOBILE APP)" : "DISABLED (HIDDEN ON MOBILE APP)"}
            </span>
          </div>
          <p className="text-xs opacity-90 font-medium mt-0.5 max-w-2xl">
            {marketplaceEnabled 
              ? "All store catalogs, verified shops carousel, and vendor recruitment cards are currently visible to customers on mobile. Click the switch button anytime to turn off and hide everything marketplace." 
              : "Marketplace is completely hidden on mobile app (no store catalogs, no merchant enrollment, and the mobile dashboard hero button dynamically converts to 'Live Tracking' radar). Click to turn on."}
          </p>
        </div>
      </div>
      <button
        onClick={toggleMarketplace}
        className={`h-10 px-5 rounded-xl text-xs font-black shrink-0 transition-all shadow-xs flex items-center gap-2 cursor-pointer ${
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
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-black/10 dark:border-white/10 pb-3">
      <div className="flex gap-2 flex-wrap">
        <button onClick={() => setActiveSubTab("products")} className={"h-10 px-4 rounded-xl text-xs font-black transition-all flex items-center cursor-pointer " + (activeSubTab === "products" ? "bg-[#FFB800] text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          📦 Products ({totalProducts})
        </button>
        <button onClick={() => setActiveSubTab("stores")} className={"h-10 px-4 rounded-xl text-xs font-black transition-all relative flex items-center cursor-pointer " + (activeSubTab === "stores" ? "bg-[#FFB800] text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          🏪 Vendor Stores ({stores.length})
          {pendingStores.length > 0 && <span className="ml-2 px-2 py-0.5 bg-red-500 text-white rounded-full text-[10px]">{pendingStores.length}</span>}
        </button>
        <button onClick={() => setActiveSubTab("orders")} className={"h-10 px-4 rounded-xl text-xs font-black transition-all flex items-center cursor-pointer " + (activeSubTab === "orders" ? "bg-[#FFB800] text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          🛒 Orders ({orders.length})
        </button>
        <button onClick={() => setActiveSubTab("payouts")} className={"h-10 px-4 rounded-xl text-xs font-black transition-all relative flex items-center cursor-pointer " + (activeSubTab === "payouts" ? "bg-[#FFB800] text-[#111] shadow-xs" : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 font-bold hover:bg-gray-200 dark:hover:bg-white/10")}>
          💳 Payouts ({payoutRequests.length})
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
                            onClick={() => {
                              setSettlePayoutTarget(p);
                              setSettleBankRef(`TXN-BNK-${Date.now().toString().slice(-6)}`);
                              setSettleNotes("");
                            }}
                            className="px-3 py-1.5 bg-emerald-500 text-white font-black text-[10px] rounded-xl hover:bg-emerald-600 transition-colors cursor-pointer"
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

    {/* Settle Payout Modal */}
    {settlePayoutTarget && (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50 animate-fade-in" onClick={() => setSettlePayoutTarget(null)}>
        <div className="bg-white dark:bg-[#1a1a1a] rounded-3xl p-6 w-full max-w-md shadow-2xl border border-black/10 dark:border-white/10 space-y-4" onClick={e => e.stopPropagation()}>
          <div className="flex items-center justify-between pb-2 border-b border-black/10 dark:border-white/10">
            <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
              <DollarSign className="w-5 h-5 text-[#FFB800]" /> Approve Vendor Payout
            </h3>
            <button onClick={() => setSettlePayoutTarget(null)} className="text-xs font-bold text-gray-500 hover:text-red-500 cursor-pointer">Cancel</button>
          </div>
          
          <div className="p-3 bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800/40 rounded-2xl space-y-1">
            <p className="text-xs font-extrabold text-emerald-900 dark:text-emerald-200">{settlePayoutTarget.storeName || "Vendor Store"}</p>
            <p className="text-xl font-black text-[#111] dark:text-white">₦{settlePayoutTarget.amount.toLocaleString()}</p>
            <p className="text-[11px] text-gray-600 dark:text-gray-300 font-medium">{settlePayoutTarget.bankName} • {settlePayoutTarget.accountNumber}</p>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1">Bank Reference / Session ID *</label>
              <input
                type="text"
                value={settleBankRef}
                onChange={e => setSettleBankRef(e.target.value)}
                placeholder="e.g. 00001324091211550001"
                className="w-full px-3 py-2.5 rounded-xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-mono font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1">Settlement Notes (Optional)</label>
              <input
                type="text"
                value={settleNotes}
                onChange={e => setSettleNotes(e.target.value)}
                placeholder="e.g. Cleared via corporate NIP transfer"
                className="w-full px-3 py-2.5 rounded-xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-medium text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50"
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              onClick={() => setSettlePayoutTarget(null)}
              className="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 cursor-pointer"
            >
              Cancel
            </button>
            <button
              disabled={settlingPayout || !settleBankRef.trim()}
              onClick={async () => {
                setSettlingPayout(true);
                try {
                  const txId = "PAYOUT-" + Date.now();
                  await updateDoc(doc(db, "vendor_payout_requests", settlePayoutTarget.id), {
                    status: "APPROVED",
                    referenceId: settleBankRef.trim(),
                    notes: settleNotes.trim() || null,
                    processedAt: Timestamp.now()
                  });
                  await setDoc(doc(db, "transactions", txId), {
                    id: txId,
                    type: "VENDOR_PAYOUT",
                    title: `Vendor Payout Settlement: ${settlePayoutTarget.storeName || "Vendor"}`,
                    amount: settlePayoutTarget.amount,
                    vendorId: settlePayoutTarget.vendorId,
                    bankName: settlePayoutTarget.bankName,
                    accountNumber: settlePayoutTarget.accountNumber,
                    referenceId: settleBankRef.trim(),
                    notes: settleNotes.trim() || null,
                    status: "SUCCESS",
                    createdAt: Timestamp.now(),
                    timestamp: Date.now()
                  });
                  addLog("Approve Payout", `Approved ₦${settlePayoutTarget.amount.toLocaleString()} for ${settlePayoutTarget.storeName} (Ref: ${settleBankRef.trim()})`);
                  addToast("success", `Approved ₦${settlePayoutTarget.amount.toLocaleString()} payout with reference ${settleBankRef.trim()}`);
                  setSettlePayoutTarget(null);
                } catch (err: any) {
                  addToast("error", "Settlement failed: " + err.message);
                } finally {
                  setSettlingPayout(false);
                }
              }}
              className="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-black text-xs rounded-xl shadow-md cursor-pointer disabled:opacity-50"
            >
              {settlingPayout ? "Recording..." : "Confirm Settlement"}
            </button>
          </div>
        </div>
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
  const [statusFilter, setStatusFilter] = useState<"ALL" | "OPEN" | "RESOLVED" | "CLOSED">("ALL");
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

  const handleToggleStatus = async () => {
    if (!selectedTicket || !db) return;
    const newStatus = selectedTicket.status === "RESOLVED" ? "OPEN" : "RESOLVED";
    try {
      await updateDoc(doc(db, "support_chats", selectedTicket.id), {
        status: newStatus,
        lastUpdated: Date.now()
      });
      setSelectedTicket({ ...selectedTicket, status: newStatus });
      addLog("Support Status", `Marked ticket ${selectedTicket.ticketId} as ${newStatus}`);
      addToast("success", `Ticket marked as ${newStatus}`);
    } catch (err: any) {
      addToast("error", err.message || "Failed to update ticket status");
    }
  };

  const filteredTickets = tickets.filter(
    (t) => statusFilter === "ALL" || (t.status || "OPEN").toUpperCase() === statusFilter
  );

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
          <div className="flex items-center justify-between px-2 mb-2">
            <h3 className="font-bold text-sm text-[#111] dark:text-white">Conversations ({filteredTickets.length})</h3>
          </div>
          <div className="flex items-center gap-1 px-2 mb-3">
            {(["ALL", "OPEN", "RESOLVED", "CLOSED"] as const).map((filter) => (
              <button
                key={filter}
                type="button"
                onClick={() => setStatusFilter(filter)}
                className={`px-2 py-1 rounded-lg text-[10px] font-black transition-all cursor-pointer ${
                  statusFilter === filter
                    ? "bg-[#FFB800] text-[#111] shadow-xs"
                    : "bg-black/5 dark:bg-white/5 text-gray-600 dark:text-gray-400 hover:text-[#111] dark:hover:text-white"
                }`}
              >
                {filter}
              </button>
            ))}
          </div>
          <div className="flex-1 overflow-y-auto space-y-2 pr-1">
            {loading ? (
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium p-3">Loading tickets...</p>
            ) : filteredTickets.length === 0 ? (
              <p className="text-xs text-gray-600 dark:text-gray-400 font-medium p-3">No conversations match the selected filter.</p>
            ) : (
              filteredTickets.map((t) => (
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
                        : t.status === "RESOLVED"
                        ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300"
                        : "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300"
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
              <div className="pb-3 border-b border-black/10 dark:border-white/10 flex items-center justify-between flex-wrap gap-2">
                <div>
                  <h4 className="font-black text-sm text-[#111] dark:text-white">{selectedTicket.userName}</h4>
                  <p className="text-[11px] text-gray-600 dark:text-gray-400 font-medium">Ticket: {selectedTicket.ticketId} • User ID: {selectedTicket.userId}</p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleToggleStatus}
                    className={`px-3 py-1.5 rounded-xl text-xs font-black flex items-center gap-1.5 transition-all cursor-pointer ${
                      selectedTicket.status === "RESOLVED"
                        ? "bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/30 hover:bg-amber-500/20"
                        : "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border border-emerald-500/30 hover:bg-emerald-500/25"
                    }`}
                  >
                    {selectedTicket.status === "RESOLVED" ? (
                      <>
                        <RefreshCw size={13} /> Reopen Ticket
                      </>
                    ) : (
                      <>
                        <CheckCircle2 size={13} /> Mark Resolved
                      </>
                    )}
                  </button>
                  <span className="text-[11px] font-bold text-emerald-800 dark:text-emerald-300 bg-emerald-500/15 px-2.5 py-1 rounded-full flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    Live Connected
                  </span>
                </div>
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
                  className="h-10 flex-1 bg-white dark:bg-[#202020] text-[#111] dark:text-white px-3.5 rounded-xl text-xs outline-none border border-black/10 dark:border-white/10 focus:ring-2 focus:ring-[#FFB800]/40"
                />
                <button
                  type="submit"
                  disabled={!replyText.trim()}
                  className="h-10 bg-[#FFB800] text-[#111] px-5 rounded-xl font-black text-xs flex items-center gap-2 hover:bg-[#FFB800]/90 disabled:opacity-50 transition-all shadow-xs cursor-pointer"
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
  const [search, setSearch] = useState("");
  const [catFilter, setCatFilter] = useState("ALL");
  const [lPage, setLPage] = useState(0);
  const lPerPage = 20;

  const filtered = useMemo(() => {
    const q = search.toLowerCase().trim();
    return logs.filter(l => {
      // Category filter
      if (catFilter !== "ALL") {
        const itemCat = (l.category || "GENERAL").toUpperCase();
        if (catFilter === "USERS & WALLET" && itemCat !== "USERS" && itemCat !== "WALLET") return false;
        if (catFilter === "SETTINGS & STAFF" && itemCat !== "SETTINGS" && itemCat !== "STAFF") return false;
        if (catFilter !== "USERS & WALLET" && catFilter !== "SETTINGS & STAFF" && itemCat !== catFilter) return false;
      }
      // Search text
      if (!q) return true;
      return (
        l.action.toLowerCase().includes(q) ||
        l.details.toLowerCase().includes(q) ||
        (l.admin && l.admin.toLowerCase().includes(q)) ||
        (l.staffId && l.staffId.toLowerCase().includes(q)) ||
        (l.adminEmail && l.adminEmail.toLowerCase().includes(q)) ||
        (l.category && l.category.toLowerCase().includes(q))
      );
    });
  }, [logs, search, catFilter]);

  const lTotalPages = Math.max(1, Math.ceil(filtered.length / lPerPage));
  const pagedLogs = filtered.slice(lPage * lPerPage, (lPage + 1) * lPerPage);

  useEffect(() => { setLPage(0); }, [search, catFilter]);

  const exportCsv = () => {
    if (filtered.length === 0) return;
    const headers = ["Timestamp", "Action", "Category", "Details", "Admin", "Staff ID", "Admin Email"];
    const rows = filtered.map(l => [
      new Date(l.timestamp).toISOString(),
      `"${(l.action || "").replace(/"/g, '""')}"`,
      `"${(l.category || "GENERAL").replace(/"/g, '""')}"`,
      `"${(l.details || "").replace(/"/g, '""')}"`,
      `"${(l.admin || "").replace(/"/g, '""')}"`,
      `"${(l.staffId || "").replace(/"/g, '""')}"`,
      `"${(l.adminEmail || "").replace(/"/g, '""')}"`,
    ]);
    const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(r => r.join(","))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `esdispatch_audit_logs_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="tab-content space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2">
            <FileText className="w-5 h-5 text-[#FFB800]" /> Forensic Audit Log & Activity Trail
          </h1>
          <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-1">
            Tamper-evident chronological record of all administrative operations, status transitions, and wallet updates.
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={exportCsv}
            disabled={filtered.length === 0}
            className="h-10 px-3.5 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:border-[#FFB800]/50 transition-all flex items-center gap-2 shadow-2xs cursor-pointer disabled:opacity-40"
          >
            <Download className="w-3.5 h-3.5 text-[#FFB800]" />
            <span>Export CSV</span>
          </button>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Total Log Entries</span>
          <p className="text-xl sm:text-2xl font-black text-[#111] dark:text-white mt-1">{logs.length}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Recorded events</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Matching Filter</span>
          <p className="text-xl sm:text-2xl font-black text-[#FFB800] mt-1">{filtered.length}</p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Visible entries</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Wallet Events</span>
          <p className="text-xl sm:text-2xl font-black text-emerald-600 dark:text-emerald-400 mt-1">
            {logs.filter(l => (l.category || "").toLowerCase() === "wallet" || l.action.toLowerCase().includes("wallet")).length}
          </p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Credits & debits</span>
        </div>
        <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-4 shadow-xs">
          <span className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">Users & Fleet</span>
          <p className="text-xl sm:text-2xl font-black text-blue-600 dark:text-blue-400 mt-1">
            {logs.filter(l => (l.category || "").toLowerCase() === "users" || l.action.toLowerCase().includes("user")).length}
          </p>
          <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400 mt-0.5 block">Accounts & drivers</span>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-2xl p-3 sm:p-4 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3 shadow-xs">
        {/* Category Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0 scrollbar-none">
          <span className="text-xs font-bold text-gray-500 dark:text-gray-400 mr-1 hidden lg:inline">Category:</span>
          {["ALL", "USERS & WALLET", "SHIPMENTS", "MARKETPLACE", "SETTINGS & STAFF"].map(cat => (
            <button
              key={cat}
              type="button"
              onClick={() => setCatFilter(cat)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer ${
                catFilter === cat
                  ? "bg-[#FFB800] text-[#111] shadow-xs"
                  : "bg-gray-100 dark:bg-[#222] text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-[#2c2c2c]"
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Generous Search with pl-12 */}
        <div className="w-full md:w-80 shrink-0">
          <SearchInput value={search} onChange={setSearch} placeholder="Search action, details, admin, ID..." />
        </div>
      </div>

      {/* Main Table */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-3xl p-5 shadow-xs overflow-hidden">
        {filtered.length === 0 ? (
          <div className="text-center py-16">
            <div className="w-12 h-12 rounded-2xl bg-[#FFB800]/10 border border-[#FFB800]/20 flex items-center justify-center text-[#FFB800] mx-auto mb-3">
              <FileText className="w-6 h-6" />
            </div>
            <p className="text-sm font-bold text-gray-900 dark:text-white">No audit entries found</p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Try resetting your search query or category filters.</p>
            <button
              type="button"
              onClick={() => { setSearch(""); setCatFilter("ALL"); }}
              className="mt-4 px-4 py-2 bg-gray-100 dark:bg-white/10 text-xs font-bold rounded-xl hover:bg-gray-200 dark:hover:bg-white/20 transition-all cursor-pointer"
            >
              Clear Filters
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50/50 dark:bg-white/5 border-b border-black/5 dark:border-white/10">
                <tr className="text-[10px] font-extrabold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  <th className="p-3 text-left">Action & Category</th>
                  <th className="p-3 text-left">Forensic Detail</th>
                  <th className="p-3 text-left hidden sm:table-cell">Authorized Admin</th>
                  <th className="p-3 text-right">Date & Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {pagedLogs.map(l => {
                  const act = l.action.toLowerCase();
                  const isCreate = act.includes("create") || act.includes("add") || act.includes("seed");
                  const isDelete = act.includes("delete") || act.includes("remove");
                  const isWallet = act.includes("credit") || act.includes("debit") || act.includes("wallet");
                  const isUpdate = act.includes("update") || act.includes("edit") || act.includes("status");

                  return (
                    <tr key={l.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group">
                      <td className="p-3">
                        <div className="flex flex-col gap-1 items-start">
                          <span className={`text-[10px] font-black px-2 py-0.5 rounded-full whitespace-nowrap ${
                            isDelete
                              ? "bg-red-100 text-red-700 dark:bg-red-950/40 dark:text-red-300 border border-red-500/20"
                              : isWallet
                              ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-500/20"
                              : isCreate
                              ? "bg-blue-100 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 border border-blue-500/20"
                              : isUpdate
                              ? "bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-500/20"
                              : "bg-gray-100 text-gray-700 dark:bg-white/10 dark:text-gray-300"
                          }`}>
                            {l.action}
                          </span>
                          {l.category && (
                            <span className="text-[9px] font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                              {l.category}
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="p-3">
                        <p className="text-gray-900 dark:text-white font-medium break-words leading-relaxed text-xs">
                          {l.details}
                        </p>
                      </td>
                      <td className="p-3 hidden sm:table-cell">
                        <div className="flex flex-col">
                          <span className="text-xs font-bold text-gray-900 dark:text-white">
                            {l.admin || "Admin"}
                          </span>
                          {(l.staffId || l.adminEmail) && (
                            <span className="text-[10px] font-mono text-gray-500 dark:text-gray-400">
                              {l.staffId ? `[${l.staffId}]` : l.adminEmail}
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="p-3 text-right whitespace-nowrap">
                        <div className="flex flex-col items-end">
                          <span className="text-xs font-semibold text-gray-900 dark:text-gray-200">
                            {new Date(l.timestamp).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}
                          </span>
                          <span className="text-[10px] font-mono text-gray-500 dark:text-gray-400">
                            {new Date(l.timestamp).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit", second: "2-digit" })}
                          </span>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {lTotalPages > 1 && (
          <div className="flex items-center justify-between flex-wrap gap-3 pt-4 border-t border-black/5 dark:border-white/10 mt-2">
            <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
              Page {lPage + 1} of {lTotalPages} ({filtered.length} entries)
            </span>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setLPage(p => Math.max(0, p - 1))}
                disabled={lPage === 0}
                className="h-8 px-3 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] flex items-center gap-1 cursor-pointer"
              >
                <ChevronLeft size={14} /> Previous
              </button>
              <button
                type="button"
                onClick={() => setLPage(p => Math.min(lTotalPages - 1, p + 1))}
                disabled={lPage >= lTotalPages - 1}
                className="h-8 px-3 rounded-xl bg-gray-100 dark:bg-[#222] text-xs font-bold text-[#111] dark:text-white disabled:opacity-30 hover:bg-gray-200 dark:hover:bg-[#333] flex items-center gap-1 cursor-pointer"
              >
                Next <ChevronRight size={14} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
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
      <button onClick={() => setShowAdd(true)} className="h-10 px-4 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-xs hover:bg-[#FFB800]/90 transition-all cursor-pointer"><Plus size={16} /> Add Slide</button>
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
                <button onClick={() => toggleActive(b.id, b.active)} className="text-[10px] font-black text-amber-800 dark:text-[#FFB800] hover:underline cursor-pointer">{b.active ? "Disable" : "Enable"}</button>
                <button onClick={() => deleteBanner(b.id)} className="text-[10px] font-bold text-red-600 dark:text-red-400 hover:underline cursor-pointer">Delete</button>
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
            <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">Title</label>
              <input value={title} onChange={e => setTitle(e.target.value)} required className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
            <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">Subtitle</label>
              <input value={subtitle} onChange={e => setSubtitle(e.target.value)} className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
            <div><label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1">Image URL</label>
              <input value={imageUrl} onChange={e => setImageUrl(e.target.value)} required placeholder="https://..." className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" /></div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setShowAdd(false)} className="h-10 px-4 bg-gray-100 dark:bg-white/10 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors text-xs font-bold rounded-xl cursor-pointer">Cancel</button>
            <button type="submit" disabled={saving} className="h-10 px-5 bg-[#FFB800] text-[#111] text-xs font-black rounded-xl hover:bg-[#FFB800]/90 transition-all shadow-xs cursor-pointer">{saving ? "Saving..." : "Add Slide"}</button>
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
      <div className="flex-1 overflow-y-auto px-4 sm:px-6 lg:px-8 pb-8 pt-3">
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
            addToast={addToast}
          />}
        {tab === "users" && <UsersTab activeUsers={activeUsers} searchQuery={searchQuery} db={db} addLog={addLog} addToast={addToast} createNotification={createNotification} />}
        {tab === "shipments" && <ShipmentsTab deliveries={deliveries} drivers={drivers} searchQuery={searchQuery} db={db} addLog={addLog} addToast={addToast} filterPrefill={shipmentsFilterPrefill} setFilterPrefill={setShipmentsFilterPrefill} />}
        {tab === "tracking" && <TrackingTab deliveries={deliveries} drivers={drivers} />}
        {tab === "broadcast" && <BroadcastNewsTab db={db} users={users} currentUserEmail={currentUser?.email} addLog={addLog} addToast={addToast} />}
        {tab === "banners" && <BannersTab banners={banners} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "referrals" && <ReferralsTab referrals={referrals} completedReferrals={completedReferrals} searchQuery={searchQuery} addToast={addToast} />}
        {tab === "promotions" && <PromotionsTab promotions={promotions} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "appcards" && <AppCardsTab appContent={appContent} db={db} addLog={addLog} addToast={addToast} />}
        {tab === "settings" && <SettingsTab db={db} addLog={addLog} addToast={addToast} activeUsers={activeUsers} />}
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

