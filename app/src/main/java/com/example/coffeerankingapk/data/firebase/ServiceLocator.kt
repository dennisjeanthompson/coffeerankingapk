package com.example.coffeerankingapk.data.firebase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffeerankingapk.data.fake.FakeAuthRepository
import com.example.coffeerankingapk.data.fake.FakeCafeRepository
import com.example.coffeerankingapk.data.fake.FakeOwnerRepository
import com.example.coffeerankingapk.data.repository.AuthRepository
import com.example.coffeerankingapk.data.repository.CafeRepository
import com.example.coffeerankingapk.data.repository.OwnerRepository
import com.example.coffeerankingapk.ui.viewmodel.AuthViewModel
import com.example.coffeerankingapk.ui.viewmodel.CafeDetailViewModel
import com.example.coffeerankingapk.ui.viewmodel.LoverDashboardViewModel
import com.example.coffeerankingapk.ui.viewmodel.OwnerAnalyticsViewModel
import com.example.coffeerankingapk.ui.viewmodel.OwnerCouponsViewModel
import com.example.coffeerankingapk.ui.viewmodel.OwnerDashboardViewModel
import com.example.coffeerankingapk.ui.viewmodel.ProfileViewModel
import com.example.coffeerankingapk.ui.viewmodel.RateViewModel
import com.example.coffeerankingapk.ui.viewmodel.RoleSelectViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {

    private const val USE_FAKE_BACKEND = true

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val fakeAuthRepository by lazy { FakeAuthRepository() }
    private val fakeCafeRepository by lazy { FakeCafeRepository() }
    private val fakeOwnerRepository by lazy { FakeOwnerRepository() }

    val authRepository: AuthRepository by lazy {
        if (USE_FAKE_BACKEND) fakeAuthRepository else FirebaseAuthRepository(firebaseAuth, firestore)
    }

    val cafeRepository: CafeRepository by lazy {
        if (USE_FAKE_BACKEND) fakeCafeRepository else FirebaseCafeRepository(firebaseAuth, firestore)
    }

    val ownerRepository: OwnerRepository by lazy {
        if (USE_FAKE_BACKEND) fakeOwnerRepository else FirebaseOwnerRepository(firebaseAuth, firestore)
    }

    val isUsingFirebaseBackend: Boolean
        get() = !USE_FAKE_BACKEND

    fun provideDefaultViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authRepository) as T
            }
            if (modelClass.isAssignableFrom(RoleSelectViewModel::class.java)) {
                return RoleSelectViewModel(authRepository) as T
            }
            if (modelClass.isAssignableFrom(LoverDashboardViewModel::class.java)) {
                return LoverDashboardViewModel(cafeRepository, authRepository) as T
            }
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(authRepository, cafeRepository) as T
            }
            if (modelClass.isAssignableFrom(OwnerDashboardViewModel::class.java)) {
                return OwnerDashboardViewModel(ownerRepository) as T
            }
            if (modelClass.isAssignableFrom(OwnerCouponsViewModel::class.java)) {
                return OwnerCouponsViewModel(ownerRepository) as T
            }
            if (modelClass.isAssignableFrom(OwnerAnalyticsViewModel::class.java)) {
                return OwnerAnalyticsViewModel(ownerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun provideCafeDetailViewModelFactory(cafeId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CafeDetailViewModel::class.java)) {
                return CafeDetailViewModel(cafeId, cafeRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun provideRateViewModelFactory(cafeId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RateViewModel::class.java)) {
                return RateViewModel(cafeId, cafeRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}