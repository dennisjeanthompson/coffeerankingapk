# Coffee Ranking APK with TomTom Integration

A native Android app built with Kotlin and Jetpack Compose that allows cafe owners and coffee lovers to interact through a comprehensive coffee ranking platform with integrated TomTom mapping, search, and routing capabilities.

## Features

### For Cafe Owners:
- **Dashboard**: View KPI metrics (total reviews, average rating, monthly visits)
- **Analytics**: Track reviews over time with interactive charts
- **Location Management**: Basic location display for cafes
- **Coupon Management**: Create and manage promotional coupons
- **Business Settings**: Manage cafe information and operating hours

### For Coffee Lovers:
- **Discover**: Browse and search local cafes
- **Reviews**: Rate and review cafes with 5-star system
- **Rewards**: Earn loyalty points and redeem coupons
- **Favorites**: Save and share favorite cafes
- **Detailed Views**: View cafe details, photos, and reviews
- **Maps & Navigation**: Search nearby places, get directions with TomTom integration

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM pattern with Repository
- **Navigation**: Jetpack Navigation Compose with nested graphs
- **Maps**: TomTom Map Display SDK with Search and Routing APIs
- **Charts**: Custom Compose charts for analytics
- **Authentication**: Mock authentication (ready for integration)
- **Database**: Mock data storage (ready for integration)
- **Image Loading**: Coil
- **Networking**: Retrofit + Moshi for TomTom API calls
- **Location**: Google Play Services FusedLocationProvider
- **Build System**: Gradle

## TomTom Integration Setup

### 1. Get TomTom API Key
1. Go to [TomTom Developer Portal](https://developer.tomtom.com/)
2. Create an account and new application
3. Generate API keys for Maps, Search, and Routing APIs

### 2. Add API Key to Project
Add the following line to your `local.properties` file:
```
TOMTOM_API_KEY=your_actual_api_key_here
```

### 3. Restrict API Key (Recommended)
In TomTom Developer Console:
- Go to your application settings
- Add package name restriction: `com.example.coffeerankingapk`
- Enable only required APIs: Maps SDK, Search API, Routing API
- Set usage limits if needed

## Build Instructions

### Prerequisites

1. **Android Studio**: Latest stable version with Kotlin support
2. **Android SDK**: API level 24+ (Android 7.0+)
3. **Java**: JDK 8 or higher
4. **TomTom API Key**: Required for maps and search functionality

### Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd coffeerankingapk
   ```

2. **Add TomTom API Key**:
   Create/edit `local.properties` and add:
   ```
   TOMTOM_API_KEY=your_tomtom_api_key
   ```

3. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**:
   ```bash
   ./gradlew installDebug
   ```

## TomTom Features

### Implemented:
- ✅ **Search API**: Find nearby cafes and places
- ✅ **Routing API**: Calculate routes with distance/duration
- ✅ **Location Services**: Device location with permissions
- ✅ **Map Display**: TomTom MapView integration ready
- ✅ **Error Handling**: Network errors with retry suggestions
- ✅ **MVVM Architecture**: Clean separation with Repository pattern

### Map Features:
- Search places with location bias
- Display search results with distance
- POI details in bottom sheet
- Route calculation and display
- Toast notifications for successful route loading
- Runtime location permission handling

## File Structure (TomTom Integration)
```
app/src/main/java/com/example/coffeerankingapk/tomtom/
├── network/
│   ├── TomtomRetrofitModule.kt    # Retrofit configuration
│   ├── SearchApi.kt               # Search API interface
│   └── RoutingApi.kt              # Routing API interface
├── models/
│   ├── SearchModels.kt            # Search response data classes
│   └── RoutingModels.kt           # Routing response data classes
├── repository/
│   └── MapRepository.kt           # API calls and data handling
├── viewmodel/
│   └── MapViewModel.kt            # UI state management
├── ui/
│   └── MapScreen.kt               # Compose UI with map integration
└── utils/
    └── PolylineDecoder.kt         # Geometry decoding utilities
```

## Troubleshooting

### TomTom-Specific Issues:
- **401 Unauthorized**: Check API key validity and format
- **403 Forbidden**: Verify package name restrictions in TomTom console
- **429 Rate Limited**: API usage exceeded, implement exponential backoff
- **Search returns no results**: Check location services and search radius

## License

This project is for demonstration purposes. Check individual dependency licenses for production use.