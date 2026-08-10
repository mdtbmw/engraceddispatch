import { useMemo, useEffect } from 'react';
import { motion } from 'motion/react';
import { ArrowRight, Users, Briefcase, Award, ShieldCheck, CheckCircle2, ChevronLeft, Star, MapPin } from 'lucide-react';
import { FLEET_DATA } from '../data';

interface VehicleProfileProps {
  vehicleId: string;
  setView: (view: string) => void;
  setSelectedFleetId: (id: string) => void;
}

const categoryLabels: Record<string, string> = {
  suvs: 'Luxury SUV',
  sedans: 'Comfort Sedan',
  vans: 'Group Van / Bus',
  vip: 'Security / VIP'
};

const categoryColors: Record<string, string> = {
  suvs: 'from-amber-500 to-amber-700',
  sedans: 'from-blue-500 to-blue-700',
  vans: 'from-emerald-500 to-emerald-700',
  vip: 'from-red-500 to-red-700'
};

export default function VehicleProfile({ vehicleId, setView, setSelectedFleetId }: VehicleProfileProps) {
  const vehicle = FLEET_DATA.find(f => f.id === vehicleId);

  const schemaJson = useMemo(() => {
    if (!vehicle) return '';
    return JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'Product',
      name: vehicle.name,
      description: vehicle.seo.description,
      image: vehicle.seo.ogImage,
      brand: {
        '@type': 'Brand',
        name: vehicle.name.split(' ').slice(0, 2).join(' ')
      },
      offers: {
        '@type': 'Offer',
        price: vehicle.pricePerDay,
        priceCurrency: 'NGN',
        availability: 'https://schema.org/InStock',
        description: `Daily rental rate: ₦${vehicle.pricePerDay.toLocaleString()}`
      },
      category: categoryLabels[vehicle.category] || 'Vehicle Rental'
    }, null, 2);
  }, [vehicle]);

  useEffect(() => {
    if (!vehicle) return;
    const existing = document.getElementById('vehicle-schema');
    if (existing) existing.remove();
    const script = document.createElement('script');
    script.id = 'vehicle-schema';
    script.type = 'application/ld+json';
    script.textContent = schemaJson;
    document.head.appendChild(script);
    return () => {
      const s = document.getElementById('vehicle-schema');
      if (s) s.remove();
    };
  }, [schemaJson, vehicle]);

  if (!vehicle) {
    return (
      <div className="pt-32 md:pt-40 pb-20 px-4 md:px-8 max-w-7xl mx-auto w-full text-center">
        <h2 className="text-2xl font-bold text-zinc-950">Vehicle not found</h2>
        <button onClick={() => setView('home')} className="mt-4 text-[#050548] underline">Back to Home</button>
      </div>
    );
  }

  const handleBook = () => {
    setSelectedFleetId(vehicle.id);
    setView('booking');
  };

  return (
    <div className="pt-32 md:pt-40 pb-20 px-4 md:px-8 max-w-7xl mx-auto w-full text-left">
      {/* Back Button */}
      <button
        onClick={() => setView('home')}
        className="flex items-center gap-2 text-zinc-500 hover:text-[#050548] transition-colors mb-8 cursor-pointer"
      >
        <ChevronLeft size={16} />
        <span className="text-xs font-bold uppercase tracking-widest">Back to Home</span>
      </button>

      {/* HERO SECTION */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-16 items-center">
        <div className="relative w-full aspect-[4/3] rounded-[2.5rem] overflow-hidden border border-zinc-200 shadow-xl bg-zinc-50">
          <img
            src={vehicle.image}
            alt={vehicle.seo.title}
            className="w-full h-full object-cover"
            loading="eager"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
          <div className={`absolute bottom-4 left-4 bg-gradient-to-r ${categoryColors[vehicle.category] || 'from-[#050548] to-[#030330]'} text-white text-xs font-bold px-3 py-1.5 rounded-full uppercase tracking-widest`}>
            {categoryLabels[vehicle.category] || vehicle.category}
          </div>
        </div>

        <div className="flex flex-col gap-6">
          <div>
            <h1 className="text-3xl md:text-5xl font-semibold text-zinc-950 tracking-tight leading-tight">
              {vehicle.name}
            </h1>
            <p className="text-zinc-500 text-sm mt-2 max-w-xl leading-relaxed">
              {vehicle.desc}
            </p>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div className="bg-zinc-50 border border-zinc-100 rounded-xl p-3 text-center">
              <Users size={16} className="text-[#050548] mx-auto mb-1" />
              <span className="text-[13px] text-zinc-500 font-mono uppercase block">Seats</span>
              <span className="text-sm font-bold text-zinc-900">{vehicle.specs.pax.split(' ')[2] || '5'} Max</span>
            </div>
            <div className="bg-zinc-50 border border-zinc-100 rounded-xl p-3 text-center">
              <Briefcase size={16} className="text-[#050548] mx-auto mb-1" />
              <span className="text-[13px] text-zinc-500 font-mono uppercase block">Luggage</span>
              <span className="text-sm font-bold text-zinc-900">{vehicle.specs.luggage.split(' ')[0]} Bags</span>
            </div>
            <div className="bg-zinc-50 border border-zinc-100 rounded-xl p-3 text-center">
              <Award size={16} className="text-[#050548] mx-auto mb-1" />
              <span className="text-[13px] text-zinc-500 font-mono uppercase block">Class</span>
              <span className="text-sm font-bold text-zinc-900 truncate">{vehicle.specs.type.split(' ')[0]}</span>
            </div>
          </div>

          <div className="bg-gradient-to-br from-[#050548] to-[#030330] rounded-2xl p-5 text-white">
            <span className="text-xs font-mono uppercase tracking-widest text-white/70">Daily Rate</span>
            <div className="flex items-baseline gap-1 mt-1">
              <span className="text-3xl font-bold">₦{vehicle.pricePerDay.toLocaleString()}</span>
              <span className="text-sm text-white/70">/ day</span>
            </div>
            <p className="text-xs text-white/60 mt-1">Chauffeur and fuel included. Custom quote for long-term hire.</p>
          </div>

          <button
            onClick={handleBook}
            className="w-full bg-[#050548] text-white font-bold py-4 rounded-xl hover:bg-[#030330] transition-all uppercase tracking-widest active:scale-95 cursor-pointer shadow-lg shadow-[#050548]/20 flex items-center justify-center gap-2"
          >
            Reserve This Vehicle <ArrowRight size={16} />
          </button>
        </div>
      </section>

      {/* KEYWORDS / TAGS */}
      <div className="flex flex-wrap gap-2 mb-16">
        {vehicle.seo.keywords.slice(0, 6).map((kw, i) => (
          <span key={i} className="text-[13px] bg-zinc-100 text-zinc-600 px-3 py-1 rounded-full font-medium">
            {kw}
          </span>
        ))}
      </div>

      {/* FEATURES & BEST FOR */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-10 mb-16">
        <div className="bg-white border border-zinc-100 rounded-[2rem] p-8 shadow-sm">
          <h2 className="text-lg font-bold text-zinc-950 uppercase tracking-widest mb-6 flex items-center gap-2">
            <ShieldCheck size={18} className="text-[#050548]" /> Features & Amenities
          </h2>
          <ul className="flex flex-col gap-3">
            {vehicle.features.map((feat, i) => (
              <li key={i} className="flex items-start gap-3 text-zinc-700 text-sm leading-relaxed">
                <CheckCircle2 size={14} className="text-[#050548] shrink-0 mt-0.5" />
                <span>{feat}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="bg-white border border-zinc-100 rounded-[2rem] p-8 shadow-sm">
          <h2 className="text-lg font-bold text-zinc-950 uppercase tracking-widest mb-6 flex items-center gap-2">
            <Star size={18} className="text-[#050548]" /> Best For
          </h2>
          <ul className="flex flex-col gap-3">
            {vehicle.bestFor.map((use, i) => (
              <li key={i} className="flex items-start gap-3 text-zinc-700 text-sm leading-relaxed">
                <MapPin size={14} className="text-[#050548] shrink-0 mt-0.5" />
                <span>{use}</span>
              </li>
            ))}
          </ul>

          <div className="mt-8 pt-6 border-t border-zinc-100">
            <h3 className="text-xs font-bold text-zinc-500 uppercase tracking-widest mb-3">Available In</h3>
            <div className="flex flex-wrap gap-2">
              <span className="flex items-center gap-1 text-xs bg-[#050548]/10 text-[#050548] px-3 py-1.5 rounded-full font-medium">
                <MapPin size={10} /> Benin City
              </span>
              <span className="flex items-center gap-1 text-xs bg-[#050548]/10 text-[#050548] px-3 py-1.5 rounded-full font-medium">
                <MapPin size={10} /> Lagos
              </span>
              <span className="flex items-center gap-1 text-xs bg-[#050548]/10 text-[#050548] px-3 py-1.5 rounded-full font-medium">
                <MapPin size={10} /> Abuja
              </span>
              <span className="flex items-center gap-1 text-xs bg-[#050548]/10 text-[#050548] px-3 py-1.5 rounded-full font-medium">
                <MapPin size={10} /> Nationwide
              </span>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ SECTION */}
      <section className="mb-16 max-w-3xl">
        <h2 className="text-lg font-bold text-zinc-950 uppercase tracking-widest mb-6">
          Frequently Asked Questions — {vehicle.name}
        </h2>
        <div className="flex flex-col gap-4">
          {vehicle.faq.map((item, i) => (
            <details key={i} className="bg-zinc-50 border border-zinc-100 rounded-2xl p-5 group open:border-[#050548]/20 open:bg-white transition-all cursor-pointer">
              <summary className="text-sm font-semibold text-zinc-900 list-none flex items-center justify-between">
                <span>{item.q}</span>
                <span className="text-[#050548] text-lg transition-transform group-open:rotate-45 shrink-0 ml-4">+</span>
              </summary>
              <p className="mt-3 text-sm text-zinc-600 leading-relaxed">{item.a}</p>
            </details>
          ))}
        </div>
      </section>

      {/* FLEET NAVIGATION */}
      <section className="border-t border-zinc-100 pt-10">
        <h3 className="text-xs font-bold text-zinc-500 uppercase tracking-widest mb-4">Explore Other Vehicles</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {FLEET_DATA.filter(f => f.id !== vehicle.id).slice(0, 4).map(v => (
            <button
              key={v.id}
              onClick={() => { setSelectedFleetId(v.id); setView('vehicle-detail'); }}
              className="bg-zinc-50 border border-zinc-100 rounded-2xl p-3 text-left hover:border-[#050548]/20 hover:bg-white transition-all cursor-pointer"
            >
              <img src={v.image} alt={v.name} className="w-full h-16 object-cover rounded-lg mb-2" />
              <span className="text-xs font-bold text-zinc-900 block">{v.name}</span>
              <span className="text-[11px] text-zinc-400 font-mono">₦{v.pricePerDay.toLocaleString()}/day</span>
            </button>
          ))}
        </div>
      </section>

      {/* CTA BANNER */}
      <section className="mt-16 bg-gradient-to-br from-[#050548] to-[#030330] rounded-[2.5rem] p-8 md:p-12 text-center text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(255,255,255,0.05),transparent_70%)] pointer-events-none" />
        <div className="relative z-10">
          <h2 className="text-2xl md:text-3xl font-semibold tracking-tight mb-3">
            Ready to Rent the {vehicle.name}?
          </h2>
          <p className="text-white/70 text-sm max-w-md mx-auto mb-6">
            Book now and enjoy a safe, comfortable ride with our professional chauffeur service.
          </p>
          <button
            onClick={handleBook}
            className="bg-white text-[#050548] font-bold px-8 py-4 rounded-xl uppercase tracking-widest active:scale-95 cursor-pointer transition-all hover:bg-white/90 shadow-lg"
          >
            Book Now — ₦{vehicle.pricePerDay.toLocaleString()}/day
          </button>
        </div>
      </section>

      {/* Hidden SEO description for crawlers */}
      <div className="sr-only" aria-hidden="false">
        <h2>About the {vehicle.name} — Engraced Logistics</h2>
        <p>{vehicle.seo.description}</p>
        <p>Keywords: {vehicle.seo.keywords.join(', ')}</p>
        <p>Daily rental rate: ₦{vehicle.pricePerDay.toLocaleString()} in Benin City, Lagos, and Abuja. Chauffeur included.</p>
      </div>
    </div>
  );
}
