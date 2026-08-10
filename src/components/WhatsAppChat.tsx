import { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

const BRANCHES = [
  { value: 'benin', label: 'Benin', phone: '2348185840000', office: 'Benin City' },
  { value: 'asaba', label: 'Asaba', phone: '2348185880000', office: 'Asaba' },
  { value: 'lagos', label: 'Lagos', phone: '2349056263010', office: 'Lagos' },
];

const SUGGESTIONS = [
  'I want to book a ride',
  'What vehicles do you have?',
];

export default function WhatsAppChat() {
  const [isOpen, setIsOpen] = useState(false);
  const [tab, setTab] = useState('benin');
  const [message, setMessage] = useState('Hello! I want to book a ride.');
  const inputRef = useRef<HTMLTextAreaElement>(null);

  const branch = BRANCHES.find(b => b.value === tab) || BRANCHES[0];

  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => inputRef.current?.focus(), 300);
    }
  }, [isOpen]);

  const handleSend = () => {
    if (!message.trim()) return;
    window.open(`https://wa.me/${branch.phone}?text=${encodeURIComponent(message)}`, '_blank');
  };

  const handleKey = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
  };

  return (
    <>
      <motion.button
        onClick={() => setIsOpen(!isOpen)}
        whileHover={{ scale: 1.06 }}
        whileTap={{ scale: 0.92 }}
        className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full shadow-2xl flex items-center justify-center cursor-pointer bg-gradient-to-br from-[#050548] to-[#050548] shadow-[#050548]/30"
      >
        {isOpen ? <X size={22} className="text-white" /> : <MessageCircle size={24} className="text-white" />}
        {!isOpen && <span className="absolute inset-0 rounded-full bg-[#050548]/30 animate-ping" />}
      </motion.button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.93 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 16, scale: 0.96 }}
            transition={{ type: 'spring', stiffness: 320, damping: 26 }}
            className="fixed bottom-24 right-6 z-50 w-[92vw] max-w-[380px] bg-white rounded-2xl shadow-2xl border border-zinc-100 overflow-hidden"
          >
            {/* Header */}
            <div className="bg-gradient-to-r from-[#050548] to-[#050548] px-5 pt-5 pb-0">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center backdrop-blur-sm">
                  <MessageCircle size={18} className="text-white" />
                </div>
                <div className="flex-1">
                  <p className="font-bold text-white text-[15px]" style={{ fontFamily: 'Outfit, sans-serif' }}>Engraced Logistics</p>
                  <p className="text-[11px] text-white/70">We reply fast</p>
                </div>
                <button onClick={() => setIsOpen(false)} className="w-7 h-7 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer transition-colors">
                  <X size={12} className="text-white" />
                </button>
              </div>

              {/* Tabs */}
              <div className="flex gap-1">
                {BRANCHES.map((b) => (
                  <button
                    key={b.value}
                    onClick={() => setTab(b.value)}
                    className={`flex-1 py-2.5 text-[12px] font-bold uppercase tracking-wider rounded-t-lg transition-all cursor-pointer ${
                      tab === b.value
                        ? 'bg-white text-[#050548]'
                        : 'bg-white/10 text-white/70 hover:bg-white/20'
                    }`}
                  >
                    {b.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Body */}
            <div className="bg-[#F0F2F5] p-4 min-h-[200px]">
              <div className="flex items-start gap-2 mb-3">
                <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#050548] to-[#050548] flex items-center justify-center shrink-0 mt-0.5">
                  <MessageCircle size={12} className="text-white" />
                </div>
                <div className="bg-white rounded-[16px] rounded-tl-sm px-3.5 py-2.5 shadow-sm max-w-[85%]">
                  <p className="text-[13px] text-zinc-700">
                    Chatting with <span className="font-bold text-[#050548]">{branch.office}</span>. How can we help?
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap gap-1.5 mb-3 pl-9">
                {SUGGESTIONS.map((s, i) => (
                  <button
                    key={i}
                    onClick={() => { setMessage(s); inputRef.current?.focus(); }}
                    className="px-3 py-1.5 rounded-full text-[11px] font-semibold bg-white border border-zinc-200 text-zinc-600 hover:border-[#050548]/30 hover:text-[#050548] transition-all cursor-pointer"
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>

            {/* Input */}
            <div className="bg-white px-4 pt-3 pb-4">
              <div className="flex items-end gap-2">
                <div className="flex-1 bg-[#F0F2F5] rounded-2xl border border-transparent focus-within:border-[#050548]/20 transition-all">
                  <textarea
                    ref={inputRef}
                    value={message}
                    onChange={e => setMessage(e.target.value)}
                    onKeyDown={handleKey}
                    rows={1}
                    placeholder="Type a message..."
                    className="w-full bg-transparent px-4 py-2.5 text-[13px] text-zinc-800 outline-none resize-none placeholder:text-zinc-400"
                  />
                </div>
                <button
                  onClick={handleSend}
                  disabled={!message.trim()}
                  className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 transition-all cursor-pointer ${
                    message.trim() ? 'bg-gradient-to-r from-[#050548] to-[#050548] shadow-sm' : 'bg-zinc-200'
                  }`}
                >
                  <Send size={14} className={message.trim() ? 'text-white' : 'text-zinc-400'} />
                </button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
