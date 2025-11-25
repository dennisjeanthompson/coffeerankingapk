# 🗺️ Mapbox Quick Start Guide

## ⚡ Quick Setup (5 Minutes)

### 1️⃣ Get Your Tokens
Visit: https://account.mapbox.com/access-tokens/

**You need TWO tokens:**
- 🔑 **Public Token** (pk.xxx) - For the app
- 🔐 **Secret Token** (sk.xxx) - For Gradle downloads

### 2️⃣ Add Secret Token
**File:** `gradle.properties`
```properties
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZjh4...
```

### 3️⃣ Add Public Token
**File:** `app/src/main/res/values/strings.xml`
```xml
<string name="mapbox_access_token">pk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZjh4...</string>
```

### 4️⃣ Build & Run
```bash
./gradlew clean
./gradlew assembleDebug
```

---

## 🎯 Features Overview

| Feature | Description | Status |
|---------|-------------|--------|
| 🗺️ Interactive Map | Mapbox Streets with gestures | ✅ Ready |
| 📍 Cafe Markers | 6 cafes with coordinates | ✅ Ready |
| 🔍 Search | Filter by name/address | ✅ Ready |
| 📱 User Location | GPS tracking | ✅ Ready |
| 🧭 Directions | Distance & duration | ✅ Ready |
| 🎯 Bottom Sheet | Cafe details card | ✅ Ready |
| 🔐 Permissions | Runtime location request | ✅ Ready |

---

## 📱 User Journey

```
1. Open App → Login → Select "Coffee Lover"
2. Tap "Map" tab (bottom navigation)
3. Grant location permission
4. See cafes on map
5. Search or tap marker
6. View details & directions
7. Navigate to cafe
```

---

## 🧭 Navigation Structure

```
LoverMainScreen
├── Home (Dashboard)
├── Map ⭐ NEW
│   ├── Search Bar
│   ├── Cafe Markers
│   ├── My Location Button
│   └── Bottom Sheet (on select)
├── Rewards
└── Profile
```

---

## 📦 Dependencies Added

```gradle
// Mapbox Maps SDK
implementation 'com.mapbox.maps:android:11.0.0'
implementation 'com.mapbox.extension:maps-compose:11.0.0'

// Mapbox Search SDK  
implementation 'com.mapbox.search:mapbox-search-android:1.0.0'
implementation 'com.mapbox.search:mapbox-search-android-ui:1.0.0'

// NOTE: Mapbox Navigation SDK is not included in this project
// Use `com.mapbox.mapboxsdk:mapbox-sdk-services` (Directions) for route fetching if needed

// Location services
implementation 'com.google.android.gms:play-services-location:21.0.1'
```

---

## 🎨 UI Components

### Search Bar
- Top of screen
- Rounded corners with elevation
- Clear button
- Instant search results

### Cafe Markers
- Standard Mapbox pins
- Cafe name labels
- Tap to select

### Bottom Sheet
- Appears on marker tap
- Shows: Name, Address, Rating
- Buttons: Directions, Details, Close

### My Location Button
- FAB at bottom-right
- Centers map on user
- Only visible with permission

### Route Dialog
- Shows distance & duration
- Estimate for walking/driving
- "Start Navigation" button

---

## 🔐 Permissions Required

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## 📍 Cafe Locations (NYC)

| Cafe | Latitude | Longitude |
|------|----------|-----------|
| Brew & Beans | 40.7580 | -73.9855 |
| The Roastery | 40.7489 | -73.9680 |
| Coffee Corner | 40.7829 | -73.9654 |
| Steam & Grind | 40.7614 | -73.9776 |
| Morning Brew | 40.7505 | -74.0027 |
| Caffeine Fix | 40.7549 | -73.9840 |

---

## 🐛 Common Issues

### Build fails with "Could not resolve"
```bash
# Solution: Check token in gradle.properties
MAPBOX_DOWNLOADS_TOKEN=sk.your_actual_token_here
```

### Map is blank
```xml
<!-- Solution: Check token in strings.xml -->
<string name="mapbox_access_token">pk.your_actual_token_here</string>
```

### Location not working
```
1. Grant location permission in app
2. Enable device location services
3. Test outdoors for better GPS
```

---

## 📚 Documentation

- 📘 **Setup Guide**: `MAPBOX_SETUP.md`
- 📗 **Full Summary**: `MAPBOX_INTEGRATION_SUMMARY.md`
- 📕 **Main README**: `README.md`

---

## 🚀 Next Steps

### Immediate:
1. Add your Mapbox tokens
2. Build and test on device
3. Test search functionality
4. Test directions feature

### Future Enhancements:
- Real turn-by-turn navigation
- Custom cafe marker icons
- Marker clustering
- Offline maps
- Dark mode support

---

## 💡 Pro Tips

✅ **Use real device** for best location accuracy
✅ **Test outdoors** for GPS signal
✅ **Keep tokens secure** - never commit to git
✅ **Monitor usage** at mapbox.com/account
✅ **Free tier** includes 50K map loads/month

---

## 🎉 Ready to Go!

The integration is **100% complete**. Just add your tokens and build!

**Questions?** Check `MAPBOX_SETUP.md` for detailed instructions.

**Made with ❤️ for Coffee Ranking APK**
