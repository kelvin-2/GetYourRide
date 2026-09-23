package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.repository.ShuttleDriverRepository
import kotlinx.coroutines.launch

/**
 * Outcome of marking a scanned booking as boarded.
 */
sealed interface ScanBoardResult {
    object Idle : ScanBoardResult
    object Loading : ScanBoardResult
    data class Success(val message: String) : ScanBoardResult
    data class Error(val message: String) : ScanBoardResult
}

/**
 * Small ViewModel behind the shuttle driver's Scan QR screen.
 *
 * The scanner reads a bookingId out of the student's QR; this calls the real
 * boarding endpoint (POST /api/shuttle-driver/boarding/mark) so the student is
 * actually recorded as boarded, rather than just flipping local UI state.
 */
class ShuttleScanViewModel(
    private val repository: ShuttleDriverRepository
) : ViewModel() {

    var boardResult: ScanBoardResult by mutableStateOf(ScanBoardResult.Idle)
        private set

    fun markBoarded(bookingId: Long) {
        boardResult = ScanBoardResult.Loading
        viewModelScope.launch {
            boardResult = try {
                val response = repository.markAsBoarded(bookingId)
                ScanBoardResult.Success(response.message ?: "Student marked as boarded.")
            } catch (e: Exception) {
                ScanBoardResult.Error(e.message ?: "Could not mark the student as boarded.")
            }
        }
    }

    fun reset() {
        boardResult = ScanBoardResult.Idle
    }
}

class ShuttleScanViewModelFactory(
    private val repository: ShuttleDriverRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleScanViewModel(repository) as T
    }
}
