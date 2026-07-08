package com.edu.ucam.gestionurbanaapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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

private suspend fun getAddressFromLatLng(
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
fun CreateIncidentScreen(
    onIncidentCreated: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val uid = auth.currentUser?.uid

    var usuarioNombre by remember { mutableStateOf("") }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var asignadoA by remember { mutableStateOf("tec1") }

    var isLoading by remember { mutableStateOf(false) }

    val selectedImages = remember { mutableStateListOf<Uri>() }

    val initialLatLng = LatLng(37.1773, -3.5986) // Granada
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 14f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted

        if (!granted) {
            Toast.makeText(
                context,
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    usuarioNombre = doc.getString("nombre") ?: ""
                }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val nuevas = uris.filterNot { it in selectedImages }.filter { uri ->
            val type = context.contentResolver.getType(uri)
            val isValidType = type == "image/jpeg" || type == "image/png"

            if (!isValidType) {
                Toast.makeText(context, "Solo JPG o PNG", Toast.LENGTH_SHORT).show()
            }

            isValidType
        }

        selectedImages.addAll(nuevas)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    selectedLatLng = latLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 17f)

                    scope.launch {
                        val resolvedAddress = getAddressFromLatLng(
                            context = context,
                            lat = latLng.latitude,
                            lng = latLng.longitude
                        )

                        if (!resolvedAddress.isNullOrBlank()) {
                            direccion = resolvedAddress
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "No se pudo obtener la ubicación actual. Puedes marcarla manualmente en el mapa.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error obteniendo ubicación: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            uid != null &&
            usuarioNombre.isNotBlank() &&
            selectedLatLng != null &&
            !isLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
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
                        text = "Crear incidencia",
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

                    Button(
                        onClick = { getCurrentLocation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text("Usar mi ubicación actual", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "También puedes tocar el mapa para marcar el punto manualmente",
                        fontSize = 14.sp,
                        color = titleColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission
                        ),
                        onMapClick = { latLng ->
                            selectedLatLng = latLng

                            scope.launch {
                                val resolvedAddress = getAddressFromLatLng(
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
                            "Toca el mapa para colocar el marcador"
                        },
                        color = titleColor,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir imágenes", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedImages.isEmpty()) {
                        Text(
                            text = "No has seleccionado imágenes.",
                            color = titleColor
                        )
                    } else {
                        Text(
                            text = "Imágenes seleccionadas: ${selectedImages.size}",
                            color = titleColor,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedImages.forEach { uri ->
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Imagen seleccionada",
                                        modifier = Modifier.size(100.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = { selectedImages.remove(uri) },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (uid == null) {
                                Toast.makeText(
                                    context,
                                    "Usuario no autenticado",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val latLng = selectedLatLng
                            if (latLng == null) {
                                Toast.makeText(
                                    context,
                                    "Selecciona la ubicación en el mapa",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            isLoading = true

                            val incidenciasRef = db.collection("incidencias").document()
                            val incidentId = incidenciasRef.id

                            val locations = listOf(
                                mapOf(
                                    "lat" to latLng.latitude,
                                    "lng" to latLng.longitude
                                )
                            )

                            fun saveIncident(imageUrls: List<String>) {
                                val data = hashMapOf(
                                    "asignadoA" to asignadoA,
                                    "descripcion" to descripcion,
                                    "direccion" to direccion,
                                    "estado" to "pendiente",
                                    "fechaCreacion" to Timestamp.now(),
                                    "fechaActualizacion" to Timestamp.now(),
                                    "fechaEnProceso" to null,
                                    "fechaFinalizacion" to null,
                                    "id" to incidentId,
                                    "imagenes" to imageUrls,
                                    "locations" to locations,
                                    "titulo" to titulo,
                                    "usuarioId" to uid,
                                    "usuarioNombre" to usuarioNombre
                                )

                                incidenciasRef.set(data)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Incidencia creada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onIncidentCreated(incidentId)
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Error al crear incidencia: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }

                            if (selectedImages.isEmpty()) {
                                saveIncident(emptyList())
                            } else {
                                val uploadedUrls = mutableListOf<String>()
                                var uploadedCount = 0
                                val totalImages = selectedImages.size

                                selectedImages.forEachIndexed { index, uri ->
                                    val imageRef = storage.reference
                                        .child("incidences")
                                        .child(uid)
                                        .child("${System.currentTimeMillis()}_img_$index.jpg")

                                    imageRef.putFile(uri)
                                        .addOnSuccessListener {
                                            imageRef.downloadUrl
                                                .addOnSuccessListener { downloadUri ->
                                                    uploadedUrls.add(downloadUri.toString())
                                                    uploadedCount++

                                                    if (uploadedCount == totalImages) {
                                                        saveIncident(uploadedUrls)
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Error obteniendo URL de imagen: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Text("Crear incidencia", color = Color.Black, fontSize = 16.sp)
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