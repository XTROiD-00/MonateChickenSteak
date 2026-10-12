package com.example.monatechickensteak.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Passwords are never stored as plain text. We mix in a random "salt" and
 * scramble with PBKDF2 so the same password gives a different result per user.
 */
object PasswordHasher {

    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.hexToBytes(), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return factory.generateSecret(spec).encoded.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}