"use client";
import React, { useState, useEffect } from "react";
import { 
  Radio, 
  Send, 
  AlertTriangle, 
  CheckCircle2, 
  Clock, 
  Users, 
  Trash2, 
  Bell, 
  ShieldAlert, 
  Sparkles, 
  Eye, 
  ExternalLink,
  Smartphone
} from "lucide-react";
import { 
  collection, 
  onSnapshot, 
  addDoc, 
  doc, 
  deleteDoc, 
  updateDoc, 
  Timestamp, 
  writeBatch, 
  query, 
  orderBy 
} from "firebase/firestore";

export interface BroadcastItem {
  id: string;
  title: string;
  message: string;
  targetAudience: "ALL" | "RIDERS" | "VENDORS" | "CUSTOMERS";
  priority: "NORMAL" | "PROMO" | "URGENT_TICKER" | "CRITICAL_ALERT";
  actionUrl?: string;
  active: boolean;
  recipientsCount: number;
  createdAt: any;
  expiresAt?: any;
  sentBy: string;
}

interface BroadcastNewsTabProps {
  db: any;
  users: any[];
  currentUserEmail?: string;
  addLog: (action: string, details: string) => Promise<void> | void;
  addToast: (type: "info" | "success" | "error", message: string) => void;
}

export default function BroadcastNewsTab({
  db,
  users,
  currentUserEmail,
  addLog,
  addToast
}: BroadcastNewsTabProps) {
  const [broadcasts, setBroadcasts] = useState<BroadcastItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Form states
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  const [targetAudience, setTargetAudience] = useState<"ALL" | "RIDERS" | "VENDORS" | "CUSTOMERS">("ALL");
  const [priority, setPriority] = useState<"NORMAL" | "PROMO" | "URGENT_TICKER" | "CRITICAL_ALERT">("NORMAL");
  const [durationHours, setDurationHours] = useState("48");
  const [actionUrl, setActionUrl] = useState("");
  const [showPreview, setShowPreview] = useState(false);

  // Subscribe to broadcast announcements
  useEffect(() => {
    if (!db) return;
    const q = query(collection(db, "broadcast_news"), orderBy("createdAt", "desc"));
    const unsub = onSnapshot(q, (snap) => {
      const items: BroadcastItem[] = [];
      snap.forEach((d) => {
        const data = d.data();
        items.push({
          id: d.id,
          title: data.title || "Announcement",
          message: data.message || "",
          targetAudience: data.targetAudience || "ALL",
          priority: data.priority || "NORMAL",
          actionUrl: data.actionUrl || "",
          active: data.active !== false,
          recipientsCount: data.recipientsCount || 0,
          createdAt: data.createdAt,
          expiresAt: data.expiresAt,
          sentBy: data.sentBy || "Admin",
        });
      });
      setBroadcasts(items);
      setLoading(false);
    }, (err) => {
      console.warn("Broadcast news listener notice:", err);
      setLoading(false);
    });

    return () => unsub();
  }, [db]);

  // Target audience resolution
  const getTargetRecipients = () => {
    const activeUsers = users.filter(u => !u.isDeleted);
    switch (targetAudience) {
      case "RIDERS":
        return activeUsers.filter(u => u.role === "rider");
      case "VENDORS":
        return activeUsers.filter(u => u.role === "vendor");
      case "CUSTOMERS":
        return activeUsers.filter(u => u.role === "customer" || !u.role);
      case "ALL":
      default:
        return activeUsers;
    }
  };

  const recipients = getTargetRecipients();

  const handleSendBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !message.trim()) {
      addToast("error", "Please provide both an announcement title and message body.");
      return;
    }

    setSubmitting(true);
    try {
      const now = Date.now();
      const hours = parseInt(durationHours) || 48;
      const expiresAtMs = hours > 0 ? now + (hours * 3600 * 1000) : null;
      const expiresAtTimestamp = expiresAtMs ? Timestamp.fromMillis(expiresAtMs) : null;

      // 1. Create main broadcast document
      const broadcastRef = await addDoc(collection(db, "broadcast_news"), {
        title: title.trim(),
        message: message.trim(),
        targetAudience,
        priority,
        actionUrl: actionUrl.trim() || null,
        active: true,
        recipientsCount: recipients.length,
        createdAt: Timestamp.now(),
        expiresAt: expiresAtTimestamp,
        sentBy: currentUserEmail || "ESDispatch Admin",
      });

      // 2. Fan-out to targeted users' notification inboxes in chunks of 450
      const chunkSize = 450;
      for (let i = 0; i < recipients.length; i += chunkSize) {
        const chunk = recipients.slice(i, i + chunkSize);
        const batch = writeBatch(db);
        chunk.forEach((u) => {
          const uRef = u.id || u.uid;
          if (uRef) {
            const notifDoc = doc(collection(db, "users", uRef, "notifications"));
            batch.set(notifDoc, {
              title: title.trim(),
              message: message.trim(),
              time: "Just now",
              read: false,
              isBroadcast: true,
              broadcastId: broadcastRef.id,
              priority,
              actionUrl: actionUrl.trim() || null,
              createdAt: Timestamp.now(),
              timestamp: Date.now(),
            });
          }
        });
        await batch.commit();
      }

      // 3. Mirror to root notifications collection for real-time web sync
      try {
        await addDoc(collection(db, "notifications"), {
          title: `[BROADCAST] ${title.trim()}`,
          description: message.trim(),
          time: "Just now",
          read: false,
          targetAudience,
          priority,
          createdAt: Timestamp.now(),
          timestamp: Date.now(),
        });
      } catch (_) {}

      addLog("Broadcast Sent", `Published [${priority}] to ${targetAudience} (${recipients.length} recipients): "${title}"`);
      addToast("success", `Broadcast dispatched to ${recipients.length} ${targetAudience.toLowerCase()}!`);

      // Reset form
      setTitle("");
      setMessage("");
      setActionUrl("");
      setShowPreview(false);
    } catch (err: any) {
      console.error("Broadcast dispatch failed:", err);
      addToast("error", "Broadcast failed: " + (err.message || "Unknown error"));
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevokeBroadcast = async (id: string, itemTitle: string) => {
    try {
      await updateDoc(doc(db, "broadcast_news", id), {
        active: false,
        revokedAt: Timestamp.now(),
      });
      addLog("Revoke Broadcast", `Deactivated broadcast "${itemTitle}"`);
      addToast("info", "Announcement has been deactivated.");
    } catch (err: any) {
      addToast("error", "Could not revoke broadcast: " + err.message);
    }
  };

  const handleDeleteBroadcast = async (id: string, itemTitle: string) => {
    try {
      await deleteDoc(doc(db, "broadcast_news", id));
      addLog("Delete Broadcast", `Deleted broadcast record "${itemTitle}"`);
      addToast("success", "Broadcast record removed.");
    } catch (err: any) {
      addToast("error", "Delete failed: " + err.message);
    }
  };

  const priorityStyle = (p: string) => {
    switch (p) {
      case "CRITICAL_ALERT":
        return "bg-red-500/15 text-red-700 dark:text-red-400 border border-red-500/30";
      case "URGENT_TICKER":
        return "bg-amber-500/15 text-amber-700 dark:text-amber-400 border border-amber-500/30";
      case "PROMO":
        return "bg-purple-500/15 text-purple-700 dark:text-purple-300 border border-purple-500/30";
      case "NORMAL":
      default:
        return "bg-blue-500/15 text-blue-700 dark:text-blue-300 border border-blue-500/30";
    }
  };

  return (
    <div className="tab-content space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-[#111] dark:text-white flex items-center gap-3">
            <Radio className="w-6 h-6 text-[#FFB800] animate-pulse" /> Broadcast News & Communication Center
          </h1>
          <p className="text-xs font-medium text-gray-600 dark:text-gray-400 mt-1">
            Publish operational bulletins, traffic alerts, promo news, and direct push announcements across the entire fleet and customer base.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="px-3 py-1.5 rounded-xl bg-amber-500/10 text-amber-800 dark:text-[#FFB800] font-black text-xs border border-amber-500/20">
            {recipients.length} Reachable in {targetAudience}
          </span>
        </div>
      </div>

      {/* Grid: Create Announcement & Live Mobile Preview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Creator Form */}
        <div className="lg:col-span-2 bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm space-y-5">
          <div className="flex items-center justify-between pb-3 border-b border-black/5 dark:border-white/10">
            <h2 className="text-base font-extrabold text-[#111] dark:text-white flex items-center gap-2">
              <Send size={18} className="text-[#FFB800]" /> Draft New Announcement
            </h2>
            <button
              type="button"
              onClick={() => setShowPreview(!showPreview)}
              className="text-xs font-bold text-amber-800 dark:text-[#FFB800] hover:underline flex items-center gap-1.5"
            >
              <Eye size={14} /> {showPreview ? "Hide Preview" : "Show Mobile Preview"}
            </button>
          </div>

          <form onSubmit={handleSendBroadcast} className="space-y-4">
            {/* Title */}
            <div>
              <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                Headline / Title *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Heavy Rain Alert: GRA & Ikpoba Hill Delivery Updates"
                className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 placeholder:text-gray-400"
                required
              />
            </div>

            {/* Audience & Priority Selection Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Target Audience */}
              <div>
                <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                  Target Audience Segment
                </label>
                <select
                  value={targetAudience}
                  onChange={(e) => setTargetAudience(e.target.value as any)}
                  className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 cursor-pointer"
                >
                  <option value="ALL">All Network (Everyone)</option>
                  <option value="RIDERS">Couriers & Drivers Only</option>
                  <option value="VENDORS">Enlisted Store Vendors Only</option>
                  <option value="CUSTOMERS">Customers Only</option>
                </select>
              </div>

              {/* Priority */}
              <div>
                <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                  Notification Priority & Style
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as any)}
                  className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 cursor-pointer"
                >
                  <option value="NORMAL">Standard Info / News</option>
                  <option value="PROMO">Promotion & Special Offer</option>
                  <option value="URGENT_TICKER">Operational Advisory (Amber)</option>
                  <option value="CRITICAL_ALERT">Critical Emergency Alert (Red)</option>
                </select>
              </div>
            </div>

            {/* Message Body */}
            <div>
              <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                Announcement Body *
              </label>
              <textarea
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Type the message body that will display in the mobile notification center and app banner..."
                rows={4}
                className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-medium text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 placeholder:text-gray-400"
                required
              />
            </div>

            {/* Expiry & Action URL */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                  Auto-Expire After
                </label>
                <select
                  value={durationHours}
                  onChange={(e) => setDurationHours(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 cursor-pointer"
                >
                  <option value="24">24 Hours</option>
                  <option value="48">48 Hours (Recommended)</option>
                  <option value="72">3 Days</option>
                  <option value="168">7 Days</option>
                  <option value="0">Permanent (Manual Revocation Only)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-extrabold text-gray-700 dark:text-gray-300 mb-1.5">
                  Deep Link / Action URL (Optional)
                </label>
                <input
                  type="text"
                  value={actionUrl}
                  onChange={(e) => setActionUrl(e.target.value)}
                  placeholder="e.g. esdispatch://marketplace or https://..."
                  className="w-full px-4 py-3 rounded-2xl bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 text-xs font-bold text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/50 placeholder:text-gray-400"
                />
              </div>
            </div>

            {/* Submit Action */}
            <div className="pt-2 flex items-center justify-between">
              <div className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                Will fan out to <span className="font-extrabold text-[#111] dark:text-white">{recipients.length} devices</span>
              </div>
              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-3 bg-[#FFB800] text-[#111] font-black text-xs rounded-2xl hover:bg-[#FFB800]/90 transition-all shadow-md flex items-center gap-2 cursor-pointer disabled:opacity-50"
              >
                {submitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-[#111] border-t-transparent rounded-full animate-spin" />
                    Broadcasting...
                  </>
                ) : (
                  <>
                    <Send size={15} /> Publish Broadcast
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Live Preview Card */}
        <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-black/5 dark:border-white/10">
            <h3 className="text-sm font-extrabold text-[#111] dark:text-white flex items-center gap-2">
              <Smartphone size={16} className="text-[#FFB800]" /> Mobile Feed Preview
            </h3>
            <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider">Preview</span>
          </div>

          <div className="bg-gray-100 dark:bg-[#111] rounded-2xl p-4 border border-black/5 dark:border-white/5 space-y-3">
            <div className="flex items-start justify-between gap-2">
              <span className={`px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider ${priorityStyle(priority)}`}>
                {priority.replace("_", " ")}
              </span>
              <span className="text-[10px] text-gray-500 font-bold">Just now</span>
            </div>

            <h4 className="text-xs font-black text-[#111] dark:text-white leading-snug">
              {title || "Your Announcement Headline Will Appear Here"}
            </h4>

            <p className="text-[11px] text-gray-600 dark:text-gray-300 font-medium leading-relaxed">
              {message || "The detailed announcement message body will render here for customers, couriers, or vendors with pristine readability."}
            </p>

            {actionUrl && (
              <div className="pt-2">
                <span className="inline-flex items-center gap-1.5 text-[10px] font-black text-amber-800 dark:text-[#FFB800]">
                  View Details <ExternalLink size={10} />
                </span>
              </div>
            )}
          </div>

          <div className="p-3 bg-amber-500/10 rounded-xl border border-amber-500/20 text-[11px] text-amber-900 dark:text-amber-200 font-medium">
            💡 Announcements marked as <span className="font-bold">CRITICAL_ALERT</span> pin to the top of the mobile tracking and dashboard screens until acknowledged.
          </div>
        </div>
      </div>

      {/* Active Broadcasts History Table */}
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 shadow-sm space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-black/5 dark:border-white/10">
          <div>
            <h3 className="text-base font-extrabold text-[#111] dark:text-white flex items-center gap-2">
              <Bell size={18} className="text-[#FFB800]" /> Broadcast History & Active Notices
            </h3>
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium mt-0.5">
              Review and revoke published operational announcements in real time.
            </p>
          </div>
          <span className="text-xs font-extrabold text-gray-700 dark:text-gray-300">
            {broadcasts.length} Total Records
          </span>
        </div>

        {broadcasts.length === 0 ? (
          <div className="text-center py-12 text-gray-500 font-medium">
            <Radio className="w-12 h-12 mx-auto mb-3 text-[#FFB800]/50" />
            <p className="font-extrabold text-sm text-gray-900 dark:text-white">No broadcast announcements published yet</p>
            <p className="text-xs mt-1 text-gray-600 dark:text-gray-400">Use the form above to broadcast updates to the fleet or customers.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead className="bg-gray-50 dark:bg-[#222] sticky top-0 z-10 backdrop-blur-md">
                <tr>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Announcement</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Audience</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Priority</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Status</th>
                  <th className="text-left font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Sent At</th>
                  <th className="text-right font-bold text-gray-700 dark:text-gray-300 uppercase tracking-wider text-[11px] p-4 border-b border-black/10 dark:border-white/10">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/10">
                {broadcasts.map((b) => (
                  <tr key={b.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="p-4">
                      <div className="font-extrabold text-[#111] dark:text-white text-sm">{b.title}</div>
                      <div className="text-gray-600 dark:text-gray-400 font-medium text-[11px] mt-0.5 line-clamp-1 max-w-md">{b.message}</div>
                    </td>
                    <td className="p-4 font-bold text-[#111] dark:text-white">
                      <span className="px-2.5 py-1 rounded-lg bg-gray-100 dark:bg-[#222] font-black text-[10px]">
                        {b.targetAudience} ({b.recipientsCount})
                      </span>
                    </td>
                    <td className="p-4">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-black uppercase ${priorityStyle(b.priority)}`}>
                        {b.priority.replace("_", " ")}
                      </span>
                    </td>
                    <td className="p-4">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold ${b.active ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300" : "bg-gray-200 text-gray-700 dark:bg-gray-800 dark:text-gray-400"}`}>
                        {b.active ? "ACTIVE" : "REVOKED"}
                      </span>
                    </td>
                    <td className="p-4 text-gray-600 dark:text-gray-400 font-medium text-[11px]">
                      {b.createdAt?.toDate ? b.createdAt.toDate().toLocaleString("en-US", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }) : "Recent"}
                    </td>
                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {b.active && (
                          <button
                            onClick={() => handleRevokeBroadcast(b.id, b.title)}
                            className="px-3 py-1.5 bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/20 rounded-xl text-[10px] font-bold hover:bg-amber-500/20 transition-colors cursor-pointer"
                          >
                            Revoke
                          </button>
                        )}
                        <button
                          onClick={() => handleDeleteBroadcast(b.id, b.title)}
                          className="p-1.5 text-red-600 dark:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors cursor-pointer"
                          title="Delete Record"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
