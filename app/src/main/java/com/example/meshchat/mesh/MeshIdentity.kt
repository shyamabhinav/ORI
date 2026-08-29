package com.example.meshchat.mesh

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

data class MeshIdentity(
    val userId: String,
    val displayName: String
) {
    fun endpointName(): String = "$displayName|$userId"

    companion object {
        private const val PREFS = "mesh_identity"
        private const val KEY_ID = "user_id"
        private const val KEY_NAME = "display_name"

        fun load(context: Context): MeshIdentity {
            val prefs = prefs(context)
            val id = prefs.getString(KEY_ID, null) ?: UUID.randomUUID().toString().also {
                prefs.edit().putString(KEY_ID, it).apply()
            }
            val name = prefs.getString(KEY_NAME, null) ?: "User-${id.take(4)}"
            return MeshIdentity(id, name)
        }

        fun saveDisplayName(context: Context, name: String) {
            prefs(context).edit().putString(KEY_NAME, name.trim().ifBlank { "Anonymous" }).apply()
        }

        fun parseEndpointName(endpointName: String): Pair<String, String> {
            val parts = endpointName.split("|", limit = 2)
            return if (parts.size == 2) {
                parts[0] to parts[1]
            } else {
                endpointName to endpointName
            }
        }

        private fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }
}
