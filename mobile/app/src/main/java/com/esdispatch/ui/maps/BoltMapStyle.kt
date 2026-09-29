package com.esdispatch.ui.maps

import com.google.android.gms.maps.model.MapStyleOptions

/**
 * Bolt-Grade Minimalist High-Contrast Map Styles for ESDispatch.
 * 
 * Design specifications:
 * - Commercial/Tourist POIs removed completely (visibility: off) to eliminate clutter.
 * - Road geometry is emphasized with crisp contrast and clear hierarchy.
 * - Road names and street labels are rendered in high-contrast dark slate (#1E293B) with white halos.
 * - Highways feature a subtle warm gold fill (#FEF3C7) with amber/gold borders (#F59E0B) matching ESDispatch luxury identity.
 * - Water bodies rendered in unmistakable soft blue (#C4D9F8).
 * - Parks and natural spaces rendered in gentle light green (#E8F5E9) for geographic orientation.
 * - Dark mode matches the ESDISPATCH Obsidian (#0E0E10) luxury theme with Gold highway highlights.
 */
object BoltMapStyle {

    val Dark: MapStyleOptions? by lazy {
        try {
            MapStyleOptions(
                """
                [
                  {
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#0E0E10" }
                    ]
                  },
                  {
                    "elementType": "labels.icon",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#94A3B8" }
                    ]
                  },
                  {
                    "elementType": "labels.text.stroke",
                    "stylers": [
                      { "color": "#0E0E10" },
                      { "weight": 2.5 }
                    ]
                  },
                  {
                    "featureType": "administrative",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#262832" }
                    ]
                  },
                  {
                    "featureType": "administrative.country",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#CBD5E1" }
                    ]
                  },
                  {
                    "featureType": "administrative.locality",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#F1F5F9" }
                    ]
                  },
                  {
                    "featureType": "poi",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "stylers": [
                      { "visibility": "on" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#111A15" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#4ADE80" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#1E2028" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#2D313E" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#CBD5E1" }
                    ]
                  },
                  {
                    "featureType": "road.arterial",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#242733" }
                    ]
                  },
                  {
                    "featureType": "road.arterial",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#3E4456" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#2A2616" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#FFB800" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#FFD566" }
                    ]
                  },
                  {
                    "featureType": "transit",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "featureType": "water",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#131B2A" }
                    ]
                  },
                  {
                    "featureType": "water",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#60A5FA" }
                    ]
                  }
                ]
                """.trimIndent()
            )
        } catch (_: Throwable) {
            null
        }
    }

    val Light: MapStyleOptions? by lazy {
        try {
            MapStyleOptions(
                """
                [
                  {
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#F8F9FA" }
                    ]
                  },
                  {
                    "elementType": "labels.icon",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#1E293B" }
                    ]
                  },
                  {
                    "elementType": "labels.text.stroke",
                    "stylers": [
                      { "color": "#FFFFFF" },
                      { "weight": 2.5 }
                    ]
                  },
                  {
                    "featureType": "administrative",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#CBD5E1" }
                    ]
                  },
                  {
                    "featureType": "administrative.country",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#334155" }
                    ]
                  },
                  {
                    "featureType": "administrative.locality",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#0F172A" }
                    ]
                  },
                  {
                    "featureType": "poi",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "stylers": [
                      { "visibility": "on" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#E8F5E9" }
                    ]
                  },
                  {
                    "featureType": "poi.park",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#2E7D32" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#FFFFFF" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#CBD5E1" }
                    ]
                  },
                  {
                    "featureType": "road",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#1E293B" }
                    ]
                  },
                  {
                    "featureType": "road.arterial",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#FFFFFF" }
                    ]
                  },
                  {
                    "featureType": "road.arterial",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#94A3B8" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "geometry.fill",
                    "stylers": [
                      { "color": "#FEF3C7" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "geometry.stroke",
                    "stylers": [
                      { "color": "#F59E0B" }
                    ]
                  },
                  {
                    "featureType": "road.highway",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#78350F" }
                    ]
                  },
                  {
                    "featureType": "transit",
                    "stylers": [
                      { "visibility": "off" }
                    ]
                  },
                  {
                    "featureType": "water",
                    "elementType": "geometry",
                    "stylers": [
                      { "color": "#C4D9F8" }
                    ]
                  },
                  {
                    "featureType": "water",
                    "elementType": "labels.text.fill",
                    "stylers": [
                      { "color": "#2563EB" }
                    ]
                  }
                ]
                """.trimIndent()
            )
        } catch (_: Throwable) {
            null
        }
    }
}
