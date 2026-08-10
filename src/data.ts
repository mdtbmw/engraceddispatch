/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { FleetItem, ServiceCategory } from './types';

// Import new local images intelligently (JPEGs for Prado, LC, Hilux)
import pradoFront from './images/prado_front.jpg';
import pradoBack from './images/prado_back.jpg';
import pradoSide from './images/prado_side.jpg';
import pradoBoarding from './images/prado_boarding_v2.jpg';

import lcFront from './images/lc_front.jpg';
import lcBack from './images/lc_back.jpg';
import lcSide from './images/lc_side.jpg';
import lcBoarding from './images/lc_boarding_v2.jpg';

import hiluxFront from './images/hilux_front.jpg';
import hiluxBack from './images/hilux_back.jpg';
import hiluxSide from './images/hilux_side.jpg';
import hiluxBoarding from './images/hilux_boarding_v2.jpg';

// Import JPEGs for Corolla, Camry, Hiace, Coaster, Sienna, Lexus GX
import corollaFront from './images/corolla_front.jpg';
import corollaBack from './images/corolla_back.jpg';
import corollaSide from './images/corolla_side.jpg';
import corollaBoarding from './images/corolla_boarding.jpg';

import camryFront from './images/camry_front.jpg';
import camryBack from './images/camry_back.jpg';
import camrySide from './images/camry_side.jpg';
import camryBoarding from './images/camry_boarding.jpg';

import hiaceFront from './images/hiace_front.jpg';
import hiaceBack from './images/hiace_back.jpg';
import hiaceSide from './images/hiace_side.jpg';
import hiaceBoarding from './images/hiace_boarding.jpg';

import coasterFront from './images/coaster_front.jpg';
import coasterBack from './images/coaster_back.jpg';
import coasterSide from './images/coaster_side.jpg';
import coasterBoarding from './images/coaster_boarding.jpg';

import siennaFront from './images/sienna_front.jpg';
import siennaBack from './images/sienna_back.jpg';
import siennaSide from './images/sienna_side.jpg';
import siennaBoarding from './images/sienna_boarding.jpg';

import lexusFront from './images/lexus_front.jpg';
import lexusBack from './images/lexus_back.jpg';
import lexusSide from './images/lexus_side.jpg';
import lexusBoarding from './images/lexus_boarding.jpg';

export const HERO_SLIDES = [
  {
    id: 1,
    image: lcFront,
    tagline: 'Car Rental in Benin City',
    title: 'Ride in Comfort. Arrive in Style.',
  },
  {
    id: 2,
    image: pradoFront,
    tagline: 'Cars, SUVs & Buses',
    title: 'We Have the Right Vehicle for You.',
  },
  {
    id: 3,
    image: hiluxSide,
    tagline: 'Safe Travel Always',
    title: 'Your Safety is Our Number One Priority.',
  }
];

export const DESTINATIONS = [
  "BENIN ↔ LAGOS", "BENIN ↔ ABUJA", "BENIN ↔ PORT HARCOURT", "BENIN ↔ WARRI", "NATIONWIDE TRANSPORTATION",
  "BENIN ↔ LAGOS", "BENIN ↔ ABUJA", "BENIN ↔ PORT HARCOURT", "BENIN ↔ WARRI", "NATIONWIDE TRANSPORTATION"
];

export const SERVICE_CATEGORIES: ServiceCategory[] = [
  {
    id: 'corporate_airport',
    title: 'Corporate & Airport',
    iconName: 'Briefcase',
    items: [
      { name: 'Corporate Logistics', desc: 'Need reliable transport for your business? We provide cars and drivers for companies in Benin City, Lagos, and Abuja.' },
      { name: 'Airport Transfers', desc: 'We pick you up from the airport and take you where you need to go. On time, every time.' }
    ]
  },
  {
    id: 'rentals_chauffeurs',
    title: 'Rentals & Chauffeurs',
    iconName: 'Car',
    items: [
      { name: 'Vehicle Rentals', desc: 'Rent a car by the day, week, or month. We have sedans, SUVs, buses, and luxury cars — all insured and clean.' },
      { name: 'Chauffeur Hire', desc: 'Need a driver? Our drivers are professional, friendly, and know the roads well.' }
    ]
  },
  {
    id: 'interstate_travel',
    title: 'Interstate Travel',
    iconName: 'MapPin',
    items: [
      { name: 'Interstate Travel', desc: 'Travel between cities safely and comfortably. We connect Benin City to Lagos, Abuja, Port Harcourt, and more.' }
    ]
  },
  {
    id: 'protocol_security',
    title: 'Protocol & Security',
    iconName: 'ShieldCheck',
    items: [
      { name: 'VIP & Protocol', desc: 'Special treatment for important people. Fast airport clearance, VIP movement, and executive service.' },
      { name: 'Security Escort', desc: 'Need extra security? We provide escort vehicles and trained personnel to keep you safe on the road.' }
    ]
  },
  {
    id: 'events_tourism',
    title: 'Events & Tourism',
    iconName: 'CalendarDays',
    items: [
      { name: 'Event Transport', desc: 'Planning a wedding, party, or conference? We have buses and luxury cars to move your guests in style.' },
      { name: 'Tour & Leisure', desc: 'Want to explore Benin City or beyond? Let our drivers take you on a tour. Sit back and enjoy the sights.' }
    ]
  }
];

