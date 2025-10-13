# Coffee Ranking APK

A native Android app built with Kotlin and Jetpack Compose that allows cafe owners and coffee lovers to interact through a comprehensive coffee ranking platform.

## Features

### For Cafe Owners:
- **Dashboard**: View KPI metrics (total reviews, average rating, monthly visits)
- **Analytics**: Track reviews over time with interactive charts
- **Location Management**: Place cafe on Google Maps
- **Coupon Management**: Create and manage promotional coupons
- **Business Settings**: Manage cafe information and operating hours

### For Coffee Lovers:
- **Discover**: Browse and search local cafes
- **Reviews**: Rate and review cafes with 5-star system
- **Rewards**: Earn loyalty points and redeem coupons
- **Favorites**: Save and share favorite cafes
- **Detailed Views**: View cafe details, photos, and reviews

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Hilt for Dependency Injection
- **Navigation**: Jetpack Navigation Compose with nested graphs
- **Maps**: Google Maps Compose (Maps SDK)
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

   **Google Maps API Key**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Enable Maps SDK for Android
   - Create an API key
   - Add your API key to `app/src/main/AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
   ```

   **Firebase Setup**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add Android app with package name: `com.example.coffeerankingapk`
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication and Firestore in Firebase console

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

### Current State
The app is configured with scaffolding and mock data. Key TODOs for production:

1. **Google Maps**: Uncomment Maps Compose code in `OwnerMapPlaceScreen.kt` after adding API key
2. **Firebase Auth**: Replace mock login with real Firebase Auth in `AuthScreen.kt`
3. **Firebase Firestore**: Implement real data repositories replacing mock JSON data
4. **Image URLs**: Replace placeholder URLs with real cafe images
5. **Date Pickers**: Complete coupon date picker implementation
6. **Push Notifications**: Add FCM for rewards and updates

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
- Firebase BOM for backend services
- Google Maps Compose for location features
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
2. **Maps not loading**: Verify Google Maps API key is correctly added to AndroidManifest.xml
3. **Firebase errors**: Ensure `google-services.json` is in the `app/` directory
4. **Gradle sync issues**: Use "File → Invalidate Caches and Restart"

### Support
For issues with the build process or setup, check:
- Android Studio version compatibility
- SDK tools are up to date
- Network connectivity for dependency downloads

## License

This project is for demonstration purposes. Check individual dependency licenses for production use.