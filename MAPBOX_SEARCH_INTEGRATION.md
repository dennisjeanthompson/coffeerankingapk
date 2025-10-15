# Mapbox Search SDK Integration Guide

## Overview
This app now uses the official **Mapbox Search SDK 2.14.0** with the **Place Autocomplete API** to provide real-time search capabilities for finding cafes and other places around the world.

## Dependencies Added

### Mapbox Maps SDK
```gradle
implementation 'com.mapbox.maps:android-ndk27:11.9.0'
```
- **Version**: 11.9.0-ndk27 (upgraded from 10.16.5)
- **Purpose**: Map rendering, camera controls, location tracking
- **16 KB Page Size Support**: Uses `-ndk27` variant for Android 15+ compatibility

### Mapbox Search SDK Modules
```gradle
implementation 'com.mapbox.search:place-autocomplete-ndk27:2.14.0'
implementation 'com.mapbox.search:discover-ndk27:2.14.0'
implementation 'com.mapbox.search:mapbox-search-android-ndk27:2.14.0'
implementation 'com.mapbox.search:mapbox-search-android-ui-ndk27:2.14.0'
```

| Module | Purpose |
|--------|---------|
| `place-autocomplete` | Search as you type, coordinate-based search |
| `discover` | Category search (restaurants, cafes, POIs) |
| `mapbox-search-android` | Core search functionality, custom providers |
| `mapbox-search-android-ui` | Ready-to-use UI components |

## Features Implemented

### 1. Real-Time Place Search
- **As-you-type suggestions** from Mapbox's global database
- **Minimum 3 characters** required to trigger search
- **Top 5 results** displayed with name and formatted address
- **Coordinate-based results** with precise lat/lng

### 2. Hybrid Search Results
The search UI displays two sections:

#### Mapbox Search Results (Global)
- Real-time suggestions from Mapbox Places API
- Worldwide coverage
- Autocomplete functionality
- Detailed address information

#### Saved Cafes (Local)
- Your app's local cafe database
- Filtered by name, address, description
- Star ratings displayed
- Immediate access to app-specific data

### 3. Place Selection
When a user taps a Mapbox search result:
1. `placeAutocomplete.select(suggestion)` fetches full place details
2. Camera auto-animates to location with 16x zoom
3. Place name replaces search query
4. Search results hide

### 4. User Experience Enhancements
- **Loading indicator** while searching
- **No results state** with helpful message
- **Visual separation** between result types
- **Clear button** to reset search
- **Smooth animations** for camera movement

## Code Implementation

### Initialization
```kotlin
val placeAutocomplete = remember {
    MapboxOptions.accessToken = context.getString(R.string.mapbox_access_token)
    PlaceAutocomplete.create(locationProvider = null)
}
```

### Search Trigger
```kotlin
LaunchedEffect(searchQuery) {
    if (searchQuery.isNotEmpty() && searchQuery.length >= 3) {
        isSearching = true
        try {
            val response = placeAutocomplete.suggestions(query = searchQuery)
            if (response.isValue) {
                searchSuggestions = response.value.orEmpty()
                Log.i("MapScreen", "Found ${searchSuggestions.size} suggestions")
            } else {
                Log.e("MapScreen", "Error: ${response.error}")
                searchSuggestions = emptyList()
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Exception during search", e)
            searchSuggestions = emptyList()
        }
        isSearching = false
    } else {
        searchSuggestions = emptyList()
    }
}
```

### Place Selection Handler
```kotlin
modifier = Modifier.clickable {
    coroutineScope.launch {
        try {
            val result = placeAutocomplete.select(suggestion)
            result.onValue { placeResult ->
                Log.i("MapScreen", "Selected: ${placeResult.name}")
                
                placeResult.coordinate?.let { coordinate ->
                    mapView?.mapboxMap?.setCamera(
                        CameraOptions.Builder()
                            .center(coordinate)
                            .zoom(16.0)
                            .build()
                    )
                }
                
                showSearchResults = false
                searchQuery = placeResult.name
            }
            result.onError { error ->
                Log.e("MapScreen", "Selection error", error)
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Exception selecting", e)
        }
    }
}
```

## API Version Updates (v10 → v11)

### Deprecated API (v10)
```kotlin
mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
    // ...
}
```

### New API (v11)
```kotlin
mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
    // ...
}
```

**Changes:**
- `getMapboxMap()` → `mapboxMap` property
- `loadStyleUri()` → `loadStyle()`
- `setCamera()` remains the same

## Configuration Requirements

### 1. Mapbox Access Token
Add your public token to `app/src/main/res/values/strings.xml`:
```xml
<string name="mapbox_access_token">YOUR_MAPBOX_PUBLIC_TOKEN_HERE</string>
```

