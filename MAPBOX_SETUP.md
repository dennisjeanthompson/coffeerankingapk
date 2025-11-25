# Mapbox Integration Setup Guide

This guide walks you through setting up Mapbox for the Coffee Ranking APK.

## Prerequisites

- An Android development environment set up
- A Mapbox account (free tier available)

## Step-by-Step Setup

### 1. Create a Mapbox Account

1. Go to [https://account.mapbox.com/auth/signup/](https://account.mapbox.com/auth/signup/)
2. Sign up with your email or GitHub account
3. Verify your email address

### 2. Get Your Mapbox Tokens

You'll need TWO different tokens:

#### A. Public Access Token (for runtime map display)

1. Log in to [Mapbox Account](https://account.mapbox.com/)
2. Go to the **Access Tokens** page
3. You'll see a "Default public token" - this starts with `pk.`
4. Copy this token

#### B. Secret Download Token (for SDK downloads during build)

1. On the same Access Tokens page, click **"Create a token"**
2. Name it something like "Android Downloads Token"
3. Under **Secret scopes**, check:
   - ✅ `DOWNLOADS:READ`
4. Click **"Create token"**
5. **IMPORTANT**: Copy this token immediately - you won't be able to see it again!
6. This token starts with `sk.`

### 3. Configure Your Project

#### A. Add Secret Token to `gradle.properties`

1. Open `/gradle.properties` in your project root
2. Find the line:
   ```properties
   MAPBOX_DOWNLOADS_TOKEN=YOUR_MAPBOX_SECRET_TOKEN_HERE
   ```
3. Replace `YOUR_MAPBOX_SECRET_TOKEN_HERE` with your **secret token** (starts with `sk.`)
4. Example:
   ```properties
   MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZjh4...
   ```

#### B. Add Public Token to `strings.xml`

1. Open `/app/src/main/res/values/strings.xml`
2. Find the line:
   ```xml
   <string name="mapbox_access_token" translatable="false">YOUR_MAPBOX_PUBLIC_TOKEN_HERE</string>
   ```
3. Replace `YOUR_MAPBOX_PUBLIC_TOKEN_HERE` with your **public token** (starts with `pk.`)
4. Example:
   ```xml
   <string name="mapbox_access_token" translatable="false">pk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZjh4...</string>
   ```

#### C. Add Metadata to `AndroidManifest.xml`

1. Open `/app/src/main/AndroidManifest.xml`
2. Inside the `<application>` tag, add:
   ```xml
   <meta-data
       android:name="MAPBOX_ACCESS_TOKEN"
       android:value="@string/mapbox_access_token" />
   ```

### 4. Sync and Build

1. In Android Studio, click **"Sync Project with Gradle Files"**
2. Wait for the sync to complete (may take a few minutes on first sync)
3. Build the project: **Build → Make Project**

### 5. Grant Location Permissions

When you run the app:

1. Navigate to the Map screen
2. The app will request location permissions
3. Tap **"Grant Permission"** or **"Allow"** in the system dialog
4. The map should now show your location and nearby cafes

## Features Overview

### What's Included

✅ **Interactive Map**: Powered by Mapbox Streets style
✅ **Cafe Markers**: All cafes displayed with custom markers
✅ **Search Functionality**: Search cafes by name, address, or description
✅ **User Location**: Real-time location tracking with blue dot
✅ **Route Calculation**: Distance and duration to any cafe
✅ **Directions**: Calculate routes from your location to cafes
✅ **Tap to Select**: Tap markers to see cafe details
✅ **Bottom Sheet**: Quick actions for selected cafes

### Map Controls

- **Zoom**: Pinch or double-tap to zoom in/out
- **Pan**: Drag to move around the map
- **Rotate**: Two-finger rotate gesture
- **My Location Button**: Tap to center on your location

## Troubleshooting

### Build Issues

**Problem**: `Could not resolve com.mapbox.maps:android`
- **Solution**: Check that `MAPBOX_DOWNLOADS_TOKEN` in `gradle.properties` is correct
- Ensure the token has `DOWNLOADS:READ` scope

**Problem**: Map shows but is blank
- **Solution**: Check that public token in `strings.xml` is correct
- Verify you have internet connectivity

### Runtime Issues

**Problem**: Location not showing
- **Solution**: 
  - Grant location permissions in app
  - Check device location services are enabled
  - Try moving to a different location or use GPS outdoors

**Problem**: "401 Unauthorized" error
- **Solution**: Your public token may be invalid or expired
- Generate a new token and update `strings.xml`

### Token Security

⚠️ **NEVER commit actual tokens to public repositories**

For production apps:
- Use environment variables
- Store tokens in CI/CD secrets
- Use ProGuard/R8 to obfuscate strings
- Consider using a backend proxy for token management

## Testing Without Real Location

For development/testing in emulator:

1. Open Android Studio emulator
2. Click the **"..."** (Extended Controls) button
3. Go to **Location** tab
4. Enter test coordinates or use the map to select a location
5. Click **"Send"**

Good test coordinates (New York City):
- Latitude: `40.7580`
- Longitude: `-73.9855`

## API Limits

Mapbox Free Tier includes:
- 50,000 map loads/month
- 100,000 search requests/month
- 5,000 navigation sessions/month

For production apps with more traffic, consider upgrading your Mapbox plan.

## Additional Resources

- [Mapbox Android SDK Docs](https://docs.mapbox.com/android/maps/guides/)
- [Mapbox Search SDK](https://docs.mapbox.com/android/search/guides/)
- [Mapbox Navigation SDK](https://docs.mapbox.com/android/navigation/guides/)
- [Mapbox Account Dashboard](https://account.mapbox.com/)

## Next Steps

To enhance the map functionality further:

1. **Integrate Mapbox Directions API**: Get real routing/directions (turn-by-turn navigation is a future enhancement)
2. **Add custom marker icons**: Use cafe images as markers
3. **Implement clustering**: Group nearby cafes when zoomed out
4. **Add traffic layers**: Show real-time traffic conditions
5. **Offline maps**: Download maps for offline use
6. **3D buildings**: Enable 3D view for better visualization

## Support

If you encounter issues not covered here:
1. Check [Mapbox Community Forum](https://community.mapbox.com/)
2. Review [GitHub Issues](https://github.com/mapbox/mapbox-maps-android)
3. Contact Mapbox Support (for paid plans)
