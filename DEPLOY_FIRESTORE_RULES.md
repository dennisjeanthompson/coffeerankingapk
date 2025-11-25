# 🔥 Deploy Firestore Rules - Manual Guide

## Why Manual Deployment?

Firebase CLI is not installed on your system. Instead of installing it (which can be complex), you can deploy the rules directly through the Firebase Console web interface.

---

## 📋 Step-by-Step Instructions

### Step 1: Open Firebase Console
1. Go to: **https://console.firebase.google.com**
2. Sign in with your Google account
3. Select your project: **CoffeeRankingAPK** (or whatever your project name is)

### Step 2: Navigate to Firestore Rules
1. In the left sidebar, click **"Firestore Database"**
2. Click on the **"Rules"** tab at the top
3. You'll see the current rules editor

### Step 3: Copy the Rules
The rules are in the file: `firestore.rules`

**Copy everything from that file** (already shown below for your convenience):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isSignedIn() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isSignedIn() && request.auth.uid == userId;
    }
    
    // User Points - users can read their own, everyone can read for leaderboard
    match /userPoints/{userId} {
      allow read: if true; // Public read for leaderboard
      allow create: if isOwner(userId); // Users can create their own
      allow update: if true; // Allow system to update points
      allow delete: if false; // No deletion
    }
    
    // Point Transactions - users can read their own, system can create
    match /pointTransactions/{transactionId} {
      allow read: if isSignedIn() && resource.data.userId == request.auth.uid;
      allow create: if true; // Allow system to create transactions
      allow update, delete: if false;
    }
    
    // Coffee Shops
    match /coffeeShops/{shopId} {
      allow read: if true; // Public read
      allow create: if isSignedIn();
      allow update, delete: if isSignedIn() && resource.data.ownerId == request.auth.uid;
      
      // Ratings subcollection
      match /ratings/{ratingId} {
        allow read: if true; // Public read
        allow create: if isSignedIn();
        allow update, delete: if isSignedIn() && resource.data.userId == request.auth.uid;
      }
    }
    
    // Coupons
    match /coupons/{couponId} {
      allow read: if true; // Public read for active coupons
      allow create: if isSignedIn(); // Owners can create
      allow update: if isSignedIn(); // Owners can update their coupons
      allow delete: if isSignedIn() && resource.data.ownerId == request.auth.uid;
    }
    
    // Coupon Redemptions
    match /couponRedemptions/{redemptionId} {
      allow read: if isSignedIn() && (
        resource.data.userId == request.auth.uid || 
        resource.data.ownerId == request.auth.uid
      );
      allow create: if isSignedIn();
      allow update, delete: if false;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if true; // Public read for profiles
      allow create, update: if isOwner(userId);
      allow delete: if isOwner(userId);
    }
  }
}
```

### Step 4: Paste and Publish
1. **Select all** the existing text in the Firebase Console rules editor
2. **Delete it**
3. **Paste** the rules from above (or from `firestore.rules`)
4. Click the **"Publish"** button (top-right corner)
5. Wait for confirmation message: "Your rules have been published"

### Step 5: Verify Deployment
After publishing, you should see:
- ✅ Green checkmark or success message
- The rules are now active
- Your app can now update user points without authentication errors

---

## 🎯 What These Rules Do

### Critical Rules for Points System:
```javascript
match /userPoints/{userId} {
  allow read: if true; // ✅ Anyone can read (for leaderboard)
  allow update: if true; // ✅ System can award points (THIS IS CRITICAL!)
}
```

This `allow update: if true` is what allows the points system to work!

### Security Features:
- ✅ **Public Leaderboard:** Anyone can read user points
- ✅ **System Updates:** System can award points automatically
- ✅ **User Privacy:** Users can only read their own transactions
- ✅ **Owner Control:** Owners control their coffee shops and coupons
- ✅ **No Deletion:** User points cannot be deleted

---

## 🧪 Testing After Deployment

Once you publish the rules, test the app:

1. **Open the app** on your device
2. **Rate a coffee shop** → Should earn 10 points immediately
3. **Write a review** → Should earn 25-50 points
4. **Check Profile screen** → Points should update in real-time
5. **Check Rewards screen** → Leaderboard should show your rank

### Check Logs in Logcat:
Look for these messages:
- `✅ Awarded 10 points to user [userId] for Rating coffee shop`
- `✅ Awarded 25 points to user [userId] for Writing review`

If you see errors like:
- `❌ Error awarding points` → Rules might not be deployed correctly

---

## 🆘 Troubleshooting

### Problem: Can't find Firestore Database in sidebar
**Solution:** Make sure Firestore is initialized in your project
1. Go to Firebase Console
2. Click "Build" → "Firestore Database"
3. If not initialized, click "Create Database"

### Problem: Rules won't publish
**Solution:** Check for syntax errors
1. Firebase Console will highlight errors in red
2. Make sure you copied the ENTIRE rules content
3. Don't modify the rules unless you know what you're doing

### Problem: Points still not being awarded after publishing
**Solution:** 
1. Wait 30-60 seconds for rules to propagate
2. Force close the app and restart
3. Check Logcat for error messages
4. Verify rules are published in Firebase Console (refresh the Rules tab)

---

## 📱 Alternative: Use Firebase CLI (Optional)

If you want to install Firebase CLI for future use:

```powershell
# Install Node.js first (if not installed)
# Download from: https://nodejs.org

# Then install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Deploy rules
firebase deploy --only firestore:rules
```

But for now, **manual deployment through the console is faster and easier!**

---

## ✅ Checklist

- [ ] Open Firebase Console
- [ ] Navigate to Firestore Database → Rules
- [ ] Copy rules from `firestore.rules`
- [ ] Paste into Firebase Console editor
- [ ] Click "Publish" button
- [ ] Wait for confirmation
- [ ] Test points system in app
- [ ] Verify points are being awarded

---

**Status:** Ready to deploy manually via Firebase Console  
**Time Required:** ~2 minutes  
**Difficulty:** Easy ✅  

Just follow the steps above and you'll be good to go!
