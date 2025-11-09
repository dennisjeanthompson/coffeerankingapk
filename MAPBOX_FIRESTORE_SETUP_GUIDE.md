# Mapbox + Firestore Coffee Shop Map - Complete Setup & Testing Guide

## ✅ What's Implemented

### 1. **Coffee Lover Map Tab** 
- Bottom navigation "Map" tab now shows `CoffeeShopMapScreen`
- Replaces old MapScreen with new Firestore-powered version
- Location permissions properly requested

### 2. **GPS/User Location (Fixed)**
- Blue pulsing dot shows your current location
- Map centers on Baguio City, Philippines (16.4023°N, 120.5960°E)
- User location enabled automatically when map loads

### 3. **Coffee Shop Markers**
- Custom coffee cup icon (`coffee_marker.xml`)
- Only shows shops within 100km of Baguio
- Filters by coffee-related keywords: "coffee", "cafe", "café", "espresso", "cappuccino"
- Case-insensitive matching

### 4. **Search/Filter (Working)**
- Real-time search box at top
- Filters by: shop name, address, or type
- Shows count: "X coffee shops found"
- Clear button (X) to reset

### 5. **Review System (Bottom Sheet)**
- **Tap any coffee marker** → Bottom sheet appears
- Shows:
  - Shop name
  - Current rating (★ 4.5)
  - Total ratings count
  - Address
  - Description
  - **"Rate this Shop" button**
- Tapping button navigates to `RatingScreen`

### 6. **Rating Screen**
- Star rating selector (1-5 stars)
- Fine-tune slider (1.0 - 5.0 in 0.1 increments)
- Submit button
- Uses Firestore Transaction for atomic updates

## 🔧 Setup Steps

### Step 1: Install APK

```bash
# For modern devices (2018+)
adb install app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# For older devices
adb install app/build/outputs/apk/debug/app-armeabi-v7a-debug.apk
```

### Step 2: Sign In

1. Open app
2. Sign in with Firebase Auth (email/password or Google Sign-In)
3. Select "Coffee Lover" role
4. You'll see the main screen with bottom navigation

### Step 3: Add Sample Data (CRITICAL!)

**Option A: Using Debug Button in App**
1. Go to Map tab
2. You'll see a floating **+** button (bottom-right)
3. Tap it to seed 8 sample coffee shops in Baguio
4. Wait 2 seconds for data to load
5. Map will refresh and show markers

**Option B: Via Firestore Console**
1. Go to Firebase Console → Firestore Database
2. Manually create collection `coffeeShops`
3. Add documents with this structure:
```javascript
{
  "id": "baguio_shop1",
  "name": "Baguio Brew",
  "type": "coffee shop",
  "location": GeoPoint(16.4023, 120.5960),
  "averageRating": 4.5,
  "totalRatings": 89,
  "description": "Specialty coffee with a mountain view",
  "address": "Session Road, Baguio City, Benguet"
}
```

**Option C: Via Code (MainActivity)**
Add to `MainActivity.onCreate()`:
```kotlin
import com.example.coffeerankingapk.util.SampleDataSeeder

// Run once on first launch
SampleDataSeeder.seedSampleCoffeeShops()
```

## 🧪 Testing the Complete Flow

### Test 1: Map Display
1. Open app → Sign in → Select Coffee Lover
2. Tap **Map** tab (bottom navigation)
3. ✅ **Expected**: Map loads centered on Baguio City
4. ✅ **Expected**: Blue pulsing dot shows your location (if GPS enabled)
5. ✅ **Expected**: Search box at top says "X coffee shops found"

### Test 2: Sample Data Loading
1. Tap the **+** button (bottom-right)
2. ✅ **Expected**: Toast/log message "Seeding sample data..."
3. Wait 2-3 seconds
4. ✅ **Expected**: Counter updates to "8 coffee shops found"
5. ✅ **Expected**: 8 coffee cup markers appear on map around Baguio

### Test 3: Marker Click → Bottom Sheet
1. **Tap any coffee cup marker** on the map
2. ✅ **Expected**: Bottom sheet slides up from bottom
3. ✅ **Expected**: Shows:
   - Shop name (e.g., "Baguio Brew")
   - Rating: "★ 4.5 (89 ratings)"
   - Address: "Session Road, Baguio City, Benguet"
   - Description: "Specialty coffee with a mountain view"
   - Blue button: **"Rate this Shop"**

### Test 4: Rating Flow
1. In bottom sheet, tap **"Rate this Shop"** button
2. ✅ **Expected**: Bottom sheet closes
3. ✅ **Expected**: Rating screen opens showing:
   - Shop name at top
   - Current rating display
   - 5 stars (tap to select)
   - Slider (drag to fine-tune)
   - **"Submit Rating"** button
4. Select rating (e.g., 5 stars)
5. Tap **"Submit Rating"**
6. ✅ **Expected**: Screen navigates back to map
7. ✅ **Expected**: Firestore updates:
   - `totalRatings` increments by 1
   - `averageRating` recalculates
   - New document in `ratings` subcollection

### Test 5: Search/Filter
1. In search box, type: **"Baguio"**
2. ✅ **Expected**: Only shops with "Baguio" in name/address show
3. ✅ **Expected**: Counter updates (e.g., "3 coffee shops found")
4. ✅ **Expected**: Markers on map update to show only filtered shops
5. Tap **X** button to clear
6. ✅ **Expected**: All 8 shops reappear

### Test 6: Location Filtering
1. Check that only Baguio-area shops appear
2. ✅ **Expected**: No shops from USA, Europe, or Manila
3. ✅ **Expected**: All shops within ~100km of Baguio (16.4023°N, 120.5960°E)

