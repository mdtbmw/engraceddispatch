import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { db } from "@/lib/firebase";
import { doc, onSnapshot } from "firebase/firestore";
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

export const PublicTrackingPage: React.FC = () => {
  const params = useParams<{ id: string }>();
  const trackingId = params?.id || "";

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
          // Fallback to parcels collection
          const parcelRef = doc(db, "parcels", trackingId);
          onSnapshot(parcelRef, (pSnap) => {
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
    if (s.includes("TRANSIT") || s.includes("OUT FOR DELIVERY") || s.includes("DISPATCHED")) return 2;
    if (s.includes("ASSIGNED") || s.includes("ACCEPTED")) return 1;
    return 0;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center p-6 text-white font-sans">
        <div className="text-center space-y-4">
          <div className="w-12 h-12 border-4 border-[#FFB800] border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-sm font-mono tracking-widest text-[#FFB800]">CONNECTING SATELLITE TELEMETRY...</p>
        </div>
      </div>
    );
  }

  if (notFound || !delivery) {
    return (
      <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center p-6 text-white font-sans">
        <div className="max-w-md w-full bg-[#121212] border border-[#262626] rounded-2xl p-8 text-center space-y-6">
          <div className="w-16 h-16 bg-red-500/10 rounded-2xl flex items-center justify-center mx-auto text-red-400">
            <AlertCircle className="w-8 h-8" />
          </div>
          <div>
            <h1 className="text-2xl font-bold font-mono tracking-wider text-[#FFB800]">WAYBILL NOT FOUND</h1>
            <p className="text-sm text-neutral-400 mt-2">
              No active shipment matches <span className="text-white font-mono">{trackingId}</span>.
            </p>
          </div>
          <Link
            to="/"
            className="block w-full py-3.5 bg-[#FFB800] text-black font-black tracking-wider text-xs uppercase rounded-xl hover:bg-[#FFA500] transition-colors"
          >
            RETURN HOME
          </Link>
        </div>
      </div>
    );
  }

  const activeIdx = getActiveStepIndex(delivery.status);

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white font-sans pb-16">
      {/* Top Navbar */}
      <header className="border-b border-[#262626] bg-[#121212]/80 backdrop-blur-md sticky top-0 z-40 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 rounded-lg bg-[#FFB800] flex items-center justify-center text-black font-black text-sm">
            ES
          </div>
          <span className="font-mono font-bold tracking-widest text-sm text-[#FFB800]">ESDISPATCH LIVE RADAR</span>
        </div>
        <Link
          to="/"
          className="text-xs font-mono text-neutral-400 hover:text-white transition-colors flex items-center space-x-1"
        >
          <span>Main Site</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </header>

      {/* Main Container */}
      <div className="max-w-4xl mx-auto px-4 pt-8 space-y-6">
        {/* Status Card */}
        <div className="bg-[#121212] border border-[#FFB800]/30 rounded-2xl p-6 sm:p-8 relative overflow-hidden shadow-2xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#262626] pb-6">
            <div>
              <div className="flex items-center space-x-2">
                <span className="px-2.5 py-1 rounded-md bg-[#FFB800]/10 text-[#FFB800] border border-[#FFB800]/20 text-xs font-mono font-bold">
                  {delivery.id}
                </span>
                <span className="text-xs text-neutral-500 font-mono">
                  {delivery.itemName || "Express Consignment"}
                </span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black mt-2 tracking-tight text-white uppercase">
                {delivery.status}
              </h2>
            </div>
            <button
              onClick={() => window.print()}
              className="self-start sm:self-auto px-4 py-2.5 bg-[#1C1C1C] hover:bg-[#262626] border border-[#333] text-xs font-mono text-[#FFB800] rounded-xl flex items-center space-x-2 transition-colors"
            >
              <Printer className="w-4 h-4" />
              <span>Print Waybill</span>
            </button>
          </div>

          {/* Stepper */}
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-5 gap-4">
            {STEPS.map((s, idx) => {
              const isDone = idx <= activeIdx;
              const isCurrent = idx === activeIdx;
              return (
                <div
                  key={s.key}
                  className={`p-4 rounded-xl border transition-all ${
                    isCurrent
                      ? "bg-[#FFB800]/10 border-[#FFB800] text-white"
                      : isDone
                      ? "bg-[#1C1C1C] border-[#333] text-neutral-300"
                      : "bg-[#121212] border-[#222] text-neutral-600"
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-mono font-bold">0{idx + 1}</span>
                    {isDone ? (
                      <CheckCircle2 className={`w-4 h-4 ${isCurrent ? "text-[#FFB800]" : "text-emerald-400"}`} />
                    ) : (
                      <Clock className="w-4 h-4 text-neutral-700" />
                    )}
                  </div>
                  <h4 className="text-xs font-bold font-mono tracking-wide uppercase">{s.label}</h4>
                  <p className="text-[10px] text-neutral-400 mt-1">{s.desc}</p>
                </div>
              );
            })}
          </div>

          {/* Route Details */}
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-6 bg-[#0A0A0A] p-5 rounded-xl border border-[#222]">
            <div className="flex items-start space-x-3">
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center shrink-0 mt-0.5">
                <MapPin className="w-4 h-4" />
              </div>
              <div>
                <p className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider">Pickup Location</p>
                <p className="text-xs text-white font-medium mt-0.5">{delivery.pickupAddress}</p>
              </div>
            </div>

            <div className="flex items-start space-x-3">
              <div className="w-8 h-8 rounded-lg bg-[#FFB800]/10 text-[#FFB800] flex items-center justify-center shrink-0 mt-0.5">
                <Navigation className="w-4 h-4" />
              </div>
              <div>
                <p className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider">Destination</p>
                <p className="text-xs text-white font-medium mt-0.5">{delivery.deliveryAddress}</p>
              </div>
            </div>
          </div>

          {/* Courier Banner */}
          {delivery.courierName && (
            <div className="mt-6 flex items-center justify-between bg-[#1C1C1C] p-4 rounded-xl border border-[#333]">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full bg-[#FFB800] flex items-center justify-center text-black font-black">
                  <Truck className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-bold text-white">{delivery.courierName}</p>
                  <p className="text-[10px] font-mono text-[#FFB800]">
                    In-House Courier • {delivery.riderBikeNumber || "Dispatch Unit"}
                  </p>
                </div>
              </div>
              {delivery.courierPhone && (
                <a
                  href={`tel:${delivery.courierPhone}`}
                  className="px-3.5 py-2 bg-[#FFB800] text-black font-bold text-xs rounded-lg flex items-center space-x-1.5 hover:bg-[#FFA500] transition-colors"
                >
                  <Phone className="w-3.5 h-3.5" />
                  <span>Call Courier</span>
                </a>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
