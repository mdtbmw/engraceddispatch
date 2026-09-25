import React, { useState, useEffect, useRef, useMemo } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import {
  X,
  MapPin,
  User,
  Phone,
  Package,
  ShieldCheck,
  Check,
  Search,
  Navigation,
  Truck,
  ArrowRight,
  ArrowLeft,
  AlertTriangle,
  DollarSign,
  Layers,
  Sparkles,
  Clock,
  Bike,
  ShieldAlert,
  Info
} from "lucide-react";
import { collection, addDoc, updateDoc, setDoc, doc, Timestamp } from "firebase/firestore";

interface Coord {
  lat: number;
  lng: number;
}

const BENIN_CITY_CENTER: Coord = { lat: 6.335, lng: 5.6037 };

const BENIN_LANDMARKS: { name: string; address: string; coord: Coord; keywords: string[] }[] = [
  { name: "University of Benin Teaching Hospital (UBTH)", address: "UBTH, Ugbowo Lagos Road, Benin City", coord: { lat: 6.3982, lng: 5.6111 }, keywords: ["ubth", "teaching hospital", "ugbowo hospital"] },
  { name: "University of Benin (Main Campus)", address: "UNIBEN Ugbowo Campus, Benin City", coord: { lat: 6.4024, lng: 5.6166 }, keywords: ["uniben", "ugbowo campus", "university of benin"] },
  { name: "University of Benin (Ekehuan Campus)", address: "UNIBEN Ekehuan Campus, Ekenwan Road, Benin City", coord: { lat: 6.321, lng: 5.584 }, keywords: ["uniben ekehuan", "ekehuan campus", "uniben law"] },
  { name: "King's Square / Ring Road", address: "King's Square (Ring Road), City Center, Benin City", coord: { lat: 6.335, lng: 5.6037 }, keywords: ["ring road", "kings square", "king square", "oba market"] },
  { name: "Oba of Benin Palace", address: "Oba Market Road, King's Square, Benin City", coord: { lat: 6.3325, lng: 5.601 }, keywords: ["oba palace", "palace", "oba of benin"] },
  { name: "Benin Airport (BNI)", address: "Airport Road, GRA, Benin City", coord: { lat: 6.317, lng: 5.5995 }, keywords: ["airport", "benin airport", "air port"] },
  { name: "GRA (Government Reserved Area)", address: "GRA, Benin City", coord: { lat: 6.315, lng: 5.618 }, keywords: ["gra", "boundary road", "ihama", "adesuwa"] },
  { name: "Ihama Road GRA", address: "Ihama Road, GRA, Benin City", coord: { lat: 6.318, lng: 5.616 }, keywords: ["ihama", "ihama road"] },
  { name: "Boundary Road GRA", address: "Boundary Road, GRA, Benin City", coord: { lat: 6.312, lng: 5.619 }, keywords: ["boundary", "boundary road"] },
  { name: "Ramat Park", address: "Ramat Park, Ikpoba Hill, Benin City", coord: { lat: 6.354, lng: 5.658 }, keywords: ["ramat park", "ikpoba hill", "ramat"] },
  { name: "Sapele Road", address: "Sapele Road, Benin City", coord: { lat: 6.3, lng: 5.635 }, keywords: ["sapele road", "sapele rd", "limit road"] },
  { name: "Country Home Motel / Road", address: "Country Home Road, Off Sapele Road, Benin City", coord: { lat: 6.295, lng: 5.638 }, keywords: ["country home", "country home road"] },
  { name: "New Benin Market", address: "New Benin Market, New Lagos Road, Benin City", coord: { lat: 6.345, lng: 5.631 }, keywords: ["new benin", "new benin market"] },
  { name: "Uselu Shell / Market", address: "Uselu Lagos Road, Benin City", coord: { lat: 6.368, lng: 5.615 }, keywords: ["uselu", "uselu market", "uselu shell"] },
  { name: "Ekenwan Road / Barracks", address: "Ekenwan Road, Benin City", coord: { lat: 6.32, lng: 5.58 }, keywords: ["ekenwan", "ekenwan road", "ekenwan barracks"] },
  { name: "Siluko Road / Oliha", address: "Siluko Road, Oliha, Benin City", coord: { lat: 6.358, lng: 5.595 }, keywords: ["siluko", "siluko road", "oliha market"] },
  { name: "Aduwawa / Eyaen", address: "Aduwawa, Benin City", coord: { lat: 6.375, lng: 5.67 }, keywords: ["aduwawa", "eyaen", "bypass"] },
  { name: "Upper Sakponba Road", address: "Upper Sakponba Road, Benin City", coord: { lat: 6.315, lng: 5.655 }, keywords: ["upper sakponba", "sakponba"] },
  { name: "St. Saviour Road", address: "St. Saviour Road, Upper Sakponba, Benin City", coord: { lat: 6.305, lng: 5.66 }, keywords: ["st saviour", "saint saviour"] },
  { name: "Ugbor Road", address: "Ugbor Road, GRA, Benin City", coord: { lat: 6.285, lng: 5.615 }, keywords: ["ugbor", "ugbor road"] },
  { name: "Etete Road", address: "Etete Road, GRA, Benin City", coord: { lat: 6.298, lng: 5.618 }, keywords: ["etete", "etete road"] },
  { name: "Textile Mill Road", address: "Textile Mill Road, Benin City", coord: { lat: 6.362, lng: 5.602 }, keywords: ["textile mill", "textile mill road"] },
  { name: "Mission Road", address: "Mission Road, Benin City", coord: { lat: 6.339, lng: 5.608 }, keywords: ["mission road", "mission rd"] },
  { name: "Akpakpava Road", address: "Akpakpava Road, Benin City", coord: { lat: 6.338, lng: 5.614 }, keywords: ["akpakpava", "akpakpava road"] },
  { name: "Edo State Secretariat", address: "Sapele Road, Benin City", coord: { lat: 6.328, lng: 5.626 }, keywords: ["secretariat", "palm house"] },
  { name: "University of Benin Teaching Hospital Dental", address: "UBTH Complex, Benin City", coord: { lat: 6.392, lng: 5.6105 }, keywords: ["ubth dental", "ubth clinic"] }
];

function haversineDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371;
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 10) / 10;
}

export interface AdminDispatchBookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  db: any;
  drivers: any[];
  users: any[];
  addLog: (action: string, details: string) => Promise<void> | void;
  addToast?: (type: "success" | "error" | "info", message: string) => void;
  onSuccess?: (deliveryId: string) => void;
}

export const AdminDispatchBookingModal: React.FC<AdminDispatchBookingModalProps> = ({
  isOpen,
  onClose,
  db,
  drivers = [],
  users = [],
  addLog,
  addToast,
  onSuccess
}) => {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form State
  const [category, setCategory] = useState("Standard");
  const [itemName, setItemName] = useState("");
  const [weight, setWeight] = useState(1.5);
  const [quantity, setQuantity] = useState(1);
  const [declaredValue, setDeclaredValue] = useState("");
  const [notes, setNotes] = useState("");

  // Customer / Routing State
  const [selectedCustomerId, setSelectedCustomerId] = useState("");
  const [senderName, setSenderName] = useState("");
  const [senderPhone, setSenderPhone] = useState("");
  const [pickupAddress, setPickupAddress] = useState("");
  const [pickupCoord, setPickupCoord] = useState<Coord>({ lat: 6.335, lng: 5.6037 });

  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [deliveryCoord, setDeliveryCoord] = useState<Coord>({ lat: 6.3982, lng: 5.6111 });

  // Autocomplete dropdown state
  const [pickupQuery, setPickupQuery] = useState("");
  const [pickupSuggestions, setPickupSuggestions] = useState<any[]>([]);
  const [isSearchingPickup, setIsSearchingPickup] = useState(false);
  const [showPickupDropdown, setShowPickupDropdown] = useState(false);

  const [deliveryQuery, setDeliveryQuery] = useState("");
  const [deliverySuggestions, setDeliverySuggestions] = useState<any[]>([]);
  const [isSearchingDelivery, setIsSearchingDelivery] = useState(false);
  const [showDeliveryDropdown, setShowDeliveryDropdown] = useState(false);

  // Assignment & Pricing State
  const [assignmentMode, setAssignmentMode] = useState<"auto" | "manual" | "pool">("auto");
  const [selectedRiderId, setSelectedRiderId] = useState("");
  const [customPriceOverride, setCustomPriceOverride] = useState<number | null>(null);

  // Map Refs
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const leafletMapRef = useRef<L.Map | null>(null);
  const pickupMarkerRef = useRef<L.Marker | null>(null);
  const deliveryMarkerRef = useRef<L.Marker | null>(null);
  const routePolylineRef = useRef<L.Polyline | null>(null);

  // Calculate distance
  const calculatedDistanceKm = useMemo(() => {
    return haversineDistanceKm(pickupCoord.lat, pickupCoord.lng, deliveryCoord.lat, deliveryCoord.lng);
  }, [pickupCoord, deliveryCoord]);

  // Standard rates by category
  const suggestedFare = useMemo(() => {
    const km = Math.max(1, calculatedDistanceKm);
    let base = 1500;
    let ratePerKm = 200;

    switch (category) {
      case "Express":
        base = 2000;
        ratePerKm = 250;
        break;
      case "Economy":
        base = 1000;
        ratePerKm = 150;
        break;
      case "Batch":
        base = 3500;
        ratePerKm = 220;
        break;
      case "Cold Chain":
        base = 3000;
        ratePerKm = 300;
        break;
      case "Fragile / High-Value":
        base = 2500;
        ratePerKm = 260;
        break;
      default:
        base = 1500;
        ratePerKm = 200;
    }

    const calculated = base + Math.round(km * ratePerKm);
    return Math.max(calculated, base);
  }, [category, calculatedDistanceKm]);

  const finalFare = customPriceOverride !== null ? customPriceOverride : suggestedFare;

  // Filter online active drivers
  const onlineDrivers = useMemo(() => {
    return (drivers || []).filter(d => {
      const isOnline = d.isOnline || (d.status && d.status.toLowerCase() === "online");
      const notDeleted = !d.isDeleted && d.status !== "suspended" && d.status !== "disabled";
      return isOnline && notDeleted;
    });
  }, [drivers]);

  // Find nearest rider
  const nearestDriver = useMemo(() => {
    if (onlineDrivers.length === 0) return null;
    let closest = onlineDrivers[0];
    let minD = Infinity;

    for (const d of onlineDrivers) {
      const dLat = d.lat || d.latitude || 6.335;
      const dLng = d.lng || d.longitude || 5.6037;
      const dist = haversineDistanceKm(pickupCoord.lat, pickupCoord.lng, dLat, dLng);
      if (dist < minD) {
        minD = dist;
        closest = { ...d, distanceToPickup: dist };
      }
    }
    return closest;
  }, [onlineDrivers, pickupCoord]);

  // Handle Customer Selection
  const handleSelectCustomer = (uid: string) => {
    setSelectedCustomerId(uid);
    if (!uid) {
      setSenderName("");
      setSenderPhone("");
      return;
    }
    const found = users.find(u => u.id === uid || u.uid === uid);
    if (found) {
      setSenderName(found.name || found.displayName || found.email || "");
      setSenderPhone(found.phone || "");
    }
  };

  // In-Memory Autocomplete Cache & Debounce Timer to prevent billing runaway
  const searchDebounceRef = useRef<any>(null);
  const placesCacheRef = useRef<Map<string, any[]>>(new Map());

  // Landmark Local & Google Places Search for Pickup
  const searchAddress = (q: string, isPickup: boolean) => {
    const term = q.trim().toLowerCase();
    if (!term) {
      if (isPickup) setPickupSuggestions([]);
      else setDeliverySuggestions([]);
      return;
    }

    // 1. Instant Landmark Match
    const localMatches = BENIN_LANDMARKS.filter(lm => {
      return (
        lm.name.toLowerCase().includes(term) ||
        lm.address.toLowerCase().includes(term) ||
        lm.keywords.some(k => k.includes(term) || term.includes(k))
      );
    }).map(lm => ({
      title: lm.name,
      address: lm.address,
      coord: lm.coord,
      source: "landmark"
    }));

    if (isPickup) {
      setPickupSuggestions(localMatches);
      setIsSearchingPickup(true);
    } else {
      setDeliverySuggestions(localMatches);
      setIsSearchingDelivery(true);
    }

    // 2. If already in memory cache, serve immediately with zero network overhead
    if (placesCacheRef.current.has(term)) {
      const cached = placesCacheRef.current.get(term) || [];
      const combined = [...localMatches, ...cached].slice(0, 7);
      if (isPickup) setPickupSuggestions(combined);
      else setDeliverySuggestions(combined);
      return;
    }

    // Skip network queries for short inputs (< 3 chars)
    if (term.length < 3) return;

    // 3. Debounce external Google Places API call by 350ms to stop runaway billing
    if (searchDebounceRef.current) {
      clearTimeout(searchDebounceRef.current);
    }

    if (isPickup) setIsSearchingPickup(true);
    else setIsSearchingDelivery(true);

    searchDebounceRef.current = setTimeout(async () => {
      try {
        const apiKey = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "AIzaSyCnYpvx0peHOafunoZcPMIIhd7Y-pM0NAs";
        const url = `https://maps.googleapis.com/maps/api/place/autocomplete/json?input=${encodeURIComponent(
          q
        )}&location=6.3350,5.6037&radius=30000&components=country:ng&key=${apiKey}`;

        const res = await fetch(url);
        if (res.ok) {
          const data = await res.json();
          if (data.predictions && data.predictions.length > 0) {
            const apiMatches = data.predictions.slice(0, 5).map((p: any) => ({
              title: p.structured_formatting?.main_text || p.description,
              address: p.description,
              placeId: p.place_id,
              source: "google"
            }));

            // Store in memory cache (capped at 200 items)
            if (placesCacheRef.current.size > 200) {
              const firstKey = placesCacheRef.current.keys().next().value;
              if (firstKey) placesCacheRef.current.delete(firstKey);
            }
            placesCacheRef.current.set(term, apiMatches);

            const combined = [...localMatches, ...apiMatches].slice(0, 7);
            if (isPickup) setPickupSuggestions(combined);
            else setDeliverySuggestions(combined);
          }
        }
      } catch {
        // Fallback gracefully to local matches
      } finally {
        if (isPickup) setIsSearchingPickup(false);
        else setIsSearchingDelivery(false);
      }
    }, 350);
  };

  // Initialize Leaflet Map on step 2
  useEffect(() => {
    if (step !== 2 || !mapContainerRef.current) return;

    if (!leafletMapRef.current) {
      const map = L.map(mapContainerRef.current, {
        center: [BENIN_CITY_CENTER.lat, BENIN_CITY_CENTER.lng],
        zoom: 12,
        zoomControl: true,
        attributionControl: false
      });

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19
      }).addTo(map);

      // Custom Marker Icons
      const goldIcon = L.divIcon({
        className: "custom-div-icon",
        html: `<div style="background-color: #FFB800; width: 26px; height: 26px; border-radius: 50%; border: 3px solid #111; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 10px rgba(0,0,0,0.5);"><span style="color: #111; font-weight: 900; font-size: 11px;">P</span></div>`,
        iconSize: [26, 26],
        iconAnchor: [13, 13]
      });

      const emeraldIcon = L.divIcon({
        className: "custom-div-icon",
        html: `<div style="background-color: #10B981; width: 26px; height: 26px; border-radius: 50%; border: 3px solid #111; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 10px rgba(0,0,0,0.5);"><span style="color: #fff; font-weight: 900; font-size: 11px;">D</span></div>`,
        iconSize: [26, 26],
        iconAnchor: [13, 13]
      });

      pickupMarkerRef.current = L.marker([pickupCoord.lat, pickupCoord.lng], { icon: goldIcon, draggable: true })
        .addTo(map)
        .bindPopup("<b>Pickup Location</b>");

      pickupMarkerRef.current.on("dragend", (e: any) => {
        const pos = e.target.getLatLng();
        setPickupCoord({ lat: pos.lat, lng: pos.lng });
      });

      deliveryMarkerRef.current = L.marker([deliveryCoord.lat, deliveryCoord.lng], { icon: emeraldIcon, draggable: true })
        .addTo(map)
        .bindPopup("<b>Delivery Destination</b>");

      deliveryMarkerRef.current.on("dragend", (e: any) => {
        const pos = e.target.getLatLng();
        setDeliveryCoord({ lat: pos.lat, lng: pos.lng });
      });

      routePolylineRef.current = L.polyline(
        [
          [pickupCoord.lat, pickupCoord.lng],
          [deliveryCoord.lat, deliveryCoord.lng]
        ],
        { color: "#FFB800", weight: 4, opacity: 0.8, dashArray: "6, 8" }
      ).addTo(map);

      leafletMapRef.current = map;
    }

    return () => {
      // Map preserved or cleaned
    };
  }, [step]);

  // Update map markers when coordinates change
  useEffect(() => {
    if (!leafletMapRef.current) return;

    if (pickupMarkerRef.current) {
      pickupMarkerRef.current.setLatLng([pickupCoord.lat, pickupCoord.lng]);
    }
    if (deliveryMarkerRef.current) {
      deliveryMarkerRef.current.setLatLng([deliveryCoord.lat, deliveryCoord.lng]);
    }
    if (routePolylineRef.current) {
      routePolylineRef.current.setLatLngs([
        [pickupCoord.lat, pickupCoord.lng],
        [deliveryCoord.lat, deliveryCoord.lng]
      ]);
    }

    const bounds = L.latLngBounds([
      [pickupCoord.lat, pickupCoord.lng],
      [deliveryCoord.lat, deliveryCoord.lng]
    ]);
    leafletMapRef.current.fitBounds(bounds, { padding: [50, 50], maxZoom: 15 });
  }, [pickupCoord, deliveryCoord]);

  // Final Dispatch Submission
  const handleSubmitBooking = async () => {
    if (!itemName.trim()) {
      addToast?.("error", "Please enter consignment item name.");
      setStep(1);
      return;
    }
    if (!senderName.trim() || !senderPhone.trim() || !pickupAddress.trim()) {
      addToast?.("error", "Please provide complete sender pickup details.");
      setStep(2);
      return;
    }
    if (!receiverName.trim() || !receiverPhone.trim() || !deliveryAddress.trim()) {
      addToast?.("error", "Please provide complete receiver delivery details.");
      setStep(2);
      return;
    }

    setIsSubmitting(true);
    try {
      // Generate 4-digit Handover PIN
      const otpCode = Math.floor(1000 + Math.random() * 9000).toString();

      let assignedDriver: any = null;
      let finalStatus = "PENDING";

      if (assignmentMode === "auto" && nearestDriver) {
        assignedDriver = nearestDriver;
        finalStatus = "ASSIGNED";
      } else if (assignmentMode === "manual" && selectedRiderId) {
        assignedDriver = drivers.find(d => d.id === selectedRiderId || d.uid === selectedRiderId);
        if (assignedDriver) finalStatus = "ASSIGNED";
      }

      const deliveryPayload = {
        category,
        itemName: itemName.trim(),
        weight: Number(weight) || 1.0,
        quantity: Number(quantity) || 1,
        declaredValue: declaredValue ? Number(declaredValue) : 0,
        notes: notes.trim(),
        userId: selectedCustomerId || "",
        senderName: senderName.trim(),
        senderPhone: senderPhone.trim(),
        pickupAddress: pickupAddress.trim(),
        pickupLat: pickupCoord.lat,
        pickupLng: pickupCoord.lng,
        receiverName: receiverName.trim(),
        receiverPhone: receiverPhone.trim(),
        deliveryAddress: deliveryAddress.trim(),
        deliveryLat: deliveryCoord.lat,
        deliveryLng: deliveryCoord.lng,
        distanceKm: calculatedDistanceKm,
        price: finalFare,
        deliveryFee: finalFare,
        tipAmount: 0,
        otpCode,
        status: finalStatus,
        paymentStatus: "PAID",
        bookingSource: "admin_dispatch_tool",
        riderId: assignedDriver ? assignedDriver.id : "",
        driverId: assignedDriver ? assignedDriver.id : "",
        courierName: assignedDriver ? (assignedDriver.name || assignedDriver.displayName || "Assigned Courier") : "Unassigned",
        courierPhone: assignedDriver ? (assignedDriver.phone || "") : "",
        riderBikeNumber: assignedDriver ? (assignedDriver.bikeNumber || "") : "",
        dateString: new Date().toISOString().slice(0, 10),
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now()
      };

      const docRef = await addDoc(collection(db, "deliveries"), deliveryPayload);
      await updateDoc(docRef, { id: docRef.id });

      // Customer Notification
      if (selectedCustomerId) {
        try {
          const custNotif = doc(collection(db, "users", selectedCustomerId, "notifications"));
          await setDoc(custNotif, {
            id: custNotif.id,
            title: "Shipment Dispatched",
            message: `Dispatch #${docRef.id.slice(0, 8).toUpperCase()} (${itemName}) booked by Smiles Dispatch team. Your handover PIN is ${otpCode}.`,
            parcelId: docRef.id,
            otpCode,
            isRead: false,
            createdAt: Timestamp.now()
          });
        } catch {
          // Non-blocking notification
        }
      }

      // Rider Notification
      if (assignedDriver && assignedDriver.id) {
        try {
          const riderNotif = doc(collection(db, "users", assignedDriver.id, "notifications"));
          await setDoc(riderNotif, {
            id: riderNotif.id,
            title: "New Dispatch Assigned",
            message: `New pickup: ${itemName} at ${pickupAddress}. Recipient: ${receiverName} (${receiverPhone}).`,
            parcelId: docRef.id,
            isRead: false,
            createdAt: Timestamp.now()
          });
        } catch {
          // Non-blocking notification
        }
      }

      await addLog(
        "Admin Dispatch Booking",
        `Created delivery #${docRef.id.slice(0, 8).toUpperCase()} (${itemName}) for ${senderName} -> ${receiverName}. Status: ${finalStatus}`
      );

      addToast?.("success", `Shipment #${docRef.id.slice(0, 8).toUpperCase()} booked successfully! Handover PIN: ${otpCode}`);
      onSuccess?.(docRef.id);
      onClose();
    } catch (e: any) {
      addToast?.("error", `Failed to book dispatch: ${e?.message || "Unknown error"}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-3 sm:p-5 z-50 overflow-y-auto"
      onClick={onClose}
    >
      <div
        className="bg-white dark:bg-[#161616] border border-black/10 dark:border-white/10 rounded-3xl w-full max-w-4xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh] animate-scale-in"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="bg-[#FFB800] text-[#111] px-6 py-4 flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#111] text-[#FFB800] flex items-center justify-center font-black">
              <Truck size={20} />
            </div>
            <div>
              <h2 className="text-base font-black uppercase tracking-wider">Book Dispatch For Customer</h2>
              <p className="text-[11px] font-bold opacity-80">
                Smiles Dispatch Logistics & Fleet Dispatch Center • Multi-Step Parity Booking
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-[#111]/10 hover:bg-[#111]/20 flex items-center justify-center text-[#111] transition-colors cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Stepper Progress Indicator */}
        <div className="bg-gray-100 dark:bg-[#1F1F1F] px-6 py-3 border-b border-black/5 dark:border-white/5 flex items-center justify-between text-xs font-bold">
          <div className="flex items-center gap-2">
            <div
              className={`w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-black ${
                step >= 1 ? "bg-[#FFB800] text-[#111]" : "bg-gray-300 dark:bg-white/10 text-gray-500"
              }`}
            >
              1
            </div>
            <span className={step >= 1 ? "text-gray-900 dark:text-white font-black" : "text-gray-400"}>
              Consignment & Service
            </span>
          </div>

          <div className="w-8 h-0.5 bg-gray-300 dark:bg-white/10" />

          <div className="flex items-center gap-2">
            <div
              className={`w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-black ${
                step >= 2 ? "bg-[#FFB800] text-[#111]" : "bg-gray-300 dark:bg-white/10 text-gray-500"
              }`}
            >
              2
            </div>
            <span className={step >= 2 ? "text-gray-900 dark:text-white font-black" : "text-gray-400"}>
              Customer & Interactive Route
            </span>
          </div>

          <div className="w-8 h-0.5 bg-gray-300 dark:bg-white/10" />

          <div className="flex items-center gap-2">
            <div
              className={`w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-black ${
                step >= 3 ? "bg-[#FFB800] text-[#111]" : "bg-gray-300 dark:bg-white/10 text-gray-500"
              }`}
            >
              3
            </div>
            <span className={step >= 3 ? "text-gray-900 dark:text-white font-black" : "text-gray-400"}>
              Rider Assignment & Dispatch
            </span>
          </div>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          {/* STEP 1: CONSIGNMENT & SERVICE */}
          {step === 1 && (
            <div className="space-y-5 animate-fade-in">
              <div>
                <label className="block text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300 mb-2">
                  Select Dispatch Service Tier *
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {[
                    { id: "Express", title: "Express Dispatch", desc: "Priority direct transit, immediate courier match", icon: Sparkles },
                    { id: "Standard", title: "Standard Delivery", desc: "Same-day scheduled citywide transit", icon: Clock },
                    { id: "Economy", title: "Economy Parcel", desc: "Cost-optimized grouped logistics", icon: DollarSign },
                    { id: "Batch", title: "Batch Logistics", desc: "Merchant multi-drop consignment", icon: Layers },
                    { id: "Cold Chain", title: "Cold Chain Secure", desc: "Insulated temperature control", icon: ShieldCheck },
                    { id: "Fragile / High-Value", title: "Fragile / High-Value", desc: "Special packaging with sealed verification", icon: ShieldAlert }
                  ].map(tier => {
                    const isSelected = category === tier.id;
                    const IconComp = tier.icon;
                    return (
                      <div
                        key={tier.id}
                        onClick={() => setCategory(tier.id)}
                        className={`p-3.5 rounded-2xl border-2 cursor-pointer transition-all ${
                          isSelected
                            ? "border-[#FFB800] bg-[#FFB800]/10 dark:bg-[#FFB800]/5 shadow-sm"
                            : "border-gray-200 dark:border-white/10 hover:border-gray-300 dark:hover:border-white/20 bg-gray-50 dark:bg-[#1e1e1e]"
                        }`}
                      >
                        <div className="flex items-center justify-between mb-1.5">
                          <IconComp size={18} className={isSelected ? "text-[#FFB800]" : "text-gray-400"} />
                          {isSelected && <Check size={16} className="text-[#FFB800]" />}
                        </div>
                        <h4 className="text-xs font-black text-gray-900 dark:text-white">{tier.title}</h4>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5 leading-snug">{tier.desc}</p>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Item Details */}
              <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-black uppercase text-gray-700 dark:text-gray-300 mb-1">
                      Consignment Item Description / Name *
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Legal Documents, Wedding Cake, Spare Engine Part"
                      value={itemName}
                      onChange={e => setItemName(e.target.value)}
                      className="w-full h-11 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-black uppercase text-gray-700 dark:text-gray-300 mb-1">
                      Declared Item Value (₦)
                    </label>
                    <input
                      type="number"
                      placeholder="e.g. 50000"
                      value={declaredValue}
                      onChange={e => setDeclaredValue(e.target.value)}
                      className="w-full h-11 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-bold outline-none focus:ring-2 focus:ring-[#FFB800]"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-black uppercase text-gray-700 dark:text-gray-300 mb-1">
                      Weight (kg)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      min="0.1"
                      value={weight}
                      onChange={e => setWeight(Number(e.target.value))}
                      className="w-full h-11 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-bold outline-none focus:ring-2 focus:ring-[#FFB800]"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-black uppercase text-gray-700 dark:text-gray-300 mb-1">
                      Quantity of Items
                    </label>
                    <input
                      type="number"
                      min="1"
                      value={quantity}
                      onChange={e => setQuantity(Number(e.target.value))}
                      className="w-full h-11 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-bold outline-none focus:ring-2 focus:ring-[#FFB800]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-black uppercase text-gray-700 dark:text-gray-300 mb-1">
                    Special Handling Instructions / Notes (Optional)
                  </label>
                  <textarea
                    rows={2}
                    placeholder="e.g. Call sender before reaching gate, keep upright, fragile glassware"
                    value={notes}
                    onChange={e => setNotes(e.target.value)}
                    className="w-full bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl p-3 text-xs text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-[#FFB800]"
                  />
                </div>
              </div>

              {/* Packaging Disclaimer Banner */}
              <div className="bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/40 rounded-2xl p-3.5 flex items-start gap-3">
                <Info size={18} className="text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
                <p className="text-[11px] font-semibold text-amber-900 dark:text-amber-200 leading-relaxed">
                  <span className="font-bold">Dispatch Compliance Notice:</span> Smiles Dispatch riders inspect parcel contents prior to sealing and pickup for safety, cargo integrity, and transportation compliance.
                </p>
              </div>
            </div>
          )}

          {/* STEP 2: CUSTOMER & ROUTE WITH INTERACTIVE MAP */}
          {step === 2 && (
            <div className="space-y-5 animate-fade-in">
              {/* Customer Link / Walk-in */}
              <div>
                <label className="block text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300 mb-1.5">
                  Sender Account (Customer Call-in / Walk-in)
                </label>
                <select
                  value={selectedCustomerId}
                  onChange={e => handleSelectCustomer(e.target.value)}
                  className="w-full h-11 bg-gray-50 dark:bg-[#1E1E1E] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-semibold outline-none focus:ring-2 focus:ring-[#FFB800]"
                >
                  <option value="">Walk-in / Unregistered Caller</option>
                  {(users || [])
                    .filter(u => u.role !== "driver" && u.role !== "rider")
                    .map(u => (
                      <option key={u.id || u.uid} value={u.id || u.uid}>
                        {u.name || u.displayName || u.email} {u.phone ? ` • ${u.phone}` : ""}
                      </option>
                    ))}
                </select>
              </div>

              {/* Sender & Receiver Info */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Sender Pickup Section */}
                <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-3">
                  <div className="flex items-center gap-2 text-xs font-black text-gray-900 dark:text-white uppercase tracking-wider">
                    <div className="w-6 h-6 rounded-lg bg-[#FFB800] text-[#111] flex items-center justify-center font-black">
                      <MapPin size={13} />
                    </div>
                    <span>1. Pickup Details (Sender)</span>
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Sender Full Name *</label>
                    <input
                      type="text"
                      placeholder="e.g. Osas Osayande"
                      value={senderName}
                      onChange={e => setSenderName(e.target.value)}
                      className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Sender Phone *</label>
                    <input
                      type="tel"
                      placeholder="080XXXXXXXX"
                      value={senderPhone}
                      onChange={e => setSenderPhone(e.target.value)}
                      className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                      required
                    />
                  </div>

                  {/* Pickup Address with Intelligent Autocomplete */}
                  <div className="relative">
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">
                      Pickup Address in Benin City *
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        placeholder="Search landmark (e.g. UBTH, UNIBEN, King's Square, GRA)"
                        value={pickupAddress}
                        onChange={e => {
                          setPickupAddress(e.target.value);
                          setPickupQuery(e.target.value);
                          searchAddress(e.target.value, true);
                          setShowPickupDropdown(true);
                        }}
                        onFocus={() => {
                          if (pickupSuggestions.length > 0) setShowPickupDropdown(true);
                        }}
                        className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl pl-3 pr-8 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                        required
                      />
                      <Search size={14} className="absolute right-3 top-3 text-gray-400" />
                    </div>

                    {/* Quick Landmark Chips */}
                    <div className="flex flex-wrap gap-1.5 pt-1.5">
                      {["UBTH", "King's Square", "UNIBEN Ugbowo", "Benin Airport"].map(tag => (
                        <button
                          key={tag}
                          type="button"
                          onClick={() => {
                            const lm = BENIN_LANDMARKS.find(l => l.name.toLowerCase().includes(tag.toLowerCase()));
                            if (lm) {
                              setPickupAddress(lm.address);
                              setPickupCoord(lm.coord);
                              setShowPickupDropdown(false);
                            }
                          }}
                          className="px-2 py-0.5 bg-gray-200 dark:bg-white/10 text-gray-700 dark:text-gray-300 rounded-md text-[10px] font-bold hover:bg-[#FFB800] hover:text-[#111] transition-colors"
                        >
                          {tag}
                        </button>
                      ))}
                    </div>

                    {/* Autocomplete Dropdown */}
                    {showPickupDropdown && pickupSuggestions.length > 0 && (
                      <div className="absolute top-full left-0 right-0 mt-1 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-xl shadow-xl z-50 max-h-48 overflow-y-auto">
                        {pickupSuggestions.map((item, idx) => (
                          <div
                            key={idx}
                            onClick={() => {
                              setPickupAddress(item.address || item.title);
                              if (item.coord) setPickupCoord(item.coord);
                              setShowPickupDropdown(false);
                            }}
                            className="px-3 py-2 hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer border-b border-gray-100 dark:border-white/5 last:border-0"
                          >
                            <p className="text-xs font-black text-gray-900 dark:text-white flex items-center gap-1.5">
                              <MapPin size={12} className="text-[#FFB800]" /> {item.title}
                            </p>
                            <p className="text-[10px] text-gray-500 dark:text-gray-400 pl-4">{item.address}</p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* Receiver Delivery Section */}
                <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-3">
                  <div className="flex items-center gap-2 text-xs font-black text-gray-900 dark:text-white uppercase tracking-wider">
                    <div className="w-6 h-6 rounded-lg bg-emerald-500 text-white flex items-center justify-center font-black">
                      <MapPin size={13} />
                    </div>
                    <span>2. Delivery Destination (Receiver)</span>
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Receiver Full Name *</label>
                    <input
                      type="text"
                      placeholder="e.g. Efe Agbonlahor"
                      value={receiverName}
                      onChange={e => setReceiverName(e.target.value)}
                      className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">Receiver Phone *</label>
                    <input
                      type="tel"
                      placeholder="080XXXXXXXX"
                      value={receiverPhone}
                      onChange={e => setReceiverPhone(e.target.value)}
                      className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                      required
                    />
                  </div>

                  {/* Delivery Address with Intelligent Autocomplete */}
                  <div className="relative">
                    <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 mb-0.5">
                      Delivery Address in Benin City *
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        placeholder="Search landmark (e.g. Uselu, Sapele Road, Ramat Park)"
                        value={deliveryAddress}
                        onChange={e => {
                          setDeliveryAddress(e.target.value);
                          setDeliveryQuery(e.target.value);
                          searchAddress(e.target.value, false);
                          setShowDeliveryDropdown(true);
                        }}
                        onFocus={() => {
                          if (deliverySuggestions.length > 0) setShowDeliveryDropdown(true);
                        }}
                        className="w-full h-10 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl pl-3 pr-8 text-xs text-gray-900 dark:text-white font-medium outline-none focus:ring-2 focus:ring-[#FFB800]"
                        required
                      />
                      <Search size={14} className="absolute right-3 top-3 text-gray-400" />
                    </div>

                    {/* Quick Landmark Chips */}
                    <div className="flex flex-wrap gap-1.5 pt-1.5">
                      {["Uselu", "Sapele Road", "Ramat Park", "Country Home"].map(tag => (
                        <button
                          key={tag}
                          type="button"
                          onClick={() => {
                            const lm = BENIN_LANDMARKS.find(l => l.name.toLowerCase().includes(tag.toLowerCase()));
                            if (lm) {
                              setDeliveryAddress(lm.address);
                              setDeliveryCoord(lm.coord);
                              setShowDeliveryDropdown(false);
                            }
                          }}
                          className="px-2 py-0.5 bg-gray-200 dark:bg-white/10 text-gray-700 dark:text-gray-300 rounded-md text-[10px] font-bold hover:bg-emerald-500 hover:text-white transition-colors"
                        >
                          {tag}
                        </button>
                      ))}
                    </div>

                    {/* Autocomplete Dropdown */}
                    {showDeliveryDropdown && deliverySuggestions.length > 0 && (
                      <div className="absolute top-full left-0 right-0 mt-1 bg-white dark:bg-[#1a1a1a] border border-gray-200 dark:border-white/10 rounded-xl shadow-xl z-50 max-h-48 overflow-y-auto">
                        {deliverySuggestions.map((item, idx) => (
                          <div
                            key={idx}
                            onClick={() => {
                              setDeliveryAddress(item.address || item.title);
                              if (item.coord) setDeliveryCoord(item.coord);
                              setShowDeliveryDropdown(false);
                            }}
                            className="px-3 py-2 hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer border-b border-gray-100 dark:border-white/5 last:border-0"
                          >
                            <p className="text-xs font-black text-gray-900 dark:text-white flex items-center gap-1.5">
                              <MapPin size={12} className="text-emerald-500" /> {item.title}
                            </p>
                            <p className="text-[10px] text-gray-500 dark:text-gray-400 pl-4">{item.address}</p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Real-Time Interactive Leaflet Route Map */}
              <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Navigation size={16} className="text-[#FFB800]" />
                    <span className="text-xs font-black uppercase text-gray-900 dark:text-white">
                      Live Route Verification Map
                    </span>
                  </div>
                  <div className="flex items-center gap-3 text-xs">
                    <span className="px-2.5 py-1 bg-[#FFB800]/20 text-[#111] dark:text-[#FFB800] rounded-lg font-black">
                      Distance: {calculatedDistanceKm} km
                    </span>
                    <span className="text-gray-500 text-[11px]">
                      Est. Transit: ~{Math.round(calculatedDistanceKm * 3.5 + 8)} mins
                    </span>
                  </div>
                </div>

                <div
                  ref={mapContainerRef}
                  className="w-full h-64 rounded-xl border border-gray-200 dark:border-white/10 overflow-hidden shadow-inner z-0"
                />
                <p className="text-[10px] text-gray-500 dark:text-gray-400 italic">
                  💡 Tip: You can drag the Gold (Pickup) or Emerald (Delivery) pins directly on the map to fine-tune exact geo-coordinates.
                </p>
              </div>
            </div>
          )}

          {/* STEP 3: RIDER ASSIGNMENT & FARE CALCULATION */}
          {step === 3 && (
            <div className="space-y-5 animate-fade-in">
              {/* Dispatch Summary Card */}
              <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                <div>
                  <span className="block text-[10px] uppercase font-bold text-gray-500">Service Tier</span>
                  <span className="font-black text-gray-900 dark:text-white">{category}</span>
                </div>
                <div>
                  <span className="block text-[10px] uppercase font-bold text-gray-500">Item</span>
                  <span className="font-black text-gray-900 dark:text-white">{itemName || "Consignment"}</span>
                </div>
                <div>
                  <span className="block text-[10px] uppercase font-bold text-gray-500">Estimated Route</span>
                  <span className="font-black text-gray-900 dark:text-white">{calculatedDistanceKm} km</span>
                </div>
                <div>
                  <span className="block text-[10px] uppercase font-bold text-gray-500">Declared Value</span>
                  <span className="font-black text-gray-900 dark:text-white">
                    {declaredValue ? `₦${Number(declaredValue).toLocaleString()}` : "Not declared"}
                  </span>
                </div>
              </div>

              {/* Rider Assignment Options */}
              <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-4">
                <label className="block text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                  Select Fleet Rider Assignment Method *
                </label>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {[
                    { id: "auto", title: "Auto-Assign Nearest", desc: "Selects closest online courier automatically", icon: Sparkles },
                    { id: "manual", title: "Manual Rider Selection", desc: "Assign directly to a specific courier", icon: Bike },
                    { id: "pool", title: "Broadcast to Fleet Pool", desc: "Leaves in pool for active riders to accept", icon: Layers }
                  ].map(opt => {
                    const isSelected = assignmentMode === opt.id;
                    const IconComp = opt.icon;
                    return (
                      <div
                        key={opt.id}
                        onClick={() => setAssignmentMode(opt.id as any)}
                        className={`p-3.5 rounded-2xl border-2 cursor-pointer transition-all ${
                          isSelected
                            ? "border-[#FFB800] bg-[#FFB800]/10 dark:bg-[#FFB800]/5 shadow-sm"
                            : "border-gray-200 dark:border-white/10 hover:border-gray-300 dark:hover:border-white/20 bg-white dark:bg-[#141414]"
                        }`}
                      >
                        <div className="flex items-center justify-between mb-1.5">
                          <IconComp size={18} className={isSelected ? "text-[#FFB800]" : "text-gray-400"} />
                          {isSelected && <Check size={16} className="text-[#FFB800]" />}
                        </div>
                        <h4 className="text-xs font-black text-gray-900 dark:text-white">{opt.title}</h4>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5 leading-snug">{opt.desc}</p>
                      </div>
                    );
                  })}
                </div>

                {/* Auto Match Preview */}
                {assignmentMode === "auto" && (
                  <div className="bg-white dark:bg-[#141414] p-3.5 rounded-xl border border-gray-200 dark:border-white/10 flex items-center justify-between">
                    {nearestDriver ? (
                      <>
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-[#FFB800] text-[#111] flex items-center justify-center font-black">
                            <Bike size={20} />
                          </div>
                          <div>
                            <p className="text-xs font-black text-gray-900 dark:text-white flex items-center gap-1.5">
                              {nearestDriver.name || nearestDriver.displayName}
                              <span className="px-1.5 py-0.5 bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 rounded text-[9px] font-bold">
                                ONLINE
                              </span>
                            </p>
                            <p className="text-[11px] text-gray-500">
                              Bike: {nearestDriver.bikeNumber || "Fleet Bike"} • {nearestDriver.phone || "No phone"}
                            </p>
                          </div>
                        </div>
                        <div className="text-right">
                          <span className="text-xs font-black text-[#FFB800]">
                            ~{(nearestDriver as any).distanceToPickup || 1.2} km away
                          </span>
                          <p className="text-[10px] text-gray-400">Closest courier to pickup</p>
                        </div>
                      </>
                    ) : (
                      <div className="text-center py-2 w-full text-xs text-amber-600 font-bold flex items-center justify-center gap-2">
                        <AlertTriangle size={16} /> No couriers currently marked online. Will broadcast to fleet pool upon creation.
                      </div>
                    )}
                  </div>
                )}

                {/* Manual Selection Dropdown */}
                {assignmentMode === "manual" && (
                  <div>
                    <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">
                      Choose Online Rider
                    </label>
                    <select
                      value={selectedRiderId}
                      onChange={e => setSelectedRiderId(e.target.value)}
                      className="w-full h-11 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-xl px-3.5 text-xs text-gray-900 dark:text-white font-semibold outline-none focus:ring-2 focus:ring-[#FFB800]"
                    >
                      <option value="">-- Choose Rider --</option>
                      {(drivers || []).map(d => (
                        <option key={d.id || d.uid} value={d.id || d.uid}>
                          {d.name || d.displayName} ({d.bikeNumber || "No bike"}) • {d.isOnline ? "🟢 Online" : "⚪ Offline"}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
              </div>

              {/* Fare Calculation & Pricing Override */}
              <div className="bg-gray-50 dark:bg-[#1E1E1E] p-4 rounded-2xl border border-gray-200 dark:border-white/10 space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-xs font-black uppercase text-gray-900 dark:text-white">Computed Fare Price</h4>
                    <p className="text-[11px] text-gray-500">
                      Calculated automatically using {category} rate card ({calculatedDistanceKm} km road distance)
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="text-xl font-black text-[#FFB800]">
                      ₦{finalFare.toLocaleString()}
                    </span>
                  </div>
                </div>

                <div className="pt-2 border-t border-gray-200 dark:border-white/10 flex items-center justify-between gap-4">
                  <label className="text-[11px] font-bold text-gray-700 dark:text-gray-300">
                    Admin Custom Rate Override (₦):
                  </label>
                  <input
                    type="number"
                    min="500"
                    step="100"
                    placeholder={`Suggested: ₦${suggestedFare}`}
                    value={customPriceOverride !== null ? customPriceOverride : ""}
                    onChange={e => {
                      const val = e.target.value;
                      setCustomPriceOverride(val ? Number(val) : null);
                    }}
                    className="w-40 h-9 bg-white dark:bg-[#141414] border border-gray-200 dark:border-white/10 rounded-lg px-3 text-xs text-gray-900 dark:text-white font-bold outline-none focus:ring-2 focus:ring-[#FFB800]"
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer Controls */}
        <div className="bg-gray-100 dark:bg-[#1A1A1A] px-6 py-4 border-t border-black/10 dark:border-white/10 flex items-center justify-between">
          {step > 1 ? (
            <button
              onClick={() => setStep((step - 1) as any)}
              className="h-10 px-4 rounded-xl border border-gray-300 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/5 flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <ArrowLeft size={16} /> Back
            </button>
          ) : (
            <button
              onClick={onClose}
              className="h-10 px-4 rounded-xl border border-gray-300 dark:border-white/10 text-xs font-bold text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-white/5 transition-colors cursor-pointer"
            >
              Cancel
            </button>
          )}

          {step < 3 ? (
            <button
              onClick={() => {
                if (step === 1) {
                  if (!itemName.trim()) {
                    addToast?.("error", "Please provide consignment item name.");
                    return;
                  }
                  setStep(2);
                } else if (step === 2) {
                  if (!senderName.trim() || !senderPhone.trim() || !pickupAddress.trim()) {
                    addToast?.("error", "Please fill all sender pickup information.");
                    return;
                  }
                  if (!receiverName.trim() || !receiverPhone.trim() || !deliveryAddress.trim()) {
                    addToast?.("error", "Please fill all receiver delivery information.");
                    return;
                  }
                  setStep(3);
                }
              }}
              className="h-10 px-6 rounded-xl bg-[#FFB800] text-[#111] text-xs font-black shadow-xs hover:bg-[#FFB800]/90 flex items-center gap-1.5 transition-all cursor-pointer"
            >
              Proceed to Next Step <ArrowRight size={16} />
            </button>
          ) : (
            <button
              onClick={handleSubmitBooking}
              disabled={isSubmitting}
              className="h-10 px-6 rounded-xl bg-[#FFB800] text-[#111] text-xs font-black shadow-xs hover:bg-[#FFB800]/90 flex items-center gap-1.5 transition-all disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting ? "Creating & Dispatched..." : "Confirm & Dispatch Now"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminDispatchBookingModal;
