package com.example.monatechickensteak.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Remembers who is logged in, stored in encrypted storage on the phone. */
object SessionManager {

    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_API_TOKEN = "api_token"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val app = context.applicationContext
        prefs = try {
            val masterKey = MasterKey.Builder(app)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                app,
                "monate_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            app.getSharedPreferences("monate_prefs", Context.MODE_PRIVATE)
        }
    }

    fun saveSession(name: String, email: String, token: String? = null) {
        prefs?.edit()
            ?.putBoolean(KEY_LOGGED_IN, true)
            ?.putString(KEY_NAME, name)
            ?.putString(KEY_EMAIL, email)
            ?.also { editor -> if (!token.isNullOrBlank()) editor.putString(KEY_API_TOKEN, token) }
            ?.apply()
    }

    fun isLoggedIn(): Boolean = prefs?.getBoolean(KEY_LOGGED_IN, false) ?: false

    fun userName(): String = prefs?.getString(KEY_NAME, "") ?: ""

    fun userEmail(): String = prefs?.getString(KEY_EMAIL, "") ?: ""

    fun apiToken(): String = prefs?.getString(KEY_API_TOKEN, "") ?: ""

    fun notificationsEnabled(): Boolean = prefs?.getBoolean(KEY_NOTIFICATIONS, true) ?: true

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_NOTIFICATIONS, enabled)?.apply()
    }

    fun logout() {
        // Keep the user's settings, clear who they are
        val notifications = notificationsEnabled()
        prefs?.edit()?.clear()?.putBoolean(KEY_NOTIFICATIONS, notifications)?.apply()
    }
}