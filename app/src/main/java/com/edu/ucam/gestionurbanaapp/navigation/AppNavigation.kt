package com.edu.ucam.gestionurbanaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edu.ucam.gestionurbanaapp.screens.ChangePasswordScreen
import com.edu.ucam.gestionurbanaapp.screens.CreateIncidentScreen
import com.edu.ucam.gestionurbanaapp.screens.EditIncidentScreen
import com.edu.ucam.gestionurbanaapp.screens.EditProfileScreen
import com.edu.ucam.gestionurbanaapp.screens.HomeScreen
import com.edu.ucam.gestionurbanaapp.screens.IncidentDetailScreen
import com.edu.ucam.gestionurbanaapp.screens.IncidentsScreen
import com.edu.ucam.gestionurbanaapp.screens.LoginScreen
import com.edu.ucam.gestionurbanaapp.screens.ProfileScreen
import com.edu.ucam.gestionurbanaapp.screens.RegisterScreen
import com.edu.ucam.gestionurbanaapp.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"
    const val CREATE_INCIDENT = "create_incident"
    const val INCIDENTS = "incidents"
    const val INCIDENT_DETAIL = "incident_detail/{incidentId}"
    const val EDIT_INCIDENT = "edit_incident/{incidentId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val startDestination = if (
        auth.currentUser != null && sessionManager.isSessionRemembered()
    ) {
        Routes.HOME
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onCreateIncidentClick = {
                    navController.navigate(Routes.CREATE_INCIDENT)
                },
                onViewIncidentsClick = {
                    navController.navigate(Routes.INCIDENTS)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    sessionManager.clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfileClick = {
                    navController.navigate(Routes.EDIT_PROFILE)
                },
                onChangePasswordClick = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    sessionManager.clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onPasswordChanged = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CREATE_INCIDENT) {
            CreateIncidentScreen(
                onIncidentCreated = { incidentId ->
                    navController.navigate("incident_detail/$incidentId") {
                        popUpTo(Routes.CREATE_INCIDENT) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.INCIDENTS) {
            IncidentsScreen(
                onIncidentClick = { incidentId ->
                    navController.navigate("incident_detail/$incidentId")
                },
                onEditIncidentClick = { incidentId ->
                    navController.navigate("edit_incident/$incidentId")
                }
            )
        }

        composable(
            route = Routes.INCIDENT_DETAIL,
            arguments = listOf(
                navArgument("incidentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""

            IncidentDetailScreen(
                incidentId = incidentId,
                onEditClick = { id ->
                    navController.navigate("edit_incident/$id")
                },
                onDeleteSuccess = {
                    navController.navigate(Routes.INCIDENTS) {
                        popUpTo("incident_detail/$incidentId") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_INCIDENT,
            arguments = listOf(
                navArgument("incidentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""

            EditIncidentScreen(
                incidentId = incidentId,
                onUpdateSuccess = {
                    navController.navigate("incident_detail/$incidentId") {
                        popUpTo("edit_incident/$incidentId") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}