package com.example

import com.example.data.model.BLOG_CATEGORIES
import com.example.data.model.RiskLevel
import com.example.data.model.SampleBlogs
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sampleBlogsCoverAllNineRequiredCategories() {
        val sampleCats = SampleBlogs.articles.map { it.category }.toSet()
        assertEquals(9, BLOG_CATEGORIES.size)
        BLOG_CATEGORIES.forEach { category ->
            assertTrue("Missing category $category", category in sampleCats)
        }
        assertTrue(SampleBlogs.articles.all { it.sampleContent })
    }

    @Test
    fun riskAndRoleEnumParsingFallsBackSafely() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromWire("LOW"))
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromWire("UNKNOWN"))
        assertEquals(UserRole.ADMIN, UserRole.fromWire("ADMIN"))
        assertEquals(UserRole.STUDENT, UserRole.fromWire("INVALID"))
    }
}
