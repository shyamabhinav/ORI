package com.example.meshchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meshchat.mesh.MeshMessage
import com.example.meshchat.mesh.MeshPeer
import com.example.meshchat.ui.theme.SosAmber

data class Contact(
    val id: String,
    val name: String,
    val status: String,
    val statusColor: Color,
    val initial: String? = null
)

@Composable
fun ContactsScreen(
    meshOn: Boolean,
    peers: List<MeshPeer>,
    onOpenChat: (peerId: String, peerName: String) -> Unit,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val visible = peers.filter {
        it.displayName.contains(query, ignoreCase = true) ||
            it.userId.contains(query, ignoreCase = true)
    }
    val nearby = visible.filter { it.connected && it.hopCount <= 0 }.map { it.toContact() }
    val far = visible.filter { !(it.connected && it.hopCount <= 0) }.map { it.toContact() }

    Scaffold(
        topBar = { OriTopAppBar(meshOn = meshOn, peerCount = peers.size) },
        bottomBar = {
            OriBottomNavBar(
                selected = OriNavTab.CONTACTS,
                onModesClick = onBack,
                onSettingsClick = onSettings
            )
        }
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
                placeholder = { Text("Search name or id...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            ContactSection(
                title = "EVERYONE ON MESH",
                contacts = listOf(
                    Contact(
                        id = MeshMessage.BROADCAST_ID,
                        name = "Everyone on mesh",
                        status = if (meshOn) "Floods through hops" else "Mesh starting…",
                        statusColor = SosAmber,
                        initial = "*"
                    )
                ),
                trailingIcon = Icons.Filled.Chat,
                onContactClick = onOpenChat,
                emptyText = ""
            )

            ContactSection(
                title = "DIRECT BLUETOOTH",
                contacts = nearby,
                trailingIcon = Icons.Filled.Chat,
                onContactClick = onOpenChat,
                emptyText = "Nobody in radio range yet."
            )

            ContactSection(
                title = "OUT OF RANGE (VIA HOPS)",
                contacts = far,
                trailingIcon = Icons.Filled.Chat,
                onContactClick = onOpenChat,
                emptyText = "If your friend is far, keep a third phone between you. Their name and id appear here when an announce hops through."
            )
        }
    }
}

private fun MeshPeer.toContact(): Contact = Contact(
    id = userId,
    name = displayName,
    status = statusLabel(),
    statusColor = if (connected && hopCount <= 0) Color(0xFFBC4800) else SosAmber,
    initial = displayName.take(1).uppercase()
)

@Composable
private fun ContactSection(
    title: String,
    contacts: List<Contact>,
    trailingIcon: ImageVector,
    onContactClick: (String, String) -> Unit,
    emptyText: String
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
            if (contacts.isEmpty()) {
                Text(
                    text = emptyText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    contacts.forEachIndexed { index, contact ->
                        ContactRow(
                            contact = contact,
                            trailingIcon = trailingIcon,
                            onClick = { onContactClick(contact.id, contact.name) }
                        )
                        if (index != contacts.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    trailingIcon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                if (contact.id == MeshMessage.BROADCAST_ID) {
                    Icon(Icons.Filled.Groups, contentDescription = null)
                } else {
                    Text(
                        text = contact.initial ?: contact.name.take(1),
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
        IconButton(onClick = onClick) {
            Icon(
                trailingIcon,
                contentDescription = "Chat",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
