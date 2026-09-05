package com.pesatrack.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LockTimeoutTest {

    @Test
    fun `fromMillis round-trips every entry's own millis value`() {
        LockTimeout.entries.forEach { timeout ->
            assertEquals(timeout, LockTimeout.fromMillis(timeout.millis))
        }
    }

    @Test
    fun `fromMillis falls back to IMMEDIATE for an unrecognised value`() {
        assertEquals(LockTimeout.IMMEDIATE, LockTimeout.fromMillis(-1L))
        assertEquals(LockTimeout.IMMEDIATE, LockTimeout.fromMillis(12_345L))
    }
}
