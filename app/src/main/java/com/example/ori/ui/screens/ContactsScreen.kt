package com.example.ori.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ori.ui.theme.SosAmber

data class Contact(
    val id: String,
    val name: String,
    val status: String,
    val statusColor: Color,
    val initial: String? = null // shown when there's no avatar image
)

private val nearbyContacts = listOf(
    Contact(id = "echo7", name = "Echo-7 Base", status = "Connected", statusColor = Color(0xFFBC4800)),
    Contact(id = "alpha", name = "Team Alpha", status = "Searching (15m)", statusColor = SosAmber)
)

private val savedContacts = listOf(
    Contact(id = "sarah", name = "Dr. Sarah Jenkins", status = "Offline", statusColor = Color(0xFFC3C6D7), initial = "S"),
    Contact(id = "marcus", name = "Marcus Cole", status = "Offline", statusColor = Color(0xFFC3C6D7), initial = "M")
)

@Composable
fun ContactsScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { OriTopAppBar() },
        bottomBar = { OriBottomNavBar(selected = OriNavTab.CONTACTS, onModesClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search contacts...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            ContactSection(title = "NEARBY NETWORKS", contacts = nearbyContacts, trailingIcon = Icons.Filled.MoreVert)
            ContactSection(title = "SAVED CONTACTS", contacts = savedContacts, trailingIcon = Icons.Filled.Chat)
        }
    }
}

@Composable
private fun ContactSection(
    title: String,
    contacts: List<Contact>,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                contacts.forEachIndexed { index, contact ->
                    ContactRow(contact = contact, trailingIcon = trailingIcon)
                    if (index != contacts.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* open contact detail */ }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Swap this for an AsyncImage (Coil) once real avatar URLs are wired up.
                if (contact.initial != null) {
                    Text(
                        text = contact.initial,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(contact.statusColor, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = contact.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        IconButton(onClick = { /* per-row action */ }) {
            Icon(
                trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
