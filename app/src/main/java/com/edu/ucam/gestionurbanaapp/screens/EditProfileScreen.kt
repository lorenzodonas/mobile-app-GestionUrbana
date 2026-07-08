package com.edu.ucam.gestionurbanaapp.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun EditProfileScreen(
    onSaveSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val uid = auth.currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    nombre = doc.getString("nombre") ?: ""
                    email = doc.getString("email") ?: ""
                    fotoUrl = doc.getString("foto") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_LONG).show()
                }
        } else {
            isLoading = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val buttonColor = Color(0xFFC97F61)
    val textFieldColor = Color(0xFFF7F2F2)

    val formValid = nombre.isNotBlank() && !isSaving

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Editar perfil",
                            fontSize = 28.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Nueva foto de perfil",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else if (fotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = "Foto de perfil",
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
                                Text("Sin foto", color = titleColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Seleccionar foto",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.height(0.dp))
                            Text("Seleccionar foto", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = textFieldColor,
                                unfocusedContainerColor = textFieldColor,
                                focusedBorderColor = borderColor,
                                unfocusedBorderColor = borderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = textFieldColor,
                                disabledBorderColor = borderColor,
                                disabledTextColor = Color.DarkGray,
                                disabledLabelColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (uid == null) {
                                    Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_LONG).show()
                                    return@Button
                                }

                                isSaving = true

                                fun saveProfile(finalFotoUrl: String) {
                                    val updates = mapOf(
                                        "nombre" to nombre,
                                        "foto" to finalFotoUrl
                                    )

                                    db.collection("usuarios")
                                        .document(uid)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                            onSaveSuccess()
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_LONG).show()
                                        }
                                }

                                if (selectedImageUri != null) {
                                    val imageRef = storage.reference
                                        .child("usuarios")
                                        .child(uid)
                                        .child("perfil.jpg")

                                    imageRef.putFile(selectedImageUri!!)
                                        .addOnSuccessListener {
                                            imageRef.downloadUrl
                                                .addOnSuccessListener { downloadUri ->
                                                    saveProfile(downloadUri.toString())
                                                }
                                                .addOnFailureListener {
                                                    isSaving = false
                                                    Toast.makeText(context, "Error obteniendo URL de imagen", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            Toast.makeText(context, "Error subiendo imagen", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    saveProfile(fotoUrl)
                                }
                            },
                            enabled = formValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar cambios",
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.height(0.dp))
                                Text("Guardar cambios", color = Color.Black, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = onBackClick) {
                            Text("Volver", color = titleColor)
                        }
                    }
                }
            }
        }
    }
}