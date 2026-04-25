package com.anmol.web_client_lib

import com.anmol.web_client_lib.security.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClaimsMapperTests {
    @Test
    fun `default claims mapper maps valid claims`() {
        val claims = mapOf("mobileNumber" to "1234567890", "role" to "customer")
        val mapper = DefaultClaimsMapper()
        val result = mapper.map(claims)
        assertEquals("1234567890", result.id)
        assertEquals(Role.CUSTOMER, result.role)
    }

    @Test
    fun `default claims mapper throws for missing mobileNumber`() {
        val claims = mapOf("role" to "customer")
        val mapper = DefaultClaimsMapper()
        assertThrows(IllegalArgumentException::class.java) {
            mapper.map(claims)
        }
    }

    @Test
    fun `default claims mapper throws for missing role`() {
        val claims = mapOf("mobileNumber" to "1234567890")
        val mapper = DefaultClaimsMapper()
        assertThrows(IllegalArgumentException::class.java) {
            mapper.map(claims)
        }
    }
}

