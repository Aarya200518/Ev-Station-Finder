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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
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
fun RangePlannerScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val stations by viewModel.allStations.collectAsState()

    var selectedVehicleId by remember(vehicles) { mutableStateOf(vehicles.firstOrNull()?.id ?: "nexon") }
    var fullChargeRangeInput by remember { mutableStateOf("300") }
    var batteryPctInput by remember { mutableStateOf("40") }
    var destinationDistInput by remember { mutableStateOf("100") }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    // Parse inputs safely with validation
    val fullChargeRange = fullChargeRangeInput.toDoubleOrNull() ?: 0.0
    val batteryPct = batteryPctInput.toDoubleOrNull() ?: -1.0
    val destinationDist = destinationDistInput.toDoubleOrNull() ?: -1.0

    // Validation checks
    val isRangeValid = fullChargeRange > 0.0
    val isBatteryValid = batteryPct in 0.0..100.0
    val isDestValid = destinationDist >= 0.0
    val areInputsValid = isRangeValid && isBatteryValid && isDestValid

    val estimatedRemainingRange = if (areInputsValid) (fullChargeRange * batteryPct) / 100.0 else 0.0
    val rangeBuffer = if (areInputsValid) estimatedRemainingRange - destinationDist else 0.0
    val isReachable = areInputsValid && rangeBuffer >= 0.0
    val shortfall = if (areInputsValid && rangeBuffer < 0.0) -rangeBuffer else 0.0

    // Filter compatible or nearby charging stations if range is insufficient or for planning
    val compatibleStations = stations.filter { station ->
        selectedVehicle == null || station.connector.contains(selectedVehicle.connector, ignoreCase = true) || station.powerKw >= 50
    }.sortedBy { it.distanceKm }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart EV Range & Charging Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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

            // Trip & Battery Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Trip & Battery Parameters", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = fullChargeRangeInput,
                        onValueChange = { fullChargeRangeInput = it },
                        label = { Text("100% Full-Charge Range (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isRangeValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isRangeValid) {
                        Text("Please enter a valid range greater than 0 km.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = batteryPctInput,
                        onValueChange = { batteryPctInput = it },
                        label = { Text("Current Battery Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isBatteryValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isBatteryValid) {
                        Text("Battery percentage must be between 0 and 100.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = destinationDistInput,
                        onValueChange = { destinationDistInput = it },
                        label = { Text("Destination Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isDestValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isDestValid) {
                        Text("Destination distance cannot be negative.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }
            }

            // Calculation Results Card
            if (areInputsValid) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isReachable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
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
                                imageVector = if (isReachable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isReachable) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (isReachable) "Destination Within Estimated Range" else "Destination Outside Estimated Range!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isReachable) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                        }

                        HorizontalDivider(color = if (isReachable) Color(0xFFC8E6C9) else Color(0xFFFFCDD2))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Remaining Range:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.1f".format(estimatedRemainingRange)} km", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Destination Distance:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${destinationDist} km", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isReachable) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Range Buffer:", fontSize = 14.sp, color = Color(0xFF2E7D32))
                                Text("+${"%.1f".format(rangeBuffer)} km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Range Shortfall:", fontSize = 14.sp, color = Color(0xFFC62828))
                                Text("-${"%.1f".format(shortfall)} km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: This is an estimate. Actual driving range depends on traffic, speed, weather, terrain, and vehicle condition.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Charging Station Suggestions Section (Shown when range is insufficient or for planning)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!isReachable) "Recommended Charging Stops" else "Nearby Charging Stations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${compatibleStations.size} available",
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
                                text = "No compatible charging stations found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    compatibleStations.take(4).forEach { station ->
                        PlannerStationCard(
                            station = station,
                            onBookClick = {
                                viewModel.startBooking(station.id, selectedVehicleId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlannerStationCard(
    station: StationEntity,
    onBookClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Text(text = station.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = station.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${station.distanceKm} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                    Text(text = "${station.connector} · ${station.powerKw} kW", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Reserve Slot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
