"use client";

import React from "react";
import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-obsidian text-white flex flex-col items-center justify-center space-y-4">
      <h1 className="text-2xl font-bold text-gold">404 - Page Not Found</h1>
      <p className="text-sm text-gray-400">The page you are looking for does not exist.</p>
      <Link href="/" className="px-4 py-2 bg-gold text-obsidian rounded font-bold text-xs hover:bg-yellow-600 transition">
        Go Back Home
      </Link>
    </div>
  );
}
