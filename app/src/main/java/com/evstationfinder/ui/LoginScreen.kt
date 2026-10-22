package com.evstationfinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evstationfinder.data.ProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val allProfiles by viewModel.allProfiles.collectAsState()
    val activeProfile by viewModel.profile.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("Bandra Kurla Complex, Mumbai") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("Create New Driver Profile", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Priya Nair") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. priya@email.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. +91 98765 00000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Default Location") },
                        placeholder = { Text("e.g. Worli, Mumbai") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newEmail.isNotBlank()) {
                            viewModel.createAndLogin(newName, newEmail, newPhone, newLocation)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Register & Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Hero Logo & Title
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.EvStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EV Station Finder",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Select driver profile to continue",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Available Profiles (${allProfiles.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Profiles List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            allProfiles.forEach { profile ->
                val isActive = activeProfile?.id == profile.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.loginAs(profile.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = profile.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = profile.email,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = profile.savedLocation,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.loginAs(profile.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Active", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showCreateDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Driver Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