## 🐛 Troubleshooting

### Problem: "0 coffee shops found"

**Cause**: No data in Firestore  
**Fix**: 
1. Tap the **+** button to seed data
2. Check Firestore Console → `coffeeShops` collection exists
3. Check logs for errors: `adb logcat | grep CoffeeShop`

### Problem: Markers don't appear

**Cause**: Map not loading or markers not rendered  
**Fix**:
1. Check Mapbox token in `res/values/strings.xml`
2. Check logs: `adb logcat | grep MapboxMapView`
3. Look for: "Loaded X coffee shops on map"
4. Verify drawable exists: `app/src/main/res/drawable/coffee_marker.xml`

### Problem: Bottom sheet doesn't show when clicking marker

**Cause**: Click detection not working  
**Fix**:
1. Check logs: `adb logcat | grep "Map clicked"`
2. Should see: "Found X features" and "Clicked shop: {shopId}"
3. Verify layer name: "coffee-shops-layer" exists in style
4. Try clicking directly on the marker center

### Problem: GPS location not showing

**Cause**: Location permissions not granted  
**Fix**:
1. Android Settings → Apps → CoffeeRankingAPK → Permissions → Location → Allow
2. Or app will prompt when opening Map tab
3. Check logs: `adb logcat | grep "User location"`

### Problem: Search doesn't filter

**Cause**: State not updating  
**Fix**:
1. Check ViewModel logs: `adb logcat | grep CoffeeShopViewModel`
2. Type in search box and look for filtered count change
3. Verify search query state updates

### Problem: Rating doesn't save

**Cause**: Firestore transaction failing or not logged in  
**Fix**:
1. Verify user is logged in: Check Firebase Auth current user
2. Check Firestore security rules allow writes:
```javascript
match /coffeeShops/{shopId} {
  allow read: if true;
  allow write: if request.auth != null;
  
  match /ratings/{ratingId} {
    allow read: if true;
    allow create: if request.auth != null;
  }
}
```
3. Check logs: `adb logcat | grep "Rating submitted"`

## 📊 Debugging Commands

### View all relevant logs:
```bash
adb logcat | grep -E "CoffeeShop|MapboxMapView|Firestore"
```

### Check Firestore writes:
```bash
adb logcat | grep "Firestore"
```

### Check marker clicks:
```bash
adb logcat | grep "Map clicked"
```

### Check rating submissions:
```bash
adb logcat | grep "Rating"
```

## 🔍 Sample Data Details

8 shops seeded in Baguio area:

| Shop Name | Type | Coordinates | Distance |
|-----------|------|-------------|----------|
| Baguio Brew | coffee shop | 16.4023, 120.5960 | 0 km |
| Hill Station Coffee | Coffee Shop | 16.4113, 120.5927 | ~1 km |
| Cordillera Café | cafe | 16.3950, 120.5850 | ~1 km |
| Pine Ridge Espresso | Espresso Bar | 16.4180, 120.5980 | ~2 km |
| Café de Cordillera | Café | 16.3880, 120.5920 | ~2 km |
| The Baguio Coffee House | COFFEE HOUSE | 16.4100, 120.6010 | ~1 km |
| Session Road Brew | coffee shop | 16.4050, 120.5975 | ~0.5 km |
| La Trinidad Coffee Co. | coffee shop | 16.4620, 120.5920 | ~7 km |

All should appear on the map when data is loaded!

## ✅ Verification Checklist

- [ ] App installs without errors
- [ ] Sign in works (Firebase Auth)
- [ ] Coffee Lover role selected
- [ ] Bottom navigation shows 4 tabs
- [ ] Map tab opens successfully
- [ ] Location permission granted
- [ ] Blue GPS dot appears on map
- [ ] Map centers on Baguio City
- [ ] Search box visible at top
- [ ] "0 coffee shops found" initially
- [ ] Floating + button visible (bottom-right)
- [ ] Tapping + seeds data
- [ ] Counter updates to "8 coffee shops found"
- [ ] 8 coffee cup markers appear
- [ ] Tapping marker opens bottom sheet
- [ ] Bottom sheet shows shop details
- [ ] "Rate this Shop" button visible
- [ ] Tapping button opens rating screen
- [ ] Rating screen shows stars and slider
- [ ] Submit button works
- [ ] Rating saves to Firestore
- [ ] Map updates with new rating
- [ ] Search filters shops correctly
- [ ] Clear button resets search

## 🎯 Key Files Reference

| Component | File Path |
|-----------|-----------|
| Map Screen | `app/src/main/java/.../ui/screens/lover/CoffeeShopMapScreen.kt` |
| Rating Screen | `app/src/main/java/.../ui/screens/lover/RatingScreen.kt` |
| ViewModel | `app/src/main/java/.../viewmodel/CoffeeShopViewModel.kt` |
| Repository | `app/src/main/java/.../data/repository/CoffeeShopRepository.kt` |
| Data Model | `app/src/main/java/.../data/model/CoffeeShop.kt` |
| Sample Data | `app/src/main/java/.../util/SampleDataSeeder.kt` |
| Navigation | `app/src/main/java/.../navigation/NavGraph.kt` |
| Marker Icon | `app/src/main/res/drawable/coffee_marker.xml` |

Everything is properly configured and connected! The flow is:

**Map Tab → GPS Location → Load Firestore Data → Show Markers → Tap Marker → Bottom Sheet → Rate Button → Rating Screen → Submit → Firestore Transaction**

All pieces are in place! 🎉☕📱
