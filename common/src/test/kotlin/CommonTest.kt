package me.baldo3000.common

import kotlin.test.Test
import kotlin.test.assertEquals

internal class BasicTest {

    @Test
    fun testMessage() {
        val message = "message"
        assertEquals("message", message)
    }
}