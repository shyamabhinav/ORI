package com.example.meshchat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meshchat.mesh.MeshMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    repeating: Boolean,
    emergencies: List<MeshMessage>,
    onSendOnce: (String) -> Unit,
    onToggleRepeat: (String) -> Unit,
    onBack: () -> Unit
) {
    var body by remember { mutableStateOf("EMERGENCY — I need help") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS Mode") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (repeating) {
                    "Repeating SOS every 15s. The list keeps one live SOS per person — old copies are replaced."
                } else {
                    "Send SOS once, or repeat so new phones still hear it. Each person shows as a single row."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("SOS message") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSendOnce(body) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send SOS now")
            }
            Button(
                onClick = { onToggleRepeat(body) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (repeating) "Stop repeating SOS" else "Repeat SOS")
            }
            Spacer(Modifier.height(8.dp))
            Text("Emergency traffic", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(emergencies, key = { it.senderId }) { message ->
                    Text(
                        text = "${message.senderName} (id ${message.senderId.take(8)})\n${message.body}\n${message.hopLabel()}",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
