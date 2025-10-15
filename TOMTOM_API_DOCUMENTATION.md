# TomTom API Integration Documentation

## Overview
This project implements a comprehensive TomTom API integration following the official TomTom developer documentation. All APIs require a valid API key from [TomTom Developer Portal](https://developer.tomtom.com/user/me/apps).

## API Key Configuration
The API key is configured in `BuildConfig.TOMTOM_API_KEY` and currently set to: `9RgDW09xur9AoyHZzrVdEMoyeqDEsdCFre`

## Available APIs

### 1. Search API
**File:** `TomTomSearchApi.kt`
**Documentation:** https://developer.tomtom.com/search-api/documentation

#### Endpoints Available:
- **Fuzzy Search** - Universal search combining geocoding and POI search
- **Category Search** - Search for specific POI categories (e.g., coffee shops)
- **POI Details** - Get detailed information about specific places
- **Nearby Search** - Find POIs around a location
- **Geocoding** - Convert addresses to coordinates
- **Reverse Geocoding** - Convert coordinates to addresses
- **Structured Geocoding** - Search with structured address components

#### Example Usage:
```kotlin
// Search for coffee shops
val coffeeShops = repository.searchCoffeeShops(
    lat = 40.7128,
    lon = -74.0060,
    radius = 5000,
    limit = 20
)

// Fuzzy search
val places = repository.fuzzySearch(
    query = "Starbucks New York",
    lat = 40.7128,
    lon = -74.0060
)
```

### 2. Routing API
**File:** `TomTomRoutingApi.kt`
**Documentation:** https://developer.tomtom.com/routing-api/documentation

#### Endpoints Available:
- **Calculate Route** - Get optimized routes between points
- **Calculate Reachable Range** - Find areas reachable within constraints
- **Matrix Routing** - Calculate travel times between multiple points

#### Route Types:
- `fastest` - Fastest route (default)
- `shortest` - Shortest distance
- `eco` - Most fuel-efficient
- `thrilling` - Most scenic/entertaining

#### Travel Modes:
- `car` (default), `truck`, `taxi`, `bus`, `van`, `motorcycle`, `bicycle`, `pedestrian`

#### Example Usage:
```kotlin
// Calculate route between two points
val route = repository.calculateRoute(
    fromLat = 40.7128,
    fromLon = -74.0060,
    toLat = 40.7589,
    toLon = -73.9851,
    routeType = "fastest",
    travelMode = "car",
    traffic = true
)

// Multi-point route with optimization
val waypoints = listOf(
    40.7128 to -74.0060,  // Start
    40.7589 to -73.9851,  // Waypoint 1
    40.7614 to -73.9776,  // Waypoint 2
    40.7128 to -74.0060   // Return to start
)
val optimizedRoute = repository.calculateMultiPointRoute(
    waypoints = waypoints,
    optimizeOrder = true
)
```

### 3. Traffic API
**File:** `TomTomAdditionalApis.kt`
**Documentation:** https://developer.tomtom.com/traffic-api/documentation

#### Endpoints Available:
- **Traffic Flow** - Get real-time traffic flow data
- **Traffic Incidents** - Get traffic incidents and road closures

#### Example Usage:
```kotlin
// Get traffic flow
val trafficFlow = repository.getTrafficFlow(
    lat = 40.7128,
    lon = -74.0060,
    zoom = 12
)

// Get traffic incidents in area
val incidents = repository.getTrafficIncidents(
    centerLat = 40.7128,
    centerLon = -74.0060,
    radiusKm = 10.0
)
```

### 4. Maps API
**File:** `TomTomAdditionalApis.kt`
**Documentation:** https://developer.tomtom.com/maps-api/documentation

#### Map Styles:
- `main` - Standard map
- `night` - Dark theme
- `s1`, `s2`, `s3` - Alternative styles

#### Map Layers:
- `basic` - Basic map tiles
- `hybrid` - Satellite + labels
- `labels` - Labels only

### 5. Places API
**File:** `TomTomAdditionalApis.kt`
**Documentation:** https://developer.tomtom.com/places-api/documentation

#### Endpoints Available:
- **Place Details** - Get comprehensive place information
- **Place Photos** - Get photos for places
- **Place Reviews** - Get user reviews and ratings

### 6. Geofencing API
**File:** `TomTomAdditionalApis.kt`
**Documentation:** https://developer.tomtom.com/geofencing-api/documentation

## Implementation Architecture

### Repository Pattern
The implementation uses two repository classes:

1. **TomTomRepository** - Basic implementation for existing screens
2. **EnhancedTomTomRepository** - Comprehensive implementation with all features

### Network Module
`TomTomNetworkModule` provides configured Retrofit instances for all APIs with:
- HTTP logging interceptor
- Moshi JSON conversion
- 30-second timeouts
- Proper error handling

### Data Models
`TomTomModels.kt` contains comprehensive data classes matching TomTom's API responses:
- Search responses with POI details, addresses, categories
- Route responses with summaries, legs, and guidance
- Traffic flow and incident data
- Place details with photos and reviews
- Complete type safety with Moshi annotations

## Common POI Categories

The implementation includes predefined category constants:

```kotlin
// Coffee & Food
TomTomApiConstants.COFFEE_SHOP_CATEGORY = "9376"
TomTomApiConstants.RESTAURANT_CATEGORY = "7315"

// Transportation
TomTomApiConstants.GAS_STATION_CATEGORY = "7311"
TomTomApiConstants.AIRPORT_CATEGORY = "7383"
TomTomApiConstants.PARKING_CATEGORY = "7369"

// Services
TomTomApiConstants.HOSPITAL_CATEGORY = "7321"
TomTomApiConstants.BANK_CATEGORY = "7328"
TomTomApiConstants.ATM_CATEGORY = "7397"
TomTomApiConstants.HOTEL_CATEGORY = "7314"
```

## Specialized Workflows

### Coffee Shop Discovery
The `EnhancedTomTomRepository` includes specialized methods for coffee discovery:

```kotlin
// Complete coffee discovery with traffic analysis
val result = repository.discoverCoffeeShopsWithDetails(
    userLat = 40.7128,
    userLon = -74.0060,
    maxResults = 10,
    maxDistanceKm = 5.0
)

// Plan optimal coffee shop tour
val tourPlan = repository.planCoffeeShopTour(
    userLat = 40.7128,
    userLon = -74.0060,
    shopIds = listOf("shop1", "shop2", "shop3"),
    optimizeOrder = true
)
```

## Error Handling

All repository methods return `Result<T>` for safe error handling:

```kotlin
when (val result = repository.searchCoffeeShops(lat, lon)) {
    is Result.Success -> {
        val coffeeShops = result.getOrNull()
        // Handle success
    }
    is Result.Failure -> {
        val error = result.exceptionOrNull()
        // Handle error
    }
}
```

## Usage in UI

### Current Implementation
The `SimpleMapScreen` uses the basic repository for coffee shop search and routing.

### For Enhanced Features
Use `EnhancedTomTomRepository` for:
- Traffic-aware routing
- Detailed place information
- Multi-stop route optimization
- Real-time traffic analysis

## API Rate Limits

TomTom APIs have rate limits based on your subscription:
- **Free Tier:** 2,500 requests/day
- **Developer Tier:** 100,000 requests/month
- **Production Tiers:** Higher limits available

Monitor usage through the [TomTom Developer Dashboard](https://developer.tomtom.com/user/me/apps).

## Best Practices

1. **Cache Results:** Store search results locally to reduce API calls
2. **Batch Requests:** Use matrix routing for multiple destinations
3. **Optimize Queries:** Use appropriate radius and limit parameters
4. **Error Handling:** Always handle network failures gracefully
5. **Location Permissions:** Request location permissions properly
6. **Traffic Data:** Use traffic-aware routing for better user experience

## Testing

Test the APIs using:
- Android emulator with mock locations
- Physical device with GPS
- TomTom's API testing tools
- Postman for direct API testing

## Support

- **TomTom Documentation:** https://developer.tomtom.com/
- **Community Forum:** https://developer.tomtom.com/tomtom-forums
- **Support Portal:** https://developer.tomtom.com/support
- **API Status:** https://developer.tomtom.com/status