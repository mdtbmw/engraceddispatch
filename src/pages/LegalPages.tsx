import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import PrivacyPanel from "@/components/PrivacyPanel";
import TermsPanel from "@/components/TermsPanel";

const PolicyShell: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <div className="min-h-screen bg-white text-[#111111] flex flex-col">
    <header className="bg-[#111111] sticky top-0 z-50 px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between gap-4">
      <Link to="/" className="flex items-center gap-3 shrink-0 group">
        <div className="w-9 h-9 rounded-xl bg-[#FFB800] flex items-center justify-center text-black font-black text-sm shadow-md">
          ES
        </div>
        <div>
          <div className="text-sm font-black tracking-wider text-white group-hover:text-[#FFB800] transition-colors">
            ESDISPATCH
          </div>
          <div className="text-[9px] font-bold tracking-widest text-[#FFB800]">
            PREMIUM LOGISTICS & DISPATCH
          </div>
        </div>
      </Link>

      <Link
        to="/"
        className="h-10 px-3.5 rounded-xl bg-white/10 hover:bg-white/15 border border-white/15 text-xs font-bold text-neutral-200 hover:text-white flex items-center gap-1.5 transition-all"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Home</span>
      </Link>
    </header>

    <main className="flex-1">{children}</main>

    <footer className="border-t border-black/10 py-6 text-center text-xs text-neutral-500 font-mono">
      &copy; {new Date().getFullYear()} ESDISPATCH. ALL RIGHTS RESERVED. PREMIUM LOGISTICS & DISPATCH.
    </footer>
  </div>
);

export const PrivacyPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <PolicyShell>
      <PrivacyPanel setView={() => navigate("/")} />
    </PolicyShell>
  );
};

export const TermsPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <PolicyShell>
      <TermsPanel setView={() => navigate("/")} />
    </PolicyShell>
  );
};
