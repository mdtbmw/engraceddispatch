/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState } from 'react';
import { Navigation, ShieldCheck, MapPin, Cpu } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export default function CoverageSimulation() {
  const [activeRegion, setActiveRegion] = useState<'BEN' | 'ASB' | 'LAG'>('BEN');

  const regionDetails = {
    BEN: {
      name: "Benin City Office",
      coordinates: "17 Upper Adesuwa Road, GRA",
      convoyCounts: "081-8584-0000",
      threatLevel: "Open Mon - Sun, 6am - 10pm",
      activeCrews: "Benin City, Edo State",
      history: ["Our main office. Come see us or call.", "We serve all of Edo and nearby states."]
    },
    ASB: {
      name: "Asaba Office",
      coordinates: "Suite 03, Faith Akpede Plaza",
      convoyCounts: "081-8588-0000",
      threatLevel: "Open Mon - Sun, 6am - 10pm",
      activeCrews: "Asaba, Delta State",
      history: ["Our Asaba branch is ready to serve you.", "Covering Delta State and beyond."]
    },
    LAG: {
      name: "Lagos Office",
      coordinates: "34 Ikorodu Road, Fadeyi, Yaba",
      convoyCounts: "081-8587-0000",
      threatLevel: "Open Mon - Sun, 6am - 10pm",
      activeCrews: "Lagos, Nigeria",
      history: ["Visit our Lagos office for fast service.", "Covering all of Lagos and the South-West."]
    }
  };

  return (
    <section id="coverage" className="py-16 px-4 md:px-8 max-w-7xl mx-auto w-full">
      <motion.div 
        initial={{ opacity: 0, y: 40 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        className="bg-zinc-50 border border-zinc-100 rounded-[1.8rem] sm:rounded-[2.5rem] p-2 sm:p-3.5 flex flex-col md:flex-row items-stretch gap-6 md:gap-4 overflow-hidden shadow-lg"
      >
        
        {/* Monitoring Info */}
        <div className="w-full md:w-[52%] p-4 sm:p-6 lg:p-12 relative z-10 text-left flex flex-col justify-between">
           <div>
              <span className="text-[13px] xs:text-[14.5px] font-mono text-zinc-500 uppercase tracking-widest block mb-1 font-bold">Our Offices</span>
             <h3 className="text-[16px] xs:text-sm sm:text-lg md:text-2xl lg:text-3xl font-medium text-zinc-950 leading-tight mb-2 tracking-tight">
                Visit Us or Call Us
              </h3>
              <p className="hidden xs:block text-[14px] xs:text-[15.5px] sm:text-xs md:text-sm text-zinc-500 leading-normal mb-3 md:mb-8 max-w-sm">
                We have offices in Benin City, Asaba, and Lagos. Walk in, call, or chat with us on WhatsApp. We are always ready to help you move.
              </p>
           </div>
 
           {/* Region Selector Tab List */}
            <div className="grid grid-cols-3 gap-1 mb-3 sm:mb-8 border-b border-zinc-200/65 pb-2.5 sm:pb-5">
              {(['BEN', 'ASB', 'LAG'] as const).map((region) => (
                <button
                  key={region}
                  onClick={() => setActiveRegion(region)}
                  className={`text-[13px] xs:text-[14.5px] sm:text-[15.5px] font-bold py-1 sm:py-1.5 rounded-md sm:rounded-lg uppercase tracking-wider transition-colors cursor-pointer text-center truncate ${
                    activeRegion === region 
                      ? 'bg-gradient-to-br from-[#050548] to-[#030330] text-white shadow-md font-bold' 
                      : 'bg-white border border-zinc-200 text-zinc-500 hover:text-zinc-950'
                  }`}
                >
                  {region === 'BEN' ? 'Benin' : region === 'ASB' ? 'Asaba' : 'Lagos'}
                </button>
              ))}
           </div>
           
            {/* Dynamic Telemetry Panel */}
            <div className="bg-white border border-zinc-200 p-2 sm:p-5 rounded-xl sm:rounded-2xl shadow-sm min-h-[200px] sm:min-h-[220px] relative overflow-hidden flex flex-col justify-center">
              <AnimatePresence mode="wait">
                <motion.div
                  key={activeRegion}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
                  className="space-y-2 sm:space-y-4 text-left w-full"
                >
                  <div className="flex flex-col xs:flex-row items-start xs:items-center justify-between border-b border-zinc-100 pb-1.5 sm:pb-2 gap-0.5">
                    <span className="text-[13.5px] xs:text-[15.5px] sm:text-[16px] font-bold text-zinc-900 uppercase tracking-widest flex items-center gap-1 sm:gap-2">
                      <Cpu size={10} className="text-[#050548] animate-pulse shrink-0" />
                      {regionDetails[activeRegion].name}
                    </span>
                    <span className="text-[13px] xs:text-[14px] sm:text-[15px] font-mono text-zinc-400 truncate max-w-full">{regionDetails[activeRegion].coordinates}</span>
                  </div>
                  
                   <div className="grid grid-cols-1 xs:grid-cols-2 gap-1.5 sm:gap-4">
                     <div>
                        <span className="text-[13px] xs:text-[14.5px] sm:text-[15px] text-zinc-400 block font-mono">PHONE NUMBER</span>
                        <span className="text-[13.5px] xs:text-[16px] sm:text-xs font-bold text-zinc-800 uppercase font-sans">{regionDetails[activeRegion].convoyCounts}</span>
                     </div>
                     <div>
                        <span className="text-[13px] xs:text-[14.5px] sm:text-[15px] text-zinc-400 block font-mono">LOCATION</span>
                        <span className="text-[13.5px] xs:text-[16px] sm:text-xs font-bold text-zinc-800 uppercase font-sans">{regionDetails[activeRegion].activeCrews}</span>
                     </div>
                     <div className="col-span-1 xs:col-span-2">
                        <span className="text-[13px] xs:text-[14.5px] sm:text-[15px] text-zinc-400 block font-mono">WORKING HOURS</span>
                         <span className="text-[13.5px] xs:text-[16px] sm:text-xs font-bold text-white uppercase font-sans">{regionDetails[activeRegion].threatLevel}</span>
                     </div>
                   </div>
       
                   <div className="hidden sm:block bg-zinc-50 p-2.5 rounded-lg border border-zinc-100">
                     <span className="text-[14.5px] font-bold text-zinc-400 block uppercase tracking-widest mb-1">About This Office</span>
                     {regionDetails[activeRegion].history.map((hist, i) => (
                       <p key={i} className="text-[16px] text-zinc-500 font-mono flex items-center gap-1.5 mt-0.5">
                         <span className="w-1 h-1 rounded-full bg-[#050548] shrink-0" />
                         {hist}
                       </p>
                     ))}
                   </div>
                </motion.div>
              </AnimatePresence>
            </div>
          </div>
 
         {/* RADAR CANVAS SCREEN (MAP IMAGE CARD) */}
         <div className="w-full md:w-[48%] aspect-square bg-gradient-to-br from-[#050548] to-[#030330] rounded-[1.2rem] sm:rounded-[2rem] p-3 sm:p-6 lg:p-12 relative overflow-hidden flex flex-col items-center justify-center self-center shadow-lg">
           {/* Static Premium Route Tracking Map Image */}
           <img 
             src="https://images.unsplash.com/photo-1524661135-423995f22d0b?auto=format&fit=crop&w=1200&q=80" 
             alt="Regional Protocol Map" 
             className="absolute inset-0 w-full h-full object-cover opacity-35 mix-blend-luminosity" 
           />
           {/* Dark gradient overlay for premium look */}
           <div className="absolute inset-0 bg-gradient-to-t from-[#030330] via-transparent to-[#050548]/50 z-10 pointer-events-none" />
           
           {/* Technical Grid Pattern Overlay */}
           <div className="absolute inset-0 opacity-10 z-10 pointer-events-none" style={{ backgroundImage: 'linear-gradient(#050548 1px, transparent 1px), linear-gradient(90deg, #050548 1px, transparent 1px)', backgroundSize: '50px 50px' }}></div>
           
           {/* Elegant overlay detail representing active transit paths */}
           <div className="relative z-20 flex flex-col items-center gap-1.5 sm:gap-3 bg-[#0A0A78]/90 border border-[#050548]/30 p-3 sm:p-6 rounded-xl sm:rounded-2xl max-w-[90%] text-center backdrop-blur-md shadow-2xl">
             <div className="w-7 h-7 sm:w-10 sm:h-10 bg-[#050548]/10 border border-[#050548]/30 rounded-lg sm:rounded-xl flex items-center justify-center text-[#050548]">
               <Navigation className="w-3.5 h-3.5 sm:w-4.5 sm:h-4.5 animate-pulse" />
             </div>
             <div>
                 <span className="text-[13px] xs:text-[14px] sm:text-[15px] font-bold text-white uppercase tracking-widest font-mono block">We Are Here For You</span>
                <span className="text-[14px] xs:text-[16px] sm:text-[15.5px] font-medium text-white block mt-0.5 sm:mt-1">Benin • Asaba • Lagos</span>
                <span className="hidden xs:block text-[13.5px] xs:text-[14.5px] sm:text-[15px] text-white/70 block mt-0.5 sm:mt-1 font-mono">Visit any of our offices</span>
             </div>
           </div>
 
           {/* Floating Widget */}
           <div className="absolute top-2.5 left-2.5 sm:top-4 sm:left-4 flex items-center gap-1 sm:gap-2 bg-[#0A0A78]/90 border border-[#050548]/30 px-2 sm:px-3 py-1 sm:py-1.5 rounded-lg sm:rounded-xl z-20 shadow-lg">
              <span className="relative flex h-1.5 w-1.5 sm:h-2 sm:w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-450 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-1.5 w-1.5 sm:h-2 sm:w-2 bg-red-505"></span>
              </span>
              <span className="text-[13px] xs:text-[13.5px] sm:text-[14.5px] font-bold text-white uppercase tracking-widest font-mono">We Cover All 36 States</span>
           </div>
          </div>
       </motion.div>
     </section>
  );
}
