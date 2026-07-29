package com.example.getyourride.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.DeleteDriverProfileRequest
import com.example.getyourride.data.OfferRideRequest
import com.example.getyourride.data.StudentProfileRequest
import com.example.getyourride.data.UseCaseSubmitStatus
import com.example.getyourride.data.ValidationResult
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.repository.GeocodingRepository
import com.example.getyourride.network.ApiService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModels for various use cases.
 * API calls have been mocked to resolve issues with the network service as requested.
 */

@OptIn(FlowPreview::class)
class OfferRideViewModel(
    private val apiService: ApiService,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var submitStatus by mutableStateOf<UseCaseSubmitStatus>(UseCaseSubmitStatus.Idle)
        private set

    // --- Autocomplete Logic ---
    private val _pickup = MutableStateFlow(LocationFieldState())
    val pickup: StateFlow<LocationFieldState> = _pickup

    private val _destination = MutableStateFlow(LocationFieldState())
    val destination: StateFlow<LocationFieldState> = _destination

    private val pickupQuery = MutableStateFlow("")
    private val destinationQuery = MutableStateFlow("")

    init {
        observeQuery(pickupQuery, _pickup)
        observeQuery(destinationQuery, _destination)
    }

    private fun observeQuery(queryFlow: MutableStateFlow<String>, stateFlow: MutableStateFlow<LocationFieldState>) {
        viewModelScope.launch {
            queryFlow
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.length >= 3 }
                .collectLatest { query ->
                    if (stateFlow.value.text != query) return@collectLatest
                    stateFlow.update { it.copy(isLoading = true) }

                    geocodingRepository.suggest(query)
                        .onSuccess { results ->
                            stateFlow.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = results, isLoading = false)
                            }
                        }
                        .onFailure {
                            stateFlow.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = emptyList(), isLoading = false)
                            }
                        }
                }
        }
    }

    fun onPickupTextChanged(text: String) {
        _pickup.value = _pickup.value.copy(text = text, selected = null, suggestions = emptyList())
        pickupQuery.value = text
    }

    fun onPickupSuggestionSelected(suggestion: AddressSuggestion) {
        _pickup.value = LocationFieldState(text = suggestion.displayName, selected = suggestion)
    }

    fun onDestinationTextChanged(text: String) {
        _destination.value = _destination.value.copy(text = text, selected = null, suggestions = emptyList())
        destinationQuery.value = text
    }

    fun onDestinationSuggestionSelected(suggestion: AddressSuggestion) {
        _destination.value = LocationFieldState(text = suggestion.displayName, selected = suggestion)
    }
    // ---------------------------

    fun postRide(request: OfferRideRequest) {
        val validationResult = validateOfferRide(request)
        errorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (!validationResult.isValid) {
            submitStatus = UseCaseSubmitStatus.Error(validationResult.message)
            return
        }

        submitStatus = UseCaseSubmitStatus.Loading
        
        // Mocking API call
        mainHandler.postDelayed({
            submitStatus = UseCaseSubmitStatus.Success("Ride posted successfully (Mocked)")
        }, 1500)

        /* Commented out real API call
        Thread {
            val result = apiService.offerRide(request)
            mainHandler.post {
                submitStatus = when (result) {
                    is ApiResult.Success -> UseCaseSubmitStatus.Success(result.data.message)
                    is ApiResult.Error -> UseCaseSubmitStatus.Error(result.message)
                }
            }
        }.start()
        */
    }

    private fun validateOfferRide(request: OfferRideRequest): ValidationResult {
        return when {
            request.pickupLocation.isBlank() -> ValidationResult(false, "Enter a pickup location.")
            request.destination.isBlank() -> ValidationResult(false, "Enter a destination.")
            request.rideDate.isBlank() -> ValidationResult(false, "Select a ride date.")
            request.rideTime.isBlank() -> ValidationResult(false, "Select a ride time.")
            request.availableSeats !in 1..7 -> ValidationResult(false, "Seats must be between 1 and 7.")
            request.farePerSeat < 0.0 -> ValidationResult(false, "Fare cannot be negative.")
            !isRideDateTimeAllowed(request.rideDate, request.rideTime) -> {
                ValidationResult(false, "Ride time must be at least 30 minutes from now.")
            }
            else -> ValidationResult(true)
        }
    }
}

private fun isRideDateTimeAllowed(
    rideDate: String,
    rideTime: String
): Boolean {
    val selectedDate = parseRideDate(rideDate) ?: return false
    val selectedTime = parseRideTime(rideTime) ?: return false
    val selectedDateTime = Calendar.getInstance().apply {
        time = selectedDate.time
        set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val earliestAllowed = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return !selectedDateTime.before(earliestAllowed)
}

private fun parseRideDate(date: String): Calendar? {
    return runCatching {
        Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                ?: return null
        }
    }.getOrNull()
}

private fun parseRideTime(time: String): Calendar? {
    return runCatching {
        Calendar.getInstance().apply {
            this.time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)
                ?: return null
        }
    }.getOrNull()
}

class DeleteDriverProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())

    var submitStatus by mutableStateOf<UseCaseSubmitStatus>(UseCaseSubmitStatus.Idle)
        private set

    fun deactivateProfile(reason: String = "") {
        submitStatus = UseCaseSubmitStatus.Loading

        // Mocking API call
        mainHandler.postDelayed({
            submitStatus = UseCaseSubmitStatus.Success("Driver profile deactivated (Mocked)")
        }, 1500)

        /* Commented out real API call
        Thread {
            val result = apiService.deleteDriverProfile(
                DeleteDriverProfileRequest(reason = reason.trim())
            )
            mainHandler.post {
                submitStatus = when (result) {
                    is ApiResult.Success -> UseCaseSubmitStatus.Success(result.data.message)
                    is ApiResult.Error -> UseCaseSubmitStatus.Error(result.message)
                }
            }
        }.start()
        */
    }
}

class StudentProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var submitStatus by mutableStateOf<UseCaseSubmitStatus>(UseCaseSubmitStatus.Idle)
        private set

    fun createStudentProfile(request: StudentProfileRequest) {
        val validationResult = validateStudentProfile(request)
        errorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (!validationResult.isValid) {
            submitStatus = UseCaseSubmitStatus.Error(validationResult.message)
            return
        }

        submitStatus = UseCaseSubmitStatus.Loading

        // Mocking API call
        mainHandler.postDelayed({
            submitStatus = UseCaseSubmitStatus.Success("Student profile created successfully (Mocked)")
        }, 1500)

        /* Commented out real API call
        Thread {
            val result = apiService.createStudentProfile(request)
            mainHandler.post {
                submitStatus = when (result) {
                    is ApiResult.Success -> UseCaseSubmitStatus.Success(result.data.message)
                    is ApiResult.Error -> UseCaseSubmitStatus.Error(result.message)
                }
            }
        }.start()
        */
    }

    private fun validateStudentProfile(request: StudentProfileRequest): ValidationResult {
        return when {
            request.firstName.isBlank() -> ValidationResult(false, "Enter your first name.")
            request.surname.isBlank() -> ValidationResult(false, "Enter your surname.")
            request.studentNumber.isBlank() -> ValidationResult(false, "Enter your student number.")
            request.contactNumber.isBlank() -> ValidationResult(false, "Enter your contact number.")
            !request.universityEmail.endsWith("@mandela.ac.za", ignoreCase = true) -> {
                ValidationResult(false, "Use your NMU email ending with @mandela.ac.za.")
            }
            request.password.length < 8 -> ValidationResult(false, "Password must be at least 8 characters.")
            else -> ValidationResult(true)
        }
    }
}
