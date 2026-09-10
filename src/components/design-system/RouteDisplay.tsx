import React, { useState } from "react";
import { ArrowRight, MapPin, Copy, Check, ChevronDown, ChevronUp } from "lucide-react";

export interface RouteDisplayProps {
  pickupAddress: string;
  deliveryAddress: string;
  variant?: "compact" | "card" | "detail";
  senderName?: string;
  receiverName?: string;
  senderPhone?: string;
  receiverPhone?: string;
  className?: string;
  onCopy?: (text: string) => void;
}

export const RouteDisplay: React.FC<RouteDisplayProps> = ({
  pickupAddress,
  deliveryAddress,
  variant = "card",
  senderName,
  receiverName,
  senderPhone,
  receiverPhone,
  className = "",
  onCopy,
}) => {
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [expanded, setExpanded] = useState(false);

  const handleCopy = (text: string, key: string, e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    onCopy?.(text);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  if (variant === "compact") {
    return (
      <div className={`flex items-center gap-1.5 text-xs select-text ${className}`}>
        <span
          className="font-medium text-[#111] dark:text-white truncate max-w-[180px]"
          title={pickupAddress}
        >
          {pickupAddress || "Pickup"}
        </span>
        <ArrowRight className="w-3.5 h-3.5 text-[#FFB800] shrink-0" />
        <span
          className="font-medium text-[#111] dark:text-white truncate max-w-[180px]"
          title={deliveryAddress}
        >
          {deliveryAddress || "Destination"}
        </span>
      </div>
    );
  }

  if (variant === "detail") {
    return (
      <div
        className={`bg-gray-50/70 dark:bg-[#18181A] border border-black/10 dark:border-white/10 rounded-2xl p-4 space-y-3 ${className}`}
      >
        {/* Pickup Item */}
        <div className="flex items-start gap-3">
          <div className="w-5 h-5 rounded-full bg-[#FFB800]/20 flex items-center justify-center shrink-0 mt-0.5">
            <span className="w-2 h-2 rounded-full bg-[#FFB800]" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <p className="text-[10px] font-black uppercase tracking-wider text-amber-700 dark:text-[#FFB800]">
                Pickup Point
              </p>
              <button
                type="button"
                onClick={(e) => handleCopy(pickupAddress, "pickup", e)}
                className="text-black/40 dark:text-white/40 hover:text-[#FFB800] transition-colors p-1"
                title="Copy pickup address"
                aria-label="Copy pickup address"
              >
                {copiedKey === "pickup" ? (
                  <Check className="w-3 h-3 text-emerald-500" />
                ) : (
                  <Copy className="w-3 h-3" />
                )}
              </button>
            </div>
            {senderName && (
              <p className="text-xs font-semibold text-black/60 dark:text-white/60">
                {senderName} {senderPhone ? `• ${senderPhone}` : ""}
              </p>
            )}
            <p className="text-sm font-medium text-[#111] dark:text-white mt-0.5 select-text leading-relaxed break-words">
              {pickupAddress || "Pickup address not specified"}
            </p>
          </div>
        </div>

        {/* Rail */}
        <div className="ml-2.5 w-0.5 h-4 bg-gray-200 dark:bg-neutral-800" />

        {/* Destination Item */}
        <div className="flex items-start gap-3">
          <div className="w-5 h-5 rounded-full bg-black/10 dark:bg-white/10 flex items-center justify-center shrink-0 mt-0.5">
            <MapPin className="w-3 h-3 text-[#111] dark:text-white" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <p className="text-[10px] font-black uppercase tracking-wider text-black/50 dark:text-white/50">
                Delivery Destination
              </p>
              <button
                type="button"
                onClick={(e) => handleCopy(deliveryAddress, "dropoff", e)}
                className="text-black/40 dark:text-white/40 hover:text-[#FFB800] transition-colors p-1"
                title="Copy delivery address"
                aria-label="Copy delivery address"
              >
                {copiedKey === "dropoff" ? (
                  <Check className="w-3 h-3 text-emerald-500" />
                ) : (
                  <Copy className="w-3 h-3" />
                )}
              </button>
            </div>
            {receiverName && (
              <p className="text-xs font-semibold text-black/60 dark:text-white/60">
                {receiverName} {receiverPhone ? `• ${receiverPhone}` : ""}
              </p>
            )}
            <p className="text-sm font-medium text-[#111] dark:text-white mt-0.5 select-text leading-relaxed break-words">
              {deliveryAddress || "Destination address not specified"}
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Default: Card Variant
  const isLong = (pickupAddress?.length || 0) > 55 || (deliveryAddress?.length || 0) > 55;

  return (
    <div
      className={`bg-gray-50/70 dark:bg-[#141416] border border-black/5 dark:border-white/5 rounded-xl p-3 select-text ${className}`}
    >
      <div className="flex items-start gap-2.5">
        <div className="w-4 h-4 rounded-full bg-[#FFB800]/20 flex items-center justify-center shrink-0 mt-0.5">
          <span className="w-1.5 h-1.5 rounded-full bg-[#FFB800]" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-[9px] font-black uppercase tracking-wider text-amber-800 dark:text-[#FFB800]">
            From
          </p>
          <p
            className={`text-xs font-medium text-[#111] dark:text-white leading-snug ${
              expanded ? "" : "line-clamp-2"
            }`}
            title={pickupAddress}
          >
            {pickupAddress || "Pickup location"}
          </p>
        </div>
      </div>

      <div className="ml-2 w-0.5 h-3 bg-gray-200 dark:bg-neutral-800 my-1" />

      <div className="flex items-start gap-2.5">
        <div className="w-4 h-4 rounded-full bg-black/10 dark:bg-white/10 flex items-center justify-center shrink-0 mt-0.5">
          <MapPin className="w-2.5 h-2.5 text-[#111] dark:text-white" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-[9px] font-black uppercase tracking-wider text-black/40 dark:text-white/40">
            To
          </p>
          <p
            className={`text-xs font-medium text-[#111] dark:text-white leading-snug ${
              expanded ? "" : "line-clamp-2"
            }`}
            title={deliveryAddress}
          >
            {deliveryAddress || "Delivery address"}
          </p>
        </div>
      </div>

      {isLong && (
        <button
          type="button"
          onClick={() => setExpanded(!expanded)}
          className="mt-2 text-[10px] font-bold text-[#FFB800] hover:underline flex items-center gap-1 ml-auto"
        >
          {expanded ? "Show less" : "Show full address"}
          {expanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
        </button>
      )}
    </div>
  );
};

export default RouteDisplay;
