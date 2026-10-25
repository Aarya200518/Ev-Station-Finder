package com.evstationfinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evstationfinder.data.StationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingCostScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val stations by viewModel.allStations.collectAsState()

    var selectedVehicleId by remember(vehicles) { mutableStateOf(vehicles.firstOrNull()?.id ?: "nexon") }
    var currentBatteryInput by remember { mutableStateOf("20") }
    var targetBatteryInput by remember { mutableStateOf("80") }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    // Parse inputs safely
    val currentBattery = currentBatteryInput.toDoubleOrNull() ?: -1.0
    val targetBattery = targetBatteryInput.toDoubleOrNull() ?: -1.0

    // Validation checks
    val isCurrentValid = currentBattery in 0.0..100.0
    val isTargetValid = targetBattery in 0.0..100.0
    val isDeltaValid = isCurrentValid && isTargetValid && targetBattery >= currentBattery
    val isCapacityValid = selectedVehicle != null && selectedVehicle.capacityKwh > 0.0
    val areInputsValid = isCurrentValid && isTargetValid && isDeltaValid && isCapacityValid

    val energyRequired = if (areInputsValid && selectedVehicle != null) {
        (selectedVehicle.capacityKwh * (targetBattery - currentBattery)) / 100.0
    } else {
        0.0
    }

    val batteryAdded = if (areInputsValid) targetBattery - currentBattery else 0.0

    // Compare costs across all stations
    val stationCosts = stations.map { station ->
        val isCompatible = selectedVehicle == null ||
                station.connector.contains(selectedVehicle.connector, ignoreCase = true) ||
                selectedVehicle.connector.contains(station.connector.substringBefore(" · "), ignoreCase = true)
        val cost = energyRequired * station.pricePerKwh
        StationCostInfo(station, cost, isCompatible)
    }

    val compatibleStations = stationCosts.filter { it.isCompatible }.sortedBy { it.estimatedCost }
    val incompatibleStations = stationCosts.filter { !it.isCompatible }

    val cheapestStation = compatibleStations.firstOrNull()
    val expensiveStation = compatibleStations.lastOrNull()
    val potentialSavings = if (cheapestStation != null && expensiveStation != null) {
        expensiveStation.estimatedCost - cheapestStation.estimatedCost
    } else {
        0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EV Charging Cost Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle Selection Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Select Vehicle", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vehicles) { vehicle ->
                            val isSelected = selectedVehicleId == vehicle.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVehicleId = vehicle.id },
                                label = { Text("${vehicle.name} (${vehicle.capacityKwh} kWh)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    if (selectedVehicle != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Battery Capacity: ${selectedVehicle.capacityKwh} kWh · Connector: ${selectedVehicle.connector}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Battery Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Charging Parameters", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = currentBatteryInput,
                        onValueChange = { currentBatteryInput = it },
                        label = { Text("Current Battery Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isCurrentValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isCurrentValid) {
                        Text("Current battery must be between 0 and 100.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = targetBatteryInput,
                        onValueChange = { targetBatteryInput = it },
                        label = { Text("Target Battery Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isTargetValid || !isDeltaValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isTargetValid) {
                        Text("Target battery must be between 0 and 100.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    } else if (!isDeltaValid) {
                        Text("Target battery must be greater than or equal to current battery.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }
            }

            // Calculation Summary Card
            if (areInputsValid && selectedVehicle != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalAtm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Charging Requirement Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Battery To Be Added:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("+${"%.0f".format(batteryAdded)}% (${currentBattery.toInt()}% → ${targetBattery.toInt()}%)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Energy Required:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${"%.2f".format(energyRequired)} kWh", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        if (cheapestStation != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lowest Estimated Cost:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("₹${"%.0f".format(cheapestStation.estimatedCost)} (${cheapestStation.station.name})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (potentialSavings > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Potential Savings:", fontSize = 14.sp, color = Color(0xFF2E7D32))
                                Text("Up to ₹${"%.0f".format(potentialSavings)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Note: Estimates are calculated based on stored tariffs. Actual billing may vary due to charging losses, taxes, and station fees.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Station Price Comparison Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Charging Station Price Comparison",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sorted by lowest cost",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (compatibleStations.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No compatible charging stations found for this vehicle connector.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    compatibleStations.forEachIndexed { index, info ->
                        StationCostCard(
                            info = info,
                            isCheapest = index == 0,
                            onBookClick = {
                                viewModel.startBooking(info.station.id, selectedVehicleId)
                            }
                        )
                    }
                }

                if (incompatibleStations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Incompatible Stations (${incompatibleStations.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    incompatibleStations.forEach { info ->
                        StationCostCard(
                            info = info,
                            isCheapest = false,
                            onBookClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationCostCard(
    info: StationCostInfo,
    isCheapest: Boolean,
    onBookClick: () -> Unit
) {
    val station = info.station
    val cost = info.estimatedCost
    val isCompatible = info.isCompatible

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheapest && isCompatible) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = station.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (isCheapest && isCompatible) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32)
                            ) {
                                Text(
                                    text = "LOWEST COST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(text = station.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%.0f".format(cost)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${station.pricePerKwh}/kWh",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${station.connector} · ${station.powerKw} kW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompatible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }

                if (isCompatible) {
                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Reserve Slot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Incompatible Connector",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

data class StationCostInfo(
    val station: StationEntity,
    val estimatedCost: Double,
    val isCompatible: Boolean
)
