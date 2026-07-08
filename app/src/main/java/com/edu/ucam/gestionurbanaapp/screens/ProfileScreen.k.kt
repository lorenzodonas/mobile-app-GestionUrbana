package com.edu.ucam.gestionurbanaapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var foto by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    nombre = doc.getString("nombre") ?: ""
                    email = doc.getString("email") ?: ""
                    foto = doc.getString("foto") ?: ""
                    rol = doc.getString("rol") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val buttonColor = Color(0xFFC97F61)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                        // FOTO PERFIL
                        if (foto.isNotEmpty()) {
                            AsyncImage(
                                model = foto,
                                contentDescription = "Foto perfil",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin foto")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(nombre, fontSize = 22.sp, color = titleColor)
                        Text(email, fontSize = 16.sp, color = titleColor)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Rol: $rol", color = titleColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar perfil", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onChangePasswordClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        onLogoutClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", color = Color.White)
                }
            }
        }
    }
}