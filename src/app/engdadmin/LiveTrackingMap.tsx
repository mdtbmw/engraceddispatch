"use client";
import React, { useEffect, useMemo, useRef, useState, useCallback } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import {
  MapPin,
  Bike,
  Phone,
  Copy,
  Check,
  X,
  Crosshair,
  Eye,
  Maximize2,
  Minimize2,
  Layers,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
  Compass,
  Zap,
  Clock,
  ArrowRight,
  Sparkles
} from "lucide-react";

export interface Coord {
  lat: number;
  lng: number;
}

export interface RegistryEntry {
  id: string;
  name: string;
  tags: string[];
  lat: number;
  lng: number;
  zone: string;
  active: boolean;
}

/** Documented default map center (system_config/geo). Only used before the geo doc loads. */
export const DEFAULT_GEO_CENTER: Coord = { lat: 6.3350, lng: 5.6037 };

export const isBeninCityCoord = (lat?: number, lng?: number): boolean => {
  if (typeof lat !== "number" || typeof lng !== "number" || isNaN(lat) || isNaN(lng)) return false;
  return lat >= 6.10 && lat <= 6.55 && lng >= 5.45 && lng <= 5.85;
};

/** Broader validity check for Nigerian geographic coordinates so fringe locations are not dropped */
export const isValidGeoCoord = (lat?: number, lng?: number): boolean => {
  if (typeof lat !== "number" || typeof lng !== "number" || isNaN(lat) || isNaN(lng)) return false;
  if (lat === 0 && lng === 0) return false;
  return lat >= 4.0 && lat <= 14.5 && lng >= 2.5 && lng <= 15.0;
};

/**
 * Resolves an address string against the admin-owned address_registry.
 * Matching is case-insensitive: the registry name is looked for inside the address,
 * or any registry tag is looked for inside the address. Active entries win over inactive ones.
 * Returns null when nothing matches — callers must NOT invent a fallback point.
 */
export function resolveFromRegistry(address: string, entries: RegistryEntry[] | undefined): Coord | null {
  const a = (address || "").toLowerCase().trim();
  if (!a || !entries || entries.length === 0) return null;
  let inactiveMatch: Coord | null = null;
  for (const e of entries) {
    if (typeof e.lat !== "number" || typeof e.lng !== "number") continue;
    if (!isValidGeoCoord(e.lat, e.lng)) continue;
    const name = (e.name || "").toLowerCase().trim();
    const matchName = name.length >= 3 && a.includes(name);
    const matchTag = (e.tags || []).some(t => {
      const tag = String(t || "").toLowerCase().trim();
      return tag.length >= 3 && a.includes(tag);
    });
    if (!matchName && !matchTag) continue;
    if (e.active !== false) return { lat: e.lat, lng: e.lng };
    if (!inactiveMatch) inactiveMatch = { lat: e.lat, lng: e.lng };
  }
  return inactiveMatch;
}

/** Stored coordinates always win; otherwise fall back to the registry; otherwise no point at all. */
export function resolveEndpoint(
  lat: unknown,
  lng: unknown,
  address: string,
  entries: RegistryEntry[] | undefined
): Coord | null {
  if (typeof lat === "number" && typeof lng === "number" && isFinite(lat) && isFinite(lng) && isValidGeoCoord(lat, lng)) {
    return { lat, lng };
  }
  return resolveFromRegistry(address, entries);
}

export interface DriverMarkerData {
  id: string;
  pos: Coord;
  name: string;
  phone?: string;
  photoUrl?: string;
  avatarUrl?: string;
  vehicleType?: string;
  vehiclePlate?: string;
  rating?: number;
  deliveryCount?: number;
  speed?: number;
  heading?: number;
  status: string;
  isOnline: boolean;
  isOccupied: boolean;
  isDelayed: boolean;
  lastUpdateAgo?: string;
  activeDelivery?: any;
}

interface Props {
  deliveries: any[];
  drivers: any[];
  selectedId: string | null;
  onSelect: (id: string | null) => void;
  addressRegistry: RegistryEntry[];
  geoCenter: Coord;
  isFullscreen?: boolean;
  onToggleFullscreen?: () => void;
}

type TileTheme = "googleRoadmap" | "googleHybrid" | "darkObsidian";

const TILE_CONFIGS: Record<TileTheme, { name: string; url: string; subdomains: string[] | string; maxZoom: number; attr: string }> = {
  googleRoadmap: {
    name: "Roadmap",
    url: "https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}",
    subdomains: ["mt0", "mt1", "mt2", "mt3"],
    maxZoom: 20,
    attr: "© Google Maps"
  },
  googleHybrid: {
    name: "Satellite",
    url: "https://{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}",
    subdomains: ["mt0", "mt1", "mt2", "mt3"],
    maxZoom: 20,
    attr: "© Google Maps Satellite"
  },
  darkObsidian: {
    name: "Dark",
    url: "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png",
    subdomains: "abcd",
    maxZoom: 19,
    attr: "© CartoDB"
  }
};

const CSS_STYLE_ID = "esdispatch-live-map-styles";

