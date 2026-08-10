/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { DESTINATIONS } from '../data';

export default function RouteMarquee() {
  return (
    <div className="w-full overflow-hidden bg-gradient-to-r from-[#f0f5ff] to-[#e8f0fe] py-4.5 border-y border-[#050548]/10 mb-16 relative">
      <div className="absolute left-0 top-0 bottom-0 w-20 bg-gradient-to-r from-[#f0f5ff] to-transparent z-10 pointer-events-none" />
      <div className="absolute right-0 top-0 bottom-0 w-20 bg-gradient-to-l from-[#e8f0fe] to-transparent z-10 pointer-events-none" />
      
      <div className="animate-marquee flex items-center gap-10">
        {DESTINATIONS.map((dest, i) => (
          <div key={i} className="flex items-center gap-10 shrink-0">
            <span className="text-[16.5px] font-bold text-zinc-500 uppercase tracking-[0.2em] font-mono">
              {dest}
            </span>
            <span className="text-[#050548] text-sm">✦</span>
          </div>
        ))}
      </div>
    </div>
  );
}
