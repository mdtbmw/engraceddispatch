/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */


interface PrivacyPanelProps {
  setView: (view: string) => void;
}

export default function PrivacyPanel({ setView }: PrivacyPanelProps) {
  const privacySections = [
    {
      num: "01",
      title: "Data Collection Scope",
      content: "We collect direct client names, secure corporate emails, direct phone lines, trip dates, travel parameters, and pickup/drop-off coordinates to configure transport bookings and authorize route plans."
    },
    {
      num: "02",
      title: "Real-Time Telemetry & GPS Logging",
      content: "For active transits, our vehicles record continuous GPS coordinates, speed assessments, and route geofence logs. This telemetry is transmitted securely to our Benin HQ and Lagos protocol hubs solely for passenger safety and convoy monitoring."
    },
    {
      num: "03",
      title: "Information Security Protocols",
      content: "All client files, vehicle logs, and security schedules are processed using secure network protocols and stored on protected local servers. Access is restricted to authorized desk controllers and operations supervisors."
    },
    {
      num: "04",
      title: "Third-Party Coordination",
      content: "Personal details and route schedules are never shared, leased, or distributed for commercial purposes. Telemetry data or driver profiles may be coordinated with certified security escorts or local emergency response units solely when requested in a premium booking package."
    },
    {
      num: "05",
      title: "Client Rights & Data Audits",
      content: "Corporate clients and individuals may request full audits of their booking history, journey telemetry logs, and personal account parameters by contacting our compliance desk at info@engracedlogistics.com.ng."
    }
  ];

  return (
    <div className="pt-32 md:pt-40 pb-16 px-4 md:px-8 max-w-7xl mx-auto w-full text-left animate-slide-up">
      
      {/* Header Section */}
      <section id="privacy-hero" className="mb-12 bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-8 md:p-14 relative overflow-hidden text-white shadow-2xl">
        <div className="absolute top-0 right-0 w-[30rem] h-[30rem] bg-[#050548]/10 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 max-w-3xl">
           <h2 className="text-[16px] font-bold text-white uppercase tracking-widest mb-3 font-mono">Operations Desk</h2>
          <h1 className="text-3xl md:text-5xl font-medium tracking-tight mb-5 leading-tight">
            Privacy Policy
          </h1>
          <p className="text-zinc-400 text-xs md:text-sm leading-relaxed mb-6">
            Standards for securing telemetry logs, routing data, client coordinates, and billing details processed by Engraced Logistics.
          </p>
          <div className="flex gap-4">
            <button 
              onClick={() => setView('booking')}
               className="bg-[#050548] text-white text-[16px] font-bold px-6 py-3.5 rounded-xl hover:bg-white transition-all uppercase tracking-widest active:scale-95 cursor-pointer shadow-lg shadow-[#050548]/30"
            >
              Open Booking Desk
            </button>
          </div>
        </div>
      </section>

      {/* Sections Grid */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start mb-16">
        <div className="lg:col-span-4 text-left">
          <span className="text-[15px] font-bold text-zinc-400 uppercase tracking-widest block mb-2 font-mono">Policy Overview</span>
          <h3 className="text-xl md:text-2xl font-medium text-zinc-950 tracking-tight leading-tight mb-4">
            Security End-to-End
          </h3>
          <p className="text-zinc-500 text-xs md:text-sm leading-relaxed mb-4">
            Protecting client privacy, transit confidentiality, and telemetry coordinates is fundamental to our security commitment.
          </p>
          <p className="text-zinc-500 text-xs md:text-sm leading-relaxed">
            Data is logged in compliance with operational standards and archived securely after transit resolution.
          </p>
        </div>

        <div className="lg:col-span-8 space-y-8">
          {privacySections.map((sec) => (
            <div key={sec.num} className="bg-zinc-50 border border-zinc-150 rounded-2xl p-6 md:p-8 flex gap-6 items-start hover:border-[#050548]/25 transition-all">
              <span className="text-2xl font-bold font-mono text-[#050548] leading-none">{sec.num}</span>
              <div>
                <h4 className="text-sm font-bold text-zinc-950 uppercase tracking-wider mb-2 font-sans">{sec.title}</h4>
                <p className="text-[15.5px] text-zinc-500 leading-relaxed font-sans">{sec.content}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
      
    </div>
  );
}
