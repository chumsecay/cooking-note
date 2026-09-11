package com.cookingnote.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class Base64EmpiricalTest {

    @Test
    fun javaUtilBase64_encodesDeterministicallyOnJvmWithoutStubs() {
        val sampleBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        val encoded = Base64.getEncoder().encodeToString(sampleBytes)
        assertEquals("/9j/", encoded.take(4))
        val decoded = Base64.getDecoder().decode(encoded)
        assertTrue(sampleBytes.contentEquals(decoded))
    }

    @Test
    fun javaUtilBase64_doesNotContainLinebreaksEvenForLargeBuffers() {
        // android.util.Base64.DEFAULT inserts newlines every 76 chars, while java.util.Base64 RFC 4648 does not.
        // In data URLs ("data:image/jpeg;base64,..."), newlines break HTTP JSON schemas.
        val largeImageBytes = ByteArray(4096) { it.toByte() }
        val encoded = Base64.getEncoder().encodeToString(largeImageBytes)

        assertFalse("Base64 string must not contain carriage return", encoded.contains("\r"))
        assertFalse("Base64 string must not contain newline", encoded.contains("\n"))
        assertEquals(
            "Base64 length should match standard 4 * ceil(n / 3)",
            (4 * Math.ceil(largeImageBytes.size / 3.0)).toInt(),
            encoded.length
        )
    }

    @Test
    fun javaUtilBase64_handlesEmptyAndBoundarySizes() {
        val emptyBytes = ByteArray(0)
        assertEquals("", Base64.getEncoder().encodeToString(emptyBytes))

        val oneByte = byteArrayOf(65) // 'A' -> "QQ=="
        assertEquals("QQ==", Base64.getEncoder().encodeToString(oneByte))

        val twoBytes = byteArrayOf(65, 66) // "AB" -> "QUI="
        assertEquals("QUI=", Base64.getEncoder().encodeToString(twoBytes))

        val threeBytes = byteArrayOf(65, 66, 67) // "ABC" -> "QUJD"
        assertEquals("QUJD", Base64.getEncoder().encodeToString(threeBytes))
    }
}
