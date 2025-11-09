# Coffee Shop Rating System - Implementation Guide

## Overview
This implementation provides a complete coffee shop rating system with:
- Mapbox Maps integration with GeoJSON rendering
- Custom coffee cup markers
- Interactive bottom sheet for shop selection
- Transaction-based rating submissions (prevents race conditions)
- Real-time Firestore integration

## Architecture

### Data Layer
- **CoffeeShop.kt**: Data model with GeoPoint location, averageRating, totalRatings
- **CoffeeShopRepository.kt**: Firestore CRUD operations with transaction-based rating
- **CoffeeShopViewModel.kt**: StateFlow-based reactive state management

### UI Layer
- **CoffeeShopMapScreen.kt**: Mapbox map with GeoJSON markers and bottom sheet
- **RatingScreen.kt**: Star rating UI with slider for fine-tuned ratings (1.0 - 5.0)

### Navigation
- `coffee_shop_map` → Main map screen
- `coffee_shop_rating/{shopId}` → Rating screen for specific shop

## Firestore Structure

```
coffeeShops (collection)
  ├── {shopId} (document)
  │   ├── name: String
  │   ├── type: "coffee shop"
  │   ├── location: GeoPoint (latitude, longitude)
  │   ├── averageRating: Double
  │   ├── totalRatings: Int
  │   ├── description: String (optional)
  │   ├── address: String (optional)
  │   └── ratings (subcollection)
  │       └── {ratingId} (document)
  │           ├── userId: String
  │           ├── rating: Double
  │           ├── timestamp: Long
  │           └── comment: String (optional)
```

## Setup Instructions

### 1. Add Sample Data to Firestore

To test the system, you can seed sample coffee shops:

```kotlin
// In MainActivity.onCreate() or a debug screen
import com.example.coffeerankingapk.util.SampleDataSeeder

// Run once to populate Firestore
SampleDataSeeder.seedSampleCoffeeShops()
```

This adds 5 sample coffee shops in San Francisco with varying ratings.

### 2. Navigate to Coffee Shop Map

From your navigation graph:

```kotlin
navController.navigate("coffee_shop_map")
```

Or add a button in your existing UI:

```kotlin
Button(onClick = { navController.navigate("coffee_shop_map") }) {
    Text("View Coffee Shops")
}
```

### 3. Mapbox Configuration

Ensure your Mapbox access token is configured in:
- `res/values/strings.xml` → `mapbox_access_token`
- `AndroidManifest.xml` → `<meta-data>` tag

## How It Works

### Task 1: Map Display with GeoJSON
1. `CoffeeShopViewModel.loadCoffeeShops()` fetches all documents where `type == "coffee shop"`
2. `MapboxMapView` converts List<CoffeeShop> to GeoJSON FeatureCollection
3. Each feature has properties: shopId, name, averageRating, totalRatings
4. Custom coffee marker icon (`coffee_marker.xml`) is loaded into Mapbox style
5. GeoJsonSource + SymbolLayer renders markers on map

### Task 2: Marker Clicks & Bottom Sheet
1. `addOnMapClickListener` detects taps on map
2. `queryRenderedFeatures` checks if tap hit a marker in "coffee-shops-layer"
3. Extracts `shopId` from feature properties
4. Updates `viewModel.selectedShop` StateFlow
5. ModalBottomSheet observes `selectedShop` and displays shop info
6. "Rate this Shop" button navigates to rating screen

### Task 3: Transaction-Based Rating Submission
1. User selects rating (1.0 - 5.0) using stars + slider
2. Submit button calls `viewModel.submitRating(shopId, userId, rating)`
3. **Repository uses Firestore Transaction**:
   ```kotlin
   db.runTransaction { transaction ->
       val snapshot = transaction.get(shopRef)
       val oldAvg = snapshot.getDouble("averageRating") ?: 0.0
       val oldTotal = snapshot.getLong("totalRatings")?.toInt() ?: 0
       val newTotal = oldTotal + 1
       val newAvg = ((oldAvg * oldTotal) + newRatingValue) / newTotal
       
       transaction.update(shopRef, "averageRating", newAvg)
       transaction.update(shopRef, "totalRatings", newTotal)
       transaction.set(ratingRef, ratingData)
   }
   ```
4. Transaction ensures atomic updates (no race conditions if multiple users rate simultaneously)
5. New rating document created in `ratings` subcollection
6. UI reloads shop data and navigates back

## Testing

### Manual Testing
1. Run the app and navigate to `coffee_shop_map`
2. Map should display 5 coffee shops around San Francisco
3. Tap a marker → bottom sheet appears with shop details
4. Tap "Rate this Shop" → rating screen appears
5. Select rating with stars or slider
6. Submit → transaction updates Firestore
7. Navigate back → map shows updated rating

