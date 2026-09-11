package com.cookingnote.app

import android.os.Bundle
import android.util.Log
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

interface DummyService {
    fun compute(value: Int): String
}

/**
 * Adversarial empirical verification test suite for Milestone 1.
 *
 * Verifies:
 * 1. JUnit 4 runtime and assertions.
 * 2. Kotlinx Coroutines Test (1.9.0) with virtual time scheduling and dispatchers.
 * 3. Turbine (1.2.0) flow testing with item assertion and flow completion.
 * 4. MockK (1.13.13) bytecode mocking, stubbing, and verification.
 * 5. Lifecycle Runtime Compose (2.8.7) class availability (FlowExtKt).
 * 6. unitTests.isReturnDefaultValues = true (android.util.Log and android.os.Bundle unmocked calls).
 * 7. RFC 4648 Base64 encoding conformance for vision API payloads.
 */
class Milestone1AdversarialVerificationTest {

    @Test
    fun verifyJunit4_basicAssertionExecution() {
        assertEquals("JUnit 4 must execute correctly", 4, 2 + 2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun verifyKotlinxCoroutinesTest_virtualTimeExecution() = runTest {
        var executed = false
        val job = launch {
            delay(10_000)
            executed = true
        }
        assertFalse("Should not have executed before time advance", executed)
        advanceTimeBy(10_001)
        assertTrue("Should have executed after virtual time advance", executed)
        job.cancel()
    }

    @Test
    fun verifyTurbine_flowTestingExecution() = runTest {
        val flow = flowOf("ingredient_1", "ingredient_2", "ingredient_3")
        flow.test {
            assertEquals("ingredient_1", awaitItem())
            assertEquals("ingredient_2", awaitItem())
            assertEquals("ingredient_3", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun verifyMockk_mockingAndVerification() {
        val mock = mockk<DummyService>()
        every { mock.compute(42) } returns "result_42"

        val result = mock.compute(42)
        assertEquals("result_42", result)
        verify(exactly = 1) { mock.compute(42) }
    }

    @Test
    fun verifyLifecycleRuntimeCompose_classIsAvailableOnClasspath() {
        // androidx-lifecycle-runtime-compose provides FlowExtKt with collectAsStateWithLifecycle
        val clazz = Class.forName("androidx.lifecycle.compose.FlowExtKt")
        assertNotNull("FlowExtKt from androidx.lifecycle.compose must be resolvable", clazz)
    }

    @Test
    fun verifyTestOptions_isReturnDefaultValues_allowsAndroidLogWithoutCrash() {
        // When isReturnDefaultValues = true, Log.d/w/e returns 0 instead of throwing RuntimeException
        val logResult = Log.d("ChallengerTag", "Testing unmocked Android Log")
        assertEquals("Unmocked Log.d should return default int 0", 0, logResult)

        val logWarnResult = Log.w("ChallengerTag", "Testing unmocked Android Log.w")
        assertEquals("Unmocked Log.w should return default int 0", 0, logWarnResult)
    }

    @Test
    fun verifyTestOptions_isReturnDefaultValues_allowsAndroidBundleWithoutCrash() {
        // Verifies Android framework classes don't throw "Method ... not mocked"
        val bundle = Bundle()
        assertNotNull("Bundle instantiation should return default object stub", bundle)
        assertEquals("getString on unmocked bundle returns default null", null, bundle.getString("any_key"))
    }

    @Test
    fun verifyBase64_conformanceForLargeVisionPayloads() {
        val largePayload = ByteArray(1024 * 512) { (it % 256).toByte() }
        val encoded = Base64.getEncoder().encodeToString(largePayload)
        assertFalse("Payload must not contain newlines", encoded.contains('\n'))
        assertFalse("Payload must not contain carriage returns", encoded.contains('\r'))
        assertTrue("Encoded payload must have non-zero length", encoded.isNotEmpty())
    }
}
