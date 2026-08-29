package com.example.meshchat

import android.app.Application
import com.example.meshchat.mesh.MeshEngine

class MeshChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MeshEngine.init(this)
    }
}
