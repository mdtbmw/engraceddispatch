/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { ArrowRight, ShieldCheck, Globe } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { HERO_SLIDES } from '../data';

interface HeroSliderProps {
  setView: (view: string) => void;
}

export default function HeroSlider({ setView }: HeroSliderProps) {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [feedIndex, setFeedIndex] = useState(0);

  const LIVE_FEED = [
    "We are here for you 24/7. Call or chat with us!",
    "Booking a ride is easy. Select, pay, and go!",
    "New vehicles available. Check our fleet!",
    "We cover Benin, Asaba, Lagos, and all of Nigeria.",
    "Need a Hilux? We have the toughest trucks ready!"
  ];

  useEffect(() => {
    const slideTimer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % HERO_SLIDES.length);
    }, 6000);

    return () => clearInterval(slideTimer);
  }, [currentSlide]);

  useEffect(() => {
    const feedTimer = setInterval(() => {
      setFeedIndex((prev) => (prev + 1) % LIVE_FEED.length);
    }, 4000);

    return () => clearInterval(feedTimer);
  }, []);

  return (
    <header id="hero" className="pt-32 pb-4 md:pt-40 md:pb-6 px-4 md:px-8 w-full max-w-[100vw] overflow-hidden flex justify-center">
      <div className="relative w-full max-w-7xl h-[60vh] xs:h-[65vh] md:h-[75vh] min-h-[420px] md:min-h-[500px] rounded-[2rem] md:rounded-[2.5rem] overflow-hidden bg-gradient-to-br from-[#050548] to-[#030330] shadow-2xl group">
        
        {/* Slides */}
        <AnimatePresence mode="wait">
          <motion.div
            key={currentSlide}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
            className="absolute inset-0 z-10 cursor-grab active:cursor-grabbing"
            drag="x"
            dragElastic={0.2}
            dragConstraints={{ left: 0, right: 0 }}
            onDragEnd={(e, info) => {
              const swipeThreshold = 55;
              if (info.offset.x < -swipeThreshold) {
                setCurrentSlide((prev) => (prev + 1) % HERO_SLIDES.length);
              } else if (info.offset.x > swipeThreshold) {
                setCurrentSlide((prev) => (prev - 1 + HERO_SLIDES.length) % HERO_SLIDES.length);
              }
            }}
          >
            {/* Darker elegant overlay */}
            <div className="absolute inset-0 bg-gradient-to-t from-[#030330] via-[#050548]/50 to-[#050548]/25 z-10" />
            <motion.img 
              src={HERO_SLIDES[currentSlide].image} 
              alt={HERO_SLIDES[currentSlide].title} 
              initial={{ scale: 1.06, opacity: 0 }}
              animate={{ scale: 1.01, opacity: 1 }}
              transition={{ duration: 1.2, ease: [0.16, 1, 0.3, 1] }}
              className="absolute inset-0 w-full h-full object-cover"
            />
            
            <div className="absolute bottom-0 left-0 w-full p-5 xs:p-8 md:p-12 z-30 flex flex-col justify-end h-full">
              <div className="max-w-4xl">
                <motion.div
                  initial={{ y: 25, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ type: "spring", stiffness: 100, damping: 18, delay: 0.15 }}
                  className="mb-3 md:mb-4"
                >
                  <span className="text-white text-[16px] md:text-xs font-bold uppercase tracking-widest bg-[#050548]/60 px-3.5 py-1.5 rounded-full backdrop-blur-md border border-[#050548]/50 shadow-[0_2px_10px_rgba(15,15,139,0.3)]">
                    {HERO_SLIDES[currentSlide].tagline}
                  </span>
                </motion.div>

                <motion.h1
                  initial={{ y: 35, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ type: "spring", stiffness: 80, damping: 15, delay: 0.3 }}
                  className="text-white text-2xl xs:text-3xl md:text-5xl lg:text-6xl font-semibold leading-[1.1] mb-5 md:mb-6 tracking-tight"
                >
                  {HERO_SLIDES[currentSlide].title}
                </motion.h1>

                <motion.div
                  initial={{ y: 20, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ type: "spring", stiffness: 90, damping: 16, delay: 0.45 }}
                  className="flex flex-row items-center gap-2.5 w-full sm:w-auto"
                >
                  <button 
                    onClick={() => setView('booking')}
                    className="flex-1 sm:flex-none bg-[#050548] text-white text-[15px] xs:text-[16px] md:text-xs font-bold px-3 py-3 md:px-8 md:py-4 rounded-xl hover:bg-white transition-all duration-300 flex items-center justify-center gap-1.5 xs:gap-2.5 uppercase tracking-widest active:scale-95 shadow-[0_4px_20px_rgba(15,15,139,0.35)] cursor-pointer whitespace-nowrap"
                  >
                    Book a Ride <ArrowRight size={12} className="shrink-0" />
                  </button>
                  <button 
                    onClick={() => {
                      setView('home');
                      setTimeout(() => {
                        document.getElementById('fleet')?.scrollIntoView({ behavior: 'smooth' });
                      }, 100);
                    }}
                    className="flex-1 sm:flex-none bg-white/10 text-white backdrop-blur-md border border-white/20 text-[15px] xs:text-[16px] md:text-xs font-bold px-3 py-3 md:px-8 md:py-4 rounded-xl hover:bg-white/20 transition-all uppercase tracking-widest justify-center flex items-center active:scale-95 cursor-pointer whitespace-nowrap"
                  >
                    View Our Fleet
                  </button>
                </motion.div>
              </div>
            </div>
          </motion.div>
        </AnimatePresence>

        {/* Operational Control Desk Module (Floating widget) */}
        <div className="hidden md:flex absolute top-8 right-8 z-40 bg-[#0A0A78]/90 backdrop-blur-xl border border-[#050548]/30 rounded-2xl p-4 flex-col gap-2 shadow-2xl w-full max-w-[320px]">
          <div className="flex items-center justify-between border-b border-[#050548]/30 pb-2">
            <span className="text-[15.5px] font-bold text-white uppercase tracking-widest flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
              We Are Ready For You
            </span>
            <div className="relative w-2 h-2 rounded-full bg-[#050548] animate-pulse-ring"></div>
          </div>
          <div className="relative h-10 overflow-hidden w-full">
            <AnimatePresence mode="wait">
              <motion.p 
                key={feedIndex} 
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.3 }}
                className="absolute inset-0 text-xs text-white font-medium leading-snug flex items-center pr-1 font-mono"
              >
                {LIVE_FEED[feedIndex]}
              </motion.p>
            </AnimatePresence>
          </div>
        </div>

        {/* Dynamic Rotary Watermark Animation */}
        <div className="hidden lg:flex absolute top-1/2 right-12 -translate-y-1/2 z-30 w-36 h-36 items-center justify-center opacity-90 mix-blend-screen pointer-events-none">
          <svg className="absolute inset-0 w-full h-full animate-spin-slow" viewBox="0 0 100 100">
            <path id="textPath" d="M 50, 50 m -37, 0 a 37,37 0 1,1 74,0 a 37,37 0 1,1 -74,0" fill="none" />
              <text fill="#050548" className="text-[16px] font-bold uppercase tracking-[0.25em]">
                <textPath href="#textPath" startOffset="0%">✦ Call Us Anytime ✦ We Move You Safely ✦ </textPath>
              </text>
          </svg>
          <div className="w-14 h-14 bg-[#0A0A78]/90 rounded-full backdrop-blur-md border border-[#050548]/30 flex items-center justify-center">
            <ShieldCheck size={24} className="text-[#050548]" />
          </div>
        </div>

        {/* Pagers / Progress Indicators */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 md:left-auto md:translate-x-0 md:top-auto md:bottom-12 md:right-12 z-40 flex flex-row md:flex-col items-center md:items-end gap-3 bg-[#0A0A78]/50 md:bg-transparent px-3 py-1.5 md:p-0 rounded-full backdrop-blur-sm md:backdrop-blur-none border border-white/10 md:border-none shadow-lg md:shadow-none">
          <div className="text-left md:text-right flex items-center md:block">
              <span className="text-white text-[16px] md:text-xs font-bold font-mono">0{currentSlide + 1}</span>
            <span className="text-white/70 text-[15px] md:text-[16px] font-mono">/0{HERO_SLIDES.length}</span>
          </div>
          <div className="flex gap-1.5 md:gap-2">
            {HERO_SLIDES.map((_, index) => (
              <button 
                key={index} 
                className="w-6 md:w-8 h-[3px] bg-white/20 rounded-full overflow-hidden focus:outline-none cursor-pointer"
                onClick={() => setCurrentSlide(index)}
              >
                <div 
                  className={`h-full bg-[#050548] transition-all duration-300 ${
                    index === currentSlide ? 'w-full' : 'w-0'
                  }`}
                />
              </button>
            ))}
          </div>
        </div>

      </div>
    </header>
  );
}