export const FLEET_DATA: FleetItem[] = [
  {
    id: 'toyota_prado_2014',
    name: 'Toyota Prado (2014)',
    image: pradoFront,
    images: [pradoFront, pradoBack, pradoSide, pradoBoarding],
    specs: { pax: 'Up to 5 Passengers', luggage: '4 Large Bags', type: 'Classic 4x4 SUV' },
    desc: 'A reliable and sturdy SUV perfect for executives and VIPs. Comfortable, spacious, and built for long trips. Available in Benin City, Lagos, and Abuja.',
    pricePerDay: 80000,
    category: 'suvs',
    seo: {
      title: 'Toyota Prado 2014 Rental in Nigeria | Reliable SUV Hire Benin City, Lagos & Abuja',
      description: 'Rent the 2014 Toyota Prado in Nigeria — a sturdy, reliable SUV for VIP transport, executive travel, and long-distance comfort. Available in Benin City, Lagos, and Abuja with professional chauffeur.',
      keywords: ['Toyota Prado 2014 rental Nigeria', 'reliable SUV hire Lagos', 'VIP car rental Benin City', 'executive SUV Abuja', 'Prado chauffeur service Nigeria', 'luxury car hire for wedding Nigeria', 'SUV for airport transfer'],
      ogImage: pradoFront
    },
    features: [
      'Leather interior with comfortable seating',
      'Robust 4x4 system for all road types',
      'Standard infotainment system with Bluetooth',
      'Rear parking sensors and camera',
      'Cruise control',
      'Dual-zone automatic climate control',
      'Power sunroof',
      'CD/MP3 audio system',
      'Durable and smooth suspension system'
    ],
    bestFor: [
      'Executive business travel between cities',
      'VIP airport pickup and drop-off',
      'Corporate retreats and management transport',
      'Wedding convoy and special event fleet',
      'Comfortable interstate road trips with family',
      'Government official movement and protocol'
    ],
    faq: [
      { q: 'How much does it cost to rent a Toyota Prado 2014 per day in Nigeria?', a: 'The Toyota Prado 2014 rents for ₦80,000 per day with a professional chauffeur included. Long-term and corporate rates are available on request.' },
      { q: 'Can I drive the Prado myself or does a driver come with it?', a: 'Our standard rental includes a professional chauffeur. Self-drive options may be available for qualified clients with valid ID and driving license.' },
      { q: 'Which cities can I rent the Toyota Prado 2014 in?', a: 'The Toyota Prado 2014 is available for rental in Benin City, Lagos, and Abuja. Interstate travel between these cities is our specialty.' },
      { q: 'Is the Prado 2014 good for long road trips from Benin to Lagos?', a: 'Absolutely. The Prado is built for durability and comfort on long journeys, making it ideal for Benin City to Lagos trips.' }
    ]
  },
  {
    id: 'toyota_landcruiser_2015',
    name: 'Toyota Land Cruiser (2015)',
    image: lcFront,
    images: [lcFront, lcBack, lcSide, lcBoarding],
    specs: { pax: 'Up to 5 Passengers', luggage: '4 Large Bags', type: 'VIP Convoy SUV' },
    desc: 'The classic king of SUVs. Strong, tough, and great for VIP trips. Used by government officials and executives who want reliable prestige.',
    pricePerDay: 120000,
    category: 'suvs',
    seo: {
      title: 'Toyota Land Cruiser 2015 Rental Nigeria | VIP SUV Hire',
      description: 'Rent the 2015 Toyota Land Cruiser in Nigeria — the classic VIP SUV for government officials, executives, and security convoys. Available in Benin City, Lagos, Abuja. Chauffeur included.',
      keywords: ['Toyota Land Cruiser 2015 rental Nigeria', 'VIP SUV hire Lagos', 'armored car rental Nigeria', 'government official transport Benin City', 'luxury SUV Abuja', 'executive convoy vehicle', 'Land Cruiser chauffeur service'],
      ogImage: lcFront
    },
    features: [
      'Powerful V8 engine',
      'Full-time 4WD with locking center differential',
      'Premium leather seats',
      'Rear-seat DVD entertainment system',
      'Multi-terrain select',
      'Underbody protection and reinforced suspension',
      'Cool box in center console for refreshments',
      'JBL sound system',
      'Convoy-ready capabilities'
    ],
    bestFor: [
      'Head of state and government official movement',
      'High-profile VIP security convoys',
      'Executive airport transfers with protocol clearance',
      'Diplomatic missions and embassy transport',
      'Premium interstate VIP travel',
      'Corporate CEO daily transport'
    ],
    faq: [
      { q: 'How much per day for the 2015 Land Cruiser rental in Nigeria?', a: 'The 2015 Toyota Land Cruiser rents for ₦120,000 per day with a professional chauffeur and full support. Security escort packages are available extra.' },
      { q: 'Is the Land Cruiser available for security convoy operations?', a: 'Yes. We specialize in security convoy deployments. The Land Cruiser can be configured with escort vehicles, trained drivers, and coordination support.' },
      { q: 'Can I rent the Land Cruiser for a wedding in Benin City?', a: 'Certainly. The Land Cruiser is an excellent choice for wedding convoys. It brings prestige and elegance to any occasion.' }
    ]
  },
  {
    id: 'toyota_hilux_2015',
    name: 'Toyota Hilux (2015)',
    image: hiluxFront,
    images: [hiluxFront, hiluxBack, hiluxSide, hiluxBoarding],
    specs: { pax: 'Up to 5 Passengers', luggage: '4 Medium Bags', type: 'Escort Pickup' },
    desc: 'A tough, proven pickup truck built for rough roads and heavy work. Perfect for security, farm work, and construction sites.',
    pricePerDay: 70000,
    category: 'vip',
    seo: {
      title: 'Toyota Hilux 2015 Rental Nigeria | Security Escort & Utility Pickup Hire',
      description: 'Rent the 2015 Toyota Hilux in Nigeria — the classic pickup truck for security escort, construction sites, and rural travel. Available in Benin City, Lagos, Abuja.',
      keywords: ['Toyota Hilux 2015 rental Nigeria', 'security escort vehicle hire', 'pickup truck rental Benin City', 'Hilux for farm work Nigeria', 'construction site transport Lagos', 'rugged vehicle hire Abuja', 'armed escort Hilux'],
      ogImage: hiluxFront
    },
    features: [
      'Reliable diesel engine',
      'High ground clearance for rough terrain',
      'Reinforced cargo bed with tie-down hooks',
      'Heavy-duty suspension for load carrying',
      'Durable cloth seats and rubber floor mats',
      'Power windows and central locking',
      'Manual air conditioning',
      'Ready for security escort and convoy support equipment'
    ],
    bestFor: [
      'Security escort and protection detail convoys',
      'Construction site material transport',
      'Agricultural and farm operations',
      'Rural and off-road travel in difficult terrain',
      'Emergency response and support vehicle',
      'Organized patrol and surveillance operations'
    ],
    faq: [
      { q: 'How much to rent a Toyota Hilux per day in Nigeria?', a: 'The 2015 Toyota Hilux rents for ₦70,000 per day with a professional driver. Long-term and corporate rates are available.' },
      { q: 'Can the Hilux be used for armed security escort?', a: 'Yes. The Hilux is our primary vehicle for security escort operations. It can be equipped for support roles and convoy coordination.' },
      { q: 'Is the Hilux available for farm and rural use?', a: 'Absolutely. The Hilux is built for tough conditions. It is perfect for farm access roads, rural communities, and construction zones.' }
    ]
  },
  {
    id: 'corolla_2010',
    name: 'Toyota Corolla (2010)',
    image: corollaFront,
    images: [corollaFront, corollaBack, corollaSide, corollaBoarding],
    specs: { pax: 'Up to 4 Passengers', luggage: '2 Medium Bags', type: 'Comfort Sedan' },
    desc: 'A simple, reliable car that is easy on fuel. Great for city trips, meetings, and airport runs. Affordable and comfortable.',
    pricePerDay: 25000,
    category: 'sedans',
    seo: {
      title: 'Toyota Corolla 2010 Rental Nigeria | Affordable Sedan Hire Benin City',
      description: 'Rent a 2010 Toyota Corolla in Nigeria — affordable, fuel-efficient, and perfect for city trips, airport runs, and meetings. Available in Benin City, Lagos, Abuja.',
      keywords: ['Toyota Corolla 2010 rental Nigeria', 'affordable car hire Benin City', 'budget sedan Lagos', 'fuel efficient car rental Abuja', 'city car hire Nigeria', 'cheap rental car Benin', 'airport transfer sedan'],
      ogImage: corollaFront
    },
    features: [
      '1.8L petrol engine with automatic transmission',
      'Fuel-efficient — great for city driving and long trips',
      'Air conditioning',
      'Comfortable cloth seats',
      'Power steering, windows, and central locking',
      'CD/MP3 audio system',
      'Spacious trunk for luggage',
      'Anti-lock braking system (ABS)',
      'Low maintenance and high reliability'
    ],
    bestFor: [
      'Daily city commuting and office runs',
      'Airport pickup and drop-off in Benin City',
      'Short business meetings and client visits',
      'Affordable weekend trips and errands',
      'Budget-friendly self-drive or chauffeur hire',
      'University and school-related transport'
    ],
    faq: [
      { q: 'How much does the Corolla 2010 cost per day?', a: 'The Toyota Corolla 2010 rents for ₦25,000 per day, making it our most affordable option. Driver and fuel are included.' },
      { q: 'Is the Corolla good for airport transfers?', a: 'Yes, the Corolla is perfect for airport runs in Benin City and Lagos. It is fuel-efficient, easy to park, and comfortable for short trips.' },
      { q: 'How many people fit in the Corolla?', a: 'The Corolla comfortably seats 4 passengers plus the driver. It is ideal for individuals or small groups.' }
    ]
  },
  {
    id: 'camry_2008',
    name: 'Toyota Camry (2008)',
    image: camryFront,
    images: [camryFront, camryBack, camrySide, camryBoarding],
    specs: { pax: 'Up to 4 Passengers', luggage: '2 Medium Bags', type: 'Premium Sedan' },
    desc: 'A spacious and smooth sedan. Perfect for executive travel and long trips. Comfortable seats and a quiet ride.',
    pricePerDay: 30000,
    category: 'sedans',
    seo: {
      title: 'Toyota Camry 2008 Rental Nigeria | Executive Sedan Hire Benin City',
      description: 'Rent a 2008 Toyota Camry in Nigeria — spacious, smooth, and ideal for executive travel and interstate trips. Available with chauffeur in Benin City, Lagos, Abuja.',
      keywords: ['Toyota Camry 2008 rental Nigeria', 'executive sedan hire Benin City', 'spacious car rental Lagos', 'comfortable sedan Abuja', 'interstate travel car hire', 'business trip car Nigeria', 'Camry chauffeur service'],
      ogImage: camryFront
    },
    features: [
      '2.4L engine with smooth automatic transmission',
      'Spacious interior with premium fabric seats',
      'Extra legroom for rear passengers',
      'Large trunk capacity for luggage and bags',
      'Air conditioning',
      'Power windows, mirrors, and door locks',
      'CD/MP3 stereo',
      'Cruise control for highway driving',
      'Anti-lock brakes and multiple airbags'
    ],
    bestFor: [
      'Executive travel and business trips',
      'Interstate journeys between Benin and Lagos',
      'Airport transfers with extra comfort',
      'Family outings and weekend getaways',
      'Professional chauffeur-driven service',
      'Corporate staff transport and client pickups'
    ],
    faq: [
      { q: 'What is the daily rental price for the Toyota Camry 2008?', a: 'The Toyota Camry 2008 rents for ₦30,000 per day with a professional driver. Long-term discounts are available.' },
      { q: 'Is the Camry good for Benin to Lagos road trips?', a: 'Yes, the Camry is an excellent choice for Benin to Lagos journeys. Its spacious interior and smooth ride make long trips comfortable.' },
      { q: 'How many passengers fit in the Camry?', a: 'The Camry seats up to 4 passengers plus the driver. It offers more rear legroom than the Corolla.' }
    ]
  },
  {
    id: 'hiace_bus_2012',
    name: 'Toyota Hiace Bus (2012 Hummer Model)',
    image: hiaceFront,
    images: [hiaceFront, hiaceBack, hiaceSide, hiaceBoarding],
    specs: { pax: 'Up to 14 Passengers', luggage: '8 Medium Bags', type: 'Commuter Bus' },
    desc: 'A 14-seater bus perfect for groups, family trips, and corporate outings. The classic reliable "Hummer" bus.',
    pricePerDay: 60000,
    category: 'vans',
    seo: {
      title: 'Toyota Hiace Bus 2012 Rental Nigeria | 14-Seater Group Transport Benin City',
      description: 'Rent a 2012 Toyota Hiace "Hummer" Bus in Nigeria — perfect for group travel, corporate outings, weddings, and family trips. 14 seats with AC. Available in Benin City, Lagos, Abuja.',
      keywords: ['Toyota Hiace bus 2012 rental Nigeria', '14 seater bus hire Benin City', 'group transport Lagos', 'corporate bus rental Abuja', 'wedding bus hire Nigeria', 'family trip bus Benin', 'hummer bus rental'],
      ogImage: hiaceFront
    },
    features: [
      '14 comfortable seats with seat belts',
      'Reliable diesel engine',
      'Air conditioning with overhead vents',
      'Under-seat luggage storage',
      'Professional uniformed driver included',
      'Tinted windows for privacy and sun protection',
      'Regularly serviced and roadworthy certified'
    ],
    bestFor: [
      'Corporate group travel and staff transport',
      'Wedding guest shuttles and event logistics',
      'Family reunions and group outings',
      'Airport group transfers and shuttle services',
      'School and university excursions',
      'Church and community group transportation'
    ],
    faq: [
      { q: 'How much to rent a Toyota Hiace Bus per day?', a: 'The 2012 Toyota Hiace Bus rents for ₦60,000 per day with a professional driver and fuel included. Volume discounts for multiple days.' },
      { q: 'How many people can the Hiace Bus carry?', a: 'The Hiace Bus seats up to 14 passengers comfortably plus the driver.' },
      { q: 'Can I use the Hiace Bus for a wedding in Benin City?', a: 'Absolutely. The Hiace Bus is frequently booked for wedding guest transport. It keeps your guests together and on time.' }
    ]
  },
  {
    id: 'coaster_bus_2012',
    name: 'Toyota Coaster Bus (2012)',
    image: coasterFront,
    images: [coasterFront, coasterBack, coasterSide, coasterBoarding],
    specs: { pax: 'Up to 28 Passengers', luggage: '15 Large Bags', type: 'Group Transit Coach' },
    desc: 'A big 28-seater bus for large groups, weddings, and events. Spacious and reliable for long trips.',
    pricePerDay: 100000,
    category: 'vans',
    seo: {
      title: 'Toyota Coaster Bus 2012 Rental Nigeria | 28-Seater Coach Hire Benin City',
      description: 'Rent a 2012 Toyota Coaster Bus in Nigeria — the largest group transport option for weddings, corporate events, and mass transit. 28 seats, AC. Available Benin City, Lagos, Abuja.',
      keywords: ['Toyota Coaster bus 2012 rental Nigeria', '28 seater bus hire Benin City', 'large group transport Lagos', 'wedding coach rental Abuja', 'corporate event bus Nigeria', 'mass transit bus Benin', 'excursion coach hire Lagos'],
      ogImage: coasterFront
    },
    features: [
      '28 comfortable seats',
      'Powerful diesel engine built for long-distance travel',
      'Air conditioning',
      'Luggage compartments',
      'Overhead storage racks for carry-on items',
      'Professional driver with route knowledge',
      'Reading lights',
      'Tinted privacy windows',
      'Regular safety inspections and certified roadworthiness'
    ],
    bestFor: [
      'Large wedding guest convoys and receptions',
      'Corporate off-site retreats and staff events',
      'School and university field trips',
      'Church group excursions and pilgrimages',
      'Political rallies and campaign movement',
      'Mass transit for events and conferences'
    ],
    faq: [
      { q: 'How much is the Toyota Coaster Bus rental per day?', a: 'The 2012 Toyota Coaster Bus rents for ₦100,000 per day with a professional driver. Fuel and basic maintenance included.' },
      { q: 'How many passengers can the Coaster Bus carry?', a: 'The Coaster Bus seats up to 28 passengers comfortably.' },
      { q: 'Is the Coaster Bus good for interstate travel?', a: 'Yes, the Coaster Bus is built for long-distance travel. It is comfortable, air conditioned, and reliable for cross-country trips.' }
    ]
  },
  {
    id: 'toyota_sienna_2010',
    name: 'Toyota Sienna (2010)',
    image: siennaFront,
    images: [siennaFront, siennaBack, siennaSide, siennaBoarding],
    specs: { pax: 'Up to 7 Passengers', luggage: '5 Medium Bags', type: 'Minivan' },
    desc: 'A spacious minivan for families and small groups. Great for airport runs, city trips, and comfortable travel with luggage.',
    pricePerDay: 45000,
    category: 'vans',
    seo: {
      title: 'Toyota Sienna 2010 Rental Nigeria | 7-Seater Family Van Hire',
      description: 'Rent a 2010 Toyota Sienna Minivan in Nigeria — spacious 7-seater for family trips, airport runs, and group outings. Available in Benin City, Lagos, Abuja.',
      keywords: ['Toyota Sienna 2010 rental Nigeria', '7 seater minivan hire Benin City', 'family van rental Lagos', 'airport minivan Abuja', 'group transport Benin', 'spacious car hire for family Nigeria', 'Sienna chauffeur service'],
      ogImage: siennaFront
    },
    features: [
      '3.5L V6 engine',
      '7 passenger seats with easy fold access',
      'Air conditioning',
      'Stowable third-row seats for extra cargo space',
      'Sliding doors for easy entry and exit',
      'Premium cloth seating options',
      'Multiple cup holders and storage compartments',
      'Smooth ride quality for long journeys'
    ],
    bestFor: [
      'Family vacations and road trips',
      'Airport transfers with luggage for groups',
      'Small group tours and excursions',
      'Corporate staff shuttle for small teams',
      'Shopping trips and errands with family',
      'Comfortable travel for elderly passengers'
    ],
    faq: [
      { q: 'What is the daily rental price for the Toyota Sienna 2010?', a: 'The 2010 Toyota Sienna rents for ₦45,000 per day with a professional driver included.' },
      { q: 'How many people fit in the Sienna?', a: 'The Sienna seats up to 7 passengers comfortably. It is ideal for families and small groups.' },
      { q: 'Can I rent the Sienna for a trip from Benin to Lagos?', a: 'Absolutely. The Sienna is comfortable for Benin to Lagos trips and offers a smooth, quiet ride for everyone.' }
    ]
  },
  {
    id: 'lexus_gx_2012',
    name: 'Lexus GX 460 (2012)',
    image: lexusFront,
    images: [lexusFront, lexusBack, lexusSide, lexusBoarding],
    specs: { pax: 'Up to 5 Passengers', luggage: '4 Large Bags', type: 'Luxury Executive SUV' },
    desc: 'Classic luxury SUV with leather seats and a super smooth ride. A favorite for VIPs and executives.',
    pricePerDay: 100000,
    category: 'suvs',
    seo: {
      title: 'Lexus GX 460 2012 Rental Nigeria | Luxury Executive SUV Hire Benin City',
      description: 'Rent a 2012 Lexus GX 460 in Nigeria — a premium luxury SUV for VIP executives and high-end travel. Leather, comfort, and prestige. Available Benin City, Lagos, Abuja.',
      keywords: ['Lexus GX 460 rental Nigeria', 'luxury SUV hire Benin City', 'executive car rental Lagos', 'premium SUV Abuja', 'VIP car hire Nigeria', 'luxury chauffeur service Benin', 'Lexus rental for wedding Nigeria'],
      ogImage: lexusFront
    },
    features: [
      '4.6L V8 engine',
      'Full-time 4WD',
      'Premium leather seating throughout',
      'Heated and ventilated front seats',
      'Premium sound system',
      'Automatic climate control',
      'Power third-row seats that fold flat',
      'Smooth suspension for ultra-smooth ride'
    ],
    bestFor: [
      'VIP executive daily transport',
      'High-end corporate client pickups',
      'Luxury wedding convoys and events',
      'Premium airport transfer service',
      'CEO and board member transportation',
      'Exclusive leisure and weekend getaways'
    ],
    faq: [
      { q: 'What is the rental cost for a Lexus GX 460 per day?', a: 'The 2012 Lexus GX 460 SUV rents for ₦100,000 per day with a professional chauffeur. Executive packages available.' },
      { q: 'Is the Lexus GX better than the Prado?', a: 'The Lexus GX is the luxury version of the Prado. It offers a quieter cabin, more premium materials, and a smoother ride.' },
      { q: 'Can the Lexus GX be used for interstate VIP travel?', a: 'Yes. The Lexus GX is perfect for VIP interstate travel. Its luxury interior and smooth suspension make long trips enjoyable.' }
    ]
  }
];
