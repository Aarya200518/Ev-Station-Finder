package com.evstationfinder.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    val stations: Flow<List<StationEntity>> = database.stationDao().observeAll()
    val vehicles: Flow<List<VehicleEntity>> = database.vehicleDao().observeAll()
    val bookings: Flow<List<BookingEntity>> = database.bookingDao().observeAll()
    val chargingSessions: Flow<List<ChargingSessionEntity>> = database.chargingSessionDao().observeAll()
    val activeProfile: Flow<ProfileEntity?> = database.profileDao().observeActive()
    val allProfiles: Flow<List<ProfileEntity>> = database.profileDao().observeAll()

    suspend fun seedIfNeeded() {
        database.stationDao().insertAll(
            listOf(
                // Bandra / BKC, Mumbai
                StationEntity("tata-power-bkc", "Tata Power BKC", "Maker Maxity, Bandra Kurla Complex, Mumbai", 1.2, 4.8, "CCS", 120, 15, 6, 4, true, "Open 24 hours", "Cafe|Wi-Fi|Restrooms", "green"),
                StationEntity("jio-bp-bkc", "Jio-bpPulse BKC", "G Block, Bandra Kurla Complex, Bandra East, Mumbai", 1.8, 4.7, "CCS · Type 2", 150, 16, 8, 6, true, "Open 24 hours", "Food court|Wi-Fi|Valet", "green"),
                StationEntity("exicom-bandra", "Exicom Charging Hub", "Turner Road, Bandra West, Mumbai", 2.5, 4.5, "CCS", 60, 14, 4, 2, true, "Open until 11:00 PM", "Shopping|Restrooms", "blue"),

                // Worli / Lower Parel, Mumbai
                StationEntity("chargezone-worli", "ChargeZone Worli", "Atria Mall, Dr Annie Besant Road, Worli, Mumbai", 3.8, 4.6, "CCS · Type 2", 60, 13, 8, 3, true, "Open until 11:00 PM", "Mall access|Food court|Parking", "blue"),
                StationEntity("ather-phoenix", "Ather Grid Phoenix", "High Street Phoenix, Lower Parel, Mumbai", 4.2, 4.8, "CCS · Type 2", 50, 12, 6, 4, true, "Open 24 hours", "Shopping|Lounge|Valet", "blue"),
                StationEntity("tata-worli-seaface", "Tata Power Worli Sea Face", "Worli Sea Face, Worli, Mumbai", 4.9, 4.6, "CCS", 120, 15, 4, 3, true, "Open 24 hours", "Sea View|Restrooms", "green"),

                // Andheri / Juhu, Mumbai
                StationEntity("plugngo-andheri", "PlugNGo Andheri", "The Hub, Western Express Highway, Andheri, Mumbai", 7.4, 4.4, "CHAdeMO · CCS", 50, 11, 4, 2, true, "Open until 10:00 PM", "Lounge|Security|Parking", "amber"),
                StationEntity("relux-andheri-west", "Relux EV Charging", "Veera Desai Road, Andheri West, Mumbai", 8.1, 4.5, "CCS", 60, 13, 6, 5, true, "Open 24 hours", "Cafe|Wi-Fi", "green"),
                StationEntity("tata-juhu", "Tata Power Juhu", "Juhu Tara Road, Juhu, Mumbai", 9.0, 4.7, "CCS · Type 2", 120, 15, 4, 1, true, "Open 24 hours", "Beach access|Restrooms", "green"),

                // Powai / Thane, Mumbai
                StationEntity("zeon-powai", "Zeon Charging Hub", "Hiranandani Gardens, Powai, Mumbai", 12.6, 4.7, "CCS", 50, 14, 5, 5, true, "Open 24 hours", "Cafe|Restrooms|Shopping", "green"),
                StationEntity("kazam-powai", "Kazam EV Powai", "R-City Mall, Ghatkopar West, Mumbai", 13.2, 4.4, "CCS · Type 2", 60, 12, 8, 4, true, "Open until 11:00 PM", "Mall access|Food court", "blue"),
                StationEntity("chargezone-thane", "ChargeZone Viviana", "Eastern Express Highway, Thane, Mumbai", 18.5, 4.6, "CCS", 120, 14, 10, 7, true, "Open 24 hours", "Mall access|Parking", "green"),

                // New Delhi
                StationEntity("tata-cp-delhi", "Tata Power CP Hub", "Inner Circle, Connaught Place, New Delhi", 1.5, 4.8, "CCS · Type 2", 150, 15, 8, 5, true, "Open 24 hours", "Metro access|Cafe|Restrooms", "green"),
                StationEntity("eesl-janpath", "EESL Fast Charging", "Janpath Road, Connaught Place, New Delhi", 2.1, 4.5, "CCS", 60, 11, 6, 4, true, "Open 24 hours", "Parking|Security", "blue"),
                StationEntity("chargezone-aerocity", "ChargeZone Aerocity", "Worldmark 2, Aerocity, New Delhi", 14.2, 4.9, "CCS · Type 2", 120, 16, 12, 8, true, "Open 24 hours", "Airport shuttle|Lounge|Cafe", "green"),

                // Bengaluru
                StationEntity("ather-mg-road", "Ather Grid MG Road", "Church Street, MG Road, Bengaluru", 1.1, 4.9, "CCS · Type 2", 60, 13, 6, 4, true, "Open 24 hours", "Cafe|Wi-Fi|Metro access", "green"),
                StationEntity("bescom-kanthirava", "Bescom Charging Station", "Kanthirava Stadium, Bengaluru", 2.3, 4.4, "CCS", 50, 10, 8, 6, true, "Open 24 hours", "Sports complex|Parking", "blue"),
                StationEntity("zeon-indiranagar", "Zeon Fast Charger", "100 Feet Road, Indiranagar, Bengaluru", 4.5, 4.8, "CCS", 120, 15, 6, 3, true, "Open 24 hours", "Cafe|Shopping|Restrooms", "green"),

                // Pune
                StationEntity("tata-hadapsar", "Tata Power Seasons Mall", "Magarpatta City, Hadapsar, Pune", 1.4, 4.7, "CCS", 120, 14, 8, 6, true, "Open 24 hours", "Mall access|Food court|Restrooms", "green"),
                StationEntity("chargezone-viman-nagar", "ChargeZone Viman Nagar", "Phoenix Marketcity, Viman Nagar, Pune", 5.2, 4.6, "CCS · Type 2", 60, 13, 6, 2, true, "Open until 11:00 PM", "Mall access|Valet", "blue"),
                StationEntity("relux-fc-road", "Relux EV FC Road", "Fergusson College Road, Shivajinagar, Pune", 7.8, 4.5, "CCS", 50, 12, 4, 3, true, "Open 24 hours", "Cafe|Restrooms", "green"),

                // Hyderabad
                StationEntity("ather-gachibowli", "Ather Grid Gachibowli", "DLF Cyber City, Gachibowli, Hyderabad", 1.2, 4.8, "CCS · Type 2", 60, 13, 6, 4, true, "Open 24 hours", "IT Park|Food court|Wi-Fi", "green"),
                StationEntity("tata-jubilee-hills", "Tata Power Jubilee Hills", "Road No 36, Jubilee Hills, Hyderabad", 4.8, 4.7, "CCS", 120, 15, 8, 5, true, "Open 24 hours", "Valet|Lounge|Cafe", "green")
            )
        )

        database.vehicleDao().insertAll(
            listOf(
                VehicleEntity("nexon", "Tata Nexon EV", 40.5, 20, "CCS"),
                VehicleEntity("mg-zs", "MG ZS EV", 50.3, 32, "CCS"),
                VehicleEntity("kona", "Hyundai Kona Electric", 39.2, 45, "CCS")
            )
        )

        if (database.profileDao().count() == 0) {
            database.profileDao().save(
                ProfileEntity(
                    id = 1,
                    name = "Aarav Mehta",
                    email = "aarav.mehta@email.com",
                    phone = "+91 98765 43210",
                    savedLocation = "Bandra Kurla Complex, Mumbai",
                    isActive = true
                )
            )
            database.profileDao().save(
                ProfileEntity(
                    id = 2,
                    name = "Ananya Sharma",
                    email = "ananya.sharma@email.com",
                    phone = "+91 98123 45678",
                    savedLocation = "Worli & Lower Parel, Mumbai",
                    isActive = false
                )
            )
            database.profileDao().save(
                ProfileEntity(
                    id = 3,
                    name = "Rohan Verma",
                    email = "rohan.verma@email.com",
                    phone = "+91 99887 76655",
                    savedLocation = "Connaught Place, New Delhi",
                    isActive = false
                )
            )
        }
    }

    suspend fun loginProfile(id: Int) {
        database.profileDao().clearActive()
        database.profileDao().setActive(id)
    }

    suspend fun createAndLoginProfile(name: String, email: String, phone: String, savedLocation: String) {
        database.profileDao().clearActive()
        database.profileDao().save(
            ProfileEntity(
                name = name,
                email = email,
                phone = phone,
                savedLocation = savedLocation,
                isActive = true
            )
        )
    }

    suspend fun logout() {
        database.profileDao().clearActive()
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
    suspend fun recordChargingSession(session: ChargingSessionEntity) = database.chargingSessionDao().insert(session)

    companion object {
        fun estimate(draft: BookingDraft, station: StationEntity, vehicle: VehicleEntity): ChargingEstimate {
            val energy = ((draft.targetBattery - draft.currentBattery).coerceAtLeast(0) / 100.0) * vehicle.capacityKwh
            val charge = kotlin.math.round(energy * station.pricePerKwh).toInt()
            val tax = kotlin.math.round(charge * 0.05).toInt()
            return ChargingEstimate(energy, charge, 10, tax, charge + 10 + tax, (energy / station.powerKw * 60).toInt().coerceAtLeast(1))
        }
    }
}
