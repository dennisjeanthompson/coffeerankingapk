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
// TODO: Import Google Maps Compose components when implementing
// import com.google.maps.android.compose.GoogleMap
// import com.google.maps.android.compose.MapProperties
// import com.google.maps.android.compose.MapType
// import com.google.maps.android.compose.MapUiSettings

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
            // TODO: Implement Google Maps Compose
            // GoogleMap(
            //     modifier = Modifier.fillMaxSize(),
            //     properties = MapProperties(mapType = MapType.NORMAL),
            //     uiSettings = MapUiSettings(zoomControlsEnabled = false),
            //     onMapClick = { latLng ->
            //         selectedLocation = Pair(latLng.latitude, latLng.longitude)
            //     }
            // ) {
            //     selectedLocation?.let { location ->
            //         Marker(
            //             state = MarkerState(position = LatLng(location.first, location.second)),
            //             title = "Your Cafe Location"
            //         )
            //     }
            // }
            
            // Placeholder for map (remove when implementing real Google Maps)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Google Maps will be displayed here\n\nTODO: Add Google Maps API key to AndroidManifest.xml",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
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