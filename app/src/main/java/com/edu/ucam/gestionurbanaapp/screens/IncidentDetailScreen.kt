package com.edu.ucam.gestionurbanaapp.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.edu.ucam.gestionurbanaapp.utils.HistoryChange
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

fun formatIncidentDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Pendiente de registrar"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

fun formatStatusName(status: String): String {
    return when (status.lowercase()) {
        "pendiente" -> "pendiente"
        "proceso", "en_proceso", "en proceso" -> "en proceso"
        "resuelta", "finalizada", "cerrada" -> "cerrada"
        "abierta" -> "abierta"
        else -> status.ifBlank { "sin valor" }
    }
}

fun formatHistoryValue(value: String): String {
    return when (value.lowercase()) {
        "pendiente" -> "pendiente"
        "proceso", "en_proceso", "en proceso" -> "en proceso"
        "resuelta", "finalizada", "cerrada" -> "cerrada"
        "abierta" -> "abierta"
        else -> value.ifBlank { "sin valor" }
    }
}

fun formatHistoryField(field: String): String {
    return when (field.lowercase()) {
        "estado" -> "Estado"
        "titulo" -> "Título"
        "descripcion" -> "Descripción"
        "direccion" -> "Dirección"
        "prioridad" -> "Prioridad"
        "asignadoa" -> "Asignado a"
        else -> field.ifBlank { "Campo" }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    onEditClick: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var usuarioNombre by remember { mutableStateOf("") }
    var usuarioId by remember { mutableStateOf("") }
    var asignadoA by remember { mutableStateOf("") }
    var fechaCreacion by remember { mutableStateOf<Timestamp?>(null) }
    var fechaActualizacion by remember { mutableStateOf<Timestamp?>(null) }

    val imagenes = remember { mutableStateListOf<String>() }
    val historialCambios = remember { mutableStateListOf<HistoryChange>() }

    var isLoading by remember { mutableStateOf(true) }
    var isUpdatingStatus by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun loadIncident() {
        isLoading = true

        db.collection("incidencias")
            .document(incidentId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    titulo = doc.getString("titulo") ?: ""
                    descripcion = doc.getString("descripcion") ?: ""
                    direccion = doc.getString("direccion") ?: ""
                    estado = doc.getString("estado") ?: "pendiente"
                    usuarioNombre = doc.getString("usuarioNombre") ?: ""
                    usuarioId = doc.getString("usuarioId") ?: ""
                    asignadoA = doc.getString("asignadoA") ?: ""
                    fechaCreacion = doc.getTimestamp("fechaCreacion")
                    fechaActualizacion = doc.getTimestamp("fechaActualizacion")

                    imagenes.clear()
                    imagenes.addAll(doc.get("imagenes") as? List<String> ?: emptyList())

                    db.collection("historico")
                        .whereEqualTo("incidenciaId", incidentId)
                        .get()
                        .addOnSuccessListener { result ->
                            historialCambios.clear()

                            val cambiosOrdenados = result.documents
                                .mapNotNull { historicoDoc ->
                                    val fecha = historicoDoc.getTimestamp("fecha")
                                    val usuarioNombreHistorico =
                                        historicoDoc.getString("usuarioNombre") ?: ""

                                    val cambios =
                                        historicoDoc.get("cambios") as? List<Map<String, Any>>

                                    cambios?.mapNotNull { cambio ->
                                        val campo = cambio["campo"] as? String ?: ""
                                        val anterior = cambio["anterior"]?.toString() ?: ""
                                        val nuevo = cambio["nuevo"]?.toString() ?: ""

                                        HistoryChange(
                                            campo = campo,
                                            anterior = anterior,
                                            nuevo = nuevo,
                                            fecha = fecha,
                                            usuarioNombre = usuarioNombreHistorico
                                        )
                                    }
                                }
                                .flatten()
                                .sortedBy { it.fecha?.seconds ?: 0L }

                            historialCambios.addAll(cambiosOrdenados)
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Error cargando histórico: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Incidencia no encontrada",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(
                    context,
                    "Error al cargar incidencia: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun guardarHistorico(
        cambios: List<Map<String, String>>,
        fecha: Timestamp,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val historico = hashMapOf(
            "incidenciaId" to incidentId,
            "fecha" to fecha,
            "usuarioId" to usuarioId,
            "usuarioNombre" to usuarioNombre,
            "cambios" to cambios
        )

        db.collection("historico")
            .add(historico)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun changeStatusToInProgress() {
        isUpdatingStatus = true

        val estadoAnterior = estado
        val estadoNuevo = "proceso"
        val now = Timestamp.now()

        db.collection("incidencias")
            .document(incidentId)
            .update(
                mapOf(
                    "estado" to estadoNuevo,
                    "fechaActualizacion" to now
                )
            )
            .addOnSuccessListener {
                guardarHistorico(
                    cambios = listOf(
                        mapOf(
                            "campo" to "Estado",
                            "anterior" to estadoAnterior,
                            "nuevo" to estadoNuevo
                        )
                    ),
                    fecha = now,
                    onSuccess = {
                        isUpdatingStatus = false
                        Toast.makeText(
                            context,
                            "Incidencia puesta en proceso",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadIncident()
                    },
                    onError = { e ->
                        isUpdatingStatus = false
                        Toast.makeText(
                            context,
                            "Estado actualizado, pero error guardando histórico: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        loadIncident()
                    }
                )
            }
            .addOnFailureListener { e ->
                isUpdatingStatus = false
                Toast.makeText(
                    context,
                    "Error al actualizar estado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun changeStatusToResolved() {
        isUpdatingStatus = true

        val estadoAnterior = estado
        val estadoNuevo = "resuelta"
        val now = Timestamp.now()

        db.collection("incidencias")
            .document(incidentId)
            .update(
                mapOf(
                    "estado" to estadoNuevo,
                    "fechaActualizacion" to now
                )
            )
            .addOnSuccessListener {
                guardarHistorico(
                    cambios = listOf(
                        mapOf(
                            "campo" to "Estado",
                            "anterior" to estadoAnterior,
                            "nuevo" to estadoNuevo
                        )
                    ),
                    fecha = now,
                    onSuccess = {
                        isUpdatingStatus = false
                        Toast.makeText(
                            context,
                            "Incidencia resuelta",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadIncident()
                    },
                    onError = { e ->
                        isUpdatingStatus = false
                        Toast.makeText(
                            context,
                            "Estado actualizado, pero error guardando histórico: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        loadIncident()
                    }
                )
            }
            .addOnFailureListener { e ->
                isUpdatingStatus = false
                Toast.makeText(
                    context,
                    "Error al finalizar incidencia: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    LaunchedEffect(incidentId) {
        loadIncident()
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
                            text = "Detalle de incidencia",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = titulo,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        StatusBadgeDetail(status = estado)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Histórico de cambios",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PrettyHistoryTimeline(
                            fechaCreacion = fechaCreacion,
                            usuarioNombre = usuarioNombre,
                            cambios = historialCambios
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        DetailRow("Descripción", descripcion)
                        DetailRow("Usuario", usuarioNombre)
                        DetailRow("Usuario ID", usuarioId)
                        DetailRow("Asignado a", asignadoA.ifBlank { "Sin asignar" })
                        DetailRow("Fecha creación", formatIncidentDate(fechaCreacion))
                        DetailRow("Última actualización", formatIncidentDate(fechaActualizacion))

                        Spacer(modifier = Modifier.height(14.dp))

                        Row {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Dirección",
                                tint = titleColor
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = direccion,
                                color = titleColor,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = secondaryCardColor)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Imágenes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (imagenes.isEmpty()) {
                            Text(
                                text = "No hay imágenes adjuntas.",
                                color = titleColor
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                imagenes.forEach { imageUrl ->
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Imagen incidencia",
                                        modifier = Modifier.size(110.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))



                Spacer(modifier = Modifier.height(12.dp))

                if (estado.equals("pendiente", ignoreCase = true)) {
                    Button(
                        onClick = { onEditClick(incidentId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar incidencia", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar incidencia", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                TextButton(onClick = onBackClick) {
                    Text("Volver", color = titleColor)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("incidencias")
                            .document(incidentId)
                            .delete()
                            .addOnSuccessListener {
                                showDeleteDialog = false
                                Toast.makeText(
                                    context,
                                    "Incidencia eliminada",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDeleteSuccess()
                            }
                            .addOnFailureListener { e ->
                                showDeleteDialog = false
                                Toast.makeText(
                                    context,
                                    "Error al eliminar incidencia: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Eliminar incidencia") },
            text = { Text("¿Seguro que quieres eliminar esta incidencia?") }
        )
    }
}

@Composable
fun PrettyHistoryTimeline(
    fechaCreacion: Timestamp?,
    usuarioNombre: String,
    cambios: List<HistoryChange>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PrettyTimelineItem(
            title = "Incidencia creada",
            subtitle = "Registro inicial",
            date = fechaCreacion,
            description = "Creada por ${usuarioNombre.ifBlank { "usuario" }}",
            isLast = cambios.isEmpty(),
            isCreation = true
        )

        cambios.forEachIndexed { index, cambio ->
            val campo = formatHistoryField(cambio.campo)
            val anterior = formatHistoryValue(cambio.anterior)
            val nuevo = formatHistoryValue(cambio.nuevo)

            PrettyTimelineItem(
                title = "$campo actualizado",
                subtitle = "$anterior → $nuevo",
                date = cambio.fecha,
                description = "Cambio realizado por ${cambio.usuarioNombre.ifBlank { "usuario" }}",
                isLast = index == cambios.lastIndex,
                isCreation = false
            )
        }
    }
}

@Composable
fun PrettyTimelineItem(
    title: String,
    subtitle: String,
    date: Timestamp?,
    description: String,
    isLast: Boolean,
    isCreation: Boolean
) {
    val accentColor = Color(0xFFC97F61)
    val darkColor = Color(0xFF4F3A33)
    val lineColor = Color(0xFF6E5447)
    val softCard = Color(0xFFF7F2F2)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(34.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isCreation) darkColor else accentColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCreation) Icons.Default.History else Icons.Default.SwapHoriz,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(4.dp)
                        .height(76.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 5f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = softCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = darkColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Fecha",
                        tint = darkColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatIncidentDate(date),
                    color = darkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}


@Composable
fun DetailRow(label: String, value: String) {
    val titleColor = Color(0xFF4F3A33)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = titleColor
        )
    }
}

@Composable
fun StatusBadgeDetail(status: String) {
    val normalized = status.lowercase()

    val (bgColor, textColor, icon) = when (normalized) {
        "pendiente", "abierta" -> Triple(
            Color(0xFFFFE0B2),
            Color(0xFF7A4B00),
            Icons.Default.WarningAmber
        )
        "proceso", "en_proceso", "en proceso" -> Triple(
            Color(0xFFBBDEFB),
            Color(0xFF0D47A1),
            Icons.Default.Build
        )
        "resuelta", "finalizada", "cerrada" -> Triple(
            Color(0xFFC8E6C9),
            Color(0xFF1B5E20),
            Icons.Default.CheckCircle
        )
        else -> Triple(
            Color(0xFFE0E0E0),
            Color(0xFF424242),
            Icons.Default.WarningAmber
        )
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = textColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatStatusName(status),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}