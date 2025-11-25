package com.example.coffeerankingapk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.UserPoints
import com.example.coffeerankingapk.data.model.PointTransaction
import com.example.coffeerankingapk.data.repository.PointsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PointsViewModel : ViewModel() {
    private val repository = PointsRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _userPoints = MutableStateFlow<UserPoints?>(null)
    val userPoints: StateFlow<UserPoints?> = _userPoints.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<UserPoints>>(emptyList())
    val leaderboard: StateFlow<List<UserPoints>> = _leaderboard.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<PointTransaction>>(emptyList())
    val transactions: StateFlow<List<PointTransaction>> = _transactions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _pointsAwarded = MutableStateFlow<Int?>(null)
    val pointsAwarded: StateFlow<Int?> = _pointsAwarded.asStateFlow()
    
    companion object {
        private const val TAG = "PointsViewModel"
    }
    
    init {
        loadUserPoints()
    }
    
    /**
     * Load current user's points with real-time updates
     */
    fun loadUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.observeUserPoints(userId).collect { result ->
                result.onSuccess { points ->
                    _userPoints.value = points
                    _isLoading.value = false
                    Log.d(TAG, "User points loaded: ${points.totalPoints}")
                }.onFailure { e ->
                    _error.value = "Failed to load points: ${e.message}"
                    _isLoading.value = false
                    Log.e(TAG, "Error loading user points", e)
                }
            }
        }
    }
    
    /**
     * Load leaderboard with real-time updates
     */
    fun loadLeaderboard(limit: Int = 100) {
        viewModelScope.launch {
            repository.getLeaderboard(limit).collect { result ->
                result.onSuccess { leaderboardList ->
                    _leaderboard.value = leaderboardList
                    Log.d(TAG, "Leaderboard loaded: ${leaderboardList.size} users")
                }.onFailure { e ->
                    _error.value = "Failed to load leaderboard: ${e.message}"
                    Log.e(TAG, "Error loading leaderboard", e)
                }
            }
        }
    }
    
    /**
     * Load user's transaction history
     */
    fun loadTransactions(limit: Int = 50) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            repository.getUserTransactions(userId, limit).collect { result ->
                result.onSuccess { transactionList ->
                    _transactions.value = transactionList
                    Log.d(TAG, "Transactions loaded: ${transactionList.size}")
                }.onFailure { e ->
                    _error.value = "Failed to load transactions: ${e.message}"
                    Log.e(TAG, "Error loading transactions", e)
                }
            }
        }
    }
    
    /**
     * Award points for rating a cafe
     */
    fun awardPointsForRating(ratingId: String, cafeId: String, cafeName: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            // Check if already rewarded
            repository.hasBeenRewarded(userId, "rating", ratingId).onSuccess { alreadyRewarded ->
                if (alreadyRewarded) {
                    Log.d(TAG, "User already rewarded for this rating")
                    return@onSuccess
                }
                
                // Award points
                repository.awardPoints(
                    userId = userId,
                    points = UserPoints.POINTS_PER_RATING,
                    action = "rating",
                    description = "Rated $cafeName",
                    relatedId = ratingId
                ).onSuccess {
                    _pointsAwarded.value = UserPoints.POINTS_PER_RATING
                    Log.d(TAG, "Awarded ${UserPoints.POINTS_PER_RATING} points for rating")
                    
                    // Clear after 3 seconds
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _pointsAwarded.value = null
                    }
                }.onFailure { e ->
                    _error.value = "Failed to award points: ${e.message}"
                    Log.e(TAG, "Error awarding points for rating", e)
                }
            }
        }
    }
    
    /**
     * Award points for writing a review
     */
    fun awardPointsForReview(reviewId: String, cafeId: String, cafeName: String, reviewText: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            // Check if already rewarded
            repository.hasBeenRewarded(userId, "review", reviewId).onSuccess { alreadyRewarded ->
                if (alreadyRewarded) {
                    Log.d(TAG, "User already rewarded for this review")
                    return@onSuccess
                }
                
                // Award more points for detailed reviews
                val points = if (reviewText.length >= 50) {
                    UserPoints.POINTS_PER_DETAILED_REVIEW
                } else {
                    UserPoints.POINTS_PER_REVIEW
                }
                
                repository.awardPoints(
                    userId = userId,
                    points = points,
                    action = "review",
                    description = "Reviewed $cafeName",
                    relatedId = reviewId
                ).onSuccess {
                    _pointsAwarded.value = points
                    Log.d(TAG, "Awarded $points points for review")
                    
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _pointsAwarded.value = null
                    }
                }.onFailure { e ->
                    _error.value = "Failed to award points: ${e.message}"
                    Log.e(TAG, "Error awarding points for review", e)
                }
            }
        }
    }
    
    /**
     * Award points for redeeming a coupon
     */
    fun awardPointsForCoupon(couponId: String, shopName: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            repository.awardPoints(
                userId = userId,
                points = UserPoints.POINTS_PER_COUPON_REDEEM,
                action = "coupon_redeem",
                description = "Redeemed coupon at $shopName",
                relatedId = couponId
            ).onSuccess {
                _pointsAwarded.value = UserPoints.POINTS_PER_COUPON_REDEEM
                Log.d(TAG, "Awarded ${UserPoints.POINTS_PER_COUPON_REDEEM} points for coupon")
                
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _pointsAwarded.value = null
                }
            }.onFailure { e ->
                _error.value = "Failed to award points: ${e.message}"
                Log.e(TAG, "Error awarding points for coupon", e)
            }
        }
    }
    
    /**
     * Award points for visiting a cafe
     */
    fun awardPointsForVisit(cafeId: String, cafeName: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            repository.awardPoints(
                userId = userId,
                points = UserPoints.POINTS_PER_CAFE_VISIT,
                action = "cafe_visit",
                description = "Visited $cafeName",
                relatedId = cafeId
            ).onSuccess {
                _pointsAwarded.value = UserPoints.POINTS_PER_CAFE_VISIT
                Log.d(TAG, "Awarded ${UserPoints.POINTS_PER_CAFE_VISIT} points for visit")
                
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _pointsAwarded.value = null
                }
            }.onFailure { e ->
                _error.value = "Failed to award points: ${e.message}"
                Log.e(TAG, "Error awarding points for visit", e)
            }
        }
    }
    
    /**
     * Get user's rank in leaderboard
     */
    fun updateUserRank() {
        val userId = auth.currentUser?.uid ?: return
        val points = _userPoints.value?.totalPoints ?: return
        
        viewModelScope.launch {
            repository.getUserRank(userId, points).onSuccess { rank ->
                Log.d(TAG, "User rank updated: #$rank")
            }.onFailure { e ->
                Log.e(TAG, "Error updating user rank", e)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear points awarded notification
     */
    fun clearPointsAwarded() {
        _pointsAwarded.value = null
    }
}
