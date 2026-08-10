/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  Car, 
  User, 
  Mail, 
  Phone, 
  Calendar, 
  Clock, 
  MapPin, 
  Activity, 
  Receipt, 
  ArrowRight,
  CheckCircle2
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { FLEET_DATA } from '../data';
import CustomDropdown from './CustomDropdown';
import LogoIcon from './LogoIcon';

const BRANCHES = [
  { value: 'benin', label: 'Benin City', phone: '2348185840000', address: '17 Upper Adesuwa Road, GRA, Benin City' },
  { value: 'asaba', label: 'Asaba', phone: '2348185880000', address: 'Suite 03, Faith Akpede Plaza, Asaba, Delta State' },
  { value: 'lagos', label: 'Lagos', phone: '2349056263010', address: '34 Ikorodu Road, Fadeyi, Yaba, Lagos' },
];

interface BookingPortalProps {
  initialVehicleId?: string;
  setView?: (view: string) => void;
}

export default function BookingPortal({ initialVehicleId, setView }: BookingPortalProps) {
  // Select initial vehicle
  const defaultVehicle = FLEET_DATA.find(f => f.id === initialVehicleId) || FLEET_DATA[0];
  
  const [selectedVehicle, setSelectedVehicle] = useState(defaultVehicle);
  const [serviceType, setServiceType] = useState<'chauffeur' | 'vip-protocol'>('chauffeur');
  const [clientName, setClientName] = useState('');
  const [clientEmail, setClientEmail] = useState('');
  const [clientPhone, setClientPhone] = useState('');
  const [pickupLoc, setPickupLoc] = useState('Benin City');
  const [dropoffLoc, setDropoffLoc] = useState('Lagos');
  const [pickupDate, setPickupDate] = useState('2026-06-12');
  const [pickupTime, setPickupTime] = useState('08:00');
  const [selectedBranch, setSelectedBranch] = useState('benin');
  const [securityLevel, setSecurityLevel] = useState<'standard' | 'armed-escort' | 'covert'>('standard');
  const [duration, setDuration] = useState(3);
  const [bookingConfirmed, setBookingConfirmed] = useState(false);
  const [receiptNumber, setReceiptNumber] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  // Dynamically update vehicle if prop changes
  useEffect(() => {
    if (initialVehicleId) {
      const match = FLEET_DATA.find(f => f.id === initialVehicleId);
      if (match) setSelectedVehicle(match);
    }
  }, [initialVehicleId]);

  const handleBookingSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim() || !clientEmail.trim() || !clientPhone.trim()) {
      setErrorMessage("Please fill in your name, email, and phone number.");
      return;
    }
    setErrorMessage('');

    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let randCode = '';
    for (let i = 0; i < 6; i++) {
      randCode += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    const receiptId = `ENG-${randCode}-${Math.floor(1000 + Math.random() * 9000)}`;
    const newBooking = {
      id: receiptId,
      vehicleType: selectedVehicle.name,
      serviceType,
      pickupLocation: pickupLoc,
      dropoffLocation: dropoffLoc,
      pickupDate,
      pickupTime,
      securityLevel,
      durationDays: duration,
      totalCost: 0,
      clientName,
      status: 'pending' as const
    };

    // Store in LocalStorage for shared persistence
    const existing = JSON.parse(localStorage.getItem('engraced_logistics_bookings') || '[]');
    existing.unshift(newBooking);
    localStorage.setItem('engraced_logistics_bookings', JSON.stringify(existing));

    setReceiptNumber(receiptId);
    setBookingConfirmed(true);
  };

  const handleReset = () => {
    setBookingConfirmed(false);
    setClientName('');
    setClientEmail('');
    setClientPhone('');
    setDuration(3);
    setErrorMessage('');
  };

  const handleWhatsAppShare = () => {
    const branch = BRANCHES.find(b => b.value === selectedBranch) || BRANCHES[0];
    const message = `Hello! I want to book a vehicle.

Name: ${clientName}
Phone: ${clientPhone}
Email: ${clientEmail}

Pickup: ${pickupLoc}
Destination: ${dropoffLoc}
Date: ${pickupDate}
Time: ${pickupTime}

Vehicle: ${selectedVehicle.name}
Duration: ${duration} days
Branch: ${branch.label}

Please call me back. Thank you!`;

    const encodedMessage = encodeURIComponent(message);
    const whatsappUrl = `https://wa.me/${branch.phone}?text=${encodedMessage}`;
    window.open(whatsappUrl, '_blank');
  };

  return (
    <div className="pt-32 md:pt-40 pb-28 px-4 md:px-8 max-w-7xl mx-auto w-full text-left">
      <div className="text-center mb-8 md:mb-12 flex flex-col items-center gap-2">
         <LogoIcon className="w-24 h-24 mx-auto mb-1" />
        <div>
           <span className="text-[15px] font-mono text-white uppercase tracking-widest block mb-1">Booking Desk</span>
          <h1 className="text-3xl md:text-4xl font-medium text-zinc-950 tracking-tight">Book a Vehicle</h1>
        </div>
        <p className="text-zinc-500 text-xs md:text-sm max-w-sm mx-auto mt-2">
          Select your vehicle, choose your options, and book instantly.
        </p>
      </div>

      <AnimatePresence mode="wait">
        {!bookingConfirmed ? (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start"
          >
            {/* Form Column - Drawer on Mobile, static column on Desktop */}
            {isDrawerOpen && (
              <div 
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  setIsDrawerOpen(false);
                }}
                className="lg:hidden fixed inset-0 bg-black/60 backdrop-blur-sm z-45 transition-opacity"
              />
            )}
            
            <form 
              onSubmit={(e) => {
                handleBookingSubmit(e);
                setIsDrawerOpen(false);
              }}
              className={`lg:col-span-7 bg-white border border-zinc-150 rounded-[2rem] p-5 md:p-8 space-y-6 shadow-sm ${
                isDrawerOpen 
                  ? 'fixed bottom-0 left-0 w-full max-h-[85vh] overflow-y-auto z-50 rounded-t-[2.5rem] rounded-b-none border-t shadow-2xl block animate-slide-up pb-12' 
                  : 'max-lg:hidden lg:block'
              }`}
            >
              {/* Mobile Drawer drag handle indicator */}
              <div className="lg:hidden w-12 h-1 bg-zinc-200 rounded-full mx-auto mb-2 cursor-pointer" onClick={() => setIsDrawerOpen(false)} />
              
              <div className="flex justify-between items-center border-b border-zinc-100 pb-3">
                <h3 className="text-sm font-bold text-zinc-900 uppercase tracking-widest flex items-center gap-2">
                  <Receipt size={16} className="text-[#050548]" />
                  1. Booking Details
                </h3>
                <button 
                  type="button"
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    setIsDrawerOpen(false);
                  }}
                  className="lg:hidden text-xs font-bold text-[#050548] uppercase tracking-wider cursor-pointer"
                >
                  Done
                </button>
              </div>

              {/* Secure Booking Notice */}
              <div className="bg-zinc-50 border border-zinc-150 rounded-2xl p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-3.5 mt-2">
                <div className="flex items-center gap-3 text-left">
                  <div className="relative flex h-2 w-2 shrink-0">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#050548] opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-[#050548]"></span>
                  </div>
                  <div>
                    <span className="text-[16px] font-bold text-zinc-900 uppercase tracking-widest font-mono block">Book With Confidence</span>
                    <span className="text-[15.5px] text-zinc-500 font-sans block mt-0.5">Your information is safe with us</span>
                  </div>
                </div>
                <div className="bg-zinc-100 text-zinc-700 text-[14.5px] font-bold font-mono px-2.5 py-1 rounded border border-zinc-200 uppercase tracking-wider shrink-0">
                  Secured Desk
                </div>
              </div>

              {errorMessage && (
                <div className="bg-amber-50 text-amber-800 border border-amber-200/50 p-3.5 rounded-xl text-[15px] font-sans">
                  {errorMessage}
                </div>
              )}

              {/* Client Contacts */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Client Name</label>
                  <div className="relative">
                    <User size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="text" 
                      required 
                      placeholder="e.g. Aliko Bello" 
                      value={clientName} 
                      onChange={e => setClientName(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all placeholder:text-zinc-400"
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Secure Email</label>
                  <div className="relative">
                    <Mail size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="email" 
                      required 
                      placeholder="corporate@domain.com" 
                      value={clientEmail} 
                      onChange={e => setClientEmail(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all placeholder:text-zinc-400"
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Direct Secure Phone</label>
                  <div className="relative">
                    <Phone size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="tel" 
                      required 
                      placeholder="+234 810 000..." 
                      value={clientPhone} 
                      onChange={e => setClientPhone(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all placeholder:text-zinc-400"
                    />
                  </div>
                </div>
              </div>

              {/* Config Options */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Service Type</label>
                  <CustomDropdown
                    options={[
                      { value: 'chauffeur', label: 'Chauffeur Service', description: 'Professional driver for your trip.' },
                      { value: 'vip-protocol', label: 'VIP Escort', description: 'VIP treatment and priority service.' }
                    ]}
                    value={serviceType}
                    onChange={val => setServiceType(val as any)}
                  />
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Vehicle Selection</label>
                  <CustomDropdown
                    options={FLEET_DATA.map(f => ({
                      value: f.id,
                      label: f.name,
                      description: `${f.specs.type} • ${f.specs.pax}`
                    }))}
                    value={selectedVehicle.id}
                    onChange={val => {
                      const match = FLEET_DATA.find(f => f.id === val);
                      if (match) setSelectedVehicle(match);
                    }}
                  />
                </div>
              </div>

              {/* Branch Selection */}
              <div className="flex flex-col gap-1.5">
                <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Select Your Branch</label>
                <CustomDropdown
                  options={BRANCHES.map(b => ({
                    value: b.value,
                    label: b.label,
                    description: b.address
                  }))}
                  value={selectedBranch}
                  onChange={val => setSelectedBranch(val)}
                />
                <p className="text-[13px] text-zinc-400 mt-1">
                  Your booking will be sent to your selected branch.
                  {BRANCHES.find(b => b.value === selectedBranch) && (
                    <> Current branch: <span className="font-bold text-[#050548]">{BRANCHES.find(b => b.value === selectedBranch)?.label}</span></>
                  )}
                </p>
              </div>

              {/* Transit Coordinates */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Departure</label>
                  <div className="relative">
                    <MapPin size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="text" 
                      required 
                      value={pickupLoc} 
                      onChange={e => setPickupLoc(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all"
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Destination</label>
                  <div className="relative">
                    <MapPin size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="text" 
                      required 
                      value={dropoffLoc} 
                      onChange={e => setDropoffLoc(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all"
                    />
                  </div>
                </div>
              </div>

              {/* Calendars & Duration */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Date</label>
                  <div className="relative">
                    <Calendar size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="date" 
                      required 
                      value={pickupDate} 
                      onChange={e => setPickupDate(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all"
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">Departure Time</label>
                  <div className="relative">
                    <Clock size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
                    <input 
                      type="time" 
                      required 
                      value={pickupTime} 
                      onChange={e => setPickupTime(e.target.value)}
                      className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 pl-10 pr-4 text-xs font-medium text-zinc-800 outline-none transition-all"
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">No. of Days</label>
                  <input 
                    type="number" 
                    required 
                    min={1} 
                    max={60} 
                    value={duration} 
                    onChange={e => setDuration(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 px-4 text-xs font-medium text-zinc-800 outline-none transition-all"
                  />
                </div>
              </div>

              {/* Premium Escort & Comfort Upgrades */}
              <div className="flex flex-col gap-3 pt-2">
                <label className="text-[15px] font-bold text-zinc-500 uppercase tracking-wider font-mono">UPGRADES</label>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {[
                    { level: 'standard', title: 'Standard', surcharge: 'Standard', desc: 'Professional chauffeur service.' },
                    { level: 'armed-escort', title: 'Armed Escort', surcharge: 'Contact us', desc: 'Security escort vehicle for your trip.' },
                    { level: 'covert', title: 'Convoy Plan', surcharge: 'Contact us', desc: 'Premium convoy with full support.' }
                  ].map((opt) => (
                    <button
                      key={opt.level}
                      type="button"
                      onClick={() => setSecurityLevel(opt.level as any)}
                      className={`p-4 rounded-2xl border text-left transition-all flex flex-col justify-between min-h-[110px] md:min-h-[140px] cursor-pointer ${
                        securityLevel === opt.level 
                          ? 'border-[#050548] bg-[#050548]/10 text-[#050548] font-bold' 
                          : 'border-zinc-200 bg-white hover:border-zinc-300'
                      }`}
                    >
                      <div>
                        <span className="text-[16px] font-bold uppercase tracking-wide block">{opt.title}</span>
                        <span className="text-[15px] text-[#050548] font-mono block mt-1">{opt.surcharge}</span>
                      </div>
                      <p className="text-[15.5px] text-zinc-500 font-medium leading-relaxed font-sans mt-2 md:mt-3">
                        {opt.desc}
                      </p>
                    </button>
                  ))}
                </div>
              </div>

              {/* Confirm Submission */}
              <div className="pt-4">
                <button 
                  type="submit"
                  className="w-full bg-[#050548] text-white font-bold text-xs py-4.5 rounded-xl hover:bg-[#030330] transition-colors flex items-center justify-center gap-2 uppercase tracking-widest active:scale-95 cursor-pointer shadow-lg shadow-[#050548]/20"
                >
                  Confirm Booking <ArrowRight size={16} />
                </button>
              </div>

            </form>

            {/* Live Pricing Recalculator Invoice Card */}
            <div className="lg:col-span-5 space-y-6 w-full">
              <div className="bg-gradient-to-br from-[#050548] to-[#030330] text-white rounded-[2rem] border border-[#050548]/30 p-6 md:p-8 space-y-6 relative overflow-hidden shadow-xl">
                <div className="absolute top-0 right-0 w-32 h-32 bg-[#050548]/10 rounded-full blur-3xl pointer-events-none"></div>

                <div className="flex items-center justify-between border-b border-[#050548]/30 pb-4">
                  <div>
                     <span className="text-[14px] font-mono text-white uppercase tracking-[0.2em] block">Desk Verification</span>
                    <h3 className="text-xs font-bold uppercase tracking-wider text-white mt-1">Reservation Statement</h3>
                  </div>
                   <span className="text-[14px] font-mono bg-[#050548]/10 text-white border border-[#050548]/20 px-2 py-0.5 rounded-md uppercase tracking-wider">
                    Authorized Summary
                  </span>
                </div>

                <div className="flex flex-col gap-3.5 bg-[#0A0A78]/60 p-4 border border-[#050548]/30 rounded-2xl">
                  <div className="flex items-center gap-4">
                    <img src={selectedVehicle.image} alt={selectedVehicle.name} className="w-20 h-14 object-cover rounded-xl border border-[#050548]/20 shrink-0" />
                    <div className="text-left flex-1 min-w-0">
                      <span className="text-[14px] font-bold text-white/70 uppercase font-mono block">Selected Vehicle Class</span>
                      <span className="text-xs font-semibold text-white uppercase block mt-0.5 truncate">{selectedVehicle.name}</span>
                       <span className="text-[15px] text-white font-mono block mt-0.5 font-bold font-mono">
                        Elite Transit Class
                      </span>
                    </div>
                  </div>
                  
                  {/* Dynamic Spec Badges in Invoice Card */}
                  <div className="grid grid-cols-3 gap-2 pt-2 border-t border-[#050548]/20 text-[15px] font-mono">
                    <div className="bg-[#030330]/70 rounded-lg p-2 border border-[#050548]/30 text-center">
                      <span className="text-[13.5px] text-white/70 uppercase block">Pax Cap</span>
                      <span className="text-white font-bold block mt-0.5">{selectedVehicle.specs.pax.replace('Up to ', '')}</span>
                    </div>
                    <div className="bg-[#030330]/70 rounded-lg p-2 border border-[#050548]/30 text-center">
                      <span className="text-[13.5px] text-white/70 uppercase block">Luggage</span>
                      <span className="text-white font-bold block mt-0.5">{selectedVehicle.specs.luggage.split(' ')[0]}</span>
                    </div>
                    <div className="bg-[#030330]/70 rounded-lg p-2 border border-[#050548]/30 text-center">
                      <span className="text-[13.5px] text-white/70 uppercase block">Specs Type</span>
                      <span className="text-white font-bold block mt-0.5 truncate">{selectedVehicle.specs.type.split(' ')[0]}</span>
                    </div>
                  </div>
                </div>

                <div className="space-y-3 pt-2 text-[15px] font-mono">
                  <div className="flex justify-between items-center text-white/70 border-b border-[#050548]/30 pb-2.5">
                    <span>Reservation Span:</span>
                    <span className="text-white font-sans font-semibold">{duration} continuous days</span>
                  </div>
                  <div className="flex justify-between items-center text-white/70 border-b border-[#050548]/30 pb-2.5">
                    <span>Route Coordinates:</span>
                    <span className="text-white font-sans font-semibold truncate max-w-[150px]">{pickupLoc.split(' (')[0]} ↔ {dropoffLoc.split(' (')[0]}</span>
                  </div>
                  <div className="flex justify-between items-center text-white/70 border-b border-[#050548]/30 pb-2.5">
                    <span>Operational Upgrade:</span>
                    <span className="text-white font-sans font-semibold capitalize">{securityLevel === 'standard' ? 'Standard Chauffeur' : securityLevel.replace('-', ' ')}</span>
                  </div>
                  <div className="flex justify-between items-baseline pt-3 border-t border-[#050548]/30 text-sm font-sans font-bold">
                    <span className="text-white">RATE SPECIFICATION:</span>
                        <span className="text-white text-xs font-mono uppercase">Custom Quote On-Demand</span>
                  </div>
                </div>

                <div className="bg-[#0A0A78]/50 p-3.5 border border-[#050548]/30 rounded-xl space-y-1.5 text-[16px] text-white/70 font-sans leading-relaxed">
                   <div className="flex items-start gap-2">
                      <ShieldCheck size={12} className="text-white shrink-0 mt-0.5" />
                     <p>24/7 central desk telemetry and operations monitoring enabled upon confirmation.</p>
                   </div>
                   <div className="flex items-start gap-2">
                      <ShieldCheck size={12} className="text-white shrink-0 mt-0.5" />
                     <p>Escort logs and security details will be assigned within 15 minutes.</p>
                   </div>
                </div>

                <div className="border-t border-[#050548]/20 pt-4 mt-2">
                  <p className="text-[14.5px] font-mono text-white/60 uppercase tracking-wider leading-relaxed">
                    * Rates are custom calculated by the central protocol desk based on route safety audits, fuel indexes, and operational support.
                  </p>
                </div>

              </div>

              {/* Mobile Drawer Trigger button - fixed at bottom of screen like a native app bar */}
              {!isDrawerOpen && (
                <div className="lg:hidden fixed bottom-0 left-0 right-0 p-4 bg-white/95 backdrop-blur-md border-t border-zinc-150 z-40 shadow-lg">
                  <button 
                    type="button" 
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      setIsDrawerOpen(true);
                    }}
                    className="w-full bg-[#050548] text-white font-bold text-xs py-4 rounded-xl flex items-center justify-center gap-2 uppercase tracking-widest active:scale-95 cursor-pointer shadow-lg shadow-[#050548]/30"
                  >
                    Fill Booking Details <ArrowRight size={14} />
                  </button>
                </div>
              )}

            </div>

          </motion.div>
        ) : (
          /* Receipt Card Panel */
          <motion.div 
            id="print-receipt-area"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="max-w-2xl mx-auto bg-white border border-zinc-200/80 rounded-[2.5rem] p-6 md:p-12 shadow-2xl space-y-8 relative"
          >
            {/* Print Stylesheet Injection */}
            <style dangerouslySetInnerHTML={{__html: `
              @media print {
                body * {
                  visibility: hidden !important;
                }
                #print-receipt-area, #print-receipt-area * {
                  visibility: visible !important;
                }
                #print-receipt-area {
                  position: absolute !important;
                  left: 0 !important;
                  top: 0 !important;
                  width: 100% !important;
                  border: none !important;
                  box-shadow: none !important;
                  padding: 0 !important;
                  margin: 0 !important;
                }
                .no-print {
                  display: none !important;
                }
              }
            `}} />

            {/* Action Row at Top of Receipt Card (no-print) */}
            <div className="flex flex-row justify-between items-center bg-zinc-50 border border-zinc-150 p-4.5 rounded-2xl no-print gap-3">
              <span className="text-[15px] font-bold text-zinc-500 uppercase tracking-widest font-mono hidden sm:inline">Protocol Desk</span>
              <div className="flex items-center gap-2.5 w-full sm:w-auto justify-end">
                <button
                  type="button"
                  onClick={() => window.print()}
                  className="bg-[#050548] text-white text-[16px] font-bold px-4 py-2.5 rounded-xl uppercase tracking-wider hover:bg-[#030330] transition-colors flex items-center gap-1.5 cursor-pointer"
                >
                  <Receipt size={14} /> Download
                </button>
                <button
                  type="button"
                  onClick={handleWhatsAppShare}
                  className="bg-green-600 text-white text-[16px] font-bold px-4.5 py-2.5 rounded-xl uppercase tracking-wider hover:bg-green-700 transition-colors flex items-center gap-1.5 cursor-pointer shadow-md shadow-green-650/15"
                >
                  <svg className="w-3.5 h-3.5 fill-current" viewBox="0 0 24 24">
                    <path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946C.06 5.348 5.397.01 12.008.01c3.202.001 6.212 1.246 8.477 3.514 2.266 2.268 3.507 5.28 3.505 8.484-.004 6.657-5.34 11.997-11.953 11.997-2.005-.001-3.973-.502-5.724-1.457L0 24zm6.59-4.846c1.6.95 3.188 1.449 4.825 1.451 5.436 0 9.86-4.37 9.864-9.799.002-2.63-1.023-5.101-2.885-6.965C16.488 1.978 14.021 1.043 11.4 1.045 5.966 1.045 1.54 5.416 1.536 10.846c-.001 1.698.446 3.354 1.3 4.8l-.995 3.637 3.737-.98l.009.007c1.393.829 2.99 1.265 4.5 1.267zm10.743-7.516c-.297-.149-1.758-.868-2.031-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"/>
                  </svg>
                  WhatsApp
                </button>
              </div>
            </div>

            {/* Header Stamp */}
            <div className="flex flex-col items-center text-center gap-3">
              <div className="w-16 h-16 bg-[#050548]/15 border border-[#050548]/30 rounded-full flex items-center justify-center text-[#050548] shadow-lg p-3 animate-bounce">
<LogoIcon className="w-24 h-24" />
              </div>
              <div>
                 <span className="text-white text-[16px] font-bold font-mono uppercase tracking-[0.25em] block">Booking Confirmed</span>
                <h3 className="text-xl md:text-2xl font-semibold text-zinc-950 uppercase tracking-tight mt-1">Booking Receipt</h3>
                <span className="text-zinc-400 text-xs font-mono block mt-1">Booking ID: {receiptNumber}</span>
              </div>
            </div>

            {/* Custom Barcode */}
            <div className="flex flex-col items-center justify-center py-4 bg-zinc-50 border border-zinc-100 rounded-2xl">
              <div className="flex gap-[2px] h-10 items-stretch">
                {[2, 4, 1, 3, 2, 8, 1, 4, 2, 5, 1, 3, 8, 2, 1, 4, 1, 3, 1, 8, 2, 4, 1, 2, 1].map((bar, i) => (
                  <div key={i} className="bg-zinc-900" style={{ width: `${bar}px` }}></div>
                ))}
              </div>
              <span className="text-[14.5px] font-mono text-zinc-500 uppercase tracking-widest mt-2">{receiptNumber} - ENG TRAVELS LOG</span>
            </div>

            {/* Confirmation details review */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 border-y border-zinc-100 py-6 text-xs font-sans">
              <div>
                 <span className="text-[15.5px] text-zinc-400 font-mono block uppercase">Client</span>
                 <span className="font-bold text-zinc-900 block mt-1">{clientName}</span>
                 <span className="text-zinc-500 block mt-0.5">{clientEmail}</span>
                 <span className="text-zinc-500 block">{clientPhone}</span>
              </div>
              <div>
                 <span className="text-[15.5px] text-zinc-400 font-mono block uppercase">Vehicle</span>
                  <span className="font-bold text-white block mt-1 uppercase">{selectedVehicle.name}</span>
                 <span className="text-zinc-500 block mt-0.5 uppercase">Service Upgrade: {securityLevel}</span>
                 <span className="text-zinc-500 block">Span: {duration} days</span>
              </div>
              <div>
                 <span className="text-[15.5px] text-zinc-400 font-mono block uppercase">Departure</span>
                 <span className="font-bold text-zinc-800 block mt-1">{pickupLoc}</span>
                 <span className="text-zinc-500 block mt-0.5">Date: {pickupDate} @ {pickupTime}</span>
              </div>
              <div>
                 <span className="text-[15.5px] text-zinc-400 font-mono block uppercase">Destination</span>
                 <span className="font-bold text-zinc-800 block mt-1">{dropoffLoc}</span>
                 <span className="text-zinc-500 block mt-0.5 font-sans">Pending Desk confirmation</span>
              </div>
            </div>

            <div className="flex justify-between items-center font-mono text-sm bg-zinc-50 p-4 border border-zinc-150 rounded-xl">
               <span className="font-bold text-zinc-650">PRICING STRUCTURE:</span>
               <span className="text-xs font-bold text-zinc-900 uppercase font-sans">Custom Quote On-Demand</span>
            </div>

            <div className="flex flex-col sm:flex-row gap-3 no-print">
              <button 
                onClick={handleReset}
                className="flex-1 bg-zinc-100 text-zinc-700 text-[16px] font-bold py-3.5 rounded-xl text-center uppercase tracking-widest cursor-pointer hover:bg-zinc-200 active:scale-95 transition-all"
              >
                Book Again
              </button>
              {setView && (
                <button 
                  onClick={() => setView('home')}
                  className="flex-1 bg-[#050548] text-white text-[16px] font-bold py-3.5 rounded-xl text-center uppercase tracking-widest cursor-pointer hover:bg-[#030330] transition-colors flex items-center justify-center gap-1.5 active:scale-95 transition-all"
                >
                  Back to Home <ArrowRight size={12} />
                </button>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
