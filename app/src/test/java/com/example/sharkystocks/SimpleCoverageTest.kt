package com.example.sharkystocks

import org.junit.Test
import kotlin.test.assertTrue

class SimpleCoverageTest {
    @Test
    fun `test addition`() {
        assertTrue(1 + 1 == 2)
    }
    
    @Test
    fun `test string`() {
        val text = "Hello"
        assertTrue(text.length == 5)
    }
}
