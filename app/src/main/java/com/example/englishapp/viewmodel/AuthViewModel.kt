package com.example.englishapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    fun register(context: Context, name: String, pin: String, confirmPin: String): String? {
        if (name.trim().length < 2) return "Explorer name must be at least 2 characters."
        if (pin.length < 4 || !pin.all(Char::isDigit)) return "PIN must contain at least 4 numbers."
        if (pin != confirmPin) return "Your PINs don't match."
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, name.trim())
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_SIGNED_IN, true)
            .apply()
        return null
    }

    fun login(context: Context, name: String, pin: String): String? {
        if (name.isBlank() || pin.isBlank()) return "Enter your explorer name and PIN."
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val savedName = preferences.getString(KEY_NAME, null)
        val savedPin = preferences.getString(KEY_PIN, null)
        if (savedName == null || savedPin == null) return "Create an explorer account first."
        if (!savedName.equals(name.trim(), ignoreCase = true) || savedPin != pin) {
            return "That name or PIN doesn't match."
        }
        preferences.edit().putBoolean(KEY_SIGNED_IN, true).apply()
        return null
    }

    fun savedExplorerName(context: Context): String? {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(KEY_SIGNED_IN, false)) return null
        return preferences.getString(KEY_NAME, null)
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SIGNED_IN, false)
            .apply()
    }

    companion object {
        private const val PREFERENCES = "kids_english_auth"
        private const val KEY_NAME = "explorer_name"
        private const val KEY_PIN = "explorer_pin"
        private const val KEY_SIGNED_IN = "signed_in"
    }
}
