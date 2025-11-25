# Coffee Ranking APK

A native Android app built with Kotlin and Jetpack Compose that allows cafe owners and coffee lovers to interact through a comprehensive coffee ranking platform.

## Features

### For Cafe Owners:
- **Dashboard**: View KPI metrics (total reviews, average rating, monthly visits)
- **Analytics**: Track reviews over time with interactive charts
- **Coupon Management**: Create and manage promotional coupons
- **Business Settings**: Manage cafe information and operating hours

### For Coffee Lovers:
- **Discover**: Browse and search local cafes
- **Interactive Map**: View cafes on Mapbox map with search and routing
- **Directions**: Get routing information (distance and duration) to cafes
- **Reviews**: Rate and review cafes with 5-star system
- **Rewards**: Earn loyalty points and redeem coupons
- **Favorites**: Save and share favorite cafes
- **Detailed Views**: View cafe details, photos, and reviews

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Hilt for Dependency Injection
- **Navigation**: Jetpack Navigation Compose with nested graphs
-- **Maps**: Mapbox Maps SDK with Search & Directions (Navigation SDK not included)
- **Location**: Google Play Services Location
- **Charts**: Custom Compose charts for analytics
- **Authentication**: Firebase Auth (configured for stubs)
- **Database**: Firebase Firestore (configured for stubs)
- **Image Loading**: Coil
- **Build System**: Gradle

## Design System

The app follows precise design tokens for consistent styling:

- **Colors**: Warm cream background (#F6F0E9), brown primary (#6B3E2A), accent light (#E9DCCF)
- **Typography**: Inter font family with defined weights and sizes
- **Shapes**: Consistent border radius (8dp, 12dp, 18dp)
- **Spacing**: 16dp screen padding, 12-16dp list gaps
- **Touch Targets**: Minimum 44dp for accessibility

## Project Structure

```
app/src/main/java/com/example/coffeerankingapk/
├── ui/
│   ├── theme/           # AppTheme, colors, typography, shapes
│   ├── components/      # Reusable UI components
│   └── screens/         # Feature screens (owner/, lover/, auth)
├── navigation/          # NavGraph with nested routing
├── data/mock/          # Sample JSON data for development
├── di/                 # Hilt dependency injection modules
├── MainActivity.kt     # Entry point
└── CoffeeRankingApplication.kt
```

## Build Instructions

### Prerequisites

1. **Android Studio**: Latest stable version with Kotlin support
2. **Android SDK**: API level 24+ (Android 7.0+)
3. **Java**: JDK 8 or higher

### Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd coffeerankingapk
   ```

2. **API Keys Setup** (Required for full functionality):

   **Firebase Setup**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add Android app with package name: `com.example.coffeerankingapk`
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication and Firestore in Firebase console

   **Mapbox Setup** (Required for map functionality):
   - Go to [Mapbox Account](https://account.mapbox.com/)
   - Sign up or log in to your account
   - Navigate to "Access Tokens" page
   - Copy your **default public token** (starts with `pk.`)
   - Create a **secret token** with `DOWNLOADS:READ` scope for SDK downloads
   - In `gradle.properties`, replace `YOUR_MAPBOX_SECRET_TOKEN_HERE` with your secret token
   - In your app code, add your public token where needed (see Configuration section)

   **Important**: Never commit your actual tokens to version control. Use environment variables or secure key management for production.

3. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on device**:
   ```bash
   ./gradlew installDebug
   ```

### Development

1. **Open in Android Studio**:
   - File → Open → Select project root directory
   - Wait for Gradle sync to complete

2. **Run on emulator/device**:
   - Click "Run" button or press Shift+F10
   - Select target device

3. **Build variants**:
   - Debug: `./gradlew assembleDebug`
   - Release: `./gradlew assembleRelease`

## Configuration Notes

### Mapbox Public Token Configuration

To enable the Mapbox map features, you need to add your public access token:

1. **Create a strings resource** (if not exists):
   Create or edit `app/src/main/res/values/strings.xml`:
   ```xml
   <resources>
       <string name="mapbox_access_token">YOUR_MAPBOX_PUBLIC_TOKEN_HERE</string>
   </resources>
   ```

2. **Add to AndroidManifest.xml**:
   ```xml
   <application>
       <meta-data
           android:name="MAPBOX_ACCESS_TOKEN"
           android:value="@string/mapbox_access_token" />
   </application>
   ```

### Map Features

The integrated Mapbox map includes:

- **📍 Cafe Markers**: All cafes displayed with markers on the map
- **🔍 Search Bar**: Search cafes by name, address, or description
- **📱 User Location**: Shows your current location (requires location permission)
- **🧭 Directions**: Calculate distance and duration to any cafe
- **🗺️ Interactive Map**: Zoom, pan, and tap markers for cafe details
- **🎯 Navigation**: Tap "Directions" to get routing information
- **📊 Route Info**: Distance and estimated walking/driving time

### Current State
The app is configured with mock data and complete navigation flows. Key features implemented:

✅ **Complete Navigation**: Both owner and lover flows with bottom navigation
✅ **Enhanced UI**: Dashboard, rating, rewards, profile screens matching mockups
✅ **Analytics Charts**: Interactive charts for owner analytics
✅ **Mapbox Integration**: Interactive maps with search, markers, and routing
✅ **Location Services**: Real-time user location tracking

Key TODOs for production:
1. **Firebase Auth**: Replace mock login with real Firebase Auth in `AuthScreen.kt`
2. **Firebase Firestore**: Implement real data repositories replacing mock JSON data
3. **Mapbox Tokens**: Add your actual Mapbox tokens (public and secret)
4. **Mapbox Directions API**: Integrate real routing API for accurate routing/directions (turn-by-turn navigation is a future enhancement)
5. **Image URLs**: Replace placeholder URLs with real cafe images
6. **Date Pickers**: Complete coupon date picker implementation
7. **Push Notifications**: Add FCM for rewards and updates

### Mock Data
The app includes sample data in `data/mock/`:
- `cafes.json`: Sample cafe listings
- `reviews.json`: Sample user reviews
- `coupons.json`: Sample promotional coupons

### Dependencies
All dependencies are configured in `app/build.gradle`:
- Compose BOM for UI consistency
- Hilt for dependency injection
- Navigation Compose for routing
- Mapbox Maps SDK for interactive maps
- Mapbox Search SDK for location search
- Mapbox route fetching via `mapbox-sdk-services` (no Navigation SDK)
- Google Play Services Location for user location
- Firebase BOM for backend services
- Coil for image loading

## Testing

Run tests with:
```bash
./gradlew test           # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Troubleshooting

### Common Issues:

1. **Build fails with "Could not resolve"**: Run `./gradlew clean` then rebuild
2. **Firebase errors**: Ensure `google-services.json` is in the `app/` directory
3. **Mapbox dependency errors**: Ensure `MAPBOX_DOWNLOADS_TOKEN` is set in `gradle.properties`
4. **Map not showing**: Check that Mapbox public token is configured in resources
5. **Location not working**: Grant location permissions in app settings
6. **Gradle sync issues**: Use "File → Invalidate Caches and Restart"

### Support
For issues with the build process or setup, check:
- Android Studio version compatibility
- SDK tools are up to date
- Network connectivity for dependency downloads

## License

This project is for demonstration purposes. Check individual dependency licenses for production use.