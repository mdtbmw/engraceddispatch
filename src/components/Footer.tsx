/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Globe, Briefcase, MapPin } from 'lucide-react';
import LogoIcon from './LogoIcon';

interface FooterProps {
  setView: (view: string) => void;
}

export default function Footer({ setView }: FooterProps) {
  return (
    <footer className="bg-gradient-to-br from-[#050548] to-[#030330] text-white pt-24 pb-28 md:pb-24 px-6 md:px-12 relative overflow-hidden border-t border-[#050548]/20 text-left">
      {/* Background Subtle Ambient Light Orb */}
      <div className="absolute left-1/3 top-0 -translate-y-1/2 w-96 h-96 bg-[#050548]/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="max-w-7xl mx-auto relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-10 lg:gap-12 mb-20">
          
          {/* Col 1: Brand Info & Status */}
          <div className="lg:col-span-2 flex flex-col items-start text-left">
            <div className="flex items-center gap-2.5 mb-6">
               <LogoIcon className="w-20 h-20" />
               <div className="flex items-center gap-1.5">
                 <span className="font-black tracking-widest text-xs uppercase text-white" style={{ fontFamily: 'Arial Black, sans-serif' }}>ENGRACED</span>
                 <span className="font-black tracking-widest text-xs uppercase text-white" style={{ fontFamily: 'Arial Black, sans-serif' }}>LOGISTICS</span>
               </div>
            </div>
            <p className="text-[15px] text-white leading-relaxed font-sans max-w-sm">
              We help you move safely and comfortably across Nigeria. Car rental, airport pickup, and travel services you can trust.
            </p>
            
          </div>

          {/* Col 2: Navigation (Company Desk) */}
          <div className="flex flex-col gap-4 text-left">
            <span className="text-[16px] font-bold text-white uppercase tracking-widest font-mono">Company Desk</span>
            <div className="flex flex-col gap-2.5">
              <button onClick={() => setView('home')} className="text-[15px] text-white hover:text-[#050548] cursor-pointer text-left transition-colors font-sans hover:translate-x-1 duration-200">Overview Hub</button>
              <button onClick={() => setView('about')} className="text-[15px] text-white hover:text-[#050548] cursor-pointer text-left transition-colors font-sans hover:translate-x-1 duration-200">About Us</button>
              <button onClick={() => setView('booking')} className="text-[15px] text-white hover:text-[#050548] cursor-pointer text-left transition-colors font-sans hover:translate-x-1 duration-200">Booking Desk</button>
            </div>
          </div>

          {/* Col 3: Legal & Policy */}
          <div className="flex flex-col gap-4 text-left">
            <span className="text-[16px] font-bold text-white uppercase tracking-widest font-mono">Legal Desk</span>
            <div className="flex flex-col gap-2.5">
              <button onClick={() => setView('terms')} className="text-[15px] text-white hover:text-[#050548] cursor-pointer text-left transition-colors font-sans hover:translate-x-1 duration-200">Terms of Service</button>
              <button onClick={() => setView('privacy')} className="text-[15px] text-white hover:text-[#050548] cursor-pointer text-left transition-colors font-sans hover:translate-x-1 duration-200">Privacy Policy</button>
            </div>
          </div>

          {/* Col 4: Newsletter Sign-up */}
          <div className="flex flex-col gap-4 text-left">
            <span className="text-[16px] font-bold text-white uppercase tracking-widest font-mono">Operations Registry</span>
            <p className="text-[16.5px] text-white leading-relaxed font-sans">
              Get updates on our services, new vehicles, and travel tips.
            </p>
            <form onSubmit={(e) => e.preventDefault()} className="flex gap-2 mt-1">
              <input 
                type="email" 
                placeholder="Enter corporate email" 
                className="w-full bg-[#0A0A78] border border-[#050548]/30 rounded-xl px-3.5 py-2 text-[16.5px] text-white placeholder-zinc-500 focus:outline-none focus:border-[#050548]/50 transition-colors font-sans"
              />
              <button 
                type="submit" 
                className="bg-[#050548] hover:bg-[#0A0A78] text-white font-bold uppercase tracking-widest text-[15.5px] px-3.5 py-2 rounded-xl cursor-pointer active:scale-95 transition-all shrink-0"
              >
                Sub
              </button>
            </form>
          </div>

        </div>

        {/* Branch Offices */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 pt-10 pb-8 border-t border-[#050548]/20 text-left text-white text-[16px] font-mono uppercase tracking-wider relative z-20">
          <div className="flex items-center gap-3">
             <MapPin size={12} className="text-white/70 shrink-0" />
            <div>
              <span className="text-white font-bold block">Benin City</span>
              17 Upper Adesuwa Road, GRA, Benin City<br />
               <span className="text-white text-[14px]">081-8584-0000</span>
            </div>
          </div>
          <div className="flex items-center gap-3">
             <MapPin size={12} className="text-white/70 shrink-0" />
            <div>
              <span className="text-white font-bold block">Asaba</span>
              Suite 03, Faith Akpede Plaza, Asaba, Delta State<br />
               <span className="text-white text-[14px]">081-8588-0000</span>
            </div>
          </div>
          <div className="flex items-center gap-3">
             <MapPin size={12} className="text-white/70 shrink-0" />
            <div>
              <span className="text-white font-bold block">Lagos</span>
              34 Ikorodu Road, Fadeyi, Yaba, Lagos<br />
               <span className="text-white text-[14px]">081-8587-0000</span>
            </div>
          </div>
        </div>

        {/* Footer Bottom copyright and icons */}
        <div className="flex flex-col md:flex-row items-center justify-between pt-8 border-t border-[#050548]/20 gap-6 relative z-20">
          <p className="text-[16px] font-bold text-white uppercase tracking-widest font-mono text-center md:text-left">
            &copy; {new Date().getFullYear()} Engraced Logistics. All Rights Reserved.
          </p>
          <div className="flex gap-3">
             <div onClick={() => setView('home')} className="w-8 h-8 rounded-full bg-[#0A0A78] border border-[#050548]/30 flex items-center justify-center hover:bg-[#050548] hover:text-white hover:border-[#050548]/30 transition-all duration-300 cursor-pointer text-white shadow-sm" title="Overview Hub"><Globe size={13}/></div>
             <div onClick={() => setView('about')} className="w-8 h-8 rounded-full bg-[#0A0A78] border border-[#050548]/30 flex items-center justify-center hover:bg-[#050548] hover:text-white hover:border-[#050548]/30 transition-all duration-300 cursor-pointer text-white shadow-sm" title="About Us">
                <span className="font-mono text-[15px] font-bold">US</span>
             </div>
             <div onClick={() => setView('terms')} className="w-8 h-8 rounded-full bg-[#0A0A78] border border-[#050548]/30 flex items-center justify-center hover:bg-[#050548] hover:text-white hover:border-[#050548]/30 transition-all duration-300 cursor-pointer text-white shadow-sm" title="Terms"><span className="font-mono text-[15px] font-bold">TS</span></div>
          </div>
        </div>
      </div>

      {/* Massive Bold Iconic Watermark */}
      <div 
        className="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-12 md:translate-y-16 select-none pointer-events-none text-[13vw] font-black uppercase tracking-[0.15em] text-center leading-none whitespace-nowrap font-mono opacity-60"
        style={{
          WebkitTextStroke: '1.5px rgba(255, 255, 255, 0.05)',
          color: 'transparent'
        }}
      >
        ENGRACED
      </div>
    </footer>
  );
}
