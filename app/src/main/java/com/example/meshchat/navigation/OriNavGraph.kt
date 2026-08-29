package com.example.meshchat.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.meshchat.mesh.MeshEngine
import com.example.meshchat.ui.screens.ChatScreen
import com.example.meshchat.ui.screens.ContactsScreen
import com.example.meshchat.ui.screens.LoginScreen
import com.example.meshchat.ui.screens.ModeSelectionScreen
import com.example.meshchat.ui.screens.SignUpScreen
import com.example.meshchat.ui.screens.SosScreen

object OriRoutes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val MODE = "mode"
    const val CONTACTS = "contacts"
    const val SOS = "sos"
    const val CHAT = "chat/{peerId}/{peerName}"

    fun chat(peerId: String, peerName: String): String {
        return "chat/${Uri.encode(peerId)}/${Uri.encode(peerName)}"
    }
}

@Composable
fun OriNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = OriRoutes.LOGIN,
    onEnterMesh: () -> Unit
) {
    val identity by MeshEngine.identity.collectAsState()
    val running by MeshEngine.running.collectAsState()
    val peers by MeshEngine.peers.collectAsState()
    val messages by MeshEngine.messages.collectAsState()
    val emergencyMode by MeshEngine.emergencyMode.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(OriRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { name ->
                    MeshEngine.setDisplayName(name)
                    onEnterMesh()
                    navController.navigate(OriRoutes.MODE) {
                        popUpTo(OriRoutes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(OriRoutes.SIGN_UP) }
            )
        }

        composable(OriRoutes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = { name ->
                    MeshEngine.setDisplayName(name)
                    onEnterMesh()
                    navController.navigate(OriRoutes.MODE) {
                        popUpTo(OriRoutes.LOGIN) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(OriRoutes.MODE) {
            ModeSelectionScreen(
                meshOn = running,
                peerCount = peers.size,
                onStandardModeClick = { navController.navigate(OriRoutes.CONTACTS) },
                onSosModeClick = { navController.navigate(OriRoutes.SOS) },
                onContactsClick = { navController.navigate(OriRoutes.CONTACTS) }
            )
        }

        composable(OriRoutes.CONTACTS) {
            ContactsScreen(
                meshOn = running,
                peers = peers,
                onOpenChat = { peerId, peerName ->
                    navController.navigate(OriRoutes.chat(peerId, peerName))
                },
                onBack = { navController.popBackStack() },
                onSettings = { navController.popBackStack(OriRoutes.MODE, inclusive = false) }
            )
        }

        composable(OriRoutes.SOS) {
            SosScreen(
                repeating = emergencyMode,
                emergencies = messages.filter { it.isEmergency }.distinctBy { it.senderId },
                onSendOnce = { MeshEngine.sendEmergency(it) },
                onToggleRepeat = { text -> MeshEngine.setEmergencyMode(!emergencyMode, text) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = OriRoutes.CHAT,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType }
            )
        ) { entry ->
            val peerId = Uri.decode(entry.arguments?.getString("peerId").orEmpty())
            val peerName = Uri.decode(entry.arguments?.getString("peerName").orEmpty())
            val thread = messages.filter { it.belongsToThread(identity.userId, peerId) }
            ChatScreen(
                peerName = peerName,
                peerId = peerId,
                localUser = identity,
                messages = thread,
                meshOn = running,
                onSend = { text -> MeshEngine.sendChat(text, peerId) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
