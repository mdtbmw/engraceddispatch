"use client";
import React, { useEffect, useMemo, useState } from "react";
import { APIProvider, Map, AdvancedMarker, Pin, useMap } from "@vis.gl/react-google-maps";

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

/** Camera bounds controller inside the Google Maps context */
function MapCameraBounds({ points, geoCenter }: { points: google.maps.LatLngLiteral[]; geoCenter: Coord }) {
  const map = useMap();

  useEffect(() => {
    if (!map) return;
    if (points.length > 1 && typeof google !== "undefined" && google.maps?.LatLngBounds) {
      const bounds = new google.maps.LatLngBounds();
      points.forEach(p => bounds.extend(p));
      map.fitBounds(bounds, 50);
    } else if (points.length === 1) {
      map.panTo(points[0]);
      map.setZoom(15);
    } else {
      map.panTo({ lat: geoCenter.lat, lng: geoCenter.lng });
      map.setZoom(13);
    }
  }, [map, points, geoCenter]);

  return null;
}

export default function LiveTrackingMap({ deliveries, drivers, selectedId, onSelect, addressRegistry, geoCenter }: Props) {
  const apiKey = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "";

  // Compute active map markers
  const { pickupMarkers, deliveryMarkers, driverMarkers, courierMarker, allPoints } = useMemo(() => {
    const pickups: { id: string; pos: Coord; title: string; desc: string; isSelected: boolean }[] = [];
    const dropoffs: { id: string; pos: Coord; title: string; desc: string; isSelected: boolean }[] = [];
    const couriers: { id: string; pos: Coord; name: string; status: string }[] = [];
    let activeCourier: { id: string; pos: Coord; name: string } | null = null;
    const pts: google.maps.LatLngLiteral[] = [];

    deliveries.forEach((d) => {
      const isSelected = d.id === selectedId;
      const pickup = resolveEndpoint(d.pickupLat, d.pickupLng, d.pickupAddress || "", addressRegistry);
      const delivery = resolveEndpoint(d.deliveryLat, d.deliveryLng, d.deliveryAddress || "", addressRegistry);

      if (pickup) {
        pickups.push({
          id: d.id,
          pos: pickup,
          title: `Pickup: ${d.itemName || "Shipment"}`,
          desc: d.pickupAddress || "",
          isSelected
        });
        pts.push(pickup);
      }
      if (delivery) {
        dropoffs.push({
          id: d.id,
          pos: delivery,
          title: `Delivery: ${d.receiverName || "Customer"}`,
          desc: d.deliveryAddress || "",
          isSelected
        });
        pts.push(delivery);
      }
    });

    drivers.forEach((r) => {
      if (typeof r.lat === "number" && typeof r.lng === "number" && isFinite(r.lat) && isFinite(r.lng)) {
        if (isBeninCityCoord(r.lat, r.lng)) {
          couriers.push({
            id: r.id || r.uid || Math.random().toString(),
            pos: { lat: r.lat, lng: r.lng },
            name: r.name || "Driver",
            status: r.status || "idle"
          });
          pts.push({ lat: r.lat, lng: r.lng });
        }
      }
    });

    // Selected courier live position
    const selected = selectedId ? deliveries.find(d => d.id === selectedId) : null;
    if (selected && isBeninCityCoord(selected.courierLatitude, selected.courierLongitude)) {
      const cPos = { lat: selected.courierLatitude, lng: selected.courierLongitude };
      activeCourier = {
        id: selected.id,
        pos: cPos,
        name: selected.courierName || "Assigned Courier"
      };
      pts.push(cPos);
    }

    return {
      pickupMarkers: pickups,
      deliveryMarkers: dropoffs,
      driverMarkers: couriers,
      courierMarker: activeCourier,
      allPoints: pts
    };
  }, [deliveries, drivers, selectedId, addressRegistry]);

  return (
    <div className="w-full h-[440px] rounded-3xl overflow-hidden border border-black/10 dark:border-white/10 shadow-sm relative bg-[#111216]">
      <APIProvider apiKey={apiKey}>
        <Map
          style={{ width: "100%", height: "100%" }}
          defaultCenter={{ lat: geoCenter.lat, lng: geoCenter.lng }}
          defaultZoom={13}
          mapId="DEMO_MAP_ID"
          internalUsageAttributionIds={["gmp_git_agentskills_v1"]}
          gestureHandling="greedy"
          disableDefaultUI={false}
        >
          <MapCameraBounds points={allPoints} geoCenter={geoCenter} />

          {/* 1. Pickup Markers */}
          {pickupMarkers.map((p) => (
            <AdvancedMarker
              key={`pickup-${p.id}`}
              position={p.pos}
              onClick={() => onSelect(p.id)}
              title={p.title}
            >
              <div className={`cursor-pointer transition-transform duration-200 ${p.isSelected ? "scale-125 z-30" : "scale-100 z-10"}`}>
                <Pin
                  background="#FFB800"
                  glyphColor="#0E0E10"
                  borderColor="#0E0E10"
                  scale={p.isSelected ? 1.2 : 0.9}
                />
              </div>
            </AdvancedMarker>
          ))}

          {/* 2. Destination Markers */}
          {deliveryMarkers.map((d) => (
            <AdvancedMarker
              key={`dropoff-${d.id}`}
              position={d.pos}
              onClick={() => onSelect(d.id)}
              title={d.title}
            >
              <div className={`cursor-pointer transition-transform duration-200 ${d.isSelected ? "scale-125 z-30" : "scale-100 z-10"}`}>
                <Pin
                  background="#0E0E10"
                  glyphColor="#FFB800"
                  borderColor="#FFB800"
                  scale={d.isSelected ? 1.2 : 0.9}
                />
              </div>
            </AdvancedMarker>
          ))}

          {/* 3. Driver Fleet Markers */}
          {driverMarkers.map((r) => (
            <AdvancedMarker
              key={`driver-${r.id}`}
              position={r.pos}
              title={`${r.name} (${r.status})`}
            >
              <div className="flex items-center gap-1.5 px-2.5 py-1 bg-[#0E0E10] text-white border border-[#FFB800] rounded-full shadow-lg text-[11px] font-semibold">
                <span className="text-sm">🏍️</span>
                <span>{r.name}</span>
              </div>
            </AdvancedMarker>
          ))}

          {/* 4. Active Selected Courier Live Position */}
          {courierMarker && (
            <AdvancedMarker
              position={courierMarker.pos}
              title={`Live: ${courierMarker.name}`}
            >
              <div className="relative flex items-center justify-center">
                <div className="absolute w-8 h-8 rounded-full bg-[#FFB800]/40 animate-ping" />
                <div className="w-5 h-5 rounded-full bg-[#FFB800] border-2 border-[#0E0E10] shadow-md z-10" />
              </div>
            </AdvancedMarker>
          )}
        </Map>
      </APIProvider>
    </div>
  );
}
