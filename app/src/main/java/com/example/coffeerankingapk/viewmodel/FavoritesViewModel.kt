package com.example.coffeerankingapk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Favorite
import com.example.coffeerankingapk.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    val userFavorites: Flow<List<Favorite>> = repository.getUserFavorites()
        .map { result ->
            result.getOrElse { emptyList() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun isFavorite(shopId: String): Flow<Boolean> {
        val flow = MutableStateFlow(false)
        viewModelScope.launch {
            val result = repository.isFavorite(shopId)
            flow.value = result.getOrElse { false }
        }
        return flow
    }

    fun addFavorite(
        shopId: String,
        shopName: String,
        shopAddress: String,
        averageRating: Float
    ) {
        viewModelScope.launch {
            repository.addFavorite(
                shopId = shopId,
                shopName = shopName,
                shopAddress = shopAddress,
                averageRating = averageRating.toDouble()
            )
        }
    }

    fun removeFavorite(shopId: String) {
        viewModelScope.launch {
            repository.removeFavorite(shopId)
        }
    }
}
