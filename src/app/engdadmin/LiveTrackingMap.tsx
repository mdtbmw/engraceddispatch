"use client";
import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

interface Coord { lat: number; lng: number; }
type LatLng = [number, number];

const NIGERIA_CENTER: Coord = { lat: 9.082, lng: 8.6753 };

const ADDRESS_COORDS: Record<string, Coord> = {
  "ikeja": { lat: 6.6018, lng: 3.3515 },
  "victoria island": { lat: 6.4281, lng: 3.4219 },
  "lekki": { lat: 6.4308, lng: 3.4664 },
  "surulere": { lat: 6.5013, lng: 3.3586 },
  "yaba": { lat: 6.5134, lng: 3.377 },
  "ajah": { lat: 6.4667, lng: 3.6 },
  "port harcourt": { lat: 4.8158, lng: 7.0301 },
  "gwarinpa": { lat: 9.0899, lng: 7.4019 },
  "abuja": { lat: 9.0579, lng: 7.4951 },
  "wuse": { lat: 9.0609, lng: 7.4891 },
  "warri": { lat: 5.5167, lng: 5.75 },
  "benin": { lat: 6.3176, lng: 5.6145 },
  "kano": { lat: 12.0022, lng: 8.592 },
  "ibadan": { lat: 7.3775, lng: 3.947 },
  "bodija": { lat: 7.4167, lng: 3.9167 },
  "dugbe": { lat: 7.3833, lng: 3.9 },
  "enugu": { lat: 6.4403, lng: 7.504 },
  "awka": { lat: 6.2128, lng: 7.0673 },
  "aba": { lat: 5.1066, lng: 7.3667 },
  "owerri": { lat: 5.4853, lng: 7.0363 },
  "calabar": { lat: 4.9755, lng: 8.3417 },
  "uyo": { lat: 5.0333, lng: 7.9333 },
  "lagos": { lat: 6.5244, lng: 3.3792 },
  "island": { lat: 6.4281, lng: 3.4219 },
  "mainland": { lat: 6.5013, lng: 3.3586 },
  "asaba": { lat: 6.2021, lng: 6.6915 },
  "onitsha": { lat: 6.1349, lng: 6.7852 },
  "ilorin": { lat: 8.4966, lng: 4.5421 },
  "kaduna": { lat: 10.5264, lng: 7.4388 },
  "jos": { lat: 9.8965, lng: 8.8583 },
  "maiduguri": { lat: 11.8314, lng: 13.1505 },
  "sokoto": { lat: 13.0606, lng: 5.2426 },
  "yenogoa": { lat: 4.9188, lng: 6.2647 },
};

function addressToCoord(addr: string, fallback: Coord = NIGERIA_CENTER): Coord {
  const lower = addr.toLowerCase();
  for (const [key, coord] of Object.entries(ADDRESS_COORDS)) {
    if (lower.includes(key)) return coord;
  }
  return fallback;
}

function getDeliveryCoords(d: any): { pickup: Coord; delivery: Coord } {
  const pickup = d.pickupLat && d.pickupLng
    ? { lat: d.pickupLat, lng: d.pickupLng }
    : addressToCoord(d.pickupAddress || "");
  const delivery = d.deliveryLat && d.deliveryLng
    ? { lat: d.deliveryLat, lng: d.deliveryLng }
    : addressToCoord(d.deliveryAddress || "");
  return { pickup, delivery };
}

interface Props {
  deliveries: any[];
  drivers: any[];
  selectedId: string | null;
  onSelect: (id: string | null) => void;
}

export default function LiveTrackingMap({ deliveries, drivers, selectedId, onSelect }: Props) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<L.Map | null>(null);
  const markersLayer = useRef<L.LayerGroup | null>(null);

  useEffect(() => {
    if (!mapRef.current || mapInstance.current) return;
    const map = L.map(mapRef.current, {
      center: [NIGERIA_CENTER.lat, NIGERIA_CENTER.lng],
      zoom: 6,
      zoomControl: true,
      attributionControl: false,
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
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
      html: `<div style="width:20px;height:20px;background:#FFC542;border-radius:50%;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.3);"></div>`,
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
      html: `<div style="width:22px;height:22px;background:#FFC542;border-radius:50%;border:3px solid #fff;box-shadow:0 0 12px rgba(255,197,66,0.6);display:flex;align-items:center;justify-content:center;font-size:10px;">🏍</div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11],
    });
    const selIcon = L.divIcon({
      className: "",
      html: `<div style="width:28px;height:28px;background:#FFC542;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 4px rgba(255,197,66,0.4);display:flex;align-items:center;justify-content:center;color:#111;font-weight:bold;font-size:12px;">📍</div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 14],
    });

    deliveries.forEach((d) => {
      const { pickup, delivery } = getDeliveryCoords(d);
      const isSelected = d.id === selectedId;

      const pLatLng: LatLng = [pickup.lat, pickup.lng];
      const dLatLng: LatLng = [delivery.lat, delivery.lng];

      if (pickup.lat && pickup.lng) {
        L.marker(pLatLng, { icon: isSelected ? selIcon : goldIcon })
          .addTo(layer)
          .bindPopup(`<b>Pickup</b><br>${d.itemName || "Parcel"}<br>${d.pickupAddress}`);
        pts.push(pLatLng);
      }
      if (delivery.lat && delivery.lng) {
        L.marker(dLatLng, { icon: darkIcon })
          .addTo(layer)
          .bindPopup(`<b>Delivery</b><br>${d.receiverName}<br>${d.deliveryAddress}`);
        pts.push(dLatLng);
      }
      if (pickup.lat && pickup.lng && delivery.lat && delivery.lng) {
        L.polyline([pLatLng, dLatLng], {
          color: isSelected ? "#FFC542" : "#FFC54260",
          weight: isSelected ? 3 : 2,
          dashArray: isSelected ? "" : "8 6",
          opacity: isSelected ? 0.9 : 0.4,
        }).addTo(layer);
        pts.push(pLatLng, dLatLng);
      }
    });

    drivers.forEach((r) => {
      if (r.lat && r.lng) {
        const rLatLng: LatLng = [r.lat, r.lng];
        L.marker(rLatLng, { icon: riderIcon })
          .addTo(layer)
          .bindPopup(`<b>${r.name}</b><br>${r.status || "idle"}<br>${r.deliveryCount || 0} deliveries`);
        pts.push(rLatLng);
      }
    });

    if (pts.length) {
      map.fitBounds(L.latLngBounds(pts), { padding: [60, 60], maxZoom: 12 });
    }
  }, [deliveries, drivers, selectedId]);

  return <div ref={mapRef} className="w-full h-[400px] rounded-3xl overflow-hidden border border-black/10 dark:border-white/10 shadow-sm" />;
}
