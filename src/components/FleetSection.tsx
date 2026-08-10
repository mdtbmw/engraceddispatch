/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { Users, Briefcase, Award, ArrowRight } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { FLEET_DATA } from '../data';
import { FleetItem } from '../types';

interface FleetSectionProps {
  setView: (view: string) => void;
  setSelectedFleetId: (id: string) => void;
}

const parentVariants = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.08
    }
  }
};

const childFadeUp = {
  hidden: { opacity: 0, y: 15 },
  visible: { 
    opacity: 1, 
    y: 0,
    transition: { type: "spring", stiffness: 95, damping: 14 }
  }
};

const imageReveal = {
  hidden: { opacity: 0, scale: 0.96 },
  visible: { 
    opacity: 1, 
    scale: 1,
    transition: { duration: 0.6, ease: [0.16, 1, 0.3, 1] }
  }
};

const specsContainer = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.05
    }
  }
};

const specBadgeReveal = {
  hidden: { opacity: 0, scale: 0.85 },
  visible: { 
    opacity: 1, 
    scale: 1,
    transition: { type: "spring", stiffness: 120, damping: 13 }
  }
};

export default function FleetSection({ setView, setSelectedFleetId }: FleetSectionProps) {
  const categories = [
    { id: 'suvs', name: 'Executive SUVs' },
    { id: 'sedans', name: 'Premium Sedans' },
    { id: 'vip', name: 'Hilux' },
    { id: 'vans', name: 'Buses & Minivans' }
  ];

  const [activeCategory, setActiveCategory] = useState('suvs');
  const filteredVehicles = FLEET_DATA.filter(v => v.category === activeCategory);
  const [selectedVehicle, setSelectedVehicle] = useState<FleetItem>(filteredVehicles[0]);

  const [activeImage, setActiveImage] = useState<string>('');

  // Keep selected vehicle in sync when active category changes
  useEffect(() => {
    if (filteredVehicles.length > 0) {
      setSelectedVehicle(filteredVehicles[0]);
    }
  }, [activeCategory]);

  // Sync activeImage with selectedVehicle image
  useEffect(() => {
    if (selectedVehicle) {
      setActiveImage(selectedVehicle.image);
    }
  }, [selectedVehicle]);

  const handleReserve = (id: string) => {
    setSelectedFleetId(id);
    setView('booking');
  };

  const handleViewProfile = (id: string) => {
    setSelectedFleetId(id);
    setView('vehicle-detail');
  };

  return (
    <section id="fleet" className="py-16 px-4 md:px-8 max-w-7xl mx-auto w-full">
      <motion.div 
        initial={{ opacity: 0, y: 40 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        className="bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-6 md:p-12 flex flex-col overflow-hidden relative shadow-2xl"
      >
        {/* Ambient Blur */}
        <div className="absolute top-0 right-0 w-[40rem] h-[40rem] bg-[#050548]/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3 pointer-events-none"></div>

        <div className="flex flex-col lg:flex-row lg:items-end justify-between mb-10 gap-6 relative z-10 text-left">
          <div className="max-w-2xl">
             <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">Our Fleet</h2>
             <h3 className="text-2xl md:text-3.5xl font-medium text-white leading-none tracking-tight mb-4">Cars for Every Need</h3>
             <p className="text-white text-xs md:text-sm leading-relaxed">
                We have the right vehicle for you. From luxury SUVs to tough Hilux trucks, from comfortable sedans to big buses — all our cars are clean, safe, and ready to go.
             </p>
          </div>
          <p className="text-white text-xs md:text-sm max-w-md leading-relaxed border-l-2 border-[#050548]/40 pl-4 lg:pl-6">
            Every car is well maintained, fully insured, and checked before every trip. Your safety comes first.
          </p>
        </div>

        {/* Fleet Horizontal Pagers */}
        <div className="grid grid-cols-2 md:flex md:flex-row md:flex-wrap w-full gap-2 md:gap-3 pb-6 border-b border-[#050548]/40 relative z-10">
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setActiveCategory(cat.id)}
              className={`flex items-center justify-between gap-2 md:gap-5 p-2.5 md:p-3.5 md:pr-6 rounded-xl md:rounded-2xl transition-all duration-300 cursor-pointer active:scale-95 border ${
                activeCategory === cat.id 
                  ? 'bg-[#0A0A78] text-white border-[#050548]/40 shadow-xl font-bold' 
                  : 'bg-transparent text-white border-[#050548]/20 hover:text-white hover:bg-[#0A0A78]/50'
              }`}
            >
              <span className="text-[16px] md:text-[15.5px] font-bold uppercase tracking-widest pl-1 md:pl-2 font-sans truncate">{cat.name}</span>
              {activeCategory === cat.id && <ArrowRight size={12} className="text-[#050548] shrink-0" />}
            </button>
          ))}
        </div>

        {/* Fleet Active Detail Card */}
        <div className="w-full bg-[#0A0A78]/60 rounded-[2rem] p-6 md:p-10 relative z-10 overflow-hidden min-h-[480px] flex flex-col justify-between gap-6">
          
          {/* Sub-selector for models in the category */}
          {filteredVehicles.length > 1 && (
            <div className="flex flex-wrap gap-2 pb-5 border-b border-[#050548]/20 mb-2">
              {filteredVehicles.map((vehicle) => (
                <button
                  key={vehicle.id}
                  onClick={() => setSelectedVehicle(vehicle)}
                  className={`px-4 py-2 rounded-xl text-[15px] font-bold uppercase tracking-widest transition-all duration-300 border cursor-pointer active:scale-95 ${
                    selectedVehicle.id === vehicle.id
                       ? 'bg-[#050548] text-white border-[#050548] font-bold shadow-md shadow-[#050548]/10'
                      : 'bg-[#030330] text-white border-transparent hover:text-white hover:bg-[#050548]/20'
                  }`}
                >
                  {vehicle.name.split(' (')[0]}
                </button>
              ))}
            </div>
          )}

          <AnimatePresence mode="wait">
            {selectedVehicle && (
              <motion.div 
                key={selectedVehicle.id} 
                initial="hidden"
                animate="visible"
                exit="hidden"
                variants={parentVariants}
                className="h-full flex flex-col justify-between"
              >
                <div className="relative w-full">
                  <motion.div variants={imageReveal} className="relative w-full aspect-[3/2] rounded-2xl overflow-hidden mb-4 shadow-inner group">
                     <img src={activeImage} alt={selectedVehicle.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000" />
                     <div className="absolute inset-0 bg-gradient-to-t from-[#030330]/20 to-transparent pointer-events-none"></div>
                      <div className="absolute top-4 left-4 bg-[#050548] text-white font-bold font-mono text-[15px] px-3 py-1.5 rounded-lg uppercase tracking-widest shadow-lg">
                       Premium Fleet
                     </div>
                  </motion.div>

                  {/* Dynamic image gallery thumbnails */}
                  {selectedVehicle.images && selectedVehicle.images.length > 1 && (
                    <div className="flex gap-2.5 overflow-x-auto pb-4 hide-scrollbar">
                      {selectedVehicle.images.map((img, idx) => (
                        <button
                          key={idx}
                          onClick={() => setActiveImage(img)}
                          className={`w-20 h-14 rounded-lg overflow-hidden border-2 transition-all cursor-pointer shrink-0 active:scale-95 ${
                            activeImage === img ? 'border-[#050548] scale-95 shadow-md shadow-[#050548]/10' : 'border-[#050548]/30 hover:border-[#050548]/50'
                          }`}
                        >
                          <img src={img} alt={`${selectedVehicle.name} view ${idx + 1}`} className="w-full h-full object-cover" />
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-8 text-left">
                  <motion.div variants={childFadeUp} className="max-w-xl">
                      <div className="flex items-center gap-3 mb-2 flex-wrap">
                        <h4 className="text-xl font-medium text-white">{selectedVehicle.name}</h4>
                        <span className="text-[16px] font-mono text-[#050548] uppercase font-bold px-2 py-0.5 border border-[#050548]/30 rounded bg-[#050548]/10">
                          Verified
                        </span>
                      </div>
                     <p className="text-[15.5px] text-white leading-relaxed max-w-lg mb-6">{selectedVehicle.desc}</p>
                     
                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => handleViewProfile(selectedVehicle.id)}
                          className="bg-transparent border border-white/20 text-white text-[16.5px] font-bold px-5 py-3.5 rounded-xl hover:bg-white/10 transition-all uppercase tracking-widest active:scale-95 cursor-pointer"
                        >
                          Details
                        </button>
                        <button 
                          onClick={() => handleReserve(selectedVehicle.id)}
                           className="bg-[#050548] text-white text-[16.5px] font-bold px-6 py-3.5 rounded-xl hover:bg-white transition-all flex items-center gap-2 uppercase tracking-widest active:scale-95 shadow-lg shadow-[#050548]/30 cursor-pointer"
                        >
                          Book This Vehicle <ArrowRight size={14} />
                        </button>
                      </div>
                  </motion.div>

                  <motion.div 
                    variants={specsContainer}
                    className="grid grid-cols-2 gap-4 md:flex md:flex-row md:flex-nowrap self-stretch lg:self-auto justify-start lg:justify-end w-full md:w-auto"
                  >
                    <motion.div variants={specBadgeReveal} className="col-span-2 md:w-auto bg-[#030330] border border-[#050548]/30 rounded-2xl p-4 flex flex-col gap-1.5 items-center min-w-[85px] shadow-lg">
                      <Users size={16} className="text-[#050548]" />
                      <span className="text-[14.5px] font-bold text-white uppercase tracking-widest font-mono">Pax Cap</span>
                      <span className="text-xs font-bold text-white uppercase font-sans text-center">{selectedVehicle.specs.pax}</span>
                    </motion.div>
                    <motion.div variants={specBadgeReveal} className="col-span-1 md:flex-initial bg-[#030330] border border-[#050548]/30 rounded-2xl p-4 flex flex-col gap-1.5 items-center min-w-[85px] shadow-lg">
                      <Briefcase size={16} className="text-[#050548]" />
                      <span className="text-[14.5px] font-bold text-white uppercase tracking-widest font-mono">Luggage</span>
                      <span className="text-xs font-bold text-white uppercase font-sans text-center">{selectedVehicle.specs.luggage}</span>
                    </motion.div>
                    <motion.div variants={specBadgeReveal} className="col-span-1 md:flex-initial bg-[#030330] border border-[#050548]/30 rounded-2xl p-4 flex flex-col gap-1.5 items-center min-w-[85px] shadow-lg">
                      <Award size={16} className="text-[#050548]" />
                      <span className="text-[14.5px] font-bold text-white uppercase tracking-widest font-mono">Specs Type</span>
                      <span className="text-xs font-bold text-white uppercase font-sans text-center">{selectedVehicle.specs.type}</span>
                    </motion.div>
                  </motion.div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </motion.div>
    </section>
  );
}
