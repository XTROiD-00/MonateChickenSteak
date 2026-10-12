package com.example.monatechickensteak

import com.example.monatechickensteak.data.MockData
import com.example.monatechickensteak.util.MenuFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class MenuFilterTest {

    @Test fun allCategoryReturnsEveryItem() {
        assertEquals(12, MenuFilter.filter(MockData.menu, "All", "").size)
    }

    @Test fun categoryFilterOnlyReturnsThatCategory() {
        val drinks = MenuFilter.filter(MockData.menu, "Drinks", "")
        assertEquals(3, drinks.size)
        assert(drinks.all { it.category == "Drinks" })
    }

    @Test fun searchIsCaseInsensitive() {
        val result = MenuFilter.filter(MockData.menu, "All", "SPRITE")
        assertEquals(1, result.size)
        assertEquals("Sprite 330ml", result.first().name)
    }

    @Test fun searchMatchesDescriptionsToo() {
        // "Quarter Chicken" and "Chicken Wrap" both mention chicken
        assertEquals(2, MenuFilter.filter(MockData.menu, "All", "chicken").size)
    }

    @Test fun noMatchGivesEmptyList() {
        assertEquals(0, MenuFilter.filter(MockData.menu, "All", "zzzz").size)
    }

    @Test fun categoryAndSearchCombine() {
        assertEquals(0, MenuFilter.filter(MockData.menu, "Steak", "sprite").size)
    }
}