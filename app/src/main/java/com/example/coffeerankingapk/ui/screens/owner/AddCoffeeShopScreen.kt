package com.example.coffeerankingapk.ui.screens.owner

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.R
import com.example.coffeerankingapk.viewmodel.AddCoffeeShopViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCoffeeShopScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCoffeeShopViewModel = viewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    LaunchedEffect(success) {
        if (success) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Coffee Shop") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (currentUser == null) {
                Text(
                    text = "You must be logged in to add a coffee shop",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
                return@Scaffold
            }
            
            Text(
                text = "Fill in the details of your coffee shop",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Shop Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Shop Name *") },
                placeholder = { Text("e.g., Baguio Coffee House") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                isError = formState.nameError != null
            )
            if (formState.nameError != null) {
                Text(
                    text = formState.nameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Type
            OutlinedTextField(
                value = formState.type,
                onValueChange = { viewModel.updateType(it) },
                label = { Text("Type") },
                placeholder = { Text("e.g., Coffee Shop, Café") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )
            
            // Address
            OutlinedTextField(
                value = formState.address,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("Address *") },
                placeholder = { Text("e.g., Session Road, Baguio City") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                isError = formState.addressError != null
            )
            if (formState.addressError != null) {
                Text(
                    text = formState.addressError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                placeholder = { Text("Describe your coffee shop...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp),
                maxLines = 4
            )
            
            // Location Picker Section
            Text(
                text = "Select Location on Map *",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Tap on the map to set your coffee shop location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (formState.latitude != null && formState.longitude != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Location: ${String.format("%.4f", formState.latitude)}, ${String.format("%.4f", formState.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (formState.locationError != null) {
                Text(
                    text = formState.locationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Map for location picking
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LocationPickerMap(
                    onLocationSelected = { lat, lng ->
                        viewModel.updateLocation(lat, lng)
                    },
                    selectedLatitude = formState.latitude,
                    selectedLongitude = formState.longitude,
                    onMapViewCreated = { mapView = it }
                )
            }
            
            // Image URL (optional)
            OutlinedTextField(
                value = formState.imageUrl,
                onValueChange = { viewModel.updateImageUrl(it) },
                label = { Text("Image URL (optional)") },
                placeholder = { Text("https://...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Error message
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            
            // Submit button
            Button(
                onClick = {
                    viewModel.saveCoffeeShop(currentUser.uid)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Add Coffee Shop")
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LocationPickerMap(
    onLocationSelected: (Double, Double) -> Unit,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    onMapViewCreated: (MapView) -> Unit
) {
    val context = LocalContext.current
    var mapReady by remember { mutableStateOf(false) }
    val createdMapView = remember { mutableStateOf<MapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    
    // Runtime permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    // Check permission
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
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                createdMapView.value = this
                onMapViewCreated(this)
                
                mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                    mapReady = true
                    
                    // Set initial camera to Baguio City
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(120.5960, 16.4023))
                            .zoom(13.0)
                            .build()
                    )
                    
                    // Add click listener to select location
                    mapboxMap.addOnMapClickListener { point ->
                        onLocationSelected(point.latitude(), point.longitude())
                        true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Update marker when location is selected
    LaunchedEffect(mapReady, selectedLatitude, selectedLongitude) {
        if (mapReady && selectedLatitude != null && selectedLongitude != null) {
            createdMapView.value?.mapboxMap?.getStyle { style ->
                updateLocationMarker(style, selectedLatitude, selectedLongitude, context)
            }
        }
    }
}

private fun updateLocationMarker(
    style: Style,
    latitude: Double,
    longitude: Double,
    context: android.content.Context
) {
    val sourceId = "selected-location-source"
    val layerId = "selected-location-layer"
    
    // Remove previous marker
    if (style.styleLayerExists(layerId)) {
        try { style.removeStyleLayer(layerId) } catch (_: Exception) {}
    }
    if (style.styleSourceExists(sourceId)) {
        try { style.removeStyleSource(sourceId) } catch (_: Exception) {}
    }
    
    // Create marker icon
    if (style.getStyleImage("location-pin-icon") == null) {
        try {
            val vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.coffee_marker)
            if (vectorDrawable != null) {
                val bitmap = android.graphics.Bitmap.createBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                vectorDrawable.draw(canvas)
                style.addImage("location-pin-icon", bitmap)
            }
        } catch (_: Exception) {
            // Create fallback marker
            val size = 64
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.RED
            paint.isAntiAlias = true
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
            style.addImage("location-pin-icon", bitmap)
        }
    }
    
    // Add new marker
    val feature = com.mapbox.geojson.Feature.fromGeometry(
        Point.fromLngLat(longitude, latitude)
    )
    val featureCollection = com.mapbox.geojson.FeatureCollection.fromFeatures(listOf(feature))
    
    style.addSource(
        geoJsonSource(sourceId) {
            featureCollection(featureCollection)
        }
    )
    
    style.addLayer(
        symbolLayer(layerId, sourceId) {
            iconImage("location-pin-icon")
            iconAnchor(IconAnchor.CENTER)
            iconSize(1.2)
            iconAllowOverlap(true)
        }
    )
}
