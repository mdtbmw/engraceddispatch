import React, { useState, useMemo } from "react";
import {
  ArrowLeft,
  Package,
  User,
  UserCheck,
  Phone,
  MessageSquare,
  Copy,
  Check,
  Printer,
  CheckCircle2,
  Clock,
  Truck,
  MapPin,
  AlertTriangle,
  ShieldCheck,
  Zap,
  UserPlus,
  RefreshCw,
  ExternalLink,
  Lock,
  DollarSign,
  ChevronRight,
} from "lucide-react";
import { StatusBadge } from "./StatusBadge";
import { RouteDisplay } from "./RouteDisplay";
import { PriceDisplay } from "./PriceDisplay";

export interface ShipmentMicroPageProps {
  delivery: any;
  drivers: any[];
  deliveries: any[];
  onBack: () => void;
  onAssignRider: (deliveryId: string, rider: any) => Promise<void>;
  onReserveRider?: (deliveryId: string, rider: any) => Promise<void>;
  onUpdateStatus: (deliveryId: string, newStatus: string) => Promise<void>;
  onPrintWaybill: (delivery: any) => void;
  addToast?: (type: any, message: string) => void;
}

export const ShipmentMicroPage: React.FC<ShipmentMicroPageProps> = ({
  delivery,
  drivers,
  deliveries,
  onBack,
  onAssignRider,
  onReserveRider,
  onUpdateStatus,
  onPrintWaybill,
  addToast,
}) => {
  const [copiedField, setCopiedField] = useState<string | null>(null);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [assigningRiderId, setAssigningRiderId] = useState<string | null>(null);
  const [showRiderPicker, setShowRiderPicker] = useState(false);
  const [riderSearch, setRiderSearch] = useState("");
  const [statusSuccessFlash, setStatusSuccessFlash] = useState(false);

  if (!delivery) {
    return (
      <div className="tab-content p-8 bg-white dark:bg-[#1a1a1a] rounded-3xl border border-black/10 dark:border-white/10 text-center space-y-3">
        <p className="text-sm font-bold text-[#111] dark:text-white">Shipment data unavailable</p>
        <button onClick={onBack} className="px-4 py-2 bg-[#FFB800] text-[#111] rounded-xl text-xs font-black cursor-pointer">
          Back to Shipments
        </button>
      </div>
    );
  }

  const copyToClipboard = (text: string, fieldName: string) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    setCopiedField(fieldName);
    if (addToast) addToast("info", `Copied ${fieldName} to clipboard`);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const getRiderActiveLoad = (riderId: string) => {
    return deliveries.filter(
      (d: any) =>
        (d.riderId === riderId || d.driverId === riderId) &&
        ["ASSIGNED", "TRANSIT", "OUT_FOR_DELIVERY"].includes(d.status)
    ).length;
  };

  // Rank available drivers by availability, active load, and location proximity
  const rankedDrivers = useMemo(() => {
    return [...drivers]
      .filter((r) => {
        if (!riderSearch.trim()) return true;
        const q = riderSearch.toLowerCase();
        return (
          (r.name || "").toLowerCase().includes(q) ||
          (r.phone || "").toLowerCase().includes(q) ||
          (r.bikeNumber || "").toLowerCase().includes(q)
        );
      })
      .map((r) => {
        const load = getRiderActiveLoad(r.id);
        let score = 50;
        if (r.isOnline !== false) score += 30;
        if (load === 0) score += 20;
        else if (load === 1) score += 10;
        else if (load >= 3) score -= 20;

        return { r, load, score };
      })
      .sort((a, b) => b.score - a.score);
  }, [drivers, deliveries, riderSearch]);

  const assignedDriver = useMemo(() => {
    if (!delivery?.riderId && !delivery?.driverId) return null;
    const targetId = delivery.riderId || delivery.driverId;
    return (
      drivers.find((d) => d.id === targetId || d.uid === targetId) || {
        id: targetId,
        name: delivery.courierName || delivery.driverName || "Assigned Courier",
        phone: delivery.courierPhone || "",
        bikeNumber: delivery.riderBikeNumber || "Fleet Bike",
      }
    );
  }, [delivery, drivers]);

  const hasRider = Boolean(
    delivery?.riderId ||
      delivery?.driverId ||
      (delivery?.courierName && delivery.courierName !== "Unassigned")
  );

  const handleStatusClick = async (newStatus: string) => {
    if (updatingStatus) return;
    if (
      !hasRider &&
      ["TRANSIT", "ARRIVED", "OUT_FOR_DELIVERY", "HANDOVER_VERIFIED", "DELIVERED"].includes(
        newStatus
      )
    ) {
      if (addToast) {
        addToast("warning", "Please assign a rider first before moving to " + newStatus.replace(/_/g, " "));
      } else {
        alert("Please assign a rider first before moving to " + newStatus.replace(/_/g, " "));
      }
      setShowRiderPicker(true);
      return;
    }

    setUpdatingStatus(true);
    try {
      await onUpdateStatus(delivery.id, newStatus);
      setStatusSuccessFlash(true);
      setTimeout(() => setStatusSuccessFlash(false), 2500);
      if (addToast) addToast("success", `Shipment status updated to ${newStatus.replace(/_/g, " ")}`);
    } catch (err: any) {
      if (addToast) addToast("error", err?.message || "Failed to update status");
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleQuickAssign = async (rider: any) => {
    setAssigningRiderId(rider.id);
    try {
      await onAssignRider(delivery.id, rider);
      setShowRiderPicker(false);
      setStatusSuccessFlash(true);
      setTimeout(() => setStatusSuccessFlash(false), 3000);
      if (addToast) {
        addToast(
          "success",
          `${rider.name} assigned! You can now advance shipment status below.`
        );
      }
    } catch (err: any) {
      if (addToast) addToast("error", err?.message || "Failed to assign rider");
    } finally {
      setAssigningRiderId(null);
    }
  };

  const statusPipeline = [
    { key: "PENDING", label: "Booked", icon: Package },
    { key: "ASSIGNED", label: "Assigned", icon: UserCheck },
    { key: "TRANSIT", label: "In Transit", icon: Truck },
    { key: "ARRIVED", label: "Arrived", icon: MapPin },
    { key: "DELIVERED", label: "Delivered", icon: CheckCircle2 },
  ];

  const currentStatus = (delivery?.status || "PENDING").toUpperCase();

  const getStepIndex = (status: string) => {
    switch (status) {
      case "PENDING":
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
  };

  const activeStepIdx = getStepIndex(currentStatus);

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Micro-Page Header Navigation Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="flex items-center gap-2 px-3.5 py-2 rounded-xl bg-gray-100 dark:bg-[#262626] text-gray-800 dark:text-gray-200 hover:bg-black/10 dark:hover:bg-white/10 font-black text-xs transition-all cursor-pointer group"
          >
            <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform text-amber-600 dark:text-[#FFB800]" />
            Back to Shipments
          </button>
          <div className="h-5 w-px bg-black/10 dark:border-white/10" />
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-mono font-bold text-gray-600 dark:text-gray-400">
                #{delivery?.id ? delivery.id.slice(0, 8) : "--------"}
              </span>
              <StatusBadge status={delivery?.status || "PENDING"} size="default" useAdminLabel={true} />
              {delivery.category && (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-gray-100 dark:bg-[#333] text-gray-800 dark:text-gray-200">
                  {delivery.category}
                </span>
              )}
            </div>
            <h1 className="text-lg font-black text-[#111] dark:text-white mt-0.5">
              {delivery.itemName || "Consignment Parcel"}
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={() => onPrintWaybill(delivery)}
            className="px-4 py-2 bg-gray-100 dark:bg-[#262626] hover:bg-gray-200 dark:hover:bg-[#333] text-[#111] dark:text-white font-bold text-xs rounded-xl flex items-center gap-1.5 transition-all shadow-xs cursor-pointer"
            title="Print Thermal Waybill"
          >
            <Printer size={15} className="text-amber-600 dark:text-[#FFB800]" />
            Print Waybill
          </button>
          <button
            onClick={() => copyToClipboard(delivery.id, "Shipment ID")}
            className="px-3 py-2 bg-gray-100 dark:bg-[#262626] text-gray-700 dark:text-gray-300 hover:text-black dark:hover:text-white rounded-xl text-xs font-bold flex items-center gap-1 transition-all"
            title="Copy ID"
          >
            {copiedField === "Shipment ID" ? <Check size={14} className="text-emerald-500" /> : <Copy size={14} />}
          </button>
        </div>
      </div>

      {/* Immediate Status Switcher & Pipeline Banner */}
      <div
        className={`p-6 rounded-3xl border transition-all duration-300 ${
          statusSuccessFlash
            ? "bg-[#FFB800]/15 border-[#FFB800] ring-2 ring-[#FFB800]/40"
            : "bg-white dark:bg-[#1a1a1a] border-black/10 dark:border-white/10"
        } shadow-sm space-y-5`}
      >
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
          <div>
            <span className="text-[10px] font-black uppercase tracking-wider text-amber-800 dark:text-[#FFB800]">
              Operational Lifecycle Controller
            </span>
            <h2 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">
              Dispatch State: <span className="text-amber-800 dark:text-[#FFB800] uppercase font-black">{currentStatus.replace(/_/g, " ")}</span>
            </h2>
            <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">
              1-click immediate status switcher. Advancing status automatically updates customer and courier notifications.
            </p>
          </div>

          {/* Quick 1-Click Action Buttons */}
          <div className="flex items-center gap-2 flex-wrap">
            {currentStatus === "PENDING" && (
              <button
                onClick={() => setShowRiderPicker(true)}
                className="px-4 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer"
              >
                <UserPlus size={15} />
                Assign Rider to Dispatch
              </button>
            )}

            {currentStatus === "ASSIGNED" && (
              <>
                <button
                  disabled={updatingStatus}
                  onClick={() => handleStatusClick("PICKED_UP")}
                  className="px-4 py-2 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
                >
                  <Package size={14} /> Mark Picked Up
                </button>
                <button
                  disabled={updatingStatus}
                  onClick={() => handleStatusClick("TRANSIT")}
                  className="px-4 py-2 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
                >
                  <Truck size={14} /> Mark In Transit
                </button>
              </>
            )}

            {currentStatus === "PICKED_UP" && (
              <button
                disabled={updatingStatus}
                onClick={() => handleStatusClick("TRANSIT")}
                className="px-4 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
              >
                <Truck size={15} />
                Mark In Transit
              </button>
            )}

            {currentStatus === "TRANSIT" && (
              <button
                disabled={updatingStatus}
                onClick={() => handleStatusClick("ARRIVED")}
                className="px-4 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
              >
                <MapPin size={15} />
                Mark Arrived (At Destination)
              </button>
            )}

            {currentStatus === "ARRIVED" && (
              <button
                disabled={updatingStatus}
                onClick={() => handleStatusClick("DELIVERED")}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
              >
                <CheckCircle2 size={16} />
                Verify OTP & Complete Delivery
              </button>
            )}

            {(currentStatus === "OUT_FOR_DELIVERY" || currentStatus === "HANDOVER_VERIFIED") && (
              <button
                disabled={updatingStatus}
                onClick={() => handleStatusClick("DELIVERED")}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-black flex items-center gap-1.5 shadow-sm transition-all cursor-pointer disabled:opacity-50"
              >
                <CheckCircle2 size={16} />
                Mark Delivered
              </button>
            )}

            {currentStatus !== "DELIVERED" && currentStatus !== "CANCELLED" && (
              <button
                disabled={updatingStatus}
                onClick={() => {
                  if (confirm("Are you sure you want to cancel this delivery booking?")) {
                    handleStatusClick("CANCELLED");
                  }
                }}
                className="px-3 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-600 dark:text-red-400 rounded-xl text-xs font-bold transition-all cursor-pointer"
              >
                Cancel Booking
              </button>
            )}
          </div>
        </div>

        {/* 5-Stage Step Progress Rail */}
        <div className="pt-2">
          <div className="relative">
            <div className="overflow-hidden h-2 mb-4 flex rounded-full bg-gray-100 dark:bg-[#262626]">
              <div
                style={{ width: `${(activeStepIdx / (statusPipeline.length - 1)) * 100}%` }}
                className="shadow-none flex flex-col text-center whitespace-nowrap text-[#111] font-black justify-center bg-[#FFB800] transition-all duration-500"
              />
            </div>
            <div className="grid grid-cols-5 gap-2 text-center">
              {statusPipeline.map((step, idx) => {
                const isPassed = idx <= activeStepIdx;
                const isCurrent = idx === activeStepIdx;
                return (
                  <div key={step.key} className="flex flex-col items-center">
                    <div
                      className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-black mb-1 transition-all ${
                        isCurrent
                          ? "bg-[#FFB800] text-[#111] ring-4 ring-[#FFB800]/20 scale-110"
                          : isPassed
                          ? "bg-[#FFB800]/80 text-[#111]"
                          : "bg-gray-100 dark:bg-[#262626] text-gray-500 dark:text-gray-400"
                      }`}
                    >
                      {idx + 1}
                    </div>
                    <span
                      className={`text-[10px] font-bold ${
                        isCurrent
                          ? "text-amber-800 dark:text-[#FFB800] font-black"
                          : isPassed
                          ? "text-[#111] dark:text-white"
                          : "text-gray-500 dark:text-gray-400"
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
      </div>

      {/* Main Grid: Courier/Rider on Left, Routing on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Courier & Rider Assignment Block */}
        <div className="space-y-6">
          <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-black text-[#111] dark:text-white flex items-center gap-2">
                <Truck className="w-4 h-4 text-amber-600 dark:text-[#FFB800]" /> Assigned Fleet Courier
              </h3>
              {assignedDriver && (
                <button
                  onClick={() => setShowRiderPicker(!showRiderPicker)}
                  className="text-xs font-bold text-amber-800 dark:text-[#FFB800] hover:underline cursor-pointer"
                >
                  {showRiderPicker ? "Close" : "Change Rider"}
                </button>
              )}
            </div>

            {assignedDriver ? (
              <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/5 dark:border-white/5 space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full bg-[#FFB800] text-[#111] font-black text-base flex items-center justify-center shrink-0">
                    {(assignedDriver.name || "C").charAt(0).toUpperCase()}
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="font-extrabold text-sm text-[#111] dark:text-white truncate">
                      {assignedDriver.name}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400 font-mono font-medium">
                      {assignedDriver.bikeNumber || "Fleet Bike"}
                    </p>
                  </div>
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0" title="Online" />
                </div>

                {assignedDriver.phone && (
                  <div className="flex items-center gap-2 pt-2 border-t border-black/5 dark:border-white/5">
                    <a
                      href={`tel:${assignedDriver.phone}`}
                      className="flex-1 py-2 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Phone size={13} /> Call {assignedDriver.phone}
                    </a>
                    <a
                      href={`https://wa.me/${assignedDriver.phone.replace(/[^0-9]/g, "")}`}
                      target="_blank"
                      rel="noreferrer"
                      className="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1 transition-colors"
                    >
                      <MessageSquare size={13} /> WhatsApp
                    </a>
                  </div>
                )}
              </div>
            ) : (
              <div className="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-center space-y-2">
                <AlertTriangle className="w-6 h-6 text-amber-500 mx-auto" />
                <p className="text-xs font-bold text-[#111] dark:text-white">
                  No Courier Assigned Yet
                </p>
                <p className="text-[11px] text-gray-700 dark:text-gray-300">
                  Select an available fleet driver below to dispatch this booking.
                </p>
                <button
                  onClick={() => setShowRiderPicker(true)}
                  className="mt-2 w-full py-2 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] font-black text-xs rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-xs cursor-pointer"
                >
                  <UserPlus size={14} /> Open Courier Selector
                </button>
              </div>
            )}

            {/* Quick Available Rider List / Picker */}
            {(showRiderPicker || !assignedDriver) && (
              <div className="pt-2 border-t border-black/10 dark:border-white/10 space-y-3 animate-fade-in">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-black uppercase text-gray-700 dark:text-gray-300">
                    Available Fleet Couriers
                  </span>
                  <span className="text-[10px] text-gray-500 dark:text-gray-400 font-semibold">
                    {rankedDrivers.length} couriers
                  </span>
                </div>

                <input
                  type="text"
                  placeholder="Search name, phone, bike number..."
                  value={riderSearch}
                  onChange={(e) => setRiderSearch(e.target.value)}
                  className="w-full bg-gray-50 dark:bg-[#222] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 outline-none focus:ring-2 focus:ring-[#FFB800]/40"
                />

                <div className="max-h-60 overflow-y-auto space-y-2 pr-1">
                  {rankedDrivers.length === 0 ? (
                    <p className="text-xs text-gray-600 dark:text-gray-400 text-center py-4">
                      No matching couriers found.
                    </p>
                  ) : (
                    rankedDrivers.slice(0, 8).map(({ r, load, score }) => (
                      <div
                        key={r.id}
                        className="p-2.5 rounded-xl bg-gray-50 dark:bg-[#222] border border-black/5 dark:border-white/5 flex items-center justify-between gap-2 hover:border-[#FFB800]/40 transition-all"
                      >
                        <div className="min-w-0 flex items-center gap-2">
                          <div className="w-8 h-8 rounded-full bg-amber-100 dark:bg-[#FFB800]/20 text-amber-900 dark:text-[#FFB800] font-bold text-xs flex items-center justify-center shrink-0">
                            {(r.name || "C").charAt(0)}
                          </div>
                          <div className="min-w-0">
                            <p className="text-xs font-bold text-[#111] dark:text-white truncate">
                              {r.name}
                            </p>
                            <p className="text-[10px] text-gray-600 dark:text-gray-400 truncate">
                              {load === 0 ? "Free (0 drops)" : `${load} active drops`} • {r.phone || "No phone"}
                            </p>
                          </div>
                        </div>

                        <button
                          disabled={assigningRiderId === r.id}
                          onClick={() => handleQuickAssign(r)}
                          className="px-3 py-1.5 bg-[#FFB800] hover:bg-[#FFB800]/80 text-[#111] rounded-lg text-xs font-black shrink-0 transition-all shadow-2xs cursor-pointer disabled:opacity-50 flex items-center gap-1"
                        >
                          {assigningRiderId === r.id ? (
                            <RefreshCw className="w-3 h-3 animate-spin" />
                          ) : (
                            "Quick Assign"
                          )}
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Handover OTP & Security */}
          <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-3">
            <h3 className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300 flex items-center gap-1.5">
              <Lock className="w-3.5 h-3.5 text-amber-600 dark:text-[#FFB800]" /> Handover Security OTP
            </h3>
            <div className="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/25 text-center">
              <p className="text-[10px] font-bold text-gray-700 dark:text-gray-300 mb-1">
                Recipient Verification Code
              </p>
              <div className="flex items-center justify-center gap-2">
                <span className="font-mono text-2xl font-black tracking-widest text-[#111] dark:text-white">
                  {delivery.otpCode || "----"}
                </span>
                {delivery.otpCode && (
                  <button
                    onClick={() => copyToClipboard(delivery.otpCode, "OTP Code")}
                    className="p-1.5 text-gray-500 dark:text-gray-400 hover:text-black dark:hover:text-white rounded-lg"
                    title="Copy OTP"
                  >
                    {copiedField === "OTP Code" ? (
                      <Check size={14} className="text-emerald-500" />
                    ) : (
                      <Copy size={14} />
                    )}
                  </button>
                )}
              </div>
              <p className="text-[10px] text-gray-600 dark:text-gray-400 mt-1">
                Courier requests this 4-digit code at destination to complete drop-off.
              </p>
            </div>
          </div>
        </div>

        {/* Right Columns: Sender, Receiver, Route & Particulars */}
        <div className="lg:col-span-2 space-y-6">
          {/* Routing Overview Card */}
          <div className="bg-white dark:bg-[#1a1a1a] p-6 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-4">
            <h3 className="text-sm font-black text-[#111] dark:text-white flex items-center gap-2">
              <MapPin className="w-4 h-4 text-amber-600 dark:text-[#FFB800]" /> Benin City Delivery Route
            </h3>
            <RouteDisplay
              pickupAddress={delivery.pickupAddress || "Pickup Address Unavailable"}
              deliveryAddress={delivery.deliveryAddress || "Delivery Address Unavailable"}
              variant="detail"
            />
          </div>

          {/* Shipper & Consignee Contact Cards (2 Columns) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Shipper Card */}
            <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                  Sender (Shipper)
                </span>
                <span className="w-2 h-2 rounded-full bg-[#FFB800]" />
              </div>
              <div>
                <p className="font-extrabold text-sm text-[#111] dark:text-white">
                  {delivery.senderName || "Sender Not Specified"}
                </p>
                <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">
                  {delivery.pickupAddress || "Benin City"}
                </p>
              </div>

              {delivery.senderPhone && (
                <div className="flex items-center gap-2 pt-2 border-t border-black/5 dark:border-white/5">
                  <a
                    href={`tel:${delivery.senderPhone}`}
                    className="flex-1 py-2 bg-gray-100 dark:bg-[#262626] hover:bg-gray-200 dark:hover:bg-[#333] text-[#111] dark:text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors"
                  >
                    <Phone size={13} /> {delivery.senderPhone}
                  </a>
                  <a
                    href={`https://wa.me/${delivery.senderPhone.replace(/[^0-9]/g, "")}`}
                    target="_blank"
                    rel="noreferrer"
                    className="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1 transition-colors"
                  >
                    <MessageSquare size={13} />
                  </a>
                </div>
              )}
            </div>

            {/* Consignee Card */}
            <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                  Receiver (Consignee)
                </span>
                <span className="w-2 h-2 rounded-full bg-emerald-500" />
              </div>
              <div>
                <p className="font-extrabold text-sm text-[#111] dark:text-white">
                  {delivery.receiverName || "Receiver Not Specified"}
                </p>
                <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">
                  {delivery.deliveryAddress || "Benin City"}
                </p>
              </div>

              {delivery.receiverPhone && (
                <div className="flex items-center gap-2 pt-2 border-t border-black/5 dark:border-white/5">
                  <a
                    href={`tel:${delivery.receiverPhone}`}
                    className="flex-1 py-2 bg-gray-100 dark:bg-[#262626] hover:bg-gray-200 dark:hover:bg-[#333] text-[#111] dark:text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors"
                  >
                    <Phone size={13} /> {delivery.receiverPhone}
                  </a>
                  <a
                    href={`https://wa.me/${delivery.receiverPhone.replace(/[^0-9]/g, "")}`}
                    target="_blank"
                    rel="noreferrer"
                    className="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1 transition-colors"
                  >
                    <MessageSquare size={13} />
                  </a>
                </div>
              )}
            </div>
          </div>

          {/* Consignment Particulars & Pricing */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Particulars */}
            <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-3">
              <h4 className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                Consignment Details
              </h4>
              <div className="space-y-2 text-xs">
                <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5">
                  <span className="text-gray-600 dark:text-gray-400">Item Name:</span>
                  <span className="font-bold text-[#111] dark:text-white">{delivery.itemName || "Parcel"}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5">
                  <span className="text-gray-600 dark:text-gray-400">Weight:</span>
                  <span className="font-bold text-[#111] dark:text-white">{delivery.weight || 1} kg</span>
                </div>
                <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5">
                  <span className="text-gray-600 dark:text-gray-400">Quantity:</span>
                  <span className="font-bold text-[#111] dark:text-white">{delivery.quantity || 1} unit(s)</span>
                </div>
                <div className="flex justify-between py-1">
                  <span className="text-gray-600 dark:text-gray-400">Category:</span>
                  <span className="font-bold text-[#111] dark:text-white">{delivery.category || "Standard"}</span>
                </div>
              </div>
            </div>

            {/* Financial Breakdown */}
            <div className="bg-white dark:bg-[#1a1a1a] p-5 rounded-3xl border border-black/10 dark:border-white/10 shadow-xs space-y-3">
              <h4 className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                Payment & Fare
              </h4>
              <div className="space-y-2 text-xs">
                <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5">
                  <span className="text-gray-600 dark:text-gray-400">Delivery Charge:</span>
                  <PriceDisplay amount={delivery.price || 0} variant="compact" />
                </div>
                {delivery.tipAmount > 0 && (
                  <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5">
                    <span className="text-gray-600 dark:text-gray-400">Driver Tip:</span>
                    <span className="font-mono font-bold text-emerald-600 dark:text-emerald-400">
                      +₦{delivery.tipAmount.toLocaleString()}
                    </span>
                  </div>
                )}
                <div className="flex justify-between py-1 border-b border-black/5 dark:border-white/5 font-extrabold text-sm">
                  <span className="text-[#111] dark:text-white">Total Amount:</span>
                  <PriceDisplay
                    amount={(delivery.price || 0) + (delivery.tipAmount || 0)}
                    variant="standard"
                  />
                </div>
                <div className="flex justify-between items-center py-1">
                  <span className="text-gray-600 dark:text-gray-400">Payment Status:</span>
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30">
                    {delivery.paymentStatus || "PAID"}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
