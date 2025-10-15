# Coffee Ranking APK

A native Android app built with Kotlin and Jetpack Compose that allows cafe owners and coffee lovers to interact through a comprehensive coffee ranking platform.

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

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM pattern
- **Navigation**: Jetpack Navigation Compose with nested graphs
- **Charts**: Custom Compose charts for analytics
- **Authentication**: Mock authentication (ready for integration)
- **Database**: Mock data storage (ready for integration)
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

2. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on device**:
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
The app is configured with mock data and placeholder features. Key features implemented:

✅ **Complete Navigation**: Both owner and lover flows with bottom navigation
✅ **Enhanced UI**: Dashboard, rating, rewards, profile screens matching mockups
✅ **Mock Data Integration**: Sample cafes, reviews, and coupons for testing

Key areas ready for integration:
1. **Authentication**: Replace mock login with your preferred auth solution in `AuthScreen.kt`
2. **Data Storage**: Implement real data repositories replacing mock JSON data
3. **Image URLs**: Replace placeholder URLs with real cafe images
4. **Date Pickers**: Complete coupon date picker implementation
5. **Push Notifications**: Add notification system for rewards and updates
6. **Location Services**: Add real location features and cafe mapping

### Mock Data
The app includes sample data in `data/mock/`:
- `cafes.json`: Sample cafe listings
- `reviews.json`: Sample user reviews
- `coupons.json`: Sample promotional coupons

### Dependencies
All dependencies are configured in `app/build.gradle`:
- Compose BOM for UI consistency
- Navigation Compose for routing
- MPAndroidChart for analytics charts
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
2. **Gradle sync issues**: Use "File → Invalidate Caches and Restart"
3. **App crashes on startup**: Check that all mock data files are properly loaded

### Support
For issues with the build process or setup, check:
- Android Studio version compatibility
- SDK tools are up to date
- Network connectivity for dependency downloads

## License

This project is for demonstration purposes. Check individual dependency licenses for production use.