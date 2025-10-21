package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RateUiState(
    val cafe: Cafe? = null,
    val isSubmitting: Boolean = false,
    val submissionSucceeded: Boolean = false,
    val errorMessage: String? = null
)

class RateViewModel(
    private val cafeId: String,
    private val cafeRepository: CafeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RateUiState())
    val uiState: StateFlow<RateUiState> = _uiState.asStateFlow()

    init {
        if (cafeId.isNotBlank()) {
            observeCafe()
        } else {
            _uiState.update { it.copy(errorMessage = "Cafe not found") }
        }
    }

    fun submitReview(
        rating: Int,
        comment: String,
        quickNote: String?,
        isCoffeeHot: Boolean?,
        photoUrl: String?
    ) {
        if (cafeId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, submissionSucceeded = false) }
            val result = cafeRepository.submitReview(
                cafeId = cafeId,
                rating = rating,
                comment = comment,
                quickNote = quickNote,
                isCoffeeHot = isCoffeeHot,
                photoUrl = photoUrl
            )
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, submissionSucceeded = true)
                } else {
                    it.copy(
                        isSubmitting = false,
                        submissionSucceeded = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to submit review"
                    )
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(submissionSucceeded = false) }
    }

    private fun observeCafe() {
        viewModelScope.launch {
            cafeRepository.observeCafe(cafeId).collect { cafe ->
                _uiState.update { it.copy(cafe = cafe) }
            }
        }
    }
}