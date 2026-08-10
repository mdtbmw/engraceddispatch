/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Car, 
  MapPin, 
  ShieldCheck, 
  CalendarDays, 
  Briefcase, 
  Package,
  ArrowRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { SERVICE_CATEGORIES } from '../data';

const iconMap: Record<string, React.ComponentType<{ size: number; className?: string }>> = {
  Car,
  MapPin,
  ShieldCheck,
  CalendarDays,
  Briefcase,
  Package
};

interface ServicesSectionProps {
  setView: (view: string) => void;
}

const serviceDetailsMap: Record<string, { sla: string; availability: string; pricing: string; coverage: string }> = {
  'Corporate Logistics': { sla: 'Zero Delay B2B SLA', availability: 'Dedicated Manager', pricing: 'Custom Quotation', coverage: 'Lagos, Abuja, Benin' },
  'Airport Transfers': { sla: 'Flight Delay Sync', availability: 'Immediate Deployment', pricing: 'Custom Quotation', coverage: 'All Intl Airports' },
  'Vehicle Rentals': { sla: '24/7 Replacement SLA', availability: 'Chauffeur Included', pricing: 'Custom Quotation', coverage: 'Lagos, Abuja, Benin' },
  'Chauffeur Hire': { sla: 'Defensive Certified', availability: 'Vetted Drivers', pricing: 'Custom Quotation', coverage: 'Nationwide' },
  'Interstate Travel': { sla: 'Double-Driver Safety', availability: 'Daily Scheduled', pricing: 'Custom Quotation', coverage: 'Major highway hubs' },
  'VIP & Protocol': { sla: 'VIP Fast-Track Clear', availability: '24h Booking Lead', pricing: 'Custom Quotation', coverage: 'All Major Cities' },
  'Security Escort': { sla: 'Armed Convoy Escort', availability: '48h Clearance Lead', pricing: 'Custom Quotation', coverage: 'High-Risk Corridors' },
  'Event Transport': { sla: 'Unified Event SLA', availability: 'Dedicated Marshal', pricing: 'Custom Quotation', coverage: 'Venue Coordinates' },
  'Tour & Leisure': { sla: 'Custom Itinerary SLA', availability: 'Local Tour Guides', pricing: 'Custom Quotation', coverage: 'Resort & City Hubs' }
};

const serviceFeaturesMap: Record<string, string[]> = {
  'Corporate Logistics': ['Dedicated Account Manager', 'Custom Corporate Billing', 'Fleet Management Portal', '99.9% Route Reliability'],
  'Airport Transfers': ['Flight Time Auto-Tracking', 'Meet & Greet Service', 'Luggage Assistance', 'Sanitized Premium Fleet'],
  'Vehicle Rentals': ['Daily, Weekly, Monthly Plans', 'Fully Insured Fleet', 'GPS Tracking', 'Regular Preventive Service'],
  'Chauffeur Hire': ['Highly Trained & Vetted', 'Defensive Driving Cert.', 'Professional Uniformed', 'Multilingual Drivers'],
  'Interstate Travel': ['Pre-Vetted Highway Corridors', 'Double Driver Operations', 'Real-time Trip Tracking', 'Comfort Stopovers'],
  'VIP & Protocol': ['VIP Fast-Track Access', 'Executive Chauffeur Service', 'Discreet Professionalism', 'Diplomatic Coordination'],
  'Security Escort': ['Professional Escort Coordination', 'Tactical Support Vehicle', 'Route Risk Assessment', 'Command Monitoring'],
  'Event Transport': ['Consolidated Fleet Operations', 'On-Site Coordinator', 'Custom Guest Shuttle Service', 'Premium Fleet Branding'],
  'Tour & Leisure': ['Knowledgeable Driver Guides', 'Flexible Excursion Hours', 'Custom Itineraries', 'Hotel & Resort Transfers']
};

const gridVariants = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.1
    }
  }
};

const cardVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { 
    opacity: 1, 
    y: 0,
    transition: { type: "spring", stiffness: 100, damping: 16 }
  }
};

