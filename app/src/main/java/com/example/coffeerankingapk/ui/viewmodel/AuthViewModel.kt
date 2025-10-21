package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.UserRole
import com.example.coffeerankingapk.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessful: Boolean = false,
    val pendingRole: UserRole? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val userProfile = authRepository.observeUserProfile()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun signIn() {
        quickSignIn(UserRole.LOVER)
    }

    fun quickSignIn(role: UserRole) {
        val credentials = defaultCredentialsFor(role)
        _uiState.update {
            it.copy(
                email = credentials.email,
                password = credentials.password,
                errorMessage = null,
                pendingRole = role
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pendingRole = role) }

            val result = authRepository.signInWithEmail(
                email = credentials.email,
                password = credentials.password
            )

            if (result.isSuccess) {
                authRepository.updateUserRole(role)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccessful = true,
                        pendingRole = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Unable to sign in",
                        pendingRole = null
                    )
                }
            }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccessful = false, pendingRole = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState()
        }
    }

    private fun defaultCredentialsFor(role: UserRole): Credentials {
        return if (role == UserRole.OWNER) ownerCredentials else loverCredentials
    }

    private data class Credentials(val email: String, val password: String)

    companion object {
        private val loverCredentials = Credentials(email = "lover@example.com", password = "password")
        private val ownerCredentials = Credentials(email = "owner@example.com", password = "password")
    }
}