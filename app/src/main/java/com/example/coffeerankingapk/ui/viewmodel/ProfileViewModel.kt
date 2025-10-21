package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.repository.AuthRepository
import com.example.coffeerankingapk.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = "Coffee Lover",
    val email: String = "",
    val reviewsCount: Int = 0,
    val favoritesCount: Int = 0,
    val points: Int = 0,
    val pushNotificationsEnabled: Boolean = true,
    val emailUpdatesEnabled: Boolean = false,
    val favoriteCafes: List<Cafe> = emptyList(),
    val errorMessage: String? = null,
    val signOutComplete: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val cafeRepository: CafeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                authRepository.updateNotificationPreferences(
                    pushEnabled = enabled,
                    emailEnabled = _uiState.value.emailUpdatesEnabled
                )
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.localizedMessage) }
            }
        }
    }

    fun toggleEmailUpdates(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                authRepository.updateNotificationPreferences(
                    pushEnabled = _uiState.value.pushNotificationsEnabled,
                    emailEnabled = enabled
                )
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.localizedMessage) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(signOutComplete = true) }
        }
    }

    fun consumeSignOut() {
        _uiState.update { it.copy(signOutComplete = false) }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                authRepository.observeUserProfile(),
                cafeRepository.observeCafes(),
                cafeRepository.observeFavoriteIds()
            ) { profile, cafes, favorites ->
                Triple(profile, cafes, favorites)
            }.collect { (profile, cafes, favorites) ->
                if (profile == null) {
                    _uiState.update { it.copy(isLoading = false, favoriteCafes = emptyList()) }
                } else {
                    val favoriteCafes = cafes.filter { favorites.contains(it.id) }.take(6)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = profile.name,
                            email = profile.email,
                            reviewsCount = profile.reviewsCount,
                            favoritesCount = favorites.size,
                            points = profile.points,
                            pushNotificationsEnabled = profile.pushNotificationsEnabled,
                            emailUpdatesEnabled = profile.emailUpdatesEnabled,
                            favoriteCafes = favoriteCafes,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}