package com.esdispatch.ui.maps

import com.google.android.gms.maps.model.MapStyleOptions

/**
 * Bolt-Grade Minimalist Clean Map Styles for ESDispatch.
 * 
 * Design specifications:
 * - Commercial/Tourist POIs removed completely (visibility: off) to eliminate clutter.
 * - Road geometry is emphasized with crisp contrast and clear hierarchy.
 * - Dark mode matches the ESDISPATCH Obsidian (#0E0E10) luxury theme.
 * - Light mode provides a clean, modern silver-and-white canvas.
 */
object BoltMapStyle {

    val Dark: MapStyleOptions = MapStyleOptions(
        """
        [
          {
            "elementType": "geometry",
            "stylers": [
              { "color": "#121318" }
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
              { "color": "#8E929E" }
            ]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [
              { "color": "#121318" }
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
              { "color": "#B4B8C5" }
            ]
          },
          {
            "featureType": "administrative.land_parcel",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "administrative.locality",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#CFD3DE" }
            ]
          },
          {
            "featureType": "poi",
            "stylers": [
              { "visibility": "off" }
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
              { "color": "#16181F" }
            ]
          },
          {
            "featureType": "road",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#7E8290" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#2C2E3A" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [
              { "color": "#1D1F27" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#A5A9B8" }
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
              { "color": "#090A0D" }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#424652" }
            ]
          }
        ]
        """.trimIndent()
    )

    val Light: MapStyleOptions = MapStyleOptions(
        """
        [
          {
            "elementType": "geometry",
            "stylers": [
              { "color": "#F3F4F7" }
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
              { "color": "#4A4D57" }
            ]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [
              { "color": "#F3F4F7" }
            ]
          },
          {
            "featureType": "administrative.land_parcel",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "administrative.neighborhood",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "poi",
            "stylers": [
              { "visibility": "off" }
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
              { "color": "#E5E7EB" }
            ]
          },
          {
            "featureType": "road",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#6B7280" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#ECEEF2" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [
              { "color": "#D5D8DF" }
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
              { "color": "#D8DEE6" }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#8C96A5" }
            ]
          }
        ]
        """.trimIndent()
    )
}