function escapeHtml(str: string): string {
  return (str || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

export default function LiveTrackingMap({
  deliveries,
  drivers,
  selectedId,
  onSelect,
  addressRegistry,
  geoCenter,
  isFullscreen: externalFullscreen,
  onToggleFullscreen: externalToggleFullscreen,
}: Props) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const tileLayerRef = useRef<L.TileLayer | null>(null);
  const markersGroupRef = useRef<L.LayerGroup | null>(null);
  const routeGroupRef = useRef<L.LayerGroup | null>(null);
  const markerMapRef = useRef<Map<string, L.Marker>>(new Map());

  // Component state
  const [tileTheme, setTileTheme] = useState<TileTheme>("googleRoadmap");
  const [filterTransitOnly, setFilterTransitOnly] = useState(true);
  const [selectedDriver, setSelectedDriver] = useState<DriverMarkerData | null>(null);
  const [followedRiderId, setFollowedRiderId] = useState<string | null>(null);
  const [isCopiedPhone, setIsCopiedPhone] = useState(false);
  const [sideDrawerOpen, setSideDrawerOpen] = useState(true);
  const [internalFullscreen, setInternalFullscreen] = useState(false);

  const isFullscreen = externalFullscreen !== undefined ? externalFullscreen : internalFullscreen;
  const toggleFullscreen = externalToggleFullscreen || (() => setInternalFullscreen(prev => !prev));

  // Compute all couriers and detect active delivery transit
  const { allCouriers, inTransitCouriers, activeDeliveryPoints } = useMemo(() => {
    const list: DriverMarkerData[] = [];
    const inTransit: DriverMarkerData[] = [];
    const activeDelivPoints: { pickup?: Coord; dropoff?: Coord }[] = [];

    drivers.forEach((r) => {
      // Find matching active delivery
      const activeDelivery = deliveries.find(d => {
        const isAssigned = (
          d.riderId === r.id ||
          d.riderId === r.uid ||
          d.assignedRiderId === r.id ||
          d.assignedRiderId === r.uid ||
          (r.activeBookingId && d.id === r.activeBookingId)
        );
        const inProgress = !["DELIVERED", "CANCELLED", "FAILED", "RETURNED"].includes(d.status);
        return isAssigned && inProgress;
      });

      const isOccupied = Boolean(activeDelivery);

      // Determine coordinate: driver GPS > active delivery courier coordinates > initial courier coordinates > pickup
      let lat: number | undefined = typeof r.lat === "number" && isFinite(r.lat) ? r.lat : undefined;
      let lng: number | undefined = typeof r.lng === "number" && isFinite(r.lng) ? r.lng : undefined;

      if (!isValidGeoCoord(lat, lng) && activeDelivery) {
        if (isValidGeoCoord(activeDelivery.courierLatitude, activeDelivery.courierLongitude)) {
          lat = activeDelivery.courierLatitude;
          lng = activeDelivery.courierLongitude;
        } else if (isValidGeoCoord(activeDelivery.initialCourierLat, activeDelivery.initialCourierLng)) {
          lat = activeDelivery.initialCourierLat;
          lng = activeDelivery.initialCourierLng;
        } else if (isValidGeoCoord(activeDelivery.pickupLat, activeDelivery.pickupLng)) {
          lat = activeDelivery.pickupLat;
          lng = activeDelivery.pickupLng;
        }
      }

      if (!isValidGeoCoord(lat, lng)) return;

      // Telemetry freshness calculation
      const lastUpdateMs = typeof r.lastLocationUpdate === "number"
        ? r.lastLocationUpdate
        : (r.lastLocationUpdate?.toMillis ? r.lastLocationUpdate.toMillis() : null);
      const now = Date.now();
      const diffMinutes = lastUpdateMs ? Math.floor((now - lastUpdateMs) / 60000) : null;
      const isDelayed = diffMinutes !== null ? diffMinutes >= 5 : false;
      const lastUpdateAgo = diffMinutes !== null ? (diffMinutes === 0 ? "Just now" : `${diffMinutes}m ago`) : undefined;

      const courierData: DriverMarkerData = {
        id: r.id || r.uid || Math.random().toString(),
        pos: { lat: lat!, lng: lng! },
        name: r.name || "Fleet Courier",
        phone: r.phone || r.phoneNumber || "",
        photoUrl: r.photoUrl || r.avatarUrl || "",
        avatarUrl: r.avatarUrl || r.photoUrl || "",
        vehicleType: r.vehicleType || r.bikeModel || "Motorcycle",
        vehiclePlate: r.vehiclePlate || r.bikeNumber || r.plateNumber || "",
        rating: typeof r.rating === "number" ? r.rating : 4.9,
        deliveryCount: r.deliveryCount || 0,
        speed: typeof r.speed === "number" ? r.speed : (isOccupied ? 24 : 0),
        heading: typeof r.heading === "number" ? r.heading : 0,
        status: isOccupied ? "In Transit" : (r.isOnline !== false ? "Available" : "Offline"),
        isOnline: r.isOnline !== false,
        isOccupied,
        isDelayed,
        lastUpdateAgo,
        activeDelivery
      };

      list.push(courierData);
      if (isOccupied) {
        inTransit.push(courierData);

        const pickup = resolveEndpoint(activeDelivery.pickupLat, activeDelivery.pickupLng, activeDelivery.pickupAddress || "", addressRegistry);
        const dropoff = resolveEndpoint(activeDelivery.deliveryLat, activeDelivery.deliveryLng, activeDelivery.deliveryAddress || "", addressRegistry);
        activeDelivPoints.push({ pickup: pickup || undefined, dropoff: dropoff || undefined });
      }
    });

    return {
      allCouriers: list,
      inTransitCouriers: inTransit,
      activeDeliveryPoints: activeDelivPoints
    };
  }, [deliveries, drivers, addressRegistry]);

  // The active couriers to display on map based on filter
  const displayedCouriers = useMemo(() => {
    return filterTransitOnly ? inTransitCouriers : allCouriers;
  }, [filterTransitOnly, inTransitCouriers, allCouriers]);

  // Keep selected driver data fresh when telemetry updates
  useEffect(() => {
    if (!selectedDriver) return;
    const fresh = allCouriers.find(c => c.id === selectedDriver.id);
    if (fresh) {
      setSelectedDriver(fresh);
    }
  }, [allCouriers]);

  // Synchronize when external selectedId (delivery ID) changes
  useEffect(() => {
    if (!selectedId) return;
    const matched = inTransitCouriers.find(c => c.activeDelivery?.id === selectedId);
    if (matched) {
      setSelectedDriver(matched);
      setSideDrawerOpen(true);
    }
  }, [selectedId, inTransitCouriers]);

  // Inject CSS once
  useEffect(() => {
    if (typeof document === "undefined") return;
    if (document.getElementById(CSS_STYLE_ID)) return;

    const style = document.createElement("style");
    style.id = CSS_STYLE_ID;
    style.textContent = `
      @keyframes esdispatch-map-pulse {
        0% { transform: scale(0.6); opacity: 0.95; }
        100% { transform: scale(2.0); opacity: 0; }
      }
      .esdispatch-bike-marker, .esdispatch-pickup-marker, .esdispatch-delivery-marker {
        background: transparent !important;
        border: none !important;
      }
      .esdispatch-custom-map .leaflet-bar a {
        background-color: #141416 !important;
        color: #FFB800 !important;
        border-color: rgba(255,255,255,0.12) !important;
      }
      .esdispatch-custom-map .leaflet-bar a:hover {
        background-color: #222226 !important;
        color: #FFFFFF !important;
      }
      .custom-scrollbar::-webkit-scrollbar {
        width: 4px;
      }
      .custom-scrollbar::-webkit-scrollbar-thumb {
        background: rgba(255,184,0,0.3);
        border-radius: 4px;
      }
    `;
    document.head.appendChild(style);
  }, []);

  // Initialize Leaflet Map
  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    const map = L.map(mapContainerRef.current, {
      center: [geoCenter.lat, geoCenter.lng],
      zoom: 13,
      zoomControl: false,
      attributionControl: false,
    });

    L.control.zoom({ position: "topleft" }).addTo(map);

    const cfg = TILE_CONFIGS[tileTheme];
    const tileLayer = L.tileLayer(cfg.url, {
      maxZoom: cfg.maxZoom,
      subdomains: cfg.subdomains,
    }).addTo(map);

    tileLayerRef.current = tileLayer;

    const routeGroup = L.layerGroup().addTo(map);
    const markersGroup = L.layerGroup().addTo(map);
    routeGroupRef.current = routeGroup;
    markersGroupRef.current = markersGroup;

    mapRef.current = map;

    // Initial fit bounds
    setTimeout(() => {
      map.invalidateSize();
    }, 150);

    return () => {
      map.remove();
      mapRef.current = null;
      markerMapRef.current.clear();
    };
  }, []);

  // Switch Tile Layer Theme
  useEffect(() => {
    if (!mapRef.current || !tileLayerRef.current) return;
    const cfg = TILE_CONFIGS[tileTheme];
    tileLayerRef.current.setUrl(cfg.url);
    // @ts-ignore
    tileLayerRef.current.options.subdomains = cfg.subdomains;
    // @ts-ignore
    tileLayerRef.current.options.maxZoom = cfg.maxZoom;
  }, [tileTheme]);

  // Invalidate size on fullscreen toggle
  useEffect(() => {
    if (!mapRef.current) return;
    const timer = setTimeout(() => {
      mapRef.current?.invalidateSize();
    }, 200);
    return () => clearTimeout(timer);
  }, [isFullscreen]);

  // Create DivIcon for rider bike with name tooltip
  const buildBikeIcon = useCallback((c: DriverMarkerData, isSelected: boolean, isFollowed: boolean): L.DivIcon => {
    const speedText = c.speed && c.speed > 0 ? `${Math.round(c.speed)} km/h` : "In Transit";
    const isDelayed = c.isDelayed;

    const html = `
      <div style="position:relative;display:flex;flex-direction:column;align-items:center;cursor:pointer;user-select:none;pointer-events:auto;">
        <!-- Tooltip Label Badge -->
        <div style="margin-bottom:6px;padding:3px 8px;border-radius:12px;background:rgba(14,14,16,0.92);border:1.5px solid ${isSelected ? '#FFB800' : isDelayed ? '#F59E0B' : 'rgba(255,184,0,0.7)'};box-shadow:0 6px 16px rgba(0,0,0,0.65);display:flex;align-items:center;gap:5px;white-space:nowrap;backdrop-filter:blur(8px);transform:${isSelected ? 'scale(1.1)' : 'scale(1)'};transition:all 0.2s;">
          <span style="width:6px;height:6px;border-radius:50%;background:${isDelayed ? '#F59E0B' : '#10B981'};box-shadow:0 0 6px ${isDelayed ? '#F59E0B' : '#10B981'};"></span>
          <span style="font-size:11px;font-weight:900;color:#FFFFFF;letter-spacing:0.2px;">${escapeHtml(c.name)}</span>
          <span style="font-size:9px;font-weight:900;color:#FFB800;background:rgba(255,184,0,0.18);padding:1px 5px;border-radius:6px;">${speedText}</span>
        </div>
        <!-- Bike Circular Beacon Container -->
        <div style="position:relative;display:flex;align-items:center;justify-content:center;">
          <div style="position:absolute;inset:-8px;border-radius:50%;background:rgba(255,184,0,0.32);animation:esdispatch-map-pulse 1.8s ease-out infinite;pointer-events:none;"></div>
          <div style="width:38px;height:38px;border-radius:50%;background:#131312;border:2.5px solid ${isSelected ? '#FFFFFF' : '#FFB800'};display:flex;align-items:center;justify-content:center;box-shadow:0 8px 20px rgba(0,0,0,0.75);transform:${isSelected ? 'scale(1.15)' : 'scale(1)'};transition:all 0.25s;">
            <span style="font-size:20px;line-height:1;">🏍️</span>
          </div>
          ${isFollowed ? `
            <div style="position:absolute;top:-4px;right:-4px;width:14px;height:14px;border-radius:50%;background:#FFB800;border:2px solid #131312;display:flex;align-items:center;justify-content:center;font-size:8px;color:#131312;font-weight:900;">
              🎯
            </div>` : ""}
        </div>
      </div>
    `;

    return L.divIcon({
      className: "esdispatch-bike-marker",
      html,
      iconSize: [140, 68],
      iconAnchor: [70, 52],
    });
  }, []);

  // Update Driver Markers on Map
  useEffect(() => {
    const map = mapRef.current;
    const group = markersGroupRef.current;
    if (!map || !group) return;

    const currentMap = markerMapRef.current;
    const activeIds = new Set<string>();

    displayedCouriers.forEach((courier) => {
      activeIds.add(courier.id);
      const isSelected = selectedDriver?.id === courier.id;
      const isFollowed = followedRiderId === courier.id;
      const icon = buildBikeIcon(courier, isSelected, isFollowed);

      if (currentMap.has(courier.id)) {
        const marker = currentMap.get(courier.id)!;
        marker.setLatLng([courier.pos.lat, courier.pos.lng]);
        marker.setIcon(icon);
      } else {
        const marker = L.marker([courier.pos.lat, courier.pos.lng], {
          icon,
          zIndexOffset: isSelected ? 300 : 100,
        });

        marker.on("click", () => {
          setSelectedDriver(courier);
          setSideDrawerOpen(true);
        });

        marker.addTo(group);
        currentMap.set(courier.id, marker);
      }
    });

    // Remove markers that are no longer displayed
    currentMap.forEach((marker, id) => {
      if (!activeIds.has(id)) {
        marker.remove();
        currentMap.delete(id);
      }
    });

    // If followed rider updated position, smooth pan camera to follow
    if (followedRiderId) {
      const followed = displayedCouriers.find(c => c.id === followedRiderId);
      if (followed) {
        map.panTo([followed.pos.lat, followed.pos.lng], { animate: true, duration: 0.8 });
      }
    }
  }, [displayedCouriers, selectedDriver, followedRiderId, buildBikeIcon]);

  // Render Route Polyline and Pickup/Dropoff Pins when rider is selected
  useEffect(() => {
    const group = routeGroupRef.current;
    if (!group) return;

    group.clearLayers();

    if (!selectedDriver || !selectedDriver.activeDelivery) return;

    const ad = selectedDriver.activeDelivery;
    const pickup = resolveEndpoint(ad.pickupLat, ad.pickupLng, ad.pickupAddress || "", addressRegistry);
    const dropoff = resolveEndpoint(ad.deliveryLat, ad.deliveryLng, ad.deliveryAddress || "", addressRegistry);
    const courierPos = selectedDriver.pos;

    const pickupIcon = L.divIcon({
      className: "esdispatch-pickup-marker",
      html: `
        <div style="display:flex;flex-direction:column;align-items:center;">
          <div style="margin-bottom:4px;padding:2px 7px;border-radius:10px;background:#0E0E10;border:1px solid #FFB800;color:#FFB800;font-size:10px;font-weight:900;white-space:nowrap;box-shadow:0 3px 8px rgba(0,0,0,0.5);">Pickup</div>
          <div style="width:26px;height:26px;border-radius:50%;background:#FFB800;border:3px solid #131312;color:#131312;font-weight:900;font-size:12px;display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(0,0,0,0.5);">P</div>
        </div>
      `,
      iconSize: [70, 52],
      iconAnchor: [35, 46],
    });

    const deliveryIcon = L.divIcon({
      className: "esdispatch-delivery-marker",
      html: `
        <div style="display:flex;flex-direction:column;align-items:center;">
          <div style="margin-bottom:4px;padding:2px 7px;border-radius:10px;background:#0E0E10;border:1px solid #10B981;color:#10B981;font-size:10px;font-weight:900;white-space:nowrap;box-shadow:0 3px 8px rgba(0,0,0,0.5);">Destination</div>
          <div style="width:26px;height:26px;border-radius:50%;background:#131312;border:3px solid #FFB800;color:#FFB800;font-weight:900;font-size:12px;display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(0,0,0,0.5);">D</div>
        </div>
      `,
      iconSize: [80, 52],
      iconAnchor: [40, 46],
    });

    if (pickup) {
      L.marker([pickup.lat, pickup.lng], { icon: pickupIcon, zIndexOffset: 80 }).addTo(group);
    }
    if (dropoff) {
      L.marker([dropoff.lat, dropoff.lng], { icon: deliveryIcon, zIndexOffset: 80 }).addTo(group);
    }

    const routeCoords: [number, number][] = [];
    routeCoords.push([courierPos.lat, courierPos.lng]);
    if (pickup) routeCoords.push([pickup.lat, pickup.lng]);
    if (dropoff) routeCoords.push([dropoff.lat, dropoff.lng]);

    if (routeCoords.length >= 2) {
      // Obsidian Casing
      L.polyline(routeCoords, {
        color: "#131312",
        weight: 9,
        opacity: 0.85,
        lineCap: "round",
        lineJoin: "round",
      }).addTo(group);

      // Gold Core Line
      L.polyline(routeCoords, {
        color: "#FFB800",
        weight: 5,
        opacity: 0.95,
        lineCap: "round",
        lineJoin: "round",
      }).addTo(group);
    }
  }, [selectedDriver, addressRegistry]);

  // Fit all active riders & deliveries on screen
  const handleFitAllBounds = useCallback(() => {
    const map = mapRef.current;
    if (!map) return;
    setFollowedRiderId(null);

    const points: [number, number][] = [];
    displayedCouriers.forEach(c => points.push([c.pos.lat, c.pos.lng]));
    activeDeliveryPoints.forEach(p => {
      if (p.pickup) points.push([p.pickup.lat, p.pickup.lng]);
      if (p.dropoff) points.push([p.dropoff.lat, p.dropoff.lng]);
    });

    if (points.length >= 2) {
      const bounds = L.latLngBounds(points);
      map.fitBounds(bounds, { padding: [60, 60], maxZoom: 16 });
    } else if (points.length === 1) {
      map.setView(points[0], 15, { animate: true });
    } else {
      map.setView([geoCenter.lat, geoCenter.lng], 13, { animate: true });
    }
  }, [displayedCouriers, activeDeliveryPoints, geoCenter]);

  // Focus and Lock camera onto a specific driver
  const handleFocusDriver = useCallback((driver: DriverMarkerData) => {
    setSelectedDriver(driver);
    setFollowedRiderId(driver.id);
    setSideDrawerOpen(true);
    const map = mapRef.current;
    if (map) {
      map.setView([driver.pos.lat, driver.pos.lng], 16, { animate: true });
    }
  }, []);

  const handleToggleFollow = useCallback((driver: DriverMarkerData) => {
    if (followedRiderId === driver.id) {
      setFollowedRiderId(null);
    } else {
      setFollowedRiderId(driver.id);
      mapRef.current?.setView([driver.pos.lat, driver.pos.lng], 16, { animate: true });
    }
  }, [followedRiderId]);

  const copyPhoneNumber = (phone: string) => {
    if (!phone) return;
    navigator.clipboard.writeText(phone);
    setIsCopiedPhone(true);
    setTimeout(() => setIsCopiedPhone(false), 2000);
  };

  // Listen to escape key to exit fullscreen
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isFullscreen) {
        toggleFullscreen();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [isFullscreen, toggleFullscreen]);

  return (
    <div className={`relative w-full overflow-hidden transition-all duration-300 ${
      isFullscreen
        ? "fixed inset-0 z-[9999] w-screen h-screen bg-[#0A0A0C] flex flex-col p-3 md:p-5"
        : "h-full w-full rounded-3xl border border-gray-200 dark:border-white/10 shadow-sm bg-[#111216]"
    }`}>
      {/* Top Header Bar when in Fullscreen Monitor Mode */}
      {isFullscreen && (
        <div className="flex items-center justify-between gap-4 pb-3 mb-3 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-[#FFB800] flex items-center justify-center font-black text-black text-sm">
              ES
            </div>
            <div>
              <h2 className="text-sm font-black text-white flex items-center gap-2">
                LIVE DISPATCH MONITORING COMMAND CENTER
              </h2>
              <p className="text-[10px] text-gray-400 font-medium">
                Autonomous real-time courier telemetry & route tracking
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-black">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
              {inTransitCouriers.length} in Transit
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-white/10 text-gray-300 text-xs font-bold">
              <Bike size={13} className="text-[#FFB800]" />
              {allCouriers.length} Registered
            </span>
            <button
              onClick={toggleFullscreen}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#FFB800] text-black font-extrabold text-xs hover:bg-[#e6a600] active:scale-95 transition-all cursor-pointer"
            >
              <Minimize2 size={14} /> Exit Fullscreen
            </button>
          </div>
        </div>
      )}

      {/* Map Viewport Area */}
      <div className="relative w-full h-full min-h-[350px] flex-1 rounded-2xl overflow-hidden esdispatch-custom-map">
        <div ref={mapContainerRef} className="w-full h-full" />

        {/* Floating Top-Center Controller Bar */}
        <div className="absolute top-3 left-14 sm:left-16 right-auto z-[990] flex items-center gap-2 flex-wrap pointer-events-auto">
          {/* Map Layer Switcher */}
          <div className="flex items-center p-1 rounded-2xl bg-[#0E0E10]/90 backdrop-blur-md border border-white/15 shadow-xl">
            {(["googleRoadmap", "googleHybrid", "darkObsidian"] as TileTheme[]).map((t) => (
              <button
                key={t}
                onClick={() => setTileTheme(t)}
                className={`px-2.5 py-1 rounded-xl text-[11px] font-extrabold transition-all cursor-pointer ${
                  tileTheme === t
                    ? "bg-[#FFB800] text-black shadow-md scale-105"
                    : "text-gray-300 hover:text-white"
                }`}
              >
                {TILE_CONFIGS[t].name}
              </button>
            ))}
          </div>

          {/* Transit Only vs All Fleet Toggle */}
          <button
            onClick={() => setFilterTransitOnly(prev => !prev)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-2xl text-[11px] font-extrabold border shadow-xl backdrop-blur-md transition-all cursor-pointer ${
              filterTransitOnly
                ? "bg-[#0E0E10]/90 text-[#FFB800] border-[#FFB800]/50"
                : "bg-[#0E0E10]/90 text-gray-300 border-white/15 hover:text-white"
            }`}
          >
            <span className={`w-2 h-2 rounded-full ${filterTransitOnly ? "bg-[#FFB800] animate-pulse" : "bg-gray-400"}`} />
            {filterTransitOnly ? "In Transit Only" : "All Fleet"}
          </button>

          {/* Focus All Button */}
          <button
            onClick={handleFitAllBounds}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-2xl bg-[#0E0E10]/90 hover:bg-[#1a1a1f] text-gray-200 border border-white/15 text-[11px] font-extrabold shadow-xl backdrop-blur-md transition-all cursor-pointer"
            title="Fit all couriers on map"
          >
            <Eye size={13} className="text-[#FFB800]" /> Fit All
          </button>

          {/* Fullscreen Button */}
          <button
            onClick={toggleFullscreen}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-2xl bg-[#0E0E10]/90 hover:bg-[#1a1a1f] text-gray-200 border border-white/15 text-[11px] font-extrabold shadow-xl backdrop-blur-md transition-all cursor-pointer"
            title={isFullscreen ? "Exit Fullscreen" : "Fullscreen Monitoring Mode"}
          >
            {isFullscreen ? <Minimize2 size={13} className="text-[#FFB800]" /> : <Maximize2 size={13} className="text-[#FFB800]" />}
            <span className="hidden sm:inline">{isFullscreen ? "Exit" : "Fullscreen"}</span>
          </button>
        </div>

        {/* Floating Side Drawer (Rider Details OR In-Transit Fleet Roster) */}
        {sideDrawerOpen && (
          <div className="absolute top-3 right-3 bottom-3 z-[1000] w-80 sm:w-96 max-w-[calc(100%-24px)] bg-[#0E0E10]/95 backdrop-blur-xl border border-[#FFB800]/40 rounded-3xl p-4 shadow-2xl text-white flex flex-col justify-between overflow-hidden animate-fade-in pointer-events-auto">
            {selectedDriver ? (
              /* ================== STATE 1: SELECTED RIDER DETAILS ================== */
              <div className="flex flex-col h-full justify-between overflow-y-auto custom-scrollbar pr-1">
                <div>
                  {/* Top Bar */}
                  <div className="flex items-center justify-between pb-3 border-b border-white/10 shrink-0">
                    <button
                      onClick={() => {
                        setSelectedDriver(null);
                        setFollowedRiderId(null);
                      }}
                      className="flex items-center gap-1.5 text-xs font-black text-gray-300 hover:text-white transition-colors cursor-pointer"
                    >
                      <ChevronLeft size={16} className="text-[#FFB800]" /> All Couriers
                    </button>
                    <div className="flex items-center gap-2">
                      {followedRiderId === selectedDriver.id && (
                        <span className="px-2 py-0.5 rounded-lg bg-[#FFB800]/20 text-[#FFB800] border border-[#FFB800]/40 text-[9px] font-extrabold flex items-center gap-1">
                          <span className="w-1.5 h-1.5 rounded-full bg-[#FFB800] animate-ping" />
                          CAMERA LOCKED
                        </span>
                      )}
                      <button
                        onClick={() => setSelectedDriver(null)}
                        className="w-6 h-6 rounded-full bg-white/10 hover:bg-white/20 text-gray-300 flex items-center justify-center text-xs cursor-pointer"
                      >
                        ✕
                      </button>
                    </div>
                  </div>

                  {/* Hero Profile */}
                  <div className="flex items-center gap-3.5 my-3.5">
                    <div className="relative shrink-0">
                      {selectedDriver.photoUrl ? (
                        <img
                          src={selectedDriver.photoUrl}
                          alt={selectedDriver.name}
                          className="w-14 h-14 rounded-full object-cover border-2 border-[#FFB800] shadow-lg bg-[#1a1a1f]"
                          onError={(e) => {
                            // Fallback to initials if photo fails to load
                            (e.target as HTMLElement).style.display = "none";
                          }}
                        />
                      ) : null}
                      {/* Initials Fallback */}
                      <div
                        className={`w-14 h-14 rounded-full bg-[#131312] border-2 border-[#FFB800] items-center justify-center text-base font-black text-[#FFB800] shadow-lg ${
                          selectedDriver.photoUrl ? "hidden" : "flex"
                        }`}
                      >
                        {selectedDriver.name.slice(0, 2).toUpperCase()}
                      </div>
                      <span className={`absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full border-2 border-[#131312] ${
                        selectedDriver.isDelayed ? "bg-amber-500" : "bg-emerald-400"
                      }`} />
                    </div>

                    <div className="min-w-0 flex-1">
                      <h4 className="text-base font-black text-white truncate">{selectedDriver.name}</h4>
                      <p className="text-xs text-gray-400 font-medium truncate mt-0.5">
                        {selectedDriver.vehicleType || "Motorcycle"} {selectedDriver.vehiclePlate ? `• ${selectedDriver.vehiclePlate}` : ""}
                      </p>
                      <div className="flex items-center gap-2 mt-1.5">
                        <span className="text-[10px] font-black text-[#FFB800] bg-[#FFB800]/15 px-2 py-0.5 rounded-md">
                          ★ {selectedDriver.rating ? selectedDriver.rating.toFixed(1) : "5.0"}
                        </span>
                        <span className="text-[10px] text-gray-400 font-medium">
                          {selectedDriver.lastUpdateAgo ? `GPS: ${selectedDriver.lastUpdateAgo}` : "Live GPS"}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Action Buttons: Call & Focus */}
                  <div className="grid grid-cols-2 gap-2 mb-3.5">
                    {selectedDriver.phone ? (
                      <a
                        href={`tel:${selectedDriver.phone}`}
                        className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs transition-colors shadow-sm text-center"
                      >
                        <Phone size={13} /> Call Courier
                      </a>
                    ) : (
                      <button disabled className="py-2 px-3 rounded-xl bg-white/5 text-gray-500 text-xs font-bold">
                        No Phone
                      </button>
                    )}

                    <button
                      onClick={() => handleToggleFollow(selectedDriver)}
                      className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl font-extrabold text-xs transition-all shadow-sm cursor-pointer ${
                        followedRiderId === selectedDriver.id
                          ? "bg-[#FFB800] text-black ring-2 ring-[#FFB800]/50"
                          : "bg-white/10 hover:bg-white/20 text-white"
                      }`}
                    >
                      <Crosshair size={13} />
                      {followedRiderId === selectedDriver.id ? "Locked" : "Focus on Rider"}
                    </button>
                  </div>

                  {/* Telemetry Strip */}
                  <div className="grid grid-cols-2 gap-2 mb-3.5">
                    <div className="p-2.5 rounded-xl bg-white/5 border border-white/10">
                      <p className="text-[9px] text-gray-400 font-bold uppercase tracking-wider">Live Speed</p>
                      <p className="text-sm font-black text-white mt-0.5 flex items-center gap-1">
                        <Zap size={13} className="text-[#FFB800]" />
                        {selectedDriver.speed && selectedDriver.speed > 0 ? `${Math.round(selectedDriver.speed)} km/h` : "In Transit"}
                      </p>
                    </div>
                    <div className="p-2.5 rounded-xl bg-white/5 border border-white/10">
                      <p className="text-[9px] text-gray-400 font-bold uppercase tracking-wider">Phone Number</p>
                      <p
                        onClick={() => copyPhoneNumber(selectedDriver.phone || "")}
                        className="text-xs font-bold text-white mt-0.5 truncate cursor-pointer hover:text-[#FFB800] flex items-center gap-1"
                        title="Click to copy"
                      >
                        {selectedDriver.phone || "Not set"}
                        {selectedDriver.phone && (isCopiedPhone ? <Check size={11} className="text-emerald-400" /> : <Copy size={11} className="text-gray-400" />)}
                      </p>
                    </div>
                  </div>

                  {/* Active Delivery Information */}
                  {selectedDriver.activeDelivery ? (
                    <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 space-y-2 mb-3.5">
                      <div className="flex items-center justify-between text-[11px] font-bold">
                        <span className="text-gray-300">Active Delivery</span>
                        <span className="text-[#FFB800] font-mono">#{selectedDriver.activeDelivery.id.slice(0, 8).toUpperCase()}</span>
                      </div>
                      <div>
                        <p className="text-xs font-black text-white truncate">{selectedDriver.activeDelivery.itemName || "Shipment Package"}</p>
                        <p className="text-[10px] text-gray-400 mt-0.5 truncate">
                          Customer: {selectedDriver.activeDelivery.receiverName} {selectedDriver.activeDelivery.receiverPhone ? `• ${selectedDriver.activeDelivery.receiverPhone}` : ""}
                        </p>
                      </div>
                      <div className="space-y-1.5 text-[11px] pt-2 border-t border-white/10">
                        <p className="text-gray-300 truncate flex items-center gap-1.5">
                          <span className="w-4 h-4 rounded-full bg-[#FFB800] text-black font-black text-[9px] flex items-center justify-center shrink-0">P</span>
                          <span className="truncate">{selectedDriver.activeDelivery.pickupAddress || "Pickup location"}</span>
                        </p>
                        <p className="text-gray-300 truncate flex items-center gap-1.5">
                          <span className="w-4 h-4 rounded-full bg-emerald-500 text-white font-black text-[9px] flex items-center justify-center shrink-0">D</span>
                          <span className="truncate">{selectedDriver.activeDelivery.deliveryAddress || "Delivery destination"}</span>
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="p-3 rounded-2xl bg-white/5 border border-white/10 text-xs text-gray-400 text-center mb-3.5">
                      Courier is currently online and available for dispatch.
                    </div>
                  )}
                </div>

                {/* Bottom Actions */}
                <div className="pt-2 border-t border-white/10 flex items-center gap-2 shrink-0">
                  <button
                    onClick={handleFitAllBounds}
                    className="flex-1 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-extrabold transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    <Eye size={13} /> View All Fleet
                  </button>
                  {selectedDriver.activeDelivery && (
                    <button
                      onClick={() => onSelect(selectedDriver.activeDelivery.id)}
                      className="flex-1 py-2 rounded-xl bg-[#FFB800] hover:bg-[#e6a600] text-black text-xs font-black transition-colors flex items-center justify-center gap-1.5 shadow-md cursor-pointer"
                    >
                      View Order <ArrowRight size={13} />
                    </button>
                  )}
                </div>
              </div>
            ) : (
              /* ================== STATE 2: ACTIVE TRANSIT ROSTER ================== */
              <div className="flex flex-col h-full justify-between">
                <div className="pb-3 border-b border-white/10 shrink-0">
                  <div className="flex items-center justify-between">
                    <h3 className="font-black text-sm text-white flex items-center gap-2">
                      <Bike className="w-4 h-4 text-[#FFB800]" /> Fleet in Transit
                    </h3>
                    <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 text-[10px] font-black border border-emerald-500/30">
                      {inTransitCouriers.length} Moving
                    </span>
                  </div>
                  <p className="text-[10px] text-gray-400 mt-1">
                    Click any courier to follow camera & view live route telemetry
                  </p>
                </div>

                {/* Scrollable Courier Roster */}
                <div className="flex-1 overflow-y-auto space-y-2 py-2 pr-1 custom-scrollbar">
                  {inTransitCouriers.length === 0 ? (
                    <div className="py-12 text-center text-gray-400">
                      <Bike className="w-9 h-9 text-gray-600 mx-auto mb-2 opacity-50" />
                      <p className="text-xs font-bold text-gray-300">No active couriers in transit</p>
                      <p className="text-[10px] text-gray-500 mt-1 max-w-[220px] mx-auto">
                        Immediately a courier accepts an order, they appear on this map live.
                      </p>
                      <button
                        onClick={() => setFilterTransitOnly(false)}
                        className="mt-3 text-[11px] font-bold text-[#FFB800] hover:underline"
                      >
                        Show all {allCouriers.length} registered riders →
                      </button>
                    </div>
                  ) : (
                    inTransitCouriers.map((c) => {
                      const speed = c.speed && c.speed > 0 ? `${Math.round(c.speed)} km/h` : "In Transit";
                      return (
                        <div
                          key={c.id}
                          onClick={() => handleFocusDriver(c)}
                          className="p-3 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/10 hover:border-[#FFB800]/50 transition-all cursor-pointer group flex items-center justify-between gap-2.5"
                        >
                          <div className="flex items-center gap-2.5 min-w-0">
                            {/* Avatar */}
                            <div className="relative shrink-0">
                              {c.photoUrl ? (
                                <img
                                  src={c.photoUrl}
                                  alt={c.name}
                                  className="w-10 h-10 rounded-full object-cover border-2 border-[#FFB800] bg-[#1a1a1f]"
                                  onError={(e) => {
                                    (e.target as HTMLElement).style.display = "none";
                                  }}
                                />
                              ) : null}
                              <div
                                className={`w-10 h-10 rounded-full bg-[#131312] border-2 border-[#FFB800] items-center justify-center text-xs font-black text-[#FFB800] ${
                                  c.photoUrl ? "hidden" : "flex"
                                }`}
                              >
                                {c.name.slice(0, 2).toUpperCase()}
                              </div>
                              <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-emerald-400 border border-[#131312]" />
                            </div>

                            <div className="min-w-0">
                              <h4 className="text-xs font-bold text-white group-hover:text-[#FFB800] transition-colors truncate">
                                {c.name}
                              </h4>
                              <p className="text-[10px] text-gray-400 truncate mt-0.5">
                                {c.activeDelivery?.deliveryAddress || c.vehiclePlate || "En route to destination"}
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-2 shrink-0">
                            <span className="px-2 py-0.5 rounded-lg bg-[#FFB800]/15 text-[#FFB800] text-[10px] font-black">
                              {speed}
                            </span>
                            <button
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleFocusDriver(c);
                              }}
                              className="p-1.5 rounded-xl bg-white/10 hover:bg-[#FFB800] hover:text-black text-gray-300 transition-colors"
                              title="Focus & Track"
                            >
                              <Crosshair size={13} />
                            </button>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>

                {/* Bottom Toggle Bar */}
                <div className="pt-2 border-t border-white/10 flex items-center justify-between text-[11px] text-gray-400 shrink-0">
                  <span>{allCouriers.length} Total Couriers on Fleet</span>
                  <button
                    onClick={handleFitAllBounds}
                    className="text-[#FFB800] font-black hover:underline flex items-center gap-1 cursor-pointer"
                  >
                    <Eye size={12} /> Fit All
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Collapsed Drawer Reopen Pill if user minimized side drawer */}
        {!sideDrawerOpen && (
          <button
            onClick={() => setSideDrawerOpen(true)}
            className="absolute top-3 right-3 z-[1000] px-3.5 py-2 rounded-2xl bg-[#0E0E10]/95 border border-[#FFB800]/50 text-white font-extrabold text-xs shadow-2xl backdrop-blur-md hover:bg-[#1a1a1f] flex items-center gap-2 cursor-pointer pointer-events-auto"
          >
            <Bike size={14} className="text-[#FFB800]" />
            <span>Open Fleet Panel ({inTransitCouriers.length})</span>
          </button>
        )}
      </div>
    </div>
  );
}
