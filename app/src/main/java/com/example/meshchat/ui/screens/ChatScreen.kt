package com.example.meshchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meshchat.mesh.MeshIdentity
import com.example.meshchat.mesh.MeshMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    peerName: String,
    peerId: String,
    localUser: MeshIdentity,
    messages: List<MeshMessage>,
    meshOn: Boolean,
    onSend: (String) -> Unit,
    onBack: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val thread = messages

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(peerName)
                        Text(
                            text = if (peerId == MeshMessage.BROADCAST_ID) {
                                "Broadcast to the mesh"
                            } else {
                                "id ${peerId.take(8)} · ${if (meshOn) "mesh on" else "mesh off"}"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(thread, key = { it.id }) { message ->
                    val mine = message.senderId == localUser.userId
                    MessageBubble(message = message, mine = mine)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message over mesh") },
                    enabled = meshOn
                )
                IconButton(
                    onClick = {
                        onSend(draft)
                        draft = ""
                    },
                    enabled = meshOn && draft.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MeshMessage, mine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (mine) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                if (!mine) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(text = message.body, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (mine && message.hop <= 0) {
                        "Sent from this phone · check the other phone for hops"
                    } else {
                        message.hopLabel()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.hop > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (!mine) {
                    Text(
                        text = "from id ${message.senderId.take(8)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
