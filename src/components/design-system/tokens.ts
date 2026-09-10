/**
 * ESDispatch Web Design System Tokens & Contracts
 * Aligned with ESDispatch Design System Recommendations.
 */

export const SpacingTokens = {
  xs: "0.25rem",   // 4px
  sm: "0.5rem",    // 8px
  md: "0.75rem",   // 12px
  lg: "1rem",      // 16px
  xl: "1.25rem",   // 20px
  "2xl": "1.5rem", // 24px
  "3xl": "2rem",   // 32px
  "4xl": "2.5rem", // 40px
} as const;

export const RadiusTokens = {
  sm: "8px",
  md: "12px",
  lg: "16px",
  xl: "20px",
  "2xl": "24px",
  "3xl": "32px",
  nav: "28px",
} as const;

export type StatusTone =
  | "neutral"
  | "amber"
  | "purple"
  | "blue"
  | "indigo"
  | "cyan"
  | "teal"
  | "green"
  | "red";

export interface StatusMeta {
  key: string;
  customerLabel: string;
  adminLabel: string;
  tone: StatusTone;
  isLive?: boolean;
  bgLight: string;
  bgDark: string;
  textLight: string;
  textDark: string;
  borderLight: string;
  borderDark: string;
  pulseColor?: string;
}

export const StatusDefinitions: Record<string, StatusMeta> = {
  PENDING: {
    key: "received",
    customerLabel: "Received",
    adminLabel: "Received",
    tone: "neutral",
    bgLight: "bg-amber-50/80",
    bgDark: "dark:bg-[#FFB800]/15",
    textLight: "text-amber-900",
    textDark: "dark:text-[#FFB800]",
    borderLight: "border-amber-200/60",
    borderDark: "dark:border-[#FFB800]/30",
  },
  RECEIVED: {
    key: "received",
    customerLabel: "Received",
    adminLabel: "Received",
    tone: "neutral",
    bgLight: "bg-amber-50/80",
    bgDark: "dark:bg-[#FFB800]/15",
    textLight: "text-amber-900",
    textDark: "dark:text-[#FFB800]",
    borderLight: "border-amber-200/60",
    borderDark: "dark:border-[#FFB800]/30",
  },
  QUEUED: {
    key: "queued",
    customerLabel: "Queued",
    adminLabel: "Waiting for rider",
    tone: "amber",
    bgLight: "bg-amber-100/70",
    bgDark: "dark:bg-amber-950/40",
    textLight: "text-amber-900",
    textDark: "dark:text-amber-300",
    borderLight: "border-amber-300",
    borderDark: "dark:border-amber-600/40",
  },
  RESERVED_NEXT: {
    key: "reserved",
    customerLabel: "Rider reserved",
    adminLabel: "Reserved next",
    tone: "purple",
    bgLight: "bg-purple-50",
    bgDark: "dark:bg-purple-950/40",
    textLight: "text-purple-900",
    textDark: "dark:text-purple-300",
    borderLight: "border-purple-200",
    borderDark: "dark:border-purple-600/40",
  },
  ASSIGNED: {
    key: "assigned",
    customerLabel: "Rider assigned",
    adminLabel: "Assigned",
    tone: "blue",
    bgLight: "bg-blue-50",
    bgDark: "dark:bg-blue-950/40",
    textLight: "text-blue-900",
    textDark: "dark:text-blue-300",
    borderLight: "border-blue-200",
    borderDark: "dark:border-blue-600/40",
  },
  PICKED_UP: {
    key: "pickup",
    customerLabel: "Heading to pickup",
    adminLabel: "Pickup phase",
    tone: "indigo",
    isLive: true,
    bgLight: "bg-indigo-50",
    bgDark: "dark:bg-indigo-950/40",
    textLight: "text-indigo-900",
    textDark: "dark:text-indigo-300",
    borderLight: "border-indigo-200",
    borderDark: "dark:border-indigo-600/40",
    pulseColor: "#6366F1",
  },
  TRANSIT: {
    key: "transit",
    customerLabel: "In transit",
    adminLabel: "In transit",
    tone: "cyan",
    isLive: true,
    bgLight: "bg-cyan-50",
    bgDark: "dark:bg-cyan-950/40",
    textLight: "text-cyan-900",
    textDark: "dark:text-cyan-300",
    borderLight: "border-cyan-200",
    borderDark: "dark:border-cyan-600/40",
    pulseColor: "#06B6D4",
  },
  OUT_FOR_DELIVERY: {
    key: "transit",
    customerLabel: "Out for delivery",
    adminLabel: "Out for delivery",
    tone: "cyan",
    isLive: true,
    bgLight: "bg-cyan-50",
    bgDark: "dark:bg-cyan-950/40",
    textLight: "text-cyan-900",
    textDark: "dark:text-cyan-300",
    borderLight: "border-cyan-200",
    borderDark: "dark:border-cyan-600/40",
    pulseColor: "#06B6D4",
  },
  ARRIVED: {
    key: "arrived",
    customerLabel: "Rider arrived",
    adminLabel: "Arrived",
    tone: "teal",
    isLive: true,
    bgLight: "bg-teal-50",
    bgDark: "dark:bg-teal-950/40",
    textLight: "text-teal-900",
    textDark: "dark:text-teal-300",
    borderLight: "border-teal-200",
    borderDark: "dark:border-teal-600/40",
    pulseColor: "#14B8A6",
  },
  HANDOVER_VERIFIED: {
    key: "arrived",
    customerLabel: "Handover verified",
    adminLabel: "Handover verified",
    tone: "teal",
    isLive: true,
    bgLight: "bg-teal-50",
    bgDark: "dark:bg-teal-950/40",
    textLight: "text-teal-900",
    textDark: "dark:text-teal-300",
    borderLight: "border-teal-200",
    borderDark: "dark:border-teal-600/40",
  },
  DELIVERED: {
    key: "delivered",
    customerLabel: "Delivered",
    adminLabel: "Delivered",
    tone: "green",
    bgLight: "bg-emerald-50",
    bgDark: "dark:bg-emerald-950/40",
    textLight: "text-emerald-900",
    textDark: "dark:text-emerald-300",
    borderLight: "border-emerald-200",
    borderDark: "dark:border-emerald-600/40",
  },
  CANCELLED: {
    key: "cancelled",
    customerLabel: "Cancelled",
    adminLabel: "Cancelled",
    tone: "red",
    bgLight: "bg-rose-50",
    bgDark: "dark:bg-rose-950/40",
    textLight: "text-rose-900",
    textDark: "dark:text-rose-300",
    borderLight: "border-rose-200",
    borderDark: "dark:border-rose-600/40",
  },
  ISSUE: {
    key: "issue",
    customerLabel: "Needs attention",
    adminLabel: "Issue",
    tone: "red",
    bgLight: "bg-red-50",
    bgDark: "dark:bg-red-950/40",
    textLight: "text-red-900",
    textDark: "dark:text-red-300",
    borderLight: "border-red-200",
    borderDark: "dark:border-red-600/40",
  },
};

export function getStatusMeta(status: string): StatusMeta {
  const norm = (status || "").trim().toUpperCase();
  if (StatusDefinitions[norm]) {
    return StatusDefinitions[norm];
  }
  const cleanLabel = status.replace(/_/g, " ");
  return {
    key: status.toLowerCase(),
    customerLabel: cleanLabel,
    adminLabel: cleanLabel,
    tone: "neutral",
    bgLight: "bg-gray-100",
    bgDark: "dark:bg-white/10",
    textLight: "text-gray-800",
    textDark: "dark:text-gray-200",
    borderLight: "border-gray-200",
    borderDark: "dark:border-white/10",
  };
}
