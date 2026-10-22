package com.evstationfinder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double,
    val rating: Double,
    val connector: String,
    val powerKw: Int,
    val pricePerKwh: Int,
    val totalSlots: Int,
    val availableSlots: Int,
    val isOpen: Boolean,
    val hours: String,
    val amenities: String,
    val accent: String
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val capacityKwh: Double,
    val currentBattery: Int,
    val connector: String
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val savedLocation: String,
    val isActive: Boolean = false
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val stationId: String,
    val stationName: String,
    val address: String,
    val date: String,
    val slot: String,
    val slotId: String,
    val vehicle: String,
    val currentBattery: Int,
    val targetBattery: Int,
    val energyKwh: Double,
    val chargeCost: Int,
    val platformFee: Int,
    val tax: Int,
    val total: Int,
    val connector: String,
    val powerKw: Int,
    val durationMinutes: Int,
    val status: String = "upcoming",
    val paymentMethod: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookingDraft(
    val stationId: String,
    val vehicleId: String,
    val currentBattery: Int,
    val targetBattery: Int,
    val date: String,
    val slot: String,
    val slotId: String
)

data class ChargingEstimate(
    val energyKwh: Double,
    val chargeCost: Int,
    val platformFee: Int,
    val tax: Int,
    val total: Int,
    val durationMinutes: Int
)
