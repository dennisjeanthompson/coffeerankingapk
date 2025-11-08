package com.example.coffeerankingapk.ui.screens.lover

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.coffeerankingapk.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions as MapboxCameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverOptions
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.discover.DiscoverResult
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CoffeeShopResult(
    val name: String,
    val address: String?,
    val coordinate: Point,
    val distance: Double?,
    val categories: List<String>?,
    val makiIcon: String?
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onNavigateToCafe: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    
    // Initialize Mapbox
    MapboxOptions.accessToken = context.getString(R.string.mapbox_access_token)
    
    // Initialize Discover SDK
    val discover = remember { Discover.create() }
    
    // Initialize Place Autocomplete for search
    val placeAutocomplete = remember { PlaceAutocomplete.create() }
    
    val locationProvider = remember { 
        LocationServiceFactory.getOrCreate().getDeviceLocationProvider(null).value
    }
    
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var coffeeShops by remember { mutableStateOf<List<CoffeeShopResult>>(emptyList()) }
    var selectedShop by remember { mutableStateOf<CoffeeShopResult?>(null) }
    var showShopDetails by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var routeLineManager by remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?>(null) }
    var isRouting by remember { mutableStateOf(false) }
    var routeDistance by remember { mutableStateOf<Double?>(null) }
    var routeDuration by remember { mutableStateOf<Double?>(null) }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<PlaceAutocompleteSuggestion>>(emptyList()) }
    var showSearchResults by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            userLocation = getCurrentLocation(context)
        }
    }
    
    // Search with Place Autocomplete
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && searchQuery.length >= 2) {
            isSearching = true
            try {
                val response = placeAutocomplete.suggestions(query = searchQuery)
                if (response.isValue) {
                    searchSuggestions = response.value.orEmpty()
                    showSearchResults = searchSuggestions.isNotEmpty()
                    Log.i("MapScreen", "Found ${searchSuggestions.size} search suggestions")
                } else {
                    Log.e("MapScreen", "Error fetching suggestions: ${response.error}")
                    searchSuggestions = emptyList()
                    showSearchResults = false
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Exception during search", e)
                searchSuggestions = emptyList()
                showSearchResults = false
            }
            isSearching = false
        } else {
            searchSuggestions = emptyList()
            showSearchResults = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapbox MapView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
                        // Enable location component
                        location.updateSettings {
                            enabled = true
                        }
                        // Prepare a polyline annotation manager for route rendering
                        routeLineManager = annotations.createPolylineAnnotationManager(null)
                        
                        // Center camera on user location
                        location.addOnIndicatorPositionChangedListener(object : OnIndicatorPositionChangedListener {
                            override fun onIndicatorPositionChanged(point: Point) {
                                mapboxMap.setCamera(
                                    MapboxCameraOptions.Builder()
                                        .center(point)
                                        .zoom(14.0)
                                        .build()
                                )
                                location.removeOnIndicatorPositionChangedListener(this)
                            }
                        })
                    }
                }
            }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search for places...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                        showSearchResults = false
                                    }
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        singleLine = true
                    )
                    
                    // Search results dropdown
                    if (showSearchResults && searchSuggestions.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(searchSuggestions) { suggestion ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Select this place and fly to it
                                            lifecycleOwner?.lifecycleScope?.launch {
                                                val result = placeAutocomplete.select(suggestion)
                                                result.onValue { placeResult ->
                                                    val coordinate = placeResult.coordinate
                                                    if (coordinate != null) {
                                                        mapView?.camera?.flyTo(
                                                            MapboxCameraOptions.Builder()
                                                                .center(coordinate)
                                                                .zoom(15.0)
                                                                .build()
                                                        )
                                                        // Request and display a preview route line
                                                        if (userLocation != null && mapView != null) {
                                                            lifecycleOwner?.lifecycleScope?.launch {
                                                                requestAndDrawRoute(
                                                                    context = context,
                                                                    originPoint = Point.fromLngLat(userLocation!!.longitude, userLocation!!.latitude),
                                                                    destinationPoint = coordinate,
                                                                    mapView = mapView!!,
                                                                    routeLineManager = routeLineManager,
                                                                    onRoutingState = { isRouting = it },
                                                                    onRouteInfo = { distance, duration ->
                                                                        routeDistance = distance
                                                                        routeDuration = duration
                                                                    }
                                                                )
                                                            }
                                                        }
                                                        searchQuery = placeResult.name
                                                        showSearchResults = false
                                                    }
                                                }
                                            }
                                        }
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = suggestion.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (suggestion.formattedAddress != null) {
                                        Text(
                                            text = suggestion.formattedAddress!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Search Nearby Button
            Button(
                onClick = {
                    if (locationPermissions.allPermissionsGranted && lifecycleOwner != null) {
                        lifecycleOwner.lifecycleScope.launch {
                            isLoading = true
                            try {
                                locationProvider?.getLastLocation { location ->
                                    if (location != null) {
                                        lifecycleOwner.lifecycleScope.launch {
                                            val userPoint = Point.fromLngLat(location.longitude, location.latitude)
                                            val response = discover.search(
                                                query = DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                                                proximity = userPoint,
                                                options = DiscoverOptions(limit = 20)
                                            )
                                            
                                            response.onValue { results ->
                                                Log.i("MapScreen", "Found ${results.size} coffee shops nearby")
                                                
                                                // Get user point for distance calculation
                                                val userPoint = Point.fromLngLat(location.longitude, location.latitude)
                                                
                                                // Convert to our data model
                                                coffeeShops = results.map { result ->
                                                    CoffeeShopResult(
                                                        name = result.name,
                                                        address = result.address.formattedAddress(),
                                                        coordinate = result.coordinate,
                                                        distance = calculateDistance(userPoint, result.coordinate, location.latitude),
                                                        categories = result.categories,
                                                        makiIcon = result.makiIcon
                                                    )
                                                }
                                                
                                                // Show markers on map
                                                mapView?.let { 
                                                    showMarkersOnMap(
                                                        mapView = it,
                                                        results = results,
                                                        context = context,
                                                        onShowRoute = { destPoint ->
                                                            val loc = userLocation
                                                            if (loc != null) {
                                                                lifecycleOwner.lifecycleScope.launch {
                                                                    requestAndDrawRoute(
                                                                        context = context,
                                                                            originPoint = Point.fromLngLat(loc.longitude, loc.latitude),
                                                                            destinationPoint = destPoint,
                                                                        mapView = it,
                                                                        routeLineManager = routeLineManager,
                                                                        onRoutingState = { isRouting = it },
                                                                        onRouteInfo = { distance, duration ->
                                                                            routeDistance = distance
                                                                            routeDuration = duration
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                isLoading = false
                                            }.onError { e ->
                                                Log.e("MapScreen", "Error searching coffee shops", e)
                                                isLoading = false
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        Log.e("MapScreen", "Location is null")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MapScreen", "Error in search", e)
                                isLoading = false
                            }
                        }
                    } else {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Nearby Coffee Shops")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search This Area Button
            Button(
                onClick = {
                    if (locationPermissions.allPermissionsGranted && lifecycleOwner != null && mapView != null) {
                        lifecycleOwner.lifecycleScope.launch {
                            isLoading = true
                            try {
                                val bounds = mapView!!.mapboxMap.getBounds()
                                val bbox = bounds.bounds
                                val boundingBox = com.mapbox.geojson.BoundingBox.fromPoints(
                                    bbox.southwest,
                                    bbox.northeast
                                )
                                val response = discover.search(
                                    query = DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                                    region = boundingBox,
                                    options = DiscoverOptions(limit = 20)
                                )
                                
                                response.onValue { results ->
                                    Log.i("MapScreen", "Found ${results.size} coffee shops in this area")
                                    
                                    coffeeShops = results.map { result ->
                                        CoffeeShopResult(
                                            name = result.name,
                                            address = result.address.formattedAddress(),
                                            coordinate = result.coordinate,
                                            distance = userLocation?.let { loc ->
                                                val userPoint = Point.fromLngLat(loc.longitude, loc.latitude)
                                                calculateDistance(userPoint, result.coordinate, loc.latitude)
                                            },
                                            categories = result.categories,
                                            makiIcon = result.makiIcon
                                        )
                                    }
                                    
                                    mapView?.let { 
                                        showMarkersOnMap(
                                            mapView = it,
                                            results = results,
                                            context = context,
                                            onShowRoute = { destPoint ->
                                                val loc = userLocation
                                                if (loc != null) {
                                                    lifecycleOwner.lifecycleScope.launch {
                                                        requestAndDrawRoute(
                                                            context = context,
                                                                originPoint = Point.fromLngLat(loc.longitude, loc.latitude),
                                                                destinationPoint = destPoint,
                                                            mapView = it,
                                                            routeLineManager = routeLineManager,
                                                            onRoutingState = { isRouting = it },
                                                            onRouteInfo = { distance, duration ->
                                                                routeDistance = distance
                                                                routeDuration = duration
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    isLoading = false
                                }.onError { e ->
                                    Log.e("MapScreen", "Error searching in area", e)
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                Log.e("MapScreen", "Error in search", e)
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && mapView != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search This Area")
            }
            
            // Show results count
            if (coffeeShops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = buildString {
                            append("Found ${coffeeShops.size} coffee shops")
                            if (isRouting) {
                                append("  •  Drawing route...")
                            } else if (routeDistance != null && routeDuration != null) {
                                val distanceKm = routeDistance!! / 1000.0
                                val durationMin = (routeDuration!! / 60.0).toInt()
                                append("  •  ${String.format("%.1f", distanceKm)} km, $durationMin min")
                            }
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Permission Request UI
        if (!locationPermissions.allPermissionsGranted) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Location Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We need your location to find nearby coffee shops",
                        style = MaterialTheme.typography.bodyMedium
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

// Helper function to show markers on map
private fun showMarkersOnMap(
    mapView: MapView,
    results: List<DiscoverResult>,
    context: Context,
    onShowRoute: (Point) -> Unit
) {
    try {
        val annotationManager = mapView.annotations.createPointAnnotationManager(null)
        annotationManager.deleteAll()
        
        if (results.isEmpty()) return
        
        val coordinates = mutableListOf<Point>()
        val resultMap = mutableMapOf<String, DiscoverResult>()
        
        results.forEach { result ->
            val options = PointAnnotationOptions()
                .withPoint(result.coordinate)
                .withIconAnchor(IconAnchor.BOTTOM)
            
            val annotation = annotationManager.create(options)
            resultMap[annotation.id] = result
            coordinates.add(result.coordinate)
        }
        
        // Add click listener for markers
        annotationManager.addClickListener { annotation ->
            resultMap[annotation.id]?.let { shop ->
                // Show dialog with route option only (turn-by-turn removed)
                android.app.AlertDialog.Builder(context)
                    .setTitle(shop.name)
                    .setMessage("Would you like to see the route to this coffee shop?")
                    .setPositiveButton("Show Route") { _, _ ->
                        onShowRoute(shop.coordinate)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            true
        }
        
        // Adjust camera to show all markers
        val edgeInsets = EdgeInsets(100.0, 100.0, 300.0, 100.0)
        mapView.mapboxMap.cameraForCoordinates(
            coordinates,
            edgeInsets
        )?.let { cameraOptions ->
            mapView.camera.flyTo(cameraOptions)
        }
        
        Log.i("MapScreen", "Displayed ${results.size} markers on map")
    } catch (e: Exception) {
        Log.e("MapScreen", "Error showing markers", e)
    }
}

// Helper function to calculate distance
private fun calculateDistance(origin: Point, destination: Point, latitude: Double): Double {
    val calculator = DistanceCalculator.instance(latitude = latitude)
    return calculator.distance(origin, destination)
}

// Helper extension for DiscoverAddress
private fun com.mapbox.search.discover.DiscoverAddress.formattedAddress(): String {
    val parts = listOfNotNull(
        houseNumber,
        street,
        locality,
        place,
        region
    )
    return if (parts.isNotEmpty()) parts.joinToString(", ") else "Address not available"
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
        Log.e("MapScreen", "Error getting location", e)
        null
    }
}

// Fetch a route via Mapbox Directions and render it as a polyline on the map
private suspend fun requestAndDrawRoute(
    context: Context,
    originPoint: Point,
    destinationPoint: Point,
    mapView: MapView,
    routeLineManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?,
    onRoutingState: (Boolean) -> Unit,
    onRouteInfo: (distance: Double, duration: Double) -> Unit
) {
    try {
        onRoutingState(true)
        
        // Build the Mapbox Directions API URL directly
        val accessToken = MapboxOptions.accessToken ?: ""
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                "${originPoint.longitude()},${originPoint.latitude()};" +
                "${destinationPoint.longitude()},${destinationPoint.latitude()}" +
                "?overview=full&geometries=polyline6&steps=true&access_token=$accessToken"
        
        Log.i("MapScreen", "Requesting route from Mapbox Directions API")
        
        // Execute the HTTP request on IO dispatcher
        val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            java.net.URL(url).readText()
        }
        
        Log.i("MapScreen", "Directions response received: ${responseText.take(200)}...")
        
        // Parse JSON response manually
        val jsonResponse = org.json.JSONObject(responseText)
        val routes = jsonResponse.optJSONArray("routes")
        
        if (routes == null || routes.length() == 0) {
            Log.e("MapScreen", "No routes returned")
            // Fallback to straight line
            drawStraightLineRoute(mapView, routeLineManager, originPoint, destinationPoint)
            onRoutingState(false)
            return
        }
        
        val route = routes.getJSONObject(0)
        val geometry = route.optString("geometry")
        
        if (geometry.isNullOrEmpty()) {
            Log.e("MapScreen", "No route geometry returned")
            // Fallback to straight line
            drawStraightLineRoute(mapView, routeLineManager, originPoint, destinationPoint)
            onRoutingState(false)
            return
        }

        // Decode the route geometry (Polyline6 format)
        val points: List<Point> = PolylineUtils.decode(geometry, 6)
        Log.i("MapScreen", "Decoded route with ${points.size} points")
        
        // Extract route info
        val distance = route.optDouble("distance", 0.0) // in meters
        val duration = route.optDouble("duration", 0.0) // in seconds
        Log.i("MapScreen", "Route: ${distance}m, ${duration}s")
        
        // Update route info
        onRouteInfo(distance, duration)

        // Draw route polyline
        drawRoutePolyline(mapView, routeLineManager, points)

        // Fit camera to route
        mapView.mapboxMap.cameraForCoordinates(points, EdgeInsets(100.0, 100.0, 300.0, 100.0))?.let { cameraOptions ->
            mapView.camera.flyTo(cameraOptions)
        }
    } catch (e: Exception) {
        Log.e("MapScreen", "Error fetching route", e)
        // Fallback to straight line on error
        try {
            drawStraightLineRoute(mapView, routeLineManager, originPoint, destinationPoint)
        } catch (fallbackError: Exception) {
            Log.e("MapScreen", "Error drawing fallback route", fallbackError)
        }
    } finally {
        onRoutingState(false)
    }
}

// Fallback: draw a simple straight line between two points
private fun drawStraightLineRoute(
    mapView: MapView,
    routeLineManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?,
    originPoint: Point,
    destinationPoint: Point
) {
    val points = listOf(originPoint, destinationPoint)
    Log.i("MapScreen", "Drawing straight-line fallback route")
    drawRoutePolyline(mapView, routeLineManager, points)
    mapView.mapboxMap.cameraForCoordinates(points, EdgeInsets(100.0, 100.0, 300.0, 100.0))?.let { cameraOptions ->
        mapView.camera.flyTo(cameraOptions)
    }
}

private fun drawRoutePolyline(
    mapView: MapView,
    routeLineManager: com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager?,
    points: List<Point>
) {
    try {
        val manager = routeLineManager ?: mapView.annotations.createPolylineAnnotationManager(null)
        // Clear previous route
        manager.deleteAll()
        val options = PolylineAnnotationOptions()
            .withPoints(points)
            .withLineColor("#1E90FF")
            .withLineWidth(6.0)
        manager.create(options)
    } catch (e: Exception) {
        Log.e("MapScreen", "Error drawing route polyline", e)
    }
}
