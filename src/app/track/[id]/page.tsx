"use client";

import React, { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { db } from "@/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";
import Link from "next/link";
import {
  Package,
  Truck,
  CheckCircle2,
  Clock,
  MapPin,
  Phone,
  ShieldCheck,
  ArrowRight,
  Navigation,
  Sparkles,
  AlertCircle,
  Printer,
  FileText,
  Search,
  MessageSquare,
  Key,
  Copy,
  Check,
  Radio,
  ExternalLink,
} from "lucide-react";

interface DeliveryDetails {
  id: string;
  itemName?: string;
  status: string;
  pickupAddress: string;
  deliveryAddress: string;
  senderName?: string;
  receiverName?: string;
  courierName?: string;
  courierPhone?: string;
  riderBikeNumber?: string;
  courierLatitude?: number;
  courierLongitude?: number;
  pickupLat?: number;
  pickupLng?: number;
  deliveryLat?: number;
  deliveryLng?: number;
  price?: number;
  type?: string;
  otpCode?: string;
  otpVerified?: boolean;
  createdAt?: any;
  lastUpdated?: number;
}

const STEPS = [
  { key: "BOOKED", label: "Order Booked", desc: "Shipment registered in dispatch pool" },
  { key: "ASSIGNED", label: "Courier Assigned", desc: "Driver dispatched to pickup point" },
  { key: "TRANSIT", label: "In Transit", desc: "Package on route to destination" },
  { key: "ARRIVED", label: "Arrived", desc: "Courier at delivery location" },
  { key: "DELIVERED", label: "Delivered", desc: "Handover completed & verified" },
];

export default function PublicTrackingPage() {
  const params = useParams();
  const router = useRouter();
  const trackingId = Array.isArray(params?.id) ? params.id[0] : (params?.id as string) || "";

  const [delivery, setDelivery] = useState<DeliveryDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [navSearch, setNavSearch] = useState("");
  const [fallbackSearch, setFallbackSearch] = useState("");
  const [copiedOtp, setCopiedOtp] = useState(false);
  const [copiedWaybill, setCopiedWaybill] = useState(false);

  useEffect(() => {
    if (!trackingId) {
      setLoading(false);
      setNotFound(true);
      return;
    }

    setLoading(true);
    setNotFound(false);

    const docRef = doc(db, "deliveries", trackingId);
    const unsubscribe = onSnapshot(
      docRef,
      (snap) => {
        if (snap.exists()) {
          setDelivery({ id: snap.id, ...snap.data() } as DeliveryDetails);
          setLoading(false);
          setNotFound(false);
        } else {
          // Fallback to parcels collection
          const parcelRef = doc(db, "parcels", trackingId);
          onSnapshot(parcelRef, (pSnap) => {
            setLoading(false);
            if (pSnap.exists()) {
              setDelivery({ id: pSnap.id, ...pSnap.data() } as DeliveryDetails);
              setNotFound(false);
            } else {
              setNotFound(true);
            }
          });
        }
      },
      (err) => {
        console.error("Tracking listener error:", err);
        setLoading(false);
        setNotFound(true);
      }
    );

    return () => unsubscribe();
  }, [trackingId]);

  const getActiveStepIndex = (statusStr: string) => {
    const s = (statusStr || "").toUpperCase();
    if (s.includes("DELIVERED") || s.includes("COMPLETED")) return 4;
    if (s.includes("ARRIVED")) return 3;
    if (
      s.includes("TRANSIT") ||
      s.includes("OUT FOR DELIVERY") ||
      s.includes("OUT_FOR_DELIVERY") ||
      s.includes("DISPATCHED") ||
      s.includes("PICKED_UP") ||
      s.includes("PICKED UP")
    )
      return 2;
    if (s.includes("ASSIGNED") || s.includes("ACCEPTED")) return 1;
    return 0;
  };

  const handleNavSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = navSearch.trim();
    if (clean) {
      router.push(`/track/${clean}`);
      setNavSearch("");
    }
  };

  const handleFallbackSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = fallbackSearch.trim();
    if (clean) {
      router.push(`/track/${clean}`);
      setFallbackSearch("");
    }
  };

  const handleCopyOtp = (code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedOtp(true);
    setTimeout(() => setCopiedOtp(false), 2000);
  };

  const handleCopyWaybill = (id: string) => {
    navigator.clipboard.writeText(id);
    setCopiedWaybill(true);
    setTimeout(() => setCopiedWaybill(false), 2000);
  };

  const formatWhatsAppUrl = (phone?: string) => {
    if (!phone) return "";
    const clean = phone.replace(/[^0-9]/g, "");
    const intl = clean.startsWith("0") ? "234" + clean.slice(1) : clean;
    const msg = `Hello, I am tracking shipment #${trackingId} with ESDISPATCH.`;
    return `https://wa.me/${intl}?text=${encodeURIComponent(msg)}`;
  };

  const activeIdx = delivery ? getActiveStepIndex(delivery.status) : 0;

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white font-sans flex flex-col selection:bg-[#FFB800] selection:text-black">
      {/* Top Navigation Bar */}
      <header className="border-b border-[#262626] bg-[#111111]/90 backdrop-blur-md sticky top-0 z-50 px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between gap-4">
        <Link href="/" className="flex items-center gap-3 shrink-0 group">
          <div className="w-9 h-9 rounded-xl bg-[#FFB800] flex items-center justify-center text-black font-black text-sm shadow-md shadow-[#FFB800]/20">
            ES
          </div>
          <div>
            <div className="text-sm font-black tracking-wider text-white group-hover:text-[#FFB800] transition-colors">
              ESDISPATCH
            </div>
            <div className="text-[9px] font-bold tracking-widest text-[#FFB800]">
              PREMIUM LOGISTICS & DISPATCH
            </div>
          </div>
        </Link>

        {/* Global Track Input in Navbar */}
        <form onSubmit={handleNavSearchSubmit} className="hidden md:flex items-center gap-2 max-w-sm w-full">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
            <input
              type="text"
              value={navSearch}
              onChange={(e) => setNavSearch(e.target.value)}
              placeholder="Track waybill (e.g. TRK-8921)..."
              className="w-full h-10 pl-9 pr-3 rounded-xl bg-[#1A1A1A] border border-[#2E2E2E] text-white placeholder-neutral-500 text-xs font-mono focus:border-[#FFB800] focus:ring-1 focus:ring-[#FFB800] outline-none transition-all"
            />
          </div>
          <button
            type="submit"
            className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-black font-black text-xs uppercase tracking-wider rounded-xl transition-all shrink-0 cursor-pointer shadow-sm active:scale-95"
          >
            Track
          </button>
        </form>

        <div className="flex items-center gap-3 shrink-0">
          <div className="hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-bold font-mono">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>SATELLITE ACTIVE</span>
          </div>
          <Link
            href="/"
            className="h-10 px-3.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-bold text-neutral-300 hover:text-white flex items-center gap-1.5 transition-all"
          >
            <span>Main Site</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6 lg:p-8 space-y-6">
        {loading ? (
          <div className="h-96 flex flex-col items-center justify-center space-y-4">
            <div className="relative flex items-center justify-center w-16 h-16">
              <div className="absolute inset-0 rounded-full border-2 border-[#FFB800]/30 animate-ping" />
              <div className="w-12 h-12 border-3 border-[#FFB800] border-t-transparent rounded-full animate-spin" />
            </div>
            <p className="text-xs text-[#FFB800] font-mono font-bold uppercase tracking-widest">
              Connecting Satellite Telemetry...
            </p>
          </div>
        ) : notFound || !delivery ? (
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 sm:p-10 text-center space-y-6 shadow-2xl">
            <div className="w-16 h-16 bg-red-500/10 border border-red-500/20 rounded-2xl flex items-center justify-center mx-auto text-red-400">
              <AlertCircle className="w-8 h-8" />
            </div>
            <div>
              <h2 className="text-2xl font-black font-mono tracking-wider text-[#FFB800] uppercase">
                Waybill Not Found
              </h2>
              <p className="text-xs text-neutral-400 max-w-md mx-auto mt-2 leading-relaxed">
                No active consignment matches tracking reference{" "}
                <span className="text-white font-mono font-bold bg-white/10 px-2 py-0.5 rounded">
                  #{trackingId}
                </span>
                . Please check your receipt code or query a new waybill below.
              </p>
            </div>

            {/* Embedded Search Bar */}
            <form onSubmit={handleFallbackSearchSubmit} className="max-w-md mx-auto flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
                <input
                  type="text"
                  value={fallbackSearch}
                  onChange={(e) => setFallbackSearch(e.target.value)}
                  placeholder="Enter tracking ID (e.g. TRK-1042)..."
                  className="w-full h-10 pl-9 pr-3 rounded-xl bg-[#1F1F1F] border border-[#333] text-white placeholder-neutral-500 text-xs font-mono focus:border-[#FFB800] focus:ring-1 focus:ring-[#FFB800] outline-none"
                />
              </div>
              <button
                type="submit"
                className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-black font-black text-xs uppercase tracking-wider rounded-xl transition-all shrink-0 cursor-pointer"
              >
                Track
              </button>
            </form>

            <div className="pt-2">
              <Link
                href="/"
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-bold text-neutral-300 hover:text-white transition-all"
              >
                <span>Return to Home</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Live Satellite Radar Telemetry Card */}
            <div className="bg-gradient-to-r from-[#141414] via-[#181818] to-[#141414] border border-[#FFB800]/25 rounded-2xl p-4 sm:p-5 relative overflow-hidden shadow-xl">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-center gap-3.5">
                  {/* Radar Concentric Beacon */}
                  <div className="relative flex items-center justify-center w-10 h-10 shrink-0">
                    <span className="absolute w-10 h-10 rounded-full bg-[#FFB800]/20 animate-ping" />
                    <span className="absolute w-7 h-7 rounded-full bg-[#FFB800]/30" />
                    <span className="w-3.5 h-3.5 rounded-full bg-[#FFB800] shadow-md shadow-[#FFB800]/80 z-10" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-black font-mono tracking-widest text-[#FFB800] uppercase">
                        SATELLITE RADAR TELEMETRY
                      </span>
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                    </div>
                    <div className="text-xs font-bold text-white mt-0.5">
                      {delivery.courierLatitude && delivery.courierLongitude
                        ? `Real-Time GPS Fix: ${delivery.courierLatitude.toFixed(4)}°N, ${delivery.courierLongitude.toFixed(4)}°E`
                        : "Active Dispatch Telemetry • Benin City Corridor"}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 font-mono text-[10px] text-neutral-400 shrink-0">
                  <span className="px-2.5 py-1 rounded-lg bg-black/40 border border-white/5 text-emerald-400">
                    GPS L1 1575.42 MHz
                  </span>
                  <span className="px-2.5 py-1 rounded-lg bg-black/40 border border-white/5 text-[#FFB800]">
                    SIGNAL 99.8%
                  </span>
                </div>
              </div>
            </div>

            {/* Consignment Status Overview Card */}
            <div className="bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 sm:p-8 relative overflow-hidden shadow-2xl">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#262626]">
                <div>
                  <div className="flex items-center gap-2 mb-2 flex-wrap">
                    <button
                      onClick={() => handleCopyWaybill(delivery.id)}
                      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#FFB800]/10 text-[#FFB800] border border-[#FFB800]/25 text-xs font-mono font-bold hover:bg-[#FFB800]/20 transition-colors"
                      title="Click to copy tracking ID"
                    >
                      <span>#{delivery.id}</span>
                      {copiedWaybill ? (
                        <Check className="w-3 h-3 text-emerald-400" />
                      ) : (
                        <Copy className="w-3 h-3 opacity-70" />
                      )}
                    </button>
                    <span className="px-2.5 py-1 rounded-lg bg-white/5 border border-white/10 text-neutral-400 text-xs font-mono">
                      {delivery.type || "EXPRESS LOGISTICS"}
                    </span>
                  </div>
                  <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
                    {delivery.itemName || "Express Consignment"}
                  </h1>
                </div>

                <div className="flex items-center gap-2.5 self-start sm:self-auto flex-wrap">
                  <div className="h-10 px-4 rounded-xl bg-[#FFB800] text-black text-xs font-black uppercase tracking-wider flex items-center shadow-lg shadow-[#FFB800]/20">
                    {delivery.status.toUpperCase()}
                  </div>
                  <button
                    onClick={() => window.print()}
                    className="h-10 px-4 rounded-xl bg-[#1F1F1F] hover:bg-[#2A2A2A] border border-[#333] text-white text-xs font-mono font-bold flex items-center gap-2 transition-all cursor-pointer"
                  >
                    <Printer className="w-4 h-4 text-[#FFB800]" />
                    <span className="hidden sm:inline">Print Waybill</span>
                  </button>
                </div>
              </div>

              {/* 5-Stage Stepper */}
              <div className="mt-8 grid grid-cols-1 sm:grid-cols-5 gap-3">
                {STEPS.map((s, idx) => {
                  const isDone = idx <= activeIdx;
                  const isCurrent = idx === activeIdx;
                  return (
                    <div
                      key={s.key}
                      className={`p-4 rounded-2xl border transition-all ${
                        isCurrent
                          ? "bg-[#FFB800]/10 border-[#FFB800] text-white shadow-md shadow-[#FFB800]/10"
                          : isDone
                          ? "bg-[#1A1A1A] border-[#333] text-neutral-300"
                          : "bg-[#111111] border-[#222] text-neutral-600"
                      }`}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-xs font-mono font-bold">0{idx + 1}</span>
                        {isDone ? (
                          <CheckCircle2
                            className={`w-4 h-4 ${isCurrent ? "text-[#FFB800]" : "text-emerald-400"}`}
                          />
                        ) : (
                          <Clock className="w-4 h-4 text-neutral-600" />
                        )}
                      </div>
                      <h4
                        className={`text-xs font-bold font-mono tracking-wide uppercase ${
                          isCurrent ? "text-[#FFB800]" : isDone ? "text-white" : "text-neutral-500"
                        }`}
                      >
                        {s.label}
                      </h4>
                      <p className="text-[10px] text-neutral-400 mt-1 leading-normal">{s.desc}</p>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Recipient OTP Handover Badge (Encrypted Security) */}
            {delivery.otpCode && (
              <div className="bg-gradient-to-br from-[#181818] to-[#121212] border border-[#FFB800]/40 rounded-3xl p-6 sm:p-7 relative overflow-hidden shadow-2xl">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-2xl bg-[#FFB800]/15 border border-[#FFB800]/30 flex items-center justify-center text-[#FFB800] shrink-0">
                      <Key className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-black font-mono tracking-widest text-[#FFB800] uppercase">
                          RECIPIENT HANDOVER VERIFICATION OTP
                        </span>
                        {delivery.otpVerified && (
                          <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold">
                            VERIFIED
                          </span>
                        )}
                      </div>
                      <h3 className="text-base font-black text-white mt-0.5">
                        Encrypted Security Release Code
                      </h3>
                      <p className="text-xs text-neutral-400 mt-1 max-w-xl leading-relaxed">
                        Share this 4-digit code with the courier upon physical parcel handover to confirm and release the delivery.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 self-start sm:self-auto bg-black/60 border border-[#333] px-5 py-3 rounded-2xl">
                    <div className="text-2xl sm:text-3xl font-mono font-black tracking-widest text-[#FFB800]">
                      {delivery.otpCode}
                    </div>
                    <button
                      onClick={() => handleCopyOtp(delivery.otpCode!)}
                      className="h-10 px-3.5 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-bold flex items-center gap-1.5 transition-all cursor-pointer"
                      title="Copy OTP"
                    >
                      {copiedOtp ? (
                        <>
                          <Check className="w-4 h-4 text-emerald-400" />
                          <span className="text-emerald-400">Copied</span>
                        </>
                      ) : (
                        <>
                          <Copy className="w-4 h-4" />
                          <span>Copy</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Courier & Dispatch Unit Grid */}
            <div className="grid sm:grid-cols-2 gap-5">
              {/* Courier Profile Card */}
              <div className="bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-[#FFB800]">
                    <Truck className="w-4 h-4" />
                    <span>Assigned Dispatch Unit</span>
                  </div>
                  <span className="px-2 py-0.5 rounded-md bg-emerald-500/10 text-emerald-400 text-[10px] font-bold uppercase tracking-wider">
                    Verified Courier
                  </span>
                </div>

                {delivery.courierName ? (
                  <div className="space-y-4">
                    <div className="flex items-center gap-3.5">
                      <div className="w-12 h-12 rounded-2xl bg-[#FFB800] flex items-center justify-center text-black font-black text-lg shadow-md">
                        <Truck className="w-6 h-6" />
                      </div>
                      <div>
                        <div className="text-base font-bold text-white">{delivery.courierName}</div>
                        <div className="text-xs text-neutral-400 font-mono">
                          Fleet Unit: {delivery.riderBikeNumber || "Dispatch Unit 01"}
                        </div>
                      </div>
                    </div>

                    {/* Direct Contact Actions (Strict h-10 height) */}
                    <div className="flex items-center gap-2.5 pt-1">
                      {delivery.courierPhone && (
                        <>
                          <a
                            href={`tel:${delivery.courierPhone}`}
                            className="flex-1 h-10 px-3.5 rounded-xl bg-[#1F1F1F] hover:bg-[#2A2A2A] border border-[#333] text-white font-bold text-xs flex items-center justify-center gap-2 transition-all"
                          >
                            <Phone className="w-4 h-4 text-[#FFB800]" />
                            <span>Call Courier</span>
                          </a>
                          <a
                            href={formatWhatsAppUrl(delivery.courierPhone)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex-1 h-10 px-3.5 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 border border-emerald-500/30 text-emerald-400 font-bold text-xs flex items-center justify-center gap-2 transition-all"
                          >
                            <MessageSquare className="w-4 h-4" />
                            <span>WhatsApp</span>
                          </a>
                        </>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="py-6 text-center space-y-2">
                    <div className="w-10 h-10 rounded-full bg-white/5 border border-white/10 flex items-center justify-center mx-auto text-neutral-500">
                      <Clock className="w-5 h-5" />
                    </div>
                    <p className="text-xs text-neutral-400">
                      Smart dispatch is currently routing the closest courier to pickup.
                    </p>
                  </div>
                )}
              </div>

              {/* Security & Guarantee Card */}
              <div className="bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 space-y-4 flex flex-col justify-between">
                <div>
                  <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-emerald-400">
                    <ShieldCheck className="w-4 h-4" />
                    <span>Verified Escrow & Transit Security</span>
                  </div>
                  <p className="text-xs text-neutral-400 leading-relaxed mt-2.5">
                    Every consignment dispatched via ESDISPATCH is protected under digital manifest logs, real-time telemetry verification, and recipient OTP authentication.
                  </p>
                </div>

                <div className="pt-2 border-t border-[#262626] flex items-center justify-between text-[11px] font-mono text-neutral-400">
                  <span>Protected Escrow Handover</span>
                  <span className="text-[#FFB800] font-bold">100% Guaranteed</span>
                </div>
              </div>
            </div>

            {/* Transit Route Details Card */}
            <div className="bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 space-y-6">
              <div className="flex items-center justify-between pb-4 border-b border-[#262626]">
                <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-neutral-300">
                  <Navigation className="w-4 h-4 text-[#FFB800]" />
                  <span>Transit Route & Dispatch Manifest</span>
                </div>
                {delivery.price && (
                  <div className="text-xs font-mono font-bold text-white">
                    <span className="text-neutral-500">Fare: </span>
                    <span className="text-[#FFB800]">₦{delivery.price.toLocaleString()}</span>
                  </div>
                )}
              </div>

              <div className="grid sm:grid-cols-2 gap-6">
                {/* Pickup Address */}
                <div className="flex items-start gap-3.5 bg-black/40 p-4 rounded-2xl border border-white/5">
                  <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center shrink-0">
                    <MapPin className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-[10px] font-mono font-bold text-neutral-500 uppercase tracking-wider">
                      Origin / Pickup Point
                    </div>
                    <div className="text-xs font-bold text-white mt-1 leading-snug">
                      {delivery.pickupAddress}
                    </div>
                    {delivery.senderName && (
                      <div className="text-[11px] text-neutral-400 mt-1 font-mono">
                        Sender: {delivery.senderName}
                      </div>
                    )}
                  </div>
                </div>

                {/* Delivery Destination */}
                <div className="flex items-start gap-3.5 bg-black/40 p-4 rounded-2xl border border-white/5">
                  <div className="w-9 h-9 rounded-xl bg-[#FFB800]/10 text-[#FFB800] flex items-center justify-center shrink-0">
                    <Navigation className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-[10px] font-mono font-bold text-neutral-500 uppercase tracking-wider">
                      Destination / Handover Address
                    </div>
                    <div className="text-xs font-bold text-white mt-1 leading-snug">
                      {delivery.deliveryAddress}
                    </div>
                    {delivery.receiverName && (
                      <div className="text-[11px] text-neutral-400 mt-1 font-mono">
                        Recipient: {delivery.receiverName}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* Quick Actions Bar */}
            <div className="flex flex-wrap items-center justify-between gap-4 p-5 rounded-3xl bg-[#141414] border border-[#2E2E2E] print:hidden">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#FFB800]/10 text-[#FFB800] flex items-center justify-center">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-white">Official Shipment Waybill</h4>
                  <p className="text-[10px] text-neutral-400">
                    Export or print physical thermal dispatch receipt
                  </p>
                </div>
              </div>
              <button
                onClick={() => window.print()}
                className="h-10 px-5 rounded-xl bg-[#FFB800] hover:bg-[#FFB800]/90 text-black font-black text-xs uppercase tracking-wider flex items-center gap-2 transition-all cursor-pointer shadow-md active:scale-95"
              >
                <Printer className="w-4 h-4" />
                <span>Print / Save Waybill</span>
              </button>
            </div>

            {/* Printable Thermal Waybill Voucher (Only visible in window.print()) */}
            <div className="hidden print:block p-8 bg-white text-black font-sans max-w-xl mx-auto border-2 border-black rounded-lg">
              <div className="text-center border-b-2 border-black pb-4 mb-4">
                <h1 className="text-2xl font-black tracking-wider">ESDISPATCH</h1>
                <p className="text-[11px] font-bold uppercase tracking-widest text-gray-700">
                  PREMIUM LOGISTICS & DISPATCH
                </p>
                <p className="text-xs font-mono mt-1 font-bold">
                  OFFICIAL WAYBILL MANIFEST: #{delivery.id}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4 border-b border-black pb-4 mb-4 text-xs">
                <div>
                  <p className="font-bold uppercase text-gray-600">Sender / Origin</p>
                  <p className="font-bold text-sm">{delivery.senderName || "Registered Sender"}</p>
                  <p className="text-gray-700">{delivery.pickupAddress}</p>
                </div>
                <div>
                  <p className="font-bold uppercase text-gray-600">Consignee / Destination</p>
                  <p className="font-bold text-sm">{delivery.receiverName || "Consignee"}</p>
                  <p className="text-gray-700">{delivery.deliveryAddress}</p>
                </div>
              </div>

              <div className="border-b border-black pb-4 mb-4 text-xs space-y-2">
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Item Description:</span>
                  <span className="font-semibold">{delivery.itemName || "Express Parcel"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Status:</span>
                  <span className="font-bold uppercase">{delivery.status}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Assigned Courier:</span>
                  <span>
                    {delivery.courierName || "Dispatch Unit"} ({delivery.riderBikeNumber || "Fleet Unit"})
                  </span>
                </div>
                {delivery.otpCode && (
                  <div className="flex justify-between">
                    <span className="font-bold text-gray-600">Verification OTP:</span>
                    <span className="font-mono font-bold">{delivery.otpCode}</span>
                  </div>
                )}
                {delivery.price && (
                  <div className="flex justify-between text-sm font-black pt-2 border-t border-dashed border-gray-300">
                    <span>Total Fare:</span>
                    <span>₦{delivery.price.toLocaleString()}</span>
                  </div>
                )}
              </div>

              <div className="text-center text-[10px] text-gray-600 pt-3">
                <p>Thank you for choosing ESDISPATCH.</p>
                <p>Official Manifest Support • https://esdispatch.com</p>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-[#262626] py-6 text-center text-xs text-neutral-500 font-mono">
        &copy; {new Date().getFullYear()} ESDISPATCH. ALL RIGHTS RESERVED. PREMIUM LOGISTICS & DISPATCH.
      </footer>
    </div>
  );
}
