package com.example.monatechickensteak

import com.example.monatechickensteak.util.PasswordHasher
import com.example.monatechickensteak.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun blankEmailGivesError() = assertNotNull(Validators.emailError(""))

    @Test fun malformedEmailGivesError() = assertNotNull(Validators.emailError("abc@"))

    @Test fun validEmailPasses() = assertNull(Validators.emailError("demo@monate.co.za"))

    @Test fun shortPasswordRejected() = assertNotNull(Validators.newPasswordError("Ab1@"))

    @Test fun passwordWithoutSpecialCharRejected() =
        assertNotNull(Validators.newPasswordError("Monate1234"))

    @Test fun strongPasswordAccepted() = assertNull(Validators.newPasswordError("Monate@123"))

    @Test fun mismatchedConfirmationRejected() =
        assertNotNull(Validators.confirmPasswordError("Monate@123", "Monate@124"))

    @Test fun loginRequiresAPassword() = assertNotNull(Validators.loginPasswordError(""))
}

class PasswordHasherTest {

    @Test fun samePasswordAndSaltGiveSameHash() {
        val salt = PasswordHasher.newSalt()
        assertEquals(
            PasswordHasher.hash("Monate@123", salt),
            PasswordHasher.hash("Monate@123", salt)
        )
    }

    @Test fun differentSaltGivesDifferentHash() {
        val h1 = PasswordHasher.hash("Monate@123", PasswordHasher.newSalt())
        val h2 = PasswordHasher.hash("Monate@123", PasswordHasher.newSalt())
        assertNotEquals(h1, h2)
    }

    @Test fun hashIsNotThePlainPassword() {
        val hash = PasswordHasher.hash("Monate@123", PasswordHasher.newSalt())
        assertNotEquals("Monate@123", hash)
    }
}