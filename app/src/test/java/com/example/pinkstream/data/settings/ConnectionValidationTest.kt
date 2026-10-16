package com.example.pinkstream.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectionValidationTest {

    @Test
    fun acceptsPlainAddresses() {
        assertNull(ConnectionValidation.hostError("127.0.0.1"))
        assertNull(ConnectionValidation.hostError(" 192.168.1.20 "))
        assertNull(ConnectionValidation.hostError("gaming-pc.local"))
    }

    @Test
    fun rejectsEmptyAndUrlLikeAddresses() {
        assertEquals("Enter your PC's address.", ConnectionValidation.hostError("   "))
        assertNotNull(ConnectionValidation.hostError("ws://192.168.1.20:8080/"))
        assertNotNull(ConnectionValidation.hostError("192.168.1.20/"))
        assertNotNull(ConnectionValidation.hostError("my pc"))
    }

    @Test
    fun portMustBeOneTo65535() {
        assertEquals(1, ConnectionValidation.parsePort("1"))
        assertEquals(65535, ConnectionValidation.parsePort("65535"))
        assertNull(ConnectionValidation.parsePort("0"))
        assertNull(ConnectionValidation.parsePort("65536"))
        assertNull(ConnectionValidation.parsePort(""))
        assertNull(ConnectionValidation.portError("8080"))
        assertNotNull(ConnectionValidation.portError("99999"))
    }
}
