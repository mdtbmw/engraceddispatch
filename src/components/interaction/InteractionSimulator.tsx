import React, { useState, useEffect } from 'react';
import { SoundEngine, SoundCue } from '../../lib/interaction/SoundEngine';

export const InteractionSimulator: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [lastEvent, setLastEvent] = useState<string>('System Idle');
  const [isMuted, setIsMuted] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl+Shift+E toggles the interaction simulator
      if (e.ctrlKey && e.shiftKey && (e.key === 'E' || e.key === 'e')) {
        e.preventDefault();
        setIsOpen(prev => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const triggerCue = (name: string, cue: SoundCue) => {
    setLastEvent(`${name} at ${new Date().toLocaleTimeString()}`);
    SoundEngine.playCue(cue);
  };

  const toggleMute = () => {
    const next = !isMuted;
    setIsMuted(next);
    SoundEngine.setMuted(next);
  };

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-4 left-4 z-50 p-2.5 rounded-full bg-neutral-900/90 text-[#FFB800] border border-[#FFB800]/30 shadow-lg hover:scale-105 active:scale-95 transition-all text-xs font-bold flex items-center gap-1.5 backdrop-blur-md"
        title="Open Interaction Physics Lab (Ctrl+Shift+E)"
      >
        <span className="w-2 h-2 rounded-full bg-[#FFB800] animate-ping" />
        ⚡ Physics Lab
      </button>
    );
  }

  return (
    <div className="fixed bottom-4 left-4 z-50 w-80 bg-[#121212]/95 border border-[#FFB800]/40 rounded-2xl shadow-2xl backdrop-blur-xl p-4 text-white font-sans text-xs space-y-3 animate-in fade-in slide-in-from-bottom-4 duration-200">
      <div className="flex items-center justify-between border-b border-neutral-800 pb-2">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full bg-[#FFB800]" />
          <h3 className="font-black tracking-wider text-[#FFB800] uppercase text-[11px]">
            ESDispatch Physics Lab
          </h3>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={toggleMute}
            className={`px-2 py-0.5 rounded text-[10px] font-bold ${
              isMuted ? 'bg-red-500/20 text-red-400' : 'bg-[#FFB800]/20 text-[#FFB800]'
            }`}
          >
            {isMuted ? 'Muted' : 'Audio ON'}
          </button>
          <button
            onClick={() => setIsOpen(false)}
            className="text-neutral-400 hover:text-white text-sm"
          >
            ✕
          </button>
        </div>
      </div>

      <div className="p-2 rounded-lg bg-neutral-900/80 border border-neutral-800 text-[11px] text-neutral-300">
        <span className="text-neutral-500 block text-[9px] uppercase tracking-wider">Active Event:</span>
        <span className="font-medium text-[#FFB800]">{lastEvent}</span>
      </div>

      <div className="space-y-1.5">
        <span className="text-[10px] uppercase font-bold tracking-wider text-neutral-400 block">
          Signature Moments:
        </span>
        <div className="grid grid-cols-2 gap-1.5">
          <button
            onClick={() => triggerCue('🚀 Dispatch Broadcast Pulse', 'dispatch_broadcast')}
            className="p-2 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-[#FFB800]/60 active:scale-95 transition-all text-left font-medium text-neutral-200 hover:text-[#FFB800]"
          >
            🚀 Dispatch Broadcast
          </button>
          <button
            onClick={() => triggerCue('📍 50m Geofence Arrival', 'geofence_arrival')}
            className="p-2 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-[#FFB800]/60 active:scale-95 transition-all text-left font-medium text-neutral-200 hover:text-[#FFB800]"
          >
            📍 Geofence Arrival
          </button>
          <button
            onClick={() => triggerCue('💰 Escrow Release Settled', 'escrow_settled')}
            className="p-2 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-[#FFB800]/60 active:scale-95 transition-all text-left font-medium text-neutral-200 hover:text-[#FFB800]"
          >
            💰 Escrow Settlement
          </button>
          <button
            onClick={() => triggerCue('⚡ Surge Pricing Broadcast', 'surge_alert')}
            className="p-2 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-[#FFB800]/60 active:scale-95 transition-all text-left font-medium text-neutral-200 hover:text-[#FFB800]"
          >
            ⚡ Surge Alert
          </button>
        </div>
      </div>

      <div className="space-y-1.5 pt-1">
        <span className="text-[10px] uppercase font-bold tracking-wider text-neutral-400 block">
          Tactile Audio Cues:
        </span>
        <div className="grid grid-cols-3 gap-1">
          <button
            onClick={() => triggerCue('Tactile Click', 'tactile_click')}
            className="p-1.5 rounded bg-neutral-900 border border-neutral-800 hover:bg-neutral-800 active:scale-95 transition-all text-center text-[10px] text-neutral-300"
          >
            Click
          </button>
          <button
            onClick={() => triggerCue('Card Swipe', 'card_swipe')}
            className="p-1.5 rounded bg-neutral-900 border border-neutral-800 hover:bg-neutral-800 active:scale-95 transition-all text-center text-[10px] text-neutral-300"
          >
            Swipe
          </button>
          <button
            onClick={() => triggerCue('Error Buzz', 'error_buzz')}
            className="p-1.5 rounded bg-neutral-900 border border-neutral-800 hover:bg-neutral-800 active:scale-95 transition-all text-center text-[10px] text-neutral-300"
          >
            Error
          </button>
        </div>
      </div>

      <div className="text-[9px] text-neutral-500 text-center pt-1">
        Press <kbd className="px-1 py-0.5 rounded bg-neutral-800 text-neutral-300">Ctrl+Shift+E</kbd> to toggle
      </div>
    </div>
  );
};
