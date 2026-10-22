package com.evstationfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evstationfinder.data.StationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.allStations.collectAsState()
    var selectedStation by remember(stations) { mutableStateOf<StationEntity?>(stations.firstOrNull()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE3EDE8))
    ) {
        // Map Canvas simulation with grid lines and EV station markers
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw map background roads
            drawRect(color = Color(0xFFE8F1EC))

            // Main roads
            drawLine(color = Color.White, start = Offset(0f, height * 0.4f), end = Offset(width, height * 0.4f), strokeWidth = 24f)
            drawLine(color = Color.White, start = Offset(width * 0.3f, 0f), end = Offset(width * 0.3f, height), strokeWidth = 20f)
            drawLine(color = Color.White, start = Offset(width * 0.7f, 0f), end = Offset(width * 0.7f, height), strokeWidth = 18f)
            drawLine(color = Color.White, start = Offset(0f, height * 0.7f), end = Offset(width, height * 0.7f), strokeWidth = 20f)
        }

        // Top App Bar
        TopAppBar(
            title = { Text("EV Stations Map", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        )

        // Station Pin Markers
        val pinPositions = listOf(
            Offset(0.3f, 0.38f),
            Offset(0.7f, 0.42f),
            Offset(0.28f, 0.68f),
            Offset(0.68f, 0.72f)
        )

        stations.take(pinPositions.size).forEachIndexed { index, station ->
            val pos = pinPositions[index]
            val isSelected = selectedStation?.id == station.id

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (pos.x * 320).dp,
                            y = (pos.y * 500).dp
                        )
                        .clickable { selectedStation = station }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(if (isSelected) 48.dp else 40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = station.name,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (isSelected) 28.dp else 22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Quick Station Card
        selectedStation?.let { station ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = station.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = station.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF8E1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = station.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${station.connector} · ${station.powerKw} kW", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "${station.distanceKm} km", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(text = "₹${station.pricePerKwh}/kWh", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val defaultVehicle = viewModel.vehicles.value.firstOrNull()?.id ?: "nexon"
                            viewModel.startBooking(station.id, defaultVehicle)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Reserve Slot at ${station.name}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
