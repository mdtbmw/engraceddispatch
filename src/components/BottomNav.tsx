/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Home, Compass, User, KeySquare, CalendarSearch, LayoutGrid, Brain } from 'lucide-react';

interface BottomNavProps {
  currentView: string;
  setView: (view: string) => void;
}

export default function BottomNav({ currentView, setView }: BottomNavProps) {
  const menu = [
    { label: 'Overview', view: 'home', icon: Home },
    { label: 'Booking', view: 'booking', icon: LayoutGrid },
    { label: 'Ops Desk', view: 'tracking', icon: Compass },
    { label: 'Contract', view: 'corporate', icon: User },
    { label: 'AI Desk', view: 'copilot', icon: Brain },
  ];

  return (
    <div className="lg:hidden fixed bottom-6 left-1/2 -translate-x-1/2 w-[92%] max-w-sm bg-gradient-to-br from-[#050548] to-[#030330] backdrop-blur-xl border border-[#050548]/30 rounded-2xl p-2.5 flex justify-around items-center z-50 shadow-2xl">
      {menu.map((item) => {
        const Icon = item.icon;
        const isActive = currentView === item.view;
        return (
          <button 
            key={item.view}
            onClick={() => setView(item.view)}
            className={`p-2 flex flex-col items-center gap-1 active:scale-90 transition-transform cursor-pointer focus:outline-none ${
               isActive ? 'text-white' : 'text-zinc-400 hover:text-white'
            }`}
          >
            <Icon size={18} />
            <span className="text-[14px] font-bold uppercase tracking-widest">{item.label}</span>
          </button>
        );
      })}
    </div>
  );
}
