"use client";

import React, { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { CheckCircle2, ShieldCheck, ArrowRight, Smartphone } from "lucide-react";

function VerifiedContent() {
  const searchParams = useSearchParams();
  const email = searchParams.get("email") || "";

  return (
    <div className="min-h-screen bg-[#050505] text-white flex flex-col items-center justify-center p-6 selection:bg-[#FFB800] selection:text-black">
      <div className="w-full max-w-md bg-[#0F0F11] border border-white/10 rounded-3xl p-8 text-center shadow-2xl relative overflow-hidden backdrop-blur-xl">
        {/* Ambient Gold Glow */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-48 h-48 bg-[#FFB800]/10 rounded-full blur-3xl pointer-events-none" />

        {/* Brand Header */}
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/5 border border-white/10 text-[11px] font-bold tracking-widest text-[#FFB800] uppercase mb-6">
          <ShieldCheck className="w-3.5 h-3.5" />
          ESDispatch Telemetry
        </div>

        {/* Success Icon */}
        <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-[#FFB800]/10 border border-[#FFB800]/30 flex items-center justify-center shadow-lg shadow-[#FFB800]/5">
          <CheckCircle2 className="w-10 h-10 text-[#FFB800]" />
        </div>

        <h1 className="text-2xl font-black tracking-tight mb-2">
          Identity Authenticated
        </h1>
        <p className="text-sm text-neutral-400 mb-6 leading-relaxed">
          {email ? (
            <>
              Your account (<span className="text-white font-medium">{email}</span>) has been verified. VIP priority routing and escrow protection are active.
            </>
          ) : (
            "Your email address has been successfully verified. Priority fleet routing and escrow protection are now active on your account."
          )}
        </p>

        {/* Next Step Box */}
        <div className="bg-black/40 border border-white/5 rounded-2xl p-4 mb-6 text-left flex items-start gap-3">
          <Smartphone className="w-5 h-5 text-[#FFB800] shrink-0 mt-0.5" />
          <div className="text-xs text-neutral-300">
            <span className="font-bold text-white block mb-0.5">Return to Mobile App</span>
            Open the ESDispatch app on your phone. Your verification status will update automatically.
          </div>
        </div>

        {/* Actions */}
        <div className="space-y-3">
          <Link
            href="/"
            className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-[#FFB800] text-black font-bold text-sm hover:bg-[#e5a600] transition-colors shadow-lg shadow-[#FFB800]/20"
          >
            Go to ESDispatch Web Portal
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {/* Footer */}
        <div className="mt-8 pt-6 border-t border-white/5 text-[11px] text-neutral-500 tracking-wider uppercase font-semibold">
          ESDISPATCH &bull; Premium Logistics &bull; 256-Bit SSL
        </div>
      </div>
    </div>
  );
}

export default function VerifiedPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#050505] text-white flex items-center justify-center">Loading...</div>}>
      <VerifiedContent />
    </Suspense>
  );
}
