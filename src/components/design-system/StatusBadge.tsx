import React from "react";
import { getStatusMeta } from "./tokens";

export interface StatusBadgeProps {
  status: string;
  size?: "compact" | "default" | "large";
  useAdminLabel?: boolean;
  showLivePulse?: boolean;
  className?: string;
  title?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  size = "default",
  useAdminLabel = false,
  showLivePulse = true,
  className = "",
  title,
}) => {
  const meta = getStatusMeta(status);
  const label = useAdminLabel ? meta.adminLabel : meta.customerLabel;

  const sizeClasses = {
    compact: "text-[11px] px-2 py-0.5 rounded-md gap-1 font-semibold",
    default: "text-[12px] px-2.5 py-1 rounded-lg gap-1.5 font-bold",
    large: "text-[13px] px-3.5 py-1.5 rounded-xl gap-2 font-extrabold",
  }[size];

  const dotSizes = {
    compact: "w-1.5 h-1.5",
    default: "w-2 h-2",
    large: "w-2.5 h-2.5",
  }[size];

  return (
    <span
      role="status"
      title={title || meta.adminLabel}
      className={`inline-flex items-center select-none border transition-colors ${sizeClasses} ${meta.bgLight} ${meta.bgDark} ${meta.textLight} ${meta.textDark} ${meta.borderLight} ${meta.borderDark} ${className}`}
    >
      {meta.isLive && showLivePulse && (
        <span className="relative flex items-center justify-center">
          <span
            className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${
              meta.tone === "cyan"
                ? "bg-cyan-400"
                : meta.tone === "teal"
                ? "bg-teal-400"
                : "bg-indigo-400"
            }`}
          />
          <span
            className={`relative inline-flex rounded-full ${dotSizes} ${
              meta.tone === "cyan"
                ? "bg-cyan-500"
                : meta.tone === "teal"
                ? "bg-teal-500"
                : "bg-indigo-500"
            }`}
          />
        </span>
      )}
      <span className="tracking-wide whitespace-nowrap">{label}</span>
    </span>
  );
};

export default StatusBadge;
