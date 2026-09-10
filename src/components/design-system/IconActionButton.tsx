import React from "react";

export interface IconActionButtonProps {
  icon: React.ReactNode;
  label: string;
  onClick: (e: React.MouseEvent) => void;
  variant?: "ghost" | "tonal" | "filled_gold" | "bordered";
  className?: string;
  disabled?: boolean;
  badgeCount?: number;
  hasDot?: boolean;
  size?: "sm" | "md" | "lg";
}

export const IconActionButton: React.FC<IconActionButtonProps> = ({
  icon,
  label,
  onClick,
  variant = "tonal",
  className = "",
  disabled = false,
  badgeCount,
  hasDot = false,
  size = "md",
}) => {
  const hitSizes = {
    sm: "w-9 h-9",
    md: "w-11 h-11", // 44px min touch target (WCAG compliant)
    lg: "w-12 h-12",
  }[size];

  const variantStyles = {
    ghost: "text-[#111] dark:text-white hover:bg-black/5 dark:hover:bg-white/5",
    tonal:
      "bg-gray-100/80 dark:bg-white/10 text-[#111] dark:text-white hover:bg-gray-200 dark:hover:bg-white/15 border border-black/5 dark:border-white/10",
    filled_gold:
      "bg-[#FFB800] text-[#111] hover:bg-[#FFB800]/90 shadow-sm font-black", // Strict lock: NO white on gold
    bordered:
      "border border-black/15 dark:border-[#FFB800]/40 text-[#111] dark:text-[#FFB800] hover:bg-[#FFB800]/10",
  }[variant];

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      title={label}
      className={`relative inline-flex items-center justify-center rounded-xl transition-all duration-150 active:scale-92 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#FFB800] ${hitSizes} ${variantStyles} ${
        disabled ? "opacity-40 cursor-not-allowed" : "cursor-pointer"
      } ${className}`}
    >
      <span className="flex items-center justify-center pointer-events-none">
        {icon}
      </span>

      {hasDot && (
        <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-[#FFB800] ring-2 ring-white dark:ring-[#121212]" />
      )}

      {typeof badgeCount === "number" && badgeCount > 0 && (
        <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 bg-[#FFB800] text-[#111] text-[9px] font-black rounded-full flex items-center justify-center shadow-xs">
          {badgeCount > 99 ? "99+" : badgeCount}
        </span>
      )}
    </button>
  );
};

export default IconActionButton;
