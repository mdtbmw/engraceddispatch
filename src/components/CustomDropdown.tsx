/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export interface DropdownOption {
  value: string;
  label: string;
  description?: string;
}

interface CustomDropdownProps {
  options: DropdownOption[];
  value: string;
  onChange: (value: string) => void;
  className?: string;
  align?: 'left' | 'right';
}

export default function CustomDropdown({
  options,
  value,
  onChange,
  className = '',
  align = 'left'
}: CustomDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  
  const selectedOption = options.find(opt => opt.value === value) || options[0];

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className={`relative ${className}`} ref={containerRef}>
      {/* Trigger Button */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-full bg-zinc-50 border border-zinc-200 focus:border-[#050548] focus:bg-white rounded-xl py-3 px-4 text-xs font-medium text-zinc-800 outline-none transition-all flex items-center justify-between cursor-pointer active:scale-[0.99] select-none text-left"
      >
        <span className="truncate">{selectedOption?.label}</span>
        <ChevronDown 
          size={14} 
          className={`text-zinc-400 shrink-0 transition-transform duration-300 ml-2 ${isOpen ? 'rotate-180 text-zinc-800' : ''}`} 
        />
      </button>

      {/* Options Panel */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 5, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 5, scale: 0.98 }}
            transition={{ duration: 0.15 }}
            className={`absolute z-[999] mt-1.5 w-full bg-white border border-zinc-200/80 rounded-2xl shadow-xl max-h-60 overflow-y-auto custom-scrollbar p-1.5 ${
              align === 'right' ? 'right-0' : 'left-0'
            }`}
          >
            {options.map((option) => {
              const isSelected = option.value === value;
              return (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => {
                    onChange(option.value);
                    setIsOpen(false);
                  }}
                  className={`w-full text-left px-3.5 py-2.5 rounded-xl transition-all flex flex-col gap-0.5 select-none cursor-pointer ${
                    isSelected 
                      ? 'bg-[#050548]/10 text-[#050548] font-bold' 
                      : 'text-zinc-600 hover:bg-zinc-50 hover:text-zinc-950'
                  }`}
                >
                  <span className="text-xs">{option.label}</span>
                  {option.description && (
                    <span className={`text-[15.5px] font-sans font-medium ${
                      isSelected ? 'text-[#050548]' : 'text-zinc-400'
                    }`}>
                      {option.description}
                    </span>
                  )}
                </button>
              );
            })}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
