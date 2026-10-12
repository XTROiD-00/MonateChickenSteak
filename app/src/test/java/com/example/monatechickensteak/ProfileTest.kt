package com.example.monatechickensteak

import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.util.LoyaltyRules
import com.example.monatechickensteak.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Tests offline-safe settings helpers; profile writes use the configured REST API. */
class ProfileTest {
    @Test fun profileNameValidationWorks() {
        assertNull(Validators.nameError("New Name"))
        assertNotNull(Validators.nameError(" "))
    }

    @Test fun passwordChangeFieldsAreValidated() {
        assertNotNull(Validators.loginPasswordError(""))
        assertNull(Validators.newPasswordError("Newpass@123"))
        assertNotNull(Validators.confirmPasswordError("Newpass@123", "not-the-same"))
    }

    @Test fun loyaltyPointsCanBeAddedAndRedeemedLocally() {
        val email = "loyalty-${System.nanoTime()}@monate.co.za"
        assertEquals(0, AuthRepository.loyaltyPoints(email))
        AuthRepository.addLoyaltyPoints(email, 120)
        assertEquals(120, AuthRepository.loyaltyPoints(email))
        assertFalse(AuthRepository.redeemPoints(email, 200))
        assertTrue(AuthRepository.redeemPoints(email, 50))
        assertEquals(70, AuthRepository.loyaltyPoints(email))
    }

    @Test fun loyaltyRulesCalculatePoints() {
        assertEquals(12, LoyaltyRules.pointsForOrder(125.0))
    }
}
