"use client";

import React, { useEffect } from "react";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("App error encountered:", error);
  }, [error]);

  return (
    <div className="min-h-screen bg-obsidian text-white flex flex-col items-center justify-center space-y-4">
      <h1 className="text-2xl font-bold text-gold">Something went wrong!</h1>
      <p className="text-sm text-gray-400">An unexpected system error occurred.</p>
      <button
        onClick={() => reset()}
        className="px-4 py-2 bg-gold text-obsidian rounded font-bold text-xs hover:bg-yellow-600 transition"
      >
        Try Again
      </button>
    </div>
  );
}
