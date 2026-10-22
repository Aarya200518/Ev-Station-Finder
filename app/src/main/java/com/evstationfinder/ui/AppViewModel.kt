package com.evstationfinder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evstationfinder.data.AppDatabase
import com.evstationfinder.data.AppRepository
import com.evstationfinder.data.BookingDraft
import com.evstationfinder.data.BookingEntity
import com.evstationfinder.data.ProfileEntity
import com.evstationfinder.data.StationEntity
import com.evstationfinder.data.VehicleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class BookSlot(val stationId: String) : Screen()
    object Payment : Screen()
    data class Confirmation(val bookingId: String) : Screen()
    object Bookings : Screen()
    object Profile : Screen()
    object Map : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.get(application))

    val searchQuery = MutableStateFlow("")
    val selectedConnectorFilter = MutableStateFlow("All")

    val profile: StateFlow<ProfileEntity?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stations: StateFlow<List<StationEntity>> = combine(
        repository.stations, profile, searchQuery, selectedConnectorFilter
    ) { list, userProfile, query, filter ->
        val userLocation = userProfile?.savedLocation ?: "Bandra Kurla Complex, Mumbai"

        list.map { station ->
            val isDirectMatch = userLocation.split(",", " ").filter { it.length > 2 }.any { part ->
                station.address.contains(part.trim(), ignoreCase = true) || station.name.contains(part.trim(), ignoreCase = true)
            }
            if (isDirectMatch) {
                station.copy(distanceKm = (station.distanceKm * 0.3).coerceAtLeast(0.8))
            } else {
                station
            }
        }.filter { station ->
            val matchesQuery = query.isEmpty() || station.name.contains(query, ignoreCase = true) ||
                    station.address.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "All" -> true
                "CCS" -> station.connector.contains("CCS", ignoreCase = true)
                "Type 2" -> station.connector.contains("Type 2", ignoreCase = true)
                "Fast (50kW+)" -> station.powerKw >= 50
                else -> true
            }
            matchesQuery && matchesFilter
        }.sortedWith(
            compareBy<StationEntity> { station ->
                val locationTokens = userLocation.split(",", " ").map { it.trim().lowercase() }.filter { it.length > 2 }
                val matchesLocation = locationTokens.any { token ->
                    station.address.lowercase().contains(token) || station.name.lowercase().contains(token)
                }
                if (matchesLocation) 0 else 1
            }.thenBy { it.distanceKm }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStations: StateFlow<List<StationEntity>> = repository.stations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles: StateFlow<List<VehicleEntity>> = repository.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<BookingEntity>> = repository.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val activeTab = MutableStateFlow("home") // "home", "map", "bookings", "profile"

    val currentDraft = MutableStateFlow<BookingDraft?>(null)
    val paymentError = MutableStateFlow<String?>(null)
    val isProcessingPayment = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun selectTab(tab: String) {
        activeTab.value = tab
        currentScreen.value = when (tab) {
            "home" -> Screen.Home
            "map" -> Screen.Map
            "bookings" -> Screen.Bookings
            "profile" -> Screen.Profile
            else -> Screen.Home
        }
    }

    fun selectLocation(newLocation: String) {
        viewModelScope.launch {
            val current = profile.value
            val name = current?.name ?: "Aarav Mehta"
            val email = current?.email ?: "aarav.mehta@email.com"
            val phone = current?.phone ?: "+91 98765 43210"
            repository.saveProfile(ProfileEntity(id = 1, name = name, email = email, phone = phone, savedLocation = newLocation))
        }
    }

    fun startBooking(stationId: String, defaultVehicleId: String = "nexon") {
        currentDraft.value = BookingDraft(
            stationId = stationId,
            vehicleId = defaultVehicleId,
            currentBattery = 20,
            targetBattery = 80,
            date = "Today",
            slot = "10:00 AM - 11:00 AM",
            slotId = "slot-10am"
        )
        currentScreen.value = Screen.BookSlot(stationId)
    }

    fun updateDraft(update: (BookingDraft) -> BookingDraft) {
        currentDraft.value?.let { current ->
            currentDraft.value = update(current)
        }
    }

    fun proceedToPayment() {
        paymentError.value = null
        currentScreen.value = Screen.Payment
    }

    fun confirmBooking(paymentMethod: String) {
        val draft = currentDraft.value ?: return
        viewModelScope.launch {
            isProcessingPayment.value = true
            paymentError.value = null
            val result = repository.reserve(draft, paymentMethod)
            isProcessingPayment.value = false
            result.onSuccess { bookingId ->
                currentScreen.value = Screen.Confirmation(bookingId)
            }.onFailure { error ->
                paymentError.value = error.message ?: "Booking failed. Please try again."
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.cancel(bookingId)
        }
    }

    fun updateProfile(name: String, email: String, phone: String, savedLocation: String) {
        viewModelScope.launch {
            repository.saveProfile(ProfileEntity(id = 1, name = name, email = email, phone = phone, savedLocation = savedLocation))
        }
    }

    fun navigateBack() {
        when (currentScreen.value) {
            is Screen.BookSlot -> currentScreen.value = Screen.Home
            is Screen.Payment -> {
                val draft = currentDraft.value
                if (draft != null) {
                    currentScreen.value = Screen.BookSlot(draft.stationId)
                } else {
                    currentScreen.value = Screen.Home
                }
            }
            is Screen.Confirmation -> {
                currentScreen.value = Screen.Home
                activeTab.value = "home"
            }
            else -> {
                currentScreen.value = Screen.Home
                activeTab.value = "home"
            }
        }
    }
}
