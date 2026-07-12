"use client";
import { useEffect } from "react";

export function SmoothScroll() {
  useEffect(() => {
    document.documentElement.style.scrollBehavior = "smooth";
    return () => {
      document.documentElement.style.scrollBehavior = "";
    };
  }, []);
  return null;
}
