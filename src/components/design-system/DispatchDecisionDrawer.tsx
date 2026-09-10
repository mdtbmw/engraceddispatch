import React, { useState, useMemo } from "react";
import {
  X,
  User,
  Phone,
  MessageSquare,
  ShieldCheck,
  AlertTriangle,
  Clock,
  CheckCircle2,
  Navigation,
  Sparkles,
  Bike,
  Package,
} from "lucide-react";
import { StatusBadge } from "./StatusBadge";
import { RouteDisplay } from "./RouteDisplay";
import { PriceDisplay } from "./PriceDisplay";
import { IconActionButton } from "./IconActionButton";

export interface DeliveryRecord {
  id: string;
  itemName?: string;
  category?: string;
  status: string;
  price?: number;
  pickupAddress?: string;
  deliveryAddress?: string;
  senderName?: string;
  senderPhone?: string;
  receiverName?: string;
  receiverPhone?: string;
  weight?: number;
  quantity?: number;
  courierName?: string;
  courierPhone?: string;
  riderId?: string;
  driverId?: string;
  dateString?: string;
  createdAt?: any;
  otpCode?: string;
  paymentStatus?: string;
  notes?: string;
}

export interface RiderRecord {
  uid: string;
  name: string;
  phone?: string;
  isOnline?: boolean;
  status?: string;
  rating?: number;
  deliveryCount?: number;
  currentLoad?: number;
  lat?: number;
  lng?: number;
  bikeNumber?: string;
  zone?: string;
}

export interface DispatchDecisionDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  delivery: DeliveryRecord | null;
  riders: RiderRecord[];
  onAssignRider: (deliveryId: string, rider: RiderRecord) => Promise<void>;
  onReserveRider?: (deliveryId: string, rider: RiderRecord) => Promise<void>;
  onUpdateStatus: (deliveryId: string, newStatus: string) => Promise<void>;
  onAddLog?: (action: string, details: string) => void;
}