### 2. Mapbox Downloads Token
Add your secret token to `gradle.properties`:
```properties
MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_TOKEN_HERE
```

### 3. Maven Repository
Already configured in `settings.gradle`:
```gradle
maven {
    url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
    authentication {
        basic(BasicAuthentication)
    }
    credentials {
        username = "mapbox"
        password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").get()
    }
}
```

## Testing the Integration

### 1. Run the App
```bash
./gradlew installDebug
```

### 2. Test Search
1. Tap the **Map** tab in Coffee Lover screen
2. Grant location permissions
3. Type in search bar (e.g., "Starbucks New York")
4. Wait for results (after 3+ characters)
5. Tap a Mapbox result to see camera move
6. Verify place name appears in search bar

### 3. Check Logs
```bash
adb logcat | grep MapScreen
```

Expected logs:
- `Found X suggestions for: <query>`
- `Selected place: <name>`
- Errors are logged with full stack traces

## Search Result Object Structure

### PlaceAutocompleteSuggestion
```kotlin
data class PlaceAutocompleteSuggestion(
    val name: String,
    val formattedAddress: String?,
    val coordinate: Point?,
    val categories: List<String>?,
    val makiIcon: String?,
    // ...
)
```

### PlaceAutocompleteResult (after selection)
```kotlin
data class PlaceAutocompleteResult(
    val id: String,
    val mapboxId: String,
    val name: String,
    val coordinate: Point,
    val address: PlaceAutocompleteAddress,
    val type: PlaceAutocompleteType,
    val phone: String?,
    val website: String?,
    val reviewCount: Int?,
    val averageRating: Double?,
    val openHours: OpenHours?,
    val primaryPhotos: List<Photo>?,
    // ...
)
```

## Performance Considerations

### Build Optimization
Current `gradle.properties` settings for codespace:
```properties
org.gradle.jvmargs=-Xmx2560m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=false
org.gradle.workers.max=1
org.gradle.caching=true
```

### Search Throttling
- **Minimum 3 characters** prevents excessive API calls
- **LaunchedEffect** automatically cancels previous searches
- **Empty query** clears results immediately

### APK Size
- Maps SDK (11.9.0): ~40 MB
- Search SDK (2.14.0): ~15 MB
- **Total APK**: ~65 MB (debug build)

## Known Issues & Limitations

### 1. Rate Limiting
Free tier includes:
- **100,000 requests/month** for Place Autocomplete
- Exceeding limits returns error responses

### 2. Offline Support
- Search requires internet connection
- Offline module (`offline-ndk27`) available but not yet integrated
- Local cafes work offline

### 3. Search Scope
- Global search, no country/region filtering in current implementation
- Can be added using `PlaceAutocompleteOptions` with country codes

## Future Enhancements

### 1. Search Filtering
```kotlin
val options = PlaceAutocompleteOptions(
    limit = 10,
    countries = listOf("US", "CA"),
    proximity = Point.fromLngLat(userLng, userLat),
    types = listOf(PlaceAutocompleteType.Category.COFFEE)
)
placeAutocomplete.suggestions(query, options)
```

### 2. Category Search (Discover SDK)
```kotlin
val discover = PlaceAutocomplete.create()
val response = discover.search(
    "coffee",
    CategorySearchOptions(
        proximity = userLocation,
        radius = 5000 // 5km
    )
)
```

### 3. Reverse Geocoding
```kotlin
val result = placeAutocomplete.reverse(Point.fromLngLat(lng, lat))
```

### 4. Search History
```kotlin
implementation 'com.mapbox.search:history:2.14.0'
```

### 5. Custom Place Data
Integrate your cafe database with Mapbox results for unified search experience.

## Documentation Links

- [Mapbox Search SDK Documentation](https://docs.mapbox.com/android/search/guides/)
- [Place Autocomplete API](https://docs.mapbox.com/android/search/guides/place-autocomplete/)
- [Mapbox Maps SDK v11](https://docs.mapbox.com/android/maps/guides/)
- [Migration Guide (v10 → v11)](https://docs.mapbox.com/android/maps/guides/migrate-to-v11/)
- [API Reference](https://docs.mapbox.com/android/search/api/)

## Support

For issues or questions:
1. Check [Mapbox Community](https://github.com/mapbox/mapbox-search-android/discussions)
2. Review [GitHub Issues](https://github.com/mapbox/mapbox-search-android/issues)
3. Contact [Mapbox Support](https://support.mapbox.com/)

---

**Last Updated**: October 15, 2025  
**SDK Versions**: Maps 11.9.0, Search 2.14.0  
**minSdk**: 24 (Android 7.0+)  
**targetSdk**: 34 (Android 14)
