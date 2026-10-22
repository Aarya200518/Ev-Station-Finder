package com.evstationfinder.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    val stations: Flow<List<StationEntity>> = database.stationDao().observeAll()
    val vehicles: Flow<List<VehicleEntity>> = database.vehicleDao().observeAll()
    val bookings: Flow<List<BookingEntity>> = database.bookingDao().observeAll()
    val profile: Flow<ProfileEntity?> = database.profileDao().observe()

    suspend fun seedIfNeeded() {
        if (database.stationDao().count() > 0) return
        database.stationDao().insertAll(
            listOf(
                StationEntity("tata-power-bkc", "Tata Power BKC", "Maker Maxity, Bandra Kurla Complex", 1.2, 4.8, "CCS", 120, 15, 6, 4, true, "Open 24 hours", "Cafe|Wi-Fi|Restrooms", "green"),
                StationEntity("chargezone-worli", "ChargeZone Worli", "Atria Mall, Dr Annie Besant Road", 3.8, 4.6, "CCS · Type 2", 60, 13, 8, 3, true, "Open until 11:00 PM", "Mall access|Food court|Parking", "blue"),
                StationEntity("plugngo-andheri", "PlugNGo Andheri", "The Hub, Western Express Highway", 7.4, 4.4, "CHAdeMO · CCS", 50, 11, 4, 2, true, "Open until 10:00 PM", "Lounge|Security|Parking", "amber"),
                StationEntity("zeon-powai", "Zeon Charging Hub", "Hiranandani Gardens, Powai", 12.6, 4.7, "CCS", 50, 14, 5, 5, true, "Open 24 hours", "Cafe|Restrooms|Shopping", "green")
            )
        )
        database.vehicleDao().insertAll(
            listOf(
                VehicleEntity("nexon", "Tata Nexon EV", 40.5, 20, "CCS"),
                VehicleEntity("mg-zs", "MG ZS EV", 50.3, 32, "CCS"),
                VehicleEntity("kona", "Hyundai Kona Electric", 39.2, 45, "CCS")
            )
        )
        database.profileDao().save(ProfileEntity(name = "Aarav Mehta", email = "aarav.mehta@email.com", phone = "+91 98765 43210", savedLocation = "Bandra Kurla Complex, Mumbai"))
    }

    suspend fun reserve(draft: BookingDraft, paymentMethod: String): Result<String> {
        val station = database.stationDao().get(draft.stationId) ?: return Result.failure(IllegalStateException("Station unavailable"))
        val vehicle = database.vehicleDao().get(draft.vehicleId) ?: return Result.failure(IllegalStateException("Vehicle unavailable"))
        if (station.availableSlots <= 0 || database.bookingDao().activeSlotCount(draft.stationId, draft.date, draft.slotId) > 0) {
            return Result.failure(IllegalStateException("That slot is no longer available"))
        }
        val estimate = estimate(draft, station, vehicle)
        val id = "EVF-${System.currentTimeMillis().toString().takeLast(6)}-${('A'..'Z').random()}${('A'..'Z').random()}"
        database.bookingDao().insert(
            BookingEntity(
                id = id, stationId = station.id, stationName = station.name, address = station.address,
                date = draft.date, slot = draft.slot, slotId = draft.slotId, vehicle = vehicle.name,
                currentBattery = draft.currentBattery, targetBattery = draft.targetBattery, energyKwh = estimate.energyKwh,
                chargeCost = estimate.chargeCost, platformFee = estimate.platformFee, tax = estimate.tax, total = estimate.total,
                connector = station.connector.substringBefore(" · "), powerKw = station.powerKw,
                durationMinutes = estimate.durationMinutes, paymentMethod = paymentMethod
            )
        )
        database.stationDao().updateAvailability(station.id, (station.availableSlots - 1).coerceAtLeast(0))
        return Result.success(id)
    }

    suspend fun cancel(id: String) {
        val booking = database.bookingDao().get(id) ?: return
        database.bookingDao().cancel(id)
        val station = database.stationDao().get(booking.stationId) ?: return
        database.stationDao().updateAvailability(station.id, (station.availableSlots + 1).coerceAtMost(station.totalSlots))
    }

    suspend fun saveProfile(profile: ProfileEntity) = database.profileDao().save(profile)

    companion object {
        fun estimate(draft: BookingDraft, station: StationEntity, vehicle: VehicleEntity): ChargingEstimate {
            val energy = ((draft.targetBattery - draft.currentBattery).coerceAtLeast(0) / 100.0) * vehicle.capacityKwh
            val charge = kotlin.math.round(energy * station.pricePerKwh).toInt()
            val tax = kotlin.math.round(charge * 0.05).toInt()
            return ChargingEstimate(energy, charge, 10, tax, charge + 10 + tax, (energy / station.powerKw * 60).toInt().coerceAtLeast(1))
        }
    }
}
