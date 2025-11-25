# Firestore Security Rules for Coupons

Add these rules to your Firebase Firestore Security Rules to secure the coupon collection.

## Complete Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Coffee Shops Collection
    match /coffeeShops/{shopId} {
      // Anyone can read coffee shops
      allow read: if true;
      
      // Only authenticated users can create shops
      allow create: if request.auth != null;
      
      // Only the owner can update their shop
      allow update: if request.auth != null && 
                       request.auth.uid == resource.data.ownerId;
      
      // Only the owner can delete their shop
      allow delete: if request.auth != null && 
                       request.auth.uid == resource.data.ownerId;
      
      // Ratings subcollection
      match /ratings/{ratingId} {
        // Anyone can read ratings
        allow read: if true;
        
        // Only authenticated users can create ratings
        allow create: if request.auth != null;
      }
    }
    
    // Coupons Collection
    match /coupons/{couponId} {
      // Anyone can read active, non-expired coupons
      allow read: if true;
      
      // Only authenticated users can create coupons
      // Must set shopId to a shop they own
      allow create: if request.auth != null &&
                       request.resource.data.shopId is string &&
                       exists(/databases/$(database)/documents/coffeeShops/$(request.resource.data.shopId)) &&
                       get(/databases/$(database)/documents/coffeeShops/$(request.resource.data.shopId)).data.ownerId == request.auth.uid;
      
      // Only the shop owner can update their coupons
      allow update: if request.auth != null &&
                       exists(/databases/$(database)/documents/coffeeShops/$(resource.data.shopId)) &&
                       get(/databases/$(database)/documents/coffeeShops/$(resource.data.shopId)).data.ownerId == request.auth.uid;
      
      // Only the shop owner can delete their coupons
      allow delete: if request.auth != null &&
                       exists(/databases/$(database)/documents/coffeeShops/$(resource.data.shopId)) &&
                       get(/databases/$(database)/documents/coffeeShops/$(resource.data.shopId)).data.ownerId == request.auth.uid;
      
      // Redemptions subcollection
      match /redemptions/{redemptionId} {
        // Only the user who redeemed can see their redemptions
        allow read: if request.auth != null && 
                      request.auth.uid == resource.data.userId;
        
        // Redemptions are created via transactions in the repository
        allow create: if request.auth != null;
      }
    }
    
    // Users Collection (if needed)
    match /users/{userId} {
      // Users can read their own data
      allow read: if request.auth != null && request.auth.uid == userId;
      
      // Users can write their own data
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Rule Explanations

### Coupons Collection Rules

1. **Read Access (Public)**
   - All users can read coupons (filtering for active/expired happens in the app)
   - Alternative: Add server-side filtering with conditions

2. **Create Access (Owner Only)**
   - User must be authenticated
   - Must reference a valid coffee shop
   - The referenced shop must belong to the authenticated user

3. **Update Access (Owner Only)**
   - User must be authenticated
   - The coupon's shop must belong to the authenticated user

4. **Delete Access (Owner Only)**
   - User must be authenticated
   - The coupon's shop must belong to the authenticated user

### Redemptions Subcollection

- Users can only read their own redemptions
- Redemptions are created via secure transactions in `CouponRepository.redeemCoupon()`

## Testing Security Rules

Use the Firebase Console Rules Playground or write unit tests:

```javascript
// Test: Owner can create coupon
allow create: if request.auth.uid == "owner123" &&
                 request.resource.data.shopId == "shop456" &&
                 get(/databases/$(database)/documents/coffeeShops/shop456).data.ownerId == "owner123";

// Test: Non-owner cannot create coupon for another shop
deny create: if request.auth.uid == "user999" &&
                request.resource.data.shopId == "shop456" &&
                get(/databases/$(database)/documents/coffeeShops/shop456).data.ownerId != "user999";
```

## Firestore Indexes

Create these composite indexes for better query performance:

### Coupons Collection

```
Collection: coupons
Fields:
  - shopId (Ascending)
  - isActive (Ascending)
  - expiryDate (Ascending)

Collection: coupons
Fields:
  - shopId (Ascending)
  - createdAt (Descending)
```

## Deployment

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Navigate to **Firestore Database** > **Rules**
4. Copy and paste the rules above
5. Click **Publish**

## Additional Security Considerations

1. **Rate Limiting**: Implement Cloud Functions to prevent abuse
2. **Data Validation**: Add field validation in rules (e.g., discount percent between 0-100)
3. **Audit Logging**: Track coupon creation/redemption in Cloud Functions
4. **Soft Deletes**: Use `isActive: false` instead of hard deletes for audit trail

## Example Enhanced Rules with Validation

```javascript
match /coupons/{couponId} {
  allow create: if request.auth != null &&
                   validateCoupon(request.resource.data) &&
                   ownsShop(request.resource.data.shopId);
  
  function validateCoupon(coupon) {
    return coupon.title is string && coupon.title.size() > 0 &&
           coupon.discountPercent >= 0 && coupon.discountPercent <= 100 &&
           coupon.discountAmount >= 0 &&
           coupon.expiryDate > request.time;
  }
  
  function ownsShop(shopId) {
    return exists(/databases/$(database)/documents/coffeeShops/$(shopId)) &&
           get(/databases/$(database)/documents/coffeeShops/$(shopId)).data.ownerId == request.auth.uid;
  }
}
```
