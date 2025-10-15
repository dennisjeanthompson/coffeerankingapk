package com.example.coffeerankingapk.ui.screens.lover

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
// TomTom SDK imports
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.search.SearchCallback
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.location.GeoPoint

// Data models for UI state
data class CoffeeShopResult(
    val name: String,
    val address: String,
    val position: GeoPoint,
    val distance: Double? = null
)

data class MapRoute(
    val lengthInMeters: Int,
    val travelTimeInSeconds: Int,
    val points: List<GeoPoint>
)


// TomTom Map Screen with Search and Routing using REST APIs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverMapScreen(
    onCafeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TomTomMapViewModel = viewModel(
        factory = TomTomMapViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    
    // Runtime permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        println("🔐 Permission result: $isGranted")
        if (isGranted) {
            println("✅ Permission granted, loading location")
            viewModel.loadUserLocation()
        } else {
            println("❌ Permission denied")
            viewModel.showPermissionDenied()
        }
    }
    
    // Check and request permission on mount
    LaunchedEffect(key1 = true) {
        println("🚀 LoverMapScreen LaunchedEffect started")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        println("📱 Has location permission: $hasPermission")
        
        if (hasPermission) {
            println("✅ Permission already granted, loading location...")
            viewModel.loadUserLocation()
        } else {
            println("📱 Requesting location permission...")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas with grid and markers
        Canvas(modifier = Modifier.fillMaxSize()) {
            println("🎨 Canvas drawing - size: ${size.width}x${size.height}, userLocation: ${uiState.userLocation}, results: ${uiState.searchResults.size}")
            
            // Background - light blue like water/sky
            drawRect(Color(0xFFE3F2FD))
            
            // Draw grid lines for map appearance
            val gridSpacing = 80.dp.toPx()
            val gridColor = Color(0xFFBBDEFB)
            
            // Vertical lines
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            
            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }
            
            // Center point (map center indicator)
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(
                color = Color(0x33000000),
                radius = 12f,
                center = center
            )
            
            // TODO: Draw route when routing is implemented
            // uiState.currentRoute?.let { route -> ... }
            
            // Draw search results (coffee shops)
            uiState.searchResults.forEach { result ->
                val pos = latLonToScreen(
                    result.position.latitude, result.position.longitude,
                    uiState.userLocation?.latitude ?: 0.0,
                    uiState.userLocation?.longitude ?: 0.0,
                    size
                )
                
                // Only draw if within visible bounds
                if (pos.x in 0f..size.width && pos.y in 0f..size.height) {
                    // Coffee marker with shadow
                    drawCircle(
                        color = Color(0x44000000),
                        radius = 26f,
                        center = pos.copy(x = pos.x + 2, y = pos.y + 2)
                    )
                    drawCircle(
                        color = Color(0xFF6D4C41),
                        radius = 24f,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 18f,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFF6D4C41),
                        radius = 12f,
                        center = pos
                    )
                }
            }
            
            // Draw user location (always at center or specified location)
            uiState.userLocation?.let { location ->
                val pos = Offset(size.width / 2, size.height / 2) // Always center for user
                
                // Outer pulse circle
                drawCircle(
                    color = Color(0x332196F3),
                    radius = 40f,
                    center = pos
                )
                
                // Main location marker
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 20f,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 10f,
                    center = pos
                )
            }
        }
        
        // Debug Text Overlay on top of canvas
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = if (uiState.userLocation != null) {
                    "📍 Location: ${String.format("%.4f", uiState.userLocation!!.latitude)}, ${String.format("%.4f", uiState.userLocation!!.longitude)}"
                } else {
                    if (uiState.isLoading) "⏳ Loading location..." else "❌ No location - tap location button"
                },
                color = if (uiState.userLocation != null) Color(0xFF4CAF50) else Color(0xFFFFC107),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            if (uiState.searchResults.isNotEmpty()) {
                Text(
                    text = "☕ ${uiState.searchResults.size} coffee shops found",
                    color = Color(0xFF00BCD4),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.errorMessage != null) {
                Text(
                    text = "⚠️ ${uiState.errorMessage}",
                    color = Color(0xFFF44336),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Search UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp)),
                placeholder = { Text("Search coffee shops...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.search(searchQuery)
                                showSearchResults = true
                            }) {
                                Icon(Icons.Default.Send, "Search", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = {
                            viewModel.searchCoffeeShops()
                            showSearchResults = true
                        }) {
                            Icon(Icons.Default.Star, "Coffee shops", tint = Color(0xFF6D4C41))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info text when no location
            if (uiState.userLocation == null && !uiState.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Info",
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tap the location button to see nearby coffee shops",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Show result count
            if (uiState.searchResults.isNotEmpty() && !showSearchResults) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSearchResults = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${uiState.searchResults.size} locations found",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Show results",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Search results
            if (showSearchResults && uiState.searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn {
                        items(uiState.searchResults) { result ->
                            SearchResultItem(
                                result = result,
                                onClick = {
                                    viewModel.selectLocation(result.position)
                                    showSearchResults = false
                                },
                                onRouteClick = {
                                    viewModel.calculateRoute(result.position)
                                    showSearchResults = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.loadUserLocation() },
                containerColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.LocationOn, "My location")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.currentRoute != null) {
                FloatingActionButton(
                    onClick = { viewModel.clearRoute() },
                    containerColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Clear, "Clear route")
                }
            }
        }
        
        // Route info - commented out until routing is implemented
        /*
        uiState.currentRoute?.let { route ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .widthIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Route Info",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Distance and time info",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        */
        
        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: CoffeeShopResult,
    onClick: () -> Unit,
    onRouteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            tint = Color(0xFF6D4C41),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                result.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                result.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            result.distance?.let { distance ->
                Text(
                    "${(distance / 1000).toInt()} km away",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6D4C41),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IconButton(onClick = onRouteClick) {
            Icon(Icons.Default.ArrowForward, "Route", tint = MaterialTheme.colorScheme.primary)
        }
    }
    Divider()
}

private fun latLonToScreen(
    lat: Double,
    lon: Double,
    centerLat: Double,
    centerLon: Double,
    size: androidx.compose.ui.geometry.Size
): Offset {
    val scale = 100000f
    val x = size.width / 2 + ((lon - centerLon) * scale).toFloat()
    val y = size.height / 2 - ((lat - centerLat) * scale).toFloat()
    return Offset(x, y)
}

// ViewModel
data class TomTomMapUiState(
    val userLocation: GeoPoint? = null,
    val searchResults: List<CoffeeShopResult> = emptyList(),
    val currentRoute: MapRoute? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TomTomMapViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(TomTomMapUiState())
    val uiState: StateFlow<TomTomMapUiState> = _uiState
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val apiKey = try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        appInfo.metaData?.getString("com.tomtom.sdk.api.key") ?: ""
    } catch (e: Exception) {
        ""
    }
    
    // Initialize TomTom Search SDK
    private val searchApi: OnlineSearch = OnlineSearch.create(context, apiKey)
    
    fun showPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Location permission is required to use the map. Please grant permission in settings."
        )
    }
    
    fun loadUserLocation() {
        println("🗺️ loadUserLocation called")
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("⚠️ Location permission not granted")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Location permission required"
            )
            return
        }
        
        println("✅ Location permission granted, requesting location...")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { loc -> 
                            println("🗺️ Location received: $loc")
                            continuation.resume(loc) 
                        }
                        .addOnFailureListener { e -> 
                            println("❌ Location error: ${e.message}")
                            continuation.resumeWithException(e) 
                        }
                }
                if (location != null) {
                    println("✅ Setting user location: ${location.latitude}, ${location.longitude}")
                    _uiState.value = _uiState.value.copy(
                        userLocation = GeoPoint(location.latitude, location.longitude),
                        isLoading = false
                    )
                } else {
                    println("⚠️ Location is null")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Location not available. Try enabling location services."
                    )
                }
            } catch (e: Exception) {
                println("❌ Exception: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Location error: ${e.message}"
                )
            }
        }
    }
    
    fun search(query: String) {
        println("🔍 search called with query: $query")
        if (apiKey.isEmpty()) {
            println("⚠️ API key is empty")
            _uiState.value = _uiState.value.copy(
                errorMessage = "TomTom API key not configured"
            )
            return
        }
        println("✅ API key available: ${apiKey.take(10)}...")
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        val options = SearchOptions(
            query = query,
            geoBias = _uiState.value.userLocation,
            limit = 10
        )
        
        println("🌐 Making TomTom SDK search call...")
        searchApi.search(options, object : SearchCallback {
            override fun onSuccess(result: SearchResponse) {
                println("✅ Search results: ${result.results.size} found")
                val coffeeShops = result.results.map { searchResult ->
                    CoffeeShopResult(
                        name = searchResult.poi?.names?.firstOrNull() ?: searchResult.poi?.categories?.firstOrNull()?.name ?: "Unknown",
                        address = searchResult.address?.freeformAddress ?: "",
                        position = searchResult.mapCoordinate,
                        distance = searchResult.detourTime?.inWholeSeconds?.toDouble()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    searchResults = coffeeShops,
                    isLoading = false
                )
            }

            override fun onFailure(failure: SearchFailure) {
                println("❌ Search failed: ${failure.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Search failed: ${failure.message}"
                )
            }
        })
    }
    
    fun searchCoffeeShops() {
        println("☕ searchCoffeeShops called")
        if (apiKey.isEmpty()) {
            println("⚠️ API key is empty")
            _uiState.value = _uiState.value.copy(
                errorMessage = "TomTom API key not configured"
            )
            return
        }
        
        val location = _uiState.value.userLocation
        if (location == null) {
            println("⚠️ User location not available")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Location not available. Please enable location."
            )
            return
        }
        println("✅ Searching near: ${location.latitude}, ${location.longitude}")
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        val options = SearchOptions(
            query = "coffee",
            geoBias = location,
            limit = 20
        )
        
        println("🌐 Making TomTom SDK category search call...")
        searchApi.search(options, object : SearchCallback {
            override fun onSuccess(result: SearchResponse) {
                println("✅ Coffee shops found: ${result.results.size}")
                val coffeeShops = result.results.map { searchResult ->
                    CoffeeShopResult(
                        name = searchResult.poi?.names?.firstOrNull() ?: searchResult.poi?.categories?.firstOrNull()?.name ?: "Unknown Coffee Shop",
                        address = searchResult.address?.freeformAddress ?: "",
                        position = searchResult.mapCoordinate,
                        distance = searchResult.detourTime?.inWholeSeconds?.toDouble()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    searchResults = coffeeShops,
                    isLoading = false
                )
            }

            override fun onFailure(failure: SearchFailure) {
                println("❌ Coffee shop search failed: ${failure.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Coffee shop search failed: ${failure.message}"
                )
            }
        })
    }
    
    // TODO: Implement routing with TomTom Routing SDK
    fun calculateRoute(destination: GeoPoint) {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Routing feature coming soon!"
        )
    }
    
    fun selectLocation(position: GeoPoint) {
        // Center map on selected location
        _uiState.value = _uiState.value.copy(userLocation = position)
    }
    
    fun clearRoute() {
        _uiState.value = _uiState.value.copy(currentRoute = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class TomTomMapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TomTomMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TomTomMapViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}