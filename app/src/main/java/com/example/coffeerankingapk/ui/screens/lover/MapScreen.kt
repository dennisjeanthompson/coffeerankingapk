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
import com.example.coffeerankingapk.ui.navigation.TurnByTurnNavigationActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions as MapboxCameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
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
                                                mapView?.let { showMarkersOnMap(it, results, context) }
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
                                    
                                    mapView?.let { showMarkersOnMap(it, results, context) }
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
                        text = "Found ${coffeeShops.size} coffee shops",
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
private fun showMarkersOnMap(mapView: MapView, results: List<DiscoverResult>, context: Context) {
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
                // Show dialog with navigation option
                android.app.AlertDialog.Builder(context)
                    .setTitle(shop.name)
                    .setMessage("Would you like to start turn-by-turn navigation to this coffee shop?")
                    .setPositiveButton("Navigate") { _, _ ->
                        val intent = TurnByTurnNavigationActivity.createIntent(
                            context = context,
                            destinationLat = shop.coordinate.latitude(),
                            destinationLng = shop.coordinate.longitude()
                        )
                        context.startActivity(intent)
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
