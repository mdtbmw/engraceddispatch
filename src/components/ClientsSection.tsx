import { motion } from 'motion/react';

interface Client {
  name: string;
  renderLogo: () => React.ReactNode;
}

export default function ClientsSection() {
  const clients: Client[] = [
    {
      name: 'Pind Foundation',
      renderLogo: () => (
        <svg viewBox="0 0 160 50" className="w-full h-full text-zinc-700 hover:text-[#050548] transition-colors" fill="currentColor">
          <g transform="translate(10, 10)">
            {/* Green / Teal Leaf/Delta Symbol */}
            <path d="M 0 15 C 5 0, 15 0, 20 15 C 15 30, 5 30, 0 15 Z" fill="#0f766e" />
            <path d="M 8 10 C 12 0, 22 0, 26 15 C 22 25, 12 25, 8 10 Z" fill="#0d9488" opacity="0.8" />
            {/* Typography */}
            <text x="35" y="20" font-family="'Outfit', sans-serif" font-weight="800" font-size="13" fill="#18181b" letter-spacing="1">PIND</text>
            <text x="35" y="28" font-family="'Inter', sans-serif" font-weight="500" font-size="7" fill="#71717a" letter-spacing="0.5">FOUNDATION</text>
          </g>
        </svg>
      )
    },
    {
      name: 'BAECC',
      renderLogo: () => (
        <svg viewBox="0 0 160 50" className="w-full h-full text-zinc-700 hover:text-[#050548] transition-colors" fill="currentColor">
          <g transform="translate(10, 8)">
            {/* Gold / Royal Blue Wings/Crest Symbol */}
            <path d="M 5 15 C 15 5, 20 15, 25 15 C 20 20, 10 20, 5 15 Z" fill="#ca8a04" />
            <path d="M 25 15 C 15 15, 10 25, 5 25 C 10 30, 20 30, 25 15 Z" fill="#ca8a04" />
            <circle cx="15" cy="18" r="4" fill="#1e3a8a" />
            {/* Typography */}
            <text x="35" y="20" font-family="'Outfit', sans-serif" font-weight="900" font-size="14" fill="#18181b" letter-spacing="1.5">BAECC</text>
            <text x="35" y="28" font-family="'Inter', sans-serif" font-weight="600" font-size="6" fill="#71717a" letter-spacing="0.5">EXECUTIVE CLUB</text>
          </g>
        </svg>
      )
    },
    {
      name: 'Lee Engineering',
      renderLogo: () => (
        <svg viewBox="0 0 160 50" className="w-full h-full text-zinc-700 hover:text-[#050548] transition-colors" fill="currentColor">
          <g transform="translate(10, 8)">
            {/* Orange Industrial Gear Segment / Hexagon */}
            <polygon points="15,5 25,10 25,22 15,27 5,22 5,10" fill="#ea580c" />
            <circle cx="15" cy="16" r="4" fill="#ffffff" />
            {/* Typography */}
            <text x="35" y="19" font-family="'Outfit', sans-serif" font-weight="800" font-size="13" fill="#18181b" letter-spacing="0.5">LEE</text>
            <text x="62" y="19" font-family="'Outfit', sans-serif" font-weight="400" font-size="13" fill="#ea580c">ENG.</text>
            <text x="35" y="28" font-family="'Inter', sans-serif" font-weight="600" font-size="6.5" fill="#71717a" letter-spacing="1">CONSTRUCTION</text>
          </g>
        </svg>
      )
    }
  ];

  // Repeat the client list to fill the marquee seamlessly
  const repeatedClients = [...clients, ...clients, ...clients, ...clients];

  return (
    <section id="clients" className="py-16 px-4 md:px-8 max-w-7xl mx-auto w-full overflow-hidden">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        className="text-center mb-12"
      >
        <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">Our Clients</h2>
        <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 tracking-tight">Trusted By Great Companies</h3>
        <p className="text-zinc-500 text-xs md:text-sm max-w-lg mx-auto mt-3">
          We are proud to work with these organizations.
        </p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0 }}
        whileInView={{ opacity: 1 }}
        viewport={{ once: true }}
        transition={{ duration: 0.8, delay: 0.2 }}
        className="relative"
      >
        <div className="absolute left-0 top-0 bottom-0 w-32 bg-gradient-to-r from-white to-transparent z-10 pointer-events-none" />
        <div className="absolute right-0 top-0 bottom-0 w-32 bg-gradient-to-l from-white to-transparent z-10 pointer-events-none" />
        
        <div className="flex gap-12 items-center animate-marquee py-4">
          {repeatedClients.map((client, idx) => (
            <div
              key={idx}
              className="flex-shrink-0 w-[170px] h-[75px] bg-white border border-zinc-100 rounded-xl flex items-center justify-center p-3 shadow-sm hover:shadow-md hover:border-[#050548]/30 transition-all duration-300"
            >
              {client.renderLogo()}
            </div>
          ))}
        </div>
      </motion.div>
    </section>
  );
}
