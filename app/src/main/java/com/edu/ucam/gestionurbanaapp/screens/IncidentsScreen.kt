package com.edu.ucam.gestionurbanaapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

data class IncidentUi(
    val id: String,
    val title: String,
    val description: String,
    val address: String,
    val date: Timestamp?,
    val status: String,
    val priority: String,
    val userId: String,
    val userName: String,
    val imageCount: Int = 0
)

@Composable
fun IncidentsScreen(
    onIncidentClick: (String) -> Unit = {},
    onEditIncidentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    val currentUid = auth.currentUser?.uid

    val incidents = remember { mutableStateListOf<IncidentUi>() }

    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var expanded by remember { mutableStateOf(false) }
    var incidentToDelete by remember { mutableStateOf<IncidentUi?>(null) }

    val filterOptions = listOf("Todas", "pendiente", "en_proceso", "resuelta")

    LaunchedEffect(currentUid) {
        if (currentUid == null) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("incidencias")
            .whereEqualTo("usuarioId", currentUid)
            .get()
            .addOnSuccessListener { result ->
                incidents.clear()

                val loaded = result.documents.map { doc ->
                    IncidentUi(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("titulo") ?: "",
                        description = doc.getString("descripcion") ?: "",
                        address = doc.getString("direccion") ?: "",
                        date = doc.getTimestamp("fechaCreacion"),
                        status = doc.getString("estado") ?: "pendiente",
                        priority = doc.getString("prioridad") ?: "media",
                        userId = doc.getString("usuarioId") ?: "",
                        userName = doc.getString("usuarioNombre") ?: "",
                        imageCount = (doc.get("imagenes") as? List<*>)?.size ?: 0
                    )
                }.sortedByDescending { it.date?.seconds ?: 0L }

                incidents.addAll(loaded)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Error al cargar incidencias", Toast.LENGTH_LONG).show()
            }
    }

    val filteredIncidents = incidents.filter { incident ->
        val matchesSearch =
            incident.title.contains(searchText, ignoreCase = true) ||
                    incident.description.contains(searchText, ignoreCase = true) ||
                    incident.address.contains(searchText, ignoreCase = true)

        val matchesFilter =
            selectedFilter == "Todas" || incident.status.equals(selectedFilter, ignoreCase = true)

        matchesSearch && matchesFilter
    }

    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val textFieldColor = Color(0xFFF7F2F2)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
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
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Mis incidencias",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Consulta, busca y filtra el estado de tus incidencias.",
                        fontSize = 14.sp,
                        color = titleColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por título, descripción o dirección") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = textFieldColor,
                            unfocusedContainerColor = textFieldColor,
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box {
                        AssistChip(
                            onClick = { expanded = true },
                            label = { Text("Filtro: $selectedFilter") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtro"
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = textFieldColor,
                                labelColor = titleColor
                            )
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedFilter = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filteredIncidents.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2F2))
                    ) {
                        Text(
                            text = "No hay incidencias que coincidan con la búsqueda o el filtro.",
                            modifier = Modifier.padding(16.dp),
                            color = titleColor
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Resultados: ${filteredIncidents.size}",
                        color = titleColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredIncidents, key = { it.id }) { incident ->
                            IncidentFirebaseCard(
                                incident = incident,
                                onClick = { onIncidentClick(incident.id) },
                                onEditClick = { onEditIncidentClick(incident.id) },
                                onDeleteClick = { incidentToDelete = incident }
                            )
                        }
                    }
                }
            }
        }
    }

    if (incidentToDelete != null) {
        AlertDialog(
            onDismissRequest = { incidentToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = incidentToDelete?.id ?: return@TextButton

                        db.collection("incidencias")
                            .document(idToDelete)
                            .delete()
                            .addOnSuccessListener {
                                incidents.removeAll { it.id == idToDelete }
                                Toast.makeText(context, "Incidencia eliminada", Toast.LENGTH_SHORT).show()
                                incidentToDelete = null
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar incidencia", Toast.LENGTH_LONG).show()
                                incidentToDelete = null
                            }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { incidentToDelete = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Eliminar incidencia") },
            text = { Text("¿Seguro que quieres eliminar esta incidencia?") }
        )
    }
}

@Composable
fun IncidentFirebaseCard(
    incident: IncidentUi,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val titleColor = Color(0xFF4F3A33)
    val cardBackground = Color(0xFFF7F2F2)
    val canEditOrDelete = incident.status.equals("pendiente", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = incident.title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadgeFirebase(status = incident.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = incident.description,
                fontSize = 14.sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = titleColor
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = incident.address,
                    color = titleColor,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fecha: ${formatTimestamp(incident.date)}",
                fontSize = 13.sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Prioridad: ${incident.priority}",
                fontSize = 13.sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Imágenes: ${incident.imageCount}",
                fontSize = 13.sp,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Ver detalle",
                        tint = titleColor
                    )
                }

                if (canEditOrDelete) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = titleColor
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadgeFirebase(status: String) {
    val normalized = status.lowercase()

    val (bgColor, textColor, icon) = when (normalized) {
        "pendiente" -> Triple(
            Color(0xFFFFE0B2),
            Color(0xFF7A4B00),
            Icons.Default.WarningAmber
        )
        "en_proceso", "en proceso" -> Triple(
            Color(0xFFBBDEFB),
            Color(0xFF0D47A1),
            Icons.Default.Build
        )
        else -> Triple(
            Color(0xFFC8E6C9),
            Color(0xFF1B5E20),
            Icons.Default.CheckCircle
        )
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = textColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Sin fecha"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}