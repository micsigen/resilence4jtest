package com.example.demo.runnable

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.RuntimeException

@DisplayName("Function Retry Test ")
@ExtendWith(MockitoExtension::class)
class FunctionRetryTest {

    @Mock
    private val service: RemoteService? = null

    @Test
    fun whenRetryIsUsedThenItWorksAsExpected() {
        val config = RetryConfig
            .custom<Any>()
            .maxAttempts(2)
            .build()
        val registry = RetryRegistry.of(config)
        val retry = registry.retry("my")
        val decoratedFunction = Retry.decorateFunction(retry) { s: Int? ->
            service?.process(s)
            null
        }
        BDDMockito.willThrow(RuntimeException::class.java).given(service)?.process(anyInt())

        try {
            decoratedFunction.apply(1)
            fail("Expected an exception to be thrown if all retries failed")
        } catch (e: Exception) {
            verify(service, times(2))?.process(anyInt())
        }
    }
}