export const DispatchDecisionDrawer: React.FC<DispatchDecisionDrawerProps> = ({
  isOpen,
  onClose,
  delivery,
  riders,
  onAssignRider,
  onReserveRider,
  onUpdateStatus,
}) => {
  const [selectedRiderId, setSelectedRiderId] = useState<string>("");
  const [loadingAction, setLoadingAction] = useState<string | null>(null);

  // Close on Escape key
  React.useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isOpen) {
        onClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  // Proximity and smart rider scoring for Benin City
  const scoredRiders = useMemo(() => {
    if (!delivery) return [];

    const beninZones = [
      "GRA",
      "Ring Road",
      "Ugbowo",
      "Airport Road",
      "Sapele Road",
      "Upper Sakponba",
      "Ikpoba Hill",
      "New Benin",
      "Uselu",
      "Ekenwan",
      "Siluko",
      "Aduwawa",
    ];
    const pickupLower = (delivery.pickupAddress || "").toLowerCase();
    const matchedZone = beninZones.find((z) => pickupLower.includes(z.toLowerCase()));

    return riders
      .map((r) => {
        let score = 50;
        let matchReason = "Available for dispatch";

        if (!r.isOnline) {
          return {
            ...r,
            score: 0,
            matchReason: "Offline • Cannot assign",
          };
        }

        if (r.status === "SUSPENDED" || r.status === "DEACTIVATED") {
          return {
            ...r,
            score: 0,
            matchReason: "Account restricted",
          };
        }

        // Proximity match
        if (matchedZone && r.zone && r.zone.toLowerCase().includes(matchedZone.toLowerCase())) {
          score += 30;
          matchReason = `Near pickup zone (${matchedZone})`;
        }

        // Rating bonus
        if ((r.rating || 0) >= 4.8) {
          score += 15;
        }

        // Active load penalty
        const load = r.currentLoad || 0;
        if (load === 0) {
          score += 20;
          if (!matchedZone) matchReason = "Zero active runs • Immediate dispatch";
        } else if (load === 1) {
          score += 5;
          if (!matchedZone) matchReason = "1 active order • Can reserve next";
        } else {
          score -= 20;
          matchReason = `${load} active orders • Heavy load`;
        }

        return {
          ...r,
          score,
          matchReason,
        };
      })
      .sort((a, b) => b.score - a.score);
  }, [delivery, riders]);

  if (!isOpen || !delivery) return null;

  const handleAction = async (actionKey: string, fn: () => Promise<void>) => {
    try {
      setLoadingAction(actionKey);
      await fn();
    } finally {
      setLoadingAction(null);
    }
  };

  const selectedRider = riders.find((r) => r.uid === selectedRiderId);

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-xs transition-opacity duration-300">
      {/* Click outside backdrop */}
      <div className="flex-1" onClick={onClose} />

      {/* Drawer surface */}
      <div className="w-full max-w-xl bg-white dark:bg-[#121214] border-l border-black/10 dark:border-white/10 h-full overflow-y-auto shadow-2xl flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-black/5 dark:border-white/5 flex items-center justify-between sticky top-0 bg-white/95 dark:bg-[#121214]/95 backdrop-blur-md z-10">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono text-black/50 dark:text-white/50">
                #{delivery.id.slice(-8).toUpperCase()}
              </span>
              <StatusBadge status={delivery.status} size="compact" useAdminLabel />
              {delivery.category && (
                <span className="text-[10px] font-black uppercase px-2 py-0.5 rounded-md bg-gray-100 dark:bg-white/10 text-black/70 dark:text-white/70">
                  {delivery.category}
                </span>
              )}
            </div>
            <h2 className="text-xl font-black text-[#111] dark:text-white mt-1">
              {delivery.itemName || "Delivery Dispatch Request"}
            </h2>
          </div>

          <IconActionButton
            icon={<X className="w-5 h-5" />}
            label="Close Drawer"
            onClick={onClose}
            variant="ghost"
          />
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 flex-1">
          {/* Customer & Recipient Details */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Sender */}
            <div className="p-4 bg-gray-50/80 dark:bg-[#18181A] rounded-2xl border border-black/5 dark:border-white/5">
              <p className="text-[10px] font-black uppercase tracking-wider text-black/40 dark:text-white/40">
                Sender Client
              </p>
              <p className="text-sm font-bold text-[#111] dark:text-white mt-1">
                {delivery.senderName || "Direct Booking"}
              </p>
              {delivery.senderPhone && (
                <div className="flex items-center gap-2 mt-2">
                  <a
                    href={`tel:${delivery.senderPhone}`}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-[#FFB800] hover:underline"
                  >
                    <Phone className="w-3 h-3" /> {delivery.senderPhone}
                  </a>
                  <a
                    href={`https://wa.me/234${delivery.senderPhone.replace(/^0/, "")}`}
                    target="_blank"
                    rel="noreferrer"
                    className="p-1 rounded-md text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/10"
                    title="Open WhatsApp"
                  >
                    <MessageSquare className="w-3.5 h-3.5" />
                  </a>
                </div>
              )}
            </div>

            {/* Recipient */}
            <div className="p-4 bg-gray-50/80 dark:bg-[#18181A] rounded-2xl border border-black/5 dark:border-white/5">
              <p className="text-[10px] font-black uppercase tracking-wider text-black/40 dark:text-white/40">
                Recipient Contact
              </p>
              <p className="text-sm font-bold text-[#111] dark:text-white mt-1">
                {delivery.receiverName || "Anonymous Receiver"}
              </p>
              {delivery.receiverPhone && (
                <div className="flex items-center gap-2 mt-2">
                  <a
                    href={`tel:${delivery.receiverPhone}`}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-[#FFB800] hover:underline"
                  >
                    <Phone className="w-3 h-3" /> {delivery.receiverPhone}
                  </a>
                  <a
                    href={`https://wa.me/234${delivery.receiverPhone.replace(/^0/, "")}`}
                    target="_blank"
                    rel="noreferrer"
                    className="p-1 rounded-md text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/10"
                    title="Open WhatsApp"
                  >
                    <MessageSquare className="w-3.5 h-3.5" />
                  </a>
                </div>
              )}
            </div>
          </div>

          {/* Route Section */}
          <div>
            <h3 className="text-xs font-black uppercase tracking-wider text-black/50 dark:text-white/50 mb-2">
              Route & Landmark Coordinates
            </h3>
            <RouteDisplay
              pickupAddress={delivery.pickupAddress || ""}
              deliveryAddress={delivery.deliveryAddress || ""}
              variant="detail"
              senderName={delivery.senderName}
              receiverName={delivery.receiverName}
              senderPhone={delivery.senderPhone}
              receiverPhone={delivery.receiverPhone}
            />
          </div>

          {/* Logistics Financials & Parcel Metrics */}
          <div className="p-4 bg-gray-50/80 dark:bg-[#18181A] rounded-2xl border border-black/5 dark:border-white/5 flex items-center justify-between">
            <div>
              <p className="text-[10px] font-black uppercase tracking-wider text-black/40 dark:text-white/40">
                Settlement Amount
              </p>
              <PriceDisplay
                amount={delivery.price || 0}
                variant="metric"
                className="mt-0.5"
              />
            </div>
            <div className="text-right">
              <p className="text-[10px] font-black uppercase tracking-wider text-black/40 dark:text-white/40">
                Cargo Specs
              </p>
              <p className="text-xs font-bold text-[#111] dark:text-white mt-1">
                {delivery.weight || 1} kg • {delivery.quantity || 1} unit(s)
              </p>
              <p className="text-[10px] text-emerald-600 dark:text-emerald-400 font-bold mt-0.5">
                {delivery.paymentStatus || "PAID"}
              </p>
            </div>
          </div>

          {/* Rider Assignment Engine */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-black uppercase tracking-wider text-black/50 dark:text-white/50 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-[#FFB800]" />
                Rider Dispatch Ranking
              </h3>
              <span className="text-[11px] font-semibold text-black/40 dark:text-white/40">
                {scoredRiders.filter((r) => r.isOnline).length} Active Online
              </span>
            </div>

            {/* Currently assigned rider banner if present */}
            {(delivery.courierName || delivery.riderId) && (
              <div className="p-3.5 rounded-xl bg-blue-50 dark:bg-blue-950/40 border border-blue-200 dark:border-blue-700/40 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-black text-xs">
                    {(delivery.courierName || "R").charAt(0)}
                  </div>
                  <div>
                    <p className="text-xs font-black text-blue-900 dark:text-blue-200">
                      {delivery.courierName || "Assigned Rider"}
                    </p>
                    <p className="text-[10px] text-blue-700 dark:text-blue-300">
                      Currently handling this route
                    </p>
                  </div>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-blue-200 dark:bg-blue-800 text-blue-900 dark:text-blue-100">
                  Assigned
                </span>
              </div>
            )}

            {/* Rider select list */}
            <div className="max-h-60 overflow-y-auto space-y-2 pr-1">
              {scoredRiders.map((rider) => {
                const isSelected = selectedRiderId === rider.uid;
                return (
                  <div
                    key={rider.uid}
                    onClick={() => rider.isOnline && setSelectedRiderId(rider.uid)}
                    className={`p-3 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
                      isSelected
                        ? "border-[#FFB800] bg-[#FFB800]/10 dark:bg-[#FFB800]/15"
                        : "border-black/5 dark:border-white/5 bg-gray-50/50 dark:bg-[#18181A] hover:border-[#FFB800]/50"
                    } ${!rider.isOnline ? "opacity-40 cursor-not-allowed" : ""}`}
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div className="w-8 h-8 rounded-full bg-black/10 dark:bg-white/10 flex items-center justify-center font-bold text-xs shrink-0">
                        {rider.name.charAt(0)}
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center gap-1.5">
                          <p className="text-xs font-bold text-[#111] dark:text-white truncate">
                            {rider.name}
                          </p>
                          {rider.isOnline && (
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0" />
                          )}
                        </div>
                        <p className="text-[10px] text-black/50 dark:text-white/50 truncate">
                          {rider.matchReason}
                        </p>
                      </div>
                    </div>

                    <div className="text-right shrink-0">
                      <span className="text-xs font-black text-[#FFB800]">
                        ★ {(rider.rating || 5.0).toFixed(1)}
                      </span>
                      <p className="text-[9px] text-black/40 dark:text-white/40">
                        {rider.currentLoad || 0} active
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Rider action buttons */}
            {selectedRider && (
              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  disabled={loadingAction !== null}
                  onClick={() =>
                    handleAction("assign", () => onAssignRider(delivery.id, selectedRider))
                  }
                  className="flex-1 py-2.5 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] font-black text-xs rounded-xl transition-all shadow-sm flex items-center justify-center gap-1.5"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  {loadingAction === "assign" ? "Assigning..." : `Assign to ${selectedRider.name}`}
                </button>

                {onReserveRider && (
                  <button
                    type="button"
                    disabled={loadingAction !== null}
                    onClick={() =>
                      handleAction("reserve", () => onReserveRider(delivery.id, selectedRider))
                    }
                    className="py-2.5 px-3 bg-purple-100 hover:bg-purple-200 dark:bg-purple-900/40 dark:hover:bg-purple-900/60 text-purple-900 dark:text-purple-200 font-bold text-xs rounded-xl transition-all"
                  >
                    Reserve Next
                  </button>
                )}
              </div>
            )}
          </div>

          {/* Valid Next Actions (Enforcing Section 13 & 17 Lifecycle) */}
          <div className="space-y-2 pt-2 border-t border-black/5 dark:border-white/5">
            <h3 className="text-xs font-black uppercase tracking-wider text-black/50 dark:text-white/50">
              Valid Status Transitions
            </h3>
            <div className="flex flex-wrap gap-2">
              {delivery.status === "PENDING" && (
                <button
                  type="button"
                  onClick={() => handleAction("queue", () => onUpdateStatus(delivery.id, "QUEUED"))}
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-amber-100 dark:bg-amber-900/40 text-amber-900 dark:text-amber-200 hover:bg-amber-200"
                >
                  Queue Request
                </button>
              )}

              {delivery.status === "ASSIGNED" && (
                <button
                  type="button"
                  onClick={() =>
                    handleAction("pickup", () => onUpdateStatus(delivery.id, "PICKED_UP"))
                  }
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-indigo-100 dark:bg-indigo-900/40 text-indigo-900 dark:text-indigo-200 hover:bg-indigo-200"
                >
                  Confirm Pickup Phase
                </button>
              )}

              {["ASSIGNED", "PICKED_UP"].includes(delivery.status) && (
                <button
                  type="button"
                  onClick={() =>
                    handleAction("transit", () => onUpdateStatus(delivery.id, "TRANSIT"))
                  }
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-cyan-100 dark:bg-cyan-900/40 text-cyan-900 dark:text-cyan-200 hover:bg-cyan-200"
                >
                  Mark In Transit
                </button>
              )}

              {delivery.status === "TRANSIT" && (
                <button
                  type="button"
                  onClick={() =>
                    handleAction("arrived", () => onUpdateStatus(delivery.id, "ARRIVED"))
                  }
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-teal-100 dark:bg-teal-900/40 text-teal-900 dark:text-teal-200 hover:bg-teal-200"
                >
                  Mark Arrived
                </button>
              )}

              {["ARRIVED", "HANDOVER_VERIFIED", "OUT_FOR_DELIVERY"].includes(delivery.status) && (
                <button
                  type="button"
                  onClick={() =>
                    handleAction("delivered", () => onUpdateStatus(delivery.id, "DELIVERED"))
                  }
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-100 dark:bg-emerald-900/40 text-emerald-900 dark:text-emerald-200 hover:bg-emerald-200"
                >
                  Verify Handover & Complete
                </button>
              )}

              {delivery.status !== "CANCELLED" && delivery.status !== "DELIVERED" && (
                <button
                  type="button"
                  onClick={() =>
                    handleAction("cancel", () => onUpdateStatus(delivery.id, "CANCELLED"))
                  }
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 hover:bg-rose-100"
                >
                  Cancel Order
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DispatchDecisionDrawer;
