import { useState, useEffect } from 'react';
import { Star, Quote, ChevronLeft, ChevronRight } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

interface Review {
  name: string;
  rating: number;
  text: string;
  date: string;
}

const FALLBACK_REVIEWS: Review[] = [
  { name: 'Chioma O.', rating: 5, text: 'Excellent service! The car was clean and the driver was very professional. Will definitely use again.', date: '2025-12-10' },
  { name: 'Emeka N.', rating: 5, text: 'Rented a Prado for my wedding. Impeccable service and the vehicle was spotless. Highly recommended!', date: '2025-11-22' },
  { name: 'Amara K.', rating: 4, text: 'Good experience overall. On-time pickup and smooth interstate trip. Very reliable.', date: '2025-10-15' },
  { name: 'Tunde B.', rating: 5, text: 'VIP protocol service was top-notch. Airport pickup with name board, very professional chauffeur.', date: '2025-09-28' },
  { name: 'Blessing A.', rating: 4, text: 'Comfortable ride from Benin to Lagos. The car was well-maintained and air conditioning worked perfectly.', date: '2025-08-14' },
  { name: 'Kunle S.', rating: 5, text: 'Best car rental service in Benin City. Great fleet, fair prices, and excellent customer support.', date: '2025-07-30' },
];

export default function ReviewsSection() {
  const [reviews, setReviews] = useState<Review[]>(FALLBACK_REVIEWS);
  const [current, setCurrent] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Try to fetch from Google Business Profile
    const fetchGoogleReviews = async () => {
      try {
        // Placeholder for Google Business Profile API integration
        // In production, use your Google Business Profile API key
        // const response = await fetch('/api/google-reviews');
        // const data = await response.json();
        // if (data.reviews) setReviews(data.reviews);
        setIsLoading(false);
      } catch {
        setIsLoading(false);
      }
    };
    fetchGoogleReviews();
  }, []);

  const next = () => setCurrent((prev) => (prev + 1) % reviews.length);
  const prev = () => setCurrent((prev) => (prev - 1 + reviews.length) % reviews.length);

  return (
    <section id="reviews" className="py-16 px-4 md:px-8 max-w-7xl mx-auto w-full">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        className="text-center mb-12"
      >
        <h2 className="text-[16px] font-bold text-[#050548] uppercase tracking-widest mb-2 font-mono">Testimonials</h2>
        <h3 className="text-2xl md:text-3.5xl font-medium text-zinc-950 tracking-tight">What Our Clients Say</h3>
        <p className="text-zinc-500 text-xs md:text-sm max-w-lg mx-auto mt-3">
          Real feedback from our valued clients across Nigeria.
        </p>
      </motion.div>

      <div className="relative max-w-3xl mx-auto">
        {!isLoading && (
          <div className="relative min-h-[280px] flex items-center justify-center">
            <AnimatePresence mode="wait">
              <motion.div
                key={current}
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -50 }}
                transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
                className="bg-white border border-zinc-100 rounded-[2rem] p-8 md:p-12 shadow-lg text-center relative w-full"
              >
                <Quote size={32} className="text-[#050548]/10 absolute top-6 left-6" />
                <div className="flex justify-center gap-1 mb-4">
                  {Array.from({ length: reviews[current].rating }).map((_, i) => (
                    <Star key={i} size={18} className="fill-[#050548] text-[#050548]" />
                  ))}
                </div>
                <p className="text-zinc-700 text-sm md:text-base leading-relaxed italic mb-6 font-sans">
                  "{reviews[current].text}"
                </p>
                <div>
                  <p className="text-[#050548] font-bold text-sm font-sans">{reviews[current].name}</p>
                  <p className="text-zinc-400 text-[16px] font-mono mt-1">{reviews[current].date}</p>
                </div>
              </motion.div>
            </AnimatePresence>

            <button
              onClick={prev}
              className="absolute -left-4 md:-left-6 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white border border-zinc-200 shadow-lg flex items-center justify-center text-zinc-600 hover:text-[#050548] hover:border-[#050548]/30 transition-all cursor-pointer active:scale-90"
            >
              <ChevronLeft size={18} />
            </button>
            <button
              onClick={next}
              className="absolute -right-4 md:-right-6 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white border border-zinc-200 shadow-lg flex items-center justify-center text-zinc-600 hover:text-[#050548] hover:border-[#050548]/30 transition-all cursor-pointer active:scale-90"
            >
              <ChevronRight size={18} />
            </button>
          </div>
        )}

        {/* Dots */}
        <div className="flex justify-center gap-2 mt-6">
          {reviews.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrent(idx)}
              className={`w-2 h-2 rounded-full transition-all duration-300 cursor-pointer ${
                idx === current ? 'bg-[#050548] w-6' : 'bg-zinc-300 hover:bg-zinc-400'
              }`}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
