package com.example.meshchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.meshchat.mesh.MeshEngine
import com.example.meshchat.mesh.MeshPermissions
import com.example.meshchat.navigation.OriNavGraph
import com.example.meshchat.ui.theme.MeshChatTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (MeshPermissions.hasAll(this)) {
            MeshEngine.startMesh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeshChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OriNavGraph(onEnterMesh = { ensurePermissionsAndStart() })
                }
            }
        }
    }

    private fun ensurePermissionsAndStart() {
        val missing = MeshPermissions.missing(this)
        if (missing.isEmpty()) {
            MeshEngine.startMesh()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
