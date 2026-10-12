package com.example.monatechickensteak.data

import com.example.monatechickensteak.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.google.gson.JsonParser

sealed class AuthResult {
    data class Success(val user: User, val token: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** Authentication is server-backed. The API server owns password hashing and user records. */
object AuthRepository {
    private val localPoints = mutableMapOf<String, Int>()

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.login(AuthRequest(email = email.trim(), password = password))
            if (response.success && response.user != null && !response.token.isNullOrBlank()) {
                AuthResult.Success(response.user.toUser(), response.token)
            } else AuthResult.Error(response.message ?: "Unable to sign in")
        } catch (e: Exception) { AuthResult.Error(networkMessage(e)) }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.register(AuthRequest(name = name.trim(), email = email.trim(), password = password))
            if (response.success && response.user != null && !response.token.isNullOrBlank()) {
                AuthResult.Success(response.user.toUser(), response.token)
            } else AuthResult.Error(response.message ?: "Unable to create account")
        } catch (e: Exception) { AuthResult.Error(networkMessage(e)) }
    }

    suspend fun googleLogin(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.googleLogin(GoogleAuthRequest(idToken))
            if (response.success && response.user != null && !response.token.isNullOrBlank()) {
                AuthResult.Success(response.user.toUser(), response.token)
            } else AuthResult.Error(response.message ?: "Google sign-in failed")
        } catch (e: Exception) { AuthResult.Error(networkMessage(e)) }
    }

    suspend fun updateName(email: String, newName: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.apiToken()
            if (token.isBlank()) return@withContext AuthResult.Error("Please sign in again to update your profile")
            val response = ApiClient.service.updateProfile("Bearer $token", ProfileUpdateRequest(newName.trim()))
            if (response.success && response.user != null) AuthResult.Success(response.user.toUser())
            else AuthResult.Error(response.message ?: "Could not update profile")
        } catch (e: Exception) { AuthResult.Error(networkMessage(e)) }
    }

    suspend fun changePassword(email: String, currentPassword: String, newPassword: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.apiToken()
            if (token.isBlank()) return@withContext AuthResult.Error("Please sign in again to change your password")
            val response = ApiClient.service.changePassword("Bearer $token", ChangePasswordRequest(currentPassword, newPassword))
            if (response.success) AuthResult.Success(response.user?.toUser() ?: User(0, SessionManager.userName(), email))
            else AuthResult.Error(response.message ?: "Could not change password")
        } catch (e: Exception) { AuthResult.Error(networkMessage(e)) }
    }

    fun loyaltyPoints(email: String): Int = localPoints[email.lowercase()] ?: 0
    fun addLoyaltyPoints(email: String, points: Int) {
        if (points > 0) localPoints[email.lowercase()] = loyaltyPoints(email) + points
    }
    fun redeemPoints(email: String, cost: Int): Boolean {
        val points = loyaltyPoints(email)
        if (cost <= 0 || points < cost) return false
        localPoints[email.lowercase()] = points - cost
        return true
    }

    private fun networkMessage(e: Exception): String = when (e) {
        is java.net.UnknownHostException, is java.net.ConnectException -> "Can't reach the Monate API. Check the API URL and that the server is running."
        is HttpException -> {
            try {
                val raw = e.response()?.errorBody()?.string().orEmpty()
                @Suppress("DEPRECATION")
                JsonParser().parse(raw).asJsonObject.get("message")?.asString ?: "Request failed (${e.code()})"
            } catch (_: Exception) { "Request failed (${e.code()})" }
        }
        else -> e.localizedMessage ?: "A network error occurred"
    }
}
