/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { 
  Users, 
  Briefcase, 
  Award, 
  ArrowRight, 
  ShieldCheck, 
  Camera, 
  Wifi, 
  Signal, 
  Battery, 
  ChevronRight, 
  ChevronLeft,
  Zap,
  CheckCircle2,
  Info
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { FLEET_DATA } from '../data';

interface VehicleShowcaseProps {
  setView: (view: string) => void;
  setSelectedFleetId: (id: string) => void;
}

export default function VehicleShowcase({ setView, setSelectedFleetId }: VehicleShowcaseProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [activeImageIdx, setActiveImageIdx] = useState(0);
  const [showSpecialFeatures, setShowSpecialFeatures] = useState(false);
  const [dragDirection, setDragDirection] = useState<'left' | 'right' | null>(null);

  const activeVehicle = FLEET_DATA[activeIndex];

  // Premium features using clear, professional language
  const premiumFeatures: Record<string, { security: string; safety: string; performance: string; amenities: string[]; status: string }> = {
    'toyota_prado_2025': {
      security: 'Reinforced security cabin options',
      safety: 'Pre-collision safety system & lane assist',
      performance: 'All-terrain 4x4 drive configuration',
      amenities: ['Advanced GPS tracking & route mapping', 'Tri-zone automatic climate control', 'Premium leather seating'],
      status: 'Available'
    },
    'toyota_landcruiser_2024': {
      security: 'Armored protection option available',
      safety: 'Underbody shielding & run-flat safety tires',
      performance: 'V8 power & multi-terrain select control',
      amenities: ['Intercom & convoy communication system', 'Executive rear-cabin legroom', 'Cooler box & refreshment bar'],
      status: 'Available'
    },
    'toyota_hilux_2023': {
      security: 'Security escort configuration rating',
      safety: 'Rugged underbody protection plates',
      performance: 'High-clearance suspension & utility bed',
      amenities: ['Protocol convoy signal equipment', 'Reinforced cargo tie-down points', 'All-weather durable interior'],
      status: 'Available'
    }
  };

  const currentFeatures = premiumFeatures[activeVehicle.id] || {
    security: 'Standard security monitoring',
    safety: 'Advanced airbag system & ABS',
    performance: 'Comfort-tuned transmission',
    amenities: ['GPS tracking', 'Air conditioning'],
    status: 'Available'
  };

  // Reset active image index when switching vehicles
  useEffect(() => {
    setActiveImageIdx(0);
  }, [activeIndex]);

  const handleNext = () => {
    setActiveIndex((prev) => (prev + 1) % FLEET_DATA.length);
  };

  const handlePrev = () => {
    setActiveIndex((prev) => (prev - 1 + FLEET_DATA.length) % FLEET_DATA.length);
  };

  const handleReserve = () => {
    setSelectedFleetId(activeVehicle.id);
    setView('booking');
  };

  const handleViewProfile = () => {
    setSelectedFleetId(activeVehicle.id);
    setView('vehicle-detail');
  };

  // Combine cutout and standard images for maximum gallery options
  const allImages = activeVehicle.cutout 
    ? [activeVehicle.cutout, ...activeVehicle.images || []] 
    : activeVehicle.images || [activeVehicle.image];

  return (
    <section id="showcase" className="py-20 md:py-24 bg-gradient-to-br from-[#050548] to-[#030330] text-white overflow-hidden relative w-full border-t border-[#050548]/20">
      {/* Premium Background Textures & Glow Gradients */}
      <div className="absolute top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-[#0A0A78]/60 via-[#030330] to-[#030330] pointer-events-none z-0"></div>
      <div className="absolute -left-20 top-1/4 w-[35rem] h-[35rem] bg-[#050548]/5 rounded-full blur-[10rem] pointer-events-none"></div>
      <div className="absolute -right-20 bottom-1/4 w-[35rem] h-[35rem] bg-[#050548]/5 rounded-full blur-[10rem] pointer-events-none"></div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 relative z-10">
        
        {/* Eyebrow and Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-6 text-left">
          <div className="max-w-2xl">
             <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[16px] font-bold font-mono tracking-widest uppercase bg-[#050548]/20 border border-[#050548]/30 text-white mb-3 shadow-[0_2px_10px_rgba(0,0,253,0.15)]">
              <Zap size={10} className="animate-pulse" /> Fleet Showroom
            </span>
            <h2 className="text-2xl md:text-4xl font-semibold tracking-tight text-white mb-3">
              Explore Our Fleet
            </h2>
                <p className="text-white/70 text-xs leading-relaxed">
                  See all our vehicles and pick the one that works for you.
                </p>
          </div>
        </div>

        {/* ========================================================
            DESKTOP LAYOUT (lg:grid, hidden on mobile)
            Perfectly proportioned, zero scroll inside the phone
           ======================================================== */}
        <div className="hidden lg:grid lg:grid-cols-12 gap-10 items-center lg:pt-2">
          
          {/* Left Column: Dashboard Card for Details (Balanced layout) */}
          <div className="lg:col-span-6 flex flex-col gap-6 text-left">
            
            {/* Outer shell (Double-Bezel Architecture) */}
            <div className="bg-[#0A0A78]/40 border border-[#050548]/20 rounded-[2rem] p-1.5 shadow-xl">
              {/* Inner core - Set minimum height to match the compact phone height */}
              <div className="bg-[#030330]/80 rounded-[calc(2rem-0.375rem)] p-6 min-h-[440px] shadow-[inset_0_1px_1px_rgba(255,255,255,0.03)] flex flex-col justify-between gap-6">
                
                <div className="flex flex-col gap-4">
                  <AnimatePresence mode="wait">
                    <motion.div
                      key={activeVehicle.id + (showSpecialFeatures ? '-features' : '-standard')}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -10 }}
                      transition={{ duration: 0.25 }}
                      className="flex flex-col gap-4.5"
                    >
                      {/* Brand & Category Details */}
                      <div className="flex flex-col gap-1">
                         <span className="text-[15px] font-bold font-mono text-white uppercase tracking-widest">
                          {activeVehicle.category === 'suvs' ? 'SUV' : activeVehicle.category === 'luxury' ? 'Luxury' : 'Security'}
                        </span>
                        <h3 className="text-2xl md:text-3xl font-semibold tracking-tight text-white leading-none">
                          {activeVehicle.name}
                        </h3>
                      </div>

                      {/* Description */}
                      <p className="text-white/70 text-xs leading-relaxed">
                        {showSpecialFeatures 
                          ? `Specialized safety options and safety features configured for the ${activeVehicle.name}. Specially prepared for executive travels, airport transport services, and secured interstate routes.`
                          : activeVehicle.desc
                        }
                      </p>

                      {/* Info Cards / Specs */}
                      {!showSpecialFeatures ? (
                        <div className="grid grid-cols-3 gap-2.5">
                          <div className="bg-[#0A0A78]/60 border border-[#050548]/20 rounded-xl p-3 flex flex-col gap-1 items-center">
                            <Users size={14} className="text-white" />
                            <span className="text-[13.5px] font-bold text-white/70 uppercase tracking-widest font-mono">Pax Cap</span>
                            <span className="text-xs font-bold text-white">{activeVehicle.specs.pax.split(' ')[2] || '5'} Max</span>
                          </div>
                          <div className="bg-[#0A0A78]/60 border border-[#050548]/20 rounded-xl p-3 flex flex-col gap-1 items-center">
                            <Briefcase size={14} className="text-white" />
                            <span className="text-[13.5px] font-bold text-white/70 uppercase tracking-widest font-mono">Luggage</span>
                            <span className="text-xs font-bold text-white">{activeVehicle.specs.luggage.split(' ')[0] || '4'} Bags</span>
                          </div>
                          <div className="bg-[#0A0A78]/60 border border-[#050548]/20 rounded-xl p-3 flex flex-col gap-1 items-center">
                            <Award size={14} className="text-white" />
                            <span className="text-[13.5px] font-bold text-white/70 uppercase tracking-widest font-mono">Class</span>
                            <span className="text-xs font-bold text-white truncate w-full text-center">{activeVehicle.specs.type.split(' ')[0]}</span>
                          </div>
                        </div>
                      ) : (
                        <div className="flex flex-col gap-2.5 bg-[#0A0A78]/40 border border-[#050548]/20 rounded-xl p-3.5 text-xs">
                          <div className="flex justify-between border-b border-[#050548]/20 pb-1.5">
                            <span className="text-white/70 font-mono text-[14.5px] uppercase">Security Level</span>
                            <span className="text-white font-medium">{currentFeatures.security}</span>
                          </div>
                          <div className="flex justify-between border-b border-[#050548]/20 pb-1.5">
                            <span className="text-white/70 font-mono text-[14.5px] uppercase">Safety Features</span>
                            <span className="text-white font-medium">{currentFeatures.safety}</span>
                          </div>
                          <div className="flex justify-between border-b border-[#050548]/20 pb-1.5">
                            <span className="text-white/70 font-mono text-[14.5px] uppercase">Performance</span>
                            <span className="text-white font-medium">{currentFeatures.performance}</span>
                          </div>
                          <div className="flex justify-between pb-0">
                            <span className="text-white/70 font-mono text-[14.5px] uppercase">Service Status</span>
                            <span className="text-emerald-400 font-bold flex items-center gap-1">
                              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                              {currentFeatures.status}
                            </span>
                          </div>
                        </div>
                      )}
                    </motion.div>
                  </AnimatePresence>
                </div>

                <div className="flex flex-col gap-4">
                  {/* Features Toggle Button */}
                  <button 
                    onClick={() => setShowSpecialFeatures(!showSpecialFeatures)}
                    className={`w-full py-2.5 rounded-xl text-[15px] font-bold uppercase tracking-wider transition-all duration-300 border cursor-pointer active:scale-98 flex items-center justify-center gap-2 ${
                      showSpecialFeatures 
                         ? 'bg-[#050548]/10 text-white border-[#050548]/30' 
                        : 'bg-[#0A0A78] hover:bg-[#050548] text-white/70 border-[#050548]/30 hover:text-white'
                    }`}
                  >
                    <Info size={12} />
                    {showSpecialFeatures ? 'Show Specs' : 'Show Features'}
                  </button>

                  {/* Booking Call to Action (No Pricing!) */}
                  <div className="border-t border-[#050548]/20 pt-4 flex items-center justify-between">
                    <div className="flex flex-col text-left">
                      <span className="text-[13.5px] font-bold text-white/70 uppercase tracking-widest font-mono">Deployment</span>
                      <span className="text-xs font-bold text-white">Ready nationwide</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={handleViewProfile}
                        className="bg-transparent border border-white/20 text-white text-xs font-bold px-4 py-3.5 rounded-xl hover:bg-white/10 transition-all uppercase tracking-widest active:scale-95 cursor-pointer"
                      >
                        View Details
                      </button>
                      <button 
                        onClick={handleReserve}
                        className="bg-[#050548] text-white text-xs font-bold px-6 py-3.5 rounded-xl hover:bg-white transition-all flex items-center gap-2 uppercase tracking-widest active:scale-95 shadow-lg shadow-[#050548]/30 cursor-pointer"
                      >
                        Book Vehicle <ArrowRight size={14} />
                      </button>
                    </div>
                  </div>
                </div>

              </div>
            </div>

            {/* Navigation Buttons */}
            <div className="flex items-center gap-3 mt-1">
              <button 
                onClick={handlePrev}
                className="w-10 h-10 rounded-xl bg-[#0A0A78] border border-[#050548]/30 flex items-center justify-center text-white/70 hover:text-white hover:bg-[#050548] hover:border-[#050548]/50 transition-all cursor-pointer active:scale-95 shadow-md"
              >
                <ChevronLeft size={16} />
              </button>
              <div className="flex gap-1.5">
                {FLEET_DATA.map((_, idx) => (
                  <button
                    key={idx}
                    onClick={() => setActiveIndex(idx)}
                    className={`w-8 h-1 bg-[#050548]/30 rounded-full overflow-hidden transition-all duration-300 cursor-pointer ${
                      idx === activeIndex ? 'bg-[#050548] w-12' : 'hover:bg-zinc-700'
                    }`}
                  />
                ))}
              </div>
              <button 
                onClick={handleNext}
                className="w-10 h-10 rounded-xl bg-[#0A0A78] border border-[#050548]/30 flex items-center justify-center text-white/70 hover:text-white hover:bg-[#050548] hover:border-[#050548]/50 transition-all cursor-pointer active:scale-95 shadow-md"
              >
                <ChevronRight size={16} />
              </button>
            </div>

          </div>

          {/* Right Column: Highly Compact Smartphone Bezel Wrapper (Fits desktop viewports easily) */}
          <div className="lg:col-span-6 flex justify-center items-center w-full relative">
            
            {/* Outer Shell (Double-Bezel Mockup, reduced max-width for compact aspect ratio) */}
            <div className="w-full max-w-[270px] aspect-[9/18.5] p-2.5 rounded-[2.85rem] bg-[#030330] border border-[#050548]/30 shadow-[0_20px_50px_-10px_rgba(0,0,0,0.7)] flex items-center justify-center relative z-10">
              
              {/* Ring Bezel reflection */}
              <div className="absolute inset-0.5 rounded-[2.75rem] border border-white/5 pointer-events-none"></div>
              
              {/* Inner Core Screen */}
              <div className="w-full h-full rounded-[2.15rem] bg-[#0A0A78] overflow-hidden relative shadow-[inset_0_1px_1px_rgba(255,255,255,0.15)] flex flex-col justify-between select-none">
                
                {/* Phone Header Bezel & Status Bar (Compact notch) */}
                <div className="pt-3 pb-1.5 px-5 bg-[#030330] border-b border-[#050548]/20 flex justify-between items-center text-[15px] font-mono font-bold text-white/70 relative z-30">
                  <span>08:13</span>
                  <div className="w-20 h-4.5 rounded-full bg-[#030330] absolute left-1/2 -translate-x-1/2 top-2 flex justify-center items-center pointer-events-none">
                    <div className="w-1.5 h-1.5 rounded-full bg-[#0A0A78] absolute right-3"></div>
                  </div>
                  <div className="flex items-center gap-1">
                    <Signal size={9} />
                    <Wifi size={9} />
                    <Battery size={11} className="rotate-0 text-white/70" />
                  </div>
                </div>

                {/* Screen App Content (Strict layout structure - Zero Scroll!) */}
                <div className="flex-grow flex flex-col justify-between p-3 relative overflow-hidden bg-[#0A0A78]/20">
                  
                  {/* Small App Navigation Header */}
                  <div className="flex justify-between items-center mb-1 z-20">
                    <div className="flex flex-col text-left">
                      <span className="text-[13px] font-bold uppercase tracking-wider text-white/70 font-mono">Operations</span>
                      <span className="text-[16px] font-bold text-white leading-none">Active Unit</span>
                    </div>
                       <span className="text-[14px] font-bold font-mono px-2 py-0.5 border border-[#050548]/30 rounded-xl text-white/70 bg-[#030330]/80">
                      {activeIndex + 1} / {FLEET_DATA.length}
                    </span>
                  </div>

                  {/* Vehicle Image swipable container - Compact Height */}
                  <div className="flex-grow flex items-center justify-center relative w-full h-32 z-10 my-1">
                    <AnimatePresence mode="popLayout">
                      <motion.div
                        key={activeVehicle.id}
                        initial={{ opacity: 0, scale: 0.9, x: dragDirection === 'left' ? 100 : dragDirection === 'right' ? -100 : 0 }}
                        animate={{ opacity: 1, scale: 1, x: 0 }}
                        exit={{ opacity: 0, scale: 0.9, x: dragDirection === 'left' ? -100 : dragDirection === 'right' ? 100 : 0 }}
                        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
                        className="absolute inset-0 w-full h-full flex flex-col items-center justify-center cursor-grab active:cursor-grabbing"
                        drag="x"
                        dragConstraints={{ left: 0, right: 0 }}
                        dragElastic={0.4}
                        onDragStart={() => setDragDirection(null)}
                        onDrag={(e, info) => {
                          if (info.offset.x < -10) setDragDirection('left');
                          else if (info.offset.x > 10) setDragDirection('right');
                        }}
                        onDragEnd={(e, info) => {
                          const swipeThreshold = 45;
                          if (info.offset.x < -swipeThreshold) {
                            handleNext();
                          } else if (info.offset.x > swipeThreshold) {
                            handlePrev();
                          }
                        }}
                      >
                        <div className="absolute w-24 h-24 rounded-full blur-2xl opacity-50 z-0 bg-[#050548]/20 pointer-events-none" />

                                <motion.img 
                                  key={activeImageIdx}
                                  src={allImages[activeImageIdx]} 
                                  alt={activeVehicle.name} 
                                  initial={{ opacity: 0, scale: 0.95 }}
                                  animate={{ opacity: 1, scale: 1 }}
                                  exit={{ opacity: 0, scale: 0.95 }}
                                  transition={{ duration: 0.3 }}
                                  className="w-[90%] h-auto max-h-[90%] object-contain select-none pointer-events-none filter drop-shadow-[0_8px_16px_rgba(0,0,0,0.5)] rounded-2xl"
                                />
                      </motion.div>
                    </AnimatePresence>
                  </div>

                  {/* Camera Angles - Compact thumbnails */}
                  <div className="flex flex-col gap-1 z-20">
                    <span className="text-[13.5px] font-bold text-white/70 uppercase tracking-widest font-mono text-left block">
                      Camera Angles
                    </span>

                    <div className="flex gap-1.5 justify-start overflow-x-auto pb-1 hide-scrollbar">
                      {allImages.map((img, idx) => (
                        <button
                          key={idx}
                          onClick={() => setActiveImageIdx(idx)}
                           className={`w-9 h-7 rounded-xl overflow-hidden border transition-all shrink-0 active:scale-95 cursor-pointer relative ${
                            activeImageIdx === idx 
                              ? 'border-[#050548] bg-[#030330] scale-95 shadow-sm' 
                              : 'border-[#050548]/30 bg-[#0A0A78]/60'
                          }`}
                        >
                          <img src={img} alt={`view ${idx + 1}`} className="w-full h-full object-cover" />
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Compact Status Indicator row */}
                  <div className="bg-[#030330]/70 border border-[#050548]/20 rounded-xl p-2 flex items-center gap-1.5 mt-2 z-20">
                    <ShieldCheck size={11} className="text-white shrink-0" />
                    <span className="text-[14px] font-bold text-white truncate text-left w-full">
                      {activeVehicle.id === 'toyota_hilux' ? 'Strong and tough, ready for any road' : activeVehicle.id === 'toyota_landcruiser' ? 'Top of the line SUV for VIPs' : 'Comfortable and reliable for daily use'}
                    </span>
                  </div>

                </div>

                {/* App Bottom Quick Booking CTA (Compact height, no pricing) */}
                <div className="p-3.5 bg-[#030330] border-t border-[#050548]/20 flex justify-between items-center relative z-20">
                  <div className="flex flex-col text-left">
                    <span className="text-[13px] font-bold text-white/70 uppercase tracking-widest font-mono">Status</span>
                    <span className="text-[15px] font-bold text-emerald-400 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> Available
                    </span>
                  </div>
                  <button 
                    onClick={handleReserve}
                      className="bg-[#050548] hover:bg-white text-white text-[14.5px] font-bold px-3 py-2 rounded-lg uppercase tracking-widest transition-all active:scale-95 cursor-pointer flex items-center gap-1 shadow-md shadow-[#050548]/30"
                  >
                    Book <ArrowRight size={8} />
                  </button>
                </div>

                {/* Screen footer bar */}
                <div className="h-3.5 bg-[#030330] flex justify-center items-center pb-1">
                  <div className="w-20 h-0.5 rounded-full bg-[#050548]/30"></div>
                </div>

              </div>
            </div>

          </div>

        </div>

        {/* ========================================================
            UNIQUE MOBILE LAYOUT (col-span-1, lg:hidden)
            Renders a premium mobile-native card deck experience
           ======================================================== */}
        <div className="lg:hidden flex flex-col gap-6 w-full">
          
          {/* Touch-Swipable Main Card */}
          <div className="bg-[#0A0A78]/40 border border-[#050548]/20 rounded-[2.5rem] p-1.5 shadow-2xl relative w-full overflow-hidden">
            
            {/* Ambient inner glow */}
            <div className="absolute top-0 right-0 w-48 h-48 bg-[#050548]/5 rounded-full blur-3xl pointer-events-none"></div>

            {/* Inner Core Card */}
            <div className="bg-[#030330]/95 rounded-[calc(2.5rem-0.375rem)] p-5 shadow-[inset_0_1px_1px_rgba(255,255,255,0.03)] flex flex-col gap-5 text-left select-none relative z-10">
              
              {/* Category & Counter Badge */}
              <div className="flex justify-between items-center">
                       <span className="inline-block text-[15px] font-bold font-mono text-white uppercase tracking-widest bg-[#050548]/20 border border-[#050548]/20 px-2.5 py-0.5 rounded-md">
                  {activeVehicle.category === 'suvs' ? 'SUV' : activeVehicle.category === 'luxury' ? 'Luxury' : 'Security'}
                </span>
                       <span className="text-[16px] font-bold font-mono text-white/70 bg-[#0A0A78]/80 px-2 py-0.5 rounded-xl border border-[#050548]/20">
                  {activeIndex + 1} / {FLEET_DATA.length}
                </span>
              </div>

              {/* Touch Drag-to-Swipe Area for Mobile Card */}
              <div className="relative w-full h-48 flex items-center justify-center bg-[#0A0A78]/10 rounded-2xl border border-[#050548]/20 overflow-hidden">
                
                {/* Background lighting */}
                <div className="absolute w-32 h-32 rounded-full blur-3xl bg-[#050548]/10 z-0 pointer-events-none"></div>
                
                <AnimatePresence mode="popLayout">
                  <motion.div
                    key={activeVehicle.id}
                    initial={{ opacity: 0, scale: 0.92, x: dragDirection === 'left' ? 100 : dragDirection === 'right' ? -100 : 0 }}
                    animate={{ opacity: 1, scale: 1, x: 0 }}
                    exit={{ opacity: 0, scale: 0.92, x: dragDirection === 'left' ? -100 : dragDirection === 'right' ? 100 : 0 }}
                    transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
                    className="absolute inset-0 w-full h-full flex items-center justify-center cursor-grab active:cursor-grabbing z-10"
                    drag="x"
                    dragConstraints={{ left: 0, right: 0 }}
                    dragElastic={0.4}
                    onDragStart={() => setDragDirection(null)}
                    onDrag={(e, info) => {
                      if (info.offset.x < -10) setDragDirection('left');
                      else if (info.offset.x > 10) setDragDirection('right');
                    }}
                    onDragEnd={(e, info) => {
                      const swipeThreshold = 55;
                      if (info.offset.x < -swipeThreshold) {
                        handleNext();
                      } else if (info.offset.x > swipeThreshold) {
                        handlePrev();
                      }
                    }}
                  >
                                <motion.img 
                                  key={activeImageIdx}
                                  src={allImages[activeImageIdx]} 
                                  alt={activeVehicle.name} 
                                  initial={{ opacity: 0, scale: 0.96 }}
                                  animate={{ opacity: 1, scale: 1 }}
                                  exit={{ opacity: 0, scale: 0.96 }}
                                  transition={{ duration: 0.3 }}
                                  className="w-[85%] h-auto max-h-[85%] object-contain select-none pointer-events-none filter drop-shadow-[0_8px_16px_rgba(0,0,0,0.5)] rounded-2xl"
                                />
                  </motion.div>
                </AnimatePresence>
              </div>

              {/* Camera Angle Selector for Mobile */}
              <div className="flex flex-col gap-1.5">
                <span className="text-[14px] font-bold text-white/70 uppercase tracking-widest font-mono flex items-center gap-1">
                  <Camera size={10} className="text-white" /> View Angle
                </span>
                <div className="flex gap-2 overflow-x-auto pb-1 hide-scrollbar">
                  {allImages.map((img, idx) => (
                    <button
                      key={idx}
                      onClick={() => setActiveImageIdx(idx)}
                           className={`w-11 h-9 rounded-xl overflow-hidden border-2 transition-all shrink-0 active:scale-95 ${
                        activeImageIdx === idx 
                          ? 'border-[#050548] scale-95 shadow-md shadow-[#050548]/10' 
                          : 'border-[#050548]/30'
                      }`}
                    >
                      <img src={img} alt={`view ${idx + 1}`} className="w-full h-full object-cover" />
                    </button>
                  ))}
                </div>
              </div>

              {/* Title & Description */}
              <div className="flex flex-col gap-1">
                <h3 className="text-xl font-bold text-white">{activeVehicle.name}</h3>
                <p className="text-white/70 text-xs leading-relaxed">{activeVehicle.desc}</p>
              </div>

               {/* Specifications Badges */}
               <div className="grid grid-cols-3 gap-2 p-2 bg-[#0A0A78]/10 border border-[#050548]/20 rounded-2xl my-1">
                 <div className="flex flex-col items-center justify-center p-2 bg-[#0A0A78]/50 rounded-xl">
                   <Users size={14} className="text-white mb-0.5" />
                   <span className="text-[13px] text-white/70 font-mono uppercase tracking-widest">Seats</span>
                   <span className="text-xs font-bold text-white">{activeVehicle.specs.pax.split(' ')[2] || '5'} Max</span>
                 </div>
                 <div className="flex flex-col items-center justify-center p-2 bg-[#0A0A78]/50 rounded-xl">
                   <Briefcase size={14} className="text-white mb-0.5" />
                   <span className="text-[13px] text-white/70 font-mono uppercase tracking-widest">Bags</span>
                   <span className="text-xs font-bold text-white">{activeVehicle.specs.luggage.split(' ')[0] || '4'} Pcs</span>
                 </div>
                 <div className="flex flex-col items-center justify-center p-2 bg-[#0A0A78]/50 rounded-xl">
                   <Award size={14} className="text-white mb-0.5" />
                   <span className="text-[13px] text-white/70 font-mono uppercase tracking-widest">Class</span>
                   <span className="text-xs font-bold text-white truncate w-full text-center">{activeVehicle.specs.type.split(' ')[0]}</span>
                 </div>
               </div>

              {/* Amenities Checklist */}
              <div className="flex flex-col gap-2 bg-[#0A0A78]/40 border border-[#050548]/20 rounded-xl p-3 text-xs text-white/70">
                <span className="text-[14.5px] font-bold text-white/70 uppercase tracking-widest font-mono">Features</span>
                <div className="flex flex-col gap-1.5">
                  {currentFeatures.amenities.map((amenity, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <CheckCircle2 size={12} className="text-white shrink-0" />
                      <span>{amenity}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Book button for Mobile */}
              <div className="flex gap-2 mt-2">
                <button
                  onClick={handleViewProfile}
                  className="flex-1 bg-transparent border border-white/20 text-white text-xs font-bold py-3.5 rounded-xl uppercase tracking-widest active:scale-95 transition-all cursor-pointer"
                >
                  View Details
                </button>
                <button 
                  onClick={handleReserve}
                  className="flex-1 bg-[#050548] text-[#030330] text-xs font-bold py-3.5 rounded-xl uppercase tracking-widest active:scale-95 transition-all flex items-center justify-center gap-1.5 shadow-md shadow-[#050548]/30 cursor-pointer"
                >
                  Book Vehicle <ArrowRight size={12} />
                </button>
              </div>

            </div>

          </div>

          {/* Animated Swipe Helper Instructions for Mobile */}
          <div className="flex flex-col items-center gap-2 mt-2">
            <div className="flex items-center gap-2.5 text-[16px] font-mono uppercase tracking-widest text-white font-bold bg-[#050548]/50 border border-[#050548]/40 px-4 py-2 rounded-full">
              <motion.span 
                animate={{ x: [-4, 4, -4] }} 
                transition={{ repeat: Infinity, duration: 1.5, ease: "easeInOut" }}
              >
                ←
              </motion.span>
              Swipe Card to Switch Vehicle
              <motion.span 
                animate={{ x: [4, -4, 4] }} 
                transition={{ repeat: Infinity, duration: 1.5, ease: "easeInOut" }}
              >
                →
              </motion.span>
            </div>
            
            {/* Pagination dots */}
            <div className="flex gap-2.5 mt-1">
              {FLEET_DATA.map((_, idx) => (
                <button
                  key={idx}
                  onClick={() => setActiveIndex(idx)}
                  className={`w-2 h-2 rounded-full transition-all duration-300 ${
                    idx === activeIndex ? 'bg-[#050548] w-6' : 'bg-[#050548]/30'
                  }`}
                />
              ))}
            </div>
          </div>

        </div>

      </div>
    </section>
  );
}
