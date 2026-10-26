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

    fun computeRecommendations(
        stations: List<StationEntity>,
        vehicle: VehicleEntity?
    ): List<StationRecommendation> {
        if (stations.isEmpty()) return emptyList()

        val evaluated = stations.map { station ->
            val vConnector = vehicle?.connector?.trim()?.lowercase() ?: ""
            val sConnector = station.connector.trim().lowercase()

            val isCompatible = when {
                vConnector.isEmpty() || sConnector.isEmpty() -> false
                sConnector.contains(vConnector) || vConnector.contains(sConnector) -> true
                else -> false
            }

            val note = if (isCompatible) "Compatible (${station.connector})" else "Connector mismatch (${station.connector})"

            Triple(station, isCompatible, note)
        }

        val validStations = evaluated.map { it.first }
        val maxDist = validStations.maxOfOrNull { it.distanceKm }?.takeIf { it > 0.0 } ?: 20.0
        val maxPrice = validStations.maxOfOrNull { it.pricePerKwh.toDouble() }?.takeIf { it > 0.0 } ?: 20.0
        val maxPower = validStations.maxOfOrNull { it.powerKw.toDouble() }?.takeIf { it > 0.0 } ?: 150.0

        val scored = evaluated.map { (station, isCompatible, note) ->
            var score = 0.0

            if (isCompatible) {
                score += 50.0

                val distNorm = (1.0 - (station.distanceKm / maxDist).coerceIn(0.0, 1.0)) * 25.0
                score += distNorm

                val priceNorm = (1.0 - (station.pricePerKwh / maxPrice).coerceIn(0.0, 1.0)) * 15.0
                score += priceNorm

                val powerNorm = (station.powerKw / maxPower).coerceIn(0.0, 1.0) * 10.0
                score += powerNorm

                if (station.isOpen) score += 5.0
                if (station.availableSlots > 0) score += (station.availableSlots.toDouble() / station.totalSlots.coerceAtLeast(1)) * 5.0
            } else {
                score = -100.0
            }

            StationRecommendation(
                station = station,
                isCompatible = isCompatible,
                compatibilityNote = note,
                score = score,
                categories = emptyList()
            )
        }

        val sorted = scored.sortedByDescending { it.score }
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
