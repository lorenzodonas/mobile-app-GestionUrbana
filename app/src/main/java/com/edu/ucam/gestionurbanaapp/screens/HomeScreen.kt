package com.edu.ucam.gestionurbanaapp.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.ucam.gestionurbanaapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    onCreateIncidentClick: () -> Unit = {},
    onViewIncidentsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var userName by remember { mutableStateOf("Usuario") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nombre") ?: "Usuario"
                    isLoading = false
                }
                .addOnFailureListener {
                    userName = "Usuario"
                    isLoading = false
                }
        } else {
            userName = "Usuario"
            isLoading = false
        }
    }

    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val buttonColor = Color(0xFFC97F61)
    val secondaryCardColor = Color(0xFFF7F2F2)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo Gestión Urbana",
                            modifier = Modifier.height(85.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Portal de Incidencias",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Bienvenido, $userName",
                            fontSize = 20.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gestiona y consulta incidencias de forma rápida y sencilla",
                            fontSize = 14.sp,
                            color = titleColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = secondaryCardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Resumen",
                            tint = titleColor,
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Acceso rápido",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = titleColor
                            )
                            Text(
                                text = "Crea una incidencia, consulta su estado o revisa tu perfil.",
                                fontSize = 14.sp,
                                color = titleColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

               HomeActionButton(
                    text = "Crear incidencia",
                    icon = Icons.Default.AddCircle,
                    buttonColor = buttonColor,
                    onClick = onCreateIncidentClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                HomeActionButton(
                    text = "Ver incidencias",
                    icon = Icons.Default.ListAlt,
                    buttonColor = buttonColor,
                    onClick = onViewIncidentsClick
                )

                Spacer(modifier = Modifier.height(14.dp))

               HomeActionButton(
                    text = "Mi perfil",
                    icon = Icons.Default.Person,
                    buttonColor = buttonColor,
                    onClick = onProfileClick
                )

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = secondaryCardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información",
                                tint = titleColor
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Información útil",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = titleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• Puedes registrar incidencias urbanas de forma sencilla.",
                            color = titleColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• Consulta el estado de tus solicitudes en cualquier momento.",
                            color = titleColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• Mantén actualizado tu perfil de usuario.",
                            color = titleColor,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = {
                        auth.signOut()
                        onLogoutClick()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = titleColor
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Cerrar sesión",
                        color = titleColor,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HomeActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Black
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color.Black,
            fontSize = 17.sp
        )
    }
}