/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { Globe, Menu, X, ShieldCheck, Cpu, LayoutGrid, CalendarRange } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import LogoIcon from './LogoIcon';

interface NavbarProps {
  currentView: string;
  setView: (view: string) => void;
}

export default function Navbar({ currentView, setView }: NavbarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navItems = [
    { label: 'Overview', view: 'home' },
    { label: 'About Us', view: 'about' },
    { label: 'Booking Desk', view: 'booking' },
    { label: 'Terms', view: 'terms' },
    { label: 'Privacy', view: 'privacy' },
  ];

  return (
    <>
      <nav className={`fixed top-0 left-0 w-full z-50 px-4 md:px-8 transition-all duration-500 ${scrolled ? 'py-3' : 'py-5'}`}>
        <div className={`transition-all duration-500 flex items-center justify-between px-6 py-3.5 max-w-7xl mx-auto rounded-2xl ${
          scrolled 
            ? 'bg-white/90 backdrop-blur-lg shadow-[0_12px_40px_rgba(15,15,139,0.06)] border border-[#050548]/15' 
            : 'bg-white/95 backdrop-blur-md shadow-lg border border-zinc-100'
        }`}>
          {/* Logo */}
          <button 
            onClick={() => { setView('home'); setIsOpen(false); }}
            className="flex items-center gap-2.5 text-left group cursor-pointer focus:outline-none"
          >
             <LogoIcon className="w-16 h-16" />
            <div className="flex flex-col leading-none">
               <span className="font-black text-[12px] xs:text-[13px] sm:text-[15px] uppercase tracking-widest block" style={{color:'#050548', fontFamily: 'Arial Black, sans-serif'}}>ENGRACED</span>
               <span className="font-black text-[12px] xs:text-[13px] sm:text-[15px] uppercase tracking-widest block -mt-0.5" style={{color:'#050548', fontFamily: 'Arial Black, sans-serif'}}>LOGISTICS</span>
            </div>
          </button>
          
          {/* Desktop Navigation Links */}
          <div className="hidden lg:flex items-center gap-6">
            {navItems.map((item) => (
              <button
                key={item.view}
                onClick={() => setView(item.view)}
                className={`text-[16px] font-bold py-1.5 px-3 rounded-lg uppercase tracking-wider transition-all cursor-pointer relative ${
                  currentView === item.view
                    ? 'text-[#050548]'
                    : 'text-zinc-500 hover:text-zinc-950'
                }`}
              >
                <span className="relative z-10">{item.label}</span>
                {currentView === item.view && (
                  <motion.div 
                    layoutId="activeIndicator"
                    className="absolute inset-0 bg-[#050548]/10 rounded-lg"
                    transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                  />
                )}
              </button>
            ))}
          </div>

          {/* Desktop Call to Action */}
          <div className="hidden lg:flex items-center gap-4">
            <button 
              onClick={() => setView('booking')}
              className="bg-[#050548] text-white text-[16px] font-bold px-5 py-3 rounded-xl hover:bg-[#030330] transition-colors uppercase tracking-widest active:scale-95 cursor-pointer shadow-md"
            >
              Book a Ride
            </button>
          </div>

          {/* Mobile Hamburguer Toggle */}
          <button 
            className="lg:hidden text-zinc-950 active:scale-95 transition-transform p-1 rounded-lg focus:outline-none"
            onClick={() => setIsOpen(!isOpen)}
          >
            {isOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </nav>

      {/* Mobile Menu Panel */}
      <AnimatePresence>
        {isOpen && (
          <motion.div 
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.2 }}
            className="lg:hidden fixed top-[80px] left-0 w-full z-40 px-4"
          >
            <div className="bg-white/98 backdrop-blur-xl border border-zinc-100 rounded-2xl shadow-xl p-6 flex flex-col gap-4">
              <span className="text-[15px] font-bold text-zinc-400 uppercase tracking-widest border-b border-zinc-100 pb-2">Navigation Deck</span>
              {navItems.map((item) => (
                <button
                  key={item.view}
                  onClick={() => {
                    setView(item.view);
                    setIsOpen(false);
                  }}
                  className={`text-xs font-bold uppercase tracking-widest text-left py-2 px-3 rounded-xl transition-all ${
                    currentView === item.view
                      ? 'bg-[#050548]/10 text-[#050548]'
                      : 'text-zinc-500 hover:bg-zinc-50 hover:text-zinc-950'
                  }`}
                >
                  {item.label}
                </button>
              ))}
              <div className="border-t border-zinc-100 pt-4 mt-2 flex flex-col gap-3">
                <button 
                  onClick={() => {
                    setView('booking');
                    setIsOpen(false);
                  }}
                  className="w-full bg-[#050548] text-white text-[16px] font-bold py-3 px-4 rounded-xl text-center uppercase tracking-widest active:scale-95"
                >
                  Book a Ride
                </button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
