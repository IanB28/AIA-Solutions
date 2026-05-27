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
    fun `extractBusinessId keeps direct id value`() {
        assertEquals("business789", extractBusinessId("business789"))
    }
}
