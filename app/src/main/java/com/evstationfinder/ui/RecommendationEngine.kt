package com.evstationfinder.ui

import com.evstationfinder.data.StationEntity
import com.evstationfinder.data.VehicleEntity

data class StationRecommendation(
    val station: StationEntity,
    val isCompatible: Boolean,
    val compatibilityNote: String,
    val score: Double,
    val categories: List<RecommendationCategory>
)

enum class RecommendationCategory {
    BEST_MATCH,
    NEAREST,
    BUDGET_FRIENDLY,
    FAST_CHARGING
}

object RecommendationEngine {

    /**
     * Evaluates compatibility, location relevance, and ranks stations for the given vehicle and user location.
     * Scoring factors:
     * - Location matching: Stations matching the user's city/area receive a strong priority bonus. Out-of-city stations are penalized.
     * - Compatibility: Must match connector (e.g., CCS, Type 2, CHAdeMO).
     * - Distance: Lower distanceKm gets higher score.
     * - Price: Lower pricePerKwh gets higher score.
     * - Power: Higher powerKw gets higher score.
     * - Availability: Open status and available slots boost score.
     */
    fun computeRecommendations(
        stations: List<StationEntity>,
        vehicle: VehicleEntity?,
        userLocation: String?
    ): List<StationRecommendation> {
        if (stations.isEmpty()) return emptyList()

        val location = userLocation ?: "Bandra Kurla Complex, Mumbai"
        val genericWords = setOf("india", "east", "west", "road", "street", "near")
        val locationTokens = location.split(",", " ")
            .map { it.trim().lowercase() }
            .filter { it.length > 2 && !genericWords.contains(it) }

        // Extract city token specifically (e.g., mumbai, delhi, bengaluru, pune, hyderabad)
        val knownCities = setOf("mumbai", "delhi", "bengaluru", "pune", "hyderabad")
        val userCity = location.split(",", " ").map { it.trim().lowercase() }.firstOrNull { knownCities.contains(it) } ?: "mumbai"

        val evaluated = stations.map { station ->
            val vConnector = vehicle?.connector?.trim()?.lowercase() ?: ""
            val sConnector = station.connector.trim().lowercase()

            val isCompatible = when {
                vConnector.isEmpty() || sConnector.isEmpty() -> false
                sConnector.contains(vConnector) || vConnector.contains(sConnector) -> true
                else -> false
            }

            val stationAddrLower = station.address.lowercase()
            val stationNameLower = station.name.lowercase()

            val isLocationMatch = locationTokens.any { token ->
                stationAddrLower.contains(token) || stationNameLower.contains(token)
            }

            val isCityMatch = stationAddrLower.contains(userCity) || stationNameLower.contains(userCity)

            val note = when {
                !isCompatible -> "Connector mismatch (${station.connector})"
                !isCityMatch && !isLocationMatch -> "Located outside $userCity"
                else -> "Compatible & nearby (${station.connector})"
            }

            Triple(station, isCompatible, Triple(isLocationMatch, isCityMatch, note))
        }

        val validStations = evaluated.map { it.first }
        val maxDist = validStations.maxOfOrNull { it.distanceKm }?.takeIf { it > 0.0 } ?: 20.0
        val maxPrice = validStations.maxOfOrNull { it.pricePerKwh.toDouble() }?.takeIf { it > 0.0 } ?: 20.0
        val maxPower = validStations.maxOfOrNull { it.powerKw.toDouble() }?.takeIf { it > 0.0 } ?: 150.0

        val scored = evaluated.map { (station, isCompatible, meta) ->
            val (isLocationMatch, isCityMatch, note) = meta
            var score = 0.0

            if (isCompatible) {
                score += 50.0

                // Location relevance scoring
                if (isLocationMatch) {
                    score += 150.0 // Strong area match bonus (e.g., Andheri / BKC)
                } else if (isCityMatch) {
                    score += 50.0  // Same city bonus (e.g., Mumbai)
                } else {
                    score -= 300.0 // Heavy penalty for out-of-city stations
                }

                val distNorm = (1.0 - (station.distanceKm / maxDist).coerceIn(0.0, 1.0)) * 25.0
                score += distNorm

                val priceNorm = (1.0 - (station.pricePerKwh / maxPrice).coerceIn(0.0, 1.0)) * 15.0
                score += priceNorm

                val powerNorm = (station.powerKw / maxPower).coerceIn(0.0, 1.0) * 10.0
                score += powerNorm

                if (station.isOpen) score += 5.0
                if (station.availableSlots > 0) score += (station.availableSlots.toDouble() / station.totalSlots.coerceAtLeast(1)) * 5.0
            } else {
                score = -600.0
            }

            // Consider compatible if connector matches and either location or city matches (or fallback if none match)
            val passesLocationFilter = isLocationMatch || isCityMatch
            StationRecommendation(
                station = station,
                isCompatible = isCompatible && passesLocationFilter,
                compatibilityNote = if (!isCompatible) note else if (!passesLocationFilter) "Outside selected region ($userCity)" else note,
                score = score,
                categories = emptyList()
            )
        }

        // Fallback if strict location filtering leaves nothing
        val hasAnyValid = scored.any { it.isCompatible }
        val finalScored = if (!hasAnyValid) {
            scored.map { it.copy(isCompatible = it.score > -400.0) }
        } else {
            scored
        }

        val sorted = finalScored.sortedByDescending { it.score }
        val compatibleSorted = sorted.filter { it.isCompatible }

        val bestMatchId = compatibleSorted.firstOrNull()?.station?.id
        val nearestId = compatibleSorted.minByOrNull { it.station.distanceKm }?.station?.id
        val budgetId = compatibleSorted.minByOrNull { it.station.pricePerKwh }?.station?.id
        val fastestId = compatibleSorted.maxByOrNull { it.station.powerKw }?.station?.id

        return sorted.map { rec ->
            val cats = mutableListOf<RecommendationCategory>()
            if (rec.isCompatible) {
                if (rec.station.id == bestMatchId) cats.add(RecommendationCategory.BEST_MATCH)
                if (rec.station.id == nearestId && cats.isEmpty()) cats.add(RecommendationCategory.NEAREST)
                if (rec.station.id == budgetId && !cats.contains(RecommendationCategory.BEST_MATCH)) cats.add(RecommendationCategory.BUDGET_FRIENDLY)
                if (rec.station.id == fastestId && !cats.contains(RecommendationCategory.BEST_MATCH)) cats.add(RecommendationCategory.FAST_CHARGING)
                if (cats.isEmpty()) cats.add(RecommendationCategory.BEST_MATCH)
            }
            rec.copy(categories = cats)
        }
    }
}