export default function ServicesSection({ setView }: ServicesSectionProps) {
  const [activeTab, setActiveTab] = useState(SERVICE_CATEGORIES[0].id);

  return (
    <section id="services" className="pb-16 px-4 md:px-8 max-w-7xl mx-auto w-full">
      <motion.div 
        initial={{ opacity: 0, y: 30 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        className="flex flex-col lg:flex-row lg:items-end justify-between mb-12 gap-6"
      >
        <div>
          <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">What We Do</h2>
          <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 leading-tight tracking-tight">
            We Move You<br/>Safely & Comfortably
          </h3>
        </div>
          <p className="text-zinc-500 text-xs md:text-sm max-w-md leading-relaxed">
            Need a car in Benin City? A driver in Lagos? Or VIP transport in Abuja? We help you get where you need to go — safely, on time, and with a smile.
          </p>
      </motion.div>

      <motion.div 
        initial={{ opacity: 0, y: 40 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.8, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
        className="flex flex-col gap-8"
      >
        {/* Ribbon of category pills */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:flex lg:flex-row lg:flex-wrap w-full gap-2 md:gap-3 pb-3">
          {SERVICE_CATEGORIES.map((category) => {
            const Icon = iconMap[category.iconName] || Car;
            const isActive = activeTab === category.id;
            return (
              <button
                key={category.id}
                onClick={() => setActiveTab(category.id)}
                className={`flex items-center gap-2 md:gap-3 p-2.5 md:p-3.5 md:pr-6 rounded-xl md:rounded-2xl text-left transition-all duration-300 cursor-pointer active:scale-95 border ${
                  isActive 
                    ? 'bg-gradient-to-br from-[#050548] to-[#030330] text-white shadow-lg border-[#050548]/40 font-bold' 
                    : 'bg-white border border-zinc-100 text-zinc-500 hover:text-zinc-950 hover:bg-zinc-50'
                }`}
              >
                <div className={`p-1.5 md:p-2 rounded-lg md:rounded-xl transition-colors shrink-0 ${
                  isActive ? 'bg-[#050548] text-white' : 'bg-zinc-100 text-zinc-500'
                }`}>
                  <Icon size={14} />
                </div>
                <span className="text-[15.5px] md:text-[15px] font-bold uppercase tracking-widest font-sans truncate">{category.title}</span>
              </button>
            );
          })}
        </div>

        {/* Content Box */}
        <div className="w-full">
          <div className="bg-gradient-to-br from-[#fafcff] to-[#f0f5ff] border border-[#050548]/8 rounded-[2rem] p-6 lg:p-12 min-h-[420px] lg:min-h-[380px] flex flex-col justify-between shadow-lg relative overflow-hidden group">
            <div className="absolute -right-24 -top-24 w-80 h-80 bg-[#050548]/5 rounded-full blur-3xl group-hover:bg-[#050548]/8 transition-colors duration-700 pointer-events-none"></div>

            <AnimatePresence mode="wait">
              {SERVICE_CATEGORIES.map((category) => {
                if (category.id !== activeTab) return null;
                const Icon = iconMap[category.iconName] || Car;
                return (
                  <motion.div 
                    key={category.id} 
                    initial="hidden"
                    animate="visible"
                    exit={{ opacity: 0, transition: { duration: 0.15 } }}
                    className="flex flex-col justify-between h-full"
                  >
                    <div>
                      <div className="flex items-center gap-4 mb-8">
                        <div className="w-12 h-12 bg-zinc-50 border border-zinc-100 rounded-2xl flex items-center justify-center text-[#050548] shadow-sm">
                          <Icon size={20} />
                        </div>
                        <div>
                          <h4 className="text-base font-bold text-zinc-950 uppercase tracking-widest font-mono">
                            {category.title}
                          </h4>
                          <span className="text-[15px] font-mono text-[#050548] uppercase tracking-wider">We Make It Happen</span>
                        </div>
                      </div>
                      
                      <motion.div 
                        variants={gridVariants}
                        className="grid grid-cols-1 md:grid-cols-2 gap-5 relative z-20"
                      >
                        {category.items.map((item, idx) => {
                          const details = serviceDetailsMap[item.name] || { sla: 'Operational SLA', availability: 'On-Demand', pricing: 'Custom Quote', coverage: 'Nationwide' };
                          const features = serviceFeaturesMap[item.name] || ['GPS Tracked Transit', 'Fully Vetted Crew', 'SLA Compliant', '24/7 Support Desk'];
                          return (
                            <motion.div 
                              variants={cardVariants}
                              key={idx} 
                              className="bg-white border border-[#050548]/8 rounded-2xl p-5 hover:border-[#050548]/20 hover:bg-white hover:shadow-md transition-all duration-300 group/item flex flex-col justify-between gap-5 text-left"
                            >
                              <div className="flex flex-col gap-3.5">
                                <div>
                                  <div className="flex items-center gap-2 mb-2">
                                    <div className="w-1.5 h-1.5 rounded-full bg-[#050548] shadow-[0_0_8px_#050548] transition-transform group-hover/item:scale-135"></div>
                                    <h5 className="text-[15px] font-bold text-zinc-900 uppercase tracking-widest">{item.name}</h5>
                                  </div>
                                  <p className="text-[16.5px] text-zinc-500 leading-relaxed font-sans">
                                    {item.desc}
                                  </p>
                                </div>

                                {/* Features List */}
                                <div className="flex flex-col gap-1.5 pt-1">
                                  {features.map((feature, fIdx) => (
                                    <div key={fIdx} className="flex items-center gap-2 text-[15.5px] text-zinc-600 font-sans">
                                      <svg className="w-3 h-3 text-[#050548] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                      </svg>
                                      <span>{feature}</span>
                                    </div>
                                  ))}
                                </div>
                              </div>
                              
                              <div className="flex flex-col gap-3.5">
                                {/* Details Grid */}
                                <div className="grid grid-cols-2 gap-y-2.5 gap-x-4 pt-3 border-t border-zinc-200/60 font-mono text-[14.5px] text-zinc-400">
                                  <div>
                                    <span className="text-zinc-500 block uppercase font-bold">Service SLA</span>
                                    <span className="text-zinc-700 font-medium font-sans mt-0.5 block">{details.sla}</span>
                                  </div>
                                  <div>
                                    <span className="text-zinc-500 block uppercase font-bold">Vetting Lead</span>
                                    <span className="text-zinc-700 font-medium font-sans mt-0.5 block">{details.availability}</span>
                                  </div>
                                  <div>
                                    <span className="text-zinc-500 block uppercase font-bold">Rates Benchmark</span>
                                    <span className="text-[#050548] font-bold font-sans mt-0.5 block">{details.pricing}</span>
                                  </div>
                                  <div>
                                    <span className="text-zinc-500 block uppercase font-bold">Active Coverage</span>
                                    <span className="text-zinc-700 font-medium font-sans mt-0.5 block truncate">{details.coverage}</span>
                                  </div>
                                </div>

                                <button 
                                  onClick={() => setView('booking')}
                                  className="w-full py-2 bg-[#050548] text-white rounded-xl text-[15px] font-bold uppercase tracking-widest hover:bg-[#030330] transition-all duration-300 cursor-pointer active:scale-[0.98] flex items-center justify-center gap-1.5"
                                >
                                  Book Now
                                  <ArrowRight size={10} />
                                </button>
                              </div>
                            </motion.div>
                          );
                        })}
                      </motion.div>
                    </div>
                    
                    <div className="mt-10 pt-6 border-t border-zinc-50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                       <p className="text-[16px] text-zinc-400 font-mono uppercase">Book at least 24 hours ahead</p>
                       <button 
                         onClick={() => setView('booking')}
                         className="text-[#050548] text-[16.5px] font-bold uppercase tracking-widest flex items-center gap-2 hover:gap-3 transition-all active:scale-95 cursor-pointer hover:text-zinc-950"
                       >
                         Book Now <ArrowRight size={14} />
                       </button>
                    </div>
                  </motion.div>
                );
              })}
            </AnimatePresence>
          </div>
        </div>
      </motion.div>
    </section>
  );
}
