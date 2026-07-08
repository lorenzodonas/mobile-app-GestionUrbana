package com.edu.ucam.gestionurbanaapp



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edu.ucam.gestionurbanaapp.screens.CreateIncidentScreen
import com.edu.ucam.gestionurbanaapp.screens.LoginScreen
import com.edu.ucam.gestionurbanaapp.screens.RegisterScreen
import com.edu.ucam.gestionurbanaapp.screens.HomeScreen
import com.edu.ucam.gestionurbanaapp.screens.IncidentCreatedScreen
import com.edu.ucam.gestionurbanaapp.screens.IncidentsScreen
import com.edu.ucam.gestionurbanaapp.screens.ProfileScreen
import com.edu.ucam.gestionurbanaapp.navigation.AppNavigation
import android.util.Log


import com.edu.ucam.gestionurbanaapp.ui.theme.GestionUrbanaAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionUrbanaAppTheme {
                AppNavigation()
            }
        }
    }
}