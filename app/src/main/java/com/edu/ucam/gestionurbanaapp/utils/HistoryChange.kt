package com.edu.ucam.gestionurbanaapp.utils

import com.google.firebase.Timestamp

data class HistoryChange(
    val campo: String = "",
    val anterior: String = "",
    val nuevo: String = "",
    val fecha: Timestamp? = null,
    val usuarioNombre: String = ""
)