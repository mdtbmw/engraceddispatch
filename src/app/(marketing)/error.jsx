"use client";

import { useEffect } from "react";

export default function Error({ error, reset }) {
  useEffect(() => {
    console.error("Marketing error encountered:", error);
  }, [error]);

  return (
    <div className="min-h-screen bg-obsidian text-white flex flex-col items-center justify-center space-y-4 p-6 text-center">
      <h1 className="text-2xl font-bold text-gold">Something went wrong!</h1>
      <p className="text-sm text-gray-400 max-w-md">
        We encountered an unexpected error. Please try again or contact us if
        the problem persists.
      </p>
      <button
        onClick={reset}
        className="px-8 py-2.5 bg-gold text-obsidian rounded font-bold text-xs hover:bg-yellow-600 transition"
      >
        Try Again
      </button>
    </div>
  );
}
