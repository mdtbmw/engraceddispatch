import React from "react";
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from "lucide-react";
import { AppNotification, NotificationLifecycleManager } from "./NotificationLifecycle";

export interface NotificationBannerProps {
  notifications: AppNotification[];
  onDismiss: (id: string, dedupeKey: string) => void;
  onAction?: (actionTarget?: string) => void;
  maxVisible?: number;
}

export const NotificationBanner: React.FC<NotificationBannerProps> = ({
  notifications,
  onDismiss,
  onAction,
  maxVisible = 3,
}) => {
  const visibleItems = notifications.slice(-maxVisible);

  if (visibleItems.length === 0) return null;

  return (
    <div
      aria-live="polite"
      className="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none"
    >
      {visibleItems.map((notif) => {
        const severityStyles = {
          success: "bg-emerald-950/90 text-emerald-100 border-emerald-700/50",
          warning: "bg-amber-950/90 text-amber-100 border-amber-700/50",
          error: "bg-rose-950/90 text-rose-100 border-rose-700/50",
          info: "bg-neutral-900/95 text-white border-white/10",
        }[notif.severity];

        const icon = {
          success: <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />,
          warning: <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />,
          error: <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />,
          info: <Info className="w-4 h-4 text-[#FFB800] shrink-0 mt-0.5" />,
        }[notif.severity];

        return (
          <div
            key={notif.id}
            role="alert"
            className={`pointer-events-auto rounded-2xl p-4 shadow-xl border backdrop-blur-md transition-all duration-300 animate-slide-in flex items-start gap-3 ${severityStyles}`}
          >
            {icon}
            <div className="flex-1 min-w-0">
              <p className="text-xs font-black leading-tight tracking-wide">
                {notif.title}
              </p>
              <p className="text-xs opacity-80 mt-1 leading-snug break-words">
                {notif.message}
              </p>
              {notif.actionTarget && (
                <button
                  type="button"
                  onClick={() => onAction?.(notif.actionTarget)}
                  className="mt-2 text-[11px] font-bold text-[#FFB800] hover:underline cursor-pointer"
                >
                  View Details →
                </button>
              )}
            </div>

            <button
              type="button"
              aria-label="Dismiss notification"
              onClick={() => {
                NotificationLifecycleManager.dismiss(notif.dedupeKey);
                onDismiss(notif.id, notif.dedupeKey);
              }}
              className="opacity-60 hover:opacity-100 p-1 rounded-md transition-opacity cursor-pointer"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        );
      })}
    </div>
  );
};

export default NotificationBanner;
