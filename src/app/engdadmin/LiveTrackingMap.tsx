"use client";
import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

export interface Coord { lat: number; lng: number; }

export interface RegistryEntry {
  id: string;
  name: string;
  tags: string[];
  lat: number;
  lng: number;
  zone: string;
  active: boolean;
}

type LatLng = [number, number];

/** Documented default map center (system_config/geo). Only used before the geo doc loads. */
export const DEFAULT_GEO_CENTER: Coord = { lat: 6.3350, lng: 5.6037 };

export const isBeninCityCoord = (lat?: number, lng?: number): boolean => {
  if (typeof lat !== "number" || typeof lng !== "number" || isNaN(lat) || isNaN(lng)) return false;
  return lat >= 6.10 && lat <= 6.55 && lng >= 5.45 && lng <= 5.85;
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
    if (!isBeninCityCoord(e.lat, e.lng)) continue;
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
  if (typeof lat === "number" && typeof lng === "number" && isFinite(lat) && isFinite(lng)) {
    return { lat, lng };
  }
  return resolveFromRegistry(address, entries);
}

interface Props {
  deliveries: any[];
  drivers: any[];
  selectedId: string | null;
  onSelect: (id: string | null) => void;
  addressRegistry: RegistryEntry[];
  geoCenter: Coord;
}

export default function LiveTrackingMap({ deliveries, drivers, selectedId, onSelect, addressRegistry, geoCenter }: Props) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<L.Map | null>(null);
  const markersLayer = useRef<L.LayerGroup | null>(null);

  useEffect(() => {
    if (!mapRef.current || mapInstance.current) return;
    const map = L.map(mapRef.current, {
      center: [DEFAULT_GEO_CENTER.lat, DEFAULT_GEO_CENTER.lng],
      zoom: 12,
      zoomControl: true,
      attributionControl: false,
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);
    markersLayer.current = L.layerGroup().addTo(map);
    mapInstance.current = map;
    return () => { map.remove(); mapInstance.current = null; };
  }, []);

  useEffect(() => {
    const map = mapInstance.current;
    const layer = markersLayer.current;
    if (!map || !layer) return;
    layer.clearLayers();

    const pts: LatLng[] = [];

    const goldIcon = L.divIcon({
      className: "",
      html: `<div style="width:20px;height:20px;background:#FFB800;border-radius:50%;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.3);"></div>`,
      iconSize: [20, 20],
      iconAnchor: [10, 10],
    });
    const darkIcon = L.divIcon({
      className: "",
      html: `<div style="width:20px;height:20px;background:#111;border-radius:50%;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.3);"></div>`,
      iconSize: [20, 20],
      iconAnchor: [10, 10],
    });
    const riderIcon = L.divIcon({
      className: "",
      html: `<div style="width:22px;height:22px;background:#FFB800;border-radius:50%;border:3px solid #fff;box-shadow:0 0 12px rgba(255,197,66,0.6);display:flex;align-items:center;justify-content:center;font-size:10px;">&#127949;</div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11],
    });
    const selIcon = L.divIcon({
      className: "",
      html: `<div style="width:28px;height:28px;background:#FFB800;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 4px rgba(255,197,66,0.4);display:flex;align-items:center;justify-content:center;color:#111;font-weight:bold;font-size:12px;">&#128205;</div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 14],
    });

    deliveries.forEach((d) => {
      const pickup = resolveEndpoint(d.pickupLat, d.pickupLng, d.pickupAddress || "", addressRegistry);
      const delivery = resolveEndpoint(d.deliveryLat, d.deliveryLng, d.deliveryAddress || "", addressRegistry);
      const isSelected = d.id === selectedId;

      if (pickup) {
        const pLatLng: LatLng = [pickup.lat, pickup.lng];
        const marker = L.marker(pLatLng, { icon: isSelected ? selIcon : goldIcon })
          .addTo(layer)
          .bindPopup(`<b>Pickup</b><br>${d.itemName || "Parcel"}<br>${d.pickupAddress}`);
        marker.on("click", () => onSelect(d.id));
        pts.push(pLatLng);
      }
      if (delivery) {
        const dLatLng: LatLng = [delivery.lat, delivery.lng];
        const marker = L.marker(dLatLng, { icon: darkIcon })
          .addTo(layer)
          .bindPopup(`<b>Delivery</b><br>${d.receiverName}<br>${d.deliveryAddress}`);
        marker.on("click", () => onSelect(d.id));
        pts.push(dLatLng);
      }
      if (pickup && delivery) {
        L.polyline([[pickup.lat, pickup.lng], [delivery.lat, delivery.lng]], {
          color: "#FFB800",
          weight: 3,
          dashArray: "8, 8",
          opacity: isSelected ? 0.95 : 0.4,
        }).addTo(layer);
      }
    });

    drivers.forEach((r) => {
      if (typeof r.lat === "number" && typeof r.lng === "number" && isFinite(r.lat) && isFinite(r.lng)) {
        const rLatLng: LatLng = [r.lat, r.lng];
        L.marker(rLatLng, { icon: riderIcon })
          .addTo(layer)
          .bindPopup(`<b>${r.name}</b><br>${r.status || "idle"}<br>${r.deliveryCount || 0} deliveries`);
        if (isBeninCityCoord(r.lat, r.lng)) pts.push(rLatLng);
      }
    });

    // Live courier position for the selected shipment (real telemetry only)
    const selected = selectedId ? deliveries.find(d => d.id === selectedId) : null;
    if (selected && isBeninCityCoord(selected.courierLatitude, selected.courierLongitude)) {
      const cLatLng: LatLng = [selected.courierLatitude, selected.courierLongitude];
      const courierMarker = L.circleMarker(cLatLng, {
        radius: 9,
        color: "#111",
        weight: 2,
        fillColor: "#FFB800",
        fillOpacity: 1,
      })
        .addTo(layer)
        .bindTooltip("Courier", { direction: "top", permanent: false, opacity: 1 })
        .bindPopup(`<b>Courier</b><br>${selected.courierName || "Assigned rider"}<br>Live position`);
      courierMarker.on("click", () => onSelect(selected.id));
      pts.push(cLatLng);
    }

    if (pts.length > 0) {
      map.fitBounds(L.latLngBounds(pts), { padding: [50, 50], maxZoom: 14 });
    } else {
      map.setView([geoCenter.lat, geoCenter.lng], 13);
    }
  }, [deliveries, drivers, selectedId, addressRegistry, geoCenter, onSelect]);

  return <div ref={mapRef} className="w-full h-[400px] rounded-3xl overflow-hidden border border-black/10 dark:border-white/10 shadow-sm" />;
}
