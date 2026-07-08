package com.edu.ucam.gestionurbanaapp.screens

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private suspend fun getAddressFromLatLngEdit(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results: List<Address>? = geocoder.getFromLocation(lat, lng, 1)

            val address = results?.firstOrNull()
            if (address != null) {
                val street = address.thoroughfare ?: ""
                val number = address.subThoroughfare ?: ""
                val locality = address.locality ?: ""
                val adminArea = address.adminArea ?: ""

                val mainLine = listOf(street, number)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                when {
                    mainLine.isNotBlank() && locality.isNotBlank() -> "$mainLine, $locality"
                    mainLine.isNotBlank() -> mainLine
                    locality.isNotBlank() && adminArea.isNotBlank() -> "$locality, $adminArea"
                    !address.getAddressLine(0).isNullOrBlank() -> address.getAddressLine(0)
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun EditIncidentScreen(
    incidentId: String,
    onUpdateSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    if (incidentId.isBlank()) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "ID de incidencia inválido", Toast.LENGTH_LONG).show()
            onBackClick()
        }
        return
    }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var usuarioId by remember { mutableStateOf("") }

    val existingImages = remember { mutableStateListOf<String>() }
    val newImages = remember { mutableStateListOf<Uri>() }

    val defaultLatLng = LatLng(37.1773, -3.5986)
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 14f)
    }

    var isInitialLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(incidentId) {
        db.collection("incidencias")
            .document(incidentId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    titulo = doc.getString("titulo") ?: ""
                    descripcion = doc.getString("descripcion") ?: ""
                    direccion = doc.getString("direccion") ?: ""
                    usuarioId = doc.getString("usuarioId") ?: ""

                    existingImages.clear()
                    existingImages.addAll(doc.get("imagenes") as? List<String> ?: emptyList())

                    val rawLocations = doc.get("locations") as? List<Map<String, Any>>
                    val firstLocation = rawLocations?.firstOrNull()
                    val lat = (firstLocation?.get("lat") as? Number)?.toDouble()
                    val lng = (firstLocation?.get("lng") as? Number)?.toDouble()

                    if (lat != null && lng != null) {
                        val latLng = LatLng(lat, lng)
                        selectedLatLng = latLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 17f)
                    }
                } else {
                    Toast.makeText(context, "Incidencia no encontrada", Toast.LENGTH_LONG).show()
                    onBackClick()
                }
                isInitialLoading = false
            }
            .addOnFailureListener { e ->
                isInitialLoading = false
                Toast.makeText(
                    context,
                    "Error al cargar la incidencia: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val nuevas = uris.filterNot { it in newImages }
        newImages.addAll(nuevas)
    }

    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val buttonColor = Color(0xFFC97F61)
    val textFieldColor = Color(0xFFF7F2F2)

    val formValid = titulo.isNotBlank() &&
            descripcion.isNotBlank() &&
            direccion.isNotBlank() &&
            selectedLatLng != null &&
            !isSaving &&
            !isInitialLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        if (isInitialLoading) {
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Editar incidencia",
                            fontSize = 28.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth(),
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
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
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
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección / referencia") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = textFieldColor,
                                unfocusedContainerColor = textFieldColor,
                                focusedBorderColor = borderColor,
                                unfocusedBorderColor = borderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Ubicación de la incidencia",
                            fontSize = 18.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = false),
                            onMapClick = { latLng ->
                                selectedLatLng = latLng

                                scope.launch {
                                    val resolvedAddress = getAddressFromLatLngEdit(
                                        context = context,
                                        lat = latLng.latitude,
                                        lng = latLng.longitude
                                    )

                                    if (!resolvedAddress.isNullOrBlank()) {
                                        direccion = resolvedAddress
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "No se pudo obtener la dirección exacta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            selectedLatLng?.let { latLng ->
                                Marker(
                                    state = MarkerState(position = latLng),
                                    title = "Ubicación seleccionada"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (selectedLatLng != null) {
                                "Lat: ${selectedLatLng!!.latitude}, Lng: ${selectedLatLng!!.longitude}"
                            } else {
                                "Toca el mapa para seleccionar la ubicación"
                            },
                            color = titleColor,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Imágenes actuales",
                            fontSize = 18.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (existingImages.isEmpty()) {
                            Text(
                                text = "No hay imágenes guardadas.",
                                color = titleColor
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                existingImages.forEach { imageUrl ->
                                    Box {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Imagen actual",
                                            modifier = Modifier.size(100.dp),
                                            contentScale = ContentScale.Crop
                                        )

                                        IconButton(
                                            onClick = { existingImages.remove(imageUrl) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Quitar imagen",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Añadir imágenes", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (newImages.isNotEmpty()) {
                            Text(
                                text = "Nuevas imágenes: ${newImages.size}",
                                fontSize = 18.sp,
                                color = titleColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                newImages.forEach { uri ->
                                    Box {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Nueva imagen",
                                            modifier = Modifier.size(100.dp),
                                            contentScale = ContentScale.Crop
                                        )

                                        IconButton(
                                            onClick = { newImages.remove(uri) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Quitar imagen nueva",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val latLng = selectedLatLng
                                if (latLng == null) {
                                    Toast.makeText(
                                        context,
                                        "Selecciona la ubicación en el mapa",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }

                                isSaving = true

                                fun updateIncident(finalImageUrls: List<String>) {
                                    val updates = mapOf(
                                        "titulo" to titulo,
                                        "descripcion" to descripcion,
                                        "direccion" to direccion,
                                        "imagenes" to finalImageUrls,
                                        "locations" to listOf(
                                            mapOf(
                                                "lat" to latLng.latitude,
                                                "lng" to latLng.longitude
                                            )
                                        ),
                                        "fechaActualizacion" to Timestamp.now()
                                    )

                                    db.collection("incidencias")
                                        .document(incidentId)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            Toast.makeText(
                                                context,
                                                "Incidencia actualizada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onUpdateSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isSaving = false
                                            Toast.makeText(
                                                context,
                                                "Error al actualizar incidencia: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }

                                if (newImages.isEmpty()) {
                                    updateIncident(existingImages.toList())
                                } else {
                                    val uploadedUrls = mutableListOf<String>()
                                    var uploadedCount = 0
                                    val totalImages = newImages.size

                                    newImages.forEachIndexed { index, uri ->
                                        val imageRef = storage.reference
                                            .child("incidences")
                                            .child(usuarioId.ifBlank { "unknown_user" })
                                            .child("${System.currentTimeMillis()}_edit_$index.jpg")

                                        imageRef.putFile(uri)
                                            .addOnSuccessListener {
                                                imageRef.downloadUrl
                                                    .addOnSuccessListener { downloadUri ->
                                                        uploadedUrls.add(downloadUri.toString())
                                                        uploadedCount++

                                                        if (uploadedCount == totalImages) {
                                                            updateIncident(existingImages.toList() + uploadedUrls)
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isSaving = false
                                                        Toast.makeText(
                                                            context,
                                                            "Error obteniendo URL de imagen: ${e.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                isSaving = false
                                                Toast.makeText(
                                                    context,
                                                    "Error subiendo imágenes: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    }
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
                                Text("Guardar cambios", color = Color.Black, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = onBackClick) {
                            Text("Volver", color = titleColor)
                        }

                        if (!formValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Completa título, descripción, dirección y elige el punto en el mapa.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}