package com.example.ori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ori.navigation.OriNavGraph
import com.example.ori.ui.theme.OriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OriTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OriNavGraph()
                }
            }
        }
    }
}
