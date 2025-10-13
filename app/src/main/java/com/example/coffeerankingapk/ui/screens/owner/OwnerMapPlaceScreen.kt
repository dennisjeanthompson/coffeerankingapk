package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.MapPlacePin
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
// MapTiler integration using OSMDroid
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OwnerMapPlaceScreen(
    onLocationConfirmed: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    Scaffold(
        containerColor = BgCream,
        floatingActionButton = {
            if (selectedLocation != null) {
                FloatingActionButton(
                    onClick = onLocationConfirmed,
                    containerColor = PrimaryBrown,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Confirm location",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // MapTiler Map using OSMDroid
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // Configure OSMDroid
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
                        controller.setCenter(GeoPoint(37.7749, -122.4194)) // San Francisco
                        
                        // Add click listener
                        setOnClickListener { _ ->
                            // Handle map clicks
                        }
                    }
                }
            )
            
            // Instruction overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Place your cafe on the map",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
            
            // Center crosshair
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MapPlacePin()
            }
        }
    }
}