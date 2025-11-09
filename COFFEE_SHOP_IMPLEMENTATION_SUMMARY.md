# Coffee Shop Rating System - Quick Start

## What Was Implemented

✅ **Complete 3-Task Implementation**

### Task 1: Mapbox Map with GeoJSON
- `CoffeeShopMapScreen.kt` - Mapbox composable with coffee shop markers
- Converts Firestore data to GeoJSON FeatureCollection
- Custom coffee cup marker icon (`coffee_marker.xml`)
- Displays all shops with `type = "coffee shop"`

### Task 2: Interactive Markers & Bottom Sheet
- Click listeners on map markers
- `queryRenderedFeatures` extracts shop data from clicked markers
- ModalBottomSheet shows shop name, rating, address, description
- "Rate this Shop" button navigates to rating screen

### Task 3: Transaction-Based Rating Submission
- `RatingScreen.kt` - Star rating UI (1.0 - 5.0) with fine-tune slider
- `CoffeeShopRepository.submitRating()` uses Firestore Transaction
- Atomic updates prevent race conditions:
  ```
  newAvg = ((oldAvg * oldTotal) + newRating) / (oldTotal + 1)
  ```
- Creates rating document in `ratings` subcollection

## Files Created/Modified

### New Files (8 total)
1. `/app/src/main/java/.../data/model/CoffeeShop.kt` - Data model
2. `/app/src/main/java/.../data/repository/CoffeeShopRepository.kt` - Firestore operations
3. `/app/src/main/java/.../viewmodel/CoffeeShopViewModel.kt` - State management
4. `/app/src/main/java/.../ui/screens/lover/CoffeeShopMapScreen.kt` - Map UI
5. `/app/src/main/java/.../ui/screens/lover/RatingScreen.kt` - Rating UI
6. `/app/src/main/res/drawable/coffee_marker.xml` - Custom marker icon
7. `/app/src/main/java/.../util/SampleDataSeeder.kt` - Test data utility
8. `/COFFEE_SHOP_RATING_GUIDE.md` - Complete documentation

### Modified Files (1)
1. `/app/src/main/java/.../navigation/NavGraph.kt` - Added routes:
   - `coffee_shop_map` → Main map screen
   - `coffee_shop_rating/{shopId}` → Rating screen

## Quick Test

### 1. Seed Sample Data (First Time Only)
```kotlin
// In MainActivity.onCreate() after setContent
import com.example.coffeerankingapk.util.SampleDataSeeder

SampleDataSeeder.seedSampleCoffeeShops()
```

### 2. Navigate to Map
```kotlin
navController.navigate("coffee_shop_map")
```

### 3. Expected Behavior
- Map loads with 5 coffee shops in San Francisco
- Tap marker → Bottom sheet shows shop info
- Tap "Rate this Shop" → Rating screen
- Select rating → Submit → Returns to map with updated rating

## Architecture Pattern

```
UI (Compose)
    ↓
ViewModel (StateFlow)
    ↓
Repository (suspend functions)
    ↓
Firestore (Transaction)
```

## Key Features

✨ **Transaction-Safe Rating**: Multiple users can rate simultaneously without data corruption  
✨ **Real-time Updates**: StateFlow automatically updates UI when data changes  
✨ **GeoJSON Rendering**: Efficient map marker rendering with Mapbox  
✨ **Material 3 Design**: Modern UI with bottom sheets and Material Design 3  
✨ **Type-Safe Navigation**: Compose navigation with type-safe arguments  

## Firestore Security Rules Required

```javascript
match /coffeeShops/{shopId} {
  allow read: if true;
  allow write: if request.auth != null;
  
  match /ratings/{ratingId} {
    allow read: if true;
    allow create: if request.auth != null 
                  && request.resource.data.userId == request.auth.uid;
  }
}
```

## Testing Checklist

- [ ] Sample data appears in Firestore Console
- [ ] Map loads with 5 markers
- [ ] Tapping marker shows bottom sheet
- [ ] Bottom sheet displays correct shop info
- [ ] Rating screen shows shop name and current rating
- [ ] Star rating updates when clicked
- [ ] Slider provides fine-tuned control
- [ ] Submit button creates transaction
- [ ] Firestore document updates correctly:
  - `totalRatings` increments
  - `averageRating` recalculates
  - New document in `ratings` subcollection
- [ ] Map shows updated rating after returning

## Troubleshooting

### "No markers appear"
- Check Firestore collection named `coffeeShops`
- Verify documents have `type: "coffee shop"`
- Check logcat for "Loaded X coffee shops"

### "Bottom sheet not showing"
- Verify marker tap registered (check logs)
- Ensure `shopId` matches Firestore document ID

### "Rating not saving"
- Verify user is logged in (Firebase Auth)
- Check Firestore security rules
- View logcat for transaction errors

## Next Steps

See `COFFEE_SHOP_RATING_GUIDE.md` for:
- Advanced customization
- Performance optimization
- Adding search/filters
- User location tracking
- Photo uploads
- Clustering for many markers

## Dependencies (Already in build.gradle)

```gradle
// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")

// Mapbox
implementation("com.mapbox.maps:android:11.9.0")

// Compose
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
implementation("androidx.compose.material3:material3:1.1.2")
```

All code is complete and ready to test! 🚀
