/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */


interface TermsPanelProps {
  setView: (view: string) => void;
}

export default function TermsPanel({ setView }: TermsPanelProps) {
  const termsSections = [
    {
      num: "01",
      title: "Services & Scope of Agreement",
      content: "Engraced Logistics provides premium vehicle rental, chauffeur services, interstate travel transit, airport protocol services, and executive convoy coordination. These Terms of Service govern all bookings, service requests, and operational agreements made through our desks."
    },
    {
      num: "02",
      title: "Reservations & Pricing Parameters",
      content: "All reservations are processed through our central booking system. Rates are calculated in real time based on active vehicle grades, trip duration, route clearance variables, and requested security escort packages. Custom or on-demand quotes are subject to direct confirmation by our booking team."
    },
    {
      num: "03",
      title: "Chauffeur & Operator Policies",
      content: "For chauffeur-driven services, our operators are certified and pre-vetted to executive standards, including rigorous safety and defensive driving qualifications. Clients must not request drivers to bypass highway regulations, speed limits, or security guidelines set by our central desk."
    },
    {
      num: "04",
      title: "Cancellations, Delays & Refund Schedules",
      content: "Cancellations made more than 24 hours prior to scheduled departure are eligible for a full credit statement. Cancellations within 24 hours are subject to a 50% reservation hold fee. Delays due to commercial flights or protocol operations will be monitored and accommodated according to fleet availability."
    },
    {
      num: "05",
      title: "Liability & Vehicle Maintenance",
      content: "We guarantee all fleet vehicles are HSE-compliant, fully serviced, and vetted for highway transit. In the unlikely event of mechanical failure, we will deploy a replacement asset immediately. We maintain comprehensive insurance coverages, but are not liable for delays caused by force majeure or regional security anomalies."
    }
  ];

  return (
    <div className="pt-32 md:pt-40 pb-16 px-4 md:px-8 max-w-7xl mx-auto w-full text-left animate-slide-up">
      
      {/* Header Section */}
      <section id="terms-hero" className="mb-12 bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-8 md:p-14 relative overflow-hidden text-white shadow-2xl">
        <div className="absolute top-0 right-0 w-[30rem] h-[30rem] bg-[#050548]/10 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 max-w-3xl">
           <h2 className="text-[16px] font-bold text-white uppercase tracking-widest mb-3 font-mono">Operations Desk</h2>
          <h1 className="text-3xl md:text-5xl font-medium tracking-tight mb-5 leading-tight">
            Terms of Service
          </h1>
          <p className="text-zinc-400 text-xs md:text-sm leading-relaxed mb-6">
            Operational guidelines, booking parameters, safety regulations, and compliance standards governing all premium transit services offered by Engraced Logistics.
          </p>
          <div className="flex gap-4">
            <button 
              onClick={() => setView('booking')}
              className="bg-[#050548] text-[#030330] text-[16px] font-bold px-6 py-3.5 rounded-xl hover:bg-white transition-all uppercase tracking-widest active:scale-95 cursor-pointer shadow-lg shadow-[#050548]/30"
            >
              Open Booking Desk
            </button>
          </div>
        </div>
      </section>

      {/* Sections Grid */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start mb-16">
        <div className="lg:col-span-4 text-left">
          <span className="text-[15px] font-bold text-zinc-400 uppercase tracking-widest block mb-2 font-mono">Overview & Compliance</span>
          <h3 className="text-xl md:text-2xl font-medium text-zinc-950 tracking-tight leading-tight mb-4">
            Structured for Dependability
          </h3>
          <p className="text-zinc-500 text-xs md:text-sm leading-relaxed mb-4">
            Please read these terms carefully before authorizing bookings. By reserving assets or using our travel desks, you confirm acceptance of these operational boundaries.
          </p>
          <p className="text-zinc-500 text-xs md:text-sm leading-relaxed">
            We have offices in Benin City (17 Upper Adesuwa Road, GRA), Asaba (Suite 03, Faith Akpede Plaza), and Lagos (34 Ikorodu Road, Fadeyi, Yaba).
          </p>
        </div>

        <div className="lg:col-span-8 space-y-8">
          {termsSections.map((sec) => (
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
