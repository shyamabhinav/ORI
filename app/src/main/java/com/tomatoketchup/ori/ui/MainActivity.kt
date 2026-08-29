package com.tomatoketchup.ori.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tomatoketchup.ori.data.repo.MessageRepository
import com.tomatoketchup.ori.data.room.AppDatabase
import com.tomatoketchup.ori.network.NetworkClient

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        val repo = MessageRepository(db.messageDao())
        val api = NetworkClient.create(applicationContext)

        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MessageViewModel(repo, api) as T
            }
        }

        val vm: MessageViewModel by viewModels { factory }

        setContent {
            var sender by remember { mutableStateOf("") }
            var recipient by remember { mutableStateOf("") }
            var body by remember { mutableStateOf("") }

            val messages by vm.incomingFor(recipient).collectAsState(initial = emptyList())

            Scaffold(topBar = { TopAppBar(title = { Text("ORI — Mesh Chat Demo") }) }) { inner ->
                Column(modifier = Modifier.padding(inner).padding(12.dp)) {
                    OutlinedTextField(value = sender, onValueChange = { sender = it }, label = { Text("Sender ID") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Recipient ID") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { if (sender.isNotBlank() && recipient.isNotBlank() && body.isNotBlank()) { vm.sendMessage(sender, recipient, body); body = "" } }, modifier = Modifier.fillMaxWidth()) {
                        Text("Send")
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Incoming messages for: $recipient", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(messages) { m ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("From: ${m.senderId}", style = MaterialTheme.typography.titleSmall)
                                    Text(m.body)
                                    Text("Status: ${m.status}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
