"use client";

import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
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
  FileText
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
  { key: "DELIVERED", label: "Delivered", desc: "Handover completed & verified" }
];

export default function PublicTrackingPage() {
  const params = useParams();
  const trackingId = Array.isArray(params?.id) ? params.id[0] : (params?.id as string) || "";

  const [delivery, setDelivery] = useState<DeliveryDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!trackingId) {
      setLoading(false);
      setNotFound(true);
      return;
    }

    const docRef = doc(db, "deliveries", trackingId);
    const unsubscribe = onSnapshot(
      docRef,
      (snap) => {
        setLoading(false);
        if (snap.exists()) {
          setDelivery({ id: snap.id, ...snap.data() } as DeliveryDetails);
          setNotFound(false);
        } else {
          setNotFound(true);
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
    if (s.includes("TRANSIT") || s.includes("OUT FOR DELIVERY") || s.includes("DISPATCHED")) return 2;
    if (s.includes("ASSIGNED") || s.includes("ACCEPTED")) return 1;
    return 0; // PENDING / BOOKED
  };

  const activeIndex = delivery ? getActiveStepIndex(delivery.status) : 0;

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white flex flex-col font-sans selection:bg-[#FFC542] selection:text-[#111]">
      {/* Top Navigation Bar */}
      <header className="border-b border-white/10 bg-[#111111]/80 backdrop-blur-md sticky top-0 z-50 px-4 lg:px-8 py-3.5 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-3 group">
          <div className="w-9 h-9 rounded-xl bg-[#FFC542] flex items-center justify-center text-[#111] font-black text-lg shadow-lg shadow-[#FFC542]/20">
            ES
          </div>
          <div>
            <div className="text-sm font-black tracking-wider text-white group-hover:text-[#FFC542] transition-colors">
              ESDISPATCH
            </div>
            <div className="text-[9px] font-bold tracking-widest text-[#FFC542]">
              PREMIUM LOGISTICS & DISPATCH
            </div>
          </div>
        </Link>

        <div className="flex items-center gap-2">
          <div className="hidden sm:flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-bold">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            Live Dispatch Network
          </div>
          <Link
            href="/sign-in"
            className="text-xs font-bold px-3.5 py-1.5 rounded-xl bg-white/10 hover:bg-white/20 transition-all border border-white/10"
          >
            Portal Login
          </Link>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6 lg:p-8 space-y-6">
        {loading ? (
          <div className="h-96 flex flex-col items-center justify-center space-y-4">
            <div className="w-12 h-12 border-3 border-[#FFC542] border-t-transparent rounded-full animate-spin" />
            <p className="text-xs text-white/50 font-bold uppercase tracking-wider">
              Connecting to Live Satellite Telemetry...
            </p>
          </div>
        ) : notFound || !delivery ? (
          <div className="bg-[#161616] border border-white/10 rounded-3xl p-8 text-center space-y-4 shadow-xl">
            <AlertCircle className="w-12 h-12 text-[#FFC542] mx-auto opacity-80" />
            <h2 className="text-lg font-black text-white">Tracking Reference Not Found</h2>
            <p className="text-xs text-white/60 max-w-md mx-auto">
              We couldn't locate active shipment details for #{trackingId}. Please verify your tracking number or contact dispatch support.
            </p>
            <Link
              href="/"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-[#FFC542] text-[#111] font-black rounded-xl text-xs hover:bg-[#FFC542]/90 transition-transform active:scale-95"
            >
              Return Home <ArrowRight size={14} />
            </Link>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Header Hero Card */}
            <div className="bg-[#141414] border border-white/10 rounded-3xl p-6 sm:p-8 relative overflow-hidden shadow-2xl">
              <div className="absolute top-0 right-0 w-64 h-64 bg-[#FFC542]/5 rounded-full blur-3xl pointer-events-none" />
              
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-white/10">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-[10px] font-black tracking-widest text-[#FFC542] uppercase bg-[#FFC542]/10 border border-[#FFC542]/20 px-2 py-0.5 rounded-md">
                      {delivery.type || "EXPRESS LOGISTICS"}
                    </span>
                    <span className="text-xs text-white/40">•</span>
                    <span className="text-xs text-white/60 font-medium">
                      Tracking #{delivery.id}
                    </span>
                  </div>
                  <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
                    {delivery.itemName || "Dispatched Parcel"}
                  </h1>
                </div>

                <div className="flex items-center gap-3">
                  <div className="px-4 py-2 rounded-2xl bg-[#FFC542] text-[#111] text-xs font-black uppercase tracking-wider shadow-lg shadow-[#FFC542]/20">
                    {delivery.status.toUpperCase()}
                  </div>
                </div>
              </div>

              {/* Stepper Progress */}
              <div className="pt-8">
                <div className="grid grid-cols-5 gap-2 relative">
                  {STEPS.map((step, idx) => {
                    const isPassed = idx <= activeIndex;
                    const isCurrent = idx === activeIndex;
                    return (
                      <div key={step.key} className="flex flex-col items-center text-center group">
                        <div
                          className={`w-8 h-8 rounded-full flex items-center justify-center mb-2 transition-all duration-300 ${
                            isCurrent
                              ? "bg-[#FFC542] text-[#111] shadow-lg shadow-[#FFC542]/40 scale-110 font-black"
                              : isPassed
                              ? "bg-emerald-500 text-white font-bold"
                              : "bg-white/5 border border-white/10 text-white/30"
                          }`}
                        >
                          {isPassed && !isCurrent ? (
                            <CheckCircle2 size={16} />
                          ) : (
                            <span className="text-xs font-bold">{idx + 1}</span>
                          )}
                        </div>
                        <span
                          className={`text-[11px] font-black leading-tight ${
                            isCurrent
                              ? "text-[#FFC542]"
                              : isPassed
                              ? "text-white"
                              : "text-white/30"
                          }`}
                        >
                          {step.label}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Courier & Route Grid */}
            <div className="grid sm:grid-cols-2 gap-4">
              {/* Courier Profile */}
              <div className="bg-[#141414] border border-white/10 rounded-3xl p-6 space-y-4">
                <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-[#FFC542]">
                  <Truck size={16} /> Assigned Courier
                </div>
                {delivery.courierName ? (
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="text-base font-bold text-white">
                          {delivery.courierName}
                        </div>
                        <div className="text-xs text-white/50">
                          Unit: {delivery.riderBikeNumber || "Fleet Bike 01"}
                        </div>
                      </div>
                      <div className="w-10 h-10 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center text-xl">
                        🛵
                      </div>
                    </div>
                    {delivery.courierPhone && (
                      <a
                        href={`tel:${delivery.courierPhone}`}
                        className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-bold text-white transition-colors"
                      >
                        <Phone size={13} className="text-[#FFC542]" /> Call Courier ({delivery.courierPhone})
                      </a>
                    )}
                  </div>
                ) : (
                  <div className="py-4 text-center text-xs text-white/40">
                    Smart dispatch is assigning the nearest courier...
                  </div>
                )}
              </div>

              {/* Delivery Security Badge */}
              <div className="bg-[#141414] border border-white/10 rounded-3xl p-6 space-y-3">
                <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-emerald-400">
                  <ShieldCheck size={16} /> Verified Security
                </div>
                <p className="text-xs text-white/60 leading-relaxed">
                  Every ESDispatch delivery is protected by encrypted handover protocols and real-time telemetry. Recipient must present the security OTP code upon parcel arrival.
                </p>
                <div className="pt-1 flex items-center gap-2 text-[10px] font-bold text-white/40 uppercase tracking-widest">
                  <Sparkles size={12} className="text-[#FFC542]" /> End-to-End Escrow Protection
                </div>
              </div>
            </div>

            {/* Route Timeline */}
            <div className="bg-[#141414] border border-white/10 rounded-3xl p-6 space-y-4">
              <div className="flex items-center gap-2 text-xs font-black tracking-wider uppercase text-white/60">
                <Navigation size={15} className="text-[#FFC542]" /> Transit Route
              </div>

              <div className="space-y-4 relative pl-4 border-l-2 border-dashed border-white/10 ml-2">
                <div className="relative">
                  <span className="absolute -left-[23px] top-1 w-3.5 h-3.5 rounded-full bg-[#FFC542] ring-4 ring-[#141414]" />
                  <div className="text-[10px] font-bold text-white/40 uppercase tracking-wider">Pickup Location</div>
                  <div className="text-xs font-semibold text-white mt-0.5">{delivery.pickupAddress}</div>
                </div>

                <div className="relative">
                  <span className="absolute -left-[23px] top-1 w-3.5 h-3.5 rounded-full bg-emerald-400 ring-4 ring-[#141414]" />
                  <div className="text-[10px] font-bold text-white/40 uppercase tracking-wider">Delivery Destination</div>
                  <div className="text-xs font-semibold text-white mt-0.5">{delivery.deliveryAddress}</div>
                </div>
              </div>
            </div>

            {/* Quick Actions & Waybill Receipt */}
            <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-3xl bg-[#141414] border border-white/10 print:hidden">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#FFC542]/10 text-[#FFC542] flex items-center justify-center">
                  <FileText size={20} />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-white">Official Shipment Waybill</h4>
                  <p className="text-[10px] text-white/50">Print or export thermal receipt for records</p>
                </div>
              </div>
              <button
                onClick={() => window.print()}
                className="px-4 py-2.5 rounded-xl bg-[#FFC542] hover:bg-[#FFC542]/90 text-[#111] font-black text-xs flex items-center gap-2 transition-all shadow-md active:scale-95 cursor-pointer"
              >
                <Printer size={15} /> Print / Save Waybill
              </button>
            </div>

            {/* Printable Waybill Voucher (Only visible when printing) */}
            <div className="hidden print:block p-8 bg-white text-black font-sans max-w-xl mx-auto border-2 border-black rounded-lg">
              <div className="text-center border-b-2 border-black pb-4 mb-4">
                <h1 className="text-2xl font-black tracking-wider">ESDISPATCH</h1>
                <p className="text-[11px] font-bold uppercase tracking-widest text-gray-700">PREMIUM LOGISTICS & DISPATCH</p>
                <p className="text-xs font-mono mt-1 font-bold">WAYBILL / RECEIPT: #{delivery.id}</p>
              </div>
              
              <div className="grid grid-cols-2 gap-4 border-b border-black pb-4 mb-4 text-xs">
                <div>
                  <p className="font-bold uppercase text-gray-600">Sender / Origin</p>
                  <p className="font-bold text-sm">{delivery.senderName || "Sender"}</p>
                  <p className="text-gray-700">{delivery.pickupAddress}</p>
                </div>
                <div>
                  <p className="font-bold uppercase text-gray-600">Consignee / Destination</p>
                  <p className="font-bold text-sm">{delivery.receiverName || "Receiver"}</p>
                  <p className="text-gray-700">{delivery.deliveryAddress}</p>
                </div>
              </div>

              <div className="border-b border-black pb-4 mb-4 text-xs space-y-1.5">
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Item Description:</span>
                  <span className="font-semibold">{delivery.itemName || "General Parcel"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Status:</span>
                  <span className="font-bold uppercase">{delivery.status}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-bold text-gray-600">Assigned Courier:</span>
                  <span>{delivery.courierName || "Assigned Courier"} ({delivery.riderBikeNumber || "Fleet Unit"})</span>
                </div>
                {delivery.price && (
                  <div className="flex justify-between text-sm font-black pt-2 border-t border-dashed border-gray-300">
                    <span>Total Amount Paid:</span>
                    <span>₦{delivery.price.toLocaleString()}</span>
                  </div>
                )}
              </div>

              <div className="text-center text-[10px] text-gray-600 pt-3">
                <p>Thank you for choosing ESDISPATCH.</p>
                <p>Official Support: support@esdispatch.com | https://esdispatch.com</p>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-white/10 py-6 text-center text-xs text-white/40">
        &copy; {new Date().getFullYear()} ESDISPATCH. All rights reserved. Premium Logistics & Dispatch.
      </footer>
    </div>
  );
}
