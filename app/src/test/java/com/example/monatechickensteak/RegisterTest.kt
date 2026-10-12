package com.example.monatechickensteak

import com.example.monatechickensteak.util.Validators
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Authentication now requires the configured REST API; unit tests cover client-side validation. */
class RegisterTest {
    @Test fun validRegistrationDetailsPassValidation() {
        assertNull(Validators.nameError("Thabi M"))
        assertNull(Validators.emailError("thabi.test@monate.co.za"))
        assertNull(Validators.newPasswordError("Monate@123"))
        assertNull(Validators.confirmPasswordError("Monate@123", "Monate@123"))
    }

    @Test fun invalidRegistrationDetailsAreRejected() {
        assertNotNull(Validators.nameError(" "))
        assertNotNull(Validators.emailError("not-an-email"))
        assertNotNull(Validators.newPasswordError("weak"))
        assertNotNull(Validators.confirmPasswordError("Monate@123", "different"))
    }
}
