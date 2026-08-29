package com.example.meshchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeSelectionScreen(
    meshOn: Boolean,
    peerCount: Int,
    onStandardModeClick: () -> Unit,
    onSosModeClick: () -> Unit,
    onContactsClick: () -> Unit
) {
    Scaffold(
        topBar = { OriTopAppBar(meshOn = meshOn, peerCount = peerCount) },
        bottomBar = {
            OriBottomNavBar(
                selected = OriNavTab.MODES,
                onContactsClick = onContactsClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Mode",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (meshOn) {
                    "Mesh radio on · $peerCount phone(s) linked"
                } else {
                    "Starting Bluetooth mesh…"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Card(
                onClick = onStandardModeClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Standard Mode",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Chat through nearby phones. Messages hop until they reach the person.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                onClick = onSosModeClick,
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.onError, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Emergency,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "SOS Mode",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Flood an emergency packet through every phone in the mesh.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onError.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriTopAppBar(meshOn: Boolean = false, peerCount: Int = 0) {
    TopAppBar(
        title = {
            Text(
                text = "ORI",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Filled.NetworkCell,
                    contentDescription = if (meshOn) "Mesh on" else "Mesh off"
                )
            }
        },
        actions = {
            Text(
                text = if (meshOn) "$peerCount linked" else "offline",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

enum class OriNavTab { MODES, CONTACTS, SETTINGS }

@Composable
fun OriBottomNavBar(
    selected: OriNavTab,
    onModesClick: () -> Unit = {},
    onContactsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        NavigationBarItem(
            selected = selected == OriNavTab.MODES,
            onClick = onModesClick,
            icon = { Icon(Icons.Filled.Hub, contentDescription = "Modes") },
            label = { Text("Modes") }
        )
        NavigationBarItem(
            selected = selected == OriNavTab.CONTACTS,
            onClick = onContactsClick,
            icon = { Icon(Icons.Filled.Style, contentDescription = "Contacts") },
            label = { Text("Contacts") }
        )
        NavigationBarItem(
            selected = selected == OriNavTab.SETTINGS,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
