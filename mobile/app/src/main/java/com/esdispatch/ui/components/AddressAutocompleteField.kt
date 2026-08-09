package com.esdispatch.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*
import com.esdispatch.utils.GeocoderUtils
import com.esdispatch.utils.SearchResultItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddressAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddressSelected: (SearchResultItem) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "DELIVERY ADDRESS",
    placeholder: String = "Enter street address, landmark or POI…",
    isDark: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLocatingGPS by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val fusedLocationClient = remember(context) {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    // Trigger debounced Mapbox Places autocomplete search when value changes
    fun performSearch(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            searchResults = emptyList()
            isSearching = false
            showDropdown = false
            return
        }
        searchJob = scope.launch {
            isSearching = true
            delay(350L) // 350ms debouncing
            val items = GeocoderUtils.fetchMapboxPlacesAutocompleteItems(query)
            searchResults = items
            isSearching = false
            showDropdown = items.isNotEmpty()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Gold,
                letterSpacing = 1.sp
            )

            // "📍 Use Current Location" GPS trigger chip
            Surface(
                onClick = {
                    val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (!fineGranted && !coarseGranted) {
                        Toast.makeText(context, "Location permission required to auto-detect address", Toast.LENGTH_SHORT).show()
                        return@Surface
                    }

                    isLocatingGPS = true
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                            if (loc != null) {
                                scope.launch {
                                    val detectedAddress = GeocoderUtils.reverseGeocodeCoordinates(context, loc.latitude, loc.longitude)
                                    // Keep title blank so displayInput resolves to the raw address — no "Current Location," prefix
                                    val item = SearchResultItem(
                                        title = "",
                                        fullAddress = detectedAddress,
                                        lat = loc.latitude,
                                        lng = loc.longitude
                                    )
                                    onValueChange(detectedAddress)
                                    onAddressSelected(item)
                                    showDropdown = false
                                    isLocatingGPS = false
                                    Toast.makeText(context, "📍 Address detected via GPS!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                isLocatingGPS = false
                                Toast.makeText(context, "Unable to get GPS location. Please type address.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            isLocatingGPS = false
                            Toast.makeText(context, "GPS detection error. Please type address manually.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        isLocatingGPS = false
                        Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = Gold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLocatingGPS) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Gold, strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Detect Location",
                            tint = Gold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLocatingGPS) "Detecting..." else "Use Current Location",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                }
            }
        }

        // Search Input Field
        OutlinedTextField(
            value = value,
            onValueChange = { newQuery ->
                onValueChange(newQuery)
                performSearch(newQuery)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextGray, fontSize = 12.sp) },
            leadingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Gold, strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = {
                        onValueChange("")
                        searchResults = emptyList()
                        showDropdown = false
                    }) {
                        Icon(Icons.Filled.Clear, null, tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = if (isDark) BorderDark else Slate,
                focusedContainerColor = if (isDark) LuxuryBlack else Color.White,
                unfocusedContainerColor = if (isDark) LuxuryBlack else Color.White
            ),
            singleLine = true
        )

        // Mapbox Live Autocomplete Dropdown Suggestions Surface
        AnimatedVisibility(
            visible = showDropdown && searchResults.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Charcoal else Color.White,
                border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .heightIn(max = 220.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(searchResults) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(item.displayInput)
                                    onAddressSelected(item)
                                    showDropdown = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Obsidian,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.fullAddress,
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
