package com.edu.ucam.gestionurbanaapp.model

import com.google.firebase.Timestamp

data class IncidentStatusHistory(
    val estado: String = "",
    val fecha: Timestamp? = null,
    val comentario: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = ""
)