package com.evstationfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.evstationfinder.ui.*

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EVStationFinderTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    val showBottomBar = when (currentScreen) {
        is Screen.Home, is Screen.Map, is Screen.Bookings, is Screen.Profile -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = { viewModel.selectTab("home") },
                        icon = { Icon(Icons.Default.EvStation, contentDescription = "Home") },
                        label = { Text("Stations", fontWeight = FontWeight.SemiBold) }
                    )
                    NavigationBarItem(
                        selected = activeTab == "map",
                        onClick = { viewModel.selectTab("map") },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Map", fontWeight = FontWeight.SemiBold) }
                    )
                    NavigationBarItem(
                        selected = activeTab == "bookings",
                        onClick = { viewModel.selectTab("bookings") },
                        icon = { Icon(Icons.Default.EventNote, contentDescription = "Bookings") },
                        label = { Text("Bookings", fontWeight = FontWeight.SemiBold) }
                    )
                    NavigationBarItem(
                        selected = activeTab == "profile",
                        onClick = { viewModel.selectTab("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        when (val screen = currentScreen) {
            is Screen.Login -> LoginScreen(viewModel = viewModel, modifier = modifier)
            is Screen.Home -> HomeScreen(viewModel = viewModel, modifier = modifier)
            is Screen.BookSlot -> BookSlotScreen(stationId = screen.stationId, viewModel = viewModel, modifier = modifier)
            is Screen.Payment -> PaymentScreen(viewModel = viewModel, modifier = modifier)
            is Screen.Confirmation -> ConfirmationScreen(bookingId = screen.bookingId, viewModel = viewModel, modifier = modifier)
            is Screen.Bookings -> BookingsScreen(viewModel = viewModel, modifier = modifier)
            is Screen.Profile -> ProfileScreen(viewModel = viewModel, modifier = modifier)
            is Screen.Map -> MapScreen(viewModel = viewModel, modifier = modifier)
        }
    }
}
