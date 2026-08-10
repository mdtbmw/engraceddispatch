/**
 * @license
 * SPDX-License-Identifier: Apache-2.5
 */

import { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HeroSlider from './components/HeroSlider';
import RouteMarquee from './components/RouteMarquee';
import ServicesSection from './components/ServicesSection';
import MetricsSection from './components/MetricsSection';
import CoverageSimulation from './components/CoverageSimulation';
import FleetSection from './components/FleetSection';
import VehicleShowcase from './components/VehicleShowcase';
import ProtocolTimeline from './components/ProtocolTimeline';
import AdvantageSection from './components/AdvantageSection';
import ClientsSection from './components/ClientsSection';
import ReviewsSection from './components/ReviewsSection';
import BookingPortal from './components/BookingPortal';
import Footer from './components/Footer';
import AboutUsPanel from './components/AboutUsPanel';
import TermsPanel from './components/TermsPanel';
import PrivacyPanel from './components/PrivacyPanel';
import WhatsAppChat from './components/WhatsAppChat';
import VehicleProfile from './components/VehicleProfile';
import { motion, AnimatePresence } from 'motion/react';

const GlobalStyles = () => (
  <style>{`
    @keyframes marquee {
      0% { transform: translateX(0%); }
      100% { transform: translateX(-50%); }
    }
    .animate-marquee {
      display: flex;
      width: 200%;
      animation: marquee 35s linear infinite;
    }
    .hide-scrollbar::-webkit-scrollbar {
      display: none;
    }
    .hide-scrollbar {
      -ms-overflow-style: none;
      scrollbar-width: none;
    }
    /* Hide scrollbar completely on mobile */
    @media (max-width: 1023px) {
      ::-webkit-scrollbar {
        display: none !important;
        width: 0 !important;
        height: 0 !important;
      }
      * {
        -ms-overflow-style: none !important;
        scrollbar-width: none !important;
      }
      *::-webkit-scrollbar {
        display: none !important;
      }
    }
    /* Customized scrollbar all over the system on desktop */
    @media (min-width: 1024px) {
      ::-webkit-scrollbar {
        width: 8px;
        height: 8px;
      }
      ::-webkit-scrollbar-track {
        background: rgba(240, 240, 240, 0.4);
        border-radius: 4px;
      }
      ::-webkit-scrollbar-thumb {
        background: #d4d4d8;
        border-radius: 4px;
      }
      ::-webkit-scrollbar-thumb:hover {
        background: #050548;
      }
      .custom-scrollbar::-webkit-scrollbar {
        width: 6px;
        height: 6px;
      }
      .custom-scrollbar::-webkit-scrollbar-track {
        background: rgba(24, 24, 27, 0.4);
        border-radius: 8px;
      }
      .custom-scrollbar::-webkit-scrollbar-thumb {
        background: #050548;
        border-radius: 8px;
      }
      .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background: #0A0A78;
      }
      .custom-scrollbar {
        scrollbar-width: thin;
        scrollbar-color: #050548 rgba(24, 24, 27, 0.4);
      }
    }
    body {
      -webkit-tap-highlight-color: transparent;
      overflow-x: hidden;
      font-family: 'Inter', sans-serif;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
    }
    h1, h2, h3, h4, h5, h6 {
      font-family: 'Outfit', sans-serif;
    }
    @keyframes fillProgress {
      0% { width: 0%; }
      100% { width: 100%; }
    }
    .animate-progress {
      animation: fillProgress 6s linear forwards;
    }
    @keyframes spin-slow {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    .animate-spin-slow {
      animation: spin-slow 16s linear infinite;
    }
    @keyframes pulse-ring {
      0% { transform: scale(0.85); opacity: 0.6; }
      100% { transform: scale(1.6); opacity: 0; }
    }
    .animate-pulse-ring::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: inherit;
      background-color: #050548;
      animation: pulse-ring 2.5s cubic-bezier(0.25, 1, 0.5, 1) infinite;
    }
    @keyframes radar-sweep {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    .animate-radar {
      background: conic-gradient(from 0deg, transparent 65%, rgba(15, 43, 94, 0.35) 100%);
      border-radius: 50%;
      animation: radar-sweep 5s linear infinite;
    }
    .smooth-scroll {
      scroll-behavior: smooth;
    }
    @keyframes gradient {
      0% { background-position: 0% 50%; }
      50% { background-position: 100% 50%; }
      100% { background-position: 0% 50%; }
    }
    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
    .animate-slide-up {
      animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
  `}</style>
);

export default function App() {
  const [view, setView] = useState('home');
  const [selectedFleetId, setSelectedFleetId] = useState<string>('toyota_prado_2025');

  // Automatically scroll to the top of the viewport when changing pages for premium single page transition UX
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' as any });
  }, [view]);

  return (
    <>
      <GlobalStyles />
      <div className="min-h-screen bg-white font-sans text-zinc-900 selection:bg-[#050548] selection:text-white smooth-scroll flex flex-col justify-between">
        
        {/* Persistent Premium Navbar */}
        <Navbar currentView={view} setView={setView} />
        
        {/* Multi-view Interactive Layout Page Stream */}
        <main className="flex-grow">
          <AnimatePresence mode="wait">
            {view === 'home' && (
              <motion.div
                key="home"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.4 }}
              >
                <HeroSlider setView={setView} />
                <RouteMarquee />
                <ServicesSection setView={setView} />
                <MetricsSection />
                <CoverageSimulation />
                <FleetSection setView={setView} setSelectedFleetId={setSelectedFleetId} />
                <VehicleShowcase setView={setView} setSelectedFleetId={setSelectedFleetId} />
                <ProtocolTimeline />
                <AdvantageSection setView={setView} />
                <ClientsSection />
                <ReviewsSection />
              </motion.div>
            )}

            {view === 'about' && (
              <motion.div
                key="about"
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -15 }}
                transition={{ duration: 0.4 }}
              >
                <AboutUsPanel setView={setView} />
              </motion.div>
            )}

            {view === 'booking' && (
              <motion.div
                key="booking"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.3 }}
              >
                <BookingPortal initialVehicleId={selectedFleetId} setView={setView} />
              </motion.div>
            )}

            {view === 'terms' && (
              <motion.div
                key="terms"
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -15 }}
                transition={{ duration: 0.4 }}
              >
                <TermsPanel setView={setView} />
              </motion.div>
            )}

            {view === 'privacy' && (
              <motion.div
                key="privacy"
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -15 }}
                transition={{ duration: 0.4 }}
              >
                <PrivacyPanel setView={setView} />
              </motion.div>
            )}

            {view === 'vehicle-detail' && (
              <motion.div
                key="vehicle-detail"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ duration: 0.4 }}
              >
                <VehicleProfile vehicleId={selectedFleetId} setView={setView} setSelectedFleetId={setSelectedFleetId} />
              </motion.div>
            )}
          </AnimatePresence>
        </main>

        {/* Persistent Premium Footer */}
        <Footer setView={setView} />

        {/* Floating WhatsApp Chat */}
        <WhatsAppChat />

      </div>
    </>
  );
}
