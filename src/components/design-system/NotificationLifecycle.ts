/**
 * ESDispatch Notification Lifecycle Engine
 * Section 14: Stateful notifications with deduplication and dismissal persistence.
 */

export interface AppNotification {
  id: string;
  eventType: "order_created" | "rider_assigned" | "status_changed" | "issue" | "payout" | "system";
  entityId: string;
  title: string;
  message: string;
  severity: "info" | "success" | "warning" | "error";
  createdAt: number;
  readAt?: number;
  dismissedAt?: number;
  dedupeKey: string;
  actionTarget?: string;
}

const DISMISSED_KEYS_STORAGE = "esdispatch_dismissed_notif_keys";

export class NotificationLifecycleManager {
  private static dismissedKeys: Set<string> = new Set();
  private static initialized = false;

  private static init() {
    if (this.initialized || typeof window === "undefined") return;
    try {
      const stored = localStorage.getItem(DISMISSED_KEYS_STORAGE);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed)) {
          this.dismissedKeys = new Set(parsed);
        }
      }
    } catch (_) {
      this.dismissedKeys = new Set();
    }
    this.initialized = true;
  }

  static isDismissed(dedupeKey: string): boolean {
    this.init();
    return this.dismissedKeys.has(dedupeKey);
  }

  static dismiss(dedupeKey: string): void {
    this.init();
    this.dismissedKeys.add(dedupeKey);
    try {
      // Keep set bounded to last 200 items to prevent unbounded localStorage growth
      const list = Array.from(this.dismissedKeys).slice(-200);
      localStorage.setItem(DISMISSED_KEYS_STORAGE, JSON.stringify(list));
    } catch (_) {}
  }

  static generateDedupeKey(eventType: string, entityId: string, stateOrStatus: string): string {
    return `${eventType}_${entityId}_${stateOrStatus}`.toLowerCase();
  }

  /**
   * Evaluates if a notification should be presented to the user.
   * Suppresses duplicate notifications if the exact event has already been dismissed.
   */
  static shouldShow(notif: AppNotification): boolean {
    if (this.isDismissed(notif.dedupeKey)) {
      return false;
    }
    return true;
  }
}
