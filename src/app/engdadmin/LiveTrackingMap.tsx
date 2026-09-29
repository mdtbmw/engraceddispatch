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

export interface DriverMarkerData {
  id: string;
  pos: Coord;
  name: string;
  phone?: string;
  vehicleType?: string;
  vehiclePlate?: string;
  status: string;
  isOnline: boolean;
  isOccupied: boolean;
  isDelayed: boolean;
  lastUpdateAgo?: string;
  activeDelivery?: any;
}

export default function LiveTrackingMap({ deliveries, drivers, selectedId, onSelect, addressRegistry, geoCenter }: Props) {
  const apiKey = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "";
  const [selectedDriver, setSelectedDriver] = useState<DriverMarkerData | null>(null);

  // Compute active map markers
  const { pickupMarkers, deliveryMarkers, driverMarkers, courierMarker, allPoints } = useMemo(() => {
    const pickups: { id: string; pos: Coord; title: string; desc: string; isSelected: boolean }[] = [];
    const dropoffs: { id: string; pos: Coord; title: string; desc: string; isSelected: boolean }[] = [];
    const couriers: DriverMarkerData[] = [];
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
          // Check active assignment
          const activeDelivery = deliveries.find(d =>
            (d.riderId === r.id || d.riderId === r.uid || d.assignedRiderId === r.id || d.assignedRiderId === r.uid || (r.activeBookingId && d.id === r.activeBookingId)) &&
            !["DELIVERED", "CANCELLED", "FAILED", "RETURNED"].includes(d.status)
          );
          const isOccupied = Boolean(activeDelivery);

          // Check if telemetry is delayed (> 5 mins)
          const lastUpdateMs = typeof r.lastLocationUpdate === "number"
            ? r.lastLocationUpdate
            : (r.lastLocationUpdate?.toMillis ? r.lastLocationUpdate.toMillis() : null);
          const now = Date.now();
          const diffMinutes = lastUpdateMs ? Math.floor((now - lastUpdateMs) / 60000) : null;
          const isDelayed = diffMinutes !== null ? diffMinutes >= 5 : false;
          const lastUpdateAgo = diffMinutes !== null ? (diffMinutes === 0 ? "Just now" : `${diffMinutes}m ago`) : undefined;

          couriers.push({
            id: r.id || r.uid || Math.random().toString(),
            pos: { lat: r.lat, lng: r.lng },
            name: r.name || "Driver",
            phone: r.phone || r.phoneNumber || "",
            vehicleType: r.vehicleType || "Motorcycle",
            vehiclePlate: r.vehiclePlate || "",
            status: isOccupied ? "Occupied" : (r.isOnline !== false ? "Available" : "Offline"),
            isOnline: r.isOnline !== false,
            isOccupied,
            isDelayed,
            lastUpdateAgo,
            activeDelivery
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
          {driverMarkers.map((r) => {
            const isSel = selectedDriver?.id === r.id;
            return (
              <AdvancedMarker
                key={`driver-${r.id}`}
                position={r.pos}
                onClick={() => setSelectedDriver(isSel ? null : r)}
                title={`${r.name} - ${r.isOccupied ? "On Delivery" : "Available"}${r.isDelayed ? " (Delayed GPS)" : ""}`}
              >
                <div
                  className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full shadow-xl text-[11px] font-bold cursor-pointer transition-all duration-200 select-none ${
                    isSel ? "scale-110 ring-2 ring-white z-30" : "scale-100 hover:scale-105 z-20"
                  } ${
                    r.isDelayed
                      ? "bg-[#181206] text-amber-300 border-2 border-amber-500"
                      : r.isOccupied
                      ? "bg-[#0E0E10] text-[#FFB800] border-2 border-[#FFB800]"
                      : "bg-[#061810] text-emerald-300 border-2 border-emerald-500"
                  }`}
                >
                  <span className="text-xs">
                    {r.isDelayed ? "⚠️" : r.isOccupied ? "🏍️" : "🟢"}
                  </span>
                  <span className="text-white font-extrabold">{r.name}</span>
                  <span
                    className={`text-[9px] px-1.5 py-0.5 rounded-full font-bold uppercase ${
                      r.isDelayed
                        ? "bg-amber-500/20 text-amber-300"
                        : r.isOccupied
                        ? "bg-[#FFB800]/20 text-[#FFB800]"
                        : "bg-emerald-500/20 text-emerald-300"
                    }`}
                  >
                    {r.isDelayed ? "Delayed" : r.isOccupied ? "Busy" : "Ready"}
                  </span>
                </div>
              </AdvancedMarker>
            );
          })}

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

      {/* Fleet Status Map Legend */}
      <div className="absolute top-3 right-3 bg-[#0E0E10]/90 backdrop-blur-md border border-white/10 rounded-2xl px-3 py-1.5 flex items-center gap-3 text-[10px] font-semibold text-gray-300 z-10 pointer-events-none shadow-lg">
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-emerald-400" /> Available
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#FFB800]" /> On Delivery
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-amber-500" /> Delayed GPS
        </span>
      </div>

      {/* Selected Driver Interactive Popover */}
      {selectedDriver && (
        <div className="absolute bottom-3 left-3 right-3 sm:right-auto sm:w-80 bg-[#0E0E10]/95 backdrop-blur-md border border-[#FFB800]/40 rounded-2xl p-3.5 shadow-2xl z-30 text-white animate-fade-in">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2">
              <div
                className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                  selectedDriver.isDelayed
                    ? "bg-amber-500 text-black"
                    : selectedDriver.isOccupied
                    ? "bg-[#FFB800] text-black"
                    : "bg-emerald-500 text-black"
                }`}
              >
                🏍️
              </div>
              <div>
                <h4 className="font-extrabold text-xs text-white flex items-center gap-1.5">
                  {selectedDriver.name}
                  <span
                    className={`text-[9px] px-1.5 py-0.5 rounded-full font-bold ${
                      selectedDriver.isDelayed
                        ? "bg-amber-500/20 text-amber-300 border border-amber-500/40"
                        : selectedDriver.isOccupied
                        ? "bg-[#FFB800]/20 text-[#FFB800] border border-[#FFB800]/40"
                        : "bg-emerald-500/20 text-emerald-300 border border-emerald-500/40"
                    }`}
                  >
                    {selectedDriver.isDelayed ? "GPS Delayed" : selectedDriver.isOccupied ? "On Delivery" : "Available"}
                  </span>
                </h4>
                <p className="text-[10px] text-gray-400">
                  {selectedDriver.phone ? `📞 ${selectedDriver.phone}` : "Fleet Courier"}
                  {selectedDriver.lastUpdateAgo ? ` • Signal ${selectedDriver.lastUpdateAgo}` : ""}
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={() => setSelectedDriver(null)}
              className="w-5 h-5 rounded-full bg-white/10 hover:bg-white/20 text-gray-300 flex items-center justify-center text-[10px] cursor-pointer"
            >
              ✕
            </button>
          </div>

          {selectedDriver.activeDelivery ? (
            <div className="mt-2.5 pt-2.5 border-t border-white/10">
              <div className="flex items-center justify-between text-[10px] font-bold text-gray-300 mb-0.5">
                <span>Active Assignment</span>
                <span className="text-[#FFB800] font-mono">#{selectedDriver.activeDelivery.id.slice(0, 8)}</span>
              </div>
              <p className="text-xs font-bold text-white truncate">
                {selectedDriver.activeDelivery.itemName || "Shipment"}
              </p>
              <p className="text-[10px] text-gray-400 truncate mt-0.5">
                📍 {selectedDriver.activeDelivery.deliveryAddress || "Customer Destination"}
              </p>
              <button
                type="button"
                onClick={() => {
                  onSelect(selectedDriver.activeDelivery.id);
                  setSelectedDriver(null);
                }}
                className="mt-2.5 w-full py-1.5 px-3 rounded-xl bg-[#FFB800] hover:bg-[#e6a600] active:scale-95 text-black font-extrabold text-[11px] transition-all cursor-pointer flex items-center justify-center gap-1.5 shadow-md"
              >
                <span>Open Active Assignment</span>
                <span>→</span>
              </button>
            </div>
          ) : (
            <div className="mt-2 pt-2 border-t border-white/10 text-[10px] text-gray-400">
              Courier is available and awaiting dispatch in Benin City.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
