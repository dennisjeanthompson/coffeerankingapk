package com.example.coffeerankingapk.ui.screens.lover

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.coffeerankingapk.R
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.util.SampleDataSeeder
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.example.coffeerankingapk.viewmodel.FavoritesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeShopMapScreen(
    onNavigateToRating: (String) -> Unit,
    viewModel: CoffeeShopViewModel = viewModel()
) {
    val context = LocalContext.current
    val coffeeShops by viewModel.coffeeShops.collectAsState()
    val selectedShop by viewModel.selectedShop.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    // Derive bottom sheet visibility directly from selectedShop - no separate state
    val showBottomSheet = selectedShop != null
    
    // Update bottom sheet visibility when shop is selected
    LaunchedEffect(selectedShop) {
        Log.d("CoffeeShopMapScreen", "Selected shop changed: ${selectedShop?.name}, showing sheet: $showBottomSheet")
        if (selectedShop != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    
    // Load data on screen start
    LaunchedEffect(Unit) {
        Log.d("CoffeeShopMapScreen", "Screen started, loading coffee shops...")
        viewModel.loadCoffeeShops()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // TASK 1: MapboxMap with coffee shops
        MapboxMapView(
            coffeeShops = coffeeShops,
            context = context,
            onMapViewCreated = { mapView = it },
            onShopClicked = { shop ->
                Log.d("CoffeeShopMapScreen", "onShopClicked callback triggered for: ${shop.name}")
                viewModel.selectShop(shop)
                Log.d("CoffeeShopMapScreen", "viewModel.selectShop called")
            }
        )
        
        // Search box at the top - IMPROVED UI/UX
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search coffee shops...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                
                // Shop count indicator - IMPROVED VISIBILITY
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty() && coffeeShops.isNotEmpty()) {
                            "Found ${coffeeShops.size} coffee shop${if (coffeeShops.size != 1) "s" else ""} matching \"$searchQuery\""
                        } else if (searchQuery.isNotEmpty() && coffeeShops.isEmpty()) {
                            "No coffee shops found matching \"$searchQuery\""
                        } else {
                            "${coffeeShops.size} coffee shop${if (coffeeShops.size != 1) "s" else ""} nearby"
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (searchQuery.isNotEmpty() && coffeeShops.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else if (searchQuery.isNotEmpty() && coffeeShops.isEmpty()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                // Filter mode chips: Strict / Flexible / All
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterMode == com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.StrictCoffeeOnly,
                        onClick = { viewModel.updateFilterMode(com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.StrictCoffeeOnly) },
                        label = { Text("Strict") }
                    )
                    FilterChip(
                        selected = filterMode == com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.FlexibleCafeAndCoffee,
                        onClick = { viewModel.updateFilterMode(com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.FlexibleCafeAndCoffee) },
                        label = { Text("Flexible") }
                    )
                    FilterChip(
                        selected = filterMode == com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.AllNearby,
                        onClick = { viewModel.updateFilterMode(com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel.FilterMode.AllNearby) },
                        label = { Text("All") }
                    )
                }
            }
        }
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Empty state helper (allows quick seeding)
        if (!isLoading && coffeeShops.isEmpty()) {
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "No coffee shops found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You can seed sample Baguio shops for testing.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        Log.d("CoffeeShopMapScreen", "Seeding sample data from empty-state button...")
                        SampleDataSeeder.seedSampleCoffeeShops()
                        Handler(Looper.getMainLooper()).postDelayed({ viewModel.loadCoffeeShops() }, 2000)
                    }) {
                        Text("Seed sample shops")
                    }
                }
            }
        }

        if (error != null) {
            AssistChip(
                onClick = { viewModel.clearError() },
                label = { Text(error ?: "") },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
        
        // Debug: Floating button to seed sample data
        FloatingActionButton(
            onClick = {
                Log.d("CoffeeShopMapScreen", "Seeding sample data...")
                SampleDataSeeder.seedSampleCoffeeShops()
                // Reload after seeding
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    viewModel.loadCoffeeShops()
                }, 2000)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sample Data")
        }
        
        // TASK 2: Bottom Sheet for selected shop
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.selectShop(null)
                },
                sheetState = sheetState
            ) {
                selectedShop?.let { shop ->
                    CoffeeShopBottomSheetContent(
                        shop = shop,
                        onRateClick = {
                            onNavigateToRating(shop.id)
                            viewModel.selectShop(null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MapboxMapView(
    coffeeShops: List<CoffeeShop>,
    context: Context,
    onMapViewCreated: (MapView) -> Unit,
    onShopClicked: (CoffeeShop) -> Unit
) {
    var mapReady by remember { mutableStateOf(false) }
    val createdMapView = remember { mutableStateOf<MapView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Runtime permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        Log.d("MapboxMapView", "Location permissions result: $hasLocationPermission")
        // Try enabling location again after result
        createdMapView.value?.let { mv ->
            if (hasLocationPermission) {
                try {
                    mv.location.updateSettings { enabled = true; pulsingEnabled = true }
                } catch (e: Exception) { Log.e("MapboxMapView", "Error enabling location after grant", e) }
            }
        }
    }

    // Check/request location permission
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        hasLocationPermission = fine || coarse
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    // Handle MapView lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            createdMapView.value?.let { mapView ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        // MapView lifecycle handled internally in newer versions
                    }
                    Lifecycle.Event.ON_STOP -> {
                        // MapView lifecycle handled internally in newer versions
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        // Will be cleaned up in onDispose
                    }
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).also { mapView ->
                createdMapView.value = mapView
                onMapViewCreated(mapView)
                
                mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                    mapReady = true
                    Log.d("MapboxMapView", "Map style loaded")
                    
                    // Optional: hide default POIs so only our coffee markers show
                    try {
                        // Common POI layers in Mapbox Streets styles
                        val candidatePoiLayers = listOf(
                            "poi-label", // primary POI labels/icons
                            "poi",       // some styles use this id
                            "poi-3d"     // defensive
                        )
                        candidatePoiLayers.forEach { layerId ->
                            try {
                                if (style.styleLayerExists(layerId)) {
                                    style.removeStyleLayer(layerId)
                                    Log.d("MapboxMapView", "Removed base POI layer: $layerId")
                                }
                            } catch (e: Exception) {
                                Log.w("MapboxMapView", "Couldn't remove POI layer $layerId", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("MapboxMapView", "POI hiding not applied", e)
                    }

                    // Enable user location (GPS)
                    try {
                        if (hasLocationPermission) {
                            mapView.location.updateSettings {
                                enabled = true
                                pulsingEnabled = true
                            }
                            Log.d("MapboxMapView", "User location enabled")
                        } else {
                            Log.w("MapboxMapView", "Location permission not granted; skipping enable")
                        }
                    } catch (e: Exception) {
                        Log.e("MapboxMapView", "Error enabling location", e)
                    }
                    
                    // Set initial camera position to Baguio City, Philippines
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(120.5960, 16.4023)) // Baguio coordinates
                            .zoom(12.0)
                            .build()
                    )
                    
                    // Add click listener for features
                    mapView.mapboxMap.addOnMapClickListener { point ->
                        Log.d("MapboxMapView", "Map clicked at: $point")
                        val screenCoordinate = mapView.mapboxMap.pixelForCoordinate(point)
                        val renderedQueryOptions = RenderedQueryOptions(
                            listOf("coffee-shops-layer"),
                            null
                        )
                        
                        mapView.mapboxMap.queryRenderedFeatures(
                            RenderedQueryGeometry(screenCoordinate),
                            renderedQueryOptions
                        ) { result ->
                            result.value?.let { features ->
                                Log.d("MapboxMapView", "Found ${features.size} features")
                                if (features.isNotEmpty()) {
                                    val feature = features.first()
                                    feature.queriedFeature.feature.getStringProperty("shopId")?.let { shopId ->
                                        Log.d("MapboxMapView", "Clicked shop: $shopId")
                                        val shop = coffeeShops.find { it.id == shopId }
                                        if (shop != null) {
                                            Log.d("MapboxMapView", "Opening bottom sheet for: ${shop.name}")
                                            // Ensure callback runs on main thread
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                onShopClicked(shop)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Load shops on map when ready
    LaunchedEffect(mapReady, coffeeShops) {
        Log.d("MapboxMapView", "LaunchedEffect triggered - mapReady: $mapReady, coffeeShops count: ${coffeeShops.size}")
        if (mapReady) {
            createdMapView.value?.mapboxMap?.getStyle { style ->
                Log.d("MapboxMapView", "Updating map markers with ${coffeeShops.size} shops...")
                loadCoffeeShopsOnMap(
                    style = style,
                    shops = coffeeShops,
                    context = context
                )
                
                // Auto-zoom to show all found shops
                if (coffeeShops.isNotEmpty()) {
                    zoomToShowShops(createdMapView.value, coffeeShops)
                }
            }
        }
    }
}

// TASK 1: Load coffee shops as GeoJSON on map
private fun loadCoffeeShopsOnMap(
    style: Style,
    shops: List<CoffeeShop>,
    context: Context
) {
    try {
        Log.d("MapboxMapView", "Loading ${shops.size} shops onto map")
        
        // Convert shops to GeoJSON features
        val features = shops.mapNotNull { shop ->
            shop.location?.let { geoPoint ->
                Log.d("MapboxMapView", "Adding shop: ${shop.name} at lat=${geoPoint.latitude}, lng=${geoPoint.longitude}")
                Feature.fromGeometry(
                    Point.fromLngLat(geoPoint.longitude, geoPoint.latitude)
                ).apply {
                    addStringProperty("shopId", shop.id)
                    addStringProperty("name", shop.name)
                    addNumberProperty("averageRating", shop.averageRating)
                    addNumberProperty("totalRatings", shop.totalRatings)
                }
            } ?: run {
                Log.w("MapboxMapView", "Shop ${shop.name} has no location - skipping")
                null
            }
        }
        
        Log.d("MapboxMapView", "Created ${features.size} GeoJSON features from ${shops.size} shops")
        val featureCollection = FeatureCollection.fromFeatures(features)
        
        val sourceId = "coffee-shops-source"
        val layerId = "coffee-shops-layer"

        // Always clear previous layer/source so updates are reliable
        if (style.styleLayerExists(layerId)) {
            try { style.removeStyleLayer(layerId) } catch (e: Exception) { Log.w("MapboxMapView", "Couldn't remove layer", e) }
        }
        if (style.styleSourceExists(sourceId)) {
            try { style.removeStyleSource(sourceId) } catch (e: Exception) { Log.w("MapboxMapView", "Couldn't remove source", e) }
        }

        if (features.isEmpty()) {
            Log.d("MapboxMapView", "No features to add; cleared previous markers")
            return
        }

        Log.d("MapboxMapView", "Creating new GeoJSON source and layer")

        // Load custom marker icon - convert vector drawable to bitmap
        var iconLoaded = false
        if (style.getStyleImage("coffee-icon") == null) {
            try {
                // Convert vector drawable to bitmap
                val vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.coffee_marker)
                if (vectorDrawable != null) {
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        96, 96, 
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    vectorDrawable.draw(canvas)
                    style.addImage("coffee-icon", bitmap)
                    iconLoaded = true
                    Log.d("MapboxMapView", "Added coffee marker icon (vector converted to bitmap)")
                }
            } catch (e: Exception) {
                Log.e("MapboxMapView", "Error loading coffee icon", e)
            }
        } else {
            iconLoaded = true
            Log.d("MapboxMapView", "Coffee icon already exists in style")
        }

        // Fallback: create a simple circle marker if icon failed
        if (!iconLoaded) {
            try {
                val size = 64
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                val paint = android.graphics.Paint()
                paint.color = android.graphics.Color.parseColor("#8B4513") // Brown
                paint.isAntiAlias = true
                paint.style = android.graphics.Paint.Style.FILL
                // Draw circle
                canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
                // Draw white border
                paint.color = android.graphics.Color.WHITE
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
                style.addImage("coffee-icon", bitmap)
                Log.d("MapboxMapView", "Added fallback circle marker")
            } catch (e: Exception) {
                Log.e("MapboxMapView", "Failed to create fallback marker", e)
            }
        }

        // Add GeoJSON source
        style.addSource(
            geoJsonSource(sourceId) {
                featureCollection(featureCollection)
            }
        )
        Log.d("MapboxMapView", "Added GeoJSON source with ${features.size} features")

        // Add Symbol layer with enhanced visibility
        style.addLayer(
            symbolLayer(layerId, sourceId) {
                iconImage("coffee-icon")
                iconAnchor(IconAnchor.CENTER)
                iconSize(1.0) // Increased from 0.5 for better visibility
                iconAllowOverlap(true)
                iconIgnorePlacement(true) // Force display even if crowded
            }
        )
        Log.d("MapboxMapView", "Added symbol layer - ${features.size} markers should now be visible!")
        
        Log.d("MapboxMapView", "Loaded ${shops.size} coffee shops on map")
        
    } catch (e: Exception) {
        Log.e("MapboxMapView", "Error loading shops on map", e)
    }
}

@Composable
fun CoffeeShopBottomSheetContent(
    shop: CoffeeShop,
    onRateClick: () -> Unit,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isFavorite by favoritesViewModel.isFavorite(shop.id).collectAsState(initial = false)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with title and favorite button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = {
                    scope.launch {
                        if (isFavorite) {
                            favoritesViewModel.removeFavorite(shop.id)
                            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            favoritesViewModel.addFavorite(
                                shopId = shop.id,
                                shopName = shop.name,
                                shopAddress = shop.address,
                                averageRating = shop.averageRating.toFloat()
                            )
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) PrimaryBrown else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "★ ${String.format("%.1f", shop.averageRating)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " (${shop.totalRatings} ratings)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        if (shop.address.isNotEmpty()) {
            Text(
                text = shop.address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (shop.description.isNotEmpty()) {
            Text(
                text = shop.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = onRateClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Rate this shop")
        }
    }
}

// Auto-zoom camera to show all found coffee shops
private fun zoomToShowShops(mapView: MapView?, shops: List<CoffeeShop>) {
    if (mapView == null || shops.isEmpty()) return
    
    try {
        val points = shops.mapNotNull { shop ->
            shop.location?.let { geoPoint ->
                Point.fromLngLat(geoPoint.longitude, geoPoint.latitude)
            }
        }
        
        if (points.isEmpty()) return
        
        if (points.size == 1) {
            // Single shop: zoom to it
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(points[0])
                    .zoom(15.0)
                    .build()
            )
            Log.d("MapboxMapView", "Zoomed to single shop")
        } else {
            // Multiple shops: fit all in view
            val camera = mapView.mapboxMap.cameraForCoordinates(
                points,
                com.mapbox.maps.EdgeInsets(100.0, 100.0, 100.0, 100.0),
                null,
                null
            )
            mapView.mapboxMap.setCamera(camera)
            Log.d("MapboxMapView", "Zoomed to ${points.size} shops")
        }
    } catch (e: Exception) {
        Log.e("MapboxMapView", "Error zooming to shops", e)
    }
}
