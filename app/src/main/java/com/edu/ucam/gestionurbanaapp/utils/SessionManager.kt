package com.edu.ucam.gestionurbanaapp.utils

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun setRememberSession(remember: Boolean) {
        prefs.edit().putBoolean("remember_session", remember).apply()
    }

    fun isSessionRemembered(): Boolean {
        return prefs.getBoolean("remember_session", false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}