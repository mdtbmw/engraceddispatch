"use client";
import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

interface Coord { lat: number; lng: number; }
type LatLng = [number, number];

const BENIN_CITY_CENTER: Coord = { lat: 6.3350, lng: 5.6275 };

const ADDRESS_COORDS: Record<string, Coord> = {
  // Benin City Core & Landmarks
  "ring road": { lat: 6.3350, lng: 5.6275 },
  "king's square": { lat: 6.3350, lng: 5.6275 },
  "kings square": { lat: 6.3350, lng: 5.6275 },
  "oba market": { lat: 6.3365, lng: 5.6260 },
  "oba palace": { lat: 6.3325, lng: 5.6240 },
  "ugbowo": { lat: 6.3980, lng: 5.6120 },
  "uniben": { lat: 6.4020, lng: 5.6140 },
  "ubth": { lat: 6.3910, lng: 5.6105 },
  "gra": { lat: 6.3150, lng: 5.6180 },
  "boundary road": { lat: 6.3120, lng: 5.6190 },
  "ihama": { lat: 6.3180, lng: 5.6160 },
  "airport road": { lat: 6.3080, lng: 5.5980 },
  "airport": { lat: 6.3170, lng: 5.5995 },
  "ikpoba hill": { lat: 6.3520, lng: 5.6550 },
  "ramat park": { lat: 6.3540, lng: 5.6580 },
  "sapele road": { lat: 6.3000, lng: 5.6350 },
  "country home": { lat: 6.2950, lng: 5.6380 },
  "new benin": { lat: 6.3450, lng: 5.6310 },
  "uselu": { lat: 6.3680, lng: 5.6150 },
  "ekenwan": { lat: 6.3200, lng: 5.5800 },
  "siluko": { lat: 6.3580, lng: 5.5950 },
  "aduwawa": { lat: 6.3750, lng: 5.6700 },
  "upper sakponba": { lat: 6.3150, lng: 5.6550 },
  "st saviour": { lat: 6.3050, lng: 5.6600 },
  "ugbor": { lat: 6.2850, lng: 5.6150 },
  "etete": { lat: 6.2980, lng: 5.6180 },
  "textile mill": { lat: 6.3620, lng: 5.6020 },
  "benin": { lat: 6.3350, lng: 5.6275 },

  // Regional & Interstate Hubs
  "warri": { lat: 5.5167, lng: 5.75 },
  "asaba": { lat: 6.2021, lng: 6.6915 },
  "onitsha": { lat: 6.1349, lng: 6.7852 },
  "lagos": { lat: 6.5244, lng: 3.3792 },
  "ikeja": { lat: 6.6018, lng: 3.3515 },
  "abuja": { lat: 9.0579, lng: 7.4951 },
  "port harcourt": { lat: 4.8158, lng: 7.0301 },
};

function addressToCoord(addr: string, fallback: Coord = BENIN_CITY_CENTER): Coord {
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
      center: [BENIN_CITY_CENTER.lat, BENIN_CITY_CENTER.lng],
      zoom: 12,
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
      html: `<div style="width:22px;height:22px;background:#FFB800;border-radius:50%;border:3px solid #fff;box-shadow:0 0 12px rgba(255,197,66,0.6);display:flex;align-items:center;justify-content:center;font-size:10px;">🏍</div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11],
    });
    const selIcon = L.divIcon({
      className: "",
      html: `<div style="width:28px;height:28px;background:#FFB800;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 4px rgba(255,197,66,0.4);display:flex;align-items:center;justify-content:center;color:#111;font-weight:bold;font-size:12px;">📍</div>`,
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
          color: isSelected ? "#FFB800" : "#FFB80060",
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
