package com.example.coffeerankingapk

import android.app.Application
import android.util.Log
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class CoffeeRankingApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		if (ServiceLocator.isUsingFirebaseBackend) {
			try {
				FirebaseApp.initializeApp(this)
				val settings = FirebaseFirestoreSettings.Builder()
					.setPersistenceEnabled(true)
					.build()
				FirebaseFirestore.getInstance().firestoreSettings = settings
			} catch (error: Exception) {
				Log.w("CoffeeRankingApp", "Firebase init skipped: ${error.localizedMessage}")
			}
		}
	}
}