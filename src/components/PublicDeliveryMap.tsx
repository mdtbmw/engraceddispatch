import React, { useEffect, useMemo, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { MapPin, Navigation, Truck } from "lucide-react";
import { DEFAULT_GEO_CENTER, isBeninCityCoord } from "@/app/engdadmin/LiveTrackingMap";

export interface PublicDeliveryCoords {
  pickupLat?: number;
  pickupLng?: number;
  deliveryLat?: number;
  deliveryLng?: number;
  courierLatitude?: number;
  courierLongitude?: number;
  pickupAddress?: string;
  deliveryAddress?: string;
  receiverName?: string;
  courierName?: string;
}

interface Props {
  delivery: PublicDeliveryCoords | null | undefined;
}

const asPoint = (lat?: number, lng?: number): { lat: number; lng: number } | null => {
  if (typeof lat !== "number" || typeof lng !== "number") return null;
  if (!isFinite(lat) || !isFinite(lng)) return null;
  if (!isBeninCityCoord(lat, lng)) return null;
  return { lat, lng };
};

const GOLD_PICKUP_ICON = L.divIcon({
  className: "",
  html: `<div style="width:22px;height:22px;background:#FFB800;border-radius:50%;border:3px solid #111;box-shadow:0 2px 8px rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;color:#111;font-weight:900;font-size:11px;">P</div>`,
  iconSize: [22, 22],
  iconAnchor: [11, 11],
});

const DARK_DELIVERY_ICON = L.divIcon({
  className: "",
  html: `<div style="width:22px;height:22px;background:#111;border-radius:50%;border:3px solid #FFB800;box-shadow:0 2px 8px rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;color:#FFB800;font-weight:900;font-size:11px;">D</div>`,
  iconSize: [22, 22],
  iconAnchor: [11, 11],
});

const COURIER_ICON = L.divIcon({
  className: "",
  html: `<div style="position:relative;width:18px;height:18px;">
      <span style="position:absolute;inset:-6px;border-radius:50%;background:rgba(255,184,0,0.28);animation:esdispatch-pulse 1.8s ease-out infinite;"></span>
      <span style="position:absolute;inset:0;border-radius:50%;background:#FFB800;border:3px solid #111;box-shadow:0 0 10px rgba(255,184,0,0.7);"></span>
    </div>`,
  iconSize: [18, 18],
  iconAnchor: [9, 9],
});

const PULSE_STYLE_ID = "esdispatch-public-map-pulse";

export default function PublicDeliveryMap({ delivery }: Props) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<L.Map | null>(null);
  const layerRef = useRef<L.LayerGroup | null>(null);

  const { pickup, destination, courier, hasPoints } = useMemo(() => {
    const p = asPoint(delivery?.pickupLat, delivery?.pickupLng);
    const d = asPoint(delivery?.deliveryLat, delivery?.deliveryLng);
    const c = asPoint(delivery?.courierLatitude, delivery?.courierLongitude);
    return { pickup: p, destination: d, courier: c, hasPoints: Boolean(p || d || c) };
  }, [
    delivery?.pickupLat,
    delivery?.pickupLng,
    delivery?.deliveryLat,
    delivery?.deliveryLng,
    delivery?.courierLatitude,
    delivery?.courierLongitude,
  ]);

  useEffect(() => {
    if (!hasPoints || !mapRef.current || mapInstance.current) return;

    if (!document.getElementById(PULSE_STYLE_ID)) {
      const style = document.createElement("style");
      style.id = PULSE_STYLE_ID;
      style.textContent = `@keyframes esdispatch-pulse{0%{transform:scale(0.6);opacity:0.9}100%{transform:scale(1.6);opacity:0}}
        .esdispatch-public-map .leaflet-control-attribution{background:rgba(10,10,10,0.78);color:#9CA3AF;font-size:9px;}
        .esdispatch-public-map .leaflet-control-attribution a{color:#FFB800;}
        .esdispatch-public-map .leaflet-bar a{background:#141414;color:#FFB800;border-color:#2E2E2E;}
        .esdispatch-public-map .leaflet-bar a:hover{background:#1F1F1F;color:#FFB800;}
        .esdispatch-public-map .leaflet-popup-content-wrapper,.esdispatch-public-map .leaflet-popup-tip{background:#141414;color:#fff;}`;
      document.head.appendChild(style);
    }

    const map = L.map(mapRef.current, {
      center: [DEFAULT_GEO_CENTER.lat, DEFAULT_GEO_CENTER.lng],
      zoom: 12,
      zoomControl: true,
      attributionControl: true,
      scrollWheelZoom: false,
    });
    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
      maxZoom: 19,
      subdomains: "abcd",
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    }).addTo(map);
    layerRef.current = L.layerGroup().addTo(map);
    mapInstance.current = map;

    return () => {
      map.remove();
      mapInstance.current = null;
      layerRef.current = null;
    };
  }, [hasPoints]);

  const fittedKeyRef = useRef<string>("");
  const deliveryRef = useRef<PublicDeliveryCoords | null | undefined>(delivery);
  deliveryRef.current = delivery;

  useEffect(() => {
    const map = mapInstance.current;
    const layer = layerRef.current;
    if (!map || !layer) return;
    layer.clearLayers();

    const pts: [number, number][] = [];
    const fitKey = `${pickup ? `${pickup.lat},${pickup.lng}` : "-"}|${destination ? `${destination.lat},${destination.lng}` : "-"}|${courier ? "c" : "-"}`;

    if (pickup) {
      const p: [number, number] = [pickup.lat, pickup.lng];
      L.marker(p, { icon: GOLD_PICKUP_ICON })
        .addTo(layer)
        .bindPopup(`<b>Pickup</b><br>${deliveryRef.current?.pickupAddress || "Pickup point"}`);
      pts.push(p);
    }
    if (destination) {
      const d: [number, number] = [destination.lat, destination.lng];
      L.marker(d, { icon: DARK_DELIVERY_ICON })
        .addTo(layer)
        .bindPopup(`<b>Delivery</b><br>${deliveryRef.current?.receiverName ? `${deliveryRef.current.receiverName}<br>` : ""}${deliveryRef.current?.deliveryAddress || "Destination"}`);
      pts.push(d);
    }
    if (pickup && destination) {
      L.polyline(
        [[pickup.lat, pickup.lng], [destination.lat, destination.lng]],
        { color: "#FFB800", weight: 3, dashArray: "8, 8", opacity: 0.9 }
      ).addTo(layer);
    }
    if (courier) {
      const c: [number, number] = [courier.lat, courier.lng];
      L.marker(c, { icon: COURIER_ICON })
        .addTo(layer)
        .bindPopup(`<b>Courier</b><br>${deliveryRef.current?.courierName || "Assigned rider"}<br>Live position`);
      pts.push(c);
    }

    if (pts.length > 0) {
      if (fittedKeyRef.current !== fitKey) {
        fittedKeyRef.current = fitKey;
        map.fitBounds(L.latLngBounds(pts), { padding: [40, 40], maxZoom: 15 });
      }
    } else {
      fittedKeyRef.current = "";
      map.setView([DEFAULT_GEO_CENTER.lat, DEFAULT_GEO_CENTER.lng], 12);
    }
  }, [pickup, destination, courier]);

  if (!hasPoints) {
    return (
      <div className="mt-6 bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 sm:p-8 text-center space-y-3">
        <div className="w-11 h-11 rounded-2xl bg-[#FFB800]/10 border border-[#FFB800]/25 flex items-center justify-center mx-auto text-[#FFB800]">
          <MapPin className="w-5 h-5" />
        </div>
        <p className="text-xs sm:text-sm text-neutral-400 leading-relaxed max-w-md mx-auto">
          Live map unavailable — location will appear once the courier is assigned.
        </p>
      </div>
    );
  }

  return (
    <div className="mt-6 bg-[#141414] border border-[#2E2E2E] rounded-2xl overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-3 px-4 sm:px-5 py-3 border-b border-[#262626]">
        <div className="flex items-center gap-2 text-[10px] font-black font-mono tracking-widest uppercase text-[#FFB800]">
          <Navigation className="w-3.5 h-3.5" />
          <span>Live Route Map</span>
        </div>
        <div className="flex flex-wrap items-center gap-3 text-[10px] font-mono text-neutral-400">
          {pickup && (
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[#FFB800]" />
              Pickup
            </span>
          )}
          {destination && (
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[#111] border border-[#FFB800]" />
              Delivery
            </span>
          )}
          {courier && (
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[#FFB800] animate-pulse" />
              Courier
            </span>
          )}
        </div>
      </div>
      <div ref={mapRef} className="esdispatch-public-map w-full h-[320px] sm:h-[360px] bg-[#0F0F0F]" />
      <div className="flex items-center gap-2 px-4 sm:px-5 py-2.5 border-t border-[#262626] text-[10px] font-mono text-neutral-500">
        <Truck className="w-3.5 h-3.5 text-[#FFB800] shrink-0" />
        <span>
          {courier
            ? "Courier position updates in real time while the shipment is in transit."
            : "Pickup and destination shown — courier position appears once a rider is assigned."}
        </span>
      </div>
    </div>
  );
}
