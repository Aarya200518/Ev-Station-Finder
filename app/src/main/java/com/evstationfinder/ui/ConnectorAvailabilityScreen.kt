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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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

enum class AvailabilityStatus {
    AVAILABLE,
    FULL,
    CLOSED,
    UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectorAvailabilityScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.allStations.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedConnectorFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf("Distance") }

    val primaryVehicle = vehicles.firstOrNull()

    // Extract unique connectors from stations
    val availableConnectors = remember(stations) {
        val connectors = mutableSetOf("All")
        stations.forEach { station ->
            station.connector.split("·", ",").forEach { c ->
                val trimmed = c.trim()
                if (trimmed.isNotEmpty()) connectors.add(trimmed)
            }
        }
        connectors.toList()
    }

    val statusFilters = listOf("All", "Available", "Full", "Closed", "Unknown")
    val sortOptions = listOf("Distance", "Power", "Available Slots")

    // Filter & Sort logic
    val filteredStations = remember(stations, searchQuery, selectedConnectorFilter, selectedStatusFilter, selectedSortOption) {
        stations.filter { station ->
            // Search query filter
            val matchesSearch = searchQuery.isEmpty() ||
                    station.name.contains(searchQuery, ignoreCase = true) ||
                    station.address.contains(searchQuery, ignoreCase = true)

            // Connector filter
            val matchesConnector = selectedConnectorFilter == "All" ||
                    station.connector.split("·", ",").any { it.trim().equals(selectedConnectorFilter, ignoreCase = true) }

            // Status filter
            val status = getAvailabilityStatus(station)
            val matchesStatus = when (selectedStatusFilter) {
                "Available" -> status == AvailabilityStatus.AVAILABLE
                "Full" -> status == AvailabilityStatus.FULL
                "Closed" -> status == AvailabilityStatus.CLOSED
                "Unknown" -> status == AvailabilityStatus.UNKNOWN
                else -> true
            }

            matchesSearch && matchesConnector && matchesStatus
        }.sortedWith(
            when (selectedSortOption) {
                "Power" -> compareByDescending { it.powerKw }
                "Available Slots" -> compareByDescending { it.availableSlots }
                else -> compareBy { it.distanceKm }
            }
        )
    }

    // Summary metrics
    val totalStationsCount = stations.size
    val connectorMatchingCount = stations.count { station ->
        selectedConnectorFilter == "All" || station.connector.split("·", ",").any { it.trim().equals(selectedConnectorFilter, ignoreCase = true) }
    }
    val reportedAvailableCount = stations.count { getAvailabilityStatus(it) == AvailabilityStatus.AVAILABLE }
    val unknownAvailabilityCount = stations.count { getAvailabilityStatus(it) == AvailabilityStatus.UNKNOWN }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connector Availability", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Header Search & Filters
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Find charging stations with suitable connectors and check their reported availability.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by station name or address...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Connector Filter Chips
                    Text(text = "Filter by Connector:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableConnectors) { connector ->
                            val isSelected = selectedConnectorFilter == connector
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedConnectorFilter = connector },
                                label = { Text(connector) }
                            )
                        }
                    }

                    // Status Filter Chips
                    Text(text = "Filter by Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(statusFilters) { status ->
                            val isSelected = selectedStatusFilter == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStatusFilter = status },
                                label = { Text(status) }
                            )
                        }
                    }

                    // Sort Chips
                    Text(text = "Sort By:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sortOptions) { sort ->
                            val isSelected = selectedSortOption == sort
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSortOption = sort },
                                label = { Text(sort) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(title = "Total", count = totalStationsCount, modifier = Modifier.weight(1f))
                SummaryCard(title = "Filtered", count = connectorMatchingCount, modifier = Modifier.weight(1f))
                SummaryCard(title = "Available", count = reportedAvailableCount, modifier = Modifier.weight(1f), tint = Color(0xFF2E7D32))
                SummaryCard(title = "Unknown", count = unknownAvailabilityCount, modifier = Modifier.weight(1f), tint = Color(0xFF757575))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Disclaimer Note
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Availability is based on app storage and may not reflect real-time network status.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredStations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stations match your connector and availability filters.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredStations) { station ->
                        val isVehicleCompatible = primaryVehicle != null &&
                                station.connector.split("·", ",").any { it.trim().equals(primaryVehicle.connector.trim(), ignoreCase = true) }

                        StationAvailabilityCard(
                            station = station,
                            isVehicleCompatible = isVehicleCompatible,
                            onBookClick = {
                                viewModel.startBooking(station.id, primaryVehicle?.id ?: "nexon")
                            }
                        )
                    }
                }
            }
        }
    }
}

fun getAvailabilityStatus(station: StationEntity): AvailabilityStatus {
    return when {
        !station.isOpen -> AvailabilityStatus.CLOSED
        station.availableSlots > 0 -> AvailabilityStatus.AVAILABLE
        station.availableSlots == 0 && station.totalSlots > 0 -> AvailabilityStatus.FULL
        else -> AvailabilityStatus.UNKNOWN
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = count.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = tint)
        }
    }
}

@Composable
fun StationAvailabilityCard(
    station: StationEntity,
    isVehicleCompatible: Boolean,
    onBookClick: () -> Unit
) {
    val status = getAvailabilityStatus(station)
    val (statusText, statusColor, statusBg) = when (status) {
        AvailabilityStatus.AVAILABLE -> Triple("Available (${station.availableSlots}/${station.totalSlots} slots)", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        AvailabilityStatus.FULL -> Triple("Full (0/${station.totalSlots} slots)", Color(0xFFE65100), Color(0xFFFFCC80))
        AvailabilityStatus.CLOSED -> Triple("Closed", Color(0xFFC62828), Color(0xFFFFCDD2))
        AvailabilityStatus.UNKNOWN -> Triple("Availability unknown", Color(0xFF757575), Color(0xFFEEEEEE))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

            // Status & Compatibility Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (status) {
                            AvailabilityStatus.AVAILABLE -> Icons.Default.CheckCircle
                            AvailabilityStatus.FULL, AvailabilityStatus.CLOSED -> Icons.Default.Warning
                            AvailabilityStatus.UNKNOWN -> Icons.Default.Info
                        }
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = statusText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }

                if (isVehicleCompatible) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Compatible with your EV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Connectors & Distance
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

            // Footer: Tariff + Reserve Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${station.pricePerKwh}/kWh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = status != AvailabilityStatus.CLOSED
                ) {
                    Text(if (status == AvailabilityStatus.CLOSED) "Station Closed" else "Reserve Slot", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
