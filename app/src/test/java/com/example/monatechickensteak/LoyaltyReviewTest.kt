package com.example.monatechickensteak

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.ReviewRepository
import com.example.monatechickensteak.util.LoyaltyRules
import com.example.monatechickensteak.util.ReviewValidator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoyaltyRulesTest {

    @Test fun onePointForEveryTenRand() = assertEquals(12, LoyaltyRules.pointsForOrder(125.0))

    @Test fun tiersChangeAtTheThresholds() {
        assertEquals(LoyaltyRules.Tier.BRONZE, LoyaltyRules.tierFor(99))
        assertEquals(LoyaltyRules.Tier.SILVER, LoyaltyRules.tierFor(100))
        assertEquals(LoyaltyRules.Tier.GOLD, LoyaltyRules.tierFor(300))
    }

    @Test fun bronzeHasSilverAsNextTier() =
        assertEquals(LoyaltyRules.Tier.SILVER, LoyaltyRules.nextTier(10))

    @Test fun goldHasNoNextTier() = assertNull(LoyaltyRules.nextTier(500))
}

class ReviewValidatorTest {

    @Test fun ratingIsRequired() = assertNotNull(ReviewValidator.ratingError(0))

    @Test fun shortCommentIsRejected() = assertNotNull(ReviewValidator.commentError("ok"))

    @Test fun goodReviewPasses() {
        assertNull(ReviewValidator.itemError("Chips"))
        assertNull(ReviewValidator.ratingError(4))
        assertNull(ReviewValidator.commentError("Really tasty and hot."))
    }
}

class ReviewRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        ReviewRepository.reset()
    }

    @Test fun newestReviewComesFirst() {
        ReviewRepository.add("Chips", 4, "Nice and crispy", "Thabi")
        val second = ReviewRepository.add("Rump 300g", 5, "Perfectly cooked", "Thabi")
        assertEquals(second.id, ReviewRepository.reviews.value!!.first().id)
    }

    @Test fun averageRatingIsCalculated() {
        ReviewRepository.add("Chips", 5, "Great chips here", "A")
        ReviewRepository.add("Chips", 4, "Pretty good chips", "B")
        assertEquals(4.5, ReviewRepository.averageRating(), 0.001)
    }
}

class LoyaltyPointsAccountTest {

    @Test fun pointsAreAddedToAnAccount() = runTest {
        AuthRepository.register("Points One", "points.one@monate.co.za", "Monate@123")
        AuthRepository.addLoyaltyPoints("points.one@monate.co.za", 30)
        assertEquals(30, AuthRepository.loyaltyPoints("points.one@monate.co.za"))
    }

    @Test fun redeemingDeductsThePoints() = runTest {
        AuthRepository.register("Points Two", "points.two@monate.co.za", "Monate@123")
        AuthRepository.addLoyaltyPoints("points.two@monate.co.za", 60)
        assertTrue(AuthRepository.redeemPoints("points.two@monate.co.za", 50))
        assertEquals(10, AuthRepository.loyaltyPoints("points.two@monate.co.za"))
    }

    @Test fun redeemingWithTooFewPointsFails() = runTest {
        AuthRepository.register("Points Three", "points.three@monate.co.za", "Monate@123")
        assertFalse(AuthRepository.redeemPoints("points.three@monate.co.za", 50))
        assertEquals(0, AuthRepository.loyaltyPoints("points.three@monate.co.za"))
    }
}