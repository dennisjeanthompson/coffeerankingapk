package com.example.coffeerankingapk.ui.screens.lover

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.coffeerankingapk.data.Cafe
import com.example.coffeerankingapk.data.MockData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.tasks.await

data class MapRoute(
    val distance: String,
    val duration: String,
    val coordinates: List<Point>
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onNavigateToCafe: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var selectedCafe by remember { mutableStateOf<Cafe?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var showRouteDetails by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf<MapRoute?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val cafes = MockData.cafes
    val filteredCafes = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            cafes
        } else {
            cafes.filter { cafe ->
                cafe.name.contains(searchQuery, ignoreCase = true) ||
                cafe.address.contains(searchQuery, ignoreCase = true) ||
                cafe.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            userLocation = getCurrentLocation(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapbox Map
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    getMapboxMap().apply {
                        loadStyleUri(Style.MAPBOX_STREETS) { style ->
                            // Setup location component
                            location.updateSettings {
                                enabled = locationPermissions.allPermissionsGranted
                                pulsingEnabled = true
                            }

                            // Set initial camera position
                            if (userLocation != null) {
                                setCamera(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(
                                            userLocation!!.longitude,
                                            userLocation!!.latitude
                                        ))
                                        .zoom(12.0)
                                        .build()
                                )
                            } else {
                                // Default to New York City area
                                setCamera(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(-73.9857, 40.7484))
                                        .zoom(12.0)
                                        .build()
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    TextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            showSearchResults = it.isNotEmpty()
                        },
                        placeholder = { Text("Search cafes...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                    
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            showSearchResults = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            }

            // Search Results
            if (showSearchResults && filteredCafes.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(top = 8.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    LazyColumn {
                        items(filteredCafes) { cafe ->
                            ListItem(
                                headlineContent = { Text(cafe.name) },
                                supportingContent = { Text(cafe.address) },
                                trailingContent = { 
                                    Text(
                                        text = "⭐ ${cafe.rating}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.clickable {
                                    selectedCafe = cafe
                                    showSearchResults = false
                                    searchQuery = ""
                                    
                                    // Animate camera to cafe location
                                    mapView?.getMapboxMap()?.setCamera(
                                        CameraOptions.Builder()
                                            .center(Point.fromLngLat(cafe.longitude, cafe.latitude))
                                            .zoom(15.0)
                                            .build()
                                    )
                                }
                            )
                            if (cafe != filteredCafes.last()) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My Location Button
            if (locationPermissions.allPermissionsGranted) {
                FloatingActionButton(
                    onClick = {
                        userLocation?.let { location ->
                            mapView?.getMapboxMap()?.setCamera(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(location.longitude, location.latitude))
                                    .zoom(15.0)
                                    .build()
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Selected Cafe Bottom Sheet
        if (selectedCafe != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCafe!!.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedCafe!!.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⭐ ${selectedCafe!!.rating}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            IconButton(onClick = { selectedCafe = null }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Close"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (userLocation != null) {
                                        val route = calculateRoute(
                                            userLocation!!,
                                            selectedCafe!!
                                        )
                                        currentRoute = route
                                        showRouteDetails = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = locationPermissions.allPermissionsGranted && userLocation != null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Directions")
                            }

                            OutlinedButton(
                                onClick = { onNavigateToCafe(selectedCafe!!.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Details")
                            }
                        }
                    }
                }
            }
        }

        // Route Details Dialog
        if (showRouteDetails && currentRoute != null) {
            Dialog(onDismissRequest = { showRouteDetails = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Route to ${selectedCafe?.name}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showRouteDetails = false }) {
                                Icon(Icons.Default.Clear, "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentRoute!!.distance,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Distance",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentRoute!!.duration,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Duration",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showRouteDetails = false
                                // In a real app, this would start turn-by-turn navigation
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Navigation")
                        }
                    }
                }
            }
        }

        // Request Permissions
        if (!locationPermissions.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Location Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enable location to see your position and get directions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { locationPermissions.launchMultiplePermissionRequest() }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): Location? {
    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).await()
    } catch (e: Exception) {
        null
    }
}

private fun calculateRoute(userLocation: Location, cafe: Cafe): MapRoute {
    // Calculate distance using Haversine formula
    val results = FloatArray(1)
    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        cafe.latitude,
        cafe.longitude,
        results
    )
    
    val distanceInKm = results[0] / 1000
    val distanceStr = if (distanceInKm < 1) {
        "${(distanceInKm * 1000).toInt()} m"
    } else {
        "%.1f km".format(distanceInKm)
    }
    
    // Estimate duration (assuming 5 km/h walking speed)
    val durationInMinutes = (distanceInKm / 5.0 * 60).toInt()
    val durationStr = if (durationInMinutes < 60) {
        "$durationInMinutes min"
    } else {
        "${durationInMinutes / 60} h ${durationInMinutes % 60} min"
    }
    
    // Create simple straight line route (in real app, use Mapbox Directions API)
    val coordinates = listOf(
        Point.fromLngLat(userLocation.longitude, userLocation.latitude),
        Point.fromLngLat(cafe.longitude, cafe.latitude)
    )
    
    return MapRoute(
        distance = distanceStr,
        duration = durationStr,
        coordinates = coordinates
    )
}
