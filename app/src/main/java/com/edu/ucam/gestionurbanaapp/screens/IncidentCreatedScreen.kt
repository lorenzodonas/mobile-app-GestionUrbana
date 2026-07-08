package com.edu.ucam.gestionurbanaapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IncidentCreatedScreen(
    incidentId: String = "INC-2026-001",
    title: String = "Farola rota",
    description: String = "La farola no funciona correctamente desde hace varios días.",
    location: String = "Calle Mayor 12, Granada",
    photoCount: Int = 2,
    date: String = "18/03/2026",
    onViewIncidentsClick: () -> Unit = {},
    onCreateAnotherClick: () -> Unit = {},
    onGoHomeClick: () -> Unit = {}
) {
    val backgroundColor = Color(0xFFEAE4E4)
    val cardColor = Color(0xFFD3BFA8)
    val borderColor = Color(0xFF6E5447)
    val titleColor = Color(0xFF4F3A33)
    val buttonColor = Color(0xFFC97F61)
    val secondaryCardColor = Color(0xFFF7F2F2)
    val successColor = Color(0xFF2E7D32)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Incidencia creada",
                        tint = successColor,
                        modifier = Modifier.height(72.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Incidencia creada correctamente",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tu incidencia ha sido registrada con éxito.",
                        fontSize = 15.sp,
                        color = titleColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = secondaryCardColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            InfoRow(label = "ID de incidencia", value = incidentId)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            InfoRow(label = "Título", value = title)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            InfoRow(label = "Descripción", value = description)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            InfoRow(label = "Ubicación", value = location)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            InfoRow(label = "Fotos adjuntas", value = photoCount.toString())
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            InfoRow(label = "Fecha", value = date)
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Button(
                        onClick = onViewIncidentsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "Ver incidencias",
                            tint = Color.Black
                        )

                        Spacer(modifier = Modifier.height(0.dp))

                        Text(
                            text = "Ver mis incidencias",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCreateAnotherClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Crear otra",
                            tint = Color.Black
                        )

                        Text(
                            text = "Crear otra incidencia",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onGoHomeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Volver al inicio",
                            tint = Color.Black
                        )

                        Text(
                            text = "Volver al inicio",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF4F3A33)
        )
    }
}