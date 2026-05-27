package com.example.aia_solutions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QRScannerParserTest {

    @Test
    fun `extractBusinessId returns null for blank values`() {
        assertNull(extractBusinessId("   "))
    }

    @Test
    fun `extractBusinessId supports custom aia scheme`() {
        assertEquals("business123", extractBusinessId("aia://business/business123"))
    }

    @Test
    fun `extractBusinessId supports query parameter`() {
        assertEquals("business456", extractBusinessId("https://aia.app/join?businessId=business456"))
    }

    @Test
    fun `extractBusinessId supports id query parameter`() {
        assertEquals("business999", extractBusinessId("https://aia.app/join?id=business999"))
    }

    @Test
    fun `extractBusinessId supports path-based url`() {
        assertEquals("business123", extractBusinessId("https://aia.app/business/business123"))
    }

    @Test
    fun `extractBusinessId returns null for malformed business urls`() {
        assertNull(extractBusinessId("aia://business/"))
        assertNull(extractBusinessId("https://aia.app/business/"))
        assertNull(extractBusinessId("https://aia.app/some/deep/path/business123"))
    }

    @Test
    fun `extractBusinessId keeps direct id value`() {
        assertEquals("business789", extractBusinessId("business789"))
    }

    @Test
    fun `extractBusinessId rejects invalid direct values`() {
        assertNull(extractBusinessId("invalid id with spaces"))
    }
}
