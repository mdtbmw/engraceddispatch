import React from "react";
import { Link } from "react-router-dom";

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white flex flex-col items-center justify-center p-6 text-center">
      <div className="w-16 h-16 bg-[#FFB800]/10 border border-[#FFB800]/30 rounded-2xl flex items-center justify-center mb-6">
        <span className="font-mono font-black text-[#FFB800] text-2xl">404</span>
      </div>
      <h1 className="text-3xl font-bold font-mono tracking-wider mb-2">PAGE NOT FOUND</h1>
      <p className="text-sm text-neutral-400 max-w-sm mb-8">
        The requested URL was not found on this server.
      </p>
      <Link
        to="/"
        className="px-6 py-3 bg-[#FFB800] text-black font-mono font-bold text-xs uppercase tracking-widest rounded-xl hover:bg-[#FFA500] transition-colors"
      >
        Return Home
      </Link>
    </div>
  );
};
