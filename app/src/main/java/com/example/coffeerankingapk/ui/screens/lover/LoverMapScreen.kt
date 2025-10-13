package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.MockData
import com.example.coffeerankingapk.ui.components.CafeListItem
import com.example.coffeerankingapk.ui.theme.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
// MapTiler integration using OSMDroid
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverMapScreen(
    onCafeClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showMapView by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Find Cafes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    IconButton(onClick = { /* Center on user location */ }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "My Location",
                            tint = PrimaryBrown
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search cafes, locations...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // View toggle
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        onClick = { showMapView = true },
                        label = { Text("Map View") },
                        selected = showMapView,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBrown,
                            selectedLabelColor = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilterChip(
                        onClick = { showMapView = false },
                        label = { Text("List View") },
                        selected = !showMapView,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBrown,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        
        if (showMapView) {
            // MapTiler Map View using OSMDroid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        Configuration.getInstance().userAgentValue = context.packageName
                        
                        MapView(context).apply {
                            // Set MapTiler tile source
                            setTileSource(object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                                "MapTiler",
                                0, 18, 256, ".png",
                                arrayOf("https://api.maptiler.com/maps/streets-v2/")
                            ) {
                                override fun getTileURLString(pMapTileIndex: Long): String {
                                    val zoom = org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex)
                                    val x = org.osmdroid.util.MapTileIndex.getX(pMapTileIndex)
                                    val y = org.osmdroid.util.MapTileIndex.getY(pMapTileIndex)
                                    return "https://api.maptiler.com/maps/streets-v2/$zoom/$x/$y.png?key=301m71fkixa7RnnP0FaL"
                                }
                            })
                            
                            setMultiTouchControls(true)
                            controller.setZoom(12.0)
                            controller.setCenter(GeoPoint(37.7749, -122.4194))
                        }
                    }
                )
            }
            
            // Map controls overlay
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "${MockData.cafes.size} cafes found",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryBrown,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Cafe List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (showMapView) "Nearby Results" else "All Cafes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(
                MockData.cafes.filter { 
                    searchQuery.isBlank() || 
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
                }
            ) { cafe ->
                CafeListItem(
                    cafe = cafe,
                    onClick = { onCafeClick(cafe.id) },
                    showDistance = showMapView
                )
            }
            
            if (MockData.cafes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cafes found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}