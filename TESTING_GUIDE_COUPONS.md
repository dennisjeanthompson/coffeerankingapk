# Quick Start Guide - Testing Coupon System

## Prerequisites
1. ✅ Build completed successfully
2. ⚠️ **Deploy Firestore Security Rules** (see below)
3. ⚠️ Ensure you have a Firebase project connected
4. ⚠️ Ensure you're logged in as a cafe owner

## Step 1: Deploy Firestore Security Rules

### Via Firebase Console (Recommended)
1. Go to https://console.firebase.google.com
2. Select your project: `coffeerankingapk`
3. Navigate to **Firestore Database** → **Rules**
4. Copy the rules from `FIRESTORE_COUPON_RULES.md`
5. Paste into the rules editor
6. Click **Publish**

### Via Firebase CLI (Alternative)
```powershell
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login
firebase login

# Initialize (if not done)
firebase init firestore

# Edit firestore.rules file with rules from FIRESTORE_COUPON_RULES.md

# Deploy
firebase deploy --only firestore:rules
```

## Step 2: Run the App

```powershell
# Connect your Android device or start an emulator
adb devices

# Install the app
cd c:\Users\admin\coffeerank
.\gradlew.bat installDebug

# Or run directly
.\gradlew.bat :app:installDebug
```

## Step 3: Test Coupon Creation

### As a Cafe Owner:

1. **Login** as a cafe owner account
2. **Navigate** to the Coupons tab (Star icon in bottom navigation)
3. **Create a shop first** if you haven't already:
   - Go to Map tab
   - Add a coffee shop
   - Return to Coupons tab

4. **Create your first coupon**:
   - Tap the **+** FAB button
   - Fill in:
     - Title: "Welcome Offer"
     - Description: "New customers get 20% off"
     - Discount: 20 (in percent field)
     - Minimum Purchase: 10
     - Start Date: Today
     - Expiry Date: 30 days from now
     - Leave "Unlimited Redemptions" ON
   - Tap **Create Coupon**

5. **Verify creation**:
   - You should see a success snackbar
   - The coupon should appear in the list
   - Status should be "Active"

## Step 4: Test Coupon Management

### Toggle Status
- Tap the **Switch** to deactivate the coupon
- Verify status changes to "Inactive"
- Toggle back to activate

### Edit Coupon
- Tap the **Edit** icon (pencil)
- Modify any field (e.g., change discount to 25%)
- Tap **Save Changes**
- Verify changes appear in the list

### Delete Coupon
- Tap the **Delete** icon (trash)
- Coupon should be soft-deleted (deactivated)
- It will still appear in the list as "Inactive"

## Step 5: Test Edge Cases

### Expired Coupons
1. Create a coupon with:
   - Start Date: 7 days ago
   - Expiry Date: Yesterday
2. Verify it shows "EXPIRED" badge
3. Verify the toggle switch is disabled

### Max Redemptions
1. Create a coupon with:
   - Unlimited Redemptions: OFF
   - Max Redemptions: 5
2. Verify "Redeemed: 0/5" appears
3. (To test redemption, you'd need to implement the user-side redemption feature)

### Multiple Coupons
1. Create 5-10 coupons with different:
   - Titles
   - Discounts (mix % and $)
   - Dates
   - Statuses
2. Verify they all appear in the list
3. Verify they update in real-time

## Step 6: Verify Firestore Data

### Via Firebase Console
1. Go to **Firestore Database** → **Data**
2. Open the `coupons` collection
3. Verify documents are created with correct fields:
   - `shopId` matches your shop
   - `title`, `description`, etc.
   - `createdAt` and `updatedAt` timestamps
   - `currentRedemptions` = 0
   - `isActive` = true/false

### Via Logcat
```powershell
# Filter logs for coupon operations
adb logcat -s CouponRepository CouponViewModel

# You should see logs like:
# D/CouponRepository: Coupon created: abc123, shop: xyz789
# D/CouponViewModel: Loaded 5 coupons for shop: xyz789
```

## Common Issues & Troubleshooting

### Issue: "Please add a coffee shop first"
**Solution**: Navigate to Map tab and add a coffee shop with your owner account.

### Issue: "Failed to create coupon: Permission denied"
**Solution**: 
1. Verify Firestore rules are deployed
2. Check that the shop's `ownerId` matches your authenticated user ID
3. Review Firestore Console → Rules → Request logs

### Issue: Coupons not appearing
**Solution**:
1. Check Logcat for errors
2. Verify Firestore connection (check `coffeeShops` collection)
3. Ensure you're loading the correct shop ID

### Issue: Date picker not working
**Solution**: 
1. Verify `io.github.vanpra.compose-material-dialogs:datetime:0.9.0` dependency
2. Check if you need to update the library version
3. Use alternative: `DatePickerDialog` from Material3

### Issue: Real-time updates not working
**Solution**:
1. Check internet connection
2. Verify Firestore real-time listeners (Logcat: `CouponRepository`)
3. Force refresh by navigating away and back

## Testing Checklist

### Basic Functionality
- [ ] Can create a coupon
- [ ] Coupon appears in list immediately
- [ ] Can edit coupon
- [ ] Changes appear in real-time
- [ ] Can toggle active/inactive
- [ ] Can delete coupon
- [ ] Empty state shows when no coupons

### Validation
- [ ] Cannot create with empty title
- [ ] Discount percent must be 1-100
- [ ] Expiry must be after start date
- [ ] Cannot create without a shop

### UI/UX
- [ ] Loading spinner shows during operations
- [ ] Success messages appear
- [ ] Error messages appear
- [ ] Date pickers work smoothly
- [ ] Navigation flows correctly
- [ ] Back button returns to previous screen

### Data Persistence
- [ ] Coupons persist after app restart
- [ ] Changes sync across devices (if testing with multiple devices)
- [ ] Firestore data structure is correct
- [ ] Timestamps are accurate

## Next Steps After Testing

### For Production:
1. Add Firestore indexes (console will prompt)
2. Implement user-side coupon redemption
3. Add coupon analytics/reporting
4. Add push notifications for new coupons
5. Implement QR code scanning for redemption
6. Add coupon validation on checkout

### For Enhancement:
1. Bulk operations (activate/deactivate multiple)
2. Coupon templates
3. Recurring coupons (weekly specials)
4. Location-based coupons
5. First-time user coupons
6. Loyalty program integration

## Support Commands

### Check app logs
```powershell
adb logcat | Select-String -Pattern "Coupon"
```

### Clear app data (reset)
```powershell
adb shell pm clear com.visonaries.caffinity
```

### Reinstall app
```powershell
.\gradlew.bat uninstallDebug installDebug
```

### Check Firestore rules
```powershell
firebase firestore:rules:get
```

---

**Happy Testing! 🎉**

If you encounter any issues, check:
1. Logcat output
2. Firebase Console → Firestore → Data
3. Firebase Console → Firestore → Rules → Request logs
4. `COUPON_SYSTEM_IMPLEMENTATION.md` for detailed architecture
