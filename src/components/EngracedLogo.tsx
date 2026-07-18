"use client";
import React from "react";

interface Props {
  size?: number;
  className?: string;
  gold?: boolean;
}

export default function EngracedLogo({ size = 40, className = "", gold = false }: Props) {
  const g = gold ? "#FFB800" : "#FFD700";
  const w = "#FFFFFF";
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      className={className}
      aria-label="Engraced Dispatch Logo"
    >
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M0 12C5.05631e-07 5.37258 5.37258 0 12 0C18.6274 0 24 5.37258 24 12C17.3736 12 12.0016 6.629 12 0.0028711C11.9984 6.629 6.62645 12 0 12ZM12 24C12 17.3725 17.3726 12 24 12C24 18.6274 18.6274 24 12 24ZM12 24C12 17.3725 6.62742 12 0 12C0 18.6274 5.37258 24 12 24Z"
        fill={g}
      />
    </svg>
  );
}