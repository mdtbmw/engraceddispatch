import React from "react";

export interface PriceDisplayProps {
  amount: number;
  variant?: "compact" | "standard" | "admin" | "muted" | "metric";
  className?: string;
  showSign?: boolean;
}

export const PriceDisplay: React.FC<PriceDisplayProps> = ({
  amount,
  variant = "standard",
  className = "",
  showSign = false,
}) => {
  const safeAmount = Number.isFinite(amount) ? amount : 0;

  const formatAmount = () => {
    const hasDecimals = variant === "standard" || variant === "admin";
    return new Intl.NumberFormat("en-NG", {
      minimumFractionDigits: hasDecimals ? 2 : 0,
      maximumFractionDigits: hasDecimals ? 2 : 0,
    }).format(safeAmount);
  };

  const styleClasses = {
    compact: "text-sm font-bold text-[#111] dark:text-white",
    standard: "text-base font-bold text-[#111] dark:text-white",
    admin:
      "text-xs font-bold text-[#111] dark:text-white font-mono tabular-nums tracking-tight",
    metric:
      "text-2xl sm:text-3xl font-black text-[#111] dark:text-white tracking-tight",
    muted: "text-xs font-medium text-black/50 dark:text-white/50",
  }[variant];

  const sign = showSign && safeAmount > 0 ? "+" : "";

  return (
    <span
      className={`inline-flex items-baseline tabular-nums select-text ${styleClasses} ${className}`}
    >
      <span className="mr-0.5 text-[0.9em] font-semibold text-[#FFB800] dark:text-[#FFB800]">
        ₦
      </span>
      <span>
        {sign}
        {formatAmount()}
      </span>
    </span>
  );
};

export default PriceDisplay;
