package com.evstationfinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evstationfinder.data.StationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartStationRecommendationsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val stations by viewModel.allStations.collectAsState()

    var selectedVehicleId by remember(vehicles) { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    var selectedSortOption by remember { mutableStateOf("Recommended") }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    val sortOptions = listOf("Recommended", "Nearest", "Lowest Price", "Highest Power")

    // Compute recommendations using RecommendationEngine
    val recommendations: List<StationRecommendation> = remember(stations, selectedVehicle) {
        RecommendationEngine.computeRecommendations(stations, selectedVehicle)
    }

    val sortedRecommendations = remember(recommendations, selectedSortOption) {
        when (selectedSortOption) {
            "Nearest" -> recommendations.sortedBy { rec -> rec.station.distanceKm }
            "Lowest Price" -> recommendations.sortedBy { rec -> rec.station.pricePerKwh }
            "Highest Power" -> recommendations.sortedByDescending { rec -> rec.station.powerKw }
            else -> recommendations.sortedByDescending { rec -> rec.score }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Station Recommendations", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Subtitle Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Find the right charging station for your EV.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle Selection
                    if (vehicles.isEmpty()) {
                        Text(
                            text = "No saved vehicles found. Please add a vehicle in Profile.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vehicles) { vehicle ->
                                val isSelected = selectedVehicleId == vehicle.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedVehicleId = vehicle.id },
                                    label = { Text("${vehicle.name} (${vehicle.connector})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sort Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sortOptions) { option ->
                            val isSelected = selectedSortOption == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSortOption = option },
                                label = { Text("Sort: $option") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sortedRecommendations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No charging stations available.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedRecommendations) { rec ->
                        RecommendationCard(
                            recommendation = rec,
                            onBookClick = {
                                viewModel.startBooking(rec.station.id, selectedVehicleId.ifEmpty { "nexon" })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: StationRecommendation,
    onBookClick: () -> Unit
) {
    val station = recommendation.station
    val isCompatible = recommendation.isCompatible

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompatible) MaterialTheme.colorScheme.surface else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Name & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = station.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCompatible && recommendation.categories.isNotEmpty()) {
                            val cat = recommendation.categories.first()
                            val (catText, catBg) = when (cat) {
                                RecommendationCategory.BEST_MATCH -> "BEST MATCH" to Color(0xFF2E7D32)
                                RecommendationCategory.NEAREST -> "NEAREST" to Color(0xFF0277BD)
                                RecommendationCategory.BUDGET_FRIENDLY -> "BUDGET" to Color(0xFFEF6C00)
                                RecommendationCategory.FAST_CHARGING -> "FAST" to Color(0xFF6A1B9A)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = catBg
                            ) {
                                Text(
                                    text = catText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = station.address,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = station.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                    }
                }
            }

            // Compatibility Reason / Note
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCompatible) Color(0xFFE8F5E9) else Color(0xFFFFCDD2)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isCompatible) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompatible) getRecommendationReason(recommendation) else recommendation.compatibilityNote,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompatible) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                }
            }

            // Badges Row: Connector, Distance, Power
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${station.connector} · ${station.powerKw} kW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${"%.1f".format(station.distanceKm)} km away",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Footer: Tariff + Available Slots + Reserve Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${station.pricePerKwh}/kWh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val slotColor = if (station.availableSlots > 2) Color(0xFF2E7D32) else Color(0xFFE65100)
                    Text(
                        text = "${station.availableSlots}/${station.totalSlots} Slots Available",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = slotColor
                    )
                }

                if (isCompatible) {
                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Reserve Slot", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Incompatible",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

fun getRecommendationReason(rec: StationRecommendation): String {
    return when {
        rec.categories.contains(RecommendationCategory.BEST_MATCH) -> "Strong overall match for your selected EV."
        rec.categories.contains(RecommendationCategory.NEAREST) -> "Closest suitable station."
        rec.categories.contains(RecommendationCategory.BUDGET_FRIENDLY) -> "Lower charging price among suitable stations."
        rec.categories.contains(RecommendationCategory.FAST_CHARGING) -> "Higher charging power among suitable stations."
        else -> "Compatible charging station."
    }
}
