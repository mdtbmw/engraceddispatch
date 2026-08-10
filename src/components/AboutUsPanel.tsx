/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Shield, Compass, Lock, Zap, Star, CheckCircle, Target, Eye, Award, ShieldAlert } from 'lucide-react';
import pradoImage from '../images/2025-toyota-land-cruiser.jpg';

interface AboutUsPanelProps {
  setView: (view: string) => void;
}

export default function AboutUsPanel({ setView }: AboutUsPanelProps) {
  const values = [
    { 
      letter: 'S', 
      name: 'Safety', 
      desc: 'Safety is our highest priority. We are committed to maintaining the highest standards of vehicle safety, driver competence, and operational procedures to ensure every journey is secure and worry-free.', 
      icon: <Shield size={22} className="text-[#050548]" /> 
    },
    { 
      letter: 'M', 
      name: 'Mobility Excellence', 
      desc: 'We deliver reliable, innovative, and efficient transportation solutions designed to provide comfort, convenience, and exceptional value to our clients.', 
      icon: <Compass size={22} className="text-[#050548]" /> 
    },
    { 
      letter: 'I', 
      name: 'Integrity', 
      desc: 'We conduct our business with honesty, transparency, accountability, and professionalism, building lasting relationships founded on trust and respect.', 
      icon: <Lock size={22} className="text-[#050548]" /> 
    },
    { 
      letter: 'L', 
      name: 'Leadership', 
      desc: 'We strive to lead the transportation industry through innovation, continuous improvement, professionalism, and a commitment to excellence in all that we do.', 
      icon: <Zap size={22} className="text-[#050548]" /> 
    },
    { 
      letter: 'E', 
      name: 'Exceptional Experience', 
      desc: 'We are dedicated to exceeding expectations by providing personalized service, attention to detail, comfort, reliability, and memorable travel experiences.', 
      icon: <Star size={22} className="text-[#050548]" /> 
    }
  ];

  const reasons = [
    'Professional and highly trained chauffeurs',
    'Safe, modern, and well-maintained vehicles',
    'Executive and corporate transportation solutions',
    'Reliable airport transfer services',
    'Comfortable interstate travel services',
    'Protocol and VIP transportation services',
    '24/7 customer support',
    'On-demand customized quote verification',
    'Commitment to safety, professionalism, and excellence',
    'Trusted by individuals, businesses, and organizations'
  ];

  return (
    <div className="pt-32 md:pt-40 pb-20 px-4 md:px-8 max-w-7xl mx-auto w-full text-left">
      
      {/* Hero Section */}
      <section id="about-hero" className="mb-20 bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-8 md:p-16 relative overflow-hidden text-white shadow-2xl">
        <div className="absolute top-0 right-0 w-[40rem] h-[40rem] bg-[#050548]/10 rounded-full blur-3xl pointer-events-none -translate-y-1/3 translate-x-1/3"></div>
        <div className="relative z-10 max-w-3xl">
           <h2 className="text-[16px] font-bold text-white uppercase tracking-[0.25em] mb-4 font-mono">Corporate Profile</h2>
          <h1 className="text-4xl md:text-6xl font-medium tracking-tight mb-6 leading-[1.08]">
            Travel Safe.<br />Travel Easy.
          </h1>
          <p className="text-white text-xs md:text-sm leading-relaxed mb-8 max-w-2xl font-sans">
            We help people and businesses move around Nigeria. Car rental, airport pickup, interstate travel — whatever you need, we make it simple and safe.
          </p>
          <div className="flex gap-4">
            <button 
              onClick={() => setView('booking')}
               className="bg-[#050548] text-white text-[16.5px] font-bold px-7 py-4 rounded-xl hover:bg-white transition-all uppercase tracking-widest active:scale-95 shadow-lg shadow-[#050548]/30 cursor-pointer"
            >
              Book a Ride
            </button>
          </div>
        </div>
      </section>

      {/* The Story & Visual Showcase (Dual-Column) */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 mb-24 items-center">
        
        {/* Narrative Description */}
        <div className="lg:col-span-7 space-y-6 text-left">
          <div className="space-y-2">
            <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest font-mono">Who We Are</h2>
            <h3 className="text-3xl md:text-4.5xl font-medium text-zinc-950 tracking-tight leading-tight">
              We Help You Get Where You Need to Go
            </h3>
          </div>
          
          <div className="text-zinc-650 text-xs md:text-[15.5px] leading-relaxed space-y-4 font-sans">
            <p>
              <strong className="text-zinc-900 font-semibold">Engraced Logistics</strong> is a car rental and transport company based in Nigeria. We help people and businesses move around safely and comfortably. Whether you need a car for a day, a bus for an event, or a Hilux for security, we have got you covered.
            </p>
            <p>
              We know that getting from one place to another should be easy and stress-free. That is why we take care of everything — from clean, well-serviced cars to professional drivers who know the roads. You just sit back and enjoy the ride.
            </p>
            <p>
              We offer car rental, airport pickup, interstate travel, driver hire, VIP transport, event transport, and tours. Our fleet has something for everyone — from luxury SUVs to tough pickup trucks and big buses for groups.
            </p>
            <p>
              What makes us different? We care about our customers. We show up on time. We keep our cars clean and safe. We treat every customer like family. That is the Engraced Logistics way.
            </p>
            <p>
              We are growing every day, but our goal stays the same: to be the most trusted transport company in Nigeria. We want to be the name you think of when you need to move.
            </p>
          </div>
        </div>

        {/* Creative Image Panel (Image-Oriented & Overlapping Cards) */}
        <div className="lg:col-span-5 relative w-full flex justify-center">
          <div className="relative w-full max-w-[400px] aspect-square rounded-[2rem] border border-zinc-200 shadow-xl overflow-hidden group">
            <img 
              src={pradoImage} 
              alt="Premium Toyota Prado (2025)" 
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-[#030330] via-[#030330]/40 to-transparent"></div>
            
            {/* Floating Information Overlay */}
            <div className="absolute bottom-6 left-6 right-6 text-left">
               <span className="text-[15px] font-mono text-white uppercase tracking-widest font-bold block mb-1">Fleet Telemetry</span>
              <span className="text-sm font-semibold text-white uppercase block">Nationwide Routing Desk</span>
              <span className="text-white text-[16px] block mt-1">Benin Head Office • Lagos Protocol Desk</span>
            </div>
          </div>
          
          {/* Overlapping Detail Badge Card */}
          <div className="absolute top-4 left-1/2 -translate-x-1/2 lg:top-auto lg:bottom-[-24px] lg:left-[-32px] lg:translate-x-0 bg-gradient-to-br from-[#050548] to-[#030330] border border-[#050548]/30 text-white rounded-2xl p-5 shadow-2xl flex items-center gap-3.5 max-w-[220px] text-left animate-pulse-slow z-20">
             <div className="w-10 h-10 bg-[#050548]/10 border border-[#050548]/20 rounded-xl flex items-center justify-center text-white shrink-0">
              <Award size={20} />
            </div>
            <div>
               <span className="text-[14.5px] font-mono text-white uppercase font-bold block">Certified SLA</span>
              <span className="text-xs font-medium uppercase tracking-tight block mt-0.5">Mobility Excellence</span>
            </div>
          </div>
        </div>

      </section>

      {/* Standout Mission & Vision Section */}
      <section className="mb-24">
        <div className="text-center max-w-xl mx-auto mb-12">
          <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-[0.2em] mb-2 font-mono">Our Goal</h2>
          <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 tracking-tight">What We Stand For</h3>
          <p className="text-zinc-500 text-xs md:text-sm mt-2">
            Simple ideas that guide everything we do.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          
          {/* Redesigned Mission Card */}
          <div className="bg-white border border-zinc-200/80 rounded-[2rem] p-8 md:p-12 relative overflow-hidden text-left shadow-lg hover:shadow-xl hover:border-[#050548]/30 hover:-translate-y-1 transition-all duration-500 flex flex-col justify-between min-h-[320px] group">
            {/* Top golden accent line */}
            <div className="absolute top-0 left-0 w-full h-[4px] bg-[#050548]"></div>
            
            {/* Soft decorative golden glow */}
            <div className="absolute -top-10 -right-10 w-40 h-40 bg-[#050548]/5 rounded-full blur-2xl pointer-events-none transition-opacity duration-500 group-hover:bg-[#050548]/10"></div>
            
            <div className="relative z-10 space-y-6">
              <div className="flex items-center justify-between">
                <div className="w-12 h-12 bg-[#050548]/10 border border-[#050548]/20 rounded-2xl flex items-center justify-center text-[#050548] group-hover:scale-110 transition-transform duration-500">
                  <Target size={24} />
                </div>
                <span className="text-4xl font-extrabold text-zinc-100 font-mono tracking-tighter group-hover:text-[#050548]/20 transition-colors duration-500">
                  01
                </span>
              </div>
              <div className="space-y-3">
                <span className="text-[16px] font-bold text-[#050548] uppercase tracking-[0.2em] font-mono block">Our Mission</span>
                <h4 className="text-lg font-semibold text-zinc-900 tracking-tight leading-snug font-sans">
                  Safe travel for everyone, every time.
                </h4>
                <p className="text-[15px] text-zinc-600 leading-relaxed font-sans">
                  To give you safe, reliable, and comfortable rides every time. We want every trip to end with a smile.
                </p>
              </div>
            </div>
          </div>

          {/* Redesigned Vision Card */}
          <div className="bg-white border border-zinc-200/80 rounded-[2rem] p-8 md:p-12 relative overflow-hidden text-left shadow-lg hover:shadow-xl hover:border-[#050548]/30 hover:-translate-y-1 transition-all duration-500 flex flex-col justify-between min-h-[320px] group">
            {/* Top golden accent line */}
            <div className="absolute top-0 left-0 w-full h-[4px] bg-[#050548]"></div>
            
            {/* Soft decorative golden glow */}
            <div className="absolute -top-10 -right-10 w-40 h-40 bg-[#050548]/5 rounded-full blur-2xl pointer-events-none transition-opacity duration-500 group-hover:bg-[#050548]/10"></div>
            
            <div className="relative z-10 space-y-6">
              <div className="flex items-center justify-between">
                <div className="w-12 h-12 bg-[#050548]/10 border border-[#050548]/20 rounded-2xl flex items-center justify-center text-[#050548] group-hover:scale-110 transition-transform duration-500">
                  <Eye size={24} />
                </div>
                <span className="text-4xl font-extrabold text-zinc-100 font-mono tracking-tighter group-hover:text-[#050548]/20 transition-colors duration-500">
                  02
                </span>
              </div>
              <div className="space-y-3">
                <span className="text-[16px] font-bold text-[#050548] uppercase tracking-[0.2em] font-mono block">Our Vision</span>
                <h4 className="text-lg font-semibold text-zinc-900 tracking-tight leading-snug font-sans">
                  Nigeria's most trusted transport company.
                </h4>
                <p className="text-[15px] text-zinc-600 leading-relaxed font-sans">
                  To be the company Nigerians trust most for safe, easy, and affordable travel. We want to be your first choice whenever you need to move.
                </p>
              </div>
            </div>
          </div>

        </div>
      </section>

      {/* Core Values: S.M.I.L.E */}
      <section className="mb-24">
        <div className="mb-12 text-left">
          <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">Our Values</h2>
          <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 tracking-tight">We Call It S.M.I.L.E.</h3>
          <p className="text-zinc-500 text-xs md:text-sm max-w-lg mt-2">
            Five simple values that guide everything we do, every single day.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
          {values.map((v) => (
            <div key={v.letter} className="bg-white border border-zinc-150 rounded-2xl p-6 hover:border-[#050548]/30 hover:shadow-lg transition-all duration-300 flex flex-col justify-between min-h-[280px] group text-left">
              <div>
                <div className="flex items-center justify-between mb-8">
                  <div className="w-11 h-11 bg-zinc-50 rounded-xl flex items-center justify-center group-hover:bg-[#050548]/10 transition-colors duration-300">
                    {v.icon}
                  </div>
                  <span className="text-3xl font-black text-zinc-100 font-mono tracking-widest group-hover:text-[#050548]/20 transition-colors">{v.letter}</span>
                </div>
                <h4 className="text-[15.5px] font-bold text-zinc-950 uppercase tracking-widest mb-3 font-sans">{v.name}</h4>
                <p className="text-[15px] text-zinc-550 leading-relaxed font-sans">{v.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Why Choose Us Section */}
      <section className="bg-zinc-50 border border-zinc-100 rounded-[2.5rem] p-8 md:p-14 mb-24">
        <div className="mb-12 text-left">
          <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">Why Choose Us</h2>
          <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 tracking-tight">Why Engraced Logistics?</h3>
          <p className="text-zinc-500 text-xs md:text-sm mt-2 max-w-xl">
            We are more than just cars. We are people who care about getting you where you need to go, safely and on time.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-y-6 gap-x-8">
          {reasons.map((r, idx) => (
            <div key={idx} className="flex items-start gap-3.5 text-left">
              <div className="w-5 h-5 rounded-full bg-white border border-zinc-200 flex items-center justify-center text-[#050548] shrink-0 shadow-sm mt-0.5">
                <CheckCircle size={11} className="fill-[#050548]/5" />
              </div>
              <span className="text-[15.5px] text-zinc-700 font-medium font-sans leading-relaxed">{r}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Service Promise Banner (Creative & Illustrative) */}
      <section className="bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-8 md:p-16 text-center text-white relative overflow-hidden shadow-2xl border border-[#050548]/30">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(15,15,139,0.15),transparent_70%)] pointer-events-none"></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[40rem] h-[40rem] bg-[#050548]/10 rounded-full blur-3xl pointer-events-none"></div>
        
        <div className="relative z-10 max-w-2xl mx-auto space-y-6 flex flex-col items-center">
           <div className="w-14 h-14 bg-[#050548]/15 border border-[#050548]/30 rounded-full flex items-center justify-center text-white p-3 shadow-lg">
            <Shield size={28} />
          </div>
          
          <div className="space-y-2">
             <span className="text-[15px] font-mono text-white uppercase tracking-[0.25em] font-bold">Our Promise To You</span>
            <h3 className="text-2xl md:text-4xl font-semibold uppercase tracking-tight leading-tight">
              Safe Ride.<br />Happy Customer. Every Time.
            </h3>
          </div>
          
          <p className="text-white text-xs md:text-sm leading-relaxed max-w-lg">
            We promise to treat you with respect, get you there safely, and make sure you are happy with our service. That is not just a promise — it is our way of doing business.
          </p>
          
          <div className="pt-4 flex flex-col sm:flex-row items-center gap-4.5 w-full justify-center">
            <button 
              onClick={() => setView('booking')}
               className="w-full sm:w-auto bg-[#050548] text-white text-[16px] font-bold px-8 py-4 rounded-xl hover:bg-white transition-all uppercase tracking-widest active:scale-95 shadow-lg shadow-[#050548]/30 cursor-pointer"
            >
              Book a Ride
            </button>
            <span className="text-[16px] font-bold text-white/70 uppercase tracking-widest font-mono block sm:inline">
              ENG-PROTOCOL-SLA-VERIFIED
            </span>
          </div>
        </div>
      </section>

    </div>
  );
}
