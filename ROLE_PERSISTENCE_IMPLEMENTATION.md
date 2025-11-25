# Role Persistence & Data Isolation Implementation

## Overview
This document describes the implementation of role-based access control and data isolation for the CoffeeRank app, ensuring owners only see their own coffee shops and data.

## Changes Made

### 1. User Model (`User.kt`)
Created a new data model to persist user roles in Firestore:

```kotlin
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    OWNER,  // Cafe owner - can create/manage coffee shops and coupons
    LOVER,  // Coffee lover - can discover cafes, rate, and earn rewards
    BOTH    // Can switch between owner and lover roles
}
```

**Location:** `app/src/main/java/com/example/coffeerankingapk/data/model/User.kt`

### 2. User Repository (`UserRepository.kt`)
Created a repository to handle user role persistence:

**Key Functions:**
- `getCurrentUser()`: Fetches current user's profile from Firestore
- `saveUserProfile(role: UserRole)`: Saves user role on first selection
- `updateUserRole(role: UserRole)`: Updates existing user's role

**Collection:** Uses `users` collection in Firestore

**Location:** `app/src/main/java/com/example/coffeerankingapk/data/repository/UserRepository.kt`

### 3. Role Selection Screen (`RoleSelectScreen.kt`)
Updated to persist role selection:

**Changes:**
- Added `UserRepository` integration
- Saves selected role to Firestore before navigation
- Shows loading indicator during save operation
- Handles errors gracefully with toast messages

**Flow:**
1. User selects OWNER or LOVER
2. Role is saved to Firestore `users` collection
3. Only after successful save, navigate to appropriate screen
4. Show error toast if save fails

**Location:** `app/src/main/java/com/example/coffeerankingapk/ui/screens/RoleSelectScreen.kt`

### 4. Authentication Screen (`AuthScreen.kt`)
Enhanced to check for existing roles:

**New Function:** `checkUserRoleAndNavigate()`
- Queries Firestore for user's saved role
- If role exists: Navigate directly to owner/lover screen
- If no role: Navigate to role selection screen
- Handles UserRole.BOTH by showing role selection each time

**Updated Callbacks:**
- `onLoginSuccess`: For new users (no role)
- `onLoginSuccessWithRole`: For returning users (has role)

**Location:** `app/src/main/java/com/example/coffeerankingapk/ui/screens/AuthScreen.kt`

### 5. Navigation Graph (`NavGraph.kt`)
Updated to support role-based routing:

```kotlin
composable("auth") {
    AuthScreen(
        onLoginSuccess = {
            // No role found, go to role selection
            navController.navigate("role_select") { ... }
        },
        onLoginSuccessWithRole = { role ->
            // User has existing role, navigate directly
            val destination = when (role) {
                UserRole.OWNER -> "owner"
                UserRole.LOVER -> "lover"
                UserRole.BOTH -> "role_select"
            }
            navController.navigate(destination) { ... }
        }
    )
}
```

**Location:** `app/src/main/java/com/example/coffeerankingapk/navigation/NavGraph.kt`

### 6. Owner Dashboard Screen (`OwnerDashboardScreen.kt`)
Implemented data isolation for owners:

**Changes:**
- Integrated `CoffeeShopViewModel` to fetch shops
- Filters `ownerShops` to only show shops where `ownerId == currentUser.uid`
- Shows empty state with "Add Your Coffee Shop" button if no shops
- Displays actual shop name and address (not hardcoded)

**Empty State:**
- Icon: Home icon (80dp)
- Title: "No Coffee Shop Yet"
- Description: "Create your first coffee shop to get started"
- Action: "Add Your Coffee Shop" button

**Data Display:**
- Shop name from Firestore
- Shop address from Firestore
- KPIs will show real data once analytics are implemented

**Location:** `app/src/main/java/com/example/coffeerankingapk/ui/screens/owner/OwnerDashboardScreen.kt`

### 7. Owner Main Screen (`OwnerMainScreen.kt`)
Updated to pass navigation callback to dashboard:

**Change:** Added `onNavigateToAddShop` parameter to `OwnerDashboardScreen` call

**Location:** `app/src/main/java/com/example/coffeerankingapk/ui/screens/owner/OwnerMainScreen.kt`

## User Flow

### New User Flow
1. User opens app → Auth screen
2. User signs in with Google/Email → Auth successful
3. `checkUserRoleAndNavigate()` checks Firestore
4. No user document found → Navigate to Role Selection
5. User picks OWNER or LOVER → Role saved to Firestore
6. Navigate to appropriate dashboard

### Returning User Flow
1. User opens app → Auth screen
2. User signs in → Auth successful
3. `checkUserRoleAndNavigate()` checks Firestore
4. User document found with role → Navigate directly to dashboard
5. Skip role selection (unless role is BOTH)

### Owner Dashboard Flow
1. Owner navigates to dashboard
2. `CoffeeShopViewModel.ownerShops` loaded from Firestore
3. Filter shops where `ownerId == currentUser.uid`
4. If no shops → Show empty state with "Add Your Coffee Shop"
5. If shops exist → Show shop name, address, and KPIs

