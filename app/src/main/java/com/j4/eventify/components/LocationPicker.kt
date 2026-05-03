package com.j4.eventify.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.j4.eventify.data.remote.LocationClient
import com.j4.eventify.data.remote.OsmPlace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPicker(
    currentLocationName: String,
    onLocationSelected: (name: String, lat: Double?, lon: Double?) -> Unit,
    accentColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<OsmPlace>>(emptyList()) }
    var isFetchingGps by remember { mutableStateOf(false) }

    // ── THE FIX: A simple flag to tell the search engine to ignore the next text change ──
    var skipNextSearch by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun fetchFreshLocation() {
        isFetchingGps = true
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        scope.launch {
                            try {
                                val place = LocationClient.api.reverseGeocode(location.latitude, location.longitude)
                                val shortName = place.displayName.split(",").first()

                                // ── Trigger the skip flag before sending the text! ──
                                skipNextSearch = true
                                onLocationSelected(shortName, location.latitude, location.longitude)
                                expanded = false
                            } catch (e: Exception) {
                                skipNextSearch = true
                                onLocationSelected("Current GPS Location", location.latitude, location.longitude)
                                expanded = false
                            } finally {
                                isFetchingGps = false
                            }
                        }
                    } else {
                        isFetchingGps = false
                        Toast.makeText(context, "Please turn on your device's GPS", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    isFetchingGps = false
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            isFetchingGps = false
            e.printStackTrace()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            fetchFreshLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(currentLocationName) {
        // ── THE FIX: If the flag is raised, lower it and skip the search ──
        if (skipNextSearch) {
            skipNextSearch = false
            return@LaunchedEffect
        }

        if (currentLocationName.length > 2 && currentLocationName != "Current GPS Location") {
            delay(600)
            try {
                searchResults = LocationClient.api.searchPlaces(currentLocationName)
                expanded = searchResults.isNotEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            expanded = false
        }
    }

    Surface(
        shape = RoundedCornerShape(13.dp),
        color = if (textColor == Color.White) Color(0xFF2A2A2A) else Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = currentLocationName,
                onValueChange = { newText ->
                    // ── If the user is typing manually, ensure the flag is off ──
                    skipNextSearch = false
                    onLocationSelected(newText, null, null)
                },
                placeholder = { Text("Add location", color = textColor.copy(alpha = 0.38f), fontSize = 16.sp) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = accentColor, modifier = Modifier.padding(start = 8.dp).size(22.dp)) },
                trailingIcon = {
                    if (isFetchingGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 12.dp).size(20.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                fetchFreshLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Use GPS", tint = accentColor)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = textColor),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = accentColor,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )

            if (expanded) {
                Column {
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                    searchResults.forEach { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // ── Trigger the skip flag before sending the text! ──
                                    skipNextSearch = true

                                    onLocationSelected(
                                        place.displayName.split(",").first(),
                                        place.lat.toDoubleOrNull(),
                                        place.lon.toDoubleOrNull()
                                    )
                                    expanded = false
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = place.displayName,
                                fontSize = 14.sp,
                                color = textColor,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}