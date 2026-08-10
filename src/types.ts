/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface FleetItem {
  id: string;
  name: string;
  image: string;
  images?: string[];
  cutout?: string;
  specs: {
    pax: string;
    luggage: string;
    type: string;
  };
  desc: string;
  pricePerDay: number;
  category: string;
  seo: {
    title: string;
    description: string;
    keywords: string[];
    ogImage: string;
  };
  features: string[];
  bestFor: string[];
  faq: { q: string; a: string }[];
}

export interface ServiceItem {
  name: string;
  desc: string;
}

export interface ServiceCategory {
  id: string;
  title: string;
  iconName: string; // Dynamic icon rendering name
  items: ServiceItem[];
}

export interface Booking {
  id: string;
  vehicleType: string;
  serviceType: 'chauffeur' | 'vip-protocol';
  pickupLocation: string;
  dropoffLocation: string;
  pickupDate: string;
  pickupTime: string;
  securityLevel: 'standard' | 'armed-escort' | 'covert';
  durationDays: number;
  totalCost: number;
  status: 'pending' | 'active' | 'completed';
  clientName: string;
  clientEmail: string;
  clientPhone: string;
}

export interface TrackingMission {
  id: string;
  driverName: string;
  vehicleName: string;
  status: 'In Transit' | 'Departed' | 'Arrived' | 'Standby';
  route: string;
  speed: number;
  fuelLevel: number;
  coordinates: { x: number; y: number };
  telemetryLog: string[];
}