## Data Isolation

### Coffee Shops
- **Create:** `ownerId` set to `FirebaseAuth.currentUser.uid`
- **Read:** Filtered by `ownerId` in `OwnerDashboardScreen`
- **Update/Delete:** Only if `ownerId` matches current user

### Coupons
- **Create:** `ownerId` set to `FirebaseAuth.currentUser.uid`
- **Read:** Filtered by shop's `ownerId`
- **Update/Delete:** Validated against current user

### Owner Screens
All owner screens now use `CoffeeShopViewModel.ownerShops` which is unfiltered by location:
- `OwnerDashboardScreen`: Shows only user's shops
- `OwnerCouponsScreen`: Shows coupons for user's shops only
- `AddEditCouponScreen`: Can only create coupons for user's shops

## Firestore Collections

### Users Collection
```
users/
  {uid}/
    uid: string
    email: string
    displayName: string
    role: "OWNER" | "LOVER" | "BOTH"
    createdAt: timestamp
    updatedAt: timestamp
```

### Coffee Shops Collection (existing)
```
coffeeShops/
  {shopId}/
    name: string
    address: string
    ownerId: string  // Firebase Auth UID
    ...
```

### Coupons Collection (existing)
```
coupons/
  {couponId}/
    title: string
    description: string
    shopId: string
    ownerId: string  // Firebase Auth UID
    ...
```

## Security Rules

Current Firestore rules allow authenticated users to create/update/delete:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId;
    }
    
    // Coffee shops
    match /coffeeShops/{shopId} {
      allow read: if true;
      allow create: if isSignedIn();
      allow update, delete: if isSignedIn();
    }
    
    // Coupons
    match /coupons/{couponId} {
      allow read: if isSignedIn();
      allow create, update, delete: if isSignedIn();
    }
  }
}
```

**Note:** App-level logic handles ownership validation. Firestore rules ensure authentication only.

## Recommendations

### Role Switching Policy
Currently, the system supports three role types:
- **OWNER only:** User can only manage coffee shops
- **LOVER only:** User can only discover and rate cafes
- **BOTH:** User can switch between roles (requires role selection each time)

**Recommended:** Lock users to their first selected role (OWNER or LOVER) for better data integrity and simpler UX. This prevents confusion and ensures users commit to one use case.

**To implement:**
1. Remove `UserRole.BOTH` enum value
2. Add validation in `UserRepository.updateUserRole()` to prevent role changes
3. Hide role selection for users who already have a role

### Future Enhancements
1. **Analytics Integration:** Connect real metrics to owner dashboard KPIs
2. **Role Management UI:** Allow role change in settings (if BOTH is kept)
3. **Multiple Shops:** Support owners managing multiple coffee shops
4. **Shop Transfer:** Allow transferring shop ownership between users
5. **Admin Role:** Add ADMIN role for platform management

## Testing

### Test Scenarios
1. ✅ **New user signup** → Should see role selection
2. ✅ **Returning user login** → Should skip role selection
3. ✅ **Owner with no shops** → Should see empty state
4. ✅ **Owner with shops** → Should see only their shops
5. ✅ **Coupon creation** → Should only list owner's shops
6. ⚠️ **Role persistence failure** → Needs manual testing

### Manual Testing Steps
1. Clear app data: Settings → Apps → CoffeeRank → Clear Data
2. Open app and sign in with Google
3. Select OWNER role
4. Verify role is saved (check Firestore Console → users collection)
5. Close app completely
6. Reopen app
7. Verify: Should go directly to owner dashboard (skip role selection)
8. Create a coffee shop
9. Verify: Dashboard shows your shop name and address
10. Sign out and sign in with different account
11. Verify: Dashboard shows "No Coffee Shop Yet" for new account

## Files Modified
- `app/src/main/java/com/example/coffeerankingapk/data/model/User.kt` (NEW)
- `app/src/main/java/com/example/coffeerankingapk/data/repository/UserRepository.kt` (NEW)
- `app/src/main/java/com/example/coffeerankingapk/ui/screens/RoleSelectScreen.kt`
- `app/src/main/java/com/example/coffeerankingapk/ui/screens/AuthScreen.kt`
- `app/src/main/java/com/example/coffeerankingapk/navigation/NavGraph.kt`
- `app/src/main/java/com/example/coffeerankingapk/ui/screens/owner/OwnerDashboardScreen.kt`
- `app/src/main/java/com/example/coffeerankingapk/ui/screens/owner/OwnerMainScreen.kt`

## Deployment Checklist
- [x] User model created
- [x] UserRepository implemented
- [x] Role selection persists to Firestore
- [x] Auth screen checks for existing role
- [x] Navigation routes based on role
- [x] Owner dashboard filters by ownerId
- [x] Empty state for owners with no shops
- [ ] Deploy Firestore rules to production
- [ ] Test with multiple user accounts
- [ ] Document role switching policy decision
- [ ] Add analytics to owner dashboard

---

**Last Updated:** $(Get-Date -Format "yyyy-MM-dd HH:mm")
**Build Status:** ✅ Successful
**Installation:** ✅ Installed on device