### Verify Transactions Work
1. Open Firestore Console in Firebase
2. Watch `coffeeShops/{shopId}` document
3. Submit a rating
4. Verify:
   - `totalRatings` incremented by 1
   - `averageRating` recalculated correctly
   - New document in `ratings` subcollection

### Formula Verification
If shop has:
- averageRating = 4.0
- totalRatings = 10
- New rating = 5.0

Expected result:
```
newTotal = 10 + 1 = 11
newAvg = ((4.0 * 10) + 5.0) / 11 = 45.0 / 11 = 4.09
```

## Customization

### Change Map Initial Position
Edit `CoffeeShopMapScreen.kt`:
```kotlin
mapView.mapboxMap.setCamera(
    CameraOptions.Builder()
        .center(Point.fromLngLat(-122.4194, 37.7749)) // Your coordinates
        .zoom(12.0)
        .build()
)
```

### Custom Marker Icon
Replace `res/drawable/coffee_marker.xml` with your own vector drawable or bitmap.

### Add User Location
Add to `CoffeeShopMapScreen.kt`:
```kotlin
import com.mapbox.maps.plugin.locationcomponent.location

mapView.location.updateSettings {
    enabled = true
    pulsingEnabled = true
}
```

### Filter by Distance
Modify `CoffeeShopRepository.getAllCoffeeShops()` to filter by radius:
```kotlin
// Add GeoFirestore dependency first
val center = GeoPoint(userLat, userLng)
val radiusKm = 5.0

coffeeShops.filter { shop ->
    shop.location?.let { 
        calculateDistance(center, it) <= radiusKm 
    } ?: false
}
```

## Troubleshooting

### Markers Don't Appear
1. Check Mapbox token is valid
2. Verify Firestore data has `type = "coffee shop"`
3. Check logcat for "Loaded X coffee shops on map"
4. Ensure GeoPoint values are valid (lat: -90 to 90, lng: -180 to 180)

### Bottom Sheet Not Showing
1. Verify `viewModel.selectedShop` is not null
2. Check `showBottomSheet` state updates
3. Ensure clicked feature has `shopId` property

### Rating Not Saving
1. Check Firebase Auth user is logged in
2. Verify Firestore security rules allow writes
3. Check logcat for transaction errors
4. Ensure `shopId` is valid

### Transaction Fails
1. Check internet connectivity
2. Verify Firestore indexes are created
3. Ensure document exists before rating
4. Check Firebase Console → Firestore → Rules

## Security Rules

Add to Firestore Rules:

```javascript
match /coffeeShops/{shopId} {
  allow read: if true; // Public read
  allow write: if request.auth != null; // Authenticated writes only
  
  match /ratings/{ratingId} {
    allow read: if true;
    allow create: if request.auth != null 
                  && request.resource.data.userId == request.auth.uid;
    allow update, delete: if request.auth.uid == resource.data.userId;
  }
}
```

## Performance Optimization

### Pagination
For large datasets, implement pagination in repository:
```kotlin
suspend fun getCoffeeShopsPaginated(lastVisible: DocumentSnapshot?, limit: Long = 20)
```

### Caching
ViewModel already caches in StateFlow. For offline support:
```kotlin
// In Repository
db.collection("coffeeShops")
    .whereEqualTo("type", "coffee shop")
    .get(Source.CACHE) // Use cache first
```

### Index Clusters
For many markers, use Mapbox clustering:
```kotlin
geoJsonSource(sourceId) {
    featureCollection(featureCollection)
    cluster(true)
    clusterRadius(50)
    clusterMaxZoom(14)
}
```

## Next Steps

1. **Search**: Add Mapbox Search SDK integration
2. **Filters**: Filter by rating, distance, open now
3. **Photos**: Add shop photos to Firestore + display in bottom sheet
4. **User Profile**: Show user's past ratings
5. **Leaderboard**: Top-rated shops
6. **Notifications**: Alert when favorite shop gets new rating

## API Reference

### CoffeeShopViewModel
- `coffeeShops: StateFlow<List<CoffeeShop>>` - All shops
- `selectedShop: StateFlow<CoffeeShop?>` - Currently selected shop
- `isLoading: StateFlow<Boolean>` - Loading state
- `loadCoffeeShops()` - Fetch from Firestore
- `selectShop(shop)` - Trigger bottom sheet
- `submitRating(shopId, userId, rating)` - Submit rating transaction

### CoffeeShopRepository
- `getAllCoffeeShops(): Result<List<CoffeeShop>>`
- `getCoffeeShop(shopId): Result<CoffeeShop?>`
- `submitRating(shopId, userId, rating): Result<Unit>`
- `getShopRatings(shopId): Result<List<Rating>>`
- `addCoffeeShop(shop): Result<String>`

## Credits

Built with:
- Jetpack Compose
- Firebase Firestore
- Mapbox Maps SDK v11.9.0
- Kotlin Coroutines + StateFlow
