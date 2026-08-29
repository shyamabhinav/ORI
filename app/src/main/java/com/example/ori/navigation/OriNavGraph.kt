package com.example.ori.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ori.ui.screens.ContactsScreen
import com.example.ori.ui.screens.LoginScreen
import com.example.ori.ui.screens.ModeSelectionScreen
import com.example.ori.ui.screens.SignUpScreen

/**
 * Flow:
 *  Login (start) --[Login button]--> Mode
 *  Login --[tap the "Sign Up" link]--> SignUp --[Sign Up button]--> Mode
 *  Mode --[Standard Mode card]--> Contacts
 *  Mode --[SOS Mode card]--> (hook up to your SOS broadcast flow)
 */
object OriRoutes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val MODE = "mode"
    const val CONTACTS = "contacts"
}

@Composable
fun OriNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = OriRoutes.LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(OriRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(OriRoutes.MODE) {
                        popUpTo(OriRoutes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(OriRoutes.SIGN_UP)
                }
            )
        }

        composable(OriRoutes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(OriRoutes.MODE) {
                        popUpTo(OriRoutes.LOGIN) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(OriRoutes.MODE) {
            ModeSelectionScreen(
                onStandardModeClick = {
                    navController.navigate(OriRoutes.CONTACTS)
                },
                onSosModeClick = {
                    // Wire this up to your emergency-broadcast flow.
                }
            )
        }

        composable(OriRoutes.CONTACTS) {
            ContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